package gov.usgs.wma.statistics.control;

import static gov.usgs.ngwmn.logic.WaterLevelStatistics.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import gov.usgs.ngwmn.model.WLSample;
import gov.usgs.wma.statistics.control.StatsService;

public class StatsServiceTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(StatsServiceTest.class);
			
	@Test
	public void test_parseData_oneFineDatum() {
		StatsService stats = new StatsService();

		String data = "1999/01/01,1.00";
		
		List<WLSample> parsed = stats.parseData(data);
		
		assertEquals(1, parsed.size());
		assertEquals("1999/01/01", parsed.get(0).time);
		assertEquals(new BigDecimal("1.00"), parsed.get(0).value);
	}

	@Test
	public void test_parseData_twoFineData() {
		StatsService stats = new StatsService();

		String data = "1999/01/01,1.00\n1999/01/02,2.00";
		
		List<WLSample> parsed = stats.parseData(data);
		
		assertEquals(2, parsed.size());
		assertEquals("1999/01/02", parsed.get(1).time);
		assertEquals(new BigDecimal("2.00"), parsed.get(1).value);
	}

	@Test
	public void test_parseData_twoWindowsData() {
		StatsService stats = new StatsService();

		String data = "1999/01/01,1.00\r\n1999/01/02,2.00";
		
		List<WLSample> parsed = stats.parseData(data);
		
		assertEquals(2, parsed.size());
		assertEquals("1999/01/02", parsed.get(1).time);
		assertEquals(new BigDecimal("2.00"), parsed.get(1).value);
	}

	@Test
	public void test_parseData_twoWhitespaceData() {
		StatsService stats = new StatsService();

		String data = " 1999/01/01 , 1.00 \n\t1999/01/02\t,\t2.00\t";
		
		List<WLSample> parsed = stats.parseData(data);
		
		assertEquals(2, parsed.size());
		assertEquals("1999/01/02", parsed.get(1).time);
		assertEquals(new BigDecimal("2.00"), parsed.get(1).value);
	}

	@Test
	public void test_parseData_twoMissingRowData() {
		StatsService stats = new StatsService();

		String data = "1999/01/01,1.00 \n \n 1999/01/02,2.00";
		
		List<WLSample> parsed = stats.parseData(data);
		
		assertEquals(2, parsed.size());
		assertEquals("1999/01/02", parsed.get(1).time);
		assertEquals(new BigDecimal("2.00"), parsed.get(1).value);
	}
	
	@Test()
	public void test_parseData_twoMissingDateData() {
		StatsService stats = new StatsService();

		String data = ",1.00\n1999/01/02,2.00";
		
		try {
			stats.parseData(data);
		} catch (RuntimeException e) {
			assertTrue(e.getMessage().startsWith("The date must be valid"));
		}
	}
	
	@Test()
	public void test_parseData_twoBadDateData() {
		StatsService stats = new StatsService();

		String data = "199/01/01,1.00\n1999/01/02,2.00";
		
		try {
			stats.parseData(data);
		} catch (RuntimeException e) {
			assertTrue(e.getMessage().endsWith("199/01/01"));
		}
	}
	
	@Test()
	public void test_parseData_twoBadValueData() {
		StatsService stats = new StatsService();

		String data = "1999/01/01,1a.00\n1999/01/02,2.00";
		
		try {
			stats.parseData(data);
		} catch (RuntimeException e) {
			assertTrue(e.getMessage().endsWith("1a.00"));
		}
	}
	
	@Test
	public void test_service_twoFineData() {
		StatsService stats = new StatsService();

		String data = "1999/01/01,1.00\n1999/01/02,2.00";
		
		ResponseEntity<String> resp = stats.service(data);
		
		assertEquals(200, resp.getStatusCode().value());
		assertTrue( resp.toString().contains("200 OK") );
		assertTrue( resp.getBody().contains(MONTHLY_WARNING) );
		
		// checking for no hiddend 300, 400, or 500 status
		assertFalse( resp.getBody().contains("'status':3") );
		assertFalse( resp.getBody().contains("'status':4") );
		assertFalse( resp.getBody().contains("'status':5") );
	}
	
	@Test
	public void test_doesThisMonthQualifyForStats_hasTenYearsData() throws Exception {
		StatsService stats = new StatsService();

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
		
		ResponseEntity<String> resp = stats.service(data);
		LOGGER.trace(resp.getBody());
		
		assertEquals(200, resp.getStatusCode().value());
		assertTrue( resp.toString().contains("200 OK") );
		assertFalse( resp.getBody().contains(MONTHLY_WARNING) );
		
		// checking for no hiddend 300, 400, or 500 status
		assertFalse( resp.getBody().contains("'status':3") );
		assertFalse( resp.getBody().contains("'status':4") );
		assertFalse( resp.getBody().contains("'status':5") );

	}
}
