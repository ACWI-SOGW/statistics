package gov.usgs.wma.statistics.model;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

public class Value {
	public static final SimpleDateFormat DATE_FORMAT_FULL  = new SimpleDateFormat("yyyy-MM-dd");
	public static final SimpleDateFormat DATE_FORMAT_MONTH = new SimpleDateFormat("yyyy-MM");
	public static final SimpleDateFormat DATE_FORMAT_YEAR  = new SimpleDateFormat("yyyy");
	public static final String UNKNOWN_VALUE = "unknown";

	public final String time;
	public final BigDecimal value; // this will be depth below land surface
	
	public Value(String time, BigDecimal value) {
		this.time = time;
		this.value = value;
	}
}
