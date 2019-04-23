package gov.usgs.wma.statistics.model;

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

import gov.usgs.wma.statistics.app.Properties;
import gov.usgs.wma.statistics.control.StatsService;
import gov.usgs.wma.statistics.logic.StatisticsCalculator;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = { "/application.properties" })
public class ValdiationErrorsTest {

	private static final String SPRING_ENV_FAIL = "Spring env failure.";
	
	@Autowired
	Environment spring;
	
	Properties env;
	JsonDataBuilder builder;
	StatsService stats;
	
	String data = "2001-01-01,1.1,\n2002-02-02,2.2";
	String mediation = "AboveDatum";
	String medians = "false";
	String percentiles = "10, 20, 30";
	
	@Before
	public void setup() {
		env = new Properties().setEnvironment(spring);
		builder = new JsonDataBuilder(env);
		stats = new StatsService().setProperties(env);
		data = "2001-01-01,1.1,\n2002-02-02,2.2";
		mediation = "AboveDatum";
		medians = "false";
		percentiles = "10, 20, 30";
	}
	public String getText(String property) {
		String text = spring.getProperty(property, SPRING_ENV_FAIL);
		return isBlank(text) ?SPRING_ENV_FAIL :text;
	}
	private Optional<String> fetchMessageLike(String property, int length) {
		String message = getText(property);
		String startWith = message.substring(0, length);
		Optional<String> actual = builder.errors().filter(msg -> {
			return isNotBlank(msg) && msg.startsWith(startWith);
		}).findFirst();
		return actual;
	}
	private List<String> fetchMessages() {
		List<String> actual = builder.errors().collect(Collectors.toList());
		return actual;
	}

	@Test
	public void test_statService_VALID() {
		JsonData json = stats.calculate(builder, data, mediation, medians, percentiles);
		System.err.println(json.errors);
		assertFalse(json.hasErrors());
		assertTrue(json.isOk());
	}
	@Test
	public void test_statService_INVALID_MEDIATION() {
		mediation = "unknown";
		JsonData json = stats.calculate(builder, data, mediation, medians, percentiles);
		
		assertTrue(json.hasErrors());
		assertFalse(json.isOk());
		assertEquals(1, json.errors.size());
		Optional<String> msg = fetchMessageLike(ENV_INVALID_MEDIATION, 15);
		assertTrue(msg.isPresent());
		assertTrue(msg.get().contains(mediation));
	}

	@Test
	public void test_statService_INVALID_MEDIANS() {
		medians = "unknown";
		JsonData json = stats.calculate(builder, data, mediation, medians, percentiles);
		
		assertTrue(json.hasErrors());
		assertFalse(json.isOk());
		assertEquals(1, json.errors.size());
		Optional<String> msg = fetchMessageLike(ENV_INVALID_MEDIANS, 15);
		assertTrue(msg.isPresent());
		assertTrue(msg.get().contains(medians));
	}

	@Test
	public void test_statService_INVALID_ROW_COLS() {
		String badDate = "2001,01,01";
		data = badDate + ",1.1,\n2002-02-02,2.2";
		JsonData json = stats.calculate(builder, data, mediation, medians, percentiles);
		
		assertTrue(json.hasErrors());
		assertFalse(json.isOk());
		assertEquals(1, json.errors.size());
		Optional<String> msg = fetchMessageLike(ENV_INVALID_ROW_COLS, 20);
		assertTrue(msg.isPresent());
		assertTrue(msg.get().contains(badDate));
	}

	@Test
	public void test_statsService_INVALID_ROW_AGING() {
		String badAging = "QZ";
		data = "2001-01-01,1.1,"+badAging+"\n2002-02-02,2.2";
		JsonData json = stats.calculate(builder, data, mediation, medians, percentiles);
		
		assertTrue(json.hasErrors());
		assertFalse(json.isOk());
		assertEquals(1, json.errors.size());
		Optional<String> msg = fetchMessageLike(ENV_INVALID_ROW_AGING, 20);
		assertTrue(msg.isPresent());
		assertTrue(msg.get().contains(badAging));
	}

	@Test
	public void test_statsService_INVALID_ROW_VALUE() {
		String badValue = "QZ";
		data = "2001-01-01,"+badValue+"\n2002-02-02,2.2";
		JsonData json = stats.calculate(builder, data, mediation, medians, percentiles);
		
		assertTrue(json.hasErrors());
		assertFalse(json.isOk());
		assertEquals(1, json.errors.size());
		Optional<String> msg = fetchMessageLike(ENV_INVALID_ROW_VALUE, 20);
		assertTrue(msg.isPresent());
		assertTrue(msg.get().contains(badValue));
	}

	@Test
	public void test_statService_INVALID_ROW_FORMAT() {
//		ENV_INVALID_ROW_FORMAT will seldom be a case, I cannot think of a means it invoke
	}

	@Test
	public void test_statsServcie_INVALID_ROW_DATE_BLANK() {
		String badDate = "";
		data = badDate + ",1.1,\n2002-02-02,2.2";
		JsonData json = stats.calculate(builder, data, mediation, medians, percentiles);
		
		assertTrue(json.hasErrors());
		assertFalse(json.isOk());
		assertEquals(1, json.errors.size());
		Optional<String> msg = fetchMessageLike(ENV_INVALID_ROW_DATE_BLANK, 10);
		assertTrue(msg.isPresent());
		assertTrue(msg.get().contains(badDate));
	}
	
	@Test
	public void test_statisticsCalculator_INVALID_ROW_DATE_BLANK() {
		List<Value> samples = new LinkedList<>();
		samples.add( new Value("",BigDecimal.ONE) );
		
		new StatisticsCalculator<Value>(env, builder).checkAllDates(samples);

		JsonData json = builder.build();
		assertTrue(json.hasErrors());
		assertFalse(json.isOk());
		assertEquals(1, json.errors.size());
		Optional<String> msg = fetchMessageLike(ENV_INVALID_ROW_DATE_BLANK, 10);
		assertTrue(msg.isPresent());
	}

	@Test
	public void test_statsService_INVALID_ROW_DATE_FUTURE() {
		String badDate = "2107-01-01";
		data = badDate + ",1.1,\n2002-02-02,2.2";
		JsonData json = stats.calculate(builder, data, mediation, medians, percentiles);
		
		assertTrue(json.hasErrors());
		assertFalse(json.isOk());
		assertEquals(1, json.errors.size());
		Optional<String> msg = fetchMessageLike(ENV_INVALID_ROW_DATE_FUTURE, 10);
		assertTrue(msg.isPresent());
		assertTrue(msg.get().contains(badDate));
	}
	@Test
	public void test_statisticsCalculator_INVALID_ROW_DATE_FUTURE() {
		List<Value> samples = new LinkedList<>();
		String badDate = "2107-01-01";
		samples.add( new Value(badDate,BigDecimal.ONE) );
		
		new StatisticsCalculator<Value>(env, builder).checkAllDates(samples);

		JsonData json = builder.build();
		assertTrue(json.hasErrors());
		assertFalse(json.isOk());
		assertEquals(1, json.errors.size());
		Optional<String> msg = fetchMessageLike(ENV_INVALID_ROW_DATE_FUTURE, 10);
		assertTrue(msg.isPresent());
		assertTrue(msg.get().contains(badDate));
	}
	
	@Test
	public void test_jsonDataBuilder_INVALID_PERCENTILE_0() {
		builder.percentiles("11.1");
		builder.buildPercentiles();
		
		assertFalse( builder.hasErrors() );
//		 ENV_INVALID_PERCENTILE
		List<String> actual = fetchMessages();
		assertNotNull( actual );
		assertEquals(0, actual.size());
	}

	@Test
	public void test_jsonDataBuilder_INVALID_PERCENTILE_1() {
		builder.percentiles("-1.1");
		builder.buildPercentiles();
		
		assertTrue( builder.hasErrors() );
//		 ENV_INVALID_PERCENTILE
		List<String> actual = fetchMessages();
		assertNotNull( actual );
		assertEquals(1, actual.size());
		assertTrue( actual.get(0).contains("-1.1"));
	}

	@Test
	public void test_jsonDataBuilder_INVALID_PERCENTILE_2() {
		builder.percentiles("110");
		builder.buildPercentiles();
		
		assertTrue( builder.hasErrors() );
//		 ENV_INVALID_PERCENTILE
		List<String> actual = fetchMessages();
		assertNotNull( actual );
		assertEquals(1, actual.size());
		assertTrue( actual.get(0).contains("110"));
	}

	@Test
	public void test_jsonDataBuilder_INVALID_PERCENTILE_3() {
		builder.percentiles("11O"); // typo of capitol O rather than 0
		builder.buildPercentiles();
		
		assertTrue( builder.hasErrors() );
//		 ENV_INVALID_PERCENTILE
		List<String> actual = fetchMessages();
		assertNotNull( actual );
		assertEquals(1, actual.size());
		assertTrue( actual.get(0).contains("11O"));
	}
}
