package gov.usgs.ngwmn.model;


public enum WellDataType {
	REGISTRY	("text/xml", "xml", null, "VW_GWDP_GEOSERVER", false, "SITE_INFO", true),
	WATERLEVEL  ("text/xml", "xml", null, "TimeValuePair"),
	QUALITY     ("text/xml", "xml", null, "Result"),
	LOG         ("text/xml", "xml", null, "logElement|construction"),
	LITHOLOGY   ("text/xml", "xml", LOG,  "logElement"),
	CONSTRUCTION("text/xml", "xml", LOG,   null),
	SCREEN		("text/xml", "xml", null,  null, false, "SCREEN", false),	/* selected automatically when CONSTRUCTION is selected */
	CASING		("text/xml", "xml", null,  null, false, "CASING", false),	/* selected automatically when CONSTRUCTION is selected */
	ALL			("application/zip", "zip", null, "n/a", false);

	
	public final String contentType;
	public final String suffix;
	public final String rowElementName;
	public final WellDataType aliasFor;
	public final boolean cachable;
	public final boolean useGet;	//If true, use http GET requests to retrieve.  If false, use POST.
	private final String filename;	//Name used for export files
	

	private WellDataType(String contentType, String suffix, WellDataType alias, String rowElementName) {
		this(contentType, suffix, alias, rowElementName, true);		
	}
		
	private WellDataType(String contentType, String suffix, WellDataType alias, String rowElementName, boolean cachable) {
		this(contentType, suffix, alias, rowElementName, cachable, null, true);		
	}
	
	private WellDataType(String contentType, String suffix, WellDataType alias, String rowElementName, boolean cachable, String filename, boolean useGet) {
		this.contentType        = contentType;
		this.suffix             = suffix;
		this.rowElementName     = rowElementName;
		this.aliasFor           = (alias == null) ? this : alias;
		this.cachable 			= cachable;
		this.filename           = filename;
		this.useGet				= useGet;
	}

	public String makeFilename(String wellName) {
		return wellName + "_" + this.name() + "." + this.suffix;
	}

	public boolean isCachable() {
		return cachable;
	}
	
	public String getFileName() {
		if (filename==null) {
			return toString();
		}
		return filename;
	}
	
	public boolean equals(String str) {
		return this.toString().equals(str);
	}
}
