package gov.usgs.wma.statistics.logic;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.wma.statistics.model.JsonDataBuilder;
import gov.usgs.wma.statistics.model.Value;

public class MonthlyStatistics<S extends Value> extends StatisticsCalculator<S> {
	private static final Logger LOGGER = LoggerFactory.getLogger(MonthlyStatistics.class);

	public MonthlyStatistics(JsonDataBuilder stats) {
		super(stats);
	}
	
	public String percentileBasedOnMonthlyData(S value, List<S> samplesByDate) {
		String month = Value.monthUTC(value.time);
		List<S> monthlySamples = filterValuesByGivenMonth(samplesByDate, month);
		S latestValue = monthlySamples.get( monthlySamples.size()-1 );
		sortValueByQualifier(monthlySamples);
		BigDecimal percentile = percentileOfValue(monthlySamples, latestValue, S::valueOf);
		return percentile.toString();
	}

	/**
	 * Override in subclass to implement custom sort by qualifier
	 * See WaterLevelStatistics for an example.
	 * @param monthlySamples
	 */
	public void sortValueByQualifier(List<S> monthlySamples) {
		sortByValueOrderAscending(monthlySamples);
	}
	/**
	 * Override in subclass to implement custom sort by qualifier.
	 * See WaterLevelStatistics for an example.
	 * @param monthlySamples
	 */
	public Function<List<S>, List<S>> sortFunctionByQualifier() {
		return StatisticsCalculator::sortByValueOrderAscending;
	}
	
	
	public List<S> filterValuesByGivenMonth(List<S> samples, final String month) {
		// using predicate because spring 3.x includes cglib that cannot compile lambdas
		Predicate<S> monthly = new Predicate<S>() {
			@Override
			public boolean test(S value) {
				if (value == null || month == null) {
					return false;
				}
				String paddedMonth =  ((month.length()==1) ?"0" :"")+month;
				return Value.monthUTC(value.time).equals(paddedMonth);
			}
		};
		List<S> monthSamples = samples.stream().filter(monthly).collect(Collectors.toList());
		return monthSamples;
	}

	// TODO work this into the calculate model
	/**
	 * @param sortedByValue a list of all site samples in sorted order by value
	 * @return a map of monthly maps of percentile data
	 */
	public boolean monthlyStats(List<S> sortedByValue) {
		LOGGER.trace("entered");
		
		boolean monthlyCalculated = false;
		if (sortedByValue == null || sortedByValue.size() == 0) {
			return monthlyCalculated;
		}
		
		for(int m=1; m<=12; m++) {
			String month = ""+m;
			List<S> monthSamples = filterValuesByGivenMonth(sortedByValue, month);
			Map<String, List<S>> sortSamplesByYear = sortSamplesByYear(monthSamples);
			List<S> normalizeMutlipleYearlyValues = medianMonthlyValues(monthSamples,  sortFunctionByQualifier());
			
			if ( doesThisMonthQualifyForStats(normalizeMutlipleYearlyValues) ) {
				monthlyCalculated = true;
				generatePercentiles(normalizeMutlipleYearlyValues, builder.buildPercentiles());
				builder.month(month);
				
				List<Value> monthYearlyMedians = generateMonthYearlyPercentiles(normalizeMutlipleYearlyValues);
				
				builder.minP50(monthYearlyMedians.get(0).value.toString());
				builder.maxP50(monthYearlyMedians.get( monthYearlyMedians.size()-1 ).value.toString());
				builder.sampleCount(monthSamples.size());

				builder.recordYears(""+sortSamplesByYear.keySet().size());
				builder.collect();
			}
		}
		
		LOGGER.trace("exited");
		return monthlyCalculated;
	}
	
	public boolean doesThisMonthQualifyForStats(List<S> normalizeMutlipleYearlyValues) {
		return normalizeMutlipleYearlyValues != null && normalizeMutlipleYearlyValues.size()>0;
	}

	/**
	 * @param samples a many year single month filtered sample list in value order
	 * @return a list of new samples holding the yearly medians in value order
	 */
	@SuppressWarnings("unchecked")
	protected List<Value> generateMonthYearlyPercentiles(List<S> samples) {
		samples = new ArrayList<>(samples);
		List<Value> monthYearlyMedians = new LinkedList<>(); 

		while (samples.size() > 0) {
			// calculate a year's median
			final String year = Value.yearUTC(samples.get(0).time);
			// using predicate because spring 3.x includes cglib that cannot compile lambdas
			Predicate<S> yearly = new Predicate<S>() {
				@Override
				public boolean test(S sample) {
					return year.equals( Value.yearUTC(sample.time) );
				}
			};
			List<S> yearSamples = samples.stream().filter(yearly).collect(Collectors.toList());
			BigDecimal value = valueOfPercentile( yearSamples, MEDIAN_PERCENTILE, S::valueOf);
			monthYearlyMedians.add( new Value("",value) );
			// remove the current year
			samples.removeAll(yearSamples);
		}
		// this cast works
		sortValueByQualifier((List<S>)monthYearlyMedians);
		return monthYearlyMedians;
	}
	
	public List<S> medianMonthlyValues(List<S> monthSamples, Function<List<S>, List<S>> sortBy) {
		List<S> normalizedSamples = new LinkedList<>();

		Map<String, List<S>> yearSamples = sortSamplesByYear(monthSamples);
		for (String year : yearSamples.keySet()) {
			List<S> samples = yearSamples.get(year);
			if (samples.size() > 1) {
				// have to remove the original values from the monthly list
				monthSamples.removeAll(samples);
				S medianSample = makeMedian(samples);
				normalizedSamples.add(medianSample);
			}
			else {
				normalizedSamples.addAll(samples);
			}
		}
		normalizedSamples = sortBy.apply(normalizedSamples);
		builder.intermediateValues(normalizedSamples);
		
		return normalizedSamples;
	}
	
}
