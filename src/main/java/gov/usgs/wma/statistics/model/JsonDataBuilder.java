package gov.usgs.wma.statistics.model;

import static gov.usgs.wma.statistics.app.Properties.*;
import static gov.usgs.wma.statistics.model.Value.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.Logger;

import gov.usgs.ngwmn.model.MediationType;
import gov.usgs.wma.statistics.app.Properties;


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
	public static final String MEDIAN        = "MEDIAN_VALUE"; // P50
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
	
	private Properties env;
	
	/**
	 * The list of percentiles to calculate.
	 */
	Set<String> percentiles   = new HashSet<>();
	/**
	 * All values for the given statistics module: overall or monthly.
	 */
	Map<String, String> values = new HashMap<>();
	
	MediationType mediation = MediationType.DEFAULT;

	/**
	 * This is a collection of all the normalized values.
	 * Is used for the overall percentile value.
	 */
	List<Value> intermediateValuesList   = new LinkedList<>();
	/**
	 * Since there are a couple places intermediate values must be calculated,
	 * This keeps track of months that have been added to avoid duplicates.
	 */
	List<String> intermediateMonthsAdded = new ArrayList<String>(12);
	/*
	 * The string of all intermediate values to return to the requester.
	 */
	StringBuilder intermediateValues     = new StringBuilder();
	/**
	 * This indicates weather or not to build and send the intermediate
	 * values string back to the user.
	 */
	boolean includeIntermediateValues    = false;
	
	JsonData jsonData;

	
	public JsonDataBuilder(Properties env) {
		this.env = env;
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
		// This logic allows for a setting override of a mediation
		// since the most prevalent mediation might undesirable.
		// The statistics will use the default sort if none is given,
		// usually the default will be ascending for non-well samples.
		if (MediationType.DEFAULT.equals(this.mediation)) {
			this.mediation = mediation;
		}
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
	
	public JsonDataBuilder percentiles(String ... percentiles) {
		for (String percentile : percentiles) {
			this.percentiles.add(percentile);
		}
		return this;
	}
	
	public Map<String, BigDecimal>  buildPercentiles() {
		Map<String, BigDecimal> percentileValues = new HashMap<>();
		for (String percentile : this.percentiles) {
			String key = "P" + percentile;
			// these are "exact" percentiles and should not limit measured precision
			try {
				BigDecimal value = 
						new BigDecimal(percentile.trim())
						.divide(new BigDecimal("100"))
						.setScale(10);
				if (value.doubleValue()<0 || value.doubleValue()>1) {
					String msg = String.format("Invalid percentile value, %s", percentile);
					throw new NumberFormatException(msg);
				} else {
					percentileValues.put(key, value);
				}
			} catch (Exception e) {
				String msg = env.getError(ENV_INVALID_PERCENTILE, percentile);
				error(msg);
			}
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
			jsonData.overall = new JsonOverall("", 0, "", "", "", "", "", "", "", "", mediation);
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
	public JsonDataBuilder newOverallMedian(String newMedian) {
		JsonOverall jo = jsonData.overall;
		JsonOverall overall = new JsonOverall(jo.recordYears, jo.sampleCount,
				jo.latestPercentile, jo.latestValue, jo.valueMax, 
				newMedian, 
				jo.valueMin, jo.dateCalc, jo.dateMax, jo.dateMin, 
				mediation);
		
		jsonData.overall = overall;
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
	
	public List<Value> getIntermediateValuesList() {
		return intermediateValuesList;
	}
	
	public JsonDataBuilder includeIntermediateValues(Boolean includeIntermediateValues) {
		this.includeIntermediateValues = includeIntermediateValues;
		return this;
	}
	
	public boolean isIncludeIntermediateValues() {
		return includeIntermediateValues;
	}
	
	public String getIntermediateValues() {
		return intermediateValues.toString();
	}
	
	protected JsonDataBuilder intermediateValue(Value sample) {
		if ( ! isIncludeIntermediateValues()) {
			return this;
		}
		
		intermediateValues.append(sample.toCSV()).append("\n");
		
		return this;
	}
	
	public JsonDataBuilder intermediateValues(List<? extends Value> samples) {
		if(samples == null || samples.isEmpty()) {
			return this;
		}
		String month = samples.get(0).getMonth();
		LOGGER.trace("month add request {} ", month);
		if (this.intermediateMonthsAdded.contains(month)) {
			return this;
		}
		LOGGER.trace("month add perform {}", month);
		this.intermediateMonthsAdded.add(month);
		this.intermediateValuesList.addAll(samples);
		
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

	public JsonDataBuilder message(String msg) {
		jsonData.addMessage(msg);
		return this;
	}
	public JsonDataBuilder messages(List<String> msgs) {
		jsonData.addMessages(msgs);
		return this;
	}
	public Stream<String> messages() {
		return jsonData.messages.stream();
	}
	
	public JsonDataBuilder error(String msg) {
		jsonData.addError(msg);
		return this;
	}
	public JsonDataBuilder errors(List<String> msgs) {
		jsonData.addErrors(msgs);
		return this;
	}
	public Stream<String> errors() {
		return jsonData.errors.stream();
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
