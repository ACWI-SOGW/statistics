package gov.usgs.wma.statistics.logic;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class UsgsDecimalTest {

	@Test
	public void test_constructor() {
		UsgsDecimal thousand_3 = new UsgsDecimal("1000", 3);
		assertEquals("1000", thousand_3.toPlainString());
		assertEquals(3, thousand_3.precision());

		UsgsDecimal thousand_4 = new UsgsDecimal("1000", 4);
		assertEquals("1000", thousand_4.toPlainString());
		assertEquals(4, thousand_4.precision());

		UsgsDecimal thousand_5 = new UsgsDecimal("1000", 5);
		assertEquals("1000.0", thousand_5.toPlainString());
		assertEquals(5, thousand_5.precision());
	}


	@Test
	public void test_noDecimalPointPrecision() {
		// with data from
		UsgsDecimal thousand_4 = new UsgsDecimal("1000");
		assertEquals("Should report 1000 as 4 without decimal point", 4,thousand_4.precision());

    	BigDecimal thousand = new UsgsDecimal("1000");
    	BigDecimal oneIsh = new BigDecimal("1.111");
    	
    	BigDecimal thousandOne = SigFigMathUtil.sigFigMultiply(thousand, oneIsh);
    	
    	assertEquals("This shows that SimpleSigFigsDecimal can work with BigDecimal and SigFigMathUtil",
    			// notice that the 1000 having 4 sigfigs in the simple impl
    			"1111", thousandOne.toPlainString());

    	BigDecimal thousand_Eleven = new UsgsDecimal("1100");
    	BigDecimal one = new BigDecimal("1.010");
    	
    	BigDecimal thousandOneOneOne = SigFigMathUtil.sigFigMultiply(thousand_Eleven, one);
    	
    	assertEquals("This shows that scientific decimal can work with BigDecimal and SigFigMathUtil",
    			// notice that the 1100 has 4 sigfigs under simple sigfigs model 
    			"1111", thousandOneOneOne.toPlainString());
    }
	
}
