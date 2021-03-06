package gov.usgs.wma.statistics.logic;

import static gov.usgs.wma.statistics.app.Properties.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import gov.usgs.ngwmn.model.WLSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.wma.statistics.app.Properties;
import gov.usgs.wma.statistics.model.JsonDataBuilder;
import gov.usgs.wma.statistics.model.Value;

public class MonthlyStatistics<S extends Value> extends StatisticsCalculator<S> {
	private static final Logger LOGGER = LoggerFactory.getLogger(MonthlyStatistics.class);

	public MonthlyStatistics(Properties env, JsonDataBuilder builder) {
		super(env, builder);
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
	 */
	public Function<List<S>, List<S>> sortFunctionByQualifier() {
		return StatisticsCalculator::sortByValueOrderAscending;
	}
	
	
	public List<S> filterValuesByGivenMonth(List<S> samples, final String month) {
		final String paddedMonth =  Value.padMonth(month);
		List<S> monthSamples = samples.stream().filter(
			value -> {
				if (value == null || month == null) {
					return false;
				}
				return Value.padMonth(Value.monthUTC(value.time)).equals(paddedMonth);
			}).collect(Collectors.toList());
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
			
			// this needs to be calculated regardless of the month's qualification for use in other statistics
			// namely: overall median and latest percentile. (At the moment, the latest percentile recalculates.)
			List<S> monthlyMedians = medianMonthlyValues(monthSamples,  sortFunctionByQualifier());
			
			if ( doesThisMonthQualifyForStats(monthlyMedians) ) {
				monthlyCalculated = true;
				generatePercentiles(monthlyMedians, builder.buildPercentiles());
				builder.month(month);
				
				List<Value> monthYearlyMedians = generateMonthYearlyPercentiles(monthlyMedians);
				
				builder.minP50(monthYearlyMedians.get(0).value.toString());
				builder.maxP50(monthYearlyMedians.get( monthYearlyMedians.size()-1 ).value.toString());
				builder.sampleCount(monthlyMedians.size());

				builder.recordYears(""+sortSamplesByYear.keySet().size());
				builder.collect();
			}
		}
		
		LOGGER.trace("exited");
		return monthlyCalculated;
	}
	
	/**
	 * The non-GWW case will be that there must be value while the GWW case will be that there must be ten years.
	 * @param normalizeMultipleYearlyValues
	 * @return
	 */
	public boolean doesThisMonthQualifyForStats(List<S> normalizeMultipleYearlyValues) {
		boolean qualified = normalizeMultipleYearlyValues != null && normalizeMultipleYearlyValues.size()>0;
		if (!qualified) {
			return qualified;
		}
		int monthYears = uniqueYears(normalizeMultipleYearlyValues);
		qualified &= monthYears >= 10;

		if ( ! qualified && monthYears>0 ) {
			Value firstSample = normalizeMultipleYearlyValues.get(0);
			int missingCount = 10 - monthYears;
			String plural = missingCount>1 ? "s" :"";
			String monthName = sampleMonthName(firstSample);
			String msg = env.getMessage(ENV_MESSAGE_MONTHLY_DETAIL, monthName, missingCount, plural);
			builder.message(msg);
		}
		return qualified;
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
			List<S> yearSamples = samples.stream()
					.filter(yearly)
					.collect(Collectors.toList());
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
		int sampleCount = monthSamples.size();
		List<S> monthlyMedians = new LinkedList<>();

		sortValueByQualifier(monthSamples); // ensure proper sort.
		Map<String, List<S>> yearSamples = sortSamplesByYear(monthSamples);
		for (String year : yearSamples.keySet()) {
			List<S> samples = yearSamples.get(year);
			if (samples.size() > 1) {
				// have to remove the original values from the monthly list
				monthSamples.removeAll(samples);
				S medianSample = makeMedian(samples);
				monthlyMedians.add(medianSample);
			} else {
				monthlyMedians.addAll(samples);
			}
		}
		monthlyMedians = sortBy.apply(monthlyMedians);
		builder.intermediateValues(monthlyMedians);
		
		if (sampleCount > monthlyMedians.size()) {
			S firstSample = monthlyMedians.get(0);
			String monthName = sampleMonthName(firstSample);
			String msg = env.getMessage(ENV_MESSAGE_MONTHLY_MEDIANS, monthName);
			builder.message(msg);
		}
		return monthlyMedians;
	}
	
}
