package gov.usgs.wma.statistics.logic;

import static gov.usgs.wma.statistics.logic.OverallStatistics.*;

//import static gov.usgs.wma.statistics.model.Value.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.wma.statistics.model.Value;

public class MonthlyStatistics<S extends Value, Q> extends StatisticsCalculator<S> {
	public static final String P50_MIN       = "P50_MIN";
	public static final String P50_MAX       = "P50_MAX";
	public static final String P10           = "P10";
	public static final String P25           = "P25";
	public static final String P50           = "P50";
	public static final String P75           = "P75";
	public static final String P90           = "P90";
	
	public static final Map<String, BigDecimal> PERCENTILES;
	static {
		PERCENTILES =  new HashMap<>();
		// these are exact percentiles and should not limit measured precision
		addPercentile(P10, "0.100000000");
		addPercentile(P25, "0.250000000");
		addPercentile(P50, MEDIAN_PERCENTIAL);
		addPercentile(P75, "0.750000000");
		addPercentile(P90, "0.900000000");
	}
	// Setter to allow for extra percentiles in string format
	public static void addPercentile(String name, String value) {
		addPercentile(name, new BigDecimal(value));
	}
	// Setter to allow for extra percentiles
	private static void addPercentile(String name, BigDecimal percentile) {
		PERCENTILES.put(name, percentile);
	}

	private final transient Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * This qualifier could be something like mediation type in water levels.
	 * There is "Above a Datum" or "Below Land" as qualifers.
	 */
	protected final Q qualifier;
	
	public MonthlyStatistics(Q qualifier) {
		this.qualifier = qualifier;
	}
	
	public String percentileBasedOnMonthlyData(S value, List<S> samplesByDate) {
		String month = monthUTC(value.time);
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
				return monthUTC(value.time).equals(paddedMonth);
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
	public Map<String,Map<String,String>> monthlyStats(List<S> sortedByValue) {
		Map<String,Map<String,String>> stats = new HashMap<>();
		if (sortedByValue == null || sortedByValue.size() == 0) {
			return stats;
		}
		
		for(int m=1; m<=12; m++) {
			String month = ""+m;
			logger.debug("MONTH="+m);
			logger.debug("SBVC="+sortedByValue.size());
			List<S> monthSamples = filterValuesByGivenMonth(sortedByValue, month);
			logger.debug("MSC="+monthSamples.size());
			String monthCount = ""+monthSamples.size();
			Map<String, List<S>> sortSamplesByYear = sortSamplesByYear(monthSamples);
			List<S> normalizeMutlipleYearlyValues = normalizeMutlipleYearlyValues(monthSamples,  sortFunctionByQualifier());
			logger.debug("NMYVC="+normalizeMutlipleYearlyValues.size());
			
			if ( doesThisMonthQualifyForStats(normalizeMutlipleYearlyValues) ) {
				Map<String,String> monthStats = generatePercentiles(normalizeMutlipleYearlyValues, PERCENTILES);
				stats.put(month, monthStats);
				
				List<Value> monthYearlyMedians = generateMonthYearlyPercentiles(normalizeMutlipleYearlyValues);
				monthStats.put(P50_MIN, monthYearlyMedians.get(0).value.toString());
				monthStats.put(P50_MAX, monthYearlyMedians.get( monthYearlyMedians.size()-1 ).value.toString());
				monthStats.put(SAMPLE_COUNT, monthCount);

				int recordYears = sortSamplesByYear.keySet().size();
				monthStats.put(RECORD_YEARS, ""+recordYears);
			}
		}
		
		return stats;
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
			final String year = yearUTC(samples.get(0).time);
			// using predicate because spring 3.x includes cglib that cannot compile lambdas
			Predicate<S> yearly = new Predicate<S>() {
				@Override
				public boolean test(S sample) {
					return year.equals( yearUTC(sample.time) );
				}
			};
			List<S> yearSamples = samples.stream().filter(yearly).collect(Collectors.toList());
			BigDecimal value = valueOfPercentile( yearSamples, PERCENTILES.get(P50), S::valueOf);
			monthYearlyMedians.add( new Value("",value) );
			// remove the current year
			samples.removeAll(yearSamples);
		}
		// this cast works
		sortValueByQualifier((List<S>)monthYearlyMedians);
		return monthYearlyMedians;
	}
	
}
