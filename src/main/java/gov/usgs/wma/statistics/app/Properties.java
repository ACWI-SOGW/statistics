package gov.usgs.wma.statistics.app;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;


@Service
public class Properties {
	private static final Logger LOGGER = LoggerFactory.getLogger(Properties.class);
	
	
	public static final String ENV_INVALID_MEDIATION   = "app.error.invalid.mediation";
	public static final String ENV_INVALID_MEDIANS     = "app.error.invalid.medians";
	public static final String ENV_INVALID_PERCENTILE  = "app.error.invalid.percentile";
	public static final String ENV_INVALID_ROW_COLS    = "app.error.invalid.row.cols";
	public static final String ENV_INVALID_ROW_AGING   = "app.error.invalid.row.aging";
	public static final String ENV_INVALID_ROW_VALUE   = "app.error.invalid.row.value";
	public static final String ENV_INVALID_ROW_FORMAT  = "app.error.invalid.row.format";
	public static final String ENV_INVALID_ROW_DATE_BLANK   = "app.error.invalid.row.date.blank";
	public static final String ENV_INVALID_ROW_DATE_FUTURE  = "app.error.invalid.row.date.future";
	
	public static final String ENV_MESSAGE_PROVISIONAL_RULE = "app.message.provisional.rule";
	public static final String ENV_MESSAGE_MONTHLY_RULE     = "app.message.monthly.rule";
	public static final String ENV_MESSAGE_MONTHLY_DETAIL   = "app.message.monthly.detail";
	public static final String ENV_MESSAGE_MONTHLY_MEDIANS  = "app.message.monthly.medians";
	public static final String ENV_MESSAGE_DATE_FIX_DAY     = "app.message.date.fix.day";
	public static final String ENV_MESSAGE_DATE_FIX_MONTH   = "app.message.date.fix.month";
	public static final String ENV_MESSAGE_OMIT_NULL        = "app.message.omit.null";
	public static final String ENV_MESSAGE_OMIT_PROVISIONAL = "app.message.omit.provisional";
	
	
	@Autowired
	Environment env;
	public Properties setEnvironment(Environment env) {
		this.env = env;
		return this;
	}
    
	public String getMessage(String messageName, Object ... args) {
		String msg = env.getProperty(messageName, "");
		if (StringUtils.isBlank(msg)) {
			LOGGER.error("'{}' property not found.", messageName);
			return "";
		}
		return String.format(msg, args);
	}
	public String getError(String errorName, Object ... args) {
		return getMessage(errorName, args);
	}
	
}
