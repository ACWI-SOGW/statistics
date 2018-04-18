package gov.usgs.ngwmn.model;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

/**
 * This enumeration contains a fixed set of constants that represent possible 
 * depth datum values.
 *
 * @author eeverman
 */
public enum DepthDatum {
	
	LAND_SURFACE("LandSurface", "Local land surface", null, true, false, false),
	HI_LOCAL("HILocal", "Local Hawaiian Datum"),
	NAD83("NAD83", "NAD83"),
	NGVD29("NGVD29", "NGVD 1929"),
	NAVD88("NAVD88", "NAVD 1988"),
	WGS84("WGS84", "WGS84"),
	MSL("MSL", "Mean Sea Level", new String[] {"LMSL"}, false, false, false),
	BLS("BLS", "Below Land Surface",  null, true, false, false),
	UNRECOGNIZED("__UNRECOGNIZED__", "INVALID DATUM - UNRECOGNIZED", null, false, true, false),
	UNSPECIFIED("__NULL__", "DATUM IS UNSPECIFIED / NULL", null, false, false, true);
	
	private final String name;
	private final String[] alias;
	private final String description;
	private final boolean localLandSurface; //True if this is a local reference to land surface, which is non-convertible to anything else
	private final boolean unrecognized; //If we do not recognize the datum name.  Not the same as null.
	private final boolean unspecified; //If the name is empty or null.
	
	/**
	 * This is a constructor for the DepthDatum enumeration.
	 * 
	 * @param name The value of the name object.
	 * @param description The value of the description object.
	 */
	private DepthDatum(String name, String description) {
		this.name = name;
		this.description = description;
		this.localLandSurface = false;
		this.unrecognized = false;
		this.unspecified = false;
		this.alias = ArrayUtils.EMPTY_STRING_ARRAY;
	}
	/**
	 * This is a constructor for the DepthDatum enumeration.
	 * 
	 * @param name The value of the name object.
	 * @param description The value of the description object.
	 * @param alias The value of the alias object.
	 * @param isLocalLandSurface The value of the isLocalLandSurface object.
	 * @param isUnrecognized The value of the isUnrecognized object.
	 * @param unspecified The value of the unspecified object.
	 */
	private DepthDatum(
		String name,
		String description,
		String[] alias,
		boolean isLocalLandSurface,
		boolean isUnrecognized,
		boolean unspecified) {

		this.name = name;
		this.description = description;
		
		if (alias != null) {
			this.alias = alias;
		} else {
			this.alias = ArrayUtils.EMPTY_STRING_ARRAY;
		}

		this.localLandSurface = isLocalLandSurface;
		this.unrecognized = isUnrecognized;
		this.unspecified = unspecified;
	}
	
	/**
	 * Returns the matching DepthDatum, UNSPECIFIED, or UNRECOGNIZED.
	 * 
	 * If the name matches a ENUM constant, the enum name or an alias, it is returned.
	 * If null or all whitespace is passed, UNSPECIFIED is returned.
	 * Anything else results in UNRECOGNIZED being returned.
	 * 
	 * @param name
	 * @return The matching DepthDatum, UNSPECIFIED, or UNRECOGNIZED.  Never null.
	 */
	public static DepthDatum get(String name) {
		
		name = StringUtils.trimToNull(name);
		
		if (name == null) {
			return UNSPECIFIED;
		} else {
		
			try {

				return DepthDatum.valueOf(name);

			} catch (Exception exception) {

				for (DepthDatum depthDatum : DepthDatum.values()) {
					if (depthDatum.name.equalsIgnoreCase(name)) return depthDatum;
					
					for (String a : depthDatum.alias) {
						if (a.equalsIgnoreCase(name)) return depthDatum;
					}
					
				}

				return UNRECOGNIZED;
			}
		}
	}
	/**
	 * This method returns the value of the name object.
	 * @return name The value of the name object.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * This method returns the value of the description object.
	 * @return description The value of the description object.
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * This method returns the value of the localLandSurface object.
	 * True if this is a local reference to land surface, which is 
	 * non-convertible to anything else
	 * @return The value of the localLandSurface object.
	 */
	public boolean isLocalLandSurface() {
		return localLandSurface;
	}
	
	/**
	 * This method returns the value of the unrecognized object.
	 * It returns true if we do not recognize the datum name.  Not the same as null.
	 * @return unrecognized The value of the unrecognized object.
	 */
	public boolean isUnrecognized() {
		return unrecognized;
	}
	
	/**
	 * This method returns the value of the unspecified object.
	 * It returns true if the name is empty or null.
	 * @return unspecified The value of the unspecified object.
	 */
	public boolean isUnspecified() {
		return unspecified;
	}
}
