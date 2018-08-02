package gov.usgs.ngwmn.model;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.text.ParseException;
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
import gov.usgs.wma.statistics.logic.StatisticsCalculator;
import gov.usgs.wma.statistics.model.Value;
import gov.usgs.ngwmn.logic.WaterlevelMediator;
import gov.usgs.ngwmn.logic.WaterlevelMediator.ValidationException;

public class WLSample extends Value {
	private static final Logger logger = LoggerFactory.getLogger(WLSample.class);
	
	public static final String PROVISIONAL_CODE = "P";

	public final BigDecimal originalValue;
	public final String units;
	public final String comment;
	public final Boolean up;
	public final String pcode;
	
	public final BigDecimal valueAboveDatum;
	
	private Boolean provisional; // most values are not provisional and we like the false default
	private boolean unknown;
	
	public WLSample(String time, BigDecimal value, String units,
			BigDecimal originalValue, String comment, Boolean up, String pcode, BigDecimal valueAboveDatum) {
		super(time, value);
		this.originalValue = originalValue;
		this.units = units;
		this.comment = comment;
		this.up = up;
		this.pcode = pcode;
		this.valueAboveDatum = valueAboveDatum;
	}
	// convenience constructor for use in storing a sample only in calculations
	public WLSample(BigDecimal value) {
		this(null,value,null,null,null,null,null,null);
	}
	
	
	
	public WLSample setProvsional(boolean isProvisional) {
		// set only once, then immutable like all other properties
		// why? because I did not want to change the constructor signature with another final value
		// it could disrupt the contract for other implementations.
		if (this.provisional == null) {
			this.provisional = isProvisional;
		}
		return this; // chaining
	}
	/**
	 * The default must be false so that agencies that do not provide this status do not have all values removed
	 * @return true if the sample is indicated with the provisional status
	 */
	public boolean isProvisional() {
		if (this.provisional == null) {
			return false;
		}
		return provisional;
	}

	public boolean isUnknown() {
		return unknown;
	}
	public void setUnknown(boolean unknown) {
		this.unknown = unknown;
	}
	
	@Override
	public String toString() {
		StringBuilder toString = new StringBuilder();
		toString.append("Time:").append(time).append("\n")
			.append("value:").append(isUnknown() ? UNKNOWN_VALUE : value).append("\n")
			.append("original_value:").append(originalValue).append("\n")
			.append("units:").append(units).append("\n")
			.append("comment:").append(comment).append("\n")
			.append("pcode:").append(pcode).append("\n")
			.append("up:").append(up).append("\n")
			.append("valueAboveDatum:").append(valueAboveDatum).append("\n");
		return toString.toString();
	}
	
	public static BigDecimal valueOf(WLSample sample) {
		if (sample == null) {
			return null;
		}
		return sample.value;
	}

	/**
	 *  sorts in descending order of value
	 */
	public static final Comparator<WLSample> DEPTH_BELOW_SURFACE_COMPARATOR = new Comparator<WLSample>() {
		public int compare(WLSample a, WLSample b) {
			if (bothAreKnown(a, b)) {
				return b.value.compareTo( a.value );
			}
			return compareUnknown(a, b);
		};
	};
	/**
	 *  sorts in ascending order of value
	 */
	public static final Comparator<WLSample> DEPTH_ABOVE_DATUM_COMPARATOR = new Comparator<WLSample>() {
		public int compare(WLSample a, WLSample b) {
			if (bothAreKnown(a, b)) {
				return a.value.compareTo( b.value );
			}
			return compareUnknown(a, b);
		};
	};
	
	public static final boolean bothAreKnown(WLSample a, WLSample b) {
		return ! a.isUnknown() && ! b.isUnknown();
	}
	
	public static final int compareUnknown(WLSample a, WLSample b) {
		if (a.isUnknown()) {
			if (b.isUnknown()) {
				return 0;
			}
			return -1;
		}
		if (b.isUnknown()) {
			return  1;
		}
		return 0;
	}
	
	/**
	 *  sorts in ascending order of date time
	 */
	public static final Comparator<WLSample> TIME_COMPARATOR = new Comparator<WLSample>() {
		public int compare(WLSample a, WLSample b) {
			return a.time.compareTo( b.time ); // ssdfg
		};
	};
	
	public String getTime() {
		return time;
	}
	public String getMonth() {
		return StatisticsCalculator.monthUTC(time);
	}
	public String getYear() {
		return StatisticsCalculator.yearUTC(time);
	}
	public BigDecimal getValue() {
		return value;
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
		logger.trace( "xml rows  " + elements.getLength() );
		
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
			
			sample.setUnknown( isUnknown(value) );
			// setting unknown values to provisional avoids statistics calculations since it purges them
			sample.setProvsional(isProvisional || sample.isUnknown() );
			samples.add(sample);
		}
		
		return samples;
	}
	
	public static boolean isUnknown(String value) {
		return UNKNOWN_VALUE.equalsIgnoreCase(value);
	}

	public static boolean checkBadValue(String value, int record, String mySiteId) {
		if (value == null) {
			logger.warn("Water Level Error - value:null record:{} site:{}", record, mySiteId);
			return true;
		} if ( isUnknown(value) ) {
			// unknown values are not "bad" values
			return false;
		} else {
			try {
				new BigDecimal(value);
			} catch (Exception e) {
				logger.warn("Water Level Error - value:'{}' record:{} site:{}", new Object[]{value, record, mySiteId});
				return true;
			}
		}
		return false;
	}


	public static boolean checkBadTime(String time, int record, String mySiteId) {
		if (time == null) {
			logger.warn("Water Level Error - time:null record:{} site:{}", record, mySiteId);
			return true;
		} else {
			int formatMatches = 0;
			try {
				DATE_FORMAT_YEAR.parse(time);
				formatMatches++;
				DATE_FORMAT_MONTH.parse(time);
				formatMatches++;
				DATE_FORMAT_FULL.parse(time);
				formatMatches++;
			} catch (ParseException e) {
				if (formatMatches == 0) {
					logger.warn("Water Level Error - time:'{}' record:{} site:{}", new Object[]{time, record, mySiteId});
					return true;
				}
			}
		}
		return false;
	}
}