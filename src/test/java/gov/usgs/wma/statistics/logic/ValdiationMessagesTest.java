package gov.usgs.wma.statistics.logic;

import static gov.usgs.wma.statistics.app.Properties.*;
import static org.apache.commons.lang.StringUtils.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.usgs.ngwmn.logic.WaterLevelStatistics;
import gov.usgs.ngwmn.model.Specifier;
import gov.usgs.ngwmn.model.WLSample;
import gov.usgs.wma.statistics.app.Properties;
import gov.usgs.wma.statistics.model.JsonDataBuilder;
import gov.usgs.wma.statistics.model.Value;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = { "/application.properties" })
public class ValdiationMessagesTest {

	private static final String SPRING_ENV_FAIL = "Spring env failure.";

	@Autowired
	Environment spring;
	
	Properties env;
	JsonDataBuilder builder;

	@Before
	public void setup() {
		env = new Properties().setEnvironment(spring);
		builder = new JsonDataBuilder(env);
	}
	
	public String getText(String property) {
		String text = spring.getProperty(property, SPRING_ENV_FAIL);
		return isBlank(text) ?SPRING_ENV_FAIL :text;
	}
	private WLSample createSample(String time, String value) {
		BigDecimal val = null;
		if (null!=value) {
			val = new BigDecimal(value);
		}
		return new WLSample(time, val, "units", val, "comment", true, "pcode", null);
	}
	private WLSample createSample(String time, String value, boolean provisional) {
		WLSample sample = createSample(time, value);
		sample.setProvsional(provisional);
		return sample;
	}
	private Optional<String> fetchMessageLike(String property, int length) {
		String message = getText(property);
		String startWith = message.substring(0, length);
		Optional<String> actual = builder.messages().filter(msg -> {
			return isNotBlank(msg) && msg.startsWith(startWith);
		}).findFirst();
		return actual;
	}
	private List<String> fetchMessages() {
		List<String> actual = builder.messages().collect(Collectors.toList());
		return actual;
	}
	
	@Test
	public void test_waterLevelStatistics_MONTHLY_RULE() {
		WaterLevelStatistics stats = new WaterLevelStatistics(env, builder);
		
		List<WLSample> samples = new LinkedList<>();
		samples.add( createSample("2000-01-01", "1.0") );
		stats.calculate(new Specifier(), samples);
		
		assertFalse( builder.hasErrors() );
		Optional<String> actual = fetchMessageLike(ENV_MESSAGE_MONTHLY_RULE, 10);
		assertNotNull( actual );
		assertTrue( actual.isPresent() );
		assertTrue( actual.get().startsWith("Too few data values for monthly statistics") );
	}
	@Test
	public void test_waterLevelStatistics_PROVISIONAL_RULE() {
		WaterLevelStatistics stats = new WaterLevelStatistics(env, builder);
		
		List<WLSample> samples = new LinkedList<>();
		samples.add( createSample("2000-01-01", "1.0") );
		samples.add( createSample("2000-02-01", "1.0", true) );
		stats.calculate(new Specifier(), samples);
		
		assertFalse( builder.hasErrors() );
		Optional<String> actual = fetchMessageLike(ENV_MESSAGE_PROVISIONAL_RULE, 10);
		assertNotNull( actual );
		assertTrue( actual.isPresent() );
		assertTrue( actual.get().startsWith("The most recent value is provisional") );
	}
	@Test
	public void test_waterLevelStatistics_MONTLY_DETAIL_1() {
		MonthlyStatistics<WLSample> stats = new WaterLevelStatistics(env, builder).getMonthlyStats();

		List<WLSample> samples = new LinkedList<>();
		samples.add( createSample("2000-01-01", "1.0") );
		stats.doesThisMonthQualifyForStats(samples);
		
		assertFalse( builder.hasErrors() );
		Optional<String> actual = fetchMessageLike(ENV_MESSAGE_MONTHLY_DETAIL, 10);
		assertNotNull( actual );
		assertTrue( actual.isPresent() );
		assertTrue( actual.get().startsWith("The month of January") );
	}
	@Test
	public void test_waterLevelStatistics_MONTLY_DETAIL_2() {
		MonthlyStatistics<WLSample> stats = new WaterLevelStatistics(env, builder).getMonthlyStats();

		List<WLSample> samples = new LinkedList<>();
		samples.add( createSample("2000-01-01", "1.0") );
		stats.doesThisMonthQualifyForStats(samples);
		samples.clear();
		samples.add( createSample("2001-02-01", "1.0") );
		samples.add( createSample("2002-02-01", "1.0") );
		samples.add( createSample("2003-02-01", "1.0") );
		samples.add( createSample("2004-02-01", "1.0") );
		samples.add( createSample("2005-02-01", "1.0") );
		samples.add( createSample("2006-02-01", "1.0") );
		samples.add( createSample("2007-02-01", "1.0") );
		samples.add( createSample("2008-02-01", "1.0") );
		samples.add( createSample("2009-02-01", "1.0") );
		stats.doesThisMonthQualifyForStats(samples);
		
		assertFalse( builder.hasErrors() );
		
		// ENV_MESSAGE_MONTHLY_DETAIL
		List<String> actual = fetchMessages();
		assertNotNull( actual );
		assertEquals(2, actual.size() );
		assertTrue( actual.get(0).startsWith("The month of January requires 9 more") );
		assertTrue( actual.get(1).startsWith("The month of February requires 1 more") );
	}

	@Test
	public void test_monthlyStatistics_MONTHLY_MEDIANS() {
		MonthlyStatistics<Value> stats = new MonthlyStatistics<>(env, builder);
		
		List<Value> samples = new LinkedList<>();
		samples.add( new Value("2000-01-01", new BigDecimal("1.0")) );
		samples.add( new Value("2000-01-10", new BigDecimal("3.0")) );
		stats.medianMonthlyValues(samples , stats.sortFunctionByQualifier());

		assertFalse( builder.hasErrors() );
		Optional<String> actual = fetchMessageLike(ENV_MESSAGE_MONTHLY_MEDIANS, 10);
		assertNotNull( actual );
		assertTrue( actual.isPresent() );
		assertTrue( actual.get().contains("January") );
	}
	
	@Test
	public void test_StatisticsCalculator_FIX_MONTH() {
		StatisticsCalculator<Value> stats = new StatisticsCalculator<>(env, builder);
		
		List<Value> samples = new LinkedList<>();
		samples.add( new Value("2000", new BigDecimal("1.0")) );
		samples.add( new Value("2000-01-01", new BigDecimal("1.0")) );
		stats.checkAllDates(samples);
		
		assertFalse( builder.hasErrors() );
		Optional<String> actual = fetchMessageLike(ENV_MESSAGE_DATE_FIX_MONTH, 10);
		assertNotNull( actual );
		assertTrue( actual.isPresent() );
		assertTrue( actual.get().contains("6-30") );
		assertTrue( actual.get().contains("Sample number 1") );
	}
	@Test
	public void test_StatisticsCalculator_FIX_DAY() {
		StatisticsCalculator<Value> stats = new StatisticsCalculator<>(env, builder);
		
		List<Value> samples = new LinkedList<>();
		samples.add( new Value("2000-01-01", new BigDecimal("1.0")) );
		samples.add( new Value("2000-01", new BigDecimal("1.0")) );
		samples.add( new Value("2000-01-03", new BigDecimal("1.0")) );
		stats.checkAllDates(samples);
		
		assertFalse( builder.hasErrors() );
		Optional<String> actual = fetchMessageLike(ENV_MESSAGE_DATE_FIX_DAY, 10);
		assertNotNull( actual );
		assertTrue( actual.isPresent() );
		assertTrue( actual.get().contains("15") );
		assertTrue( actual.get().contains("Sample number 2") );
	}
	@Test
	public void test_StatisticsCalculator_OMIT_NULL_0() {
		StatisticsCalculator<Value> stats = new StatisticsCalculator<>(env, builder);
		
		List<Value> samples = new LinkedList<>();
		samples.add( new Value("2000-01-01", new BigDecimal("1.0")) );
		samples.add( new Value("2000-01-03", new BigDecimal("1.0")) );
		stats.removeNulls(samples, "Test ID");
		
		assertFalse( builder.hasErrors() );
		// ENV_MESSAGE_OMIT_NULL
		List<String> actual = fetchMessages();
		assertNotNull( actual );
		assertEquals(0, actual.size());
	}
	@Test
	public void test_StatisticsCalculator_OMIT_NULL_1() {
		StatisticsCalculator<Value> stats = new StatisticsCalculator<>(env, builder);
		
		List<Value> samples = new LinkedList<>();
		samples.add( new Value("2000-01-01", new BigDecimal("1.0")) );
		samples.add( new Value("2000-01-02", (BigDecimal)null) );
		samples.add( new Value("2000-01-03", new BigDecimal("1.0")) );
		stats.removeNulls(samples, "Test ID");
		
		assertFalse( builder.hasErrors() );
		Optional<String> actual = fetchMessageLike(ENV_MESSAGE_OMIT_NULL, 7);
		assertNotNull( actual );
		assertTrue( actual.isPresent() );
		assertEquals("Removed 1 empty sample at row 2.", actual.get() );
	}
	@Test
	public void test_StatisticsCalculator_OMIT_NULL_2() {
		StatisticsCalculator<Value> stats = new StatisticsCalculator<>(env, builder);
		
		List<Value> samples = new LinkedList<>();
		samples.add( new Value("2000-01-01", new BigDecimal("1.0")) );
		samples.add( new Value("2000-01-02", (BigDecimal)null) );
		samples.add( new Value(null, new BigDecimal("1.0")) );
		stats.removeNulls(samples, "Test ID");
		
		assertFalse( builder.hasErrors() );
		Optional<String> actual = fetchMessageLike(ENV_MESSAGE_OMIT_NULL, 7);
		assertNotNull( actual );
		assertTrue( actual.isPresent() );
		assertEquals( "Removed 2 empty samples at rows 2, 3.", actual.get() );
	}
	@Test
	public void test_StatisticsCalculator_OMIT_PROVISIONAL_0() {
		StatisticsCalculator<Value> stats = new StatisticsCalculator<>(env, builder);
		
		List<Value> samples = new LinkedList<>();
		samples.add( new Value("2000-01-01", new BigDecimal("1.0")) );
		samples.add( new Value("2000-01-03", new BigDecimal("1.0")) );
		stats.removeProvisional(samples, "Test ID");
		
		assertFalse( builder.hasErrors() );
		// ENV_MESSAGE_OMIT_PROVISIONAL
		List<String> actual = fetchMessages();
		assertNotNull( actual );
		assertEquals(0, actual.size());
	}
	@Test
	public void test_StatisticsCalculator_OMIT_PROVISIONAL_1() {
		StatisticsCalculator<Value> stats = new StatisticsCalculator<>(env, builder);
		
		List<Value> samples = new LinkedList<>();
		samples.add( new Value("2000-01-01", new BigDecimal("1.0")) );
		samples.add( new Value("2000-01-02", new BigDecimal("1.0"), true) );
		samples.add( new Value("2000-01-03", new BigDecimal("1.0")) );
		stats.removeProvisional(samples, "Test ID");
		
		assertFalse( builder.hasErrors() );
		Optional<String> actual = fetchMessageLike(ENV_MESSAGE_OMIT_PROVISIONAL, 7);
		assertNotNull( actual );
		assertTrue( actual.isPresent() );
		assertEquals("Removed 1 provisional sample.", actual.get() );
	}
	@Test
	public void test_StatisticsCalculator_OMIT_PROVISIONAL_2() {
		StatisticsCalculator<Value> stats = new StatisticsCalculator<>(env, builder);
		
		List<Value> samples = new LinkedList<>();
		samples.add( new Value("2000-01-01", new BigDecimal("1.0"), true) );
		samples.add( new Value("2000-01-02", new BigDecimal("1.0"), true) );
		samples.add( new Value("2000-01-03", new BigDecimal("1.0")) );
		stats.removeProvisional(samples, "Test ID");
		
		assertFalse( builder.hasErrors() );
		Optional<String> actual = fetchMessageLike(ENV_MESSAGE_OMIT_PROVISIONAL, 7);
		assertNotNull( actual );
		assertTrue( actual.isPresent() );
		assertEquals( "Removed 2 provisional samples.", actual.get() );
	}
}
