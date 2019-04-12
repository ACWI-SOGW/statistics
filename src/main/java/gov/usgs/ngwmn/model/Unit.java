package gov.usgs.ngwmn.model;

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author eeverman
 */
public enum Unit {
	
	FEET("feet", "ft", false, false),
	METERS("meters", "m", false, false),
	UNRECOGNIZED("INVALID UNIT - UNRECOGNIZED", "INVALID", true, false),
	UNSPECIFIED("__NULL__", "null", false, true);
	
	
	private final String name;
	private final String abbr;
	private final boolean unrecognized;	// Returned from get() if we do not recognize the datum name.  Not the same as null.
	private final boolean unspecified;  // If the unit is empty or null.
	
	Unit(String name, String abbr, boolean unrecognized, boolean unspecified) {
		this.name = name;
		this.abbr = abbr;
		this.unrecognized = unrecognized;
		this.unspecified = unspecified;
	}
	
	/**
	 * Returns the matching DepthDatum, UNSPECIFIED, or UNRECOGNIZED.
	 * 
	 * If the name matches a ENUM constant, enum name or abbreviation, it is returned.
	 * If null or all whitespace is passed, UNSPECIFIED is returned.
	 * Anything else results in UNRECOGNIZED being returned.
	 * 
	 * Note that currently there is some temp clean up of units that are formatted
	 * like this:  'ft  ft' (unit + whitespace + unit).
	 * 
	 * @param name
	 * @return The matching Unit, UNSPECIFIED, or UNRECOGNIZED.  Never null.
	 */
	public static Unit get(String name) {
		
		name = StringUtils.trimToNull(name);
		
		if (name == null) {
			return UNSPECIFIED;
		} else {
		
			// This is a temp fix for units that come through like this: "ft  ft"
			// Since the data is cached, it is not immediately fixable
			// TODO: Remove this once the source transformation is fixed
			if (name.indexOf(' ') > 0) {
				name = name.substring(0, name.indexOf(' '));
			}

			try {

				return Unit.valueOf(name);

			} catch (Exception e) {

				for (Unit u : Unit.values()) {
					if (u.abbr.equalsIgnoreCase(name)) return u;
					if (u.name.equalsIgnoreCase(name)) return u;
				}

				return UNRECOGNIZED;
			}
		}
	}

	public String getName() {
		return name;
	}

	public String getAbbr() {
		return abbr;
	}

	public boolean isUnrecognized() {
		return unrecognized;
	}
	
	public boolean isUnspecified() {
		return unspecified;
	}
	
	
}
