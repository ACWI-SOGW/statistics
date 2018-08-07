package gov.usgs.ngwmn.model;

/**
 * This is used to name a statistical data set.
 * It is used in logging in order to give context 
 * to the state and issues when generating statistics.
 *  
 *  Originally from the NGWMN project, this specifier
 *  has been cut back to just the AgencyCd and SiteNo.
 *  
 *  If data is not from a known site or is not from a site
 *  at all then this could merely be a means to identify
 *  the statistical data set. In that case the single
 *  string constructor could be used. (Or the no args.)
 *  
 * @author duselman
 */
public class Specifier {
	private final String agencyCd;
	private final String siteNo;

	public Specifier(String agencyCd) {
		this.agencyCd = agencyCd;
		this.siteNo = null;
	}
	public Specifier(String agencyCd, String siteNo) {
		this.agencyCd = agencyCd;
		this.siteNo = siteNo;
	}
	/**
	 * A random data set ID for unknown site IDs.
	 * Use the label the statistics in proxy.
	 */
	public Specifier() {
		this("DataSetID", "" + (Math.random()*1e15));
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
		builder.append(agencyCd);
		if (siteNo != null) {
			builder.append("-").append(siteNo);
		}
		return builder.toString();
	}

}

