package gov.usgs.ngwmn.model;


import java.security.InvalidParameterException;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;


public class Specifier {
	private final String agencyCd;
	private final String siteNo;

	public Specifier(String agencyCd, String siteNo) {
		this.agencyCd = agencyCd;
		this.siteNo = siteNo;
		check();
	}
	
	public String getAgencyCd() {
		return agencyCd;
	}
	public String getSiteNo() {
		return siteNo;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Specifier [")
			.append("agencyCd=").append(agencyCd)
			.append(", siteNo=").append(siteNo)
			.append("]");
		return builder.toString();
	}

	private void check() {
		if ( StringUtils.isBlank(agencyCd) ) 
			throw new InvalidParameterException("Well agency code is required. For Example, 'USGS'");
		if ( Strings.isBlank(siteNo) ) 
			throw new InvalidParameterException("Well SiteID Number is required. For Example, '12345678'");
	}
}

