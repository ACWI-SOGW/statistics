package gov.usgs.wma.statistics.model;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.Test;

import gov.usgs.wma.statistics.logic.StatisticsCalculator;

public class ValueTest {

	@Test
	public void test_construct_strings() {
		Value value = new Value("time", "1.0");
		assertEquals("1.0", value.value.toPlainString());
	}
	@Test(expected=NumberFormatException.class)
	public void test_construct_non_number() {
		new Value("time", "value");
	}
	@Test
	public void test_construct_unknown() {
		Value value = new Value("time", Value.UNKNOWN_VALUE);
		assertNull( value.value );
	}
	@Test
	public void test_construct_number() {
		Value value = new Value("time", new BigDecimal("1.1"));
		assertEquals("1.1", value.value.toPlainString());
	}

	@Test
	public void test_construct_approved() {
		Value value = new Value("time", "1.2");
		assertFalse( value.isProvisional() );
	}
	@Test
	public void test_construct_provisional() {
		Value value = new Value("time", new BigDecimal("1.3"), true);
		assertTrue( value.isProvisional() );
	}
	
	@Test
	public void test_setter_provisional() {
		Value value = new Value("time", new BigDecimal("4.3"), true);
		value.setProvsional(false);
		assertTrue("provisional should not change once set", value.isProvisional() );

		Value value2 = new Value("time", new BigDecimal("4.4"));
		value2.setProvsional(true);
		assertTrue("provisional should change if not set", value2.isProvisional() );

		Value value3 = new Value("time", new BigDecimal("4.5"));
		value3.setProvsional(false);
		value3.setProvsional(true);
		assertFalse("provisional should set once", value3.isProvisional() );
	}
	
	@Test
	public void test_construct_copy() {
		Value base = new Value("time", "1.4");
		Value value = new Value(base);
		assertEquals(base.time, value.time);
		assertEquals(base.value, value.value);
		assertEquals(base.isProvisional(), value.isProvisional());
		
		Value base2 = new Value("time", new BigDecimal("1.5"), true);
		Value value2 = new Value(base2);
		assertEquals(base2.time, value2.time);
		assertEquals(base2.value, value2.value);
		assertEquals(base2.isProvisional(), value2.isProvisional());
	}
	
	@Test
	public void test_getters() {
		Value value = new Value("1999-09-19", "4.2");
		assertEquals("4.2", value.getValue().toPlainString());
		assertEquals("1999-09-19", value.getTime());
		assertEquals("1999", value.getYear());
		assertEquals("09", value.getMonth());
		assertFalse(value.isUnknown());
		value.setUnknown(true);
		assertTrue(value.isUnknown());
		
	}
	
	@Test
	public void test_valueOf() {
		Value value = new Value("time", "1.6");
		assertEquals("1.6", Value.valueOf(value).toPlainString());
	}
	@Test
	public void test_valueOf_null1() {
		Value value = new Value("time", (BigDecimal)null);
		assertEquals(null, Value.valueOf(value));

		assertEquals(null, Value.valueOf(null));
		// this is null protection of the toString
		// alternatively this could return an empty string
	}
	
	@Test
	public void test_bothAreKnown() {
		Value value1 = new Value("time1", "1.7");
		Value value2 = new Value("time2", "1.8");
		Value unknown1 = new Value("time1", Value.UNKNOWN_VALUE);
		Value unknown2 = new Value("time2", Value.UNKNOWN_VALUE);

		assertFalse(Value.bothAreKnown(value1, unknown1));
		assertFalse(Value.bothAreKnown(unknown2, value2));
		assertFalse(Value.bothAreKnown(unknown1, unknown2));
		
		assertTrue(Value.bothAreKnown(value1, value2));
	}
	
	@Test
	public void test_compareUnknown_knowns() {
		Value value1 = new Value("time1", "1.9");
		Value value2 = new Value("time2", "2.0");
		int actual = Value.compareUnknown(value1, value2);
		assertEquals("compareUnkown is only for values where one or both is unknown", 0, actual);
	}
	@Test
	public void test_compareUnknown_less() {
		Value value = new Value("time", "2.1");
		Value unknown = new Value("time3", Value.UNKNOWN_VALUE);
		int actual = Value.compareUnknown(unknown, value);
		assertEquals(-1, actual);
	}
	@Test
	public void test_compareUnknown_equal() {
		Value unknown1 = new Value("time4a", Value.UNKNOWN_VALUE);
		Value unknown2 = new Value("time4b", Value.UNKNOWN_VALUE);
		int actual = Value.SORT_VALUE_DESCENDING.compare(unknown1, unknown2);
		assertEquals(0, actual);
	}
	@Test
	public void test_compareUnknown_greater() {
		Value value = new Value("time", "2.2");
		Value unknown = new Value("time5", Value.UNKNOWN_VALUE);
		int actual = Value.SORT_VALUE_DESCENDING.compare(value, unknown);
		assertEquals(1, actual);
	}
	
	@Test
	public void test_compareAscend_less() {
		Value value1 = new Value("time1", "2.3");
		Value value2 = new Value("time2", "2.4");
		int actual = Value.SORT_VALUE_ASCENDING.compare(value1, value2);
		assertEquals(-1, actual);
	}
	@Test
	public void test_compareAscend_equal() {
		Value value1 = new Value("time1", "2.5");
		Value value2 = new Value("time2", "2.5");
		int actual = Value.SORT_VALUE_ASCENDING.compare(value1, value2);
		assertEquals(0, actual);
	}
	@Test
	public void test_compareAscend_greater() {
		Value value1 = new Value("time1", "2.7");
		Value value2 = new Value("time2", "2.6");
		int actual = Value.SORT_VALUE_ASCENDING.compare(value1, value2);
		assertEquals(1, actual);
	}
	@Test
	public void test_compareAscend_unknown_less() {
		Value value = new Value("time", "2.8");
		Value unknown = new Value("time3", Value.UNKNOWN_VALUE);
		int actual = Value.SORT_VALUE_ASCENDING.compare(unknown, value);
		assertEquals(-1, actual);
	}
	@Test
	public void test_compareAscend_unknown_equal() {
		Value unknown1 = new Value("time4a", Value.UNKNOWN_VALUE);
		Value unknown2 = new Value("time4b", Value.UNKNOWN_VALUE);
		int actual = Value.SORT_VALUE_ASCENDING.compare(unknown1, unknown2);
		assertEquals(0, actual);
	}
	@Test
	public void test_compareAscend_unknown_greater() {
		Value value = new Value("time", "2.9");
		Value unknown = new Value("time5", Value.UNKNOWN_VALUE);
		int actual = Value.SORT_VALUE_ASCENDING.compare(value, unknown);
		assertEquals(1, actual);
	}
	
	@Test
	public void test_compareDescend_less() {
		Value value1 = new Value("time1", "3.6");
		Value value2 = new Value("time2", "3.7");
		int actual = Value.SORT_VALUE_DESCENDING.compare(value1, value2);
		assertEquals(1, actual);
	}
	@Test
	public void test_compareDescend_equal() {
		Value value1 = new Value("time1", "3.8");
		Value value2 = new Value("time2", "3.8");
		int actual = Value.SORT_VALUE_DESCENDING.compare(value1, value2);
		assertEquals(0, actual);
	}
	@Test
	public void test_compareDescend_greater() {
		Value value1 = new Value("time1", "4.0");
		Value value2 = new Value("time2", "3.9");
		int actual = Value.SORT_VALUE_DESCENDING.compare(value1, value2);
		assertEquals(-1, actual);
	}
	@Test
	public void test_compareDescend_unknown_less() {
		Value value = new Value("time", "4.1");
		Value unknown = new Value("time3", Value.UNKNOWN_VALUE);
		int actual = Value.SORT_VALUE_DESCENDING.compare(unknown, value);
		assertEquals(-1, actual);
	}
	@Test
	public void test_compareDescend_unknown_equal() {
		Value unknown1 = new Value("time4a", Value.UNKNOWN_VALUE);
		Value unknown2 = new Value("time4b", Value.UNKNOWN_VALUE);
		int actual = Value.SORT_VALUE_DESCENDING.compare(unknown1, unknown2);
		assertEquals(0, actual);
	}
	@Test
	public void test_compareDescend_unknown_greater() {
		Value value = new Value("time", "2.9");
		Value unknown = new Value("time5", Value.UNKNOWN_VALUE);
		int actual = Value.SORT_VALUE_DESCENDING.compare(value, unknown);
		assertEquals(1, actual);
	}
	
	@Test
	public void test_compareTime_less() {
		Value value1 = new Value("2000-10-01", "3.0");
		Value value2 = new Value("2001-01-10", "3.1");
		int actual = Value.TIME_COMPARATOR.compare(value1, value2);
		assertEquals(-1, actual);
	}
	@Test
	public void test_compareTime_equal() {
		Value value1 = new Value("2011-11-11", "3.2");
		Value value2 = new Value("2011-11-11", "3.3");
		int actual = Value.TIME_COMPARATOR.compare(value1, value2);
		assertEquals(0, actual);
	}
	@Test
	public void test_compareTime_greater() {
		Value value1 = new Value("2003-03-03", "3.4");
		Value value2 = new Value("2002-02-02", "3.5");
		int actual = Value.TIME_COMPARATOR.compare(value1, value2);
		assertEquals(1, actual);
	}

	@Test
	public void test_checkBadValue_nan() {
		String bad = "not a number";
		boolean actual = Value.checkBadValue(bad, 1, "test:N/A");
		assertTrue("True means the value is bad", actual);
	}
	@Test
	public void test_checkBadValue_null() {
		String bad = null;
		boolean actual = Value.checkBadValue(bad, 2, "test:N/A");
		assertTrue("True means the value is bad", actual);
	}
	@Test
	public void test_checkBadValue_unknown() {
		String bad = Value.UNKNOWN_VALUE;
		boolean actual = Value.checkBadValue(bad, 3, "test:N/A");
		assertFalse("'" + bad + "' means the value is unknown and is not technically bad", actual);
	}
	@Test
	public void test_checkBadValue_good() {
		String good = "3.6";
		boolean actual = Value.checkBadValue(good, 4, "test:N/A");
		assertFalse("False means the value is good", actual);
	}
	
	@Test
	public void test_checkTime_good() {
		String good = "2005-06-10T04:15:00-05:00";
		boolean actual = Value.checkBadTime(good, 5, "test:N/A");
		assertFalse("False means the time is good", actual);
	}
	@Test
	public void test_checkTime_null() {
		String bad = null;
		boolean actual = Value.checkBadTime(bad, 5, "test:N/A");
		assertTrue("True means the time is bad", actual);
	}
	@Test
	public void test_checkTime_invalid() {
		String bad = "invalid time";
		boolean actual = Value.checkBadTime(bad, 5, "test:N/A");
		assertTrue("True means the time is bad", actual);
	}
	@Test
	// TODO when and if we implement date value validation then this would have to be updated
	public void test_checkTime_month() {
		String bad = "2005-13-10T04:15:00-05:00";
		boolean actual = Value.checkBadTime(bad, 5, "test:N/A");
		assertFalse("No 13th month, this should be marked bad but it only checks format", actual);
	}
	@Test
	// TODO when and if we implement date value validation then this would have to be updated
	public void test_checkTime_day() {
		String bad = "2005-02-30T04:15:00-05:00";
		boolean actual = Value.checkBadTime(bad, 5, "test:N/A");
		assertFalse("Feb has < 30 days, this should be marked bad but it only checks format", actual);
	}
	@Test
	public void test_checkTime_yearOnly() {
		String ok = "2005";
		boolean actual = Value.checkBadTime(ok, 5, "test:N/A");
		assertFalse("Year translates to midsummer", actual);
	}
	@Test
	public void test_checkTime_yearMonth() {
		String ok = "2005-10";
		boolean actual = Value.checkBadTime(ok, 5, "test:N/A");
		assertFalse("Year-month translates to midmonth", actual);
	}
	
	@Test
	public void test_today__ensure_that_the_today_method_returns_the_current_month_and_year() throws Exception {
		String today = StatisticsCalculator.today();
		Date date = new Date();

		// the Date.getYear() returns a previous century truncated value (88 rather than 1988)
		// and it returns 116 for 2016; hence the deprecation and addition of 1900 for a proper year number
		@SuppressWarnings("deprecation")
		String year = ""+(date.getYear()+1900); // see Calendar.get(Calendar.YEAR)
		assertEquals(year, Value.yearUTC(today) );

		// months are zero based in Date and humans use one based - i.e. January return zero but we want 1.
		// further more, for testing we want a double digit month zero padded. There are many ways (and most better)
		// than this to get such a value but it is a quick way to get what is needed without the complication of Calendar.
		@SuppressWarnings("deprecation")
		String month = ""+(date.getMonth()+1); // see Calendar.get(Calendar.MONTH)
		month = (month.length() == 1 ?"0" :"") + month;
		assertEquals(month, Value.monthUTC(today) );
	}
	
	@Test
	public void test_month_year_extract_from_UTC() throws Exception {
		String dateUTC = "2000-12-23...";

		String year = dateUTC.substring(0, 4);
		assertEquals("2000", year);

		String month = dateUTC.substring(5, 7);
		assertEquals("12", month);

		year = Value.yearUTC(dateUTC);
		assertEquals("2000", year);

		month = Value.monthUTC(dateUTC);
		assertEquals("12", month);
	}
	
	@Test
	public void test_toString() {
		// TODO asdf check toPlainString
		Value value = new Value("TheTime","5.0");
		assertTrue( value.toString().contains("TheTime") );
		assertTrue( value.toString().contains("5.0") );
		assertFalse( value.toString().contains(Value.PROVISIONAL_CODE) );
		
		value = new Value("TheTime", Value.UNKNOWN_VALUE);
		assertTrue( value.toString().contains(Value.UNKNOWN_VALUE) );

		value = new Value("TheTime", new BigDecimal("5.1"), true);
		value.setProvsional(true);
		assertTrue( value.toString().contains(Value.PROVISIONAL_CODE) );
	}
	
	@Test
	public void test_toCSV() {
		Value value = new Value("2000-12-12","5.2");
		String expect = "2000-12-12,5.2,"; 
		assertEquals(expect, value.toCSV() );
		
		value = new Value("2000-12-13", Value.UNKNOWN_VALUE);
		expect = "2000-12-13,%s,".replace("%s", Value.UNKNOWN_VALUE); 
		assertEquals(expect, value.toCSV() );

		value = new Value("2000-12-25", new BigDecimal("5.3"), true);
		expect = "2000-12-25,5.3,P"; 
		assertEquals(expect, value.toCSV());
	}
}
