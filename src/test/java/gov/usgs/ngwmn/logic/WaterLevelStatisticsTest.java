package gov.usgs.ngwmn.logic;

import static gov.usgs.wma.statistics.logic.StatisticsCalculator.*;
import static org.junit.Assert.*;

//import static gov.usgs.wma.statistics.logic.SigFigMathUtil.*;
//import static gov.usgs.ngwmn.logic.WaterLevelStatistics.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;

//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.Reader;
//import java.sql.SQLException;
//import org.mockito.invocation.InvocationOnMock;
//import org.mockito.stubbing.Answer;
//import java.util.Date;
//import java.util.Collections;
//import gov.usgs.wma.statistics.logic.SigFigMathUtil;
//import gov.usgs.ngwmn.model.Elevation;
//import org.junit.Ignore;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.usgs.ngwmn.logic.WaterLevelStatistics.MediationType;
import gov.usgs.ngwmn.model.PCode;
import gov.usgs.ngwmn.model.Specifier;
import gov.usgs.ngwmn.model.WLSample;
import gov.usgs.ngwmn.model.WellDataType;
import gov.usgs.wma.statistics.logic.StatisticsCalculator;
import gov.usgs.wma.statistics.model.Value;


public class WaterLevelStatisticsTest {

	WaterLevelStatistics stats = null;

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
//				if (e.value.compareTo(sample.value) < 0) {
//					break;
//				}
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
		stats = new WaterLevelStatistics();
	}


//	@Test
//	public void test_yearDiff_9yr_simple() throws Exception {
//		BigDecimal yrs = stats.yearDiff("2009-01-02", "2000-01-01");
//		assertEquals("9.0", yrs.toString());
//	}
//	@Test
//	public void test_yearDiff_10yr_simple() throws Exception {
//		BigDecimal yrs = stats.yearDiff("2010-01-02", "2000-01-01");
//		assertEquals("10.0", yrs.toString());
//	}
//	@Test
//	public void test_yearDiff_10yr_midmonth() throws Exception {
//		BigDecimal yrs = stats.yearDiff("2010-02-15", "2000-02-14");
//		assertEquals("10.0", yrs.toString());
//	}
//	@Test
//	public void test_yearDiff_10yr_minOneDay() throws Exception {
//		BigDecimal yrs = stats.yearDiff("2010-02-15", "2000-02-16");
//		assertEquals("10.0", yrs.toString());
//	}
//	@Test
//	public void test_yearDiff_10yr_plusOneDay() throws Exception {
//		BigDecimal yrs = stats.yearDiff("2010-02-15", "2000-02-14");
//		assertEquals("10.0", yrs.toString());
//	}
//	@Test
//	public void test_yearDiff_11yr_easy() throws Exception {
//		BigDecimal yrs = stats.yearDiff("2011-01-01", "2000-01-01");
//		assertEquals("11.0", yrs.toString());
//	}
//	// I used this much more complex method to ensure the above was working.
//	// The below is more accurate than necessary.
////	try {
////		Date begin = YEAR_MONTH_DAY.parse( fixMissingMonthAndDay(minDate) );
////		Date end   = YEAR_MONTH_DAY.parse( fixMissingMonthAndDay(maxDate) );
////		ReadableInterval duration = new Interval(begin.getTime(), end.getTime());
////		double yrs = Weeks.weeksIn(duration).getWeeks()/52.0;
////		MathContext mc = new MathContext(4);
////		if (yrs > 10) {
////			mc = new MathContext(5);
////		}
////		return new BigDecimal(yrs, mc);
////	} catch(ParseException e) {
////		return BigDecimal.ZERO;
////	}
//
//
//	@Test
//	public void test_daysDiff_1day() throws Exception {
//		String date1 = "2015-12-12";
//		String date2 = "2015-12-13";
//		BigDecimal actual = stats.daysDiff(date2,date1);
//		assertEquals("Expect 1 day", 1, actual.intValue());
//	}
//	@Test
//	public void test_daysDiff_406days() throws Exception {
//		String date1 = "2014-11-14";
//		String date2 = "2015-12-25";
//		BigDecimal actual = stats.daysDiff(date2,date1);
//		assertEquals("Expect 406 day", 406, actual.intValue());
//	}
//	@Test
//	public void test_fixMissingMonthAndDay_doNothing() throws Exception {
//		String expect = "2015-12-12";
//		String actual = stats.fixMissingMonthAndDay(expect);
//		assertEquals("Expect fine date to remain unchaanged", expect, actual);
//	}
//	public void test_fixMissingMonthAndDay_midMonth() throws Exception {
//		String expect = "2015-12";
//		String actual = stats.fixMissingMonthAndDay(expect);
//		assertEquals("Expect fine date to be the 15h of the month", expect+"-15", actual);
//	}
//	public void test_fixMissingMonthAndDay_midYear() throws Exception {
//		String expect = "2015";
//		String actual = stats.fixMissingMonthAndDay(expect);
//		assertEquals("Expect fine date to be June 30th of the given month", expect+"-06-30", actual);
//	}
//
//	@Test
//	public void test_removeNulls_noNulls() throws Exception {
//		WLSample min = createSample("2005-12-10T04:15:00-05:00", "1.0");
//		WLSample mid = createSample("2010-06-10T04:15:00-05:00", "1.5");
//		WLSample max = createSample("2015-06-10T04:15:00-05:00", "2.0");
//		List<WLSample> samples = new ArrayList<>(3);
//		samples.add(mid);
//		samples.add(min);
//		samples.add(max);
//		stats.removeNulls(samples, "well id");
//		assertEquals("size should remain unchanged", 3, samples.size());
//	}
//	@Test
//	public void test_removeNulls_nullInstance() throws Exception {
//		WLSample min = createSample("2005-12-10T04:15:00-05:00", "1.0");
//		WLSample mid = createSample("2010-06-10T04:15:00-05:00", "1.5");
//		WLSample max = createSample("2015-06-10T04:15:00-05:00", "2.0");
//		List<WLSample> samples = new ArrayList<>(3);
//		samples.add(mid);
//		samples.add(min);
//		samples.add(max);
//		samples.add(null);
//		assertEquals("ensuring that the null has been added for a valid boundy condition", 4, samples.size());
//		stats.removeNulls(samples, "well id");
//		assertEquals("null instance should be removed", 3, samples.size());
//	}
//	@Test
//	public void test_removeNulls_nullValue() throws Exception {
//		WLSample min = createSample("2005-12-10T04:15:00-05:00", "1.0");
//		WLSample mid = createSample("2010-06-10T04:15:00-05:00", "1.5");
//		WLSample n1 = createSample("2015-06-10T04:15:00-05:00", null);
//		WLSample n2 = createSample(null, "2.0");
//		WLSample n3 = createSample(null, null);
//		List<WLSample> samples = new ArrayList<>(3);
//		samples.add(mid);
//		samples.add(min);
//		samples.add(n1);
//		samples.add(n2);
//		samples.add(n3);
//		stats.removeNulls(samples, "well id");
//		assertEquals("null instance should be removed", 2, samples.size());
//	}
	public void test_doesThisSiteQualifyForMonthlyStats_montly_qualifies() throws Exception {
		String recent = today();
		String today  = today();

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
		int days   = daysDiff(today, recent).intValue();
		assertTrue(days < 406);
		actual = stats.doesThisSiteQualifyForMonthlyStats(years, recent, today);
		assertTrue("A site with 10 years of data and a <406 year old date for the most recent value should be true", actual);

		recent = "2015-01-20";
		days   = daysDiff(today, recent).intValue();
		assertTrue(days == 406);
		actual = stats.doesThisSiteQualifyForMonthlyStats(years, recent, today);
		assertTrue("A site with 10 years of data and a =406 year old date for the most recent value should be true", actual);
	}


	@Test
	public void test_doesThisSiteQualifyForMonthlyStats_monthly_disqualified_by_years() throws Exception {
		String recent = today();
		String today  = today();

		BigDecimal years = new BigDecimal("9");
		boolean   actual = stats.doesThisSiteQualifyForMonthlyStats(years, recent, today);
		assertFalse("A site with <10 years of data and a recent date for the most recent value should be false", actual);
	}

	@Test
	public void test_doesThisSiteQualifyForMonthlyStats_monthly_disqualified_by_recent_measure() throws Exception {
		BigDecimal years = new BigDecimal("11");

		String today  = "2016-03-01";

		String recent = "2014-02-01";
		int days = daysDiff(today, recent).intValue();
		assertTrue(days > 406);
		boolean actual = stats.doesThisSiteQualifyForMonthlyStats(years, recent, today);
		assertFalse("A site with 10 years of data and a >406 days old date for the most recent value should be false", actual);

		recent = "2015-01-19"; // 365 days from 2015-01-19 to 2016-01-19, 30 more day is 2016-02-19, one more week is 2016-02-26, and 4 more days is 2016-03-01 because 2016 is leap year
		days   = daysDiff(today, recent).intValue();
		assertTrue(days == 407);
		actual = stats.doesThisSiteQualifyForMonthlyStats(years, recent, today);
		assertFalse("A site with 10 years of data and a =407 year old date for the most recent value should be false "
		+"(note that 2016 is a leap year and the date math should handle this to the day. if this failed then check leap year math)", actual);

		recent = "2015-01-18";
		days   = daysDiff(today, recent).intValue();
		assertTrue(days > 406);
		actual = stats.doesThisSiteQualifyForMonthlyStats(years, recent, today);
		assertFalse("A site with 10 years of data and a >406 year old date for the most recent value should be false", actual);
	}

	@Test
	public void test_doesThisMonthQualifyForStats_notEngoughData() throws Exception {
		List<WLSample> monthSamples = new ArrayList<>(2);
		monthSamples.add(createSample("2005-06-10T04:15:00-05:00", "1.0"));
		monthSamples.add(createSample("2015-06-10T04:15:00-05:00", "2.0"));

		boolean qualifies = stats.doesThisMonthQualifyForStats(monthSamples);
		assertFalse("A month must have 10 unique years of data not just a 10 yr date range.", qualifies);

		qualifies = stats.doesThisMonthQualifyForStats(new ArrayList<>(2));
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

		boolean qualifies = stats.doesThisMonthQualifyForStats(monthSamples);
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

		boolean qualifies = stats.doesThisMonthQualifyForStats(monthSamples);
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

		boolean qualifies = stats.doesThisMonthQualifyForStats(monthSamples);
		assertTrue("A month with 10 unique years of data is valid.", qualifies);
	}

//	@Test
//	public void test_today__ensure_that_the_today_method_returns_the_current_month_and_year() throws Exception {
//		String today = stats.today();
//		Date date = new Date();
//
//		// the Date.getYear() returns a previous century truncated value (88 rather than 1988)
//		// and it returns 116 for 2016; hence the deprecation and addition of 1900 for a proper year number
//		@SuppressWarnings("deprecation")
//		String year = ""+(date.getYear()+1900);
//		assertEquals(year, StatisticsCalculator.yearUTC(today) );
//
//		// months are zero based in Date and humans use one based - i.e. January return zero but we want 1.
//		// further more, for testing we want a double digit month zero padded. There are many ways (and most better)
//		// than this to get such a value but it is a quick way to get what is needed without the complication of Calendar.
//		@SuppressWarnings("deprecation")
//		String month = ""+(date.getMonth()+1);
//		month = (month.length() == 1 ?"0" :"") + month;
//		assertEquals(month, StatisticsCalculator.monthUTC(today) );
//	}
//
//	@Test
//	public void test_month_year_extract_from_UTC() throws Exception {
//		String dateUTC = "2000-12-23...";
//
//		String year = dateUTC.substring(0, 4);
//		assertEquals("2000", year);
//
//		String month = dateUTC.substring(5, 7);
//		assertEquals("12", month);
//
//		year = StatisticsCalculator.yearUTC(dateUTC);
//		assertEquals("2000", year);
//
//		month = StatisticsCalculator.monthUTC(dateUTC);
//		assertEquals("12", month);
//	}

	@Test
	public void test_overallStats_0() throws Exception {
		List<WLSample> samples = new ArrayList<>(0);
		Map<String,String> overall = stats.overallStats(samples, samples, MediationType.AboveDatum);
		assertEquals("Expect empty stats for no rows", 0, overall.size());
	}
	@Test
	public void test_overallStats_min_max_1() throws Exception {
		WLSample first = createSample("2005-06-10T04:15:00-05:00", "1.0");
		List<WLSample> samples = new ArrayList<>(1);
		samples.add(first);
		Map<String,String> overall = stats.overallStats(samples, samples, MediationType.AboveDatum);
		assertEquals("Expect max to be first.time", first.time, overall.get(MAX_DATE));
		assertEquals("Expect min to be first.time", first.time, overall.get(MIN_DATE));
		assertEquals("Expect max to be first.value", first.value.toString(), overall.get(MAX_VALUE));
		assertEquals("Expect min to be first.value", first.value.toString(), overall.get(MIN_VALUE));
		assertEquals("Expect latest to be first.value", first.value.toString(), overall.get(LATEST_VALUE));
		assertEquals("Expect IS_RANKED to be 'N'", "N", overall.get(IS_RANKED));

		assertEquals("Expect record_years to be zero for one record", "0.0", overall.get(RECORD_YEARS));
	}
	@Test
	public void test_overallStats_min_max_2() throws Exception {
		WLSample min = createSample("2005-06-10T04:15:00-05:00", "1.0");
		WLSample max = createSample("2015-06-10T04:15:00-05:00", "2.0");
		List<WLSample> samples = new ArrayList<>(2);
		samples.add(min);
		samples.add(max);
		List<WLSample> sorted = new LinkedList<>(samples);
		sortByValueOrderAscending(sorted);

		Map<String,String> overall = stats.overallStats(samples, sorted, MediationType.AboveDatum);
		assertEquals("Expect max to be max.time", max.time, overall.get(MAX_DATE));
		assertEquals("Expect min to be min.time", min.time, overall.get(MIN_DATE));
		assertEquals("Expect max to be max.value", max.value.toString(), overall.get(MAX_VALUE));
		assertEquals("Expect min to be min.value", min.value.toString(), overall.get(MIN_VALUE));

		assertEquals("Expect record_years to be ten years", "10.0", overall.get(RECORD_YEARS));
	}
	@Test
	public void test_overallStats_min_max_2_rev() throws Exception {
		WLSample min = createSample("2005-06-10T04:15:00-05:00", "1.0");
		WLSample max = createSample("2015-12-10T04:15:00-05:00", "2.0");
		List<WLSample> samples = new LinkedList<>();
		samples.add(max);
		samples.add(min);
		List<WLSample> sorted = new LinkedList<>(samples);
		sortByValueOrderAscending(sorted);

		Map<String,String> overall = stats.overallStats(samples, sorted, MediationType.AboveDatum);
		assertEquals("Expect max to be max.time", max.time, overall.get(MAX_DATE));
		assertEquals("Expect min to be min.time", min.time, overall.get(MIN_DATE));
		assertEquals("Expect max to be max.value", max.value.toString(), overall.get(MAX_VALUE));
		assertEquals("Expect min to be min.value", min.value.toString(), overall.get(MIN_VALUE));

		assertEquals("Expect record_years to be ten and 1/2 years", "10.5", overall.get(RECORD_YEARS));
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
		sortByValueOrderAscending(sorted);

		Map<String,String> overall = stats.overallStats(samples, sorted, MediationType.AboveDatum);
		assertEquals("Expect max to be max.time", max.time, overall.get(MAX_DATE));
		assertEquals("Expect min to be min.time", min.time, overall.get(MIN_DATE));
		assertEquals("Expect max to be max.value", max.value.toString(), overall.get(MAX_VALUE));
		assertEquals("Expect min to be min.value", min.value.toString(), overall.get(MIN_VALUE));

		assertEquals("Expect record_years to be 9 and 1/2 years", "9.5", overall.get(RECORD_YEARS));
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
		sortByValueOrderAscending(sorted);

		Map<String,String> overall = stats.overallStats(samples, sorted, MediationType.AboveDatum);
		assertEquals("Expect count to be 3", "3", overall.get(SAMPLE_COUNT));
		assertEquals("Expect median to be mid.value", mid.value.toString(), overall.get(MEDIAN));
	}

//
//	@Test
//	public void test_overallStats_OrderSamples_3() throws Exception {
//		WLSample min = createSample("2005-06-10T04:15:00-05:00", "1.0");
//		WLSample mid = createSample("2010-06-10T04:15:00-05:00", "1.5");
//		WLSample max = createSample("2015-06-10T04:15:00-05:00", "2.0");
//		List<WLSample> samples = new OrderedSamples();
//		samples.add(mid);
//		samples.add(min);
//		samples.add(max);
//		assertEquals("Expect min to be first", min, samples.get(0) );
//		assertEquals("Expect mid to be middle", mid, samples.get(1) );
//		assertEquals("Expect max to be last", max, samples.get(2) );
//	}
//
//	@Test
//	public void test_percentileValue_2_50pct() throws Exception {
//		WLSample min = createSample("2005-06-10T04:15:00-05:00", "1.0");
//		WLSample max = createSample("2015-06-10T04:15:00-05:00", "2.0");
//		List<WLSample> samples = new OrderedSamples();
//		samples.add(min);
//		samples.add(max);
//		BigDecimal pct50 = valueOfPercentile(samples, new BigDecimal(".5"), WLSample::valueOf);
//		assertEquals("Expect 50% percetile to be the middle value", "1.5", pct50.toString() );
//	}
//
//	// a new arbitrary type that hold a value for statistical evaluation
//	public static class OtherValue {
//		public String someValue;
//		public OtherValue(String value) {
//
//			someValue = value;
//		}



//	}

//	@Test
//	// This test shows the flexibility in using lambdas
//	public void test_percentileValue_NonWLSample() throws Exception {
//
//		OtherValue min = new OtherValue("1.0");
//		OtherValue max = new OtherValue("2.0");
//		List<OtherValue> samples = new LinkedList<>();
//		samples.add(min);
//		samples.add(max);
//		// the valueOf lambda can be any type of mapping not just a getter
//		BigDecimal pct50 = valueOfPercentile(samples, new BigDecimal(".5"), (other)->new BigDecimal(other.someValue));
//		assertEquals("Expect 50% percetile to be the middle value", "1.5", pct50.toString() );
//	}
//
//	@Test
//	public void test_percentileValue_3_50pct() throws Exception {
//		WLSample min = createSample("2005-06-10T04:15:00-05:00", "1.0");
//		WLSample mid = createSample("2010-06-10T04:15:00-05:00", "1.5");
//		WLSample max = createSample("2015-06-10T04:15:00-05:00", "2.0");
//		List<WLSample> samples = new OrderedSamples();
//		samples.add(mid);
//		samples.add(min);
//		samples.add(max);
//		BigDecimal pct50 = valueOfPercentile(samples, new BigDecimal(".5"), WLSample::valueOf);
//		assertEquals("Expect 50% percetile to be the middle value", mid.value.toString(), pct50.toString());
//	}
//	@Test
//	public void test_percentileValue_3_25pct_0pct() throws Exception {
//		WLSample min = createSample("2005-06-10T04:15:00-05:00", "1.0");
//		WLSample mid = createSample("2010-06-10T04:15:00-05:00", "1.5");
//		WLSample max = createSample("2015-06-10T04:15:00-05:00", "2.0");
//		List<WLSample> samples = new OrderedSamples();
//		samples.add(mid);
//		samples.add(min);
//		samples.add(max);
//		BigDecimal pct0 = valueOfPercentile(samples, new BigDecimal("0"), WLSample::valueOf);
//		assertEquals("Expect 100% percetile to be the min value", min.value.toString(), pct0.toString() );
//		// just like 0%, 25% is 1/(N+1) = 1/(3+1)
//		BigDecimal pct25 = valueOfPercentile(samples, new BigDecimal(".25"), WLSample::valueOf);
//		assertEquals("Expect 25% percetile to be the min value", min.value.toString(), pct25.toString() );
//	}
//	@Test
//	public void test_percentileValue_3_100pct_75pct() throws Exception {
//		WLSample min = createSample("2005-06-10T04:15:00-05:00", "1.0");
//		WLSample mid = createSample("2010-06-10T04:15:00-05:00", "1.5");
//		WLSample max = createSample("2015-06-10T04:15:00-05:00", "2.0");
//		List<WLSample> samples = new OrderedSamples();
//		samples.add(mid);
//		samples.add(min);
//		samples.add(max);
//		BigDecimal pct100 = valueOfPercentile(samples, new BigDecimal("1"), WLSample::valueOf);
//		assertEquals("Expect 100% percetile to be the max value", max.value.toString(), pct100.toString() );
//		// just like 100%, 75% is N/(N+1) = 3/(3+1)
//		BigDecimal pct75 = valueOfPercentile(samples, new BigDecimal(".75"), WLSample::valueOf);
//		assertEquals("Expect 75% percetile to be the max value", max.value.toString(), pct75.toString() );
//	}
//	@Test
//	public void test_percentileValue_3_74pct_26pct_SigFig() throws Exception {
//		WLSample min = createSample("2005-06-10T04:15:00-05:00", "1.00");
//		WLSample mid = createSample("2010-06-10T04:15:00-05:00", "1.50");
//		WLSample max = createSample("2015-06-10T04:15:00-05:00", "2.00");
//		List<WLSample> samples = new OrderedSamples();
//		samples.add(mid);
//		samples.add(min);
//		samples.add(max);
//		BigDecimal pct26 = valueOfPercentile(samples, new BigDecimal(".26"), WLSample::valueOf);
//		// .26(3+1) = 1.04, index 1-1 is 0 for value 1.0, .04 (1.5-1) = .02, 1+.02 = 1.02
//		assertEquals("Expect 74% percetile to be just less than max", "1.02", pct26.toString() );
//
//		BigDecimal pct74 = valueOfPercentile(samples, new BigDecimal(".74"), WLSample::valueOf);
//		// .74(3+1) = 2.96, index 2-1 is 1 for value 1.5, .96 (2-1.5) = .48, 1.5+.48 = 1.98
//		assertEquals("Expect 74% percetile to be just less than max", "1.98", pct74.toString() );
//	}
//
//
//	@Test
//	public void test_percentileOfValue_12_75pct_90pct_SigFig_NIST() throws Exception {
//		List<WLSample> samples = new OrderedSamples();
//		samples.add( createSample("2015-01-10T04:15:00-05:00", "95.1772") );
//		samples.add( createSample("2015-02-10T04:15:00-05:00", "95.1567") );
//		samples.add( createSample("2015-03-10T04:15:00-05:00", "95.1937") );
//		samples.add( createSample("2015-04-10T04:15:00-05:00", "95.1959") );
//		samples.add( createSample("2015-05-10T04:15:00-05:00", "95.1442") );
//		samples.add( createSample("2015-06-10T04:15:00-05:00", "95.0610") );
//		samples.add( createSample("2015-07-10T04:15:00-05:00", "95.1591") );
//		samples.add( createSample("2015-08-10T04:15:00-05:00", "95.1195") );
//		samples.add( createSample("2015-09-10T04:15:00-05:00", "95.1065") );
//		samples.add( createSample("2015-10-10T04:15:00-05:00", "95.0925") );
//		samples.add( createSample("2015-11-10T04:15:00-05:00", "95.1990") );
//		samples.add( createSample("2015-12-10T04:15:00-05:00", "95.1682") );
//
//		// need to include the extra .750 in order to not loose precision on the data - this is an exact percent
//
//		BigDecimal val23 = valueOfPercentile(samples, new BigDecimal(".307692"), WLSample::valueOf);
//		assertEquals("Expect "+samples.get(3).value+" to be ", samples.get(3).value.toString(), val23.toString() );
//
//		BigDecimal pct23 = percentileOfValue(samples, samples.get(3), WLSample::valueOf);
//		assertEquals("Expect "+samples.get(3).value+" to be 23% percetile", "0.307692", pct23.toString() );
//
//
//	}
//
//
//	// NIST example http://www.itl.nist.gov/div898/handbook/prc/section2/prc262.htm
//	@Test
//	public void test_percentileValue_12_75pct_90pct_SigFig_NIST() throws Exception {
//		List<WLSample> samples = new OrderedSamples();
//		samples.add( createSample("2015-01-10T04:15:00-05:00", "95.1772") );
//		samples.add( createSample("2015-02-10T04:15:00-05:00", "95.1567") );
//		samples.add( createSample("2015-03-10T04:15:00-05:00", "95.1937") );
//		samples.add( createSample("2015-04-10T04:15:00-05:00", "95.1959") );
//		samples.add( createSample("2015-05-10T04:15:00-05:00", "95.1442") );
//		samples.add( createSample("2015-06-10T04:15:00-05:00", "95.0610") );
//		samples.add( createSample("2015-07-10T04:15:00-05:00", "95.1591") );
//		samples.add( createSample("2015-08-10T04:15:00-05:00", "95.1195") );
//		samples.add( createSample("2015-09-10T04:15:00-05:00", "95.1065") );
//		samples.add( createSample("2015-10-10T04:15:00-05:00", "95.0925") );
//		samples.add( createSample("2015-11-10T04:15:00-05:00", "95.1990") );
//		samples.add( createSample("2015-12-10T04:15:00-05:00", "95.1682") );
//
//		// need to include the extra .750 in order to not loose precision on the data - this is an exact percent
//		BigDecimal pct750 = valueOfPercentile(samples, new BigDecimal(".750"), WLSample::valueOf);
//		assertEquals("Expect 75% percetile to be 95.1896", "95.1896", pct750.toString() );
//		BigDecimal pct75 = valueOfPercentile(samples, new BigDecimal(".75"), WLSample::valueOf);
//		assertEquals("Expect 75% percetile to be 95.189",  "95.189", pct75.toString() );
//
//		BigDecimal pct90 = valueOfPercentile(samples, new BigDecimal(".90"), WLSample::valueOf);
//		assertEquals("Expect 90% percetile to be 95.1981", "95.1981", pct90.toString() );
//
//	}
//
//
//	@Test
//	public void test_collectionsSort() throws Exception {
//		List<WLSample> samples = new LinkedList<>();
//		samples.add( createSample("2015-01-10T04:15:00-05:00", "95.1772") );
//		samples.add( createSample("2015-02-10T04:15:00-05:00", "95.1567") );
//		samples.add( createSample("2015-03-10T04:15:00-05:00", "95.1937") );
//		samples.add( createSample("2015-04-10T04:15:00-05:00", "95.1959") );
//		samples.add( createSample("2015-05-10T04:15:00-05:00", "95.1442") );
//		samples.add( createSample("2015-06-10T04:15:00-05:00", "95.0610") );
//		samples.add( createSample("2015-07-10T04:15:00-05:00", "95.1591") );
//		samples.add( createSample("2015-08-10T04:15:00-05:00", "95.1195") );
//		samples.add( createSample("2015-09-10T04:15:00-05:00", "95.1065") );
//		samples.add( createSample("2015-10-10T04:15:00-05:00", "95.0925") );
//		samples.add( createSample("2015-11-10T04:15:00-05:00", "95.1990") );
//		samples.add( createSample("2015-12-10T04:15:00-05:00", "95.1682") );
//
//		List<WLSample> sorted = new LinkedList<>(samples);
//		stats.sortByValueOrderAscending(sorted);
//
//		assertEquals("Expect sorted to be the same size", samples.size(), sorted.size() );
//		assertEquals("Expect first to be smallest", samples.get(5), sorted.get(0) );
//		assertEquals("Expect last to be largest", samples.get(10), sorted.get(11) );
//	}
//
//	public String randomNumber(int length) {
//		String digits = "0123456789";
//
//		StringBuilder number = new StringBuilder();
//		for (int d=0; d<length; d++) {
//			number.append(  digits.charAt( (int)(Math.random() * digits.length()) )  );
//		}
//
//		return number.toString();
//	}
//	@Ignore // most of the time this is true but sometimes the arrangement favors the simple sort
//	// the quick sort is faster is with larger data sets and only microseconds slower than simple sort on tiny data sets
//	public void test_collectionsSort_vs_simple_3000() throws Exception {
//
//		long start = System.currentTimeMillis();
//		List<WLSample> samples = new LinkedList<>();
//		for (int i=0; i<3000; i++) {
//			samples.add( createSample("", randomNumber(10) ) );
//		}
//		long genDur = System.currentTimeMillis() - start;
//		System.out.println("gen done " + genDur);
//
//		start = System.currentTimeMillis();
//		List<WLSample> sorted = new LinkedList<>(samples);
//		stats.sortByValueOrderAscending(sorted);
//		long quick = System.currentTimeMillis() - start;
//		System.out.println("quick done " + quick);
//
//		start = System.currentTimeMillis();
//		List<WLSample> simplesort = new OrderedSamples();
//		for(WLSample sample : samples) {
//			simplesort.add(sample);
//		}
//		long simple = System.currentTimeMillis() - start;
//		System.out.println("simple done " + simple);
//
//		assertEquals("Expect quicksorted to be the same size", samples.size(), sorted.size() );
//		assertEquals("Expect simplesorted to be the same size", samples.size(), simplesort.size() );
//		assertTrue("Expect quicksorted to be the faster most of the time (there is a performace variation based on the random data)", quick <= simple);
//	}
//	@Ignore // most of the time this is true but sometimes the arrangement favors the simple sort
//	// the only time the simple sort is faster is with small data sets where we are talking about microseconds faster
//	public void test_collectionsSort_vs_simple_500() throws Exception {
//
//		long start = System.currentTimeMillis();
//		List<WLSample> samples = new LinkedList<>();
//		for (int i=0; i<100; i++) {
//			samples.add( createSample("", randomNumber(10) ) );
//		}
//		long genDur = System.currentTimeMillis() - start;
//		System.out.println("gen done " + genDur);
//
//		start = System.currentTimeMillis();
//		List<WLSample> sorted = new LinkedList<>(samples);
//		stats.sortByValueOrderAscending(sorted);
//		long quick = System.currentTimeMillis() - start;
//		System.out.println("quick  done " + quick);
//
//		start = System.currentTimeMillis();
//		List<WLSample> simplesort = new OrderedSamples();
//		for(WLSample sample : samples) {
//			simplesort.add(sample);
//		}
//		long simple = System.currentTimeMillis() - start;
//		System.out.println("simple done " + simple);
//
//		assertEquals("Expect quicksorted to be the same size", samples.size(), sorted.size() );
//		assertEquals("Expect simplesorted to be the same size", samples.size(), simplesort.size() );
//		assertTrue("Expect quicksorted to be the slower most of the time (there is a performace variation based on the random data)", (quick >= simple));
//		System.out.println("quick   " + quick);
//		System.out.println("simple  " + simple);
//	}
//
//
//	@Test
//	public void test_generatePercentiles_ascendingForAboveDatum() throws Exception {
//		List<WLSample> samples = new LinkedList<>();
//		samples.add( createSample("2015-01-10T04:15:00-05:00", "95.1772") );
//		samples.add( createSample("2015-02-10T04:15:00-05:00", "95.1567") );
//		samples.add( createSample("2015-03-10T04:15:00-05:00", "95.1937") );
//		samples.add( createSample("2015-04-10T04:15:00-05:00", "95.1959") );
//		samples.add( createSample("2015-05-10T04:15:00-05:00", "95.1442") );
//		samples.add( createSample("2015-06-10T04:15:00-05:00", "95.0610") );
//		samples.add( createSample("2015-07-10T04:15:00-05:00", "95.1591") );
//		samples.add( createSample("2015-08-10T04:15:00-05:00", "95.1195") );
//		samples.add( createSample("2015-09-10T04:15:00-05:00", "95.1065") );
//		samples.add( createSample("2015-10-10T04:15:00-05:00", "95.0925") );
//		samples.add( createSample("2015-11-10T04:15:00-05:00", "95.1990") );
//		samples.add( createSample("2015-12-10T04:15:00-05:00", "95.1682") );
//		List<WLSample> sorted = new LinkedList<>(samples);
//		stats.sortByValueOrderAscending(sorted);
//		Map<String,String> percentiles = stats.generatePercentiles(sorted, WaterLevelStatistics.PERCENTILES);
//		// as you might expect the values are low for low percentile and high for high percentile because measured above a datum
//		assertEquals("Expect P10 to be ", "95.0705", percentiles.get(P10));
//		assertEquals("Expect P25 to be ", "95.1098", percentiles.get(P25));
//		assertEquals("Expect P50 to be ", "95.1579", percentiles.get(P50));
//		assertEquals("Expect P75 to be ", "95.1896", percentiles.get(P75));
//		assertEquals("Expect P90 to be ", "95.1981", percentiles.get(P90));
//	}
//	@Test
//	public void test_generatePercentiles_descendingForBelowSurface() throws Exception {
//		List<WLSample> samples = new LinkedList<>();
//		samples.add( createSample("2015-01-10T04:15:00-05:00", "95.1772") );
//		samples.add( createSample("2015-02-10T04:15:00-05:00", "95.1567") );
//		samples.add( createSample("2015-03-10T04:15:00-05:00", "95.1937") );
//		samples.add( createSample("2015-04-10T04:15:00-05:00", "95.1959") );
//		samples.add( createSample("2015-05-10T04:15:00-05:00", "95.1442") );
//		samples.add( createSample("2015-06-10T04:15:00-05:00", "95.0610") );
//		samples.add( createSample("2015-07-10T04:15:00-05:00", "95.1591") );
//		samples.add( createSample("2015-08-10T04:15:00-05:00", "95.1195") );
//		samples.add( createSample("2015-09-10T04:15:00-05:00", "95.1065") );
//		samples.add( createSample("2015-10-10T04:15:00-05:00", "95.0925") );
//		samples.add( createSample("2015-11-10T04:15:00-05:00", "95.1990") );
//		samples.add( createSample("2015-12-10T04:15:00-05:00", "95.1682") );
//		List<WLSample> sorted = new LinkedList<>(samples);
//		stats.sortByValueOrderDescending(sorted); // <--------  sorting DESCENDING instead of Ascending
//		Map<String,String> percentiles = stats.generatePercentiles(sorted, WaterLevelStatistics.PERCENTILES);
//		// NOTICE: the values are low for high percentile and high for low percentile because measured below surface
//		assertEquals("Expect P10 to be ", "95.1981", percentiles.get(P10));
//		assertEquals("Expect P25 to be ", "95.1896", percentiles.get(P25));
//		assertEquals("Expect P50 to be ", "95.1579", percentiles.get(P50));
//		assertEquals("Expect P75 to be ", "95.1098", percentiles.get(P75));
//		// this one is not exactly the opposite of 10% because of rounding rules and data order - but it is close
//		assertEquals("Expect P90 to be ", "95.0704", percentiles.get(P90));
//	}

	@Test
	public void test_generateMonthYearlyPercentiles() throws Exception {
		List<WLSample> samples = new LinkedList<>();
		samples.add( createSample("2015-01-10T04:15:00-05:00", "95.1772") );
		samples.add( createSample("2015-01-10T04:15:00-05:00", "95.1567") );
		samples.add( createSample("2015-01-10T04:15:00-05:00", "95.1937") );
		samples.add( createSample("2015-01-10T04:15:00-05:00", "95.1959") );
		samples.add( createSample("2015-01-10T04:15:00-05:00", "95.1442") );
		samples.add( createSample("2015-01-10T04:15:00-05:00", "95.0610") );
		samples.add( createSample("2015-01-10T04:15:00-05:00", "95.1591") );
		samples.add( createSample("2015-01-10T04:15:00-05:00", "95.1195") );
		samples.add( createSample("2015-01-10T04:15:00-05:00", "95.1065") );
		samples.add( createSample("2015-01-10T04:15:00-05:00", "95.0925") );
		samples.add( createSample("2015-01-10T04:15:00-05:00", "95.1990") );
		samples.add( createSample("2015-01-10T04:15:00-05:00", "95.1682") );
		samples.add( createSample("2014-01-10T04:15:00-05:00", "94.1772") );
		samples.add( createSample("2014-01-10T04:15:00-05:00", "94.1567") );
		samples.add( createSample("2014-01-10T04:15:00-05:00", "94.1937") );
		samples.add( createSample("2014-01-10T04:15:00-05:00", "94.1959") );
		samples.add( createSample("2014-01-10T04:15:00-05:00", "94.1442") );
		samples.add( createSample("2014-01-10T04:15:00-05:00", "94.0610") );
		samples.add( createSample("2014-01-10T04:15:00-05:00", "94.1591") );
		samples.add( createSample("2014-01-10T04:15:00-05:00", "94.1195") );
		samples.add( createSample("2014-01-10T04:15:00-05:00", "94.1065") );
		samples.add( createSample("2014-01-10T04:15:00-05:00", "94.0925") );
		samples.add( createSample("2014-01-10T04:15:00-05:00", "94.1990") );
		samples.add( createSample("2014-01-10T04:15:00-05:00", "94.1682") );
		samples.add( createSample("2013-01-10T04:15:00-05:00", "93.1772") );
		samples.add( createSample("2013-01-10T04:15:00-05:00", "93.1567") );
		samples.add( createSample("2013-01-10T04:15:00-05:00", "93.1937") );
		samples.add( createSample("2013-01-10T04:15:00-05:00", "93.1959") );
		samples.add( createSample("2013-01-10T04:15:00-05:00", "93.1442") );
		samples.add( createSample("2013-01-10T04:15:00-05:00", "93.0610") );
		samples.add( createSample("2013-01-10T04:15:00-05:00", "93.1591") );
		samples.add( createSample("2013-01-10T04:15:00-05:00", "93.1195") );
		samples.add( createSample("2013-01-10T04:15:00-05:00", "93.1065") );
		samples.add( createSample("2013-01-10T04:15:00-05:00", "93.0925") );
		samples.add( createSample("2013-01-10T04:15:00-05:00", "93.1990") );
		samples.add( createSample("2013-01-10T04:15:00-05:00", "93.1682") );
		List<WLSample> sorted = new LinkedList<>(samples);
		sortByValueOrderAscending(sorted);
		List<WLSample> yearly = stats.generateMonthYearlyPercentiles(sorted, MediationType.AboveDatum);

		assertEquals("Expect 3 medians", 3, yearly.size());
		assertEquals("Expect large median to be",  "95.1579", yearly.get(2).value.toString());
		assertEquals("Expect middle median to be", "94.1579", yearly.get(1).value.toString());
		assertEquals("Expect least median to be",  "93.1579", yearly.get(0).value.toString());
	}

	@Test
	public void test_monthlyStats_yearly_sameMonth() throws Exception {
		List<WLSample> samples = new LinkedList<>();
		samples.add( createSample("2015-01-10T04:15:00-05:00", "95.1772") );
		samples.add( createSample("2015-01-10T04:15:00-05:00", "95.1567") );
		samples.add( createSample("2015-01-10T04:15:00-05:00", "95.1937") );
		samples.add( createSample("2015-01-10T04:15:00-05:00", "95.1959") );
		samples.add( createSample("2015-01-10T04:15:00-05:00", "95.1442") );
		samples.add( createSample("2015-01-10T04:15:00-05:00", "95.0610") );
		samples.add( createSample("2015-01-10T04:15:00-05:00", "95.1591") );
		samples.add( createSample("2015-01-10T04:15:00-05:00", "95.1195") );
		samples.add( createSample("2015-01-10T04:15:00-05:00", "95.1065") );
		samples.add( createSample("2015-01-10T04:15:00-05:00", "95.0925") );
		samples.add( createSample("2015-01-10T04:15:00-05:00", "95.1990") );
		samples.add( createSample("2015-01-10T04:15:00-05:00", "95.1682") );
		samples.add( createSample("2014-01-10T04:15:00-05:00", "94.1772") );
		samples.add( createSample("2014-01-10T04:15:00-05:00", "94.1567") );
		samples.add( createSample("2014-01-10T04:15:00-05:00", "94.1937") );
		samples.add( createSample("2014-01-10T04:15:00-05:00", "94.1959") );
		samples.add( createSample("2014-01-10T04:15:00-05:00", "94.1442") );
		samples.add( createSample("2014-01-10T04:15:00-05:00", "94.0610") );
		samples.add( createSample("2014-01-10T04:15:00-05:00", "94.1591") );
		samples.add( createSample("2014-01-10T04:15:00-05:00", "94.1195") );
		samples.add( createSample("2014-01-10T04:15:00-05:00", "94.1065") );
		samples.add( createSample("2014-01-10T04:15:00-05:00", "94.0925") );
		samples.add( createSample("2014-01-10T04:15:00-05:00", "94.1990") );
		samples.add( createSample("2014-01-10T04:15:00-05:00", "94.1682") );
		samples.add( createSample("2005-01-10T04:15:00-05:00", "93.1772") );
		samples.add( createSample("2005-01-10T04:15:00-05:00", "93.1567") );
		samples.add( createSample("2005-01-10T04:15:00-05:00", "93.1937") );
		samples.add( createSample("2005-01-10T04:15:00-05:00", "93.1959") );
		samples.add( createSample("2005-01-10T04:15:00-05:00", "93.1442") );
		samples.add( createSample("2005-01-10T04:15:00-05:00", "93.0610") );
		samples.add( createSample("2005-01-10T04:15:00-05:00", "93.1591") );
		samples.add( createSample("2005-01-10T04:15:00-05:00", "93.1195") );
		samples.add( createSample("2005-01-10T04:15:00-05:00", "93.1065") );
		samples.add( createSample("2005-01-10T04:15:00-05:00", "93.0925") );
		samples.add( createSample("2005-01-10T04:15:00-05:00", "93.1990") );
		samples.add( createSample("2005-01-10T04:15:00-05:00", "93.1682") );
		List<WLSample> sorted = new LinkedList<>(samples);
		sortByValueOrderAscending(sorted);

		// we are not testing this method so mock it to return what we need
		WaterLevelStatistics mockstats = new WaterLevelStatistics() {
			protected boolean doesThisMonthQualifyForStats(java.util.List<WLSample> monthSamples) {
				return monthSamples.size()>0;
			}
			public List<WLSample> normalizeMutlipleYearlyValues(List<WLSample> monthSamples, MediationType mediation) {
				// do not modify, testing the months
				return monthSamples;
			}
		};

		Map<String, Map<String, String>> monthly = mockstats.monthlyStats(sorted, MediationType.AboveDatum);

		assertEquals("Expect 1 month median", 1, monthly.size());
		assertEquals("Expect max median to be", "95.1579", monthly.get("1").get(P50_MAX));
		assertEquals("Expect mid median to be", "94.1579", monthly.get("1").get(P50));
		assertEquals("Expect min median to be", "93.1579", monthly.get("1").get(P50_MIN));
		assertEquals("Expect sample count to be ", ""+samples.size(), monthly.get("1").get(SAMPLE_COUNT));

		assertEquals("Expect record years to be 3 and not 10 because of unique year count.", "3", monthly.get("1").get(RECORD_YEARS));
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

		// we are not testing this method so mock it to return what we need
		WaterLevelStatistics mockstats = new WaterLevelStatistics() {
			public List<WLSample> normalizeMutlipleYearlyValues(List<WLSample> monthSamples, MediationType mediation) {
				// do not modify, testing the months
				return monthSamples;
			}
		};

		sortByValueOrderAscending(sorted);
		Map<String, Map<String, String>> monthly = mockstats.monthlyStats(sorted, MediationType.AboveDatum);

		assertEquals("Expect 3 only monthly stats - the other three do not have ten yrs data", 3, monthly.size());
		assertEquals("Expect May median to be", "95.1579", monthly.get("5").get(P50));
		assertEquals("Expect Apr median to be", "94.1579", monthly.get("4").get(P50));
		assertEquals("Expect Mar median to be", "93.1579", monthly.get("3").get(P50));

		assertEquals("Expect sample count to be ", "12", monthly.get("5").get(SAMPLE_COUNT));
		assertEquals("Expect sample count to be ", "12", monthly.get("4").get(SAMPLE_COUNT));
		assertEquals("Expect sample count to be ", "12", monthly.get("3").get(SAMPLE_COUNT));

		assertEquals("Expect record years to be 11 because there are 11 unique years in the given data even though there are 12 data entries and not a difference of 10 because we count the months.", "11", monthly.get("5").get(RECORD_YEARS));
		assertEquals("Expect record years to be 11 because there are 11 unique years in the given data even though there are 12 data entries and not a difference of 10 because we count the months.", "11", monthly.get("4").get(RECORD_YEARS));
		assertEquals("Expect record years to be 11 because there are 11 unique years in the given data even though there are 12 data entries and not a difference of 10 because we count the months.", "11", monthly.get("3").get(RECORD_YEARS));
	}

//	@Test
//	public void testFilterValuesByGivenMonth() {
//		List<WLSample> samples = new LinkedList<>();
//		samples.add( createSample("2015-11-10T04:15:00-05:00", "95.1772") );
//		samples.add( createSample("2015-02-11T04:15:00-05:00", "95.1567") );
//		samples.add( createSample("2015-02-12T04:15:00-05:00", "95.1937") );
//		samples.add( createSample("2015-03-13T04:15:00-05:00", "95.1959") );
//		samples.add( createSample("2015-03-14T04:15:00-05:00", "95.1442") );
//		samples.add( createSample("2015-03-15T04:15:00-05:00", "95.0610") );
//		samples.add( createSample("2015-04-16T04:15:00-05:00", "95.1591") );
//		samples.add( createSample("2015-04-17T04:15:00-05:00", "95.1195") );
//		samples.add( createSample("2015-04-18T04:15:00-05:00", "95.1065") );
//		samples.add( createSample("2015-04-19T04:15:00-05:00", "95.0925") );
//		samples.add( createSample("2015-05-20T04:15:00-05:00", "95.1990") );
//		samples.add( createSample("2005-05-21T04:15:00-05:00", "95.1682") );
//		samples.add( createSample("2014-05-10T04:15:00-05:00", "94.1772") );
//		samples.add( createSample("2014-05-11T04:15:00-05:00", "94.1567") );
//		samples.add( createSample("2014-05-12T04:15:00-05:00", "94.1937") );
//		samples.add( createSample("2014-06-13T04:15:00-05:00", "94.1959") );
//		samples.add( createSample("2014-06-14T04:15:00-05:00", "94.1442") );
//		samples.add( createSample("2014-06-15T04:15:00-05:00", "94.0610") );
//		samples.add( createSample("2014-06-16T04:15:00-05:00", "94.1591") );
//		samples.add( createSample("2014-06-17T04:15:00-05:00", "94.1195") );
//		samples.add( createSample("2014-06-17T04:15:00-05:00", "94.1065") );
//		assertEquals(1, stats.filterValuesByGivenMonth(samples, "11").size());
//		assertEquals(2, stats.filterValuesByGivenMonth(samples, "2").size());
//		assertEquals(3, stats.filterValuesByGivenMonth(samples, "3").size());
//		assertEquals(4, stats.filterValuesByGivenMonth(samples, "4").size());
//		assertEquals(5, stats.filterValuesByGivenMonth(samples, "5").size());
//		assertEquals(6, stats.filterValuesByGivenMonth(samples, "06").size());
//	}

	@Test
	public void test_generateLatestPercentileBasedOnMonthlyData() throws Exception {
		List<WLSample> samples = new LinkedList<>();
		samples.add( createSample("2015-05-10T04:15:00-05:00", "95.1772") );
		samples.add( createSample("2015-05-11T04:15:00-05:00", "95.1567") );
		samples.add( createSample("2015-05-12T04:15:00-05:00", "95.1937") );
		samples.add( createSample("2015-05-13T04:15:00-05:00", "95.1959") );
		samples.add( createSample("2015-05-14T04:15:00-05:00", "95.1442") );
		samples.add( createSample("2015-05-15T04:15:00-05:00", "95.0610") );
		samples.add( createSample("2015-05-16T04:15:00-05:00", "95.1591") );
		samples.add( createSample("2015-05-17T04:15:00-05:00", "95.1195") );
		samples.add( createSample("2015-05-18T04:15:00-05:00", "95.1065") );
		samples.add( createSample("2015-05-19T04:15:00-05:00", "95.0925") );
		samples.add( createSample("2015-05-20T04:15:00-05:00", "95.1990") );
		samples.add( createSample("2005-05-21T04:15:00-05:00", "95.1682") );
		samples.add( createSample("2014-04-10T04:15:00-05:00", "94.1772") );
		samples.add( createSample("2014-04-11T04:15:00-05:00", "94.1567") );
		samples.add( createSample("2014-04-12T04:15:00-05:00", "94.1937") );
		samples.add( createSample("2014-04-13T04:15:00-05:00", "94.1959") );
		samples.add( createSample("2014-04-14T04:15:00-05:00", "94.1442") );
		samples.add( createSample("2014-04-15T04:15:00-05:00", "94.0610") );
		samples.add( createSample("2014-04-16T04:15:00-05:00", "94.1591") );
		samples.add( createSample("2014-04-17T04:15:00-05:00", "94.1195") );
		samples.add( createSample("2014-04-18T04:15:00-05:00", "94.1065") );
		samples.add( createSample("2014-04-19T04:15:00-05:00", "94.0925") );
		samples.add( createSample("2014-04-20T04:15:00-05:00", "94.1990") );
		samples.add( createSample("2004-04-21T04:15:00-05:00", "94.1682") );
		samples.add( createSample("2013-03-10T04:15:00-05:00", "93.1772") );
		samples.add( createSample("2013-03-11T04:15:00-05:00", "93.1567") );
		samples.add( createSample("2013-03-12T04:15:00-05:00", "93.1937") );
		samples.add( createSample("2013-03-13T04:15:00-05:00", "93.1959") );
		samples.add( createSample("2013-03-14T04:15:00-05:00", "93.1442") );
		samples.add( createSample("2013-03-15T04:15:00-05:00", "93.0610") );
		samples.add( createSample("2013-03-16T04:15:00-05:00", "93.1591") );
		samples.add( createSample("2013-03-17T04:15:00-05:00", "93.1195") );
		samples.add( createSample("2013-03-18T04:15:00-05:00", "93.1065") );
		samples.add( createSample("2013-03-19T04:15:00-05:00", "93.0925") );
		samples.add( createSample("2013-03-20T04:15:00-05:00", "93.1990") );
		samples.add( createSample("2003-03-21T04:15:00-05:00", "93.1682") );

		sortByDateOrder(samples);
		String date = "2015-04-10";
		String latestPct = stats.generateLatestPercentileBasedOnMonthlyData(samples, date, MediationType.AboveDatum);
		assertTrue("The latest percetile for the date given should be based on the month it is in "
				+ "and all data for the given month is 94.???? so the latest in that month should be 100% (percentile) "
				+ latestPct, latestPct.startsWith("1"));
	}



//	@Test
//	public void test_latestPercentileBondaryContitions_LatestPercentile() throws Exception {
//		// force above datum because we are testing rounding and sigfigs
//		WaterLevelStatistics stats = new WaterLevelStatistics(){
//			@Override
//			protected MediationType findMostPrevalentMediation(Specifier spec, java.util.List<WLSample> samples) {
//				return MediationType.AboveDatum;
//			};
//			@Override
//			protected boolean doesThisSiteQualifyForMonthlyStats(BigDecimal years, String recent, String today) {
//				return true;
//			}
//		};
//
//		// the latest sample is the number 2 index entry and should NOT be zero
//		assertExpectedValues(stats, "MBMG",  "122340", "0.02409639", "Y", "17.960000");
//		// the latest sample is the second last index entry and should NOT be 100
//		assertExpectedValues(stats, "MBMG",  "3002",   "0.750000000", "Y", "200.390000");
//		// the latest sample is the largest value and should be 100%
//		assertExpectedValues(stats, "MBMG",  "73642",  "1", "Y", "146.600000");
//		// the latest sample is the least value and should be 0%
//		assertExpectedValues(stats, "MN_DNR","200105", "0", "Y", "98.29");
//		// the latest sample should be ~31% for the month rather than ~20% if compared to the entire data set
////		assertExpectedValues(stats, "USGS","430427089284901", "0.3158", "Y", "49.80");
//	}
//	// this is a helper method to test many different sites
//	protected void assertExpectedValues(WaterLevelStatistics stats, String agencyCd, String siteNo,
//			String expectedValue, String isRanked, String latestValue) throws SQLException {
//		Specifier spec = new Specifier(agencyCd,siteNo,WellDataType.WATERLEVEL);
//		String json = stats.calculate(spec);
//
//		Map<String,String> overall = extractOverall(json);
//		assertNotNull("overall should not be null", overall);
//		assertEquals("Expect IS_RANKED to be ", isRanked, overall.get(IS_RANKED));
//		assertEquals("Expect LATEST_VALUE to be ", latestValue, overall.get(LATEST_VALUE));
//		assertEquals("Expect latest percentile ", expectedValue, overall.get(LATEST_PCTILE) );
//
//		Map<String,Map<String,String>> monthly = extractMonthly(json);
//		assertNotNull("monthly should not be null", monthly);
//	}
//	// this is a helper method to load the data file for each test site
//	private List<WLSample> loadTestData(Specifier spec) {
//
//		String agencyCd = spec.getAgencyCd();
//		String siteNo   = spec.getSiteNo();
//
//		String filename = "/sample-data/stats_tests_"+agencyCd+"_"+siteNo+"_WATERLEVEL.xml";
//
//		InputStream in  = getClass().getResourceAsStream(filename);
//		assertNotNull("Expect to find resource file on classpath: " + filename,in);
//		Reader reader   = new InputStreamReader(in);
//		Elevation altVal= new Elevation(2320.0,"NGVD29");
//
//		try {
//			return WLSample.extractSamples(reader, agencyCd, siteNo, altVal);
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw new RuntimeException(e);
//		}
//	}
//
//
//	// extracted a method to put a descriptive name
//	private Answer<List<WLSample>> loadTestData() {
//		return new Answer<List<WLSample>>() {
//			public List<WLSample> answer(InvocationOnMock invocation) {
//
//				Specifier spec  = (Specifier) invocation.getArguments()[0];
//
//				String agencyCd = spec.getAgencyCd();
//				String siteNo   = spec.getSiteNo();
//				Elevation altVal= new Elevation(2320.0,"NGVD29");
//
//				String filename = "/sample-data/stats_tests_"+agencyCd+"_"+siteNo+"_WATERLEVEL.xml";
//
//				InputStream in  = getClass().getResourceAsStream(filename);
//				Reader reader   = new InputStreamReader(in);
//
//				try {
//					return WaterLevelDAO.extractSamples(reader, agencyCd, siteNo, altVal);
//				} catch (Exception e) {
//					e.printStackTrace();
//					throw new RuntimeException(e);
//				}
//			}
//		};
//	}
//
//
//	@Test
//	@SuppressWarnings("unchecked")
//	public void test_minMaxFlipBelowSurfaceMediation() throws Exception {
//		WaterLevelDAO dataDAO       = mock(WaterLevelDAO.class);
//		stats.setWaterlevelDAO(dataDAO);
//		when(dataDAO.getTimeSeries(any(Specifier.class))).thenAnswer( loadTestData() );
//		when(dataDAO.getTimeSeries(any(String.class),any(String.class))).thenAnswer( loadTestData() );
//
//		final Object[] values       = new Object[2];
//		WaterLevelStatsDAO statsDAO = mock(WaterLevelStatsDAO.class);
//		doAnswer(new Answer<Void>() {
//			public Void answer(InvocationOnMock invocation) {
//				values[0] = invocation.getArguments()[1];
//				values[1] = invocation.getArguments()[2];
//				return null;
//			}
//		}).when(statsDAO).update(any(Specifier.class), any(Map.class), any(Map.class));
//		stats.setWaterlevelstatsDAO(statsDAO);
//
//		// these are below land surface (BLS)
//		assertExpectedMinMaxValues(values, stats, "MBMG",  "122340",  "19.760000", "10.380000");
//		assertExpectedMinMaxValues(values, stats, "MBMG",  "3002",  "209.790000",   "180.620000");
//		assertExpectedMinMaxValues(values, stats, "MBMG",  "73642",  "150.790000",  "135.760000");
//		assertExpectedMinMaxValues(values, stats, "MN_DNR","200105",  "115.97", "98.29");
//		// even many USGS sites are BLS
//		assertExpectedMinMaxValues(values, stats, "USGS","430427089284901",  "58.82", "45.15");
//
//		// however some USGS are above a datum
//		assertExpectedMinMaxValues(values, stats, "USGS","405010073414901",  "-16.15", "2310.7");
//	}
//	@SuppressWarnings("unchecked")
//	protected void assertExpectedMinMaxValues(Object[] values, WaterLevelStatistics stats,
//			String agencyCd, String siteNo, String minValue, String maxValue) throws SQLException {
//		Specifier spec = new Specifier(agencyCd,siteNo,WellDataType.WATERLEVEL);
//		stats.calculate(spec);
//
//		Map<String,String> overall = (Map<String,String>) values[0];
//
//		assertNotNull("overall should not be null", overall);
//
//		assertEquals("Expect MIN_VALUE to be ", minValue, overall.get(MIN_VALUE));
//		assertEquals("Expect MAX_VALUE to be ", maxValue, overall.get(MAX_VALUE));
//	}


	// this is a helper method to convert JSON to Maps
	@SuppressWarnings("unchecked")
	protected Map<String, Object> calculationsJsonToMap(String json) {
		Map<String, Object> calculations = new HashMap<>();
		try {
			calculations = new ObjectMapper().readValue(json, Map.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
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

		List<WLSample> sortedBelowLand  = new ArrayList<>(samples1);
		stats.sortByMediation(sortedBelowLand, MediationType.BelowLand);

		List<WLSample> sortedAboveDatum  = new ArrayList<>(samples1);
		stats.sortByMediation(sortedBelowLand, MediationType.AboveDatum);

		List<WLSample> samples2  = new ArrayList<>(samples1);

		stats.removeMostRecentProvisional(samples1, sortedBelowLand);
		assertFalse("should be removed because most recent", samples1.contains(recentRetained));
		assertFalse("should be removed because most recent", sortedBelowLand.contains(recentRetained));
		stats.removeMostRecentProvisional(samples2, sortedAboveDatum);
		assertFalse("should be removed because most recent", samples2.contains(recentRetained));
		assertFalse("should be removed because most recent", sortedAboveDatum.contains(recentRetained));
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

		List<WLSample> sortedBelowLand  = new ArrayList<>(samples1);
		stats.sortByMediation(sortedBelowLand, MediationType.BelowLand);

		List<WLSample> sortedAboveDatum  = new ArrayList<>(samples1);
		stats.sortByMediation(sortedBelowLand, MediationType.AboveDatum);

		List<WLSample> samples2  = new ArrayList<>(samples1);

		stats.removeMostRecentProvisional(samples1, sortedBelowLand);
		assertTrue("should be retained because most recent not provisional", samples1.contains(recentRetained));
		assertTrue("should be retained because most recent not provisional", sortedBelowLand.contains(recentRetained));
		stats.removeMostRecentProvisional(samples2, sortedAboveDatum);
		assertTrue("should be retained because most recent not provisional", samples2.contains(recentRetained));
		assertTrue("should be retained because most recent not provisional", sortedAboveDatum.contains(recentRetained));
	}

	@Test
	public void test_removeProvisional_and_MostRecent() throws Exception {
		// note that all previous calculate tests prove that 100% non-provisional collections are retained
		Specifier spec = new Specifier("USGS", "irrelevant", WellDataType.WATERLEVEL);

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
		WLSample recentProvisional= createSample("2018-05-13T04:15:00-05:00", "100.0", true); // this one only counts for latest value

		samples.add( explicitRetained ); // should be retained because not provisional
		samples.add( explicitRemoved );  // should be REMOVED because provisional
		samples.add( recentProvisional );// should be retained only for latest value because provisional most recent

		String json = stats.calculate(spec, samples);

		Map<String, String> overall = extractOverall(json);
		Map<String, Map<String, String>> monthly = extractMonthly(json);

		assertNotNull("overall should not be null", overall);
		assertNotNull("monthly should not be null", monthly);

		assertEquals("Expect MIN_VALUE to be ", "1.0",      overall.get(MIN_VALUE));
		assertEquals("Expect MAX_VALUE to be ", "1.0",      overall.get(MAX_VALUE));
		assertEquals("Expect MEDIAN to be ", "1.0",         overall.get(MEDIAN));
		assertEquals("Expect LATEST_VALUE to be ", "100.0", overall.get(LATEST_VALUE));
		assertEquals("Expect LATEST_PCTILE to be ", "1",    overall.get(LATEST_PCTILE));

		assertEquals("Expect all percentile to be ", "1.0",    monthly.get("5").get(P10));
		assertEquals("Expect all percentile to be ", "1.0",    monthly.get("5").get(P25));
		assertEquals("Expect all percentile to be ", "1.0",    monthly.get("5").get(P50));
		assertEquals("Expect all percentile to be ", "1.0",    monthly.get("5").get(P75));
		assertEquals("Expect all percentile to be ", "1.0",    monthly.get("5").get(P90));
	}


	@Test
	public void test_removeProvisional_not_MostRecent() throws Exception {
		// note that all previous calculate tests prove that 100% non-provisional collections are retained
		Specifier spec = new Specifier("USGS", "irrelevant", WellDataType.WATERLEVEL);

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

		String json = stats.calculate(spec, samples);

		Map<String, String> overall = extractOverall(json);
		Map<String, Map<String, String>> monthly = extractMonthly(json);

		assertNotNull("overall should not be null", overall);
		assertNotNull("monthly should not be null", monthly);

		assertEquals("Expect MIN_VALUE to be ", "100.0",     overall.get(MIN_VALUE));
		assertEquals("Expect MAX_VALUE to be ",   "1.0",     overall.get(MAX_VALUE));
		assertEquals("Expect MEDIAN to be ",      "1.0",     overall.get(MEDIAN));
		assertEquals("Expect LATEST_VALUE to be ", "100.0",  overall.get(LATEST_VALUE));
		assertEquals("Expect LATEST_PCTILE to be ", "0",     overall.get(LATEST_PCTILE));

		assertEquals("Expect percentile to be 80.2", "80.2", monthly.get("5").get(P10));
		assertEquals("Expect most percentile to be ", "1.0", monthly.get("5").get(P25));
		assertEquals("Expect most percentile to be ", "1.0", monthly.get("5").get(P50));
		assertEquals("Expect most percentile to be ", "1.0", monthly.get("5").get(P75));
		assertEquals("Expect most percentile to be ", "1.0", monthly.get("5").get(P90));
	}


//	@Test
//	public void test_monthlyYearCount_low() throws Exception {
//		List<WLSample> samples = new LinkedList<>();
//		// here are some extra data points in months that have a <10 year window and excluded for calculation
//		samples.add( createSample("2013-08-10T04:15:00-05:00", "93.1937") );
//		samples.add( createSample("2013-08-11T04:15:00-05:00", "93.1937") );
//		samples.add( createSample("2013-08-10T04:15:00-05:00", "93.1937") );
//
//		int count = stats.uniqueYears(samples);
//		assertEquals("Expect that when the given record years is 1",1, count);
//	}
//
//	@Test
//	public void test_monthlyYearCount_mix() throws Exception {
//		List<WLSample> samples = new LinkedList<>();
//		// here are some extra data points in months that have a <10 year window and excluded for calculation
//		samples.add( createSample("2013-08-10T04:15:00-05:00", "93.1937") );
//		samples.add( createSample("2013-08-11T04:15:00-05:00", "93.1937") );
//		samples.add( createSample("2023-09-10T04:15:00-05:00", "93.1937") );
//
//		int count2 = stats.uniqueYears(samples);
//		assertEquals("Expect that when the given data has more than one month then it return a uniques month count for the first month",2, count2);
//	}
//
//	@Test
//	public void test_monthlyYearCount_10() throws Exception {
//		List<WLSample> samples = new LinkedList<>();
//		// here are some extra data points in months that have a <10 year window and excluded for calculation
//		samples.add( createSample("2001-08-10T04:15:00-05:00", "93.1937") );
//		samples.add( createSample("2002-08-11T04:15:00-05:00", "93.1937") );
//		samples.add( createSample("2003-09-10T04:15:00-05:00", "93.1937") );
//		samples.add( createSample("2004-08-10T04:15:00-05:00", "93.1937") );
//		samples.add( createSample("2005-08-11T04:15:00-05:00", "93.1937") );
//		samples.add( createSample("2006-09-10T04:15:00-05:00", "93.1937") );
//		samples.add( createSample("2007-08-10T04:15:00-05:00", "93.1937") );
//		samples.add( createSample("2008-08-11T04:15:00-05:00", "93.1937") );
//		samples.add( createSample("2009-09-10T04:15:00-05:00", "93.1937") );
//		samples.add( createSample("2010-09-10T04:15:00-05:00", "93.1937") );
//
//		int count10 = stats.uniqueYears(samples);
//		assertEquals("While the difference of 2010-2001 is 9 we expect 10",10, count10);
//	}


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

		Specifier site = new Specifier("not USGS", "irrelevant", WellDataType.WATERLEVEL);

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

		Specifier site = new Specifier("USGS", "irrelevant", WellDataType.WATERLEVEL);

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

		Specifier site = new Specifier("USGS", "irrelevant", WellDataType.WATERLEVEL);

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

		Specifier site = new Specifier("USGS", "irrelevant", WellDataType.WATERLEVEL);

		MediationType mediation = stats.findMostPrevalentMediation(site, samples);
		assertEquals("PCODE P62610, above a datum type, is most prevalent.", MediationType.AboveDatum, mediation);

		List<WLSample> actual = stats.useMostPrevalentPCodeMediatedValue(site, samples, mediation);
		assertEquals("Above Datum values should be used in new list", aboveDatum, actual.get(0).value);
	}

	//WL_452012093412701_MORE_72019

//	@Test
//	public void test_rounding_vs_truncation_vs_sigfigs() {
//		List<WLSample> samples = new LinkedList<>();
//		samples.add( createSample("2013-11-10T04:15:00-05:00", "12.27") );
//		samples.add( createSample("2012-11-10T04:15:00-05:00", "11.97") );
//		samples.add( createSample("2009-11-10T04:15:00-05:00", "11.43") );
//		samples.add( createSample("1978-11-10T04:15:00-05:00", "7.3") );
//		samples.add( createSample("1971-11-10T04:15:00-05:00", "4.9") );
//		samples.add( createSample("1973-11-10T04:15:00-05:00", "4.8") );
//		samples.add( createSample("1970-11-10T04:15:00-05:00", "4.5") );
//		samples.add( createSample("1968-11-10T04:15:00-05:00", "3.9") );
//		samples.add( createSample("1965-11-10T04:15:00-05:00", "2.9") );
//		samples.add( createSample("1967-11-10T04:15:00-05:00", "2.6") );
//		samples.add( createSample("1966-11-10T04:15:00-05:00", "2.3") );
//		samples.add( createSample("1964-11-10T04:15:00-05:00", "2.2") );
//		samples.add( createSample("1962-11-10T04:15:00-05:00", "1.7") );
//
//		BigDecimal p25 = valueOfPercentile(samples, WaterLevelStatistics.PERCENTILES.get(P25), WLSample::valueOf);
//
//		assertEquals("reverse sort gives this 9.3 with wrong rounding rule", "9.3", p25.toString());
//
//		List<WLSample> samples1 = new LinkedList<>();
//		samples1.add( createSample("1962-11-10T04:15:00-05:00", "1.7") );
//		samples1.add( createSample("1964-11-10T04:15:00-05:00", "2.2") );
//		samples1.add( createSample("1966-11-10T04:15:00-05:00", "2.3") );
//		samples1.add( createSample("1967-11-10T04:15:00-05:00", "2.6") );
//		samples1.add( createSample("1965-11-10T04:15:00-05:00", "2.9") );
//		samples1.add( createSample("1968-11-10T04:15:00-05:00", "3.9") );
//		samples1.add( createSample("1970-11-10T04:15:00-05:00", "4.5") );
//		samples1.add( createSample("1973-11-10T04:15:00-05:00", "4.8") );
//		samples1.add( createSample("1971-11-10T04:15:00-05:00", "4.9") );
//		samples1.add( createSample("1978-11-10T04:15:00-05:00", "7.3") );
//		samples1.add( createSample("2009-11-10T04:15:00-05:00", "11.43") );
//		samples1.add( createSample("2012-11-10T04:15:00-05:00", "11.97") );
//		samples1.add( createSample("2013-11-10T04:15:00-05:00", "12.27") );
//
//		BigDecimal p75 = valueOfPercentile(samples1, WaterLevelStatistics.PERCENTILES.get(P75), WLSample::valueOf);
//
//		assertEquals("standard sort gives this 9.4", "9.4", p75.toString()); // this is what the sigfigs should return
//		// never get 9.37 as Excel spreadsheet does
////		assertEquals("9.37", p75.toString()); // this is the inflated sigfigs value
//
//		// In the case where it is suppose to be 9.4 as a round of 9.37, we get 9.4 when sorted in
//		// increasing order because we add 7.3+2.1 = 9.4 (no rounding) but when we sort in reverse
//		// order we are taking 11.43-2.1 = 9.33 rounded to 9.3. We never see 9.37 in any case with sigfigs.
//		// The tests below show that sigfigs is the issue we are not taking 7.3+2.15 or 11.4-2.15
//		// we are using 2.1 from addition sigfigs rules.
//		// Here is why... 4.1 / 2.0 (half up) is 2.1, -4.1 / 2.0 (round UP again) is -2.1
//		// While this is the same value it is not the same overall rounding rule.
//		// In the negative case -4.1 / 2.0 (with round down) is -2.0 for 11.43-2.0 = 9.4
//		// we could also solve this by ignoring sigfigs for the one calculation
//		// fraction = difference * d (decimal index)
//
//		List<WLSample> samples2 = new LinkedList<>();
//		samples2.add( createSample("2013-11-10T04:15:00-05:00", "12.2") );
//		samples2.add( createSample("2012-11-10T04:15:00-05:00", "11.9") );
//		samples2.add( createSample("2009-11-10T04:15:00-05:00", "11.4") );
//		samples2.add( createSample("1978-11-10T04:15:00-05:00", "7.3") );
//		samples2.add( createSample("1971-11-10T04:15:00-05:00", "4.9") );
//		samples2.add( createSample("1973-11-10T04:15:00-05:00", "4.8") );
//		samples2.add( createSample("1970-11-10T04:15:00-05:00", "4.5") );
//		samples2.add( createSample("1968-11-10T04:15:00-05:00", "3.9") );
//		samples2.add( createSample("1965-11-10T04:15:00-05:00", "2.9") );
//		samples2.add( createSample("1967-11-10T04:15:00-05:00", "2.6") );
//		samples2.add( createSample("1966-11-10T04:15:00-05:00", "2.3") );
//		samples2.add( createSample("1964-11-10T04:15:00-05:00", "2.2") );
//		samples2.add( createSample("1962-11-10T04:15:00-05:00", "1.7") );
//
//		BigDecimal p25b = valueOfPercentile(samples2, WaterLevelStatistics.PERCENTILES.get(P25), WLSample::valueOf);
//
//		assertEquals("reverse sort gives this 9.3 with wrong rounding rule", "9.3", p25b.toString());
//
//		List<WLSample> samples3 = new LinkedList<>();
//		samples3.add( createSample("1962-11-10T04:15:00-05:00", "1.7") );
//		samples3.add( createSample("1964-11-10T04:15:00-05:00", "2.2") );
//		samples3.add( createSample("1966-11-10T04:15:00-05:00", "2.3") );
//		samples3.add( createSample("1967-11-10T04:15:00-05:00", "2.6") );
//		samples3.add( createSample("1965-11-10T04:15:00-05:00", "2.9") );
//		samples3.add( createSample("1968-11-10T04:15:00-05:00", "3.9") );
//		samples3.add( createSample("1970-11-10T04:15:00-05:00", "4.5") );
//		samples3.add( createSample("1973-11-10T04:15:00-05:00", "4.8") );
//		samples3.add( createSample("1971-11-10T04:15:00-05:00", "4.9") );
//		samples3.add( createSample("1978-11-10T04:15:00-05:00", "7.3") );
//		samples3.add( createSample("2009-11-10T04:15:00-05:00", "11.4") );
//		samples3.add( createSample("2012-11-10T04:15:00-05:00", "11.9") );
//		samples3.add( createSample("2013-11-10T04:15:00-05:00", "12.2") );
//
//		BigDecimal p75b = valueOfPercentile(samples3, WaterLevelStatistics.PERCENTILES.get(P75), WLSample::valueOf);
//
//		assertEquals("no change with fewer sigfigs", "9.4", p75b.toString()); // this is what the sigfigs should return
//
//
//		List<WLSample> samples4 = new LinkedList<>();
//		samples4.add( createSample("2013-11-10T04:15:00-05:00", "12.2") );
//		samples4.add( createSample("2012-11-10T04:15:00-05:00", "11.9") );
//		samples4.add( createSample("2009-11-10T04:15:00-05:00", "11.40") );
//		samples4.add( createSample("1978-11-10T04:15:00-05:00", "7.30") );
//		samples4.add( createSample("1971-11-10T04:15:00-05:00", "4.9") );
//		samples4.add( createSample("1973-11-10T04:15:00-05:00", "4.8") );
//		samples4.add( createSample("1970-11-10T04:15:00-05:00", "4.5") );
//		samples4.add( createSample("1968-11-10T04:15:00-05:00", "3.9") );
//		samples4.add( createSample("1965-11-10T04:15:00-05:00", "2.9") );
//		samples4.add( createSample("1967-11-10T04:15:00-05:00", "2.6") );
//		samples4.add( createSample("1966-11-10T04:15:00-05:00", "2.3") );
//		samples4.add( createSample("1964-11-10T04:15:00-05:00", "2.2") );
//		samples4.add( createSample("1962-11-10T04:15:00-05:00", "1.7") );
//
//		BigDecimal p25c = valueOfPercentile(samples4, WaterLevelStatistics.PERCENTILES.get(P25), WLSample::valueOf);
//
//		assertEquals("with additional sigfigs, more refined answer equal to P75", "9.35", p25c.toString());
//
//		List<WLSample> samples5 = new LinkedList<>();
//		samples5.add( createSample("1962-11-10T04:15:00-05:00", "1.7") );
//		samples5.add( createSample("1964-11-10T04:15:00-05:00", "2.2") );
//		samples5.add( createSample("1966-11-10T04:15:00-05:00", "2.3") );
//		samples5.add( createSample("1967-11-10T04:15:00-05:00", "2.6") );
//		samples5.add( createSample("1965-11-10T04:15:00-05:00", "2.9") );
//		samples5.add( createSample("1968-11-10T04:15:00-05:00", "3.9") );
//		samples5.add( createSample("1970-11-10T04:15:00-05:00", "4.5") );
//		samples5.add( createSample("1973-11-10T04:15:00-05:00", "4.8") );
//		samples5.add( createSample("1971-11-10T04:15:00-05:00", "4.9") );
//		samples5.add( createSample("1978-11-10T04:15:00-05:00", "7.30") );
//		samples5.add( createSample("2009-11-10T04:15:00-05:00", "11.40") );
//		samples5.add( createSample("2012-11-10T04:15:00-05:00", "11.9") );
//		samples5.add( createSample("2013-11-10T04:15:00-05:00", "12.2") );
//
//		BigDecimal p75c = valueOfPercentile(samples5, WaterLevelStatistics.PERCENTILES.get(P75), WLSample::valueOf);
//
//		assertEquals("with additional sigfigs, more refined answer equal to P25", "9.35", p75c.toString());
//
//	}

	@Test
	public void test_ensureThatMutlipleSamplesInOneYearForGivenMonthAreAveraged_BeforeStatsCalc() {
		// This is data as it was from the database on 03/25/2017 and in order to preserve a testable
		// state this recreates that data set for the April data only for USGS:394829074053502
		List<WLSample> monthSamples = new LinkedList<>();
		fillAprilData(monthSamples);

		Map<String,Integer>yearCounts = new HashMap<>();
		for (WLSample sample : monthSamples) {
			String year = StatisticsCalculator.yearUTC(sample.time);
			Integer count = yearCounts.get(year);
			if (count == null) {
				count = 0;
			}
			count = count+1;
			yearCounts.put(year,count);
		}

		int yearCountsGreaterThan1 = 0;

		for (String year : yearCounts.keySet()) {
			int count = yearCounts.get(year);
			if (count > 1) {
				System.out.println(year);
				yearCountsGreaterThan1++;
			}
		}

		// 2003 samples
		WLSample sample1 = monthSamples.get(1);
		WLSample sample7 = monthSamples.get(7);
		// 1968 samples
		WLSample sample17 = monthSamples.get(17);
		WLSample sample18 = monthSamples.get(18);

		// first assert what we have
		assertEquals("We expect that this sample has two years with multiple samples", 2, yearCountsGreaterThan1);
		// then check that averages removes them and that the new values are correct

		int preCount = monthSamples.size();
		List<WLSample> normalizeMutlipleYearlyValues = stats.normalizeMutlipleYearlyValues(monthSamples, MediationType.BelowLand);

		assertEquals("normalize should have removed two values, one from each of two years", preCount-2, normalizeMutlipleYearlyValues.size());

		// these should be removed
		assertFalse("values should have been removed", monthSamples.contains(sample1));
		assertFalse("values should have been removed", monthSamples.contains(sample7));
		assertFalse("values should have been removed", monthSamples.contains(sample17));
		assertFalse("values should have been removed", monthSamples.contains(sample18));

		// find the new samples for 1968 and 2003
		WLSample sample1968 = null;
		WLSample sample2003 = null;

		for (WLSample sample : normalizeMutlipleYearlyValues) {
			if (StatisticsCalculator.yearUTC(sample.time).equals("1968")) {
				sample1968 = sample;
			}
			if (StatisticsCalculator.yearUTC(sample.time).equals("2003")) {
				sample2003 = sample;
			}
		}

		assertNotNull("new value for 1968 should be found", sample1968);
		assertNotNull("new value for 2003 should be found", sample2003);

		assertEquals(9.67, (9.2+10.13)/2+.005, 0);

		assertEquals("9.6", sample2003.value.toString());
		assertEquals("2.7"/*0"*/, sample1968.value.toString());

		Map<String, Map<String, String>> april = stats.monthlyStats(monthSamples, MediationType.BelowLand);

		System.err.println(april);

	}
	protected void fillAprilData(List<WLSample> monthSamples) {
		monthSamples.add( createSample("2007-04-11T10:07:00-04:00", "10.6") );
		monthSamples.add( createSample("2003-04-01T11:00:00-05:00", "10.13") );
		monthSamples.add( createSample("2010-04-13T12:55:00-04:00", "10.0") );
		monthSamples.add( createSample("2013-04-23T13:07:00-04:00", "9.92") );
		monthSamples.add( createSample("2009-04-27T10:20:00-04:00", "9.9") );
		monthSamples.add( createSample("2000-04-05T12:00:00", "9.8") );
		monthSamples.add( createSample("2012-04-09T11:20:00-04:00", "9.6") );
		monthSamples.add( createSample("2003-04-11T12:00:00-04:00", "9.2") );
		monthSamples.add( createSample("1983-04-05T12:00:00", "8.2") );
		monthSamples.add( createSample("1981-04-28T12:00:00", "8.1") );
		monthSamples.add( createSample("1984-04-03T12:00:00", "8.0") );
		monthSamples.add( createSample("1985-04-01T12:00:00", "7.7") );
		monthSamples.add( createSample("1979-04-10T12:00:00", "7.4") );
		monthSamples.add( createSample("1980-04-18T12:00:00", "7.0") );
		monthSamples.add( createSample("1975-04-22T12:00:00", "5.4") );
		monthSamples.add( createSample("1972-04-06T12:00:00", "4.2") );
		monthSamples.add( createSample("1970-04-17T12:00:00", "3.5") );
		monthSamples.add( createSample("1968-04-02T12:00:00", "2.9") );
		monthSamples.add( createSample("1968-04-29T12:00:00", "2.5") );
		monthSamples.add( createSample("1963-04-05T12:00:00", "2.3") );
		monthSamples.add( createSample("1967-04-03T12:00:00", "2.2") );
		monthSamples.add( createSample("1966-04-07T12:00:00", "1.8") );
		monthSamples.add( createSample("1964-04-02T12:00:00", "1.7") );
		monthSamples.add( createSample("1965-04-05T12:00:00", "1.7") );
	}

//	@Test
//	public void test_Jun25Pct() {
//		// This is data as it was from the database on 03/25/2017 and in order to preserve a testable
//		// state this recreates that data set for the April data only for USGS:394829074053502
//		List<WLSample> monthSamples = new LinkedList<>();
//		fillJuneData(monthSamples);
//
//		int preCount = monthSamples.size();
//		stats.normalizeMutlipleYearlyValues(monthSamples, MediationType.BelowLand);
//		assertEquals("normalize should have removed no values", preCount, monthSamples.size());
//
//		BigDecimal p10c = valueOfPercentile(monthSamples, WaterLevelStatistics.PERCENTILES.get(P25), WLSample::valueOf);
//		assertEquals("with additional sigfigs, more refined answer equal to p25", "9.02", p10c.toString());
//
//		// 24 * %25 = 6 which is index 5 returning the raw value (note that the math is done on 24+1 but still return raw value)
//		// however when reversed
//		// (24+1) %75 = 18.75 which tries to get the 9.05-9.0 = 0.05 rounded to 0.1 and then %75 * .1 is 0.075  but rounded 0.1 again
//		// 9.0 + 0.1 = 9.1
//
//		Collections.reverse(monthSamples);
//		BigDecimal p75c = valueOfPercentile(monthSamples, WaterLevelStatistics.PERCENTILES.get(P75), WLSample::valueOf);
//		assertEquals("reverse sort should yeild the same answer for p75", "9.1", p75c.toString());
//	}
//
//	@Test
//	public void test_Jun25Pct_plusOneSigfig() {
//		// This is data as it was from the database on 03/25/2017 and in order to preserve a testable
//		// state this recreates that data set for the April data only for USGS:394829074053502
//		List<WLSample> monthSamples = new LinkedList<>();
//		fillJuneData(monthSamples);
//
//		// increased sigfig with assertion that the expected value is replaced
//		assertTrue(replaceValue(monthSamples, 6,  createSample("1995-06-05T12:00:00", "9.00"), "9.0"));
//
//		int preCount = monthSamples.size();
//		stats.normalizeMutlipleYearlyValues(monthSamples, MediationType.BelowLand);
//		assertEquals("normalize should have removed no values", preCount, monthSamples.size());
//
//		BigDecimal p10c = valueOfPercentile(monthSamples, WaterLevelStatistics.PERCENTILES.get(P25), WLSample::valueOf);
//		assertEquals("with additional sigfigs, more refined answer equal to p25", "9.04", p10c.toString());
//
//		// in this case the %25 and %75 acting on the (24+1) count accuracy of 9.05 and 9.00 both result in 9.04
//		// GWW rounds 9.04 to 9.0
//
//		Collections.reverse(monthSamples);
//		BigDecimal p75c = valueOfPercentile(monthSamples, WaterLevelStatistics.PERCENTILES.get(P75), WLSample::valueOf);
//		assertEquals("reverse sort should yeild the same answer for p75", "9.04", p75c.toString());
//	}
//
//
//
//	protected void fillJuneData(List<WLSample> monthSamples) {
//		monthSamples.add( createSample("2000-06-01T12:00:00", "9.5") );
//		monthSamples.add( createSample("2007-06-07T13:30:00-04:00", "9.5") );
//		monthSamples.add( createSample("2001-06-15T12:00:00", "9.3") );
//		monthSamples.add( createSample("2003-06-03T10:44:00-04:00", "9.3") );
//		monthSamples.add( createSample("2002-06-13T12:00:00", "9.2") );
//		monthSamples.add( createSample("2013-06-19T12:28:00-04:00", "9.05") );
//		monthSamples.add( createSample("1995-06-05T12:00:00", "9.0") );
//		monthSamples.add( createSample("1996-06-04T12:00:00", "8.8") );
//		monthSamples.add( createSample("2004-06-02T09:35:00-04:00", "8.5") );
//		monthSamples.add( createSample("1994-06-22T12:00:00", "8.4") );
//		monthSamples.add( createSample("1997-06-10T12:00:00", "8.4") );
//		monthSamples.add( createSample("1999-06-09T12:00:00", "8.4") );
//		monthSamples.add( createSample("1993-06-18T12:00:00", "8.2") );
//		monthSamples.add( createSample("1998-06-11T12:00:00", "8.2") );
//		monthSamples.add( createSample("1985-06-18T12:00:00", "7.7") );
//		monthSamples.add( createSample("1973-06-28T12:00:00", "4.3") );
//		monthSamples.add( createSample("1971-06-11T12:00:00", "3.9") );
//		monthSamples.add( createSample("1974-06-27T12:00:00", "3.9") );
//		monthSamples.add( createSample("1969-06-23T12:00:00", "2.9") );
//		monthSamples.add( createSample("1968-06-26T12:00:00", "2.5") );
//		monthSamples.add( createSample("1966-06-06T12:00:00", "2.1") );
//		monthSamples.add( createSample("1964-06-03T12:00:00", "1.6") );
//		monthSamples.add( createSample("1965-06-03T12:00:00", "1.1") );
//		monthSamples.add( createSample("1963-06-07T12:00:00", "0.9") );
//	}
//
//	@Test
//	public void test_July10PctValue() {
//		// This is data as it was from the database on 03/25/2017 and in order to preserve a testable
//		// state this recreates that data set for the April data only for USGS:394829074053502
//		List<WLSample> monthSamples = new LinkedList<>();
//		fillJulyData(monthSamples);
//
//		int preCount = monthSamples.size();
//		List<WLSample> normalizeMutlipleYearlyValues = stats.normalizeMutlipleYearlyValues(monthSamples, MediationType.BelowLand);
//		assertEquals("normalize should have removed values", preCount-1, normalizeMutlipleYearlyValues.size());
//
//		BigDecimal p10c = valueOfPercentile(monthSamples, WaterLevelStatistics.PERCENTILES.get(P10), WLSample::valueOf);
//		assertEquals("with additional sigfigs, more refined answer equal to P10", "9.3", p10c.toString());
//
//		BigDecimal down = SigFigMathUtil.sigFigAdd(new BigDecimal("9.37"), new BigDecimal("-0.1"));
//		assertEquals(new BigDecimal("9.3"), down);
//
//		BigDecimal up = SigFigMathUtil.sigFigAdd(new BigDecimal("9.1"), new BigDecimal("0.1"));
//		assertEquals(new BigDecimal("9.2"), up);
//
//		BigDecimal dif1 = sigFigSubtract(new BigDecimal("9.1"), new BigDecimal("9.37"));
//		BigDecimal dif2 = sigFigSubtract(new BigDecimal("9.37"), new BigDecimal("9.1"));
//		assertEquals("expect the differences to round to the same magnitude", dif1.abs(), dif2.abs());
//
//		// 9.37-9.1=0.27 round 0.3 and 9.1-9.37=-0.27 round -0.3
//		// then 0.5 x 0.3 = .15 round .2 while 0.5 x -0.3 is -.15 round is -0.1
//		// 9.37 -0.1 = 9.27 round 9.3
//
//		// even reversed it is the same percentile value -- is should be, testing to ensure
//		Collections.reverse(monthSamples);
//		BigDecimal p90c = valueOfPercentile(monthSamples, WaterLevelStatistics.PERCENTILES.get(P90), WLSample::valueOf);
//		assertEquals("reverse sort should yeild the same answer for P90", "9.3", p90c.toString());
//	}
//	@Test
//	public void test_July10PctValue_plusOneSigfig() {
//		// This is data as it was from the database on 03/25/2017 and in order to preserve a testable
//		// state this recreates that data set for the April data only for USGS:394829074053502
//		List<WLSample> monthSamples = new LinkedList<>();
//		fillJulyData(monthSamples);
//
//		// increased sigfig with assertion that the expected value is replaced
//		assertTrue(replaceValue(monthSamples, 1, createSample("2002-07-17T12:00:00-04:00", "9.10"), "9.1"));
//
//		int preCount = monthSamples.size();
//		List<WLSample> normalizeMutlipleYearlyValues = stats.normalizeMutlipleYearlyValues(monthSamples, MediationType.BelowLand);
//		assertEquals("normalize should have removed values", preCount-1, normalizeMutlipleYearlyValues.size());
//
//		BigDecimal p10c = valueOfPercentile(normalizeMutlipleYearlyValues, WaterLevelStatistics.PERCENTILES.get(P10), WLSample::valueOf);
//		assertEquals("with additional sigfigs, more refined answer equal to P10", "9.23", p10c.toString());
//
//		BigDecimal down = SigFigMathUtil.sigFigAdd(new BigDecimal("9.37"), new BigDecimal("-0.10"));
//		assertEquals(new BigDecimal("9.27"), down);
//
//		BigDecimal up = SigFigMathUtil.sigFigAdd(new BigDecimal("9.10"), new BigDecimal("0.10"));
//		assertEquals(new BigDecimal("9.20"), up);
//
//		BigDecimal dif1 = sigFigSubtract(new BigDecimal("9.10"), new BigDecimal("9.37"));
//		BigDecimal dif2 = sigFigSubtract(new BigDecimal("9.37"), new BigDecimal("9.10"));
//		assertEquals("expect the differences to round to the same magnitude", dif1.abs(), dif2.abs());
//
//		// 9.37-9.10=0.27 no round and 9.10-9.37=-0.27 no round
//		// then 0.5 x 0.27 = .135 round .14 while 0.5 x -0.27 is -.135 round is -0.13
//		// 9.37 -0.13 = 9.24 round 9.24 round 9.2 on gww
//
//		// even reversed it is the same percentile value -- is should be, testing to ensure
//		Collections.reverse(normalizeMutlipleYearlyValues);
//		BigDecimal p90c = valueOfPercentile(normalizeMutlipleYearlyValues, WaterLevelStatistics.PERCENTILES.get(P90), WLSample::valueOf);
//		assertEquals("reverse sort should yeild the same answer for P90", "9.24", p90c.toString());
//	}
//
//	protected void fillJulyData(List<WLSample> monthSamples) {
//		monthSamples.add( createSample("2014-07-01T13:01:00-04:00", "9.37") );
//		monthSamples.add( createSample("2002-07-17T12:00:00-04:00", "9.1") );
//		monthSamples.add( createSample("2003-07-03T12:00:00-04:00", "8.9") );
//		monthSamples.add( createSample("1989-07-17T12:00:00", "7.7") );
//		monthSamples.add( createSample("1975-07-02T12:00:00", "4.7") );
//		monthSamples.add( createSample("1972-07-11T12:00:00", "4.1") );
//		monthSamples.add( createSample("1970-07-14T12:00:00", "3.3") );
//		monthSamples.add( createSample("1968-07-17T12:00:00", "2.6") );
//		monthSamples.add( createSample("1967-07-06T12:00:00", "2.5") );
//		monthSamples.add( createSample("1966-07-27T12:00:00", "2.2") );
//		monthSamples.add( createSample("1966-07-01T12:00:00", "2.0") );
//		monthSamples.add( createSample("1965-07-06T12:00:00", "1.9") );
//		monthSamples.add( createSample("1964-07-02T12:00:00", "1.7") );
//		monthSamples.add( createSample("1963-07-03T12:00:00", "1.6") );
//		monthSamples.add( createSample("1962-07-20T12:00:00", "1.0") );
///*  these two entries
//		monthSamples.add( createSample("1966-07-27T12:00:00", "2.2") );
//		monthSamples.add( createSample("1966-07-01T12:00:00", "2.0") );
//	get normalized to this (by median or mean using 50 percentile of the list)
//		monthSamples.add( createSample("1966-07-01T12:00:00", "2.1") );
//	even so thes only have an impact on the answer because the count it reduced by one
//*/
//	}
//
//	@Test
//	public void test_Aug25Pct() {
//		// This is data as it was from the database on 03/25/2017 and in order to preserve a testable
//		// state this recreates that data set for the April data only for USGS:394829074053502
//		List<WLSample> monthSamples = new LinkedList<>();
//		fillAugData(monthSamples);
//
//		int preCount = monthSamples.size();
//		List<WLSample> normalizeMutlipleYearlyValues = stats.normalizeMutlipleYearlyValues(monthSamples, MediationType.BelowLand);
//		assertEquals("normalize should have removed values", preCount-2, normalizeMutlipleYearlyValues.size());
//
//		BigDecimal p25c = valueOfPercentile(normalizeMutlipleYearlyValues, WaterLevelStatistics.PERCENTILES.get(P25), WLSample::valueOf);
//		assertEquals("with additional sigfigs, more refined answer equal to p25", "9.6", p25c.toString());
//
//		// 03-30-2017 (Supplanted by 07-31-2018 rules which is actual a revert to original rounding)
//		// 9.5 & 9.8 mean is 9.7  because 9.8-9.5 = 0.3 and mean is 0.15 rounded to 0.2
//		// 9.7-9.46=0.24 round 0.2 and 9.46-9.7=-0.24 round -0.2
//		// then 0.25 x 0.2 = 0.05 no round  while 0.25 x -0.05  no round
//		// 9.7 -0.05 = 9.65 round 9.7
//
//		// 07-31-2018
//		// 9.5 & 9.8 mean is 9.6  because 9.5-9.8 = -0.3 and mean is -0.15 rounded to -0.2
//		// 9.46-9.6=-0.14 round -0.1
//		// then 0.25 x -0.1 = 0.025 round -0.03
//		// 9.6 -0.03 = 9.57 round 9.6
//		// because it is based off of 9.6 rounded to one fewer sigfig.
//		// if there was no sigfig rounding 9.6 - (0.25*0.14) = 9.6 - 0.035 = 9.565 and we expect this to be the same as reversed (below)
//
//		// reversed test
//		Collections.reverse(normalizeMutlipleYearlyValues);
//		BigDecimal p75c = valueOfPercentile(normalizeMutlipleYearlyValues, WaterLevelStatistics.PERCENTILES.get(P75), WLSample::valueOf);
//		assertEquals("reverse sort should yeild the same answer for p75", "9.54", p75c.toString());
//
//		// 07-31-2018
//		// 9.5 & 9.8 mean is 9.6  because 9.5-9.8 = -0.3 and mean is -0.15 rounded to -0.2
//		// 9.6-9.46 = 0.14 round 0.1
//		// then 0.75 x 0.1 = 0.075 round 0.08
//		// finally 9.46+0.08 = 9.54
//		// because reversed is based off of 9.46 there is one more sigfig.
//		// if there was no sigfig rounding 9.46+(0.75*0.14) = 9.46 + 0.105 = 9.565 and we expect this to be the same as non-reversed (above)
//	}
//
//	@Test
//	public void test_Aug25Pct_plusOneSigfig() {
//		// This is data as it was from the database on 03/25/2017 and in order to preserve a testable
//		// state this recreates that data set for the April data only for USGS:394829074053502
//		List<WLSample> monthSamples = new LinkedList<>();
//		fillAugData(monthSamples);
//		// increased sigfig with assertion that the expected value is replaced
//		assertTrue(replaceValue(monthSamples, 4, createSample("2005-08-25T12:12:00-04:00", "9.80"), "9.8"));
//		assertTrue(replaceValue(monthSamples, 5, createSample("2005-08-03T10:32:00-04:00", "9.50"), "9.5"));
//
//		int preCount = monthSamples.size();
//		List<WLSample> normalizeMutlipleYearlyValues = stats.normalizeMutlipleYearlyValues(monthSamples, MediationType.BelowLand);
//		assertEquals("normalize should have removed values", preCount-2, normalizeMutlipleYearlyValues.size());
//
//		BigDecimal p25c = valueOfPercentile(normalizeMutlipleYearlyValues, WaterLevelStatistics.PERCENTILES.get(P25), WLSample::valueOf);
//		assertEquals("with additional sigfigs, more refined answer equal to p25", "9.60", p25c.toString());
//
//		// 9.50 & 9.80 mean is 9.65  because 9.80-9.50 = 0.30 and mean is 0.15
//		// 9.65-9.46=0.19 and 9.46-9.70=-0.19
//		// then 0.25 x -0.19 = 0.0475 round 0.05 while 0.75 x 0.1425 rounded 0.14
//		// 9.65 -0.05 = 9.60  and 9.46 +0.14 = 9.60 round 9.6 on gww
//
//		// even reversed it is the same percentile value -- is should be, testing to ensure
//		Collections.reverse(normalizeMutlipleYearlyValues);
//		BigDecimal p75c = valueOfPercentile(normalizeMutlipleYearlyValues, WaterLevelStatistics.PERCENTILES.get(P75), WLSample::valueOf);
//		assertEquals("reverse sort should yeild the same answer for p75", "9.60", p75c.toString());
//	}
//
//	protected void fillAugData(List<WLSample> monthSamples) {
//		monthSamples.add( createSample("2007-08-22T10:07:00-04:00", "11.1") );
//		monthSamples.add( createSample("2007-08-09T12:18:00-04:00", "11.0") );
//		monthSamples.add( createSample("2010-08-04T08:55:00-04:00", "10.2") );
//		monthSamples.add( createSample("2008-08-07T12:16:00-04:00", "9.9") );
//		monthSamples.add( createSample("2005-08-25T12:12:00-04:00", "9.8") );
//		monthSamples.add( createSample("2005-08-03T10:32:00-04:00", "9.5") );
//		monthSamples.add( createSample("2013-08-12T14:56:00-04:00", "9.46") );
//		monthSamples.add( createSample("1979-08-06T12:00:00", "7.5") );
//		monthSamples.add( createSample("1978-08-04T12:00:00", "6.4") );
//		monthSamples.add( createSample("1977-08-03T12:00:00", "6.3") );
//		monthSamples.add( createSample("1976-08-12T12:00:00", "4.8") );
//		monthSamples.add( createSample("1974-08-09T12:00:00", "4.4") );
//		monthSamples.add( createSample("1970-08-21T12:00:00", "3.1") );
//		monthSamples.add( createSample("1968-08-20T12:00:00", "3.0") );
//		monthSamples.add( createSample("1966-08-31T12:00:00", "2.8") );
//		monthSamples.add( createSample("1965-08-04T12:00:00", "2.0") );
//		monthSamples.add( createSample("1964-08-04T12:00:00", "1.8") );
//		monthSamples.add( createSample("1967-08-08T12:00:00", "1.1") );
///*  these two entries
//		monthSamples.add( createSample("2005-08-25T12:12:00-04:00", "9.8") );
//		monthSamples.add( createSample("2005-08-03T10:32:00-04:00", "9.5") );
//	get normalized to this (by median or mean using 50 percentile of the list)
//		monthSamples.add( createSample("2005-08-25T12:12:00-04:00", "9.7") );
//*/
//	}
//
//	@Test
//	public void test_Dec25Pct() {
//		// This is data as it was from the database on 03/25/2017 and in order to preserve a testable
//		// state this recreates that data set for the April data only for USGS:394829074053502
//		List<WLSample> monthSamples = new LinkedList<>();
//		fillDecData(monthSamples);
//
//		int preCount = monthSamples.size();
//		stats.normalizeMutlipleYearlyValues(monthSamples, MediationType.BelowLand);
//		assertEquals("normalize should have removed NO values", preCount, monthSamples.size());
//
//		BigDecimal p10c = valueOfPercentile(monthSamples, WaterLevelStatistics.PERCENTILES.get(P25), WLSample::valueOf);
//		assertEquals("with additional sigfigs, more refined answer equal to P25", "10.8", p10c.toString());
//
//		// 10.75-11.1=-0.35 round -0.3
//		// then 0.75 x -0.3 = 0.225  round  -0.2
//		// 11.1-0.2 = 10.9 no round
//
//		// even reversed it is the same percentile value -- is should be, testing to ensure
//		Collections.reverse(monthSamples);
//		BigDecimal p75c = valueOfPercentile(monthSamples, WaterLevelStatistics.PERCENTILES.get(P75), WLSample::valueOf);
//		assertEquals("reverse sort should yeild the same answer for P75", "10.9", p75c.toString());
//	}
//	@Test
//	public void test_Dec25Pct_plusOneSigfig() {
//		// This is data as it was from the database on 03/25/2017 and in order to preserve a testable
//		// state this recreates that data set for the April data only for USGS:394829074053502
//		List<WLSample> monthSamples = new LinkedList<>();
//		fillDecData(monthSamples);
//		// increased sigfig with assertion that the expected value is replaced
//		assertTrue(replaceValue(monthSamples, 5, createSample("2000-12-08T12:00:00", "11.10"), "11.1"));
//
//		int preCount = monthSamples.size();
//		stats.normalizeMutlipleYearlyValues(monthSamples, MediationType.BelowLand);
//		assertEquals("normalize should have removed NO values", preCount, monthSamples.size());
//
//		BigDecimal p10c = valueOfPercentile(monthSamples, WaterLevelStatistics.PERCENTILES.get(P25), WLSample::valueOf);
//		assertEquals("with an additional sigfig 11.1 to 11.10, more refined answer equal to P25", "10.84", p10c.toString());
//
//		// 10.75-11.10=-0.35
//		// then 0.75 x -0.35 = 0.2625 round 0.26
//		// 11.10-0.26 = 10.84 rounded to 10.8 on gww
//
//		// even reversed it is the same percentile value -- is should be, testing to ensure
//		Collections.reverse(monthSamples);
//		BigDecimal p75c = valueOfPercentile(monthSamples, WaterLevelStatistics.PERCENTILES.get(P75), WLSample::valueOf);
//		assertEquals("reverse sort should yield the same answer for p75.", "10.84", p75c.toString());
//	}
//
//	protected void fillDecData(List<WLSample> monthSamples) {
//		monthSamples.add( createSample("2007-12-20T09:55:00-05:00", "12.47") );
//		monthSamples.add( createSample("2006-12-04T13:35:00-05:00", "12.12") );
//		monthSamples.add( createSample("2004-12-09T11:40:00-05:00", "11.53") );
//		monthSamples.add( createSample("2001-12-03T12:00:00", "11.5") );
//		monthSamples.add( createSample("2002-12-13T12:20:00-05:00", "11.25") );
//		monthSamples.add( createSample("2000-12-08T12:00:00", "11.1") );
//		monthSamples.add( createSample("2003-12-22T09:37:00-05:00", "10.75") );
//		monthSamples.add( createSample("1997-12-16T12:00:00", "10.4") );
//		monthSamples.add( createSample("1998-12-10T12:00:00", "10.3") );
//		monthSamples.add( createSample("1993-12-22T12:00:00", "10.0") );
//		monthSamples.add( createSample("1994-12-09T12:00:00", "9.8") );
//		monthSamples.add( createSample("1999-12-15T12:00:00", "9.6") );
//		monthSamples.add( createSample("1996-12-17T12:00:00", "9.4") );
//		monthSamples.add( createSample("1984-12-12T12:00:00", "8.4") );
//		monthSamples.add( createSample("1982-12-21T12:00:00", "8.3") );
//		monthSamples.add( createSample("1985-12-11T12:00:00", "8.2") );
//		monthSamples.add( createSample("1974-12-10T12:00:00", "6.1") );
//		monthSamples.add( createSample("1970-12-31T12:00:00", "4.8") );
//		monthSamples.add( createSample("1972-12-14T12:00:00", "4.8") );
//		monthSamples.add( createSample("1968-12-17T12:00:00", "4.7") );
//		monthSamples.add( createSample("1969-12-12T12:00:00", "4.1") );
//		monthSamples.add( createSample("1966-12-05T12:00:00", "3.0") );
//		monthSamples.add( createSample("1964-12-03T12:00:00", "2.6") );
//		monthSamples.add( createSample("1965-12-06T12:00:00", "2.6") );
//		monthSamples.add( createSample("1967-12-19T12:00:00", "2.6") );
//		monthSamples.add( createSample("1963-12-02T12:00:00", "1.3") );
//	}
//
//	protected boolean replaceValue(List<WLSample> samples, int index, WLSample newSample, String expected) {
//		WLSample sample = samples.remove(index);
//		samples.add(index,newSample );
//		boolean isExpected = expected.equals(sample.value.toString());
//		return isExpected;
//	}
//
//
	@Test
	public void testMostRecentProvistionalData_notRemoved() {
		List<WLSample> monthSamples = new LinkedList<>();
		fillMarchData(monthSamples);
		WLSample provisional = createSample("2017-03-01","12.21");
		provisional.setProvsional(true);
		monthSamples.add( provisional );
		sortByDateOrder(monthSamples);

		int expected = monthSamples.size();
		stats.removeProvisionalButNotMostRecent(monthSamples, "SITE_NO");

		int actual = monthSamples.size();
		assertEquals("should KEEP most recent provisonal value here", expected, actual);
		assertTrue(monthSamples.contains(provisional));
	}
	@Test
	public void testMostRecentProvistionalData_Removed() {
		List<WLSample> valueOrder = new LinkedList<>();
		WLSample provisional = createSample("2017-03-01","12.21");
		provisional.setProvsional(true);
		valueOrder.add( provisional );
		fillMarchData(valueOrder);
		List<WLSample> monthSamples = new ArrayList<WLSample>(valueOrder);
		sortByDateOrder(monthSamples);

		int expected = monthSamples.size();

		Map<String, String> stat = stats.findMinMaxDatesAndDateRange(monthSamples,valueOrder);

		int actual = monthSamples.size();
		assertEquals("should REMOVE most recent provisional value here", expected, actual+1);
		assertFalse(monthSamples.contains(provisional));
		assertFalse(valueOrder.contains(provisional));
		assertEquals("most recent should be the provisional", provisional.value.toString(), stat.get(LATEST_VALUE));
	}
	@Test
	public void testMostRecentProvistional_overallStats() {
		List<WLSample> valueOrder = new LinkedList<>();
		WLSample provisional = createSample("2017-03-01","12.21");
		provisional.setProvsional(true);
		valueOrder.add( provisional );
		fillMarchData(valueOrder);
		List<WLSample> monthSamples = new ArrayList<WLSample>(valueOrder);
		sortByDateOrder(monthSamples);

		Map<String, String> stat = stats.overallStats(monthSamples, valueOrder, MediationType.AboveDatum);

		assertEquals(provisional.value.toString(),stat.get(LATEST_VALUE));
		assertFalse(monthSamples.contains(provisional));
		assertFalse(valueOrder.contains(provisional));

		assertEquals("7.98",stat.get(MEDIAN));
		assertEquals("0.745",stat.get(LATEST_PCTILE));
	}
	@Test
	public void testMostRecentProvistionalNONE_overallStats() {
		List<WLSample> valueOrder = new LinkedList<>();
		WLSample notProvisional = createSample("2017-03-01","12.21");
		valueOrder.add( notProvisional );
		fillMarchData(valueOrder);
		List<WLSample> monthSamples = new ArrayList<WLSample>(valueOrder);
		sortByDateOrder(monthSamples);

		Map<String, String> stat = stats.overallStats(monthSamples, valueOrder, MediationType.AboveDatum);

		assertEquals(notProvisional.value.toString(),stat.get(LATEST_VALUE));
		assertTrue(monthSamples.contains(notProvisional));
		assertTrue(valueOrder.contains(notProvisional));

		assertEquals("7.98",stat.get(MEDIAN));
		assertEquals("1",stat.get(LATEST_PCTILE));
	}

	@Test
	public void testMostRecentProvistional_monthlyStats() {
		List<WLSample> valueOrder = new LinkedList<>();
		WLSample provisional = createSample("2017-03-01","12.21");
		provisional.setProvsional(true);
		valueOrder.add( provisional );
		fillMarchData(valueOrder);
		List<WLSample> monthSamples = new ArrayList<WLSample>(valueOrder);
		sortByDateOrder(monthSamples);
		stats.overallStats(monthSamples, valueOrder, MediationType.AboveDatum);

		Map<String, String> stat = stats.monthlyStats(monthSamples, MediationType.AboveDatum).get("3");

		assertNotNull(stat);
		assertEquals("4.12",stat.get(P50_MIN));
		assertEquals("4.44",stat.get(P10));
		assertEquals("6.4", stat.get(P25));
		assertEquals("8.0", stat.get(P50));
		assertEquals("9.08",stat.get(P75));
		assertEquals("9.36",stat.get(P90));
		assertEquals("9.37",stat.get(P50_MAX));
	}
	@Test
	public void testMostRecentProvistionalNONE_monathlyStats() {
		List<WLSample> valueOrder = new LinkedList<>();
		WLSample notProvisional = createSample("2017-03-01","12.21");
		valueOrder.add( notProvisional );
		fillMarchData(valueOrder);
		List<WLSample> monthSamples = new ArrayList<WLSample>(valueOrder);
		sortByDateOrder(monthSamples);
		stats.overallStats(monthSamples, valueOrder, MediationType.AboveDatum);

		Map<String, String> stat = stats.monthlyStats(monthSamples, MediationType.AboveDatum).get("3");

		assertNotNull(stat);
		assertEquals("4.12", stat.get(P50_MIN));
		assertEquals("4.60", stat.get(P10));
		assertEquals("6.5",  stat.get(P25));
		assertEquals("8.0",  stat.get(P50));
		assertEquals("9.25", stat.get(P75));
		assertEquals("11.36",stat.get(P90));
		assertEquals("12.21",stat.get(P50_MAX));
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


//	@Test
//	public void testBigDecimalScale() {
//		// testing if understanding of divide and rounding to the expect scale as expected
//
//		BigDecimal twelve = new BigDecimal(12);
//
//		int a = 10;
//
//		int b = 6;
//		String val = new BigDecimal(a).subtract(new BigDecimal(b))
//		.divide(twelve, 1, RoundingMode.HALF_EVEN).toString();
//
//		assertEquals("0.3", val);
//
//		b = 4;
//		val = new BigDecimal(a).subtract(new BigDecimal(b))
//		.divide(twelve, 1, RoundingMode.HALF_EVEN).toString();
//
//		assertEquals("0.5", val);
//	}

}

