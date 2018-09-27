package gov.usgs.wma.statistics.logic;

import java.util.List;

import gov.usgs.wma.statistics.app.Properties;
import gov.usgs.wma.statistics.model.JsonDataBuilder;
import gov.usgs.wma.statistics.model.Value;

public class OverallStatistics <S extends Value> extends StatisticsCalculator<S> {

	public OverallStatistics(Properties env, JsonDataBuilder builder) {
		super(env, builder);
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
		builder.minValue(minValue.value.toString());
		builder.maxValue(maxValue.value.toString());
		// number of samples
		builder.sampleCount(samples.size());
		// percentile statistics
		S medianValue = makeMedian(sortedByValue);
		builder.median(medianValue.value.toString());
				
		return builder;
	}

	public void findMinMaxDatesAndDateRange(List<S> samples, List<S> sortedByValue) {
		
		sortByDateOrder(samples); // ensure date order for sample sourced from db or tests
		S minDate =samples.get(0);
		S maxDate =samples.get(samples.size()-1);
		
		builder.latestValue(maxDate.value.toString());
		// date range
		builder.minDate(minDate.time);
		builder.maxDate(maxDate.time);
		// years of data
		builder.recordYears( yearDiff(maxDate.time, minDate.time).toString() );
	}

}
