package gov.usgs.wma.statistics.logic;

import java.util.List;

import gov.usgs.wma.statistics.model.JsonDataBuilder;
import gov.usgs.wma.statistics.model.Value;

public class OverallStatistics <S extends Value> extends StatisticsCalculator<S> {

	public OverallStatistics(JsonDataBuilder stats) {
		super(stats);
	}
	
	// TODO work this into the calculate model
	/**
	 * This calculates the overall series statistics: min, max, count, first, last, years, and percentile of the most recent
	 * @param samples time series data in temporal order
	 * @param sortedByValue  time series data in value order
	 * @return computed statistics map
	 */
	public JsonDataBuilder overallStats(List<S> samples, List<S> sortedByValue) {
		if (samples == null || samples.size() == 0) {
			return new JsonDataBuilder();
		}
		
		// after this samples is sorted by date for certain
		findMinMaxDatesAndDateRange(samples,sortedByValue); 
		
		S minValue = sortedByValue.get(0);
		S maxValue = sortedByValue.get(sortedByValue.size()-1);

		// value range
		stats.minValue(minValue.value.toString());
		stats.maxValue(maxValue.value.toString());
		// number of samples
		stats.sampleCount(samples.size());
		// percentile statistics
		S medianValue = makeMedian(sortedByValue);
		stats.median(medianValue.value.toString());
				
		return stats;
	}

	public void findMinMaxDatesAndDateRange(List<S> samples, List<S> sortedByValue) {
		
		sortByDateOrder(samples); // ensure date order for sample sourced from db or tests
		S minDate =samples.get(0);
		S maxDate =samples.get(samples.size()-1);
		
		stats.latestValue(maxDate.value.toString());
		// date range
		stats.minDate(minDate.time);
		stats.maxDate(maxDate.time);
		// years of data
		stats.recordYears( yearDiff(maxDate.time, minDate.time).toString() );
	}

}
