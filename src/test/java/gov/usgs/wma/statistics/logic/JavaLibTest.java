package gov.usgs.wma.statistics.logic;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.Test;

public class JavaLibTest {

	@Test
	public void test_stringFormat() {
		int count = 1;
		String msg = String.format("Removed %d provisional sample%s", count, count==1?"":"s");
		
		assertEquals("Removed 1 provisional sample", msg);
		
		count = 2;
		msg = String.format("Removed %d provisional sample%s", count, count==1?"":"s");
		
		assertEquals("Removed 2 provisional samples", msg);
	}

}
