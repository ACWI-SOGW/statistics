package gov.usgs.wma.statistics.logic;

import static gov.usgs.wma.statistics.model.Value.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.usgs.wma.statistics.model.Value;

public class OverallStatistics <S extends Value> extends StatisticsCalculator<S> {
	public static final String CALC_DATE     = "CALC_DATE";
	public static final String IS_RANKED     = "IS_RANKED";
	public static final String LATEST_PCTILE = "LATEST_PCTILE";
	public static final String LATEST_VALUE  = "LATEST_VALUE";
	public static final String MEDIATION     = "MEDIATION";
	public static final String MIN_DATE      = "MIN_DATE";
	public static final String MAX_DATE      = "MAX_DATE";
	public static final String MIN_VALUE     = "MIN_VALUE";
	public static final String MAX_VALUE     = "MAX_VALUE";
	public static final String MEDIAN        = "MEDIAN"; // P50
	public static final String RECORD_YEARS  = "RECORD_YEARS";

	
	// TODO work this into the calculate model
	/**
	 * This calculates the overall series statistics: min, max, count, first, last, years, and percentile of the most recent
	 * @param samples time series data in temporal order
	 * @param sortedByValue  time series data in value order
	 * @return computed statistics map
	 */
	public Map<String,String> overallStats(List<S> samples, List<S> sortedByValue) {
		if (samples == null || samples.size() == 0) {
			return new HashMap<String,String>();
		}
		
		Map<String,String> stats = findMinMaxDatesAndDateRange(samples,sortedByValue); // after this samples is sorted by date for certain
		
		S minValue = sortedByValue.get(0);
		S maxValue = sortedByValue.get(sortedByValue.size()-1);

		stats.put(IS_RANKED, "N"); // default not ranked status

		// value range
		stats.put(MIN_VALUE, minValue.value.toString());
		stats.put(MAX_VALUE, maxValue.value.toString());
		// number of samples
		stats.put(SAMPLE_COUNT, ""+samples.size());
		// percentile statistics
		S medianValue = makeMedian(sortedByValue);
		stats.put(MEDIAN, medianValue.value.toString());
		
		stats.put(CALC_DATE, DATE_FORMAT_FULL.format(new Date()));
		
		return stats;
	}

	public Map<String,String> findMinMaxDatesAndDateRange(List<S> samples, List<S> sortedByValue) {
		Map<String,String> stats = new HashMap<String,String>();
		sortByDateOrder(samples); // ensure date order for sample sourced from db or tests
		S minDate =samples.get(0);
		S maxDate =samples.get(samples.size()-1);
		
		stats.put(LATEST_VALUE, maxDate.value.toString());
		// date range
		stats.put(MIN_DATE, minDate.time);
		stats.put(MAX_DATE, maxDate.time);
		// years of data
		stats.put(RECORD_YEARS, ""+yearDiff(maxDate.time, minDate.time) );
		
		return stats;
	}

}
