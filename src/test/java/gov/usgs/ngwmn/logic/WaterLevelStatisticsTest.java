package gov.usgs.ngwmn.logic;

import static gov.usgs.wma.statistics.model.JsonDataBuilder.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.usgs.ngwmn.logic.WaterLevelStatistics.WLMonthlyStats;
import gov.usgs.ngwmn.model.MediationType;
import gov.usgs.ngwmn.model.PCode;
import gov.usgs.ngwmn.model.Specifier;
import gov.usgs.ngwmn.model.WLSample;
import gov.usgs.wma.statistics.app.Properties;
import gov.usgs.wma.statistics.logic.StatisticsCalculator;
import gov.usgs.wma.statistics.model.JsonData;
import gov.usgs.wma.statistics.model.JsonDataBuilder;
import gov.usgs.wma.statistics.model.JsonMonthly;
import gov.usgs.wma.statistics.model.JsonOverall;
import gov.usgs.wma.statistics.model.Value;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration //(locations = { "/applicationContext_mock.xml" })
public class WaterLevelStatisticsTest {

	public static final String P10 = "P10";
	public static final String P25 = "P25";
	public static final String P50 = "P50";
	public static final String P75 = "P75";
	public static final String P90 = "P90";

	@Mock
	Environment spring;
	Properties env;
	WaterLevelStatistics stats = null;
	Specifier spec = new Specifier();
	private JsonDataBuilder builder;

	private WLSample createSample(String time, String value) {
		BigDecimal val = null;
		if (null!=value) {
			val = new BigDecimal(value);
		}
		return new WLSample(time, val, "units", val, "comment", true, "pcode", null);
	}
	private WLSample createSample(String time, String value, boolean provisional) {
		WLSample sample = createSample(time, value);
		sample.setProvsional(provisional);
		return sample;
	}
	private WLSample createSample(String time, String value, PCode pcode, BigDecimal aboveDatumValue) {
		BigDecimal val = null;
		if (null!=value) {
			val = new BigDecimal(value);
		}
		return new WLSample(time, val, "units", val, "comment", true, pcode.getCode(), aboveDatumValue);
	}
	/**
	 * Helper class to sort samples by value. This is a simple sort for testing.
	 */
	public static class OrderedSamples extends LinkedList<WLSample> {
		private static final long serialVersionUID = 1L;

		Comparator<Value> comparator;

		public OrderedSamples() {
			// default sort in ascending order.
			this.comparator = WLSample.DEPTH_ABOVE_DATUM_COMPARATOR;
		}
		public OrderedSamples(Comparator<Value> comparator) {
			this.comparator = comparator;
		}

		@Override
		public boolean add(WLSample e) {
			if (e==null || e.value==null) {
				return false;
			}

			int i = 0;
			for (WLSample sample : this) {
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
		builder = new JsonDataBuilder();
		env = new Properties().setEnvironment(spring);
		stats = new WaterLevelStatistics(env, builder);
		spec = new Specifier("USGS", "Testing");

	}


	public void test_doesThisSiteQualifyForMonthlyStats_montly_qualifies() throws Exception {
		String recent = StatisticsCalculator.today();
		String today  = StatisticsCalculator.today();

		BigDecimal years = new BigDecimal("11");
		boolean actual = stats.doesThisSiteQualifyForMonthlyStats(years, recent, today);
		assertTrue("A site with >10 years of data and a recent date for the most recent value should be true", actual);

		years = new BigDecimal("10");
		actual = stats.doesThisSiteQualifyForMonthlyStats(years, recent, today);
		assertTrue("A site with =10 years of data and a recent date for the most recent value should be true", actual);

		recent = "2015-03-01";
		today  = "2016-03-01";
		actual = stats.doesThisSiteQualifyForMonthlyStats(years, recent, today);
		assertTrue("A site with >10 years of data and a 1 year old date for the most recent value should be true", actual);

		recent = "2015-01-26";
		int days   = StatisticsCalculator.daysDiff(today, recent).intValue();
		assertTrue(days < 406);
		actual = stats.doesThisSiteQualifyForMonthlyStats(years, recent, today);
		assertTrue("A site with 10 years of data and a <406 year old date for the most recent value should be true", actual);

		recent = "2015-01-20";
		days   = StatisticsCalculator.daysDiff(today, recent).intValue();
		assertTrue(days == 406);
		actual = stats.doesThisSiteQualifyForMonthlyStats(years, recent, today);
		assertTrue("A site with 10 years of data and a =406 year old date for the most recent value should be true", actual);
	}


	@Test
	public void test_doesThisSiteQualifyForMonthlyStats_monthly_disqualified_by_years() throws Exception {
		String recent = StatisticsCalculator.today();
		String today  = StatisticsCalculator.today();

		BigDecimal years = new BigDecimal("9");
		boolean   actual = stats.doesThisSiteQualifyForMonthlyStats(years, recent, today);
		assertFalse("A site with <10 years of data and a recent date for the most recent value should be false", actual);
	}

	@Test
	public void test_doesThisSiteQualifyForMonthlyStats_monthly_disqualified_by_recent_measure() throws Exception {
		BigDecimal years = new BigDecimal("11");

		String today  = "2016-03-01";

		String recent = "2014-02-01";
		int days = StatisticsCalculator.daysDiff(today, recent).intValue();
		assertTrue(days > 406);
		boolean actual = stats.doesThisSiteQualifyForMonthlyStats(years, recent, today);
		assertFalse("A site with 10 years of data and a >406 days old date for the most recent value should be false", actual);

		recent = "2015-01-19"; // 365 days from 2015-01-19 to 2016-01-19, 30 more day is 2016-02-19, one more week is 2016-02-26, and 4 more days is 2016-03-01 because 2016 is leap year
		days   = StatisticsCalculator.daysDiff(today, recent).intValue();
		assertTrue(days == 407);
		actual = stats.doesThisSiteQualifyForMonthlyStats(years, recent, today);
		assertFalse("A site with 10 years of data and a =407 year old date for the most recent value should be false "
		+"(note that 2016 is a leap year and the date math should handle this to the day. if this failed then check leap year math)", actual);

		recent = "2015-01-18";
		days   = StatisticsCalculator.daysDiff(today, recent).intValue();
		assertTrue(days > 406);
		actual = stats.doesThisSiteQualifyForMonthlyStats(years, recent, today);
		assertFalse("A site with 10 years of data and a >406 year old date for the most recent value should be false", actual);
	}

	@Test
	public void test_doesThisMonthQualifyForStats_notEngoughData() throws Exception {
		List<WLSample> monthSamples = new ArrayList<>(2);
		monthSamples.add(createSample("2005-06-10T04:15:00-05:00", "1.0"));
		monthSamples.add(createSample("2015-06-10T04:15:00-05:00", "2.0"));

		builder.mediation(MediationType.AboveDatum);
		boolean qualifies = new WLMonthlyStats(env, builder)
				.doesThisMonthQualifyForStats(monthSamples);
		assertFalse("A month must have 10 unique years of data not just a 10 yr date range.", qualifies);

		qualifies = new WLMonthlyStats(env, builder)
				.doesThisMonthQualifyForStats(new ArrayList<>(2));
		assertFalse("A month with zero should not cause trouble either.", qualifies);
	}
	@Test
	public void test_doesThisMonthQualifyForStats_notEngoughYears() throws Exception {
		List<WLSample> monthSamples = new ArrayList<>(10);
		monthSamples.add(createSample("2005-06-10T04:15:00-05:00", "1.0"));
		monthSamples.add(createSample("2015-06-10T04:15:00-05:00", "2.0"));
		monthSamples.add(createSample("2005-06-10T04:15:00-05:00", "1.0"));
		monthSamples.add(createSample("2015-06-10T04:15:00-05:00", "2.0"));
		monthSamples.add(createSample("2005-06-10T04:15:00-05:00", "1.0"));
		monthSamples.add(createSample("2015-06-10T04:15:00-05:00", "2.0"));
		monthSamples.add(createSample("2005-06-10T04:15:00-05:00", "1.0"));
		monthSamples.add(createSample("2015-06-10T04:15:00-05:00", "2.0"));
		monthSamples.add(createSample("2005-06-10T04:15:00-05:00", "1.0"));
		monthSamples.add(createSample("2015-06-10T04:15:00-05:00", "2.0"));

		builder.mediation(MediationType.AboveDatum);
		boolean qualifies = new WLMonthlyStats(env, builder)
				.doesThisMonthQualifyForStats(monthSamples);
		assertFalse("A month must have 10 unique years of data not just a 10 yr date range.", qualifies);
	}
	@Test
	public void test_doesThisMonthQualifyForStats_hasTenYearsData() throws Exception {
		List<WLSample> monthSamples = new ArrayList<>(10);
		monthSamples.add(createSample("2005-06-10T04:15:00-05:00", "1.0"));
		monthSamples.add(createSample("2006-06-10T04:15:00-05:00", "2.0"));
		monthSamples.add(createSample("2007-06-10T04:15:00-05:00", "1.0"));
		monthSamples.add(createSample("2008-06-10T04:15:00-05:00", "2.0"));
		monthSamples.add(createSample("2009-06-10T04:15:00-05:00", "1.0"));
		monthSamples.add(createSample("2010-06-10T04:15:00-05:00", "2.0"));
		monthSamples.add(createSample("2011-06-10T04:15:00-05:00", "1.0"));
		monthSamples.add(createSample("2012-06-10T04:15:00-05:00", "2.0"));
		monthSamples.add(createSample("2013-06-10T04:15:00-05:00", "1.0"));
		monthSamples.add(createSample("2014-06-10T04:15:00-05:00", "2.0"));

		builder.mediation(MediationType.AboveDatum);
		boolean qualifies = new WLMonthlyStats(env, builder)
				.doesThisMonthQualifyForStats(monthSamples);
		assertTrue("A month with 10 unique years of data is valid.", qualifies);
	}
	@Test
	public void test_doesThisMonthQualifyForStats_hasElevenYearsData() throws Exception {
		List<WLSample> monthSamples = new ArrayList<>(11);
		monthSamples.add(createSample("2005-06-10T04:15:00-05:00", "1.0"));
		monthSamples.add(createSample("2006-06-10T04:15:00-05:00", "2.0"));
		monthSamples.add(createSample("2007-06-10T04:15:00-05:00", "1.0"));
		monthSamples.add(createSample("2008-06-10T04:15:00-05:00", "2.0"));
		monthSamples.add(createSample("2009-06-10T04:15:00-05:00", "1.0"));
		monthSamples.add(createSample("2010-06-10T04:15:00-05:00", "2.0"));
		monthSamples.add(createSample("2011-06-10T04:15:00-05:00", "1.0"));
		monthSamples.add(createSample("2012-06-10T04:15:00-05:00", "2.0"));
		monthSamples.add(createSample("2013-06-10T04:15:00-05:00", "1.0"));
		monthSamples.add(createSample("2014-06-10T04:15:00-05:00", "2.0"));
		monthSamples.add(createSample("2015-06-10T04:15:00-05:00", "2.0"));

		builder.mediation(MediationType.AboveDatum);
		boolean qualifies = new WLMonthlyStats(env, builder)
				.doesThisMonthQualifyForStats(monthSamples);
		assertTrue("A month with 10 unique years of data is valid.", qualifies);
	}

	@Test
	public void test_overallStats_0() throws Exception {
		List<WLSample> samples = new ArrayList<>(0);
		stats.overallStats(samples, samples);
		assertEquals("Expect empty stats for no stats", "0", builder.build().getOverall().recordYears);
	}
	
	@Test
	public void test_overallStats_min_max_1() throws Exception {
		WLSample first = createSample("2005-06-10T04:15:00-05:00", "1.0");
		List<WLSample> samples = new ArrayList<>(1);
		samples.add(first);
		stats.setMediation(MediationType.AboveDatum);

		stats.overallStats(samples, samples);
		JsonOverall overall = builder.build().getOverall();
		
		assertEquals("Expect max to be first.time", first.time, overall.dateMax);
		assertEquals("Expect min to be first.time", first.time, overall.dateMin);
		assertEquals("Expect max to be first.value", first.value.toString(), overall.valueMax);
		assertEquals("Expect min to be first.value", first.value.toString(), overall.valueMin);
		assertEquals("Expect latest to be first.value", first.value.toString(), overall.latestValue);

		assertEquals("Expect record_years to be zero for one record", "0.0", overall.recordYears);
	}
	@Test
	public void test_overallStats_min_max_2() throws Exception {
		WLSample min = createSample("2005-06-10T04:15:00-05:00", "1.0");
		WLSample max = createSample("2015-06-10T04:15:00-05:00", "2.0");
		List<WLSample> samples = new ArrayList<>(2);
		samples.add(min);
		samples.add(max);
		List<WLSample> sorted = new LinkedList<>(samples);
		StatisticsCalculator.sortByValueOrderAscending(sorted);

		stats.setMediation(MediationType.AboveDatum);
		stats.overallStats(samples, sorted);
		JsonOverall overall = builder.build().getOverall();
		
		assertEquals("Expect max to be max.time", max.time, overall.dateMax);
		assertEquals("Expect min to be min.time", min.time, overall.dateMin);
		assertEquals("Expect max to be max.value", max.value.toString(), overall.valueMax);
		assertEquals("Expect min to be min.value", min.value.toString(), overall.valueMin);

		assertEquals("Expect record_years to be ten years", "10.0", overall.recordYears);
	}
	@Test
	public void test_overallStats_min_max_2_rev() throws Exception {
		WLSample min = createSample("2005-06-10T04:15:00-05:00", "1.0");
		WLSample max = createSample("2015-12-10T04:15:00-05:00", "2.0");
		List<WLSample> samples = new LinkedList<>();
		samples.add(max);
		samples.add(min);
		List<WLSample> sorted = new LinkedList<>(samples);
		StatisticsCalculator.sortByValueOrderAscending(sorted);

		stats.setMediation(MediationType.AboveDatum);
		stats.overallStats(samples, sorted);
		JsonOverall overall = builder.build().getOverall();
		
		assertEquals("Expect max to be max.time", max.time, overall.dateMax);
		assertEquals("Expect min to be min.time", min.time, overall.dateMin);
		assertEquals("Expect max to be max.value", max.value.toString(), overall.valueMax);
		assertEquals("Expect min to be min.value", min.value.toString(), overall.valueMin);

		assertEquals("Expect record_years to be ten and 1/2 years", "10.5", overall.recordYears);
	}
	@Test
	public void test_overallStats_min_max_3() throws Exception {
		WLSample min = createSample("2005-12-10T04:15:00-05:00", "1.0");
		WLSample mid = createSample("2010-06-10T04:15:00-05:00", "1.5");
		WLSample max = createSample("2015-06-10T04:15:00-05:00", "2.0");
		List<WLSample> samples = new LinkedList<>();
		samples.add(mid);
		samples.add(min);
		samples.add(max);
		List<WLSample> sorted = new LinkedList<>(samples);
		StatisticsCalculator.sortByValueOrderAscending(sorted);

		stats.setMediation(MediationType.AboveDatum);
		stats.overallStats(samples, sorted);
		JsonOverall overall = builder.build().getOverall();
		
		assertEquals("Expect max to be max.time", max.time, overall.dateMax);
		assertEquals("Expect min to be min.time", min.time, overall.dateMin);
		assertEquals("Expect max to be max.value", max.value.toString(), overall.valueMax);
		assertEquals("Expect min to be min.value", min.value.toString(), overall.valueMin);

		assertEquals("Expect record_years to be 9 and 1/2 years", "9.5", overall.recordYears);
	}
	@Test
	public void test_overallStats_count_median_3() throws Exception {
		WLSample min = createSample("2005-06-10T04:15:00-05:00", "1.0");
		WLSample mid = createSample("2010-06-10T04:15:00-05:00", "1.5");
		WLSample max = createSample("2015-06-10T04:15:00-05:00", "2.0");
		List<WLSample> samples = new LinkedList<>();
		samples.add(mid);
		samples.add(min);
		samples.add(max);
		List<WLSample> sorted = new LinkedList<>(samples);
		StatisticsCalculator.sortByValueOrderAscending(sorted);

		stats.setMediation(MediationType.AboveDatum);
		stats.overallStats(samples, sorted);
		JsonOverall overall = builder.build().getOverall();
		
		assertEquals("Expect count to be 3", 3, overall.sampleCount);
		assertEquals("Expect median to be mid.value", mid.value.toString(), overall.valueMedian);
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
	public void test_removeProvisionalButNotMostRecent() throws Exception {
		// note that all previous calculate tests prove that 100% non-provisional collections are retained

		WLSample defaultRetained  = createSample("2015-05-10T04:15:00-05:00", "95.1772");
		WLSample explicitRetained = createSample("2015-05-11T04:15:00-05:00", "95.1567", false);
		WLSample explicitRemoved  = createSample("2015-05-12T04:15:00-05:00", "95.1937", true);
		WLSample recentRetained   = createSample("2015-05-13T04:15:00-05:00", "95.1959", true);

		List<WLSample> samples = new LinkedList<>();
		samples.add( defaultRetained );  // should be retained because of default false
		samples.add( explicitRetained ); // should be retained because not provisional
		samples.add( explicitRemoved );  // should be REMOVED because provisional
		samples.add( recentRetained );   // should be retained because most recent

		stats.removeProvisionalButNotMostRecent(samples,"testing");

		assertTrue("should be retained because of default false", samples.contains(defaultRetained));
		assertTrue("should be retained because not provisional",  samples.contains(explicitRetained));
		assertFalse("should be REMOVED because provisional",      samples.contains(explicitRemoved));
		assertTrue("should be retained because most recent",      samples.contains(recentRetained));
	}

	@Test
	public void test_removeMostRecentProvisional_remove() throws Exception {
		// note that all previous calculate tests prove that 100% non-provisional collections are retained

		WLSample defaultRetained  = createSample("2015-05-10T04:15:00-05:00", "95.1772");
		WLSample explicitRetained = createSample("2015-05-11T04:15:00-05:00", "95.1567", false);
		WLSample explicitRemoved  = createSample("2015-05-12T04:15:00-05:00", "95.1937", true);
		WLSample recentRetained   = createSample("2015-05-13T04:15:00-05:00", "95.1959", true);

		List<WLSample> samples1 = new LinkedList<>();
		samples1.add( defaultRetained );  // should be retained because of default false
		samples1.add( explicitRetained ); // should be retained because not provisional
		samples1.add( explicitRemoved );  // should be REMOVED because provisional
		samples1.add( recentRetained );   // should be retained because most recent

		stats.removeProvisionalButNotMostRecent(samples1,"testing");

		assertTrue("should be retained because of default false", samples1.contains(defaultRetained));
		assertTrue("should be retained because not provisional",  samples1.contains(explicitRetained));
		assertFalse("should be REMOVED because provisional",      samples1.contains(explicitRemoved));
		assertTrue("should be retained because most recent",      samples1.contains(recentRetained));

		List<WLSample> sortedByMediation  = new ArrayList<>(samples1);
		stats.setMediation(MediationType.AboveDatum);
		stats.monthlyStats.sortValueByQualifier(sortedByMediation);

		stats.removeMostRecentProvisional(samples1, sortedByMediation);
		assertFalse("should be removed because most recent", samples1.contains(recentRetained));
		assertFalse("should be removed because most recent", sortedByMediation.contains(recentRetained));
	}

	@Test
	public void test_removeMostRecentProvisional_retain() throws Exception {
		// note that all previous calculate tests prove that 100% non-provisional collections are retained

		WLSample defaultRetained  = createSample("2015-05-10T04:15:00-05:00", "95.1772");
		WLSample explicitRetained = createSample("2015-05-11T04:15:00-05:00", "95.1567", false);
		WLSample explicitRemoved  = createSample("2015-05-12T04:15:00-05:00", "95.1937", true);
		WLSample recentRetained   = createSample("2015-05-13T04:15:00-05:00", "95.1959", false);

		List<WLSample> samples1 = new LinkedList<>();
		samples1.add( defaultRetained );  // should be retained because of default false
		samples1.add( explicitRetained ); // should be retained because not provisional
		samples1.add( explicitRemoved );  // should be REMOVED because provisional
		samples1.add( recentRetained );   // should be retained because most recent

		stats.removeProvisionalButNotMostRecent(samples1,"testing");

		assertTrue("should be retained because of default false", samples1.contains(defaultRetained));
		assertTrue("should be retained because not provisional",  samples1.contains(explicitRetained));
		assertFalse("should be REMOVED because provisional",      samples1.contains(explicitRemoved));
		assertTrue("should be retained because most recent",      samples1.contains(recentRetained));

		List<WLSample> sortedByMediation  = new ArrayList<>(samples1);
		stats.setMediation(MediationType.AboveDatum);
		stats.monthlyStats.sortValueByQualifier(sortedByMediation);

		stats.removeMostRecentProvisional(samples1, sortedByMediation);
		assertTrue("should be retained because most recent not provisional", samples1.contains(recentRetained));
		assertTrue("should be retained because most recent not provisional", sortedByMediation.contains(recentRetained));
	}

	@Test
	public void test_removeProvisional_and_MostRecent() throws Exception {
		// note that all previous calculate tests prove that 100% non-provisional collections are retained

		List<WLSample> samples = new LinkedList<>();
		samples.add( createSample("2004-05-10T04:15:00-05:00", "1.0") );
		samples.add( createSample("2005-05-10T04:15:00-05:00", "1.0") );
		samples.add( createSample("2006-05-10T04:15:00-05:00", "1.0") );
		samples.add( createSample("2007-05-10T04:15:00-05:00", "1.0") );
		samples.add( createSample("2008-05-10T04:15:00-05:00", "1.0") );
		samples.add( createSample("2009-05-10T04:15:00-05:00", "1.0") );
		samples.add( createSample("2010-05-10T04:15:00-05:00", "1.0") );
		samples.add( createSample("2011-05-10T04:15:00-05:00", "1.0") );
		samples.add( createSample("2012-05-10T04:15:00-05:00", "1.0") );

		WLSample explicitRetained = createSample("2013-05-11T04:15:00-05:00", "1.0", false);
		WLSample explicitRemoved  = createSample("2014-05-12T04:15:00-05:00", "1.0", true);   // this one does not count because it is provisional
		WLSample recentProvisional= createSample("2018-05-13T04:15:00-05:00", "100.0", true); // this one only counts for latest value

		samples.add( explicitRetained ); // should be retained because not provisional
		samples.add( explicitRemoved );  // should be REMOVED because provisional
		samples.add( recentProvisional );// should be retained only for latest value because provisional most recent

		JsonData json = stats.calculate(spec, samples);
		JsonOverall overall = json.getOverall();
		Map<String, JsonMonthly> monthly = json.getMonthly();

		assertNotNull("overall should not be null", overall);
		assertNotNull("monthly should not be null", monthly);

		assertEquals("Expect MIN_VALUE to be ", "1.0",      overall.valueMin);
		assertEquals("Expect MAX_VALUE to be ", "1.0",      overall.valueMax);
		assertEquals("Expect MEDIAN to be ", "1.0",         overall.valueMedian);
		assertEquals("Expect LATEST_VALUE to be ", "100.0", overall.latestValue);
		assertEquals("Expect LATEST_PCTILE to be ", "1",    overall.latestPercentile);

		assertEquals("Expect all percentile to be ", "1.0",    monthly.get("5").percentiles.get(P10));
		assertEquals("Expect all percentile to be ", "1.0",    monthly.get("5").percentiles.get(P25));
		assertEquals("Expect all percentile to be ", "1.0",    monthly.get("5").percentiles.get(P50));
		assertEquals("Expect all percentile to be ", "1.0",    monthly.get("5").percentiles.get(P75));
		assertEquals("Expect all percentile to be ", "1.0",    monthly.get("5").percentiles.get(P90));
	}


	@Test
	public void test_removeProvisional_not_MostRecent() throws Exception {
		// note that all previous calculate tests prove that 100% non-provisional collections are retained

		List<WLSample> samples = new LinkedList<>();
		samples.add( createSample("2004-05-10T04:15:00-05:00", "1.0") );
		samples.add( createSample("2005-05-10T04:15:00-05:00", "1.0") );
		samples.add( createSample("2006-05-10T04:15:00-05:00", "1.0") );
		samples.add( createSample("2007-05-10T04:15:00-05:00", "1.0") );
		samples.add( createSample("2008-05-10T04:15:00-05:00", "1.0") );
		samples.add( createSample("2009-05-10T04:15:00-05:00", "1.0") );
		samples.add( createSample("2010-05-10T04:15:00-05:00", "1.0") );
		samples.add( createSample("2011-05-10T04:15:00-05:00", "1.0") );
		samples.add( createSample("2012-05-10T04:15:00-05:00", "1.0") );

		WLSample explicitRetained = createSample("2013-05-11T04:15:00-05:00", "1.0", false);
		WLSample explicitRemoved  = createSample("2014-05-12T04:15:00-05:00", "1.0", true);  // this one does not count because it is provisional
		WLSample recent           = createSample("2018-05-13T04:15:00-05:00", "100.0");

		samples.add( explicitRetained ); // should be retained because not provisional
		samples.add( explicitRemoved );  // should be REMOVED because provisional
		samples.add( recent );

		builder.mediation(MediationType.BelowLand);
		
		JsonData json = stats.calculate(spec, samples);
		JsonOverall overall = json.getOverall();
		Map<String, JsonMonthly> monthly = json.getMonthly();

		assertNotNull("overall should not be null", overall);
		assertNotNull("monthly should not be null", monthly);

		assertEquals("Expect MIN_VALUE to be ", "100.0",     overall.valueMin);
		assertEquals("Expect MAX_VALUE to be ",   "1.0",     overall.valueMax);
		assertEquals("Expect MEDIAN to be ",      "1.0",     overall.valueMedian);
		assertEquals("Expect LATEST_VALUE to be ", "100.0",  overall.latestValue);
		assertEquals("Expect LATEST_PCTILE to be ", "0",     overall.latestPercentile);

		assertEquals("Expect percentile to be 80.2", "80.2", monthly.get("5").percentiles.get(P10));
		assertEquals("Expect most percentile to be ", "1.0", monthly.get("5").percentiles.get(P25));
		assertEquals("Expect most percentile to be ", "1.0", monthly.get("5").percentiles.get(P50));
		assertEquals("Expect most percentile to be ", "1.0", monthly.get("5").percentiles.get(P75));
		assertEquals("Expect most percentile to be ", "1.0", monthly.get("5").percentiles.get(P90));
	}


	@Test
	public void test_useMostPrevalentPCodeMediatedValue_RetainOrderAsGivenByDefault_NonUSGS() throws Exception {

		WLSample a = createSample("2015-05-10T04:15:00-05:00", "95.1772");
		WLSample b = createSample("2015-05-11T04:15:00-05:00", "95.1567");
		WLSample c = createSample("2015-05-12T04:15:00-05:00", "95.1937");
		WLSample d = createSample("2015-05-13T04:15:00-05:00", "95.1959");

		List<WLSample> samples = new LinkedList<>();
		samples.add( a );
		samples.add( b );
		samples.add( c );
		samples.add( d );

		Specifier site = new Specifier("not USGS", "irrelevant");

		MediationType mediation = stats.findMostPrevalentMediation(site, samples);
		assertEquals("non-USGS sites should default to, below land surface type.", MediationType.BelowLand, mediation);

		List<WLSample> actual = stats.useMostPrevalentPCodeMediatedValue(site, samples, mediation);

		assertEquals("Original list should be used for non-USGS agencies.",samples,actual);
	}

	@Test
	public void test_useMostPrevalentPCodeMediatedValue_RetainOrderAsGivenByDefault_USGS() throws Exception {

		WLSample a = createSample("2015-05-10T04:15:00-05:00", "95.1772");
		WLSample b = createSample("2015-05-11T04:15:00-05:00", "95.1567");
		WLSample c = createSample("2015-05-12T04:15:00-05:00", "95.1937");
		WLSample d = createSample("2015-05-13T04:15:00-05:00", "95.1959");

		List<WLSample> samples = new LinkedList<>();
		samples.add( a );
		samples.add( b );
		samples.add( c );
		samples.add( d );

		Specifier site = new Specifier("USGS", "irrelevant");

		MediationType mediation = stats.findMostPrevalentMediation(site, samples);
		assertEquals("USGS sites should default to, below land surface type, with no PCODE.", MediationType.BelowLand, mediation);

		List<WLSample> actual = stats.useMostPrevalentPCodeMediatedValue(site, samples, mediation);

		assertEquals("Original list should be used for USGS agencies w/o PCODE.",samples,actual);
	}

	@Test
	public void test_useMostPrevalentPCodeMediatedValue_RetainOrderAsGivenByBSL() throws Exception {

		BigDecimal aboveDatum = new BigDecimal("99.9999");
		WLSample a = createSample("2015-05-10T04:15:00-05:00", "95.1772", PCode.P72019, aboveDatum);
		WLSample b = createSample("2015-05-11T04:15:00-05:00", "95.1567", PCode.P72019, aboveDatum);
		WLSample c = createSample("2015-05-12T04:15:00-05:00", "95.1937", PCode.P72019, aboveDatum);
		WLSample d = createSample("2015-05-13T04:15:00-05:00", "95.1959", PCode.P62610, aboveDatum);

		List<WLSample> samples = new LinkedList<>();
		samples.add( a );
		samples.add( b );
		samples.add( c );
		samples.add( d );

		Specifier site = new Specifier("USGS", "irrelevant");

		MediationType mediation = stats.findMostPrevalentMediation(site, samples);
		assertEquals("PCODE P72019, below land surface type, is most prevalent.", MediationType.BelowLand, mediation);

		List<WLSample> actual = stats.useMostPrevalentPCodeMediatedValue(site, samples, mediation);
		assertEquals("Original list should be used for USGS BSL PCODES.",samples,actual);
	}

	@Test
	public void test_useMostPrevalentPCodeMediatedValue_newOrderAsGivenByAboveDatum() throws Exception {

		BigDecimal aboveDatum = new BigDecimal("99.9999");
		WLSample a = createSample("2015-05-10T04:15:00-05:00", "95.1772", PCode.P72019, aboveDatum);
		WLSample b = createSample("2015-05-11T04:15:00-05:00", "95.1567", PCode.P62610, aboveDatum);
		WLSample c = createSample("2015-05-12T04:15:00-05:00", "95.1937", PCode.P62610, aboveDatum);
		WLSample d = createSample("2015-05-13T04:15:00-05:00", "95.1959", PCode.P62610, aboveDatum);

		List<WLSample> samples = new LinkedList<>();
		samples.add( a );
		samples.add( b );
		samples.add( c );
		samples.add( d );

		Specifier site = new Specifier("USGS", "irrelevant");

		MediationType mediation = stats.findMostPrevalentMediation(site, samples);
		assertEquals("PCODE P62610, above a datum type, is most prevalent.", MediationType.AboveDatum, mediation);

		List<WLSample> actual = stats.useMostPrevalentPCodeMediatedValue(site, samples, mediation);
		assertEquals("Above Datum values should be used in new list", aboveDatum, actual.get(0).value);
	}




	@Test
	public void testMostRecentProvistionalData_Removed() {
		List<WLSample> valueOrder = new LinkedList<>();
		WLSample provisional = createSample("2017-03-01","12.21");
		provisional.setProvsional(true);
		valueOrder.add( provisional );
		fillMarchData(valueOrder);
		List<WLSample> monthSamples = new ArrayList<WLSample>(valueOrder);
		StatisticsCalculator.sortByDateOrder(monthSamples);

		int expected = monthSamples.size();

		stats.overallStatistics.findMinMaxDatesAndDateRange(monthSamples, valueOrder);

		int actual = monthSamples.size();
		assertEquals("should REMOVE most recent provisional value here", expected, actual+1);
		assertFalse(monthSamples.contains(provisional));
		assertFalse(valueOrder.contains(provisional));
		assertEquals("most recent should be the provisional", provisional.value.toString(), builder.get(LATEST_VALUE));
	}
	@Test
	public void testMostRecentProvistional_overallStats() {
		List<WLSample> valueOrder = new LinkedList<>();
		WLSample provisional = createSample("2017-03-01","12.21");
		provisional.setProvsional(true);
		valueOrder.add( provisional );
		fillMarchData(valueOrder);
		List<WLSample> monthSamples = new ArrayList<WLSample>(valueOrder);
		StatisticsCalculator.sortByDateOrder(monthSamples);

		stats.setMediation(MediationType.AboveDatum);
		stats.overallStats(monthSamples, valueOrder);

		assertEquals(provisional.value.toString(), builder.get(LATEST_VALUE));
		assertFalse(monthSamples.contains(provisional));
		assertFalse(valueOrder.contains(provisional));

		assertEquals("7.98", builder.get(MEDIAN));
		assertEquals("0.745", builder.get(LATEST_PCTILE));
	}
	@Test
	public void testMostRecentProvistionalNONE_overallStats() {
		List<WLSample> valueOrder = new LinkedList<>();
		WLSample notProvisional = createSample("2017-03-01","12.21");
		valueOrder.add( notProvisional );
		fillMarchData(valueOrder);
		List<WLSample> monthSamples = new ArrayList<WLSample>(valueOrder);
		StatisticsCalculator.sortByDateOrder(monthSamples);

		stats.setMediation(MediationType.AboveDatum);
		stats.overallStats(monthSamples, valueOrder);

		assertEquals(notProvisional.value.toString(), builder.get(LATEST_VALUE));
		assertTrue(monthSamples.contains(notProvisional));
		assertTrue(valueOrder.contains(notProvisional));

		assertEquals("7.98", builder.get(MEDIAN));
		assertEquals("1", builder.get(LATEST_PCTILE));
	}
	@Test
	public void testMostRecentProvistionalData_notRemoved() {
		List<WLSample> monthSamples = new LinkedList<>();
		fillMarchData(monthSamples);
		WLSample provisional = createSample("2017-03-01","12.21");
		provisional.setProvsional(true);
		monthSamples.add( provisional );
		StatisticsCalculator.sortByDateOrder(monthSamples);

		int expected = monthSamples.size();
		stats.removeProvisionalButNotMostRecent(monthSamples, "SITE_NO");

		int actual = monthSamples.size();
		assertEquals("should KEEP most recent provisonal value here", expected, actual);
		assertTrue(monthSamples.contains(provisional));
	}

	protected void fillMarchData(List<WLSample> monthSamples) {
		monthSamples.add( createSample("2009-03-01","9.44") );
		monthSamples.add( createSample("2009-03-01","9.42") );
		monthSamples.add( createSample("2009-03-01","9.41") );
		monthSamples.add( createSample("2009-03-01","9.41") );
		monthSamples.add( createSample("2013-03-01","9.4") );
		monthSamples.add( createSample("2013-03-01","9.4") );
		monthSamples.add( createSample("2009-03-01","9.39") );
		monthSamples.add( createSample("2009-03-01","9.39") );
		monthSamples.add( createSample("2013-03-01","9.39") );
		monthSamples.add( createSample("2013-03-01","9.39") );
		monthSamples.add( createSample("2013-03-01","9.39") );
		monthSamples.add( createSample("2009-03-01","9.38") );
		monthSamples.add( createSample("2009-03-01","9.38") );
		monthSamples.add( createSample("2013-03-01","9.38") );
		monthSamples.add( createSample("2013-03-01","9.37") );
		monthSamples.add( createSample("2013-03-01","9.37") );
		monthSamples.add( createSample("2013-03-01","9.37") );
		monthSamples.add( createSample("2009-03-01","9.36") );
		monthSamples.add( createSample("2013-03-01","9.36") );
		monthSamples.add( createSample("2013-03-01","9.35") );
		monthSamples.add( createSample("2013-03-01","9.35") );
		monthSamples.add( createSample("2009-03-01","9.34") );
		monthSamples.add( createSample("2009-03-01","9.34") );
		monthSamples.add( createSample("2013-03-01","9.34") );
		monthSamples.add( createSample("2013-03-01","9.34") );
		monthSamples.add( createSample("2009-03-01","9.32") );
		monthSamples.add( createSample("2013-03-01","9.31") );
		monthSamples.add( createSample("2013-03-01","9.31") );
		monthSamples.add( createSample("2009-03-01","9.3") );
		monthSamples.add( createSample("2009-03-01","9.3") );
		monthSamples.add( createSample("2013-03-01","9.3") );
		monthSamples.add( createSample("2013-03-01","9.29") );
		monthSamples.add( createSample("2009-03-01","9.28") );
		monthSamples.add( createSample("2009-03-01","9.28") );
		monthSamples.add( createSample("2013-03-01","9.24") );
		monthSamples.add( createSample("2013-03-01","9.2") );
		monthSamples.add( createSample("2013-03-01","9.17") );
		monthSamples.add( createSample("2016-03-01","9.17") );
		monthSamples.add( createSample("2013-03-01","9.16") );
		monthSamples.add( createSample("2013-03-01","9.16") );
		monthSamples.add( createSample("2013-03-01","9.16") );
		monthSamples.add( createSample("2013-03-01","9.16") );
		monthSamples.add( createSample("2016-03-01","9.16") );
		monthSamples.add( createSample("2016-03-01","9.15") );
		monthSamples.add( createSample("2016-03-01","9.15") );
		monthSamples.add( createSample("2016-03-01","9.15") );
		monthSamples.add( createSample("2013-03-01","9.14") );
		monthSamples.add( createSample("2013-03-01","9.14") );
		monthSamples.add( createSample("2013-03-01","9.14") );
		monthSamples.add( createSample("2013-03-01","9.13") );
		monthSamples.add( createSample("2013-03-01","9.13") );
		monthSamples.add( createSample("2013-03-01","9.12") );
		monthSamples.add( createSample("2016-03-01","9.12") );
		monthSamples.add( createSample("2016-03-01","9.12") );
		monthSamples.add( createSample("2016-03-01","9.12") );
		monthSamples.add( createSample("2016-03-01","9.11") );
		monthSamples.add( createSample("2016-03-01","9.11") );
		monthSamples.add( createSample("2016-03-01","9.1") );
		monthSamples.add( createSample("2016-03-01","9.1") );
		monthSamples.add( createSample("2016-03-01","9.09") );
		monthSamples.add( createSample("2016-03-01","9.08") );
		monthSamples.add( createSample("2016-03-01","9.08") );
		monthSamples.add( createSample("2016-03-01","9.08") );
		monthSamples.add( createSample("2016-03-01","9.07") );
		monthSamples.add( createSample("2016-03-01","9.07") );
		monthSamples.add( createSample("2016-03-01","9.07") );
		monthSamples.add( createSample("2016-03-01","9.07") );
		monthSamples.add( createSample("2016-03-01","9.05") );
		monthSamples.add( createSample("2016-03-01","9.05") );
		monthSamples.add( createSample("2016-03-01","9.05") );
		monthSamples.add( createSample("2016-03-01","9.04") );
		monthSamples.add( createSample("2016-03-01","9.03") );
		monthSamples.add( createSample("2016-03-01","9.03") );
		monthSamples.add( createSample("2011-03-01","9.02") );
		monthSamples.add( createSample("2011-03-01","9.02") );
		monthSamples.add( createSample("2016-03-01","9.02") );
		monthSamples.add( createSample("2011-03-01","9.01") );
		monthSamples.add( createSample("2016-03-01","9.01") );
		monthSamples.add( createSample("2016-03-01","9.0") );
		monthSamples.add( createSample("2011-03-01","8.99") );
		monthSamples.add( createSample("2011-03-01","8.99") );
		monthSamples.add( createSample("2016-03-01","8.99") );
		monthSamples.add( createSample("2011-03-01","8.98") );
		monthSamples.add( createSample("2016-03-01","8.97") );
		monthSamples.add( createSample("2016-03-01","8.96") );
		monthSamples.add( createSample("2008-03-01","8.52") );
		monthSamples.add( createSample("2008-03-01","8.51") );
		monthSamples.add( createSample("2008-03-01","8.5") );
		monthSamples.add( createSample("2008-03-01","8.49") );
		monthSamples.add( createSample("2008-03-01","8.49") );
		monthSamples.add( createSample("2011-03-01","8.49") );
		monthSamples.add( createSample("2008-03-01","8.48") );
		monthSamples.add( createSample("2008-03-01","8.48") );
		monthSamples.add( createSample("2011-03-01","8.46") );
		monthSamples.add( createSample("2011-03-01","8.46") );
		monthSamples.add( createSample("2011-03-01","8.4") );
		monthSamples.add( createSample("2008-03-01","8.39") );
		monthSamples.add( createSample("2011-03-01","8.39") );
		monthSamples.add( createSample("2008-03-01","8.27") );
		monthSamples.add( createSample("2008-03-01","8.25") );
		monthSamples.add( createSample("2008-03-01","8.24") );
		monthSamples.add( createSample("2008-03-01","8.21") );
		monthSamples.add( createSample("2008-03-01","8.21") );
		monthSamples.add( createSample("2008-03-01","8.2") );
		monthSamples.add( createSample("2008-03-01","8.2") );
		monthSamples.add( createSample("2008-03-01","8.2") );
		monthSamples.add( createSample("2008-03-01","8.2") );
		monthSamples.add( createSample("2008-03-01","8.2") );
		monthSamples.add( createSample("2008-03-01","8.2") );
		monthSamples.add( createSample("2008-03-01","8.2") );
		monthSamples.add( createSample("2008-03-01","8.19") );
		monthSamples.add( createSample("2008-03-01","8.18") );
		monthSamples.add( createSample("2008-03-01","8.18") );
		monthSamples.add( createSample("2008-03-01","8.18") );
		monthSamples.add( createSample("2008-03-01","8.16") );
		monthSamples.add( createSample("2008-03-01","8.15") );
		monthSamples.add( createSample("2008-03-01","8.14") );
		monthSamples.add( createSample("2008-03-01","8.13") );
		monthSamples.add( createSample("2008-03-01","8.13") );
		monthSamples.add( createSample("2008-03-01","8.1") );
		monthSamples.add( createSample("2008-03-01","8.08") );
		monthSamples.add( createSample("2008-03-01","8.08") );
		monthSamples.add( createSample("2011-03-01","8.07") );
		monthSamples.add( createSample("2014-03-01","8.06") );
		monthSamples.add( createSample("2011-03-01","8.05") );
		monthSamples.add( createSample("2011-03-01","8.04") );
		monthSamples.add( createSample("2014-03-01","8.04") );
		monthSamples.add( createSample("2011-03-01","8.03") );
		monthSamples.add( createSample("2011-03-01","8.03") );
		monthSamples.add( createSample("2011-03-01","8.03") );
		monthSamples.add( createSample("2011-03-01","8.03") );
		monthSamples.add( createSample("2014-03-01","8.03") );
		monthSamples.add( createSample("2011-03-01","8.02") );
		monthSamples.add( createSample("2011-03-01","8.02") );
		monthSamples.add( createSample("2011-03-01","8.02") );
		monthSamples.add( createSample("2011-03-01","8.02") );
		monthSamples.add( createSample("2014-03-01","8.02") );
		monthSamples.add( createSample("2014-03-01","8.02") );
		monthSamples.add( createSample("2014-03-01","8.02") );
		monthSamples.add( createSample("2014-03-01","8.02") );
		monthSamples.add( createSample("2014-03-01","8.02") );
		monthSamples.add( createSample("2014-03-01","8.02") );
		monthSamples.add( createSample("2011-03-01","8.01") );
		monthSamples.add( createSample("2011-03-01","8.01") );
		monthSamples.add( createSample("2011-03-01","8.01") );
		monthSamples.add( createSample("2011-03-01","8.01") );
		monthSamples.add( createSample("2011-03-01","8.01") );
		monthSamples.add( createSample("2014-03-01","8.01") );
		monthSamples.add( createSample("2014-03-01","8.01") );
		monthSamples.add( createSample("2014-03-01","8.01") );
		monthSamples.add( createSample("2014-03-01","8.01") );
		monthSamples.add( createSample("2014-03-01","8.01") );
		monthSamples.add( createSample("2014-03-01","8.01") );
		monthSamples.add( createSample("2011-03-01","8.0") );
		monthSamples.add( createSample("2011-03-01","8.0") );
		monthSamples.add( createSample("2014-03-01","8.0") );
		monthSamples.add( createSample("2014-03-01","8.0") );
		monthSamples.add( createSample("2014-03-01","8.0") );
		monthSamples.add( createSample("2014-03-01","8.0") );
		monthSamples.add( createSample("2014-03-01","8.0") );
		monthSamples.add( createSample("2014-03-01","8.0") );
		monthSamples.add( createSample("2011-03-01","7.99") );
		monthSamples.add( createSample("2014-03-01","7.99") );
		monthSamples.add( createSample("2014-03-01","7.99") );
		monthSamples.add( createSample("2014-03-01","7.99") );
		monthSamples.add( createSample("2014-03-01","7.98") );
		monthSamples.add( createSample("2014-03-01","7.98") );
		monthSamples.add( createSample("2014-03-01","7.97") );
		monthSamples.add( createSample("2014-03-01","7.97") );
		monthSamples.add( createSample("2011-03-01","7.96") );
		monthSamples.add( createSample("2014-03-01","7.94") );
		monthSamples.add( createSample("2011-03-01","7.88") );
		monthSamples.add( createSample("2015-03-01","7.65") );
		monthSamples.add( createSample("2014-03-01","7.46") );
		monthSamples.add( createSample("2015-03-01","7.46") );
		monthSamples.add( createSample("2015-03-01","7.45") );
		monthSamples.add( createSample("2014-03-01","7.31") );
		monthSamples.add( createSample("2012-03-01","7.16") );
		monthSamples.add( createSample("2012-03-01","7.13") );
		monthSamples.add( createSample("2012-03-01","7.11") );
		monthSamples.add( createSample("2012-03-01","7.08") );
		monthSamples.add( createSample("2012-03-01","7.07") );
		monthSamples.add( createSample("2012-03-01","7.05") );
		monthSamples.add( createSample("2012-03-01","7.04") );
		monthSamples.add( createSample("2012-03-01","7.02") );
		monthSamples.add( createSample("2012-03-01","7.02") );
		monthSamples.add( createSample("2012-03-01","7.0") );
		monthSamples.add( createSample("2012-03-01","6.99") );
		monthSamples.add( createSample("2012-03-01","6.97") );
		monthSamples.add( createSample("2012-03-01","6.95") );
		monthSamples.add( createSample("2012-03-01","6.95") );
		monthSamples.add( createSample("2012-03-01","6.94") );
		monthSamples.add( createSample("2007-03-01","6.93") );
		monthSamples.add( createSample("2007-03-01","6.92") );
		monthSamples.add( createSample("2007-03-01","6.92") );
		monthSamples.add( createSample("2007-03-01","6.92") );
		monthSamples.add( createSample("2012-03-01","6.91") );
		monthSamples.add( createSample("2007-03-01","6.9") );
		monthSamples.add( createSample("2007-03-01","6.9") );
		monthSamples.add( createSample("2012-03-01","6.9") );
		monthSamples.add( createSample("2007-03-01","6.88") );
		monthSamples.add( createSample("2012-03-01","6.87") );
		monthSamples.add( createSample("2007-03-01","6.85") );
		monthSamples.add( createSample("2007-03-01","6.85") );
		monthSamples.add( createSample("2010-03-01","6.85") );
		monthSamples.add( createSample("2007-03-01","6.84") );
		monthSamples.add( createSample("2012-03-01","6.84") );
		monthSamples.add( createSample("2012-03-01","6.83") );
		monthSamples.add( createSample("2012-03-01","6.83") );
		monthSamples.add( createSample("2007-03-01","6.82") );
		monthSamples.add( createSample("2012-03-01","6.82") );
		monthSamples.add( createSample("2007-03-01","6.8") );
		monthSamples.add( createSample("2007-03-01","6.79") );
		monthSamples.add( createSample("2007-03-01","6.79") );
		monthSamples.add( createSample("2010-03-01","6.79") );
		monthSamples.add( createSample("2007-03-01","6.78") );
		monthSamples.add( createSample("2007-03-01","6.78") );
		monthSamples.add( createSample("2012-03-01","6.77") );
		monthSamples.add( createSample("2012-03-01","6.75") );
		monthSamples.add( createSample("2012-03-01","6.74") );
		monthSamples.add( createSample("2007-03-01","6.73") );
		monthSamples.add( createSample("2012-03-01","6.73") );
		monthSamples.add( createSample("2007-03-01","6.72") );
		monthSamples.add( createSample("2007-03-01","6.72") );
		monthSamples.add( createSample("2007-03-01","6.7") );
		monthSamples.add( createSample("2010-03-01","6.69") );
		monthSamples.add( createSample("2012-03-01","6.68") );
		monthSamples.add( createSample("2006-03-01","6.66") );
		monthSamples.add( createSample("2006-03-01","6.66") );
		monthSamples.add( createSample("2007-03-01","6.66") );
		monthSamples.add( createSample("2007-03-01","6.66") );
		monthSamples.add( createSample("2007-03-01","6.65") );
		monthSamples.add( createSample("2006-03-01","6.64") );
		monthSamples.add( createSample("2006-03-01","6.62") );
		monthSamples.add( createSample("2007-03-01","6.62") );
		monthSamples.add( createSample("2010-03-01","6.62") );
		monthSamples.add( createSample("2007-03-01","6.61") );
		monthSamples.add( createSample("2006-03-01","6.6") );
		monthSamples.add( createSample("2012-03-01","6.59") );
		monthSamples.add( createSample("2012-03-01","6.59") );
		monthSamples.add( createSample("2006-03-01","6.56") );
		monthSamples.add( createSample("2006-03-01","6.54") );
		monthSamples.add( createSample("2006-03-01","6.53") );
		monthSamples.add( createSample("2006-03-01","6.53") );
		monthSamples.add( createSample("2007-03-01","6.53") );
		monthSamples.add( createSample("2006-03-01","6.49") );
		monthSamples.add( createSample("2010-03-01","6.49") );
		monthSamples.add( createSample("2010-03-01","6.48") );
		monthSamples.add( createSample("2006-03-01","6.46") );
		monthSamples.add( createSample("2007-03-01","6.46") );
		monthSamples.add( createSample("2006-03-01","6.44") );
		monthSamples.add( createSample("2006-03-01","6.44") );
		monthSamples.add( createSample("2006-03-01","6.44") );
		monthSamples.add( createSample("2006-03-01","6.42") );
		monthSamples.add( createSample("2006-03-01","6.4") );
		monthSamples.add( createSample("2006-03-01","6.4") );
		monthSamples.add( createSample("2010-03-01","6.4") );
		monthSamples.add( createSample("2012-03-01","6.4") );
		monthSamples.add( createSample("2006-03-01","6.39") );
		monthSamples.add( createSample("2006-03-01","6.37") );
		monthSamples.add( createSample("2006-03-01","6.36") );
		monthSamples.add( createSample("2006-03-01","6.35") );
		monthSamples.add( createSample("2006-03-01","6.35") );
		monthSamples.add( createSample("2006-03-01","6.35") );
		monthSamples.add( createSample("2006-03-01","6.33") );
		monthSamples.add( createSample("2006-03-01","6.33") );
		monthSamples.add( createSample("2010-03-01","6.33") );
		monthSamples.add( createSample("2010-03-01","6.33") );
		monthSamples.add( createSample("2006-03-01","6.32") );
		monthSamples.add( createSample("2006-03-01","6.32") );
		monthSamples.add( createSample("2010-03-01","6.32") );
		monthSamples.add( createSample("2006-03-01","6.3") );
		monthSamples.add( createSample("2006-03-01","6.3") );
		monthSamples.add( createSample("2007-03-01","6.29") );
		monthSamples.add( createSample("2010-03-01","6.28") );
		monthSamples.add( createSample("2010-03-01","6.28") );
		monthSamples.add( createSample("2012-03-01","6.28") );
		monthSamples.add( createSample("2006-03-01","6.27") );
		monthSamples.add( createSample("2010-03-01","6.26") );
		monthSamples.add( createSample("2015-03-01","6.23") );
		monthSamples.add( createSample("2006-03-01","6.22") );
		monthSamples.add( createSample("2006-03-01","6.21") );
		monthSamples.add( createSample("2015-03-01","6.16") );
		monthSamples.add( createSample("2007-03-01","6.13") );
		monthSamples.add( createSample("2015-03-01","6.11") );
		monthSamples.add( createSample("2015-03-01","6.07") );
		monthSamples.add( createSample("2015-03-01","6.06") );
		monthSamples.add( createSample("2015-03-01","6.01") );
		monthSamples.add( createSample("2015-03-01","6.01") );
		monthSamples.add( createSample("2015-03-01","5.94") );
		monthSamples.add( createSample("2015-03-01","5.91") );
		monthSamples.add( createSample("2015-03-01","5.86") );
		monthSamples.add( createSample("2007-03-01","5.84") );
		monthSamples.add( createSample("2015-03-01","5.83") );
		monthSamples.add( createSample("2015-03-01","5.81") );
		monthSamples.add( createSample("2015-03-01","5.7") );
		monthSamples.add( createSample("2015-03-01","5.6") );
		monthSamples.add( createSample("2015-03-01","5.59") );
		monthSamples.add( createSample("2015-03-01","5.56") );
		monthSamples.add( createSample("2007-03-01","5.53") );
		monthSamples.add( createSample("2015-03-01","5.44") );
		monthSamples.add( createSample("2015-03-01","5.21") );
		monthSamples.add( createSample("2015-03-01","5.19") );
		monthSamples.add( createSample("2015-03-01","5.08") );
		monthSamples.add( createSample("2015-03-01","4.84") );
		monthSamples.add( createSample("2010-03-01","4.75") );
		monthSamples.add( createSample("2015-03-01","4.75") );
		monthSamples.add( createSample("2015-03-01","4.45") );
		monthSamples.add( createSample("2010-03-01","4.19") );
		monthSamples.add( createSample("2010-03-01","4.17") );
		monthSamples.add( createSample("2010-03-01","4.07") );
		monthSamples.add( createSample("2010-03-01","4.03") );
		monthSamples.add( createSample("2010-03-01","4.03") );
		monthSamples.add( createSample("2015-03-01","3.96") );
		monthSamples.add( createSample("2010-03-01","3.95") );
		monthSamples.add( createSample("2010-03-01","3.92") );
		monthSamples.add( createSample("2015-03-01","3.88") );
		monthSamples.add( createSample("2010-03-01","3.87") );
		monthSamples.add( createSample("2010-03-01","3.82") );
		monthSamples.add( createSample("2010-03-01","3.76") );
		monthSamples.add( createSample("2015-03-01","3.58") );
		monthSamples.add( createSample("2010-03-01","3.57") );
		monthSamples.add( createSample("2010-03-01","3.42") );
		monthSamples.add( createSample("2010-03-01","3.36") );
		monthSamples.add( createSample("2015-03-01","3.23") );
		monthSamples.add( createSample("2010-03-01","3.03") );
		monthSamples.add( createSample("2015-03-01","2.83") );
		monthSamples.add( createSample("2010-03-01","2.58") );
		monthSamples.add( createSample("2010-03-01","2.11") );
		monthSamples.add( createSample("2010-03-01","2.06") );
		monthSamples.add( createSample("2015-03-01","1.78") );
		monthSamples.add( createSample("2010-03-01","1.39") );
	}
	@Test
	public void test_monthlyStats_yearly_monthly() throws Exception {
		List<WLSample> samples = new LinkedList<>();
		samples.add( createSample("2000-05-10T04:15:00-05:00", "95.1772") );
		samples.add( createSample("2001-05-10T04:15:00-05:00", "95.1567") );
		samples.add( createSample("2002-05-10T04:15:00-05:00", "95.1937") );
		samples.add( createSample("2003-05-10T04:15:00-05:00", "95.1959") );
		samples.add( createSample("2004-05-10T04:15:00-05:00", "95.1442") );
		samples.add( createSample("2005-05-10T04:15:00-05:00", "95.0610") );
		samples.add( createSample("2006-05-10T04:15:00-05:00", "95.1591") );
		samples.add( createSample("2007-05-10T04:15:00-05:00", "95.1195") );
		samples.add( createSample("2008-05-10T04:15:00-05:00", "95.1065") );
		samples.add( createSample("2009-05-10T04:15:00-05:00", "95.0925") );
		samples.add( createSample("2010-05-10T04:15:00-05:00", "95.1990") );
		samples.add( createSample("2010-05-10T04:15:00-05:00", "95.1682") );
		samples.add( createSample("2000-04-10T04:15:00-05:00", "94.1772") );
		samples.add( createSample("2001-04-10T04:15:00-05:00", "94.1567") );
		samples.add( createSample("2002-04-10T04:15:00-05:00", "94.1937") );
		samples.add( createSample("2003-04-10T04:15:00-05:00", "94.1959") );
		samples.add( createSample("2004-04-10T04:15:00-05:00", "94.1442") );
		samples.add( createSample("2005-04-10T04:15:00-05:00", "94.0610") );
		samples.add( createSample("2006-04-10T04:15:00-05:00", "94.1591") );
		samples.add( createSample("2007-04-10T04:15:00-05:00", "94.1195") );
		samples.add( createSample("2008-04-10T04:15:00-05:00", "94.1065") );
		samples.add( createSample("2009-04-10T04:15:00-05:00", "94.0925") );
		samples.add( createSample("2010-04-10T04:15:00-05:00", "94.1990") );
		samples.add( createSample("2010-04-10T04:15:00-05:00", "94.1682") );
		samples.add( createSample("2000-03-10T04:15:00-05:00", "93.1772") );
		samples.add( createSample("2001-03-10T04:15:00-05:00", "93.1567") );
		samples.add( createSample("2002-03-10T04:15:00-05:00", "93.1937") );
		samples.add( createSample("2003-03-10T04:15:00-05:00", "93.1959") );
		samples.add( createSample("2004-03-10T04:15:00-05:00", "93.1442") );
		samples.add( createSample("2005-03-10T04:15:00-05:00", "93.0610") );
		samples.add( createSample("2006-03-10T04:15:00-05:00", "93.1591") );
		samples.add( createSample("2007-03-10T04:15:00-05:00", "93.1195") );
		samples.add( createSample("2008-03-10T04:15:00-05:00", "93.1065") );
		samples.add( createSample("2009-03-10T04:15:00-05:00", "93.0925") );
		samples.add( createSample("2010-03-10T04:15:00-05:00", "93.1990") );
		samples.add( createSample("2010-03-10T04:15:00-05:00", "93.1682") );

		// here are some extra data points in months that have a <10 year window and excluded for calculation
		samples.add( createSample("2013-06-10T04:15:00-05:00", "93.1772") );
		samples.add( createSample("2013-07-10T04:15:00-05:00", "93.1567") );
		samples.add( createSample("2013-08-10T04:15:00-05:00", "93.1937") );

		List<WLSample> sorted = new LinkedList<>(samples);

		builder.mediation(MediationType.AboveDatum);
		// we are not testing this method so mock it to return what we need
		WLMonthlyStats mockstats = new WLMonthlyStats(env, builder) {
			@Override
			public List<WLSample> medianMonthlyValues(List<WLSample> monthSamples,
					Function<List<WLSample>, List<WLSample>> sortBy) {
				// do not modify, testing the months. This prevents normalization to test aggregations
				return monthSamples;
			}
		};

		StatisticsCalculator.sortByValueOrderAscending(sorted);
		mockstats.monthlyStats(sorted); // MediationType.AboveDatum
		Map<String, JsonMonthly> monthly = builder.build().getMonthly();

		assertEquals("Expect only 3 monthly stats - the other three do not have ten yrs data", 3, monthly.size());
		assertEquals("Expect May median to be", "95.1579", monthly.get("5").percentiles.get(P50));
		assertEquals("Expect Apr median to be", "94.1579", monthly.get("4").percentiles.get(P50));
		assertEquals("Expect Mar median to be", "93.1579", monthly.get("3").percentiles.get(P50));

		assertEquals("Expect sample count to be ", 12, monthly.get("5").sampleCount);
		assertEquals("Expect sample count to be ", 12, monthly.get("4").sampleCount);
		assertEquals("Expect sample count to be ", 12, monthly.get("3").sampleCount);

		assertEquals("Expect record years to be 11 because there are 11 unique years in the given data even though there are 12 data entries and not a difference of 10 because we count the months.", "11", monthly.get("5").recordYears);
		assertEquals("Expect record years to be 11 because there are 11 unique years in the given data even though there are 12 data entries and not a difference of 10 because we count the months.", "11", monthly.get("4").recordYears);
		assertEquals("Expect record years to be 11 because there are 11 unique years in the given data even though there are 12 data entries and not a difference of 10 because we count the months.", "11", monthly.get("3").recordYears);
	}
	
}

