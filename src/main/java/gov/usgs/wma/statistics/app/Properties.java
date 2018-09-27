package gov.usgs.wma.statistics.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class Properties {
	public static final String ENV_INVALID_MEDIATION   = "app.error.invalid.mediation";
	public static final String ENV_INVALID_MEDIANS     = "app.error.invalid.medians";
	public static final String ENV_INVALID_PERCENTILE  = "app.error.invalid.percetile";
	public static final String ENV_INVALID_ROW_COLS    = "app.error.invalid.row.cols";
	public static final String ENV_INVALID_ROW_FORMAT  = "app.error.invalid.row.format";
	public static final String ENV_INVALID_ROW_AGING   = "app.error.invalid.row.aging";
	public static final String ENV_INVALID_ROW_VALUE   = "app.error.invalid.row.value";
	public static final String ENV_INVALID_ROW_OTHER   = "app.error.invalid.row.other";
	public static final String ENV_INVALID_ROW_DATE_BLANK   = "app.error.invalid.row.date.blank";
	public static final String ENV_INVALID_ROW_DATE_FUTURE  = "app.error.invalid.row.date.future";
	
	public static final String ENV_MESSAGE_MONTLY_RULE = "app.message.monthly.rule";

	@Autowired
	Environment env;
	public Properties setEnvironment(Environment env) {
		this.env = env;
		return this;
	}
    
	public String getMessage(String messageName, Object ... args) {
		return String.format( env.getProperty(messageName, ""), args);
	}
	public String getError(String errorName, Object ... args) {
		return getMessage(errorName, args);
	}
	
}
