package gov.usgs.wma.statistics.model;

import static gov.usgs.wma.statistics.model.JsonDataBuilder.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import gov.usgs.ngwmn.logic.WaterLevelStatistics.MediationType;

public class JsonDataBuilderTest {
	
	final String VALUE_1    = "12.34";
	final String VALUE_2    = "43.21";
	final String DATE_UTC_1 = "2000-06-10T04:15:00-05:00";
	final String DATE_UTC_2 = "2005-06-10T04:15:00-05:00";
	final String PERCENT_1  = "24";
	final String PERCENT_2  = "42";
	final String MONTH_1    = "01";
	final String MONTH_2    = "02";
	final String YEARS      = "24";
	final int    COUNT      = 42;
	final Value  SAMPLE_1   = new Value(DATE_UTC_1, VALUE_1);
	final Value  SAMPLE_2   = new Value(DATE_UTC_2, VALUE_2);

	
	JsonDataBuilder data = new JsonDataBuilder();

	@Before
	public void setup() {
		data = new JsonDataBuilder();
	}
	
	@Test
	public void test_sampleCount() {
		data.sampleCount(COUNT);
		assertEquals(""+COUNT, data.values.get(SAMPLE_COUNT));
	}

	@Test
	public void test_recordYears() {
		data.recordYears(YEARS);
		assertEquals(""+YEARS, data.values.get(RECORD_YEARS));
	}

	@Test
	public void test_latestPercentile() {
		data.latestPercentile(VALUE_1);
		assertEquals(VALUE_1, data.values.get(LATEST_PCTILE));
	}

	@Test
	public void test_latestValue() {
		data.latestValue(VALUE_1);
		assertEquals(VALUE_1, data.values.get(LATEST_VALUE));
	}

	@Test
	public void test_maxDate() {
		data.maxDate(DATE_UTC_2);
		assertEquals(DATE_UTC_2, data.values.get(MAX_DATE));
	}

	@Test
	public void test_minDate() {
		data.minDate(DATE_UTC_1);
		assertEquals(DATE_UTC_1, data.values.get(MIN_DATE));
	}
	
	@Test
	public void test_maxDate_minDate() {
		data.minDate(DATE_UTC_1);
		data.maxDate(DATE_UTC_2);
		
		assertEquals(DATE_UTC_1, data.values.get(MIN_DATE));
		assertEquals(DATE_UTC_2, data.values.get(MAX_DATE));
	}
	
	@Test
	public void test_minValue() {
		data.minValue(VALUE_1);
		assertEquals(VALUE_1, data.values.get(MIN_VALUE));
	}
	
	@Test
	public void test_maxValue() {
		data.maxValue(VALUE_2);
		assertEquals(VALUE_2, data.values.get(MAX_VALUE));
	}
	
	@Test
	public void test_maxValue_minValue() {
		data.minValue(VALUE_1);
		data.maxValue(VALUE_2);
		
		assertEquals(VALUE_1, data.values.get(MIN_VALUE));
		assertEquals(VALUE_2, data.values.get(MAX_VALUE));
	}
	
	@Test
	public void test_median() {
		data.median(VALUE_1);
		assertEquals(VALUE_1, data.values.get(MEDIAN));
	}
	
	@Test
	public void test_mediation() {
		assertEquals(MediationType.NONE, data.mediation);
		assertEquals(null, data.values.get(MEDIATION));
		
		data.mediation(MediationType.AboveDatum);
		
		assertEquals(MediationType.AboveDatum, data.mediation);
		assertEquals(null, data.values.get(MEDIATION));
}
	
	@Test
	public void test_minP50() {
		data.minP50(VALUE_1);
		assertEquals(VALUE_1, data.values.get(P50_MIN));
	}
	
	@Test
	public void test_maxP50() {
		data.maxP50(VALUE_2);
		assertEquals(VALUE_2, data.values.get(P50_MAX));
	}
	
	@Test
	public void test_maxP50_minP50() {
		data.minP50(VALUE_1);
		data.maxP50(VALUE_2);
		
		assertEquals(VALUE_1, data.values.get(P50_MIN));
		assertEquals(VALUE_2, data.values.get(P50_MAX));
	}
	
	@Test
	public void test_month() {
		assertFalse(data.isMonthly());
		assertTrue(data.isOverall());
		data.month(MONTH_1);
		assertEquals(MONTH_1, data.values.get(MONTH));
		assertTrue(data.isMonthly());
		assertFalse(data.isOverall());
	}
	
	@Test(expected=RuntimeException.class)
	public void test_month_bigMonthNumber() {
		data.month("13");
	}
	
	@Test(expected=RuntimeException.class)
	public void test_month_smallMonthNumber() {
		data.month("0");
	}
	
	@Test(expected=RuntimeException.class)
	public void test_month_badNumber() {
		data.month("A");
	}
	
	
	@Test
	public void test_percentilesList() {
		assertTrue(data.percentiles.contains(P10));
		List<String> percentiles = new LinkedList<>(); 
		Object before = data.percentiles;
		data.percentiles(percentiles);
		Object after = data.percentiles;
		assertNotEquals(before, after);
		assertFalse(data.percentiles.contains(P10));
	}
	
	@Test
	public void test_addPercentiles() {
		assertTrue(data.percentiles.contains(P10));
		assertFalse(data.percentiles.contains(PERCENT_1));
		assertFalse(data.percentiles.contains(PERCENT_2));
		data.addPercentiles(PERCENT_1, PERCENT_2);
		assertTrue(data.percentiles.contains(P10));
		assertTrue(data.percentiles.contains(PERCENT_1));
		assertTrue(data.percentiles.contains(PERCENT_2));
	}
	
	@Test
	public void test_addPercentiles_duplicate() {
		assertTrue(data.percentiles.contains(P10));
		int sizeBefore = data.percentiles.size();
		data.addPercentiles(P10);
		int sizeAfter = data.percentiles.size();
		assertTrue(data.percentiles.contains(P10));

		assertEquals(sizeBefore, sizeAfter);
	}

	@Test
	public void test_buikdPercentiles() {
		Map<String, BigDecimal> pct = data.buildPercentiles();
		
		assertEquals(data.percentiles.size(), pct.size());
		assertEquals("0.100000000", pct.get("P10").toString());
	}
	
	@Test
	public void test_collect_overall() {
		data.recordYears(YEARS);
		data.sampleCount(COUNT);
		data.latestPercentile(VALUE_1);
		data.latestValue(VALUE_1);
		data.maxDate(DATE_UTC_2);
		data.minDate(DATE_UTC_1);
		data.minValue(VALUE_1);
		data.maxValue(VALUE_2);
		data.median(VALUE_1);
		data.mediation(MediationType.AboveDatum);

		assertNull(data.jsonData.overall);
		data.collect();
		assertNotNull(data.jsonData.overall);
		
		assertEquals(0, data.values.size());
	}

	@Test
	public void test_collect_monthly() {
		data.month(MONTH_1);
		
		data.recordYears(YEARS);
		data.sampleCount(COUNT);
		
		data.minP50(VALUE_1);
		data.maxP50(VALUE_2);

		Map<String, BigDecimal> pcts = data.buildPercentiles();
		for (String pct : pcts.keySet()) {
			data.putPercentile(pct, VALUE_1);
		}
		
		assertEquals(0, data.jsonData.monthly.size());
		data.collect();
		assertEquals(1, data.jsonData.monthly.size());
		
		assertEquals(0, data.values.size());
	}
	
	@Test
	public void test_build() {		
		final Boolean[] collectCalled = new Boolean[] {Boolean.FALSE};
		
		data = new JsonDataBuilder() {
			@Override
			public JsonDataBuilder collect() {
				collectCalled[0] = true;
				return this;
			}
		};
		data.values.remove(MEDIATION); // remove default mediation for this test.

		JsonData json = data.build();
		assertNotNull(json);
		
		assertFalse("no need to call collect on build if no new data", collectCalled[0]);
	}
	
	@Test
	public void test_build_withCollect() {
		final Boolean[] collectCalled = new Boolean[] {Boolean.FALSE};
		
		data = new JsonDataBuilder() {
			@Override
			public JsonDataBuilder collect() {
				collectCalled[0] = true;
				return super.collect();
			}
		};
		
		data.recordYears(YEARS);
		data.sampleCount(COUNT);
		data.latestPercentile(VALUE_1);
		data.latestValue(VALUE_1);
		data.maxDate(DATE_UTC_2);
		data.minDate(DATE_UTC_1);
		data.minValue(VALUE_1);
		data.maxValue(VALUE_2);
		data.median(VALUE_1);
		data.mediation(MediationType.AboveDatum);
//		test_collect_overall();
//		test_collect_monthly();
		
		JsonData json = data.build();
		assertNotNull(json);

		assertTrue("need to call collect on build if new data", collectCalled[0]);
		
		assertEquals(YEARS, json.overall.recordYears);
		assertEquals(COUNT, json.overall.sampleCount);
		assertEquals(VALUE_1, json.overall.latestPercentile);
		assertEquals(VALUE_1, json.overall.latestValue);
		assertEquals(DATE_UTC_1, json.overall.dateMin);
		assertEquals(DATE_UTC_2, json.overall.dateMax);
	}
	
	@Test
	public void test_intermediateValue() {
		assertEquals(0, data.intermediateValues.length());
		data.setIncludeIntermediateValues(true);
		data.intermediateValue(SAMPLE_1);
		
		assertTrue(data.intermediateValues.toString().contains(DATE_UTC_1));
		assertTrue(data.intermediateValues.toString().contains(VALUE_1));
		
		data.intermediateValue(SAMPLE_2);
		
		assertTrue(data.intermediateValues.toString().contains(DATE_UTC_2));
		assertTrue(data.intermediateValues.toString().contains(VALUE_2));
	}
	
	@Test
	public void test_buildIntermediateValues() {
		assertEquals(0, data.intermediateValues.length());
		data.setIncludeIntermediateValues(true);
		data.intermediateValue(SAMPLE_1);
		data.intermediateValue(SAMPLE_2);
		
		assertTrue(data.intermediateValues.toString().contains(DATE_UTC_1));
		assertTrue(data.intermediateValues.toString().contains(VALUE_1));
		assertTrue(data.intermediateValues.toString().contains(DATE_UTC_2));
		assertTrue(data.intermediateValues.toString().contains(VALUE_2));

		assertEquals('\"', data.intermediateValues.charAt(0));
		int last = data.intermediateValues.length()-1;
		assertEquals('n', data.intermediateValues.charAt(last));
		
		data.buildIntermediateValues();
		
		last = data.intermediateValues.length()-1;
		assertEquals('"', data.intermediateValues.charAt(last));
		
		last = data.jsonData.medians.length()-1;
		assertEquals('"', data.jsonData.medians.charAt(last));
	}
	
	@Test
	public void test_intermediateValues() {
		data.intermediateValue(SAMPLE_1);
		data.intermediateValue(SAMPLE_2);
		data.buildIntermediateValues();

		String individuals = data.jsonData.medians;
		
		data = new JsonDataBuilder();
		List<Value> values = new ArrayList<>();
		values.add(SAMPLE_1);
		values.add(SAMPLE_2);
		data.intermediateValues(values);
		data.buildIntermediateValues();

		String collection = data.jsonData.medians;

		assertEquals(individuals, collection);
	}
}
