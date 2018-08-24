package gov.usgs.ngwmn.model;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import gov.usgs.ngwmn.model.Elevation;
import gov.usgs.wma.statistics.model.Value;
import gov.usgs.ngwmn.logic.WaterlevelMediator;
import gov.usgs.ngwmn.logic.WaterlevelMediator.ValidationException;

public class WLSample extends Value {
	private static final Logger LOGGER = LoggerFactory.getLogger(WLSample.class);
	public static final Comparator<Value> DEPTH_BELOW_SURFACE_COMPARATOR = SORT_VALUE_DESCENDING;
	public static final Comparator<Value> DEPTH_ABOVE_DATUM_COMPARATOR = SORT_VALUE_ASCENDING;
	
	public final BigDecimal originalValue;
	public final String units;
	public final String comment;
	public final Boolean up;
	public final String pcode;
	
	public final BigDecimal valueAboveDatum;
	
	public WLSample(String time, BigDecimal value, String units, BigDecimal originalValue, 
			String comment, Boolean up, String pcode, BigDecimal valueAboveDatum) {
		super(time, value);
		this.originalValue = originalValue;
		this.units = units;
		this.comment = comment;
		this.up = up;
		this.pcode = pcode;
		this.valueAboveDatum = valueAboveDatum;
	}
	public WLSample(WLSample base) {
		this(base.time, base.value, base.units, base.originalValue,
				base.comment, base.up, base.pcode, base.valueAboveDatum);
	}
	public WLSample(BigDecimal value, BigDecimal valueAboveDatum, WLSample base) {
		this(base.time, value, base.units, value,
				base.comment, base.up, base.pcode, valueAboveDatum);
	}
	// convenience constructor for use in storing a sample only in calculations
	public WLSample(BigDecimal value) {
		this(null,value,null,null,null,null,null,null);
	}
	
	public static BigDecimal valueOfAboveDatum(WLSample sample) {
		if (sample == null) {
			return null;
		}
		return sample.valueAboveDatum;
	}

	@Override
	public String toString() {
		StringBuilder toString = new StringBuilder();
		toString.append(super.toString())
			.append("original_value:").append(originalValue).append("\n")
			.append("units:").append(units).append("\n")
			.append("comment:").append(comment).append("\n")
			.append("pcode:").append(pcode).append("\n")
			.append("up:").append(up).append("\n")
			.append("valueAboveDatum:").append(valueAboveDatum).append("\n");
		return toString.toString();
	}
	
		
	public static List<WLSample> extractSamples(Reader source, String agencyCd, String siteNo, Elevation elevation) throws IOException, ParserConfigurationException, SAXException {
		String mySiteId = agencyCd+":"+siteNo;
		// well surface elevation for mediated elevation
		String siteElevation = (elevation.value != null)?elevation.value.toString():null;
		
		// place holder list
		List<WLSample> samples = new ArrayList<>();
		
		// transform XML from CLOB stream to DOM
		StringBuilder xml = new StringBuilder();
		
		try ( BufferedReader reader = new BufferedReader(source) ) {
			String line = "";
			while ( (line=reader.readLine()) != null) {
				xml.append(line);
			}
		}
		
		InputStream is       = new ByteArrayInputStream(xml.toString().getBytes());
		DocumentBuilder doc  = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document    = doc.parse(is);

		// fetch out the time series list and prepare to parse to ORM POJO
		NodeList elements    = document.getElementsByTagName("wml2:TimeValuePair");
			samples              = new ArrayList<>(elements.getLength());
		LOGGER.trace( "xml rows  " + elements.getLength() );
		
		// transform each all rows
		for (int e=0; e<elements.getLength(); e++) {
			// FIRST, EXTRACT DATA FROM DOM
			
			// TODO I know this is more cumbersome than XPATH. XPATH stopped working
			// get current time series element
			Element el       = (Element) elements.item(e);
			// get the sample element for value and units
			Element quantity = (Element) ((Element) el.getElementsByTagName("wml2:value").item(0)).getElementsByTagName("swe:Quantity").item(0);
			String units     = quantity.getElementsByTagName("swe:uom").item(0).getAttributes().getNamedItem("code").getTextContent();
			String value     = quantity.getElementsByTagName("swe:value").item(0).getTextContent();
			String time      = el.getElementsByTagName("wml2:time").item(0).getTextContent();
			// get the comment text
			String comment   = el.getElementsByTagName("wml2:comment").item(0).getTextContent();
			// get the PCODE element if there is one
			NodeList nwis    = quantity.getElementsByTagName("gwdp:nwis");
			String pcode     = null;
			String direction = null;
			if (nwis.getLength() > 0) {
				NamedNodeMap attrs = nwis.item(0).getAttributes();
				pcode        = attrs.getNamedItem("pcode").getTextContent();
				direction    = attrs.getNamedItem("direction").getTextContent();
			}
			// get provisional status
			boolean isProvisional = false;
			NodeList status       =  el.getElementsByTagName("wml2:status");
			if (status.getLength() > 0) {
				String provisional= status.item(0).getTextContent();
				isProvisional     = PROVISIONAL_CODE.equals(provisional);
			}
			
			// SECOND, VALIDATE AND MEDIATE VALUES
			
			if ( checkBadValue(value, e, mySiteId) || checkBadTime(time, e, mySiteId) ) {
				continue;
			}

			// calculate the water levels based on well surface elevation
			BigDecimal mediatedValue = null;
			BigDecimal originalValue = null;
			BigDecimal valueAboveDatum = null;
			
			if ( ! isUnknown(value) ) {
				try {
					mediatedValue = WaterlevelMediator
						.mediateToDistanceBelowGroundLevel(value, units, pcode, null, siteElevation, elevation.datum);
				} catch (ValidationException ve) {
					//The WLSample only allows BigDecimal for the resulting values,
					//so our string explanation will need to be moved to the comment.
					comment+=  ((comment != null) ?": " :"") + ve.getMessage();
				}
				
				// added for NGWMN-1184 for statistics to mediate with most prevalent PCODE
				// not dry because of both value and comment side effects and only a single return value in java
				try {
					valueAboveDatum = WaterlevelMediator
						.mediateToDistanceAboveSiteDatum(value, units, pcode, null, siteElevation, elevation.datum);
				} catch (ValidationException ve) {
					//The WLSample only allows BigDecimal for the resulting values,
					//so our string explanation will need to be moved to the comment.
					comment+=  ((comment != null) ?": " :"") + ve.getMessage();
				}
				
				//Ignore: parsing issues on this will already be logged as a comment b/c
				//the same conversion will have failed in the mediate method above.
				 originalValue = WaterlevelMediator.toBigDecimal(value);
			}
			// THIRD, CONSTRUCT A VALID & MEDIATED INSTANCE
			
			// construct Water Level Sample POJO
			Boolean isUp = "up".equalsIgnoreCase(direction);
			WLSample sample = new WLSample(time, mediatedValue, units, originalValue, comment, isUp, pcode, valueAboveDatum);
			
			sample.setUnknown( isUnknown(value) ); // TODO make final
			// setting unknown values to provisional avoids statistics calculations since it purges them
			sample.setProvsional(isProvisional || sample.isUnknown() );
			samples.add(sample);
		}
		
		return samples;
	}
	

}