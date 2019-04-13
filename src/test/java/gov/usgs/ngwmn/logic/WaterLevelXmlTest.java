package gov.usgs.ngwmn.logic;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.usgs.ngwmn.model.Elevation;
import gov.usgs.ngwmn.model.Specifier;
import gov.usgs.wma.statistics.app.Properties;
import gov.usgs.wma.statistics.model.JsonData;
import gov.usgs.wma.statistics.model.JsonDataBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration //(locations = { "/applicationContext_mock.xml" })
public class WaterLevelXmlTest {

	final static File XML_FILE = new File("src/test/resources/sample-data/USGS_401229074290001_WATERLEVEL.xml");
	final static Reader XML_READER;
	
	static {
//		String xml = "";
		Reader xmlReader;
		try {
//			FileInputStream fin = new FileInputStream(XML_FILE);
//			FileReader xmlReader = new FileReader(XML_FILE);
//			BufferedReader br = new BufferedReader(xmlReader);
//			xml = br.readLine();
			xmlReader = new BufferedReader(new FileReader(XML_FILE));
		} catch (IOException e) {
//			xml = "";
			xmlReader = null;
		}
		XML_READER = xmlReader;
	}
	
	@Mock
	Environment spring;
	Properties env;
	WaterLevelStatistics stats = null;
	Specifier spec = new Specifier();
	JsonDataBuilder builder;
	Elevation elevation;

	@Before
	public void before() {
		env = new Properties().setEnvironment(spring);
		builder = new JsonDataBuilder(env);
		stats = new WaterLevelStatistics(env, builder);
		elevation = new Elevation(new Double(0), "testRef");
		spec = new Specifier("USGS", "401229074290001").setElevation(elevation);
	}
	
	@Test
	public void testXMLParcing() throws Exception {
		// pre-test
		assertTrue("xml file not found", XML_FILE.exists());
		
		// action under test
		JsonData json = stats.calculate(spec, XML_READER);
		
		// post-test
		assertNotNull("JSON data must be returned", json);
		assertNotNull("expected overall stats in JSON data", json.getOverall());
		assertNotNull("expected monthly stats in JSON data", json.getMonthly());
		assertEquals("lastest value is 4.99 in the test xml file", "4.99",json.getOverall().latestValue);
		assertEquals("lastest percentile in the test xml file is high", "0.9724",json.getOverall().latestPercentile);
	}

}
