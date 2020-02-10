package gov.usgs.wma.statistics.logic;

import static gov.usgs.wma.statistics.model.JsonDataBuilder.*;
import static gov.usgs.wma.statistics.model.Value.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.usgs.wma.statistics.app.Properties;
import gov.usgs.wma.statistics.model.JsonData;
import gov.usgs.wma.statistics.model.JsonDataBuilder;
import gov.usgs.wma.statistics.model.Value;

@RunWith(SpringJUnit4ClassRunner.class)
public class OverallStatisticsTest {
	@Mock
	Environment spring;
	Properties env;
	JsonDataBuilder builder;
	OverallStatistics<Value> stats;

	@Before
	public void setup() {
		env = new Properties().setEnvironment(spring);
		builder = new JsonDataBuilder(env);
		stats = new OverallStatistics<>(env, builder);
	}
	
	private Value createSample(String time, String value) {
		BigDecimal val = null;
		if (null != value) {
			val = new BigDecimal(value);
		}
		return new Value(time, val);
	}


	@Test
	public void test_overallStats_empty() {
		List<Value> empty = new LinkedList<>();
		stats.overallStats(empty, empty);
		assertEquals(null, builder.get(LATEST_PCTILE));
	}
	
	@Test
	public void test_overallStats_null() {
		stats.overallStats(null, null);
		assertEquals(null, builder.get(LATEST_PCTILE));
	}
	
	
	@Test
	public void test_findMinMaxDatesAndDateRange() {
		List<Value> valueOrder = new LinkedList<>();
		fillMarchData(valueOrder);
		List<Value> monthSamples = new ArrayList<Value>(valueOrder);
		StatisticsCalculator.sortByDateOrder(monthSamples);

		stats.findMinMaxDatesAndDateRange(monthSamples, valueOrder);
		
		assertEquals("2006-03-01", builder.get(MIN_DATE));
		assertEquals("2017-03-01", builder.get(MAX_DATE));
		assertEquals("9.17", builder.get(LATEST_VALUE));
		assertEquals("11.0", builder.get(RECORD_YEARS));
	}
	
	@Test
	public void test_overallStats() {
		List<Value> valueOrder = new LinkedList<>();
		fillMarchData(valueOrder);
		StatisticsCalculator.sortByValueOrderAscending(valueOrder);
		List<Value> monthSamples = new ArrayList<Value>(valueOrder);
		StatisticsCalculator.sortByDateOrder(monthSamples);

		stats.overallStats(monthSamples, valueOrder);
		
		assertEquals("2006-03-01", builder.get(MIN_DATE));
		assertEquals("2017-03-01", builder.get(MAX_DATE));
		assertEquals("9.17", builder.get(LATEST_VALUE));
		
		// value range
		assertEquals("1.39", builder.get(MIN_VALUE));
		assertEquals("9.44", builder.get(MAX_VALUE));
		// number of samples
		assertEquals("332", builder.get(SAMPLE_COUNT));
		assertEquals("11.0", builder.get(RECORD_YEARS));
		// percentile statistics
		assertEquals("7.98", builder.get(MEDIAN));
		
		JsonData data = builder.build();
		assertEquals(DATE_FORMAT_FULL.format(new Date()), data.getOverall().dateCalc);
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
		monthSamples.add( createSample("2017-03-01","9.17") ); // the date was changed here to ensure most recent
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
}
