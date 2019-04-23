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
	
	Reader xmlReader;
	Map<String,String> expected;

	@Before
	public void before() {
		spec     = new Specifier();
		env      = new Properties().setEnvironment(spring);
		builder  = new JsonDataBuilder(env);
		stats    = new WaterLevelStatistics(env, builder);
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
		expected.put("latestPercentile", "0.250000000");
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
		expected.put("latestValue", "146.600000");
		expected.put("latestPercentile", "0");
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
		expected.put("latestPercentile", "1");
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
		expected.put("latestPercentile", "1");
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
		expected.put("latestPercentile", "0.786");
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
		expected.put("latestPercentile", "0.6000");
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
		expected.put("latestPercentile", "0.357");
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

		// this is not really dead code. it shows how to examine data after XML extraction
//		samples.stream()
//			.sorted((a,b) -> b.value.compareTo(a.value))
////			.sorted((a,b) -> a.valueAboveDatum.compareTo(b.valueAboveDatum))
//			.filter(sample -> !sample.originalValue.equals(sample.valueAboveDatum))
////			.filter(sample -> sample.isProvisional()) // there are no provision values in this data set
//			.map(sample ->  String.format("value:%6s above:%6s below:%6s time:%s", sample.originalValue,sample.valueAboveDatum,sample.value,sample.time) )
//			.forEach(System.err::println);
		
		StatsService service  = new StatsService().setProperties(env);
		String mediation      = MediationType.BelowLand.toString(); // override from AboveDatum 
		String percentiles    = StatsService_PERCENTILES_DEFAULT;
		String includeMedians = StatsService_MEDIANS_DEFAULT;
		String data           = samples.stream()           // the service receives its data from the web as text
								.map(sample -> sample.toCSV())
								.collect(Collectors.joining("\n"));
		// TEST
		JsonData json = service.calculate(builder, data, mediation, includeMedians, percentiles);
		
		// EXPECT
		expected.put("latestPercentile", "0.36");
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

}
