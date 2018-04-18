package gov.usgs.ngwmn.model;

import org.springframework.util.StringUtils;

/**
 *
 * @author eeverman
 */
public enum PCode {

	P72019("72019", DepthDatum.LAND_SURFACE, Unit.FEET, false, false, false, "Depth to water level, feet below land surface"),
	P72230("72230", DepthDatum.HI_LOCAL, Unit.FEET, true, false, false, "Groundwater level above Local Hawaiian Datum, feet"),

	// Above Datum
	P62610("62610", DepthDatum.NGVD29, Unit.FEET, true, false, false, "Groundwater level above NGVD 1929, feet"),
	P62611("62611", DepthDatum.NAVD88, Unit.FEET, true, false, false, "Physical Groundwater level above NAVD 1988, feet"),
	P62612("62612", DepthDatum.NGVD29, Unit.METERS, true, false, false, "Groundwater level above NGVD 1929, meters"),
	P62613("62613", DepthDatum.NAVD88, Unit.METERS, true, false, false, "Physical Groundwater level above NAVD 1988, meters"),
	//Should we note that this is distance above the datum?
	P72150("72150", DepthDatum.MSL, Unit.FEET,   true, false, false, "Groundwater level relative to Mean Sea Level (MSL), feet"),

	// Below Land Surface
	P30210("30210", DepthDatum.BLS, Unit.METERS,false,false,false,"Physical Depth to water level, below land surface datum (LSD), meters Depth, from ground surface to well water level m"),
	P61055("61055", DepthDatum.BLS, Unit.FEET,  false,false,false,"Physical Water level, depth below measuring point, feet Water level in well, depth from a reference point ft"),
	P99019("99019", DepthDatum.BLS, Unit.METERS,false,false,false,"Physical Water level, depth below land surface, meters Depth, from ground surface to well water level m"),
	
	UNRECOGNIZED("__UNRECOGNIZED__", null, null, false, true, false, "The PCode string was unrecognized"),
	UNSPECIFIED("__NULL__", null, null, false, false, true, "The PCode is empty/null/unspecified");
	
	private final String code;
	private final DepthDatum datum;
	private final String description;
	private final Unit unit;
	private final boolean up;	//true if positive values indicate the direction upwards from the datum.
	private final boolean unrecognized;	//If we do not recognize the name.
	private final boolean unspecified; //If the pcode is empty or null.
	
	PCode(String code, DepthDatum datum, Unit unit, boolean isUp, boolean unrecognized, boolean unspecified, String description) {
		this.code = code;
		this.datum = datum;
		this.unit = unit;
		this.up = isUp;
		this.unrecognized = unrecognized;
		this.description = description;
		this.unspecified = unspecified;
	}
	
	/**
	 * Finds a pcode instance based on its code, which is the numerical USGS code
	 * for the PCode, such as '72019'.
	 * 
	 * If null or all whitespace is passed, UNSPECIFIED is returned.
	 * Anything else results in UNRECOGNIZED being returned.
	 * 
	 * @param name
	 * @return The matching PCode, UNSPECIFIED, or UNRECOGNIZED.  Never null.
	 */
	public static PCode get(String name) {
		
		name = StringUtils.trimAllWhitespace(name);
		
		if (name == null) {
			return UNSPECIFIED;
		} else {
			try {
				PCode p = PCode.valueOf("P" + name);
				return p;
			} catch (Exception e) {
				return UNRECOGNIZED;
			}
		}
		
	}
	
	public String getCode() {
		return code;
	}

	public DepthDatum getDatum() {
		return datum;
	}

	public String getDescription() {
		return description;
	}

	public Unit getUnit() {
		return unit;
	}

	public boolean isUp() {
		return up;
	}
	
	public boolean isUnrecognized() {
		return unrecognized;
	}
	
	public boolean isUnspecified() {
		return unspecified;
	}

}
