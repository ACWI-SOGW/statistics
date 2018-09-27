package gov.usgs.ngwmn.model;

import static org.apache.commons.lang.StringUtils.*;



public enum MediationType {
	BelowLand,
	AboveDatum,
	ASCENDING,
	DESCENDING;

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
}
