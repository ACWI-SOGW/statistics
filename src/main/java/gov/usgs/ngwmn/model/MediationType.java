package gov.usgs.ngwmn.model;

import static org.apache.commons.lang.StringUtils.*;



public enum MediationType {
	DEFAULT,   // null protection use
	AboveDatum,// well data use
	BelowLand, // well data use
	ASCENDING, // general stats use
	DESCENDING;// general stats use

	private static String validValues;

	public static String validMediations() {
		if (isBlank(validValues)) {
			StringBuilder values = new StringBuilder();
			String sep = "";
			for (MediationType mediation : MediationType.values()) {
				values.append(sep).append(mediation.toString());
				sep = ", ";
			}
			validValues = values.toString();
		}
		return validValues;
	}
	
	/**
	 * AboveDatum and Ascending are equal sort
	 * BelowLand and Descending are equal sort
	 * @param obj instance to compare
	 * @return true if equal
	 */
	public boolean equal(Object obj) {
		if (obj == null || !(obj instanceof MediationType)) {
			return false;
		}
		if (this.equals(obj)) {
			return true;
		}
		if (BelowLand.equals(this) || DESCENDING.equals(this)) {
			return (BelowLand.equals(obj) || DESCENDING.equals(obj));
		}
		// ASCENDING seems more natural sort for non-well data and is default for this reason
		if (AboveDatum.equals(this) || ASCENDING.equals(this) || DEFAULT.equals(this)) {
			return (AboveDatum.equals(obj) || ASCENDING.equals(obj) || DEFAULT.equals(obj));
		}
		return false;
	}
}
