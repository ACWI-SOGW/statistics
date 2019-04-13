package gov.usgs.ngwmn.model;

import org.apache.commons.lang.StringUtils;

public class Elevation {
	public final Double value;
	public final String datum;
	
	public Elevation() {
		this(null,null);
	}
	
	public Elevation(Double value, String datum) {
		this.value = value;
		this.datum = StringUtils.trimToNull(datum);
	}
	
	public boolean isValid() {
		return (value != null && datum != null && !"NA".equalsIgnoreCase(datum));
	}

	@Override
	public String toString() {
		return "Elevation [value=" + value + ", datum=" + datum + "]";
	}
}
