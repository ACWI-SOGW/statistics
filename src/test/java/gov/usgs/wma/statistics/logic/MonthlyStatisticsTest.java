package gov.usgs.wma.statistics.logic;

import static gov.usgs.wma.statistics.logic.MonthlyStatistics.*;
import static gov.usgs.wma.statistics.logic.OverallStatistics.*;
//import static gov.usgs.wma.statistics.logic.StatisticsCalculator.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.Test;

import gov.usgs.ngwmn.logic.WaterLevelStatistics.MediationType;
import gov.usgs.wma.statistics.model.Value;

public class MonthlyStatisticsTest {
	MonthlyStatistics<Value, MediationType> stats = new MonthlyStatistics<>(MediationType.BelowLand);

	private Value createSample(String time, String value) {
		BigDecimal val = null;
		if (null != value) {
			val = new BigDecimal(value);
		}
		return new Value(time, val);
	}

	@Test
	public void testFilterValuesByGivenMonth() {
		List<Value> samples = new LinkedList<>();
		samples.add( createSample("2015-11-10T04:15:00-05:00", "95.1772") );
		samples.add( createSample("2015-02-11T04:15:00-05:00", "95.1567") );
		samples.add( createSample("2015-02-12T04:15:00-05:00", "95.1937") );
		samples.add( createSample("2015-03-13T04:15:00-05:00", "95.1959") );
		samples.add( createSample("2015-03-14T04:15:00-05:00", "95.1442") );
		samples.add( createSample("2015-03-15T04:15:00-05:00", "95.0610") );
		samples.add( createSample("2015-04-16T04:15:00-05:00", "95.1591") );
		samples.add( createSample("2015-04-17T04:15:00-05:00", "95.1195") );
		samples.add( createSample("2015-04-18T04:15:00-05:00", "95.1065") );
		samples.add( createSample("2015-04-19T04:15:00-05:00", "95.0925") );
		samples.add( createSample("2015-05-20T04:15:00-05:00", "95.1990") );
		samples.add( createSample("2005-05-21T04:15:00-05:00", "95.1682") );
		samples.add( createSample("2014-05-10T04:15:00-05:00", "94.1772") );
		samples.add( createSample("2014-05-11T04:15:00-05:00", "94.1567") );
		samples.add( createSample("2014-05-12T04:15:00-05:00", "94.1937") );
		samples.add( createSample("2014-06-13T04:15:00-05:00", "94.1959") );
		samples.add( createSample("2014-06-14T04:15:00-05:00", "94.1442") );
		samples.add( createSample("2014-06-15T04:15:00-05:00", "94.0610") );
		samples.add( createSample("2014-06-16T04:15:00-05:00", "94.1591") );
		samples.add( createSample("2014-06-17T04:15:00-05:00", "94.1195") );
		samples.add( createSample("2014-06-17T04:15:00-05:00", "94.1065") );
		assertEquals(1, stats.filterValuesByGivenMonth(samples, "11").size());
		assertEquals(2, stats.filterValuesByGivenMonth(samples, "2").size());
		assertEquals(3, stats.filterValuesByGivenMonth(samples, "3").size());
		assertEquals(4, stats.filterValuesByGivenMonth(samples, "4").size());
		assertEquals(5, stats.filterValuesByGivenMonth(samples, "5").size());
		assertEquals(6, stats.filterValuesByGivenMonth(samples, "06").size());
	}

	
	@Test
	public void test_generateMonthYearlyPercentiles() throws Exception {
		List<Value> samples = new LinkedList<>();
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
		List<Value> sorted = new LinkedList<>(samples);
		StatisticsCalculator.sortByValueOrderAscending(sorted);
		List<Value> yearly = stats.generateMonthYearlyPercentiles(sorted); // MediationType.AboveDatum

		assertEquals("Expect 3 medians", 3, yearly.size());
		assertEquals("Expect large median to be",  "95.1579", yearly.get(2).value.toString());
		assertEquals("Expect middle median to be", "94.1579", yearly.get(1).value.toString());
		assertEquals("Expect least median to be",  "93.1579", yearly.get(0).value.toString());
	}

	@Test
	public void test_monthlyStats_yearly_sameMonth() throws Exception {
		List<Value> samples = new LinkedList<>();
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
		List<Value> sorted = new LinkedList<>(samples);
		StatisticsCalculator.sortByValueOrderAscending(sorted);

		// we are not testing this method so mock it to return what we need
		MonthlyStatistics<Value, MediationType> mockstats = new MonthlyStatistics<Value, MediationType>(MediationType.AboveDatum) {
//			protected boolean doesThisMonthQualifyForStats(List<Value> monthSamples) {
//				return monthSamples.size()>0;
//			}
			@Override
			public List<Value> normalizeMutlipleYearlyValues(List<Value> monthSamples, Function<List<Value>, List<Value>> sortBy) {
				return monthSamples;
			}
		};

		Map<String, Map<String, String>> monthly = mockstats.monthlyStats(sorted); // MediationType.AboveDatum

		assertEquals("Expect 1 month median", 1, monthly.size());
		assertEquals("Expect max median to be", "95.1579", monthly.get("1").get(P50_MAX));
		assertEquals("Expect mid median to be", "94.1579", monthly.get("1").get(P50));
		assertEquals("Expect min median to be", "93.1579", monthly.get("1").get(P50_MIN));
		assertEquals("Expect sample count to be ", ""+samples.size(), monthly.get("1").get(StatisticsCalculator.SAMPLE_COUNT));

		assertEquals("Expect record years to be 3 and not 10 because of unique year count.", "3", monthly.get("1").get(RECORD_YEARS));
	}

	
	@Test
	public void test_monthlyStats_yearly_monthly() throws Exception {
		List<Value> samples = new LinkedList<>();
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

		List<Value> sorted = new LinkedList<>(samples);

		// we are not testing this method so mock it to return what we need
		MonthlyStatistics<Value, MediationType> mockstats = new MonthlyStatistics<Value, MediationType>(MediationType.AboveDatum) {
			@Override
			public List<Value> normalizeMutlipleYearlyValues(List<Value> monthSamples,
					Function<List<Value>, List<Value>> sortBy) {
				// do not modify, testing the months. This prevents normalization to test aggregations
				return monthSamples;
			}
		};

		StatisticsCalculator.sortByValueOrderAscending(sorted);
		Map<String, Map<String, String>> monthly = mockstats.monthlyStats(sorted); // MediationType.AboveDatum

		assertEquals("Expect all 6 monthly stats for general monthly stats", 6, monthly.size());
		assertEquals("Expect May median to be", "95.1579", monthly.get("5").get(P50));
		assertEquals("Expect Apr median to be", "94.1579", monthly.get("4").get(P50));
		assertEquals("Expect Mar median to be", "93.1579", monthly.get("3").get(P50));

		assertEquals("Expect sample count to be ", "12", monthly.get("5").get(StatisticsCalculator.SAMPLE_COUNT));
		assertEquals("Expect sample count to be ", "12", monthly.get("4").get(StatisticsCalculator.SAMPLE_COUNT));
		assertEquals("Expect sample count to be ", "12", monthly.get("3").get(StatisticsCalculator.SAMPLE_COUNT));

		assertEquals("Expect record years to be 11 because there are 11 unique years in the given data even though there are 12 data entries and not a difference of 10 because we count the months.", "11", monthly.get("5").get(RECORD_YEARS));
		assertEquals("Expect record years to be 11 because there are 11 unique years in the given data even though there are 12 data entries and not a difference of 10 because we count the months.", "11", monthly.get("4").get(RECORD_YEARS));
		assertEquals("Expect record years to be 11 because there are 11 unique years in the given data even though there are 12 data entries and not a difference of 10 because we count the months.", "11", monthly.get("3").get(RECORD_YEARS));
	}

	@Test
	public void test_generateLatestPercentileBasedOnMonthlyData() throws Exception {
		List<Value> samples = new LinkedList<>();
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
		Value pctSample = createSample("2014-04-10T04:15:00-05:00", "94.1772");
		samples.add( pctSample );
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

		StatisticsCalculator.sortByDateOrder(samples);
//		String date = "2015-04-10";
		String latestPct = stats.percentileBasedOnMonthlyData(pctSample, samples); // MediationType.AboveDatum
		assertTrue("The latest percetile for the date given should be based on the month it is in "
				+ "and all data for the given month is 94.???? so the latest in that month should be 100% (percentile) "
				+ latestPct, latestPct.startsWith("1"));
	}
	
	@Test
	public void testMostRecentProvistional_monthlyStats() {
		List<Value> valueOrder = new LinkedList<>();
		Value provisional = createSample("2017-03-01","12.21");
		provisional.setProvsional(true);
		valueOrder.add( provisional );
		fillMarchData(valueOrder);
		
		stats.removeProvisional(valueOrder, "doesn't matter");
		
		List<Value> monthSamples = new ArrayList<Value>(valueOrder);
		StatisticsCalculator.sortByDateOrder(monthSamples);
//		stats.overallStats(monthSamples, valueOrder, MediationType.AboveDatum);

		Map<String, String> stat = stats.monthlyStats(monthSamples).get("3"); // MediationType.AboveDatum

		assertNotNull(stat);
		assertEquals("4.12",stat.get(P50_MIN));
		// if the provision value is not removed P10 will be 4.60
		assertEquals("4.44",stat.get(P10)); 
		assertEquals("6.4", stat.get(P25));
		assertEquals("8.0", stat.get(P50));
		assertEquals("9.08",stat.get(P75));
		assertEquals("9.36",stat.get(P90));
		assertEquals("9.37",stat.get(P50_MAX));
	}
	@Test
	public void testMostRecentProvistionalNONE_monathlyStats() {
		List<Value> valueOrder = new LinkedList<>();
		Value notProvisional = createSample("2017-03-01","12.21");
		valueOrder.add( notProvisional );
		fillMarchData(valueOrder);
		List<Value> monthSamples = new ArrayList<Value>(valueOrder);
		StatisticsCalculator.sortByDateOrder(monthSamples);
//		stats.overallStats(monthSamples, valueOrder, MediationType.AboveDatum);

		Map<String, String> stat = stats.monthlyStats(monthSamples).get("3"); //MediationType.AboveDatum

//		assertNotNull(stat);
		assertEquals("4.12", stat.get(P50_MIN));
		assertEquals("4.60", stat.get(P10));
		assertEquals("6.5",  stat.get(P25));
		assertEquals("8.0",  stat.get(P50));
		assertEquals("9.25", stat.get(P75));
		assertEquals("11.36",stat.get(P90));
		assertEquals("12.21",stat.get(P50_MAX));
	}
	protected void fillMarchData(List<Value> monthSamples) {
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
	public void test_ensureThatMutlipleSamplesInOneYearForGivenMonthAreAveraged_BeforeStatsCalc() {
		// This is data as it was from the database on 03/25/2017 and in order to preserve a testable
		// state this recreates that data set for the April data only for USGS:394829074053502
		List<Value> monthSamples = new LinkedList<>();
		fillAprilData(monthSamples);

		Map<String,Integer>yearCounts = new HashMap<>();
		for (Value sample : monthSamples) {
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
		Value sample1 = monthSamples.get(1);
		Value sample7 = monthSamples.get(7);
		// 1968 samples
		Value sample17 = monthSamples.get(17);
		Value sample18 = monthSamples.get(18);

		// first assert what we have
		assertEquals("We expect that this sample has two years with multiple samples", 2, yearCountsGreaterThan1);
		// then check that averages removes them and that the new values are correct

		int preCount = monthSamples.size();
		List<Value> normalizeMutlipleYearlyValues = stats.normalizeMutlipleYearlyValues(monthSamples, stats.sortFunctionByQualifier());

		assertEquals("normalize should have removed two values, one from each of two years", preCount-2, normalizeMutlipleYearlyValues.size());

		// these should be removed
		assertFalse("values should have been removed", monthSamples.contains(sample1));
		assertFalse("values should have been removed", monthSamples.contains(sample7));
		assertFalse("values should have been removed", monthSamples.contains(sample17));
		assertFalse("values should have been removed", monthSamples.contains(sample18));

		// find the new samples for 1968 and 2003
		Value sample1968 = null;
		Value sample2003 = null;

		for (Value sample : normalizeMutlipleYearlyValues) {
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

		Map<String, Map<String, String>> april = stats.monthlyStats(monthSamples);

		System.err.println(april);

	}
	protected void fillAprilData(List<Value> monthSamples) {
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
}
