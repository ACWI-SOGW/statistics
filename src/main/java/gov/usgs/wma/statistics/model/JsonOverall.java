package gov.usgs.wma.statistics.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import gov.usgs.ngwmn.logic.WaterLevelStatistics.MediationType;

public class JsonOverall extends JsonStats {

	@JsonProperty("LATEST_PCTILE")
	String latestPercentile;
	@JsonProperty("LATEST_VALUE")
	String latestValue;
	@JsonProperty("MAX_VALUE")
	String valueMax;
	@JsonProperty("MEDIAN")
	String valueMedian; // AKA P50
	@JsonProperty("MIN_VALUE")
	String valueMin;
	@JsonProperty("CALC_DATE")
	String dateCalc; // the date the statistics where generated.
	@JsonProperty("MAX_DATE")
	String dateMax;
	@JsonProperty("MIN_DATE")
	String dateMin;
	
	@JsonProperty("MEDIATION")
	MediationType mediation;

	public JsonOverall(int recordYears, int sampleCount, String latestPercentile, String latestValue, String valueMax,
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

/*
"CALC_DATE": "2018-08-29",
"LATEST_PCTILE": "0.31250"
"LATEST_VALUE": "11.000",
"MAX_VALUE": "1.000",
"MEDIAN": "1.500",
"MIN_VALUE": "43.000",
"MAX_DATE": "2018-06-10T04:15:00-05:00",
"MIN_DATE": "2005-06-10T04:15:00-05:00",
*/