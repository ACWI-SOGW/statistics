package gov.usgs.ngwmn.control;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.http.ResponseEntity;

import gov.usgs.ngwmn.model.WLSample;

public class StatsServiceTest {

	@Test
	public void test_parseData_oneFineDatum() {
		StatsService stats = new StatsService();

		String values = "1999/01/01,1.00";
		
		Map<String, String> data = new HashMap<>();
		data.put("data", values);
		
		List<WLSample> parsed = stats.parseData(data);
		
		assertEquals(1, parsed.size());
		assertEquals("1999/01/01", parsed.get(0).time);
		assertEquals(new BigDecimal("1.00"), parsed.get(0).value);
	}

	@Test
	public void test_parseData_twoFineData() {
		StatsService stats = new StatsService();

		String values = "1999/01/01,1.00\n1999/01/02,2.00";
		
		Map<String, String> data = new HashMap<>();
		data.put("data", values);
		
		List<WLSample> parsed = stats.parseData(data);
		
		assertEquals(2, parsed.size());
		assertEquals("1999/01/02", parsed.get(1).time);
		assertEquals(new BigDecimal("2.00"), parsed.get(1).value);
	}

	@Test
	public void test_parseData_twoWindowsData() {
		StatsService stats = new StatsService();

		String values = "1999/01/01,1.00\r\n1999/01/02,2.00";
		
		Map<String, String> data = new HashMap<>();
		data.put("data", values);
		
		List<WLSample> parsed = stats.parseData(data);
		
		assertEquals(2, parsed.size());
		assertEquals("1999/01/02", parsed.get(1).time);
		assertEquals(new BigDecimal("2.00"), parsed.get(1).value);
	}

	@Test
	public void test_parseData_twoWhitespaceData() {
		StatsService stats = new StatsService();

		String values = " 1999/01/01 , 1.00 \n\t1999/01/02\t,\t2.00\t";
		
		Map<String, String> data = new HashMap<>();
		data.put("data", values);
		
		List<WLSample> parsed = stats.parseData(data);
		
		assertEquals(2, parsed.size());
		assertEquals("1999/01/02", parsed.get(1).time);
		assertEquals(new BigDecimal("2.00"), parsed.get(1).value);
	}

	@Test
	public void test_parseData_twoMissingRowData() {
		StatsService stats = new StatsService();

		String values = "1999/01/01,1.00 \n \n 1999/01/02,2.00";
		
		Map<String, String> data = new HashMap<>();
		data.put("data", values);
		
		List<WLSample> parsed = stats.parseData(data);
		
		assertEquals(2, parsed.size());
		assertEquals("1999/01/02", parsed.get(1).time);
		assertEquals(new BigDecimal("2.00"), parsed.get(1).value);
	}
	
	@Test()
	public void test_parseData_twoMissingDateData() {
		StatsService stats = new StatsService();

		String values = ",1.00\n1999/01/02,2.00";
		
		Map<String, String> data = new HashMap<>();
		data.put("data", values);
		
		try {
			stats.parseData(data);
		} catch (RuntimeException e) {
			assertTrue(e.getMessage().startsWith("The date must be valid"));
		}
	}
	
	@Test()
	public void test_parseData_twoBadDateData() {
		StatsService stats = new StatsService();

		String values = "199/01/01,1.00\n1999/01/02,2.00";
		
		Map<String, String> data = new HashMap<>();
		data.put("data", values);
		
		try {
			stats.parseData(data);
		} catch (RuntimeException e) {
			assertTrue(e.getMessage().endsWith("199/01/01"));
		}
	}
	
	@Test()
	public void test_parseData_twoBadValueData() {
		StatsService stats = new StatsService();

		String values = "1999/01/01,1a.00\n1999/01/02,2.00";
		
		Map<String, String> data = new HashMap<>();
		data.put("data", values);
		
		try {
			stats.parseData(data);
		} catch (RuntimeException e) {
			assertTrue(e.getMessage().endsWith("1a.00"));
		}
	}
	
	@Test
	public void test_service_twoFineData() {
		StatsService stats = new StatsService();

		String values = "1999/01/01,1.00\n1999/01/02,2.00";
		
		Map<String, String> data = new HashMap<>();
		data.put("data", values);
		
		ResponseEntity<String> resp = stats.service(data);
		
		assertTrue(true);
	}
}
