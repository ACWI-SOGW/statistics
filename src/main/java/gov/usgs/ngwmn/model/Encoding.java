package gov.usgs.ngwmn.model;

public enum Encoding {
	NONE,
	CSV(",", "text/csv"),
	TSV("\t", "text/tsv"),
	XLSX;

	private final String separator;
	private final String mimeType;
	
	private Encoding(String sep, String mimeType) {
		separator = sep;		
		this.mimeType = mimeType;
	}
	
	private Encoding() {
		separator = null;
		mimeType = null;
	}
	public String extension() {
		return (this==NONE) ? null : toString().toLowerCase();
	}
	
	public String getSeparator() {
		return separator;
	}
	
	public String getMimeType() {
		return mimeType;
	}
}
