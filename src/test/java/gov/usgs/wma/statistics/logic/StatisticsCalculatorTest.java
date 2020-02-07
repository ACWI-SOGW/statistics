package gov.usgs.wma.statistics.logic;

import static gov.usgs.wma.statistics.logic.SigFigMathUtil.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.usgs.ngwmn.model.MediationType;
import gov.usgs.wma.statistics.app.Properties;
import gov.usgs.wma.statistics.model.JsonDataBuilder;
import gov.usgs.wma.statistics.model.Value;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration //(locations = { "/applicationContext_mock.xml" })
public class StatisticsCalculatorTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsCalculatorTest.class);
	
	public static final String P10 = "P10";
	public static final String P25 = "P25";
	public static final String P50 = "P50";
	public static final String P75 = "P75";
	public static final String P90 = "P90";
	
	static final Map<String, BigDecimal> PERCENTILES = new JsonDataBuilder(null).buildPercentiles();
	
	@Mock
	Environment spring;
	Properties env;
	StatisticsCalculator<Value> stats;
	MonthlyStatistics<Value> monthlyStats;
	

	private static Value createSample(String time, String value) {
		BigDecimal val = null;
		if (null != value) {
			val = new BigDecimal(value);
		}
		return new Value(time, val);
	}
	private static Value createSample(String time, String value, boolean provisional) {
		Value val = createSample(time, value);
		val.setProvsional(provisional);
		return val;
	}
	/**
	 * Helper class to sort samples by value. This is a simple sort for testing.
	 */
	public static class OrderedSamples extends LinkedList<Value> {
		private static final long serialVersionUID = 1L;

		Comparator<Value> comparator;

		public OrderedSamples() {
			// default sort in ascending order.
			this.comparator = new Comparator<Value>() {
				public int compare(Value a, Value b) {
						return a.value.compareTo( b.value );
				};
			};;
		}
		public OrderedSamples(Comparator<Value> comparator) {
			this.comparator = comparator;
		}

		@Override
		public boolean add(Value e) {
			if (e==null || e.value==null) {
				return false;
			}

			int i = 0;
			for (Value sample : this) {
				if (comparator.compare(e, sample) < 0) {
					break;
				}
				i++;
			}
			super.add(i,e);
			return true;
		}
	}

	@Before
	public void setup() {
		env = new Properties().setEnvironment(spring);
		JsonDataBuilder builder = new JsonDataBuilder(env).mediation(MediationType.AboveDatum);
		stats = new StatisticsCalculator<Value>(env, builder);
		monthlyStats = new MonthlyStatistics<Value>(env, builder);
	}


	@Test
	public void test_yearDiff_9yr_simple() throws Exception {
		BigDecimal yrs = StatisticsCalculator.yearDiff("2009-01-02", "2000-01-01");
		assertEquals("9.0", yrs.toPlainString());
	}
	@Test
	public void test_yearDiff_10yr_simple() throws Exception {
		BigDecimal yrs = StatisticsCalculator.yearDiff("2010-01-02", "2000-01-01");
		assertEquals("10.0", yrs.toPlainString());
	}
	@Test
	public void test_yearDiff_10yr_midmonth() throws Exception {
		BigDecimal yrs = StatisticsCalculator.yearDiff("2010-02-15", "2000-02-14");
		assertEquals("10.0", yrs.toPlainString());
	}
	@Test
	public void test_yearDiff_10yr_minOneDay() throws Exception {
		BigDecimal yrs = StatisticsCalculator.yearDiff("2010-02-15", "2000-02-16");
		assertEquals("10.0", yrs.toPlainString());
	}
	@Test
	public void test_yearDiff_10yr_plusOneDay() throws Exception {
		BigDecimal yrs = StatisticsCalculator.yearDiff("2010-02-15", "2000-02-14");
		assertEquals("10.0", yrs.toPlainString());
	}
	@Test
	public void test_yearDiff_11yr_easy() throws Exception {
		BigDecimal yrs = StatisticsCalculator.yearDiff("2011-01-01", "2000-01-01");
		assertEquals("11.0", yrs.toPlainString());
	}

	@Test
	public void test_daysDiff_1day() throws Exception {
		String date1 = "2015-12-12";
		String date2 = "2015-12-13";
		BigDecimal actual = StatisticsCalculator.daysDiff(date2,date1);
		assertEquals("Expect 1 day", 1, actual.intValue());
	}
	@Test
	public void test_daysDiff_406days() throws Exception {
		String date1 = "2014-11-14";
		String date2 = "2015-12-25";
		BigDecimal actual = StatisticsCalculator.daysDiff(date2,date1);
		assertEquals("Expect 406 day", 406, actual.intValue());
	}
	@Test
	public void test_fixMissingMonthAndDay_doNothing() throws Exception {
		String expect = "2015-12-12";
		String actual = StatisticsCalculator.fixMissingMonthAndDay(expect);
		assertEquals("Expect fine date to remain unchaanged", expect, actual);
	}
	public void test_fixMissingMonthAndDay_midMonth() throws Exception {
		String expect = "2015-12";
		String actual = StatisticsCalculator.fixMissingMonthAndDay(expect);
		assertEquals("Expect fine date to be the 15h of the month", expect+"-15", actual);
	}
	public void test_fixMissingMonthAndDay_midYear() throws Exception {
		String expect = "2015";
		String actual = StatisticsCalculator.fixMissingMonthAndDay(expect);
		assertEquals("Expect fine date to be June 30th of the given month", expect+"-06-30", actual);
	}

	@Test
	public void test_removeNulls_noNulls() throws Exception {
		Value min = createSample("2005-12-10T04:15:00-05:00", "1.0");
		Value mid = createSample("2010-06-10T04:15:00-05:00", "1.5");
		Value max = createSample("2015-06-10T04:15:00-05:00", "2.0");
		List<Value> samples = new ArrayList<>(3);
		samples.add(mid);
		samples.add(min);
		samples.add(max);
		stats.removeNulls(samples, "well id");
		assertEquals("size should remain unchanged", 3, samples.size());
	}
	@Test
	public void test_removeNulls_nullInstance() throws Exception {
		Value min = createSample("2005-12-10T04:15:00-05:00", "1.0");
		Value mid = createSample("2010-06-10T04:15:00-05:00", "1.5");
		Value max = createSample("2015-06-10T04:15:00-05:00", "2.0");
		List<Value> samples = new ArrayList<>(3);
		samples.add(mid);
		samples.add(min);
		samples.add(max);
		samples.add(null);
		assertEquals("ensuring that the null has been added for a valid boundy condition", 4, samples.size());
		stats.removeNulls(samples, "well id");
		assertEquals("null instance should be removed", 3, samples.size());
	}
	@Test
	public void test_removeNulls_nullValue() throws Exception {
		Value min = createSample("2005-12-10T04:15:00-05:00", "1.0");
		Value mid = createSample("2010-06-10T04:15:00-05:00", "1.5");
		Value n1 = createSample("2015-06-10T04:15:00-05:00", null);
		Value n2 = createSample(null, "2.0");
		Value n3 = createSample(null, null);
		List<Value> samples = new ArrayList<>(3);
		samples.add(mid);
		samples.add(min);
		samples.add(n1);
		samples.add(n2);
		samples.add(n3);
		stats.removeNulls(samples, "well id");
		assertEquals("null instance should be removed", 2, samples.size());
	}

	@Test
	public void test_overallStats_OrderSamples_3() throws Exception {
		Value min = createSample("2005-06-10T04:15:00-05:00", "1.0");
		Value mid = createSample("2010-06-10T04:15:00-05:00", "1.5");
		Value max = createSample("2015-06-10T04:15:00-05:00", "2.0");
		List<Value> samples = new OrderedSamples();
		samples.add(mid);
		samples.add(min);
		samples.add(max);
		assertEquals("Expect min to be first", min, samples.get(0) );
		assertEquals("Expect mid to be middle", mid, samples.get(1) );
		assertEquals("Expect max to be last", max, samples.get(2) );
	}

	@Test
	public void test_percentileValue_2_50pct() throws Exception {
		Value min = createSample("2005-06-10T04:15:00-05:00", "1.0");
		Value max = createSample("2015-06-10T04:15:00-05:00", "2.0");
		List<Value> samples = new OrderedSamples();
		samples.add(min);
		samples.add(max);
		BigDecimal pct50 = stats.valueOfPercentile(samples, new BigDecimal(".5"), Value::valueOf);
		assertEquals("Expect 50% percetile to be the middle value", "1.5", pct50.toPlainString() );
	}

	// a new arbitrary type that hold a value for statistical evaluation
	public static class OtherValue extends Value {
		public String someValue;
		public OtherValue(String value) {
			super(StatisticsCalculator.today(), BigDecimal.ZERO);
			someValue = value;
		}
		public static BigDecimal valueOf(OtherValue other) {
			return new BigDecimal(other.someValue);
		}
	}

	@Test
	// This test shows the flexibility in using lambdas
	public void test_percentileValue_NonWLSample() throws Exception {
		StatisticsCalculator<OtherValue> otherStats = new StatisticsCalculator<>(env);
		OtherValue min = new OtherValue("1.0");
		OtherValue max = new OtherValue("2.0");
		List<OtherValue> samples = new LinkedList<>();
		samples.add(min);
		samples.add(max);
		// the valueOf lambda can be any type of mapping not just a getter
		BigDecimal pct50 = otherStats.valueOfPercentile(samples, new BigDecimal(".5"), OtherValue::valueOf);
		assertEquals("Expect 50% percetile to be the middle value", "1.5", pct50.toPlainString() );
	}

	@Test
	public void test_percentileValue_3_50pct() throws Exception {
		Value min = createSample("2005-06-10T04:15:00-05:00", "1.0");
		Value mid = createSample("2010-06-10T04:15:00-05:00", "1.5");
		Value max = createSample("2015-06-10T04:15:00-05:00", "2.0");
		List<Value> samples = new OrderedSamples();
		samples.add(mid);
		samples.add(min);
		samples.add(max);
		BigDecimal pct50 = stats.valueOfPercentile(samples, new BigDecimal(".5"), Value::valueOf);
		assertEquals("Expect 50% percetile to be the middle value", mid.value.toPlainString(), pct50.toPlainString());
	}
	@Test
	public void test_percentileValue_3_25pct_0pct() throws Exception {
		Value min = createSample("2005-06-10T04:15:00-05:00", "1.0");
		Value mid = createSample("2010-06-10T04:15:00-05:00", "1.5");
		Value max = createSample("2015-06-10T04:15:00-05:00", "2.0");
		List<Value> samples = new OrderedSamples();
		samples.add(mid);
		samples.add(min);
		samples.add(max);
		BigDecimal pct0 = stats.valueOfPercentile(samples, new BigDecimal("0"), Value::valueOf);
		assertEquals("Expect 100% percetile to be the min value", min.value.toPlainString(), pct0.toPlainString() );
		// just like 0%, 25% is 1/(N+1) = 1/(3+1)
		BigDecimal pct25 = stats.valueOfPercentile(samples, new BigDecimal(".25"), Value::valueOf);
		assertEquals("Expect 25% percetile to be the min value", min.value.toPlainString(), pct25.toPlainString() );
	}
	@Test
	public void test_percentileValue_3_100pct_75pct() throws Exception {
		Value min = createSample("2005-06-10T04:15:00-05:00", "1.0");
		Value mid = createSample("2010-06-10T04:15:00-05:00", "1.5");
		Value max = createSample("2015-06-10T04:15:00-05:00", "2.0");
		List<Value> samples = new OrderedSamples();
		samples.add(mid);
		samples.add(min);
		samples.add(max);
		BigDecimal pct100 = stats.valueOfPercentile(samples, new BigDecimal("1"), Value::valueOf);
		assertEquals("Expect 100% percetile to be the max value", max.value.toPlainString(), pct100.toPlainString() );
		// just like 100%, 75% is N/(N+1) = 3/(3+1)
		BigDecimal pct75 = stats.valueOfPercentile(samples, new BigDecimal(".75"), Value::valueOf);
		assertEquals("Expect 75% percetile to be the max value", max.value.toPlainString(), pct75.toPlainString() );
	}
	@Test
	public void test_percentileValue_3_74pct_26pct_SigFig() throws Exception {
		Value min = createSample("2005-06-10T04:15:00-05:00", "1.00");
		Value mid = createSample("2010-06-10T04:15:00-05:00", "1.50");
		Value max = createSample("2015-06-10T04:15:00-05:00", "2.00");
		List<Value> samples = new OrderedSamples();
		samples.add(mid);
		samples.add(min);
		samples.add(max);
		BigDecimal pct26 = stats.valueOfPercentile(samples, new BigDecimal(".26"), Value::valueOf);
		// .26(3+1) = 1.04, index 1-1 is 0 for value 1.0, .04 (1.5-1) = .02, 1+.02 = 1.02
		assertEquals("Expect 74% percetile to be just less than max", "1.02", pct26.toPlainString() );

		BigDecimal pct74 = stats.valueOfPercentile(samples, new BigDecimal(".74"), Value::valueOf);
		// .74(3+1) = 2.96, index 2-1 is 1 for value 1.5, .96 (2-1.5) = .48, 1.5+.48 = 1.98
		assertEquals("Expect 74% percetile to be just less than max", "1.98", pct74.toPlainString() );
	}


	@Test
	public void test_percentileOfValue_12_75pct_90pct_SigFig_NIST() throws Exception {
		List<Value> samples = new OrderedSamples();
		samples.add( createSample("2015-01-10T04:15:00-05:00", "95.1772") );
		samples.add( createSample("2015-02-10T04:15:00-05:00", "95.1567") );
		samples.add( createSample("2015-03-10T04:15:00-05:00", "95.1937") );
		samples.add( createSample("2015-04-10T04:15:00-05:00", "95.1959") );
		samples.add( createSample("2015-05-10T04:15:00-05:00", "95.1442") );
		samples.add( createSample("2015-06-10T04:15:00-05:00", "95.0610") );
		samples.add( createSample("2015-07-10T04:15:00-05:00", "95.1591") );
		samples.add( createSample("2015-08-10T04:15:00-05:00", "95.1195") );
		samples.add( createSample("2015-09-10T04:15:00-05:00", "95.1065") );
		samples.add( createSample("2015-10-10T04:15:00-05:00", "95.0925") );
		samples.add( createSample("2015-11-10T04:15:00-05:00", "95.1990") );
		samples.add( createSample("2015-12-10T04:15:00-05:00", "95.1682") );

		// need to include the extra .750 in order to not loose precision on the data - this is an exact percent

		BigDecimal val23 = stats.valueOfPercentile(samples, new BigDecimal(".307692"), Value::valueOf);
		assertEquals("Expect "+samples.get(3).value+" to be ", samples.get(3).value.toPlainString(), val23.toPlainString() );

		BigDecimal pct23 = StatisticsCalculator.percentileOfValue(samples, samples.get(3), Value::valueOf);
		assertEquals("Expect "+samples.get(3).value+" to be 23% percetile", "0.307692", pct23.toPlainString() );
	}


	// NIST example http://www.itl.nist.gov/div898/handbook/prc/section2/prc262.htm
	@Test
	public void test_percentileValue_12_75pct_90pct_SigFig_NIST() throws Exception {
		List<Value> samples = new OrderedSamples();
		samples.add( createSample("2015-01-10T04:15:00-05:00", "95.1772") );
		samples.add( createSample("2015-02-10T04:15:00-05:00", "95.1567") );
		samples.add( createSample("2015-03-10T04:15:00-05:00", "95.1937") );
		samples.add( createSample("2015-04-10T04:15:00-05:00", "95.1959") );
		samples.add( createSample("2015-05-10T04:15:00-05:00", "95.1442") );
		samples.add( createSample("2015-06-10T04:15:00-05:00", "95.0610") );
		samples.add( createSample("2015-07-10T04:15:00-05:00", "95.1591") );
		samples.add( createSample("2015-08-10T04:15:00-05:00", "95.1195") );
		samples.add( createSample("2015-09-10T04:15:00-05:00", "95.1065") );
		samples.add( createSample("2015-10-10T04:15:00-05:00", "95.0925") );
		samples.add( createSample("2015-11-10T04:15:00-05:00", "95.1990") );
		samples.add( createSample("2015-12-10T04:15:00-05:00", "95.1682") );

		// need to include the extra .750 in order to not loose precision on the data - this is an exact percent
		BigDecimal pct750 = stats.valueOfPercentile(samples, new BigDecimal(".750"), Value::valueOf);
		assertEquals("Expect 75% percetile to be 95.1896", "95.1896", pct750.toPlainString() );
		BigDecimal pct75 = stats.valueOfPercentile(samples, new BigDecimal(".75"), Value::valueOf);
		assertEquals("Expect 75% percetile to be 95.189",  "95.189", pct75.toPlainString() );

		BigDecimal pct90 = stats.valueOfPercentile(samples, new BigDecimal(".90"), Value::valueOf);
		assertEquals("Expect 90% percetile to be 95.1981", "95.1981", pct90.toPlainString() );
	}


	@Test
	public void test_collectionsSort() throws Exception {
		List<Value> samples = new LinkedList<>();
		samples.add( createSample("2015-01-10T04:15:00-05:00", "95.1772") );
		samples.add( createSample("2015-02-10T04:15:00-05:00", "95.1567") );
		samples.add( createSample("2015-03-10T04:15:00-05:00", "95.1937") );
		samples.add( createSample("2015-04-10T04:15:00-05:00", "95.1959") );
		samples.add( createSample("2015-05-10T04:15:00-05:00", "95.1442") );
		samples.add( createSample("2015-06-10T04:15:00-05:00", "95.0610") );
		samples.add( createSample("2015-07-10T04:15:00-05:00", "95.1591") );
		samples.add( createSample("2015-08-10T04:15:00-05:00", "95.1195") );
		samples.add( createSample("2015-09-10T04:15:00-05:00", "95.1065") );
		samples.add( createSample("2015-10-10T04:15:00-05:00", "95.0925") );
		samples.add( createSample("2015-11-10T04:15:00-05:00", "95.1990") );
		samples.add( createSample("2015-12-10T04:15:00-05:00", "95.1682") );

		List<Value> sorted = new LinkedList<>(samples);
		StatisticsCalculator.sortByValueOrderAscending(sorted);

		assertEquals("Expect sorted to be the same size", samples.size(), sorted.size() );
		assertEquals("Expect first to be smallest", samples.get(5), sorted.get(0) );
		assertEquals("Expect last to be largest", samples.get(10), sorted.get(11) );
	}

	public String randomNumber(int length) {
		String digits = "0123456789";

		StringBuilder number = new StringBuilder();
		for (int d=0; d<length; d++) {
			number.append(  digits.charAt( (int)(Math.random() * digits.length()) )  );
		}

		return number.toString();
	}
	@Ignore // most of the time this is true but sometimes the arrangement favors the simple sort
	// the quick sort is faster is with larger data sets and only microseconds slower than simple sort on tiny data sets
	public void test_collectionsSort_vs_simple_3000() throws Exception {

		long start = System.currentTimeMillis();
		List<Value> samples = new LinkedList<>();
		for (int i=0; i<3000; i++) {
			samples.add( createSample("", randomNumber(10) ) );
		}
		long genDur = System.currentTimeMillis() - start;
		LOGGER.trace("gen done " + genDur);

		start = System.currentTimeMillis();
		List<Value> sorted = new LinkedList<>(samples);
		StatisticsCalculator.sortByValueOrderAscending(sorted);
		long quick = System.currentTimeMillis() - start;
		LOGGER.trace("quick done " + quick);

		start = System.currentTimeMillis();
		List<Value> simplesort = new OrderedSamples();
		for(Value sample : samples) {
			simplesort.add(sample);
		}
		long simple = System.currentTimeMillis() - start;
		LOGGER.trace("simple done " + simple);

		assertEquals("Expect quicksorted to be the same size", samples.size(), sorted.size() );
		assertEquals("Expect simplesorted to be the same size", samples.size(), simplesort.size() );
		assertTrue("Expect quicksorted to be the faster most of the time (there is a performace variation based on the random data)", quick <= simple);
	}
	@Ignore // most of the time this is true but sometimes the arrangement favors the simple sort
	// the only time the simple sort is faster is with small data sets where we are talking about microseconds faster
	public void test_collectionsSort_vs_simple_500() throws Exception {

		long start = System.currentTimeMillis();
		List<Value> samples = new LinkedList<>();
		for (int i=0; i<100; i++) {
			samples.add( createSample("", randomNumber(10) ) );
		}
		long genDur = System.currentTimeMillis() - start;
		LOGGER.trace("gen done " + genDur);

		start = System.currentTimeMillis();
		List<Value> sorted = new LinkedList<>(samples);
		StatisticsCalculator.sortByValueOrderAscending(sorted);
		long quick = System.currentTimeMillis() - start;
		LOGGER.trace("quick  done " + quick);

		start = System.currentTimeMillis();
		List<Value> simplesort = new OrderedSamples();
		for(Value sample : samples) {
			simplesort.add(sample);
		}
		long simple = System.currentTimeMillis() - start;
		LOGGER.trace("simple done " + simple);

		assertEquals("Expect quicksorted to be the same size", samples.size(), sorted.size() );
		assertEquals("Expect simplesorted to be the same size", samples.size(), simplesort.size() );
		assertTrue("Expect quicksorted to be the slower most of the time (there is a performace variation based on the random data)", (quick >= simple));
		LOGGER.trace("quick   " + quick);
		LOGGER.trace("simple  " + simple);
	}


	@Test
	public void test_generatePercentiles_ascendingForAboveDatum() throws Exception {
		List<Value> samples = new LinkedList<>();
		samples.add( createSample("2015-01-10T04:15:00-05:00", "95.1772") );
		samples.add( createSample("2015-02-10T04:15:00-05:00", "95.1567") );
		samples.add( createSample("2015-03-10T04:15:00-05:00", "95.1937") );
		samples.add( createSample("2015-04-10T04:15:00-05:00", "95.1959") );
		samples.add( createSample("2015-05-10T04:15:00-05:00", "95.1442") );
		samples.add( createSample("2015-06-10T04:15:00-05:00", "95.0610") );
		samples.add( createSample("2015-07-10T04:15:00-05:00", "95.1591") );
		samples.add( createSample("2015-08-10T04:15:00-05:00", "95.1195") );
		samples.add( createSample("2015-09-10T04:15:00-05:00", "95.1065") );
		samples.add( createSample("2015-10-10T04:15:00-05:00", "95.0925") );
		samples.add( createSample("2015-11-10T04:15:00-05:00", "95.1990") );
		samples.add( createSample("2015-12-10T04:15:00-05:00", "95.1682") );
		List<Value> sorted = new LinkedList<>(samples);
		StatisticsCalculator.sortByValueOrderAscending(sorted);
		JsonDataBuilder percentiles = stats.generatePercentiles(sorted, PERCENTILES);
		// as you might expect the values are low for low percentile and high for high percentile because measured above a datum
		// 2020-02-05 round HALF_DOWN changes this to .0004 from .0005 when rounding HALF_UP
		assertEquals("Expect P10 to be ", "95.0704", percentiles.get(P10));
		// 2020-02-05 round HALF_DOWN changes this to .0007 from .0008 when rounding HALF_UP
		assertEquals("Expect P25 to be ", "95.1097", percentiles.get(P25));
		assertEquals("Expect P50 to be ", "95.1579", percentiles.get(P50));
		assertEquals("Expect P75 to be ", "95.1896", percentiles.get(P75));
		assertEquals("Expect P90 to be ", "95.1981", percentiles.get(P90));
	}
	@Test
	public void test_generatePercentiles_descendingForBelowSurface() throws Exception {
		List<Value> samples = new LinkedList<>();
		samples.add( createSample("2015-01-10T04:15:00-05:00", "95.1772") );
		samples.add( createSample("2015-02-10T04:15:00-05:00", "95.1567") );
		samples.add( createSample("2015-03-10T04:15:00-05:00", "95.1937") );
		samples.add( createSample("2015-04-10T04:15:00-05:00", "95.1959") );
		samples.add( createSample("2015-05-10T04:15:00-05:00", "95.1442") );
		samples.add( createSample("2015-06-10T04:15:00-05:00", "95.0610") );
		samples.add( createSample("2015-07-10T04:15:00-05:00", "95.1591") );
		samples.add( createSample("2015-08-10T04:15:00-05:00", "95.1195") );
		samples.add( createSample("2015-09-10T04:15:00-05:00", "95.1065") );
		samples.add( createSample("2015-10-10T04:15:00-05:00", "95.0925") );
		samples.add( createSample("2015-11-10T04:15:00-05:00", "95.1990") );
		samples.add( createSample("2015-12-10T04:15:00-05:00", "95.1682") );
		List<Value> sorted = new LinkedList<>(samples);
		StatisticsCalculator.sortByValueOrderDescending(sorted); // <--------  sorting DESCENDING instead of Ascending
		JsonDataBuilder percentiles = stats.generatePercentiles(sorted, PERCENTILES);
		// NOTICE: the values are low for high percentile and high for low percentile because measured below surface
		assertEquals("Expect P10 to be ", "95.1981", percentiles.get(P10));
		assertEquals("Expect P25 to be ", "95.1896", percentiles.get(P25));
		assertEquals("Expect P50 to be ", "95.1579", percentiles.get(P50));
		// 2020-02-05 round HALF_DOWN changes this to .0007 from .0008 when rounding HALF_UP
		assertEquals("Expect P75 to be ", "95.1097", percentiles.get(P75));
		// this one is not exactly the opposite of 10% because of rounding rules and data order - but it is close
		// 2020-02-05 changing the calculation for sort down make this result .0005 from .0004 when making no adjustment
		assertEquals("Expect P90 to be ", "95.0705", percentiles.get(P90));
	}


	// this is a helper method to convert JSON to Maps
	@SuppressWarnings("unchecked")
	protected Map<String, Object> calculationsJsonToMap(String json) {
		Map<String, Object> calculations = new HashMap<>();
		try {
			calculations = new ObjectMapper().readValue(json, Map.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return calculations;
	}
	// this is a helper method to convert JSON to Maps
	@SuppressWarnings("unchecked")
	protected Map<String,String> extractOverall(String json) {
		Map<String, Object> calculations = calculationsJsonToMap(json);
		Map<String,String> overall = (Map<String,String>) calculations.get("overall");
		return overall;
	}
	// this is a helper method to convert JSON to Maps
	@SuppressWarnings("unchecked")
	protected Map<String,Map<String,String>> extractMonthly(String json) {
		Map<String, Object> calculations = calculationsJsonToMap(json);
		Map<String,Map<String,String>> monthly = (Map<String,Map<String,String>>) calculations.get("monthly");
		return monthly;
	}


	@Test
	public void test_removeProvisional() throws Exception {
		// note that all previous calculate tests prove that 100% non-provisional collections are retained

		Value defaultRetained  = createSample("2015-05-10T04:15:00-05:00", "95.1772");
		Value explicitRetained = createSample("2015-05-11T04:15:00-05:00", "95.1567", false);
		Value explicitRemoved  = createSample("2015-05-12T04:15:00-05:00", "95.1937", true);
		Value recentRemoved    = createSample("2015-05-13T04:15:00-05:00", "95.1959", true);
		Value nullRemoved      = null;

		List<Value> samples = new LinkedList<>();
		samples.add( defaultRetained );    // should be retained because of default false
		samples.add( explicitRetained );  // should be retained because not provisional
		samples.add( explicitRemoved );  // should be REMOVED because provisional
		samples.add( nullRemoved );     // should be REMOVED because NULL
		samples.add( recentRemoved );  // should be retained because most recent

		assertTrue( samples.contains(nullRemoved) ); // just checking that the null is really added
		stats.removeProvisional(samples,"testing");

		assertFalse("nulls should be removed as well as provisional", samples.contains(nullRemoved) );
		assertTrue("should be retained because of default false", samples.contains(defaultRetained));
		assertTrue("should be retained because not provisional",  samples.contains(explicitRetained));
		assertFalse("should be REMOVED because provisional",      samples.contains(explicitRemoved));
		assertFalse("should be retained because most recent",     samples.contains(recentRemoved));
	}
	

	@Test
	public void test_monthlyYearCount_low() throws Exception {
		List<Value> samples = new LinkedList<>();
		// here are some extra data points in months that have a <10 year window and excluded for calculation
		samples.add( createSample("2013-08-10T04:15:00-05:00", "93.1937") );
		samples.add( createSample("2013-08-11T04:15:00-05:00", "93.1937") );
		samples.add( createSample("2013-08-10T04:15:00-05:00", "93.1937") );

		int count = stats.uniqueYears(samples);
		assertEquals("Expect that when the given record years is 1",1, count);
	}

	@Test
	public void test_monthlyYearCount_mix() throws Exception {
		List<Value> samples = new LinkedList<>();
		// here are some extra data points in months that have a <10 year window and excluded for calculation
		samples.add( createSample("2013-08-10T04:15:00-05:00", "93.1937") );
		samples.add( createSample("2013-08-11T04:15:00-05:00", "93.1937") );
		samples.add( createSample("2023-09-10T04:15:00-05:00", "93.1937") );

		int count2 = stats.uniqueYears(samples);
		assertEquals("Expect that when the given data has more than one month then it return a uniques month count for the first month",2, count2);
	}

	@Test
	public void test_monthlyYearCount_10() throws Exception {
		List<Value> samples = new LinkedList<>();
		// here are some extra data points in months that have a <10 year window and excluded for calculation
		samples.add( createSample("2001-08-10T04:15:00-05:00", "93.1937") );
		samples.add( createSample("2002-08-11T04:15:00-05:00", "93.1937") );
		samples.add( createSample("2003-09-10T04:15:00-05:00", "93.1937") );
		samples.add( createSample("2004-08-10T04:15:00-05:00", "93.1937") );
		samples.add( createSample("2005-08-11T04:15:00-05:00", "93.1937") );
		samples.add( createSample("2006-09-10T04:15:00-05:00", "93.1937") );
		samples.add( createSample("2007-08-10T04:15:00-05:00", "93.1937") );
		samples.add( createSample("2008-08-11T04:15:00-05:00", "93.1937") );
		samples.add( createSample("2009-09-10T04:15:00-05:00", "93.1937") );
		samples.add( createSample("2010-09-10T04:15:00-05:00", "93.1937") );

		int count10 = stats.uniqueYears(samples);
		assertEquals("While the difference of 2010-2001 is 9 we expect 10",10, count10);
	}


	//WL_452012093412701_MORE_72019

	@Test
	public void test_rounding_vs_truncation_vs_sigfigs() {
		List<Value> samples = new LinkedList<>();
		samples.add( createSample("2013-11-10T04:15:00-05:00", "12.27") );
		samples.add( createSample("2012-11-10T04:15:00-05:00", "11.97") );
		samples.add( createSample("2009-11-10T04:15:00-05:00", "11.43") );
		samples.add( createSample("1978-11-10T04:15:00-05:00", "7.3") );
		samples.add( createSample("1971-11-10T04:15:00-05:00", "4.9") );
		samples.add( createSample("1973-11-10T04:15:00-05:00", "4.8") );
		samples.add( createSample("1970-11-10T04:15:00-05:00", "4.5") );
		samples.add( createSample("1968-11-10T04:15:00-05:00", "3.9") );
		samples.add( createSample("1965-11-10T04:15:00-05:00", "2.9") );
		samples.add( createSample("1967-11-10T04:15:00-05:00", "2.6") );
		samples.add( createSample("1966-11-10T04:15:00-05:00", "2.3") );
		samples.add( createSample("1964-11-10T04:15:00-05:00", "2.2") );
		samples.add( createSample("1962-11-10T04:15:00-05:00", "1.7") );

		BigDecimal p25 = stats.valueOfPercentile(samples, PERCENTILES.get(P25), Value::valueOf);

		// 2020-02-05 changing the calculation for sort down make this result 9.4 from 9.3 when making no adjustment
		assertEquals("reverse sort gives this 9.3 with wrong rounding rule but now is 9.4",
				"9.4", p25.toPlainString());

		List<Value> samples1 = new LinkedList<>();
		samples1.add( createSample("1962-11-10T04:15:00-05:00", "1.7") );
		samples1.add( createSample("1964-11-10T04:15:00-05:00", "2.2") );
		samples1.add( createSample("1966-11-10T04:15:00-05:00", "2.3") );
		samples1.add( createSample("1967-11-10T04:15:00-05:00", "2.6") );
		samples1.add( createSample("1965-11-10T04:15:00-05:00", "2.9") );
		samples1.add( createSample("1968-11-10T04:15:00-05:00", "3.9") );
		samples1.add( createSample("1970-11-10T04:15:00-05:00", "4.5") );
		samples1.add( createSample("1973-11-10T04:15:00-05:00", "4.8") );
		samples1.add( createSample("1971-11-10T04:15:00-05:00", "4.9") );
		samples1.add( createSample("1978-11-10T04:15:00-05:00", "7.3") );
		samples1.add( createSample("2009-11-10T04:15:00-05:00", "11.43") );
		samples1.add( createSample("2012-11-10T04:15:00-05:00", "11.97") );
		samples1.add( createSample("2013-11-10T04:15:00-05:00", "12.27") );

		// 2020-02-05 round HALF_DOWN changes this to 9.3 from 9.4 when rounding HALF_UP
		BigDecimal p75 = stats.valueOfPercentile(samples1, PERCENTILES.get(P75), Value::valueOf);
		assertEquals("standard sort gives this 9.3 with round HALF_DOWN",
				"9.3", p75.toPlainString()); // this is what the sigfigs should return
		// never get 9.37 as Excel spreadsheet does
//		assertEquals("9.37", p75.toPlainString()); // this is the inflated sigfigs value

		// In the case where it is suppose to be 9.4 as a round of 9.37, we get 9.4 when sorted in
		// increasing order because we add 7.3+2.1 = 9.4 (no rounding) but when we sort in reverse
		// order we are taking 11.43-2.1 = 9.33 rounded to 9.3. We never see 9.37 in any case with sigfigs.
		// The tests below show that sigfigs is the issue we are not taking 7.3+2.15 or 11.4-2.15
		// we are using 2.1 from addition sigfigs rules.
		// Here is why... 4.1 / 2.0 (half up) is 2.1, -4.1 / 2.0 (round UP again) is -2.1
		// While this is the same value it is not the same overall rounding rule.
		// In the negative case -4.1 / 2.0 (with round down) is -2.0 for 11.43-2.0 = 9.4
		// we could also solve this by ignoring sigfigs for the one calculation
		// fraction = difference * d (decimal index)

		List<Value> samples2 = new LinkedList<>();
		samples2.add( createSample("2013-11-10T04:15:00-05:00", "12.2") );
		samples2.add( createSample("2012-11-10T04:15:00-05:00", "11.9") );
		samples2.add( createSample("2009-11-10T04:15:00-05:00", "11.4") );
		samples2.add( createSample("1978-11-10T04:15:00-05:00", "7.3") );
		samples2.add( createSample("1971-11-10T04:15:00-05:00", "4.9") );
		samples2.add( createSample("1973-11-10T04:15:00-05:00", "4.8") );
		samples2.add( createSample("1970-11-10T04:15:00-05:00", "4.5") );
		samples2.add( createSample("1968-11-10T04:15:00-05:00", "3.9") );
		samples2.add( createSample("1965-11-10T04:15:00-05:00", "2.9") );
		samples2.add( createSample("1967-11-10T04:15:00-05:00", "2.6") );
		samples2.add( createSample("1966-11-10T04:15:00-05:00", "2.3") );
		samples2.add( createSample("1964-11-10T04:15:00-05:00", "2.2") );
		samples2.add( createSample("1962-11-10T04:15:00-05:00", "1.7") );

		// 2020-02-05 changing the calculation for sort down make this result 9.4 from 9.3 when making no adjustment
		BigDecimal p25b = stats.valueOfPercentile(samples2, PERCENTILES.get(P25), Value::valueOf);
		assertEquals("reverse sort gives this 9.3 with wrong rounding rule", "9.4", p25b.toPlainString());

		List<Value> samples3 = new LinkedList<>();
		samples3.add( createSample("1962-11-10T04:15:00-05:00", "1.7") );
		samples3.add( createSample("1964-11-10T04:15:00-05:00", "2.2") );
		samples3.add( createSample("1966-11-10T04:15:00-05:00", "2.3") );
		samples3.add( createSample("1967-11-10T04:15:00-05:00", "2.6") );
		samples3.add( createSample("1965-11-10T04:15:00-05:00", "2.9") );
		samples3.add( createSample("1968-11-10T04:15:00-05:00", "3.9") );
		samples3.add( createSample("1970-11-10T04:15:00-05:00", "4.5") );
		samples3.add( createSample("1973-11-10T04:15:00-05:00", "4.8") );
		samples3.add( createSample("1971-11-10T04:15:00-05:00", "4.9") );
		samples3.add( createSample("1978-11-10T04:15:00-05:00", "7.3") );
		samples3.add( createSample("2009-11-10T04:15:00-05:00", "11.4") );
		samples3.add( createSample("2012-11-10T04:15:00-05:00", "11.9") );
		samples3.add( createSample("2013-11-10T04:15:00-05:00", "12.2") );

		// 2020-02-05 round HALF_DOWN changes this to 9.3 from 9.4 when rounding HALF_UP
		BigDecimal p75b = stats.valueOfPercentile(samples3, PERCENTILES.get(P75), Value::valueOf);
		assertEquals("no change with fewer sigfigs", "9.3", p75b.toPlainString()); // this is what the sigfigs should return

		// TODO asdf this ^ assertion and the previous should probably yield the same result.

		List<Value> samples4 = new LinkedList<>();
		samples4.add( createSample("2013-11-10T04:15:00-05:00", "12.2") );
		samples4.add( createSample("2012-11-10T04:15:00-05:00", "11.9") );
		samples4.add( createSample("2009-11-10T04:15:00-05:00", "11.40") );
		samples4.add( createSample("1978-11-10T04:15:00-05:00", "7.30") );
		samples4.add( createSample("1971-11-10T04:15:00-05:00", "4.9") );
		samples4.add( createSample("1973-11-10T04:15:00-05:00", "4.8") );
		samples4.add( createSample("1970-11-10T04:15:00-05:00", "4.5") );
		samples4.add( createSample("1968-11-10T04:15:00-05:00", "3.9") );
		samples4.add( createSample("1965-11-10T04:15:00-05:00", "2.9") );
		samples4.add( createSample("1967-11-10T04:15:00-05:00", "2.6") );
		samples4.add( createSample("1966-11-10T04:15:00-05:00", "2.3") );
		samples4.add( createSample("1964-11-10T04:15:00-05:00", "2.2") );
		samples4.add( createSample("1962-11-10T04:15:00-05:00", "1.7") );

		BigDecimal p25c = stats.valueOfPercentile(samples4, PERCENTILES.get(P25), Value::valueOf);

		assertEquals("with additional sigfigs, more refined answer equal to P75", "9.35", p25c.toPlainString());

		List<Value> samples5 = new LinkedList<>();
		samples5.add( createSample("1962-11-10T04:15:00-05:00", "1.7") );
		samples5.add( createSample("1964-11-10T04:15:00-05:00", "2.2") );
		samples5.add( createSample("1966-11-10T04:15:00-05:00", "2.3") );
		samples5.add( createSample("1967-11-10T04:15:00-05:00", "2.6") );
		samples5.add( createSample("1965-11-10T04:15:00-05:00", "2.9") );
		samples5.add( createSample("1968-11-10T04:15:00-05:00", "3.9") );
		samples5.add( createSample("1970-11-10T04:15:00-05:00", "4.5") );
		samples5.add( createSample("1973-11-10T04:15:00-05:00", "4.8") );
		samples5.add( createSample("1971-11-10T04:15:00-05:00", "4.9") );
		samples5.add( createSample("1978-11-10T04:15:00-05:00", "7.30") );
		samples5.add( createSample("2009-11-10T04:15:00-05:00", "11.40") );
		samples5.add( createSample("2012-11-10T04:15:00-05:00", "11.9") );
		samples5.add( createSample("2013-11-10T04:15:00-05:00", "12.2") );

		BigDecimal p75c = stats.valueOfPercentile(samples5, PERCENTILES.get(P75), Value::valueOf);

		assertEquals("with additional sigfigs, more refined answer equal to P25", "9.35", p75c.toPlainString());

	}


	@Test
	public void test_Jun25Pct() {
		// This is data as it was from the database on 03/25/2017 and in order to preserve a testable
		// state this recreates that data set for the April data only for USGS:394829074053502
		List<Value> monthSamples = new LinkedList<>();
		fillJuneData(monthSamples);

		int preCount = monthSamples.size();
		monthlyStats.medianMonthlyValues(monthSamples, StatisticsCalculator::sortByValueOrderDescending);
		assertEquals("normalize should have removed no values", preCount, monthSamples.size());

		// 2020-02-05 changing the calculation for sort down make this result 9.05 from 9.02 when making no adjustment
		BigDecimal actual = stats.valueOfPercentile(monthSamples, PERCENTILES.get(P25), Value::valueOf);
		assertEquals("with additional sigfigs, more refined answer equal to p25", "9.05", actual.toPlainString());

		// 24 * %25 = 6 which is index 5 returning the raw value (note that the math is done on 24+1 but still return raw value)
		// however when reversed
		// (24+1) %75 = 18.75 which tries to get the 9.05-9.0 = 0.05 rounded to 0.1 and then %75 * .1 is 0.075  but rounded 0.1 again
		// 9.0 + 0.1 = 9.1

		Collections.reverse(monthSamples);
		// 2020-02-05 round HALF_DOWN changes this to 9.0 from 9.1 when rounding HALF_UP
		BigDecimal p75c = stats.valueOfPercentile(monthSamples, PERCENTILES.get(P75), Value::valueOf);
		assertEquals("reverse sort should yield the same answer for p75", "9.0", p75c.toPlainString());

		// TODO asdf the two tests here should yield the same result. But they do not because of the different figures.
		// note that it looks like the p25 is returning the exact value of the 5th index and p75 is returning the exact value of
	}

	@Test
	public void test_Jun25Pct_plusOneSigfig() {
		// This is data as it was from the database on 03/25/2017 and in order to preserve a testable
		// state this recreates that data set for the April data only for USGS:394829074053502
		List<Value> monthSamples = new LinkedList<>();
		fillJuneData(monthSamples);

		// increased sigfig with assertion that the expected value is replaced
		assertTrue(replaceValue(monthSamples, 6,  createSample("1995-06-05T12:00:00", "9.00"), "9.0"));

		int preCount = monthSamples.size();
		monthlyStats.medianMonthlyValues(monthSamples, StatisticsCalculator::sortByValueOrderDescending);
		assertEquals("normalize should have removed no values", preCount, monthSamples.size());

		BigDecimal p10c = stats.valueOfPercentile(monthSamples, PERCENTILES.get(P25), Value::valueOf);
		assertEquals("with additional sigfigs, more refined answer equal to p25", "9.04", p10c.toPlainString());

		// in this case the %25 and %75 acting on the (24+1) count accuracy of 9.05 and 9.00 both result in 9.04
		// GWW rounds 9.04 to 9.0

		Collections.reverse(monthSamples);
		BigDecimal p75c = stats.valueOfPercentile(monthSamples, PERCENTILES.get(P75), Value::valueOf);
		assertEquals("reverse sort should yield the same answer for p75", "9.04", p75c.toPlainString());
	}



	protected void fillJuneData(List<Value> monthSamples) {
		monthSamples.add( createSample("2000-06-01T12:00:00", "9.5") );
		monthSamples.add( createSample("2007-06-07T13:30:00-04:00", "9.5") );
		monthSamples.add( createSample("2001-06-15T12:00:00", "9.3") );
		monthSamples.add( createSample("2003-06-03T10:44:00-04:00", "9.3") );
		monthSamples.add( createSample("2002-06-13T12:00:00", "9.2") );
		monthSamples.add( createSample("2013-06-19T12:28:00-04:00", "9.05") );
		monthSamples.add( createSample("1995-06-05T12:00:00", "9.0") );
		monthSamples.add( createSample("1996-06-04T12:00:00", "8.8") );
		monthSamples.add( createSample("2004-06-02T09:35:00-04:00", "8.5") );
		monthSamples.add( createSample("1994-06-22T12:00:00", "8.4") );
		monthSamples.add( createSample("1997-06-10T12:00:00", "8.4") );
		monthSamples.add( createSample("1999-06-09T12:00:00", "8.4") );
		monthSamples.add( createSample("1993-06-18T12:00:00", "8.2") );
		monthSamples.add( createSample("1998-06-11T12:00:00", "8.2") );
		monthSamples.add( createSample("1985-06-18T12:00:00", "7.7") );
		monthSamples.add( createSample("1973-06-28T12:00:00", "4.3") );
		monthSamples.add( createSample("1971-06-11T12:00:00", "3.9") );
		monthSamples.add( createSample("1974-06-27T12:00:00", "3.9") );
		monthSamples.add( createSample("1969-06-23T12:00:00", "2.9") );
		monthSamples.add( createSample("1968-06-26T12:00:00", "2.5") );
		monthSamples.add( createSample("1966-06-06T12:00:00", "2.1") );
		monthSamples.add( createSample("1964-06-03T12:00:00", "1.6") );
		monthSamples.add( createSample("1965-06-03T12:00:00", "1.1") );
		monthSamples.add( createSample("1963-06-07T12:00:00", "0.9") );
	}

	@Test
	public void test_July10PctValue() {
		// This is data as it was from the database on 03/25/2017 and in order to preserve a testable
		// state this recreates that data set for the April data only for USGS:394829074053502
		List<Value> monthSamples = new LinkedList<>();
		fillJulyData(monthSamples);

		int preCount = monthSamples.size();
		List<Value> normalizeMutlipleYearlyValues = monthlyStats.medianMonthlyValues(monthSamples, StatisticsCalculator::sortByValueOrderDescending);
		assertEquals("normalize should have removed values", preCount-1, normalizeMutlipleYearlyValues.size());

		BigDecimal p10c = stats.valueOfPercentile(monthSamples, PERCENTILES.get(P10), Value::valueOf);
		assertEquals("with additional sigfigs, more refined answer equal to P10", "9.3", p10c.toPlainString());

		BigDecimal down = SigFigMathUtil.sigFigAdd(new BigDecimal("9.37"), new BigDecimal("-0.1"));
		assertEquals(new BigDecimal("9.3"), down);

		BigDecimal up = SigFigMathUtil.sigFigAdd(new BigDecimal("9.1"), new BigDecimal("0.1"));
		assertEquals(new BigDecimal("9.2"), up);

		BigDecimal dif1 = sigFigSubtract(new BigDecimal("9.1"), new BigDecimal("9.37"));
		BigDecimal dif2 = sigFigSubtract(new BigDecimal("9.37"), new BigDecimal("9.1"));
		assertEquals("expect the differences to round to the same magnitude", dif1.abs(), dif2.abs());

		// 9.37-9.1=0.27 round 0.3 and 9.1-9.37=-0.27 round -0.3
		// then 0.5 x 0.3 = .15 round .2 while 0.5 x -0.3 is -.15 round is -0.1
		// 9.37 -0.1 = 9.27 round 9.3

		// even reversed it is the same percentile value -- is should be, testing to ensure
		Collections.reverse(monthSamples);
		BigDecimal p90c = stats.valueOfPercentile(monthSamples, PERCENTILES.get(P90), Value::valueOf);
		assertEquals("reverse sort should yield the same answer for P90", "9.3", p90c.toPlainString());
	}
	@Test
	public void test_July10PctValue_plusOneSigfig() {
		// This is data as it was from the database on 03/25/2017 and in order to preserve a testable
		// state this recreates that data set for the April data only for USGS:394829074053502
		List<Value> monthSamples = new LinkedList<>();
		fillJulyData(monthSamples);

		// increased sigfig with assertion that the expected value is replaced
		assertTrue(replaceValue(monthSamples, 1, createSample("2002-07-17T12:00:00-04:00", "9.10"), "9.1"));

		int preCount = monthSamples.size();
		List<Value> normalizeMutlipleYearlyValues = monthlyStats.medianMonthlyValues(monthSamples, StatisticsCalculator::sortByValueOrderDescending);
		assertEquals("normalize should have removed values", preCount-1, normalizeMutlipleYearlyValues.size());

		// 2020-02-05 changing the calculation for sort down make this result 9.24 from 9.23 when making no adjustment
		// TODO asdf when we have parity between sort orders this should become 9.23 again.
		BigDecimal p10c = stats.valueOfPercentile(normalizeMutlipleYearlyValues, PERCENTILES.get(P10), Value::valueOf);
		assertEquals("with additional sigfigs, more refined answer equal to P10", "9.24", p10c.toPlainString());

		BigDecimal down = SigFigMathUtil.sigFigAdd(new BigDecimal("9.37"), new BigDecimal("-0.10"));
		assertEquals(new BigDecimal("9.27"), down);

		BigDecimal up = SigFigMathUtil.sigFigAdd(new BigDecimal("9.10"), new BigDecimal("0.10"));
		assertEquals(new BigDecimal("9.20"), up);

		BigDecimal dif1 = sigFigSubtract(new BigDecimal("9.10"), new BigDecimal("9.37"));
		BigDecimal dif2 = sigFigSubtract(new BigDecimal("9.37"), new BigDecimal("9.10"));
		assertEquals("expect the differences to round to the same magnitude", dif1.abs(), dif2.abs());

		// 9.37-9.10=0.27 no round and 9.10-9.37=-0.27 no round
		// then 0.5 x 0.27 = .135 round .14 while 0.5 x -0.27 is -.135 round is -0.13
		// 9.37 -0.13 = 9.24 round 9.24 round 9.2 on gww

		// even reversed it is the same percentile value -- is should be, testing to ensure
		Collections.reverse(normalizeMutlipleYearlyValues);
		// 2020-02-05 round HALF_DOWN changes this to 9.23 from 9.24 when rounding HALF_UP
		BigDecimal p90c = stats.valueOfPercentile(normalizeMutlipleYearlyValues, PERCENTILES.get(P90), Value::valueOf);
		assertEquals("reverse sort should yield the same answer for P90", "9.23", p90c.toPlainString());
		// TODO asdf when we have parity between sort orders this should match the entry above.
	}

	protected void fillJulyData(List<Value> monthSamples) {
		monthSamples.add( createSample("2014-07-01T13:01:00-04:00", "9.37") );
		monthSamples.add( createSample("2002-07-17T12:00:00-04:00", "9.1") );
		monthSamples.add( createSample("2003-07-03T12:00:00-04:00", "8.9") );
		monthSamples.add( createSample("1989-07-17T12:00:00", "7.7") );
		monthSamples.add( createSample("1975-07-02T12:00:00", "4.7") );
		monthSamples.add( createSample("1972-07-11T12:00:00", "4.1") );
		monthSamples.add( createSample("1970-07-14T12:00:00", "3.3") );
		monthSamples.add( createSample("1968-07-17T12:00:00", "2.6") );
		monthSamples.add( createSample("1967-07-06T12:00:00", "2.5") );
		monthSamples.add( createSample("1966-07-27T12:00:00", "2.2") );
		monthSamples.add( createSample("1966-07-01T12:00:00", "2.0") );
		monthSamples.add( createSample("1965-07-06T12:00:00", "1.9") );
		monthSamples.add( createSample("1964-07-02T12:00:00", "1.7") );
		monthSamples.add( createSample("1963-07-03T12:00:00", "1.6") );
		monthSamples.add( createSample("1962-07-20T12:00:00", "1.0") );
/*  these two entries that I wish to keep in this comment block
		monthSamples.add( createSample("1966-07-27T12:00:00", "2.2") );
		monthSamples.add( createSample("1966-07-01T12:00:00", "2.0") );
	get normalized to this (by median or mean using 50 percentile of the list)
		monthSamples.add( createSample("1966-07-01T12:00:00", "2.1") );
	even so these only have an impact on the answer because the count it reduced by one
*/
	}

	@Test
	public void test_Aug25Pct() {
		// This is data as it was from the database on 03/25/2017 and in order to preserve a testable
		// state this recreates that data set for the April data only for USGS:394829074053502
		List<Value> monthSamples = new LinkedList<>();
		fillAugData(monthSamples);

		int preCount = monthSamples.size();
		List<Value> normalizeMutlipleYearlyValues = monthlyStats.medianMonthlyValues(monthSamples, StatisticsCalculator::sortByValueOrderDescending);
		assertEquals("normalize should have removed values", preCount-2, normalizeMutlipleYearlyValues.size());

		BigDecimal p25c = stats.valueOfPercentile(normalizeMutlipleYearlyValues, PERCENTILES.get(P25), Value::valueOf);
		assertEquals("with additional sigfigs, more refined answer equal to p25", "9.6", p25c.toPlainString());

		// 03-30-2017 (Supplanted by 07-31-2018 rules which is actual a revert to original rounding)
		// 9.5 & 9.8 mean is 9.7  because 9.8-9.5 = 0.3 and mean is 0.15 rounded to 0.2
		// 9.7-9.46=0.24 round 0.2 and 9.46-9.7=-0.24 round -0.2
		// then 0.25 x 0.2 = 0.05 no round  while 0.25 x -0.05  no round
		// 9.7 -0.05 = 9.65 round 9.7

		// 07-31-2018
		// 9.5 & 9.8 mean is 9.6  because 9.5-9.8 = -0.3 and mean is -0.15 rounded to -0.2
		// 9.46-9.6=-0.14 round -0.1
		// then 0.25 x -0.1 = 0.025 round -0.03
		// 9.6 -0.03 = 9.57 round 9.6
		// because it is based off of 9.6 rounded to one fewer sigfig.
		// if there was no sigfig rounding 9.6 - (0.25*0.14) = 9.6 - 0.035 = 9.565 and we expect this to be the same as reversed (below)

		// reversed test
		Collections.reverse(normalizeMutlipleYearlyValues);
		// 2020-02-05 changing the calculation for sort down make this result 9.6 from 9.54 when making no adjustment
		BigDecimal p75c = stats.valueOfPercentile(normalizeMutlipleYearlyValues, PERCENTILES.get(P75), Value::valueOf);
		assertEquals("reverse sort should yield the same answer for p75", "9.6", p75c.toPlainString());

		// 07-31-2018
		// 9.5 & 9.8 mean is 9.6  because 9.5-9.8 = -0.3 and mean is -0.15 rounded to -0.2
		// 9.6-9.46 = 0.14 round 0.1
		// then 0.75 x 0.1 = 0.075 round 0.08
		// finally 9.46+0.08 = 9.54
		// because reversed is based off of 9.46 there is one more sigfig.
		// if there was no sigfig rounding 9.46+(0.75*0.14) = 9.46 + 0.105 = 9.565 and we expect this to be the same as non-reversed (above)
	}

	@Test
	public void test_Aug25Pct_plusOneSigfig() {
		// This is data as it was from the database on 03/25/2017 and in order to preserve a testable
		// state this recreates that data set for the April data only for USGS:394829074053502
		List<Value> monthSamples = new LinkedList<>();
		fillAugData(monthSamples);
		// increased sigfig with assertion that the expected value is replaced
		assertTrue(replaceValue(monthSamples, 4, createSample("2005-08-25T12:12:00-04:00", "9.80"), "9.8"));
		assertTrue(replaceValue(monthSamples, 5, createSample("2005-08-03T10:32:00-04:00", "9.50"), "9.5"));

		int preCount = monthSamples.size();
		List<Value> normalizeMutlipleYearlyValues = monthlyStats.medianMonthlyValues(monthSamples, StatisticsCalculator::sortByValueOrderDescending);
		assertEquals("normalize should have removed values", preCount-2, normalizeMutlipleYearlyValues.size());

		BigDecimal p25c = stats.valueOfPercentile(normalizeMutlipleYearlyValues, PERCENTILES.get(P25), Value::valueOf);
		assertEquals("with additional sigfigs, more refined answer equal to p25", "9.60", p25c.toPlainString());

		// 9.50 & 9.80 mean is 9.65  because 9.80-9.50 = 0.30 and mean is 0.15
		// 9.65-9.46=0.19 and 9.46-9.70=-0.19
		// then 0.25 x -0.19 = 0.0475 round 0.05 while 0.75 x 0.1425 rounded 0.14
		// 9.65 -0.05 = 9.60  and 9.46 +0.14 = 9.60 round 9.6 on gww

		// even reversed it is the same percentile value -- is should be, testing to ensure
		Collections.reverse(normalizeMutlipleYearlyValues);
		BigDecimal p75c = stats.valueOfPercentile(normalizeMutlipleYearlyValues, PERCENTILES.get(P75), Value::valueOf);
		assertEquals("reverse sort should yield the same answer for p75", "9.60", p75c.toPlainString());
	}

	protected void fillAugData(List<Value> monthSamples) {
		monthSamples.add( createSample("2007-08-22T10:07:00-04:00", "11.1") );
		monthSamples.add( createSample("2007-08-09T12:18:00-04:00", "11.0") );
		monthSamples.add( createSample("2010-08-04T08:55:00-04:00", "10.2") );
		monthSamples.add( createSample("2008-08-07T12:16:00-04:00", "9.9") );
		monthSamples.add( createSample("2005-08-25T12:12:00-04:00", "9.8") );
		monthSamples.add( createSample("2005-08-03T10:32:00-04:00", "9.5") );
		monthSamples.add( createSample("2013-08-12T14:56:00-04:00", "9.46") );
		monthSamples.add( createSample("1979-08-06T12:00:00", "7.5") );
		monthSamples.add( createSample("1978-08-04T12:00:00", "6.4") );
		monthSamples.add( createSample("1977-08-03T12:00:00", "6.3") );
		monthSamples.add( createSample("1976-08-12T12:00:00", "4.8") );
		monthSamples.add( createSample("1974-08-09T12:00:00", "4.4") );
		monthSamples.add( createSample("1970-08-21T12:00:00", "3.1") );
		monthSamples.add( createSample("1968-08-20T12:00:00", "3.0") );
		monthSamples.add( createSample("1966-08-31T12:00:00", "2.8") );
		monthSamples.add( createSample("1965-08-04T12:00:00", "2.0") );
		monthSamples.add( createSample("1964-08-04T12:00:00", "1.8") );
		monthSamples.add( createSample("1967-08-08T12:00:00", "1.1") );
/*  these two entries
		monthSamples.add( createSample("2005-08-25T12:12:00-04:00", "9.8") );
		monthSamples.add( createSample("2005-08-03T10:32:00-04:00", "9.5") );
	get normalized to this (by median or mean using 50 percentile of the list)
		monthSamples.add( createSample("2005-08-25T12:12:00-04:00", "9.7") );
*/
	}

	@Test
	public void test_Dec25Pct() {
		// This is data as it was from the database on 03/25/2017 and in order to preserve a testable
		// state this recreates that data set for the April data only for USGS:394829074053502
		List<Value> monthSamples = new LinkedList<>();
		fillDecData(monthSamples);

		int preCount = monthSamples.size();
		monthlyStats.medianMonthlyValues(monthSamples, StatisticsCalculator::sortByValueOrderDescending);
		assertEquals("normalize should have removed NO values", preCount, monthSamples.size());

		BigDecimal p10c = stats.valueOfPercentile(monthSamples, PERCENTILES.get(P25), Value::valueOf);
		assertEquals("with additional sigfigs, more refined answer equal to P25", "10.8", p10c.toPlainString());

		// 10.75-11.1=-0.35 round -0.3
		// then 0.75 x -0.3 = 0.225  round  -0.2
		// 11.1-0.2 = 10.9 no round

		// even reversed it is the same percentile value -- is should be, testing to ensure
		Collections.reverse(monthSamples);
		// 2020-02-05 changing the calculation for sort down make this result 10.82 from 10.9 when making no adjustment
		BigDecimal p75c = stats.valueOfPercentile(monthSamples, PERCENTILES.get(P75), Value::valueOf);
		assertEquals("reverse sort should yield the same answer for P75", "10.82", p75c.toPlainString());
	}
	@Test
	public void test_Dec25Pct_plusOneSigfig() {
		// This is data as it was from the database on 03/25/2017 and in order to preserve a testable
		// state this recreates that data set for the April data only for USGS:394829074053502
		List<Value> monthSamples = new LinkedList<>();
		fillDecData(monthSamples);
		// increased sigfig with assertion that the expected value is replaced
		assertTrue(replaceValue(monthSamples, 5, createSample("2000-12-08T12:00:00", "11.10"), "11.1"));

		int preCount = monthSamples.size();
		monthlyStats.medianMonthlyValues(monthSamples, StatisticsCalculator::sortByValueOrderDescending);
		assertEquals("normalize should have removed NO values", preCount, monthSamples.size());

		BigDecimal p10c = stats.valueOfPercentile(monthSamples, PERCENTILES.get(P25), Value::valueOf);
		assertEquals("with an additional sigfig 11.1 to 11.10, more refined answer equal to P25",
				"10.84", p10c.toPlainString());

		// 10.75-11.10=-0.35
		// then 0.75 x -0.35 = 0.2625 round 0.26
		// 11.10-0.26 = 10.84 rounded to 10.8 on gww

		// even reversed it is the same percentile value -- is should be, testing to ensure
		Collections.reverse(monthSamples);
		BigDecimal p75c = stats.valueOfPercentile(monthSamples, PERCENTILES.get(P75), Value::valueOf);
		assertEquals("reverse sort should yield the same answer for p75.", "10.84", p75c.toPlainString());
	}

	protected void fillDecData(List<Value> monthSamples) {
		monthSamples.add( createSample("2007-12-20T09:55:00-05:00", "12.47") );
		monthSamples.add( createSample("2006-12-04T13:35:00-05:00", "12.12") );
		monthSamples.add( createSample("2004-12-09T11:40:00-05:00", "11.53") );
		monthSamples.add( createSample("2001-12-03T12:00:00", "11.5") );
		monthSamples.add( createSample("2002-12-13T12:20:00-05:00", "11.25") );
		monthSamples.add( createSample("2000-12-08T12:00:00", "11.1") );
		monthSamples.add( createSample("2003-12-22T09:37:00-05:00", "10.75") );
		monthSamples.add( createSample("1997-12-16T12:00:00", "10.4") );
		monthSamples.add( createSample("1998-12-10T12:00:00", "10.3") );
		monthSamples.add( createSample("1993-12-22T12:00:00", "10.0") );
		monthSamples.add( createSample("1994-12-09T12:00:00", "9.8") );
		monthSamples.add( createSample("1999-12-15T12:00:00", "9.6") );
		monthSamples.add( createSample("1996-12-17T12:00:00", "9.4") );
		monthSamples.add( createSample("1984-12-12T12:00:00", "8.4") );
		monthSamples.add( createSample("1982-12-21T12:00:00", "8.3") );
		monthSamples.add( createSample("1985-12-11T12:00:00", "8.2") );
		monthSamples.add( createSample("1974-12-10T12:00:00", "6.1") );
		monthSamples.add( createSample("1970-12-31T12:00:00", "4.8") );
		monthSamples.add( createSample("1972-12-14T12:00:00", "4.8") );
		monthSamples.add( createSample("1968-12-17T12:00:00", "4.7") );
		monthSamples.add( createSample("1969-12-12T12:00:00", "4.1") );
		monthSamples.add( createSample("1966-12-05T12:00:00", "3.0") );
		monthSamples.add( createSample("1964-12-03T12:00:00", "2.6") );
		monthSamples.add( createSample("1965-12-06T12:00:00", "2.6") );
		monthSamples.add( createSample("1967-12-19T12:00:00", "2.6") );
		monthSamples.add( createSample("1963-12-02T12:00:00", "1.3") );
	}

	protected boolean replaceValue(List<Value> samples, int index, Value newSample, String expected) {
		Value sample = samples.remove(index);
		samples.add(index,newSample );
		boolean isExpected = expected.equals(sample.value.toPlainString());
		return isExpected;
	}

	@Test
	public void test_fixMissingMonthDay_year() {
		String actual = StatisticsCalculator.fixMissingMonthAndDay("2222");
		assertEquals("2222-06-30", actual);
	}
	
	@Test
	public void test_fixMissingMonthDay_month() {
		String actual = StatisticsCalculator.fixMissingMonthAndDay("2111-11");
		assertEquals("2111-11-15", actual);
	}
	
	@Test
	public void test_fixMissingMonthDay_empty() {
		String actual = StatisticsCalculator.fixMissingMonthAndDay("");
		assertEquals("", actual);
	}
	
	@Test
	public void test_fixMissingMonthDay_null() {
		String actual = StatisticsCalculator.fixMissingMonthAndDay(null);
		assertEquals("", actual);
	}
	
	@Test
	public void test_fixMissingMonthDay_badYear() {
		String actual = StatisticsCalculator.fixMissingMonthAndDay("123");
		assertEquals("", actual);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void test_daysDiff_badNumber() {
		StatisticsCalculator.daysDiff("asdfsdf", "32asa");
	}
	
	@Test
	public void test_percentileOfValue_nullAndEmptyProtection() {
		Value value = null;
		List<Value> values = new LinkedList<>();
		values.add( createSample("1963-12-02T12:00:00", "1.3") );
		BigDecimal actual = StatisticsCalculator.percentileOfValue(values, value, Value::valueOf);
		assertEquals(BigDecimal.ZERO, actual);

		value = null;
		values = new LinkedList<>();
		values.add( createSample("1963-12-02T12:00:00", "1.3") );
		actual = StatisticsCalculator.percentileOfValue(values, value, 10, Value::valueOf);
		assertEquals(BigDecimal.ZERO, actual);
		
		value = createSample("1963-12-02T12:00:00", "1.3") ;
		values = new LinkedList<>();
		actual = StatisticsCalculator.percentileOfValue(values, value, 10, Value::valueOf);
		assertEquals(BigDecimal.ZERO, actual);
		
		value = createSample("1963-12-02T12:00:00", "1.3") ;
		values = null;
		actual = StatisticsCalculator.percentileOfValue(values, value, 10, Value::valueOf);
		assertEquals(BigDecimal.ZERO, actual);
		
		value = createSample("1963-12-02T12:00:00", "1.3") ;
		values = null;
		actual = StatisticsCalculator.percentileOfValue(values, value, Value::valueOf);
		assertEquals(BigDecimal.ZERO, actual);
		
		value = createSample("1963-12-02T12:00:00", "1.3") ;
		values = new LinkedList<>();
		values.add( createSample("1963-12-02T12:00:00", "1.3") );
		actual = StatisticsCalculator.percentileOfValue(values, value, null);
		assertEquals(BigDecimal.ZERO, actual);
		
		value = createSample("1963-12-02T12:00:00", "1.3") ;
		values = new LinkedList<>();
		values.add( createSample("1963-12-02T12:00:00", "1.3") );
		actual = StatisticsCalculator.percentileOfValue(values, value, 10, null);
		assertEquals(BigDecimal.ZERO, actual);
		
		value = createSample("1963-12-02T12:00:00", "1.3") ;
		values = new LinkedList<>();
		values.add( createSample("1963-12-02T12:00:00", "1.3") );
		Function<Value, BigDecimal> valueOf = new Function<Value, BigDecimal>() {
			@Override
			public BigDecimal apply(Value t) {
				return null; // TESTING THIS
			}
		};
		actual = StatisticsCalculator.percentileOfValue(values, value, 10, valueOf);
		assertEquals(BigDecimal.ZERO, actual);
	}

	@Test
	public void test_valueOfPercentile_nullAndEmptyProtection() {
		BigDecimal percentile = BigDecimal.ONE;
		List<Value> values = new LinkedList<>();
		values.add( createSample("1963-12-02T12:00:00", "1.3") );
		BigDecimal actual = stats.valueOfPercentile(values, percentile, 10, Value::valueOf);
		assertEquals(values.get(0).value, actual);
		
		percentile = null;
		values = new LinkedList<>();
		values.add( createSample("1963-12-02T12:00:00", "1.3") );
		actual = stats.valueOfPercentile(values, percentile, 10, Value::valueOf);
		assertEquals(BigDecimal.ZERO, actual);

		percentile = BigDecimal.ONE;
		values = null;
		actual = stats.valueOfPercentile(values, percentile, 10, Value::valueOf);
		assertEquals(BigDecimal.ZERO, actual);

		percentile = BigDecimal.ONE;
		values = new LinkedList<>();
		actual = stats.valueOfPercentile(values, percentile, 10, Value::valueOf);
		assertEquals(BigDecimal.ZERO, actual);

		percentile = new BigDecimal("-0.1");
		values = new LinkedList<>();
		values.add( createSample("1963-12-02T12:00:00", "1.3") );
		actual = stats.valueOfPercentile(values, percentile, 10, Value::valueOf);
		assertEquals(BigDecimal.ZERO, actual);

		percentile = new BigDecimal("1.1");
		values = new LinkedList<>();
		values.add( createSample("1963-12-02T12:00:00", "1.3") );
		actual = stats.valueOfPercentile(values, percentile, 10, Value::valueOf);
		assertEquals(BigDecimal.ZERO, actual);
	}
	
	@Test
	public void test_uniqueYears_nullProtection() {
		int actual = stats.uniqueYears(null);
		assertEquals(0, actual);
		
		actual = stats.uniqueYears(new LinkedList<>());
		assertEquals(0, actual);
	}
	
	@Test
	public void test_valueOfPercentile_roundingSigFigCheck_ascendingSort() {
		
		List<Value> values = new LinkedList<>();
		loadWithSeptemberData(values);
		
		BigDecimal actual = stats.valueOfPercentile(values, new BigDecimal("0.5"), Value::valueOf);
		BigDecimal expect = new BigDecimal("24.7");
		assertEquals(expect, actual);
	}
	@Test
	public void test_valueOfPercentile_roundingSigFigCheck_descendingSort() {
		
		List<Value> values = new LinkedList<>();
		loadWithSeptemberData(values);
		StatisticsCalculator.sortByValueOrderDescending(values);
		
		BigDecimal actual = stats.valueOfPercentile(values, StatisticsCalculator.MEDIAN_PERCENTILE, Value::valueOf);
		BigDecimal expect = new BigDecimal("24.74"); // TODO asdf this needs to be changed to 24.7
		assertEquals(expect, actual);
	}
	public static void loadWithSeptemberData(List<Value> values) {
		values.add( createSample("2009-09-01T00:00:00","24.48") );
		values.add( createSample("2009-09-03T00:00:00","24.48") );
		values.add( createSample("2009-09-02T00:00:00","24.49") );
		values.add( createSample("2009-09-04T00:00:00","24.49") );
		values.add( createSample("2009-09-05T00:00:00","24.53") );
		values.add( createSample("2009-09-07T00:00:00","24.56") );
		values.add( createSample("2009-09-08T00:00:00","24.57") );
		values.add( createSample("2009-09-06T00:00:00","24.58") );
		values.add( createSample("2009-09-09T00:00:00","24.62") );
		values.add( createSample("2009-09-12T00:00:00","24.63") );
		
		values.add( createSample("2009-09-13T00:00:00","24.63") );
		values.add( createSample("2009-09-11T00:00:00","24.65") );
		values.add( createSample("2009-09-10T00:00:00","24.66") );
		values.add( createSample("2009-09-14T00:00:00","24.67") );
		
		values.add( createSample("2009-09-15T00:00:00","24.7") );
		values.add( createSample("2009-09-18T00:00:00","24.74") );
		
		values.add( createSample("2009-09-17T00:00:00","24.76") );
		values.add( createSample("2009-09-16T00:00:00","24.77") );
		values.add( createSample("2009-09-19T00:00:00","24.82") );
		values.add( createSample("2009-09-20T00:00:00","24.84") );
		
		values.add( createSample("2009-09-21T00:00:00","24.87") );
		values.add( createSample("2009-09-22T00:00:00","24.88") );
		values.add( createSample("2009-09-23T00:00:00","24.88") );
		values.add( createSample("2009-09-24T00:00:00","24.91") );
		values.add( createSample("2009-09-27T00:00:00","24.92") );
		values.add( createSample("2009-09-28T00:00:00","24.92") );
		values.add( createSample("2009-09-25T00:00:00","24.96") );
		values.add( createSample("2009-09-26T00:00:00","24.97") );
		values.add( createSample("2009-09-29T00:00:00","24.97") );
		values.add( createSample("2009-09-30T00:00:00","25.02") );
	}
//	@Test
//	public void test_median_valueOfPercentile_roundingSigFigCheck2() {
//		
//		List<Value> values = new LinkedList<>();
//		loadSeptemberData(values);
////		StatisticsCalculator.sortByValueOrderDescending(values);
//		MonthlyStatistics<Value> monthlyStatistics = new MonthlyStatistics<>(env, new JsonDataBuilder(env));
//		List<Value> medians = monthlyStatistics.medianMonthlyValues(values,  (samples)->{
//			return StatisticsCalculator.sortByValueOrderDescending(samples);
//		});
//		
//		BigDecimal actual = stats.valueOfPercentile(medians, new BigDecimal("0.5"), Value::valueOf);
//		BigDecimal expect = new BigDecimal("24.74");
//		assertEquals(expect, actual);
//	}
//	public static void loadSeptemberData(List<Value> values) {
//		values.add( createSample("1966-09-20","36.1"));
//		values.add( createSample("1965-09-20","35.52"));
//		values.add( createSample("1959-09-20","34.89"));
//		values.add( createSample("1968-09-20","34.37"));
//		values.add( createSample("1962-09-20","33.96"));
//		values.add( createSample("1958-09-20","33.94"));
//		values.add( createSample("1967-09-20","33.81"));
//		values.add( createSample("1964-09-20","33.27"));
//		values.add( createSample("1969-09-20","33.10"));
//		values.add( createSample("1961-09-20","33.06"));
//		values.add( createSample("1981-09-16","32.9"));
//		values.add( createSample("1960-09-20","32.89"));
//		values.add( createSample("2003-09-16","32.68"));
//		values.add( createSample("1987-09-16","32.07"));
//		values.add( createSample("1972-09-20","31.95"));
//		values.add( createSample("2004-09-16","31.91"));
//		values.add( createSample("2002-09-16","31.85"));
//		values.add( createSample("1986-09-16","31.74"));
//		values.add( createSample("1970-09-20","31.61"));
//		values.add( createSample("1980-09-16","31.46"));
//		values.add( createSample("1988-09-16","31.3"));
//		values.add( createSample("1971-09-25","31.22"));
//		values.add( createSample("1985-09-04","31.12"));
//		values.add( createSample("2001-09-16","31.06"));
//		values.add( createSample("1982-09-16","30.96"));
//		values.add( createSample("1989-09-16","30.94"));
//		values.add( createSample("1990-09-28","30.86"));
//		values.add( createSample("2018-09-16","30.85"));
//		values.add( createSample("2000-09-29","30.22"));
//		values.add( createSample("1979-09-15","30.17"));
//		values.add( createSample("1983-09-16","30.17"));
//		values.add( createSample("1998-09-30","30.17"));
//		values.add( createSample("1997-09-26","30.16"));
//		values.add( createSample("1999-09-28","30.08"));
//		values.add( createSample("2005-09-16","29.82"));
//		values.add( createSample("2017-09-16","29.75"));
//		values.add( createSample("2016-09-16","29.57"));
//		values.add( createSample("1977-09-25","29.43"));
//		values.add( createSample("1995-09-28","29.41"));
//		values.add( createSample("1993-09-28","29.29"));
//		values.add( createSample("1975-09-20","29.28"));
//		values.add( createSample("1973-09-20","29.04"));
//		values.add( createSample("2015-09-15","29"));
//		values.add( createSample("1978-09-20","28.85"));
//		values.add( createSample("1991-09-27","28.83"));
//		values.add( createSample("2013-09-16","28.71"));
//		values.add( createSample("1994-09-28","28.22"));
//		values.add( createSample("2014-09-16","28.15"));
//		values.add( createSample("1992-09-24","27.66"));
//		values.add( createSample("1984-09-16","27.61"));
//		values.add( createSample("1996-09-27","27.21"));
//		values.add( createSample("2007-09-16","27.0"));
//		values.add( createSample("2012-09-16","26.84"));
//		values.add( createSample("2011-09-16","26.50"));
//		values.add( createSample("2006-09-16","26.28"));
//		values.add( createSample("2008-09-16","26.07"));
//		values.add( createSample("2010-09-16","25.83"));
//		values.add( createSample("2009-09-16","24.74"));
//	}

	@Test
	public void testBigDecimal_zeroHasNoPrecision() {
		BigDecimal zero = new BigDecimal("0.000");
		BigDecimal three = new BigDecimal("3.000");
		assertEquals(1, zero.precision());
		assertEquals(4, three.precision());
		assertEquals(1, three.subtract(three).precision());		

		BigDecimal zero5 = zero.setScale(5);
		BigDecimal three5 = three.setScale(5);
		assertEquals(1, zero5.precision());
		assertEquals(6, three5.precision());
	}
	

}
