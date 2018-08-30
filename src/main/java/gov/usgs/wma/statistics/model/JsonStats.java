package gov.usgs.wma.statistics.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The JSON names for each value matches the tables in NGWMN.
 * 
 * @author duselman
 */
public abstract class JsonStats {
	@JsonProperty("RECORD_YEARS")
	final int recordYears;
	@JsonProperty("SAMPLE_COUNT")
	final int sampleCount;
	
	public JsonStats(int recordYears, int sampleCount) {
		this.recordYears = recordYears;
		this.sampleCount = sampleCount;
	}
}
