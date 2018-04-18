package gov.usgs.ngwmn.model;

import java.math.BigDecimal;
import java.util.Comparator;

public class WLSample {
	public static final String UNKNOWN_VALUE = "unknown";

	public final String time;
	public final BigDecimal value; // this will be depth below land surface
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
		this.time = time;
		this.value = value;
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
		return monthUTC(time);
	}
	public String getYear() {
		return yearUTC(time);
	}
	public BigDecimal getValue() {
		return value;
	}
	
	
	public static String monthUTC(String utc) {
		return utc.substring(5,7);
	}
	public static String yearUTC(String utc) {
		return utc.substring(0,4);
	}
}