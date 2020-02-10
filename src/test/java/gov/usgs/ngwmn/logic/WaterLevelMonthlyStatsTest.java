package gov.usgs.ngwmn.logic;

import gov.usgs.ngwmn.model.MediationType;
import gov.usgs.ngwmn.model.WLSample;
import gov.usgs.wma.statistics.app.Properties;
import gov.usgs.wma.statistics.logic.StatisticsCalculator;
import gov.usgs.wma.statistics.model.JsonDataBuilder;
import gov.usgs.wma.statistics.model.JsonMonthly;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
public class WaterLevelMonthlyStatsTest {
	public static final String P10 = "P10";
	public static final String P25 = "P25";
	public static final String P50 = "P50";
	public static final String P75 = "P75";
	public static final String P90 = "P90";

	@Mock
	Environment spring;
	Properties env;
	private JsonDataBuilder builder;


	@Before
	public void setup() {
		env = new Properties().setEnvironment(spring);
		builder = new JsonDataBuilder(env);
	}
	
	private WLSample createSample(String time, String value) {
		BigDecimal val = null;
		if (null!=value) {
			val = new BigDecimal(value);
		}
		return new WLSample(time, val, "units", val, "comment", true, "pcode", null);
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
		boolean qualifies = new WaterLevelMonthlyStats(env, builder)
				.doesThisMonthQualifyForStats(monthSamples);
		assertTrue("A month with 10 unique years of data is valid.", qualifies);
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
		boolean qualifies = new WaterLevelMonthlyStats(env, builder)
				.doesThisMonthQualifyForStats(monthSamples);
		assertTrue("A month with 10 unique years of data is valid.", qualifies);
	}
	
	@Test
	public void test_doesThisMonthQualifyForStats_notEngoughData() throws Exception {
		List<WLSample> monthSamples = new ArrayList<>(2);
		monthSamples.add(createSample("2005-06-10T04:15:00-05:00", "1.0"));
		monthSamples.add(createSample("2015-06-10T04:15:00-05:00", "2.0"));

		builder.mediation(MediationType.AboveDatum);
		boolean qualifies = new WaterLevelMonthlyStats(env, builder)
				.doesThisMonthQualifyForStats(monthSamples);
		assertFalse("A month must have 10 unique years of data not just a 10 yr date range.", qualifies);

		qualifies = new WaterLevelMonthlyStats(env, builder)
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
		boolean qualifies = new WaterLevelMonthlyStats(env, builder)
				.doesThisMonthQualifyForStats(monthSamples);
		assertFalse("A month must have 10 unique years of data not just a 10 yr date range.", qualifies);
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
		WaterLevelMonthlyStats mockstats = new WaterLevelMonthlyStats(env, builder) {
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
