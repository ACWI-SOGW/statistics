package gov.usgs.ngwmn.logic;

import java.math.BigDecimal;

//import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.ngwmn.model.DepthDatum;
import gov.usgs.ngwmn.model.PCode;
import gov.usgs.ngwmn.model.Unit;
import gov.usgs.wma.statistics.logic.SigFigMathUtil;


public class WaterlevelMediator {

	private static Logger LOGGER = LoggerFactory.getLogger(WaterlevelMediator.class);
	
	// Conversion not possible because of missing or unrecognized data
	public static final String NA_PREFIX = "NA";	// All NA type responses start w/ this so we can ID them
	public static final String NA_NO_VALUE =  NA_PREFIX + " (Value is null, empty or not a number)";
	public static final String NA_NO_SITE_ELEVATION = NA_PREFIX + " (Site elevation is null or not a number)";
	public static final String NA_NO_UNIT = NA_PREFIX + " (No measurement unit or unit is not recognized)";
	public static final String NA_NO_SITE_DATUM = NA_PREFIX + " (No site datum or datum is not recognized)";
	public static final String NA_NO_PCODE = NA_PREFIX + " (PCode is not recognized)";
	public static final String NA_NO_MEASURE_DATUM = NA_PREFIX + " (Measurement datum is not recognized)";
	
	// Conversion not possible because of inconsistent data
	public static final String NA_PCODE_MEASURE_DATUM_MISMATCH = NA_PREFIX + " (PCode datum does not match the measurement datum)";
	public static final String NA_SITE_PCODE_DATUM_MISMATCH = NA_PREFIX + " (Site datum does not match the PCode datum)";
	public static final String NA_SITE_MEASURE_DATUM_MISMATCH = NA_PREFIX + " (Site datum does not match the measure datum)";
	public static final String NA_PCODE_UNIT_DECLAIRED_UNIT_MISMATCH = NA_PREFIX + " (PCode unit does not match the declaired mesurement unit)";
	public static final String NA_SITE_UNIT_DECLAIRED_UNIT_MISMATCH = NA_PREFIX + " (site unit does not match the declaired mesurement unit)";
	
	
	/**
	 * Converts the passed value to distance below local ground level, with
	 * validation checks.
	 * 
	 * Validation is needed b/c conversion is not possible if units vary between
	 * the PCode and the elevation of the site, the site datum is different than
	 * the measurement datum, etc.
	 * 
	 * If no sample PCODE is provided then this assumes measurements are below land surface.
	 * 
	 * @param valueStr The measured valueStr
	 * @param unitStr
	 * @param pcodeStr
	 * @param measureDatumStr
	 * @param siteElevationStr
	 * @param siteDatumStr
	 * @return 
	 */
	public static BigDecimal mediateToDistanceBelowGroundLevel(
			String valueStr, String unitStr,
			String pcodeStr, String measureDatumStr,
			String siteElevationStr, String siteDatumStr) 
					throws ValidationException {
            
		
		if ( specialBelowGroundLevelCondition(valueStr, pcodeStr) ) {
			return toBigDecimal(valueStr);
		};

		Measure measure = initialValidation(valueStr, unitStr, pcodeStr, measureDatumStr, siteElevationStr, siteDatumStr);
		
		// There is different validation if we are dealing with a measurement
		// that is already from the land surface (no conversion needed)
		// vs a measurement that is above an external datum.
		
		BigDecimal directionalValue = negateIfConditionTrue(measure, measure.pcode.isUp());
		
		if (measure.pcode.getDatum().isLocalLandSurface()) {
			datumUnitValidation(measure);
			return directionalValue;
		} else {
			
			elevationValidation(measure, siteElevationStr, siteDatumStr);
			unitValidation(measure, siteElevationStr, siteDatumStr);
			datumValidation(measure);
			datumUnitValidation(measure);

			// Everything looks OK - calculate the adjusted valueStr
            return SigFigMathUtil.add(measure.siteElevation, directionalValue);
		}
	}
	
	
	/**
	 * Helper method for below ground level situations  
	 * for a special case of a non-USGS site which has no PCode.
	 * Assume the value is measure downward from the surface.
	 * 
	 * @param valueStr
	 * @param pcodeStr
	 * @throws ValidationException when the raw value should be used
	 */
	protected static boolean specialBelowGroundLevelCondition(String valueStr, String pcodeStr) throws ValidationException {
		if (PCode.get(pcodeStr).isUnspecified()) {
			return true;
		}
		return false;
	}


	/**
	 * Helper method to ensure site elevation is provided. It is stand alone because it is not needed
	 * when the PCODE is unspecified.
	 * 
	 * Assume land surface measure - non-USGS sites have no PCode.
	 * Need to convert from measurement wrt to local land surface to the
	 * site datum.  Site datum measurements are assumed to be up from the datum.
	 * 
	 * @param measure
	 * @param siteElevationStr
	 * @param siteDatumStr
	 * @throws ValidationException when the site elevation is missing
	 */
	protected static void elevationValidation(Measure measure, String siteElevationStr, String siteDatumStr) throws ValidationException {
		if (measure.siteElevation == null) {
			throw new ValidationException(NA_NO_SITE_ELEVATION + " Site elevation: " + siteElevationStr + "'");
		}
	}
	

	/**
	 * Helper method to ensure the units match the PCODE units 
	 * with an allowance for unspecified entries where we will use default behavior.
	 * 
	 * @param measure
	 * @param siteElevationStr
	 * @param siteDatumStr
	 * @throws ValidationException when the units are mismatched
	 */
	protected static void unitValidation(Measure measure, String siteElevationStr, String siteDatumStr) throws ValidationException {
		// Always need these values for this conversion & these consistency checks
		if (measure.siteDatum.isUnspecified() || measure.siteDatum.isUnrecognized()) {
			throw new ValidationException(NA_NO_SITE_DATUM + " Site datum: " + siteDatumStr + "'");
		}
		// pcode unit cannot be null
		if (! measure.unit.isUnspecified() && measure.pcode.getUnit() != null && ! measure.pcode.getUnit().equals(measure.unit)) {
			throw new ValidationException(NA_PCODE_UNIT_DECLAIRED_UNIT_MISMATCH + " pcode unit: '" + measure.pcode.getUnit() + "' measure unit: '" + measure.unit + "'");
		}
	}
	
	
	/**
	 * Helper method that ensures datum and units match between measure, datum, and PCODE datum
	 * @param measure
	 * @throws ValidationException measure and PCODE datum or units are mismatched
	 */
	protected static void datumUnitValidation(Measure measure) throws ValidationException {
		// Specific check measure related to the PCode
		if (! measure.datum.isUnspecified() && ! measure.pcode.getDatum().equals(measure.datum)) {
			throw new ValidationException(NA_PCODE_MEASURE_DATUM_MISMATCH + " pcode datum: '" + measure.pcode.getDatum() + "' measure datum: '" + measure.datum + "'"); 
		}
		if (! measure.unit.isUnspecified() && ! measure.pcode.getUnit().equals(measure.unit)) {
			throw new ValidationException(NA_PCODE_UNIT_DECLAIRED_UNIT_MISMATCH + " pcode unit: '" + measure.pcode.getUnit() + "' measure unit: '" + measure.unit + "'"); 
		}
	}
	

	/**
	 * Helper method, only used for below ground level, that ensures the PCODE datum
	 *  matches measured datum.
	 * 
	 * @param measure
	 * @throws ValidationException when the datum are mismatched
	 */
	protected static void datumValidation(Measure measure) throws ValidationException{
		if (! measure.pcode.getDatum().equals(measure.effectiveDatum)) {
			throw new ValidationException(NA_PCODE_MEASURE_DATUM_MISMATCH + " pcode datum: '" + measure.pcode.getDatum() + "' measure datum: '" + measure.effectiveDatum + "'"); 
		}			
		if (! measure.pcode.getDatum().equals(measure.siteDatum)) {
			throw new ValidationException(NA_SITE_PCODE_DATUM_MISMATCH + " pcode datum: '" + measure.pcode.getDatum() + "' site datum: '" + measure.siteDatum + "'"); 
		}
	}
	
	
	/**
	 * Helper method that negates the given value based on a condition. The condition may
	 * be anything but in the use cases it is based on the PCODE direction. When used above a
	 * datum the direction is different than blow land surface. Additionally, there may be
	 * default directionality if no PCODE is given.
	 * 
	 * @param measure
	 * @param condition
	 * @return negated value true; otherwise, original value
	 */
	protected static BigDecimal negateIfConditionTrue(Measure measure, boolean condition) {
		return (condition) ?measure.value.negate() :measure.value;
	}


	/**
	 * Helper method used in both mediations to ensure certain values where given.
	 * The collective nature of this grouping is organic and pulled for the original
	 * mediating methods as a copy/paste block.
	 * 
	 * We also create an instance of the structure used to pass all values between validation checks.
	 * 
	 * @param valueStr
	 * @param unitStr
	 * @param pcodeStr
	 * @param measureDatumStr
	 * @param siteElevation
	 * @param siteDatumStr
	 * @return
	 * @throws ValidationException when the measure value is missing or the PCODE, datum, or units are unrecognized. 
	 */
	protected static Measure initialValidation(String valueStr, String unitStr, String pcodeStr, String measureDatumStr, String siteElevation, String siteDatumStr) 
			throws ValidationException {
		LOGGER.trace("enter");
		Measure measure = new Measure(valueStr, unitStr, pcodeStr, measureDatumStr, siteElevation, siteDatumStr);
		
		if (measure.pcode.isUnrecognized()) {
			throw new ValidationException(NA_NO_PCODE + " Value: '" + pcodeStr + "'");
		} else if (measure.value == null) {
			throw new ValidationException(NA_NO_VALUE + " : valueStr=" + valueStr );
		} else  if (measure.datum.isUnrecognized()) {
			throw new ValidationException(NA_NO_MEASURE_DATUM + " Value: '" + measureDatumStr + "'");
		} else if (measure.unit.isUnrecognized()) {
			throw new ValidationException(NA_NO_UNIT + " Value: '" + unitStr + "'");
		}
		
		LOGGER.trace("exit");
		return measure;
	}


	/**
	 * Converts the passed value to distance below local ground level, with validation checks.
	 * 
	 * Validation is needed b/c conversion is not possible if units vary between
	 * the PCode and the elevation of the site, the site datum is different than
	 * the measurement datum, etc.
	 * 
	 * If no sample PCODE is provided then this assumes measurements are below land surface.
	 * 
	 * @param valueStr The measured valueStr
	 * @param unitStr
	 * @param pcodeStr
	 * @param measureDatumStr
	 * @param siteElevationStr
	 * @param siteDatumStr
	 * @return the mediated value above the site datum after validation and direction consideration
	 */
	public static BigDecimal mediateToDistanceAboveSiteDatum(
			String valueStr, String unitStr,
			String pcodeStr, String measureDatumStr,
			String siteElevationStr, String siteDatumStr) 
				throws ValidationException {

		Measure measure = initialValidation(valueStr, unitStr, pcodeStr, measureDatumStr, siteElevationStr, siteDatumStr);
		unitValidation(measure, siteElevationStr, siteDatumStr);
		
		// There is different validation if we are dealing with a measurement that
		// is from the land surface vs a measurement that is based on external datum.
		BigDecimal directionalValue = negateIfConditionTrue(measure,measure.pcode.isUnspecified() || ! measure.pcode.isUp());
		
		if (measure.pcode.isUnspecified()) {
			elevationValidation(measure, siteElevationStr, siteDatumStr);
			// Everything looks OK - calculate the adjusted valueStr
            return SigFigMathUtil.add(measure.siteElevation, directionalValue);
			
		} else if (measure.pcode.getDatum().isLocalLandSurface()) {
			
			// Need to convert from measurement to local land surface to the
			// site datum.  Site datum measurements are assumed to be up from the datum.
			elevationValidation(measure, siteElevationStr, siteDatumStr);
			datumUnitValidation(measure);
			
			// Everything looks OK - calculate the adjusted valueStr
            return SigFigMathUtil.add(measure.siteElevation, directionalValue);
		} else {
			// Convert from a datum based measurement (not from land surface) to
			// the site datum...  In theory.  In reality, we do not convert b/t
			// datums, so we just check to ensure they are relative to the same datum.
			// If they are not, return some form of NA.
			datumUnitValidation(measure);
			return directionalValue;
		}
	} 
	
	
	/**
	 * Helper method with exception safe conversion from string to a BigDecimal if the number is invalid,
	 * null otherwise.
	 * 
	 * @param number
	 * @return String to BigDecimal or null
	 */
	public static BigDecimal toBigDecimal(String number) {
		try {
			return new BigDecimal(number);
		} catch (Exception e) {
			return null;
		}
	}
	
	
	/**
	 * Private exception for managing mediation validation issues.
	 * It is not used outside this WaterlevelMediator class and best not be in its own file.
	 * @author duselman
	 */
	public static class ValidationException extends Exception {
		private static final long serialVersionUID = 1L;

		public ValidationException(String msg) {
			super(msg);
		}
	}
	
	
	/**
	 * Private container/structure to hold the many values validated and required for mediating well depth
	 * It is not used outside this WaterlevelMediator class and best not be in its own file.
	 * @author duselman
	 */
	protected static class Measure {
		private final BigDecimal value;
		private final PCode pcode;
		private final DepthDatum datum;
		private final Unit unit;
		private final DepthDatum siteDatum;
		private final BigDecimal siteElevation;
		private final DepthDatum effectiveDatum;
		
		public Measure(String valueStr, String unitStr, String pcodeStr, String measureDatumStr, String siteElevation, String siteDatumStr) {
			this.value = toBigDecimal(valueStr);
			this.pcode = PCode.get(pcodeStr);
			this.datum = DepthDatum.get(measureDatumStr);
			this.unit  = Unit.get(unitStr);

			// Only actually required if the measure is land surface based
			this.siteElevation = toBigDecimal(siteElevation);

			this.siteDatum = DepthDatum.get(siteDatumStr);
			// Use the datum for this single measurement if its specified, otherwise the site datum
			this.effectiveDatum = (this.datum.isUnspecified()) ?this.siteDatum :this.datum;
		 }
	}
}
