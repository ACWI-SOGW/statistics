package gov.usgs.wma.statistics.model;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class Value {
	private static final Logger LOGGER = LoggerFactory.getLogger(Value.class);
	
	public static final SimpleDateFormat DATE_FORMAT_FULL  = new SimpleDateFormat("yyyy-MM-dd");
	public static final SimpleDateFormat DATE_FORMAT_MONTH = new SimpleDateFormat("yyyy-MM");
	public static final SimpleDateFormat DATE_FORMAT_YEAR  = new SimpleDateFormat("yyyy");

	public static final String PROVISIONAL_CODE = "P";
	public static final String APPROVED_CODE = "A";
	public static final String UNKNOWN_VALUE = "unknown";

	public static final Comparator<Value> SORT_VALUE_DESCENDING = new Comparator<Value>() {
		public int compare(Value a, Value b) {
			if (bothAreKnown(a, b)) {
				return b.value.compareTo( a.value );
			}
			return compareUnknown(a, b);
		};
	};
	public static final Comparator<Value> SORT_VALUE_ASCENDING = new Comparator<Value>() {
		public int compare(Value a, Value b) {
			if (bothAreKnown(a, b)) {
				return a.value.compareTo( b.value );
			}
			return compareUnknown(a, b);
		};
	};
	
	
	public String time;
	public BigDecimal value; // this will be depth below land surface
	private boolean unknown;
	
	private Boolean provisional; // most values are not provisional and we like the false default
	
	public Value(String time, String value) {
		this.time = time;
		this.unknown = isUnknown(value);
		if (!unknown) {
			this.value = new BigDecimal(value);
		} else {
			this.value = null;
		}
	}
	
	public Value(String time, BigDecimal value) {
		this.time = time;
		this.value = value;
		this.unknown = false;
	}
	public Value(String time, BigDecimal value, boolean provisional) {
		this(time, value);
		this.provisional = provisional;
	}
	public Value(Value base) {
		this(base.time, base.value, base.isProvisional());
	}

	public static BigDecimal valueOf(Value value) {
		if (value == null) {
			return null;
		}
		return value.value;
	}

	public static boolean isUnknown(String value) {
		return UNKNOWN_VALUE.equalsIgnoreCase(value);
	}

	/**
	 *  sorts in ascending order of date time
	 */
	public static final Comparator<Value> TIME_COMPARATOR = new Comparator<Value>() {
		public int compare(Value a, Value b) {
			return a.time.compareTo( b.time );
		};
	};
	public static final boolean bothAreKnown(Value a, Value b) {
		return ! a.isUnknown() && ! b.isUnknown();
	}
	
	public static final int compareUnknown(Value a, Value b) {
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
	 * 
	 * @param value the string encoded BigDecimal value to be checked
	 * @param record the row number of the record
	 * @param mySiteId the provider:site ID for the checked value
	 * @return
	 */
	public static boolean checkBadValue(String value, int record, String mySiteId) {
		if (value == null) {
			LOGGER.warn("Water Level Error - value:null record:{} site:{}", record, mySiteId);
			return true;
		} if ( isUnknown(value) ) {
			// unknown values are not "bad" values
			return false;
		} else {
			try {
				new BigDecimal(value);
			} catch (Exception e) {
				LOGGER.warn("Water Level Error - value:'{}' record:{} site:{}", new Object[]{value, record, mySiteId});
				return true;
			}
		}
		return false;
	}


	public static boolean checkBadTime(String time, int record, String mySiteId) {
		if (time == null) {
			LOGGER.warn("Water Level Error - time:null record:{} site:{}", record, mySiteId);
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
					LOGGER.warn("Water Level Error - time:'{}' record:{} site:{}", new Object[]{time, record, mySiteId});
					return true;
				}
			}
		}
		return false;
	}
	

	public static String padMonth(String month) {
		String paddedMonth = month==null ?"" :((month.length()==1) ?"0" :"")+month;
		return paddedMonth;
	}
	public static String monthUTC(String utc) {
		return utc.substring(5,7);
	}
	public static String yearUTC(String utc) {
		return utc.substring(0,4);
	}
	

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
	public boolean isUnknown() {
		return unknown;
	}
	public void setUnknown(boolean unknown) {
		this.unknown = unknown;
	}

	public void setProvsional(boolean isProvisional) {
		// set only once, then immutable like all other properties
		// why? because I did not want to change the constructor signature with another final value
		// it could disrupt the contract for other implementations.
		if (this.provisional == null) {
			this.provisional = isProvisional;
		}
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
	
	@Override
	public String toString() {
		StringBuilder toString = new StringBuilder();
		toString.append("Time:").append(time).append("\n")
			.append("value:").append(isUnknown() ? UNKNOWN_VALUE : value).append("\n")
			.append( (isProvisional() ?"is:"+PROVISIONAL_CODE :"") );
		return toString.toString();
	}
	
	public String toCSV() {
		return toCharacterSeparatedValue(",");
	}
	public String toCharacterSeparatedValue(String separator) {
		StringBuilder toString = new StringBuilder();
		toString.append(time).append(separator)
			.append(isUnknown() ? UNKNOWN_VALUE : value).append(separator)
			.append( (isProvisional() ?PROVISIONAL_CODE :"") );
		return toString.toString();
	}
}
