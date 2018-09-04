package gov.usgs.wma.statistics.model;

import static gov.usgs.wma.statistics.model.Value.*;
//import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import gov.usgs.ngwmn.logic.WaterLevelStatistics.MediationType;


public class JsonDataBuilder {
	private static Logger LOGGER = org.slf4j.LoggerFactory.getLogger(JsonDataBuilder.class);

	
	public static final String CALC_DATE     = "CALC_DATE";
	public static final String LATEST_PCTILE = "LATEST_PCTILE";
	public static final String LATEST_VALUE  = "LATEST_VALUE";
	public static final String MEDIATION     = "MEDIATION";
	public static final String MIN_DATE      = "MIN_DATE";
	public static final String MAX_DATE      = "MAX_DATE";
	public static final String MIN_VALUE     = "MIN_VALUE";
	public static final String MAX_VALUE     = "MAX_VALUE";
	public static final String MEDIAN        = "MEDIAN"; // P50
	public static final String P50_MIN       = "P50_MIN";
	public static final String P50_MAX       = "P50_MAX";
	public static final String RECORD_YEARS  = "RECORD_YEARS";
	public static final String SAMPLE_COUNT  = "SAMPLE_COUNT";

	// default percentiles
	public static final String P10           = "10";
	public static final String P25           = "25";
	public static final String P50           = "50";
	public static final String P75           = "75";
	public static final String P90           = "90";
	
	public static final String MONTH         = "MONTH";
	
	public static final String QUOTE         = "\"";
	
	/**
	 * The list of percentiles to calculate.
	 */
	Set<String> percentiles   = new HashSet<>();
	/**
	 * All values for the given statistics module: overall or monthly.
	 */
	Map<String, String> values = new HashMap<>();
	
	MediationType mediation = MediationType.NONE;

	StringBuilder intermediateValues = new StringBuilder();
	boolean includeIntermediateValues = false;
	
	JsonData jsonData;
	
	public JsonDataBuilder() {
		// default percentiles
		percentiles.addAll(Arrays.asList(P10,P25,P50,P75,P90));
		this.jsonData = new JsonData();
	}
	
	public String get(String name) {
		return values.get(name);
	}
	
	public JsonDataBuilder latestPercentile(String percentile) {
		values.put(LATEST_PCTILE, percentile);
		return this;
	}
	
	public JsonDataBuilder latestValue(String value) {
		values.put(LATEST_VALUE, value);
		return this;
	}
	
	public JsonDataBuilder mediation(MediationType mediation) {
		this.mediation = mediation;
		return this;
	}
	public MediationType mediation() {
		return mediation;
	}

	public JsonDataBuilder median(String value) {
		values.put(MEDIAN, value);
		return this;
	}

	public JsonDataBuilder minDate(String dateUTC) {
		values.put(MIN_DATE, dateUTC);
		return this;
	}

	public JsonDataBuilder maxDate(String dateUTC) {
		values.put(MAX_DATE, dateUTC);
		return this;
	}

	public JsonDataBuilder minValue(String value) {
		values.put(MIN_VALUE, value);
		return this;
	}

	public JsonDataBuilder maxValue(String value) {
		values.put(MAX_VALUE, value);
		return this;
	}

	public JsonDataBuilder recordYears(String value) {
		values.put(RECORD_YEARS, value);
		return this;
	}
	
	public JsonDataBuilder sampleCount(int value) {
		values.put(SAMPLE_COUNT, ""+value);
		return this;
	}
	
	public JsonDataBuilder month(String value) {
		try {
			int intValue = Integer.parseInt(value);
			if (intValue < 1 || intValue > 12) {
				throw new RuntimeException();
			}
		} catch (Exception e) {
			// catch parse or range exception and throw same descriptive exception
			throw new RuntimeException(value + " is not a valid month. (1-12)");
		}
		values.put(MONTH, value);
		return this;
	}

	public JsonDataBuilder putPercentile(String percentile, String value) {
		this.values.put(percentile, value);
		return this;
	}
	
	public JsonDataBuilder minP50(String value) {
		return putPercentile(P50_MIN, value);
	}

	public JsonDataBuilder maxP50(String value) {
		return putPercentile(P50_MAX, value);
	}
	
	public JsonDataBuilder percentiles(Collection<String> percentiles) {
		this.percentiles = new HashSet<>(percentiles);
		return this;
	}
	
	public JsonDataBuilder addPercentiles(String ... percentiles) {
		for (String percentile : percentiles) {
			this.percentiles.add(percentile);
		}
		return this;
	}
	
	public Map<String, BigDecimal>  buildPercentiles() {
		Map<String, BigDecimal> percentileValues = new HashMap<>();
		for (String percentile : this.percentiles) {
			String key = "P" + percentile;
			String value = "0." + percentile.replace(".", "") + "0000000";
			
			// these are exact percentiles and should not limit measured precision
			percentileValues.put(key, new BigDecimal(value));
		}
		return percentileValues;
	}

	public JsonData build() {
		collect();
		buildIntermediateValues();
		avoidNulls();
		buildErrors();
		return jsonData;
	}
	
	protected void avoidNulls() {
		if (jsonData.overall == null) {
			jsonData.overall = new JsonOverall("", 0, "", "", "", "", "", "", "", "", MediationType.NONE);
		}
		// TODO fill in other nulls ?
	}

	public JsonDataBuilder collect() {
		if ( values.isEmpty() ) {
			return this;
		}

		String recordYears = values.remove(RECORD_YEARS);
		int sampleCount = Integer.parseInt( values.remove(SAMPLE_COUNT) );

		if (isOverall()) {
			buildOverall(recordYears, sampleCount);
		} else {
			buildMonthly(recordYears, sampleCount);
		}
		values= new HashMap<>();
		
		return this;
	}

	private void buildMonthly(String recordYears, int sampleCount) {
		String month    = values.remove(MONTH);
		
		JsonMonthly monthly = new JsonMonthly(recordYears, sampleCount, values);
		jsonData.monthly.put(month, monthly);
	}
	
	public boolean hasMonthly() {
		return jsonData.hasMonthly();
	}

	private JsonDataBuilder buildOverall(String recordYears, int sampleCount) {
		this.values.put(CALC_DATE, DATE_FORMAT_FULL.format(new Date()));
		this.values.put(MEDIATION, mediation.toString());
		
		jsonData.overall = new JsonOverall(recordYears, sampleCount,
				values.get(LATEST_PCTILE), values.get(LATEST_VALUE), 
				values.get(MAX_VALUE), values.get(MEDIAN), values.get(MIN_VALUE),
				values.get(CALC_DATE), values.get(MAX_DATE), values.get(MIN_DATE), 
				mediation);

		return this;
	}

	public boolean isOverall() {
		return ! isMonthly();
	}
	
	public boolean isMonthly() {
		// While it may be ambiguous before the month is set
		// the month should be set right off
		return values.containsKey(MONTH);
	}
	
	public JsonDataBuilder setIncludeIntermediateValues(Boolean includeIntermediateValues) {
		this.includeIntermediateValues = includeIntermediateValues;
		return this;
	}
	
	public boolean isIncludeIntermediateValues() {
		return includeIntermediateValues;
	}
	
	public String getIntermediateValues() {
		return intermediateValues.toString();
	}
	
	public JsonDataBuilder intermediateValue(Value sample) {
		if ( ! isIncludeIntermediateValues()) {
			return this;
		}
		
		intermediateValues.append(sample.toCSV()).append("\\n");
		
		return this;
	}
	
	public JsonDataBuilder intermediateValues(List<? extends Value> samples) {
		if ( ! isIncludeIntermediateValues()) {
			return this;
		}
		
		for (Value sample : samples) {
			intermediateValue(sample);
		}
		
		return this;
	}
	
	public JsonDataBuilder buildIntermediateValues() {
		jsonData.medians = "";
		
		if ( isIncludeIntermediateValues() && intermediateValues.length() > 0) {
			jsonData.medians = QUOTE + intermediateValues.toString() + QUOTE;
			LOGGER.trace(intermediateValues.toString());
		}
		return this;
	}

	public JsonDataBuilder error(String msg) {
		jsonData.addError(msg);
		return this;
	}
	public JsonDataBuilder errors(List<String> msgs) {
		jsonData.addAllErrors(msgs);
		return this;
	}
	public boolean isOk() {
		return jsonData.isOk();
	}
	public boolean hasErrors() {
		return jsonData.hasErrors();
	}
	
	public void buildErrors() {
		// if NO errors then do nothing
		if (isOk()) {
			return;
		}
		// if any errors clear the monthly stats
		// TODO this is my guess as to what we want
		jsonData.monthly.clear();
	}
}
