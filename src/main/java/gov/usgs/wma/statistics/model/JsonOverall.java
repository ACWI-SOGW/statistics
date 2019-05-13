package gov.usgs.wma.statistics.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import gov.usgs.ngwmn.model.MediationType;

/**
 * the JSON will look something like this
 * {
 *	"LATEST_PCTILE": "31.250"
 *	"LATEST_VALUE": "11.000",
 *	"MAX_VALUE": "1.000",
 *	"MEDIAN": "1.500",
 *	"MIN_VALUE": "43.000",
 *	"CALC_DATE": "2018-08-29",
 *	"MAX_DATE": "2018-06-10T04:15:00-05:00",
 *	"MIN_DATE": "2005-06-10T04:15:00-05:00",
 *	"MEDIATION": "BelowLand"
 * }
 * 
 * @author duselman
 *
 */

public class JsonOverall extends JsonStats {

	@JsonProperty("LATEST_PCTILE")
	public final String latestPercentile;
	@JsonProperty("LATEST_VALUE")
	public final String latestValue;
	@JsonProperty("MAX_VALUE")
	public final String valueMax;
	@JsonProperty("MEDIAN")
	public final String valueMedian; // AKA P50
	@JsonProperty("MIN_VALUE")
	public final String valueMin;
	@JsonProperty("CALC_DATE")
	public final String dateCalc; // the date the statistics where generated.
	@JsonProperty("MAX_DATE")
	public final String dateMax;
	@JsonProperty("MIN_DATE")
	public final String dateMin;
	
	@JsonProperty("MEDIATION")
	MediationType mediation;

	public JsonOverall(String recordYears, int sampleCount, String latestPercentile, String latestValue, String valueMax,
			String valueMedian, String valueMin, String dateCalc, String dateMax, String dateMin,
			MediationType mediation) {
		super(recordYears, sampleCount);
		this.latestPercentile = latestPercentile;
		this.latestValue = latestValue;
		this.valueMax = valueMax;
		this.valueMedian = valueMedian;
		this.valueMin = valueMin;
		this.dateCalc = dateCalc;
		this.dateMax = dateMax;
		this.dateMin = dateMin;
		this.mediation = mediation;
	}
	
	
}

