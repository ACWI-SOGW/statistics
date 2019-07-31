package gov.usgs.ngwmn.logic;

import static gov.usgs.wma.statistics.app.SwaggerConfig.*;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
//import org.mockito.stubbing.Answer;
//import org.mockito.invocation.InvocationOnMock;
//import org.mockito.stubbing.Answer;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.usgs.ngwmn.model.Elevation;
import gov.usgs.ngwmn.model.MediationType;
import gov.usgs.ngwmn.model.Specifier;
import gov.usgs.ngwmn.model.WLSample;
import gov.usgs.wma.statistics.app.Properties;
import gov.usgs.wma.statistics.control.StatsService;
import gov.usgs.wma.statistics.model.JsonData;
import gov.usgs.wma.statistics.model.JsonDataBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = { "/application.properties" })
public class WaterLevelXmlTest {

	private static final BigDecimal ZERO = new BigDecimal("0.00");
	
	@Autowired
	Environment spring;
	Specifier spec;
	Properties env;
	JsonDataBuilder builder;
	WaterLevelStatistics stats;
	Elevation elevation;
	boolean enforceRecent;
	
	Reader xmlReader;
	Map<String,String> expected;

	@Before
	public void before() {
		spec     = new Specifier();
		env      = new Properties().setEnvironment(spring);
		builder  = new JsonDataBuilder(env);
		enforceRecent = true;
		stats    = new WaterLevelStatistics(env, builder, enforceRecent);
		expected = new HashMap<>();
	}

	// datum is the code used to identify mediation base, mostly for above datum
	private void setup(String agencyCd, String siteNo, BigDecimal altVal, String datum) {
		// altVal is the database column name for altitude value.
		elevation = new Elevation(altVal, datum);
		spec = new Specifier(agencyCd, siteNo).setElevation(elevation);
		setupReader();
	}
	
	private void setupReader() {
		String agencyCd = spec.getAgencyCd();
		String siteNo   = spec.getSiteNo();
		String filename = "/sample-data/"+agencyCd+"_"+siteNo+"_WATERLEVEL.xml";
		InputStream rin = getClass().getResourceAsStream(filename);
		xmlReader   = new BufferedReader(new InputStreamReader(rin));
	}
	
	private void commonAssertions(JsonData json) {
		assertNotNull("JSON data must be returned", json);
		assertNotNull("expected overall stats in JSON data", json.getOverall());
		assertNotNull("expected monthly stats in JSON data", json.getMonthly());
		
		String valueMin = expected.get("valueMin");
		assertEquals("min value should be ", valueMin,json.getOverall().valueMin);
		String valueMax = expected.get("valueMax");
		assertEquals("max value should be ", valueMax,json.getOverall().valueMax);
		
		String latestValue = expected.get("latestValue");
		assertEquals("lastest value should be "+latestValue, latestValue,json.getOverall().latestValue);
		String latestPct = expected.get("latestPercentile");
		assertEquals("lastest percentile should be ", latestPct,json.getOverall().latestPercentile);
	}
	
	@Test
	public void test_MBMG_3002_BelowLand() throws Exception {
		// the latest sample is the second last index entry and should NOT be 100
		
		// SETUP
		setup("MBMG","3002", ZERO,"BLS");
		
		// EXPECT
		expected.put("latestValue", "200.390000");
		expected.put("latestPercentile", "25.0000000");
		expected.put("valueMin", "209.790000");
		expected.put("valueMax", "180.620000");
		
		// TEST
		JsonData json = stats.calculate(spec, xmlReader);
		
		// ASSERT
		commonAssertions(json);
	}
	@Test
	public void test_MBMG_73642_BelowLand() throws Exception {
		// the latest sample is the largest value and should be 100%
		
		// SETUP
		setup("MBMG","73642", ZERO,"BLS");
		
		// EXPECT
		// until MBMG has appropriate sigfigs, this is the precision we expect.
		expected.put("latestValue", "146.600000");
		expected.put("latestPercentile", ""); // latest values is in month 10 with only 6 other years
		expected.put("valueMin", "150.790000");
		expected.put("valueMax", "135.760000");
		
		// TEST
		JsonData json = stats.calculate(spec, xmlReader);
		
		// ASSERT
		commonAssertions(json);
	}
	@Test
	public void test_MBMG_122340_BelowLand() throws Exception {
		// the latest sample is the number 2 index entry and should NOT be zero
		
		// SETUP
		setup("MBMG","122340", ZERO,"BLS");
		
		// EXPECT
		expected.put("latestValue", "17.960000");
		expected.put("latestPercentile", "100");
		expected.put("valueMin", "19.760000");
		expected.put("valueMax", "10.380000");
		
		// TEST
		JsonData json = stats.calculate(spec, xmlReader);
		
		// ASSERT
		commonAssertions(json);
	}
	@Test
	public void test_MN_DNR_200105_BelowLand() throws Exception {
		// the latest sample is the least value and should be 0%

		// SETUP
		setup("MN_DNR","200105", ZERO,"BLS");
		
		// EXPECT
		expected.put("latestValue", "98.29");
		expected.put("latestPercentile", "100");
		expected.put("valueMin", "115.97");
		expected.put("valueMax", "98.29");
		
		// TEST
		JsonData json = stats.calculate(spec, xmlReader);
		
		// ASSERT
		commonAssertions(json);
	}
	@Test
	public void test_USGS_401229074290001_BelowLand() throws Exception {
		// SETUP
		setup("USGS","401229074290001", ZERO,"BLS");
		
		// EXPECT
		expected.put("latestValue", "4.99");
		expected.put("latestPercentile", "78.6");
		expected.put("valueMin", "12.66");
		expected.put("valueMax", "1.06");
		
		// TEST
		JsonData json = stats.calculate(spec, xmlReader);
		
		// ASSERT
		commonAssertions(json);
	}
	@Test
	public void test_USGS_430427089284901_BelowLand() throws Exception {
		// the latest sample should be ~31% for the month rather than ~20% if compared to the entire data set

		// SETUP
		setup("USGS","430427089284901", ZERO,"BLS");
		
		// EXPECT
		expected.put("latestValue", "49.80");
		expected.put("latestPercentile", "60.00");
		expected.put("valueMin", "58.82");
		expected.put("valueMax", "45.15");
		
		// TEST
		JsonData json = stats.calculate(spec, xmlReader);
		
		// ASSERT
		commonAssertions(json);
	}

	@Test
	public void test_insertAtEndOfList() {
		// SETUP
		List<String> list = new ArrayList<>();
		list.add("a string");
		list.add(0,"another String");
		// TEST
		list.add(list.size(), "insert at end");
		assertEquals("testing to ensure can insert at one greater than length", 3, list.size());
	}
	
	@Test
	public void test_USGS_405010073414901_AboveDatum() throws Exception {
		// this test ensures that loading a site mediated above a datum evaluates its overall statistics correctly
		// SETUP
		setup("USGS","405010073414901", new BigDecimal("13.6"),"NGVD29");
		
		// EXPECT
		expected.put("latestValue", "5.48");
		expected.put("latestPercentile", "35.7");
		expected.put("valueMin", "-16.15");
		expected.put("valueMax",  "10.09");
//		expected.put("valueMax",  "19.93"); // this is a BL measurement with a AD site
		
		// TEST
		JsonData json = stats.calculate(spec, xmlReader);
		
		// ASSERT
		commonAssertions(json);
	}
	
	@Test
	public void test_USGS_405010073414901_AboveDatum_forcedBelowLand() throws Exception {
		// this is testing that a site mediated above a datum can be re-mediated below land using the elevation
		// SETUP
		MathContext precisionRound2 = new MathContext(2, RoundingMode.HALF_UP);
		MathContext precisionRound3 = new MathContext(3, RoundingMode.HALF_UP);
		BigDecimal altVal = new BigDecimal("13.6");        // specify the site and its actual elevation
		setup("USGS","405010073414901", altVal,"NGVD29");
		
		// extract the samples from the XML files as the ngwmn-cache project does
		List<WLSample> samples = WLSample.extractSamples(xmlReader, spec.getAgencyCd(), spec.getSiteNo(), spec.getElevation());
		System.err.println(samples.get(samples.size()-1)); // examine the most recent sample

		// this is really NOT dead code. it shows how to examine data after XML extraction
//		samples.stream()
//			.sorted((a,b) -> b.value.compareTo(a.value))
////			.sorted((a,b) -> a.valueAboveDatum.compareTo(b.valueAboveDatum))
//			.filter(sample -> !sample.originalValue.equals(sample.valueAboveDatum))
////			.filter(sample -> sample.isProvisional()) // there are no provision values in this data set
//			.map(sample ->  String.format("value:%6s above:%6s below:%6s time:%s", sample.originalValue,sample.valueAboveDatum,sample.value,sample.time) )
//			.forEach(System.err::println);
		
		StatsService service  = new StatsService().setProperties(env);
		String mediation      = MediationType.BelowLand.toString(); // override from AboveDatum 
		String enforceRecentTrue = "true";
		String percentiles    = StatsService_PERCENTILES_DEFAULT;
		String includeMedians = StatsService_MEDIANS_DEFAULT;
		String data           = samples.stream()           // the service receives its data from the web as text
								.map(sample -> sample.toCSV())
								.collect(Collectors.joining("\n"));
		// TEST
		JsonData json = service.calculate(builder, data, mediation, includeMedians, enforceRecentTrue, percentiles);
		
		// EXPECT
		expected.put("latestPercentile", "36");
		expected.put("latestValue", new BigDecimal("-5.48").add(altVal).round(precisionRound2).toPlainString());
		expected.put("valueMin",    new BigDecimal("16.15").add(altVal).round(precisionRound3).toPlainString());
		expected.put("valueMax",    new BigDecimal("-10.09").add(altVal).round(precisionRound2).toPlainString());
		// 19.93 is the largest absolute magnitude sample value. However, it is measured BelowLand not AboveDatum
		// like the majority of samples. Therefore, it must be subtracted from the surface elevation to get the 
		// water level elevation: 13.6 - 19.93 = -6.33 and this is not the lowest measurement.
		// 
		
		// ASSERT
		commonAssertions(json);
		
		// validate the 36% - yes, both mediation direction now product the "same" percentile
		// validate 10.09 vs 19.93 as max value - 10.09 is correct because 19.93 is BLS
		// TODO possibly convert overall to be base on all monthly median data -- this will be a new story
	}

	@Test
	public void test_lowValuesWithBadDatum() throws Exception {
		// this test ensures that loading a site mediated above a datum evaluates its overall statistics correctly
		// SETUP
		setup("USGS","405010073414901", new BigDecimal("13.6"),"BadDatum");
		
		// EXPECT
		expected.put("latestValue", "5.48");
		expected.put("latestPercentile", "0.357");
		expected.put("valueMin", "-16.15");
		expected.put("valueMax",  "10.09");
		
		// measurements for an AD site that is not properly mediated with bad datum
		// the values get all messed up, so few values are retained that 
		expected.put("valueMax",  "9.30"); // this is the max BL retained sample
		expected.put("valueMin",  "19.93"); // this is the min BL retained sample
		expected.put("latestValue", "11.47"); // this is the latest retained sample
		expected.put("latestPercentile", ""); // the latest value is for month 12 w/ only three other samples
		
		// TEST
		JsonData json = stats.calculate(spec, xmlReader);
		
		// ASSERT
		commonAssertions(json);
		
		// EXPLANATION
		// All the AboveDatum samples are dropped because there is no valid datum for mediation
		// The few values that are left are all BelowLand 
		
		// The most recent value remaining is 11.47 on 1996-12-20T10:25:00-05:00
		// The most smallest value (most water) remaining is 9.30 on 1983-05-25T12:00:00
		// The most largest value (least water) remaining is 19.93 on 1996-08-28T13:45:00-05:00
		
		// The percentile of 11.47 is a bit more complicated, here are the values remaining for the same month
		//		value: 12.00 above:  null below: 12.00 time:1995-12-08T10:40:00-05:00
		//		value: 11.74 above:  null below: 11.74 time:1996-12-04T12:25:00-05:00
		//		value: 11.47 above:  null below: 11.47 time:1996-12-20T10:25:00-05:00
		//		value: 11.40 above:  null below: 11.40 time:1982-12-27T12:00:00
		// Then the two values in 1996 must be resolved to a median; however, there are only two - they average.
		//		value: 12.00 above:  null below: 12.00 time:1995-12-08T10:40:00-05:00
		//		value: 11.60 above:  null below: 11.60 time:1996-12-04T12:25:00-05:00
		//		value: 11.40 above:  null below: 11.40 time:1982-12-27T12:00:00
		// Since it happens that the most recent remaining value is the year 1996, the average really did not matter 
		// The most recent value replaces the month year
		//		value: 12.00 above:  null below: 12.00 time:1995-12-08T10:40:00-05:00
		//		value: 11.47 above:  null below: 11.47 time:1996-12-20T10:25:00-05:00
		//		value: 11.40 above:  null below: 11.40 time:1982-12-27T12:00:00
		// Now the most recent value index is exactly in the middle of all values
		
		// These are the only values mediated and it appears they are the few in the dataset that are below land.
		//		value: 19.93 above:  null below: 19.93 time:1996-08-28T13:45:00-05:00
		//		value: 18.82 above:  null below: 18.82 time:1995-09-13T10:35:00-05:00
		//		value: 18.72 above:  null below: 18.72 time:1996-08-14T13:55:00-05:00
		//		value: 18.35 above:  null below: 18.35 time:1983-08-24T12:00:00
		//		value: 18.23 above:  null below: 18.23 time:1983-07-25T12:00:00
		//		value: 17.67 above:  null below: 17.67 time:1996-08-01T12:05:00-05:00
		//		value: 17.61 above:  null below: 17.61 time:1996-07-17T09:00:00-05:00
		//		value: 17.47 above:  null below: 17.47 time:1996-06-19T09:10:00-05:00
		//		value: 17.43 above:  null below: 17.43 time:1983-06-22T12:00:00
		//		value: 17.33 above:  null below: 17.33 time:1983-09-23T12:00:00
		//		value: 16.20 above:  null below: 16.20 time:1996-06-14T08:00:00-05:00
		//		value: 15.79 above:  null below: 15.79 time:1996-07-03T15:40:00-05:00
		//		value: 15.51 above:  null below: 15.51 time:1996-09-26T12:47:00-05:00
		//		value: 14.51 above:  null below: 14.51 time:1984-06-29T13:15:00-05:00
		//		value: 14.34 above:  null below: 14.34 time:1984-06-08T20:10:00-05:00
		//		value: 13.83 above:  null below: 13.83 time:1996-10-10T14:20:00-05:00
		//		value: 13.64 above:  null below: 13.64 time:1996-10-24T11:50:00-05:00
		//		value: 13.46 above:  null below: 13.46 time:1982-10-22T12:00:00
		//		value: 13.45 above:  null below: 13.45 time:1995-09-29T13:10:00-05:00
		//		value: 13.44 above:  null below: 13.44 time:1995-10-12T13:55:00-05:00
		//		value: 13.40 above:  null below: 13.40 time:1996-05-23T09:10:00-05:00
		//		value: 13.18 above:  null below: 13.18 time:1996-11-07T15:55:00-05:00
		//		value: 12.75 above:  null below: 12.75 time:1996-04-12T11:50:00-05:00
		//		value: 12.73 above:  null below: 12.73 time:1983-10-26T08:10:00-05:00
		//		value: 12.32 above:  null below: 12.32 time:1995-11-09T15:15:00-05:00
		//		value: 12.29 above:  null below: 12.29 time:1996-05-10T14:30:00-05:00
		//		value: 12.28 above:  null below: 12.28 time:1995-10-26T12:30:00-05:00
		//		value: 12.22 above:  null below: 12.22 time:1996-03-28T09:40:00-05:00
		//		value: 12.15 above:  null below: 12.15 time:1996-01-18T12:45:00-05:00
		//		value: 12.15 above:  null below: 12.15 time:1996-01-31T09:30:00-05:00
		//		value: 12.06 above:  null below: 12.06 time:1996-02-14T12:00:00-05:00
		//		value: 12.04 above:  null below: 12.04 time:1996-03-13T11:00:00-05:00
		//		value: 12.00 above:  null below: 12.00 time:1995-12-08T10:40:00-05:00
		//		value: 11.93 above:  null below: 11.93 time:1996-02-27T10:10:00-05:00
		//		value: 11.92 above:  null below: 11.92 time:1996-01-04T13:15:00-05:00
		//		value: 11.76 above:  null below: 11.76 time:1996-04-25T12:05:00-05:00
		//		value: 11.74 above:  null below: 11.74 time:1996-12-04T12:25:00-05:00
		//		value: 11.59 above:  null below: 11.59 time:1996-11-21T13:00:00-05:00
		//		value: 11.49 above:  null below: 11.49 time:1982-11-23T12:00:00
		//		value: 11.47 above:  null below: 11.47 time:1996-12-20T10:25:00-05:00
		//		value: 11.40 above:  null below: 11.40 time:1982-12-27T12:00:00
		//		value: 11.07 above:  null below: 11.07 time:1995-11-22T10:25:00-05:00
		//		value: 10.80 above:  null below: 10.80 time:1983-01-25T12:00:00
		//		value: 10.58 above:  null below: 10.58 time:1983-04-26T12:00:00
		//		value: 10.14 above:  null below: 10.14 time:1983-03-24T12:00:00
		//		value:  9.94 above:  null below:  9.94 time:1983-02-24T12:00:00
		//		value:  9.30 above:  null below:  9.30 time:1983-05-25T12:00:00
	}
}
