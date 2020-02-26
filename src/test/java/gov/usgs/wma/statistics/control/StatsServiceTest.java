package gov.usgs.wma.statistics.control;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.usgs.ngwmn.model.MediationType;
import gov.usgs.ngwmn.model.WLSample;
import gov.usgs.wma.statistics.app.Properties;
import gov.usgs.wma.statistics.app.SwaggerConfig;
import gov.usgs.wma.statistics.model.JsonData;
import gov.usgs.wma.statistics.model.JsonDataBuilder;
import gov.usgs.wma.statistics.model.Value;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = { "/application.properties" })
public class StatsServiceTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(StatsServiceTest.class);

	@Autowired
	Environment spring;
	
	StatsService stats;
	Properties env;
	JsonDataBuilder builder;

	@Before
	public void setup() {
		stats = new StatsService();
		env = new Properties().setEnvironment(spring);
		stats.env = env;
		builder = new JsonDataBuilder(env);
	}
	
	@Test
	public void test_parseData_oneFineDatum() {
		String data = "1999/01/01,1.00";
		
		List<WLSample> parsed = stats.validateAndParseCsvData(data, builder);
		
		assertEquals(1, parsed.size());
		assertEquals("1999/01/01", parsed.get(0).time);
		assertEquals(new BigDecimal("1.00"), parsed.get(0).value);
	}

	@Test
	public void test_parseData_twoFineData() {
		String data = "1999/01/01,1.00\n1999/01/02,2.00";
		
		List<WLSample> parsed = stats.validateAndParseCsvData(data, builder);
		
		assertEquals(2, parsed.size());
		assertEquals("1999/01/02", parsed.get(1).time);
		assertEquals(new BigDecimal("2.00"), parsed.get(1).value);
	}

	@Test
	public void test_parseData_twoWindowsData() {
		StatsService stats = new StatsService();

		String data = "1999/01/01,1.00\r\n1999/01/02,2.00";
		
		List<WLSample> parsed = stats.validateAndParseCsvData(data, builder);
		
		assertEquals(2, parsed.size());
		assertEquals("1999/01/02", parsed.get(1).time);
		assertEquals(new BigDecimal("2.00"), parsed.get(1).value);
	}

	@Test
	public void test_parseData_twoWhitespaceData() {
		String data = " 1999/01/01 , 1.00 \n\t1999/01/02\t,\t2.00\t";
		
		List<WLSample> parsed = stats.validateAndParseCsvData(data, builder);
		
		assertEquals(2, parsed.size());
		assertEquals("1999/01/02", parsed.get(1).time);
		assertEquals(new BigDecimal("2.00"), parsed.get(1).value);
	}

	@Test
	public void test_parseData_twoMissingRowData() {
		String data = "1999/01/01,1.00 \n \n 1999/01/02,2.00";
		
		List<WLSample> parsed = stats.validateAndParseCsvData(data, builder);
		
		assertEquals(2, parsed.size());
		assertEquals("1999/01/02", parsed.get(1).time);
		assertEquals(new BigDecimal("2.00"), parsed.get(1).value);
	}
	
	@Test()
	public void test_parseData_twoMissingDateData() {
		String data = ",1.00\n1999/01/02,2.00";
		
		try {
			stats.validateAndParseCsvData(data, builder);
		} catch (RuntimeException e) {
			assertTrue(e.getMessage().startsWith("The date must be valid"));
		}
	}
	
	@Test()
	public void test_parseData_twoBadDateData() {
		String data = "199/01/01,1.00\n1999/01/02,2.00";
		
		try {
			stats.validateAndParseCsvData(data, builder);
		} catch (RuntimeException e) {
			assertTrue(e.getMessage().endsWith("199/01/01"));
		}
	}
	
	@Test()
	public void test_parseData_twoBadValueData() {
		String data = "1999/01/01,1a.00\n1999/01/02,2.00";
		
		try {
			stats.validateAndParseCsvData(data, builder);
		} catch (RuntimeException e) {
			assertTrue(e.getMessage().endsWith("1a.00"));
		}
	}

	@Test
	public void test_parseData_empty() {
		String data = "";
		List<WLSample> parsed = stats.validateAndParseCsvData(data, builder);
		assertEquals(0, parsed.size());
	}

	@Test
	public void test_parseData_comment() {
		String data = "1999/01/01,1.00\r\n#1999/01/02,2.00";
		List<WLSample> parsed = stats.validateAndParseCsvData(data, builder);
		assertEquals(1, parsed.size());
	}
	
	@Test
	public void test_parseData_tooFewColumns() {
		// the second data row has no commas
		String data = "1999/01/01,1.00\r\n1999/01/022.00";
		stats.validateAndParseCsvData(data, builder);
		JsonData pojo = builder.build();
		assertTrue(pojo.hasErrors());
	}
	
	@Test
	public void test_parseData_tooManyColumns() {
		// the second data row has no commas
		String data = "1999/01/01,1.00\r\n1999/01/0,22.00,a,a";
		stats.validateAndParseCsvData(data, builder);
		JsonData pojo = builder.build();
		assertTrue(pojo.hasErrors());
	}
		
	@Test()
	public void test_parseData_provisional() {
		// the second data row has no commas
		String data = "1999/01/01,1.00,P";
		Value value = stats.validateAndParseCsvData(data, builder).get(0);
		assertTrue(value.isProvisional());
	}

	@Test
	public void test_parseData_blankRow() {
		String data = "1999/01/01,1.00\r\n\r\n1999/01/02,2.00";
		List<WLSample> parsed = stats.validateAndParseCsvData(data, builder);
		assertEquals(2, parsed.size());
	}
	

	
	@Test
	public void test_service_twoFineData() throws Exception {
		String data = "1999/01/01,1.00\n1999/01/02,2.00";
		
		JsonData pojo = stats.calculate(data, MediationType.ASCENDING.toString(),
				SwaggerConfig.BOOLEAN_FALSE, SwaggerConfig.BOOLEAN_TRUE,
				SwaggerConfig.StatsService_PERCENTILES_DEFAULT);
		
		assertTrue( pojo.isOk() );
		assertFalse( pojo.hasErrors() );
		
		String json = new ObjectMapper().writeValueAsString(pojo);

		int msgIndex = json.indexOf("messages");
		String msgs = json.substring(msgIndex);
		assertTrue( msgs.contains("Too few data values for monthly statistics") );
	}
	
	@Test
	public void test_doesThisMonthQualifyForStats_hasTenYearsData() throws Exception {
		String data =
				"2005-06-10T04:15:00-05:00, 1.0\n"+
				"2006-06-10T04:15:00-05:00, 2.0\n"+
				"2007-06-10T04:15:00-05:00, 1.0\n"+
				"2008-06-10T04:15:00-05:00, 2.0\n"+
				"2009-06-10T04:15:00-05:00, 1.0\n"+
				"2010-06-10T04:15:00-05:00, 2.0\n"+
				"2011-06-10T04:15:00-05:00, 1.0\n"+
				"2012-06-10T04:15:00-05:00, 2.0\n"+
				"2013-06-10T04:15:00-05:00, 1.0\n"+
				"2014-06-10T04:15:00-05:00, 1.0\n"+
				"2015-06-10T04:15:00-05:00, 1.0\n"+
				"2016-06-10T04:15:00-05:00, 1.0\n"+
				"2017-06-10T04:15:00-05:00, 1.0\n"+
				"2018-06-10T04:15:00-05:00, 1.0\n";
		
		JsonData pojo = stats.calculate(data, MediationType.ASCENDING.toString(), SwaggerConfig.BOOLEAN_FALSE, SwaggerConfig.BOOLEAN_TRUE, SwaggerConfig.StatsService_PERCENTILES_DEFAULT);
		assertTrue( pojo.isOk() );
		assertFalse( pojo.hasErrors() );
		
		String json = new ObjectMapper().writeValueAsString(pojo);
		LOGGER.trace(json);
		
		int msgIndex = json.indexOf("messages");
		String msgs = json.substring(msgIndex);
		assertFalse( msgs.contains("Too few data values for monthly statistics") );
	}
	
	@Test
	public void test_doesThisMonthQualifyForStats_almostTenYearsData() throws Exception {
		String data =
				"2009-06-10T04:15:00-05:00, 1.0\n"+
				"2010-06-10T04:15:00-05:00, 2.0\n"+
				"2011-06-10T04:15:00-05:00, 1.0\n"+
				"2012-06-10T04:15:00-05:00, 2.0\n"+
				"2013-06-10T04:15:00-05:00, 1.0\n"+
				"2014-06-10T04:15:00-05:00, 1.0\n"+
				"2015-06-10T04:15:00-05:00, 1.0\n"+
				"2016-06-10T04:15:00-05:00, 1.0\n"+
				"2017-06-10T04:15:00-05:00, 1.0\n"+
				"2018-06-10T04:15:00-05:00, 1.0\n"+
				"2005-05-10T04:15:00-05:00, 2.0\n"+
				"2011-05-10T04:15:00-05:00, 1.0\n"+
				"2012-05-10T04:15:00-05:00, 2.0\n"+
				"2013-05-10T04:15:00-05:00, 1.0\n"+
				"2014-05-10T04:15:00-05:00, 1.0\n"+
				"2015-05-10T04:15:00-05:00, 1.0\n"+
				"2016-05-10T04:15:00-05:00, 1.0\n"+
				"2017-05-10T04:15:00-05:00, 1.0\n"+
				"2018-05-10T04:15:00-05:00, 1.0\n";
		
		JsonData pojo = stats.calculate(data, MediationType.ASCENDING.toString(), SwaggerConfig.BOOLEAN_FALSE, SwaggerConfig.BOOLEAN_TRUE, SwaggerConfig.StatsService_PERCENTILES_DEFAULT);
		assertTrue( pojo.isOk() );
		assertFalse( pojo.hasErrors() );
		
		String json = new ObjectMapper().writeValueAsString(pojo);
		LOGGER.trace(json);
		
		int msgIndex = json.indexOf("messages");
		String msgs = json.substring(msgIndex);
		assertTrue( msgs.contains("The month of May requires 1 more year") );
		assertFalse( msgs.contains("Too few data values for monthly statistics") );
	}
}

