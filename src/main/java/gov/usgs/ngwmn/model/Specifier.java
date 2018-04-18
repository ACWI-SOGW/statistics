package gov.usgs.ngwmn.model;


import gov.usgs.ngwmn.model.WellRegistry;
import gov.usgs.ngwmn.model.WellRegistryKey;

import java.security.InvalidParameterException;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;



//TODO similar to SiteID class
public class Specifier {
//	private final Object statsSyncLock = new Object();
	private final String agencyCd;
	private final String siteNo;
	private final WellDataType dataType;
	private final boolean multiSite;	//If true, the agencyCd & siteNo encode multiple sites
	
	private Date beginDate;
	private Date endDate;
	
	private Encoding encoding;
	
//	private PipeStatistics stats;
//	
//	public PipeStatistics getStats() {
//		// This is not perfect null and thread protection; it is the standard as I know it.
//		// Odd cases of runtime jvm/cpu optimization can circumvent this.
//		// The edge case where two are initializing the stats is likely NOT going to happen.
//		// The reason this was done is because NGWMN is highly threaded and it might be that
//		// data sent to the user and persisted to the cache are processed on separate threads.
//		if (stats == null) { // this prevent sync when we do not need a stats instance.
//			synchronized (statsSyncLock) {
//				if (stats == null) { // this tries to prevent double setting
//					stats = new PipeStatistics();
//					stats.setSpecifier(this);
//				}
//			}
//		}
//		return stats;
//	}
	
	public Specifier(String agencyCd, String siteNo, WellDataType dataType) {
		this(agencyCd,siteNo,dataType,false);
	}
	
	public Specifier(String agencyCd, String siteNo, WellDataType dataType, boolean multiSite) {
		this.agencyCd = agencyCd;
		this.siteNo = siteNo;
		this.dataType = dataType;
		this.multiSite = multiSite;
		check();
	}
	
	public Specifier(WellRegistry well, WellDataType dataType) {
		this( well.getAgencyCd(), well.getSiteNo(), dataType );
	}
	
	public String getAgencyCd() {
		return agencyCd;
	}
	
	public String getSiteNo() {
		return siteNo;
	}
	
	public synchronized WellDataType getDataType() {
		return dataType;
	}
	
	/**
	 * If true, the agencyCd and siteNo encode multiple sites.
	 * 
	 * Downstream validation should not try to parse or validate the site ID.
	 * 
	 * @return True if this spec represents multiple sites.
	 */
	public boolean isMultiSite() {
		return multiSite;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Specifier [agencyCd=").append(agencyCd)
				.append(", siteNo=").append(siteNo)
				.append(", dataType=").append(dataType).append("]");
		return builder.toString();
	}

	private void check() {
		if ( StringUtils.isBlank(agencyCd) ) 
			throw new InvalidParameterException("Well agency code is required.");
		if ( Strings.isBlank(siteNo) ) 
			throw new InvalidParameterException("Well Feature/Site No is required.");
		if ( dataType == null ) 
			throw new InvalidParameterException("Well data type is required.");
	}

	public WellRegistryKey getWellRegistryKey() {
		return new WellRegistryKey(agencyCd, siteNo);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((agencyCd == null) ? 0 : agencyCd.hashCode());
		result = prime * result
				+ ((siteNo == null) ? 0 : siteNo.hashCode());
		result = prime * result + ((dataType == null) ? 0 : dataType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Specifier other = (Specifier) obj;
		if (agencyCd == null) {
			if (other.agencyCd != null)
				return false;
		} else if (!agencyCd.equals(other.agencyCd))
			return false;
		if (siteNo == null) {
			if (other.siteNo != null)
				return false;
		} else if (!siteNo.equals(other.siteNo))
			return false;
		if (dataType != other.dataType)
			return false;
		if (encoding != other.encoding) 
			return false;
		return true;
	}

	public Date getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	public boolean isBoundedDates() {
		return beginDate != null || endDate != null;
	}

	public Encoding getEncoding() {
		return encoding;
	}

	public void setEncoding(Encoding separator) {
		this.encoding = separator;
	}

}

