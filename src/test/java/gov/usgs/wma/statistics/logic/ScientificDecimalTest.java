package gov.usgs.wma.statistics.logic;

import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.Test;

public class ScientificDecimalTest {

	@Test
	public void test_fixDecimalPointPrecision() {
		ScientificDecimal thousand_1 = new ScientificDecimal("1000");
		assertEquals("Should report 1000 as 1 without decimal point", 1,thousand_1.precision());
		
		BigDecimal wrong_1 = new BigDecimal("1000");
		assertEquals("BigDecimal reports 1000 as 4 - wrong", 4,wrong_1.precision());

		
		ScientificDecimal thousand_4 = new ScientificDecimal("1000.");
		assertEquals("Should report 1000 as 1 with decimal point", 4,thousand_4.precision());
		
		BigDecimal correct_4 = new BigDecimal("1000.");
		assertEquals("BigDecimal reports 1000. as 4 - correct", 4,correct_4.precision());

		
		ScientificDecimal ten_4 = new ScientificDecimal("10.00");
		assertEquals("Should report 1000 as 1 without decimal point", 4,ten_4.precision());
		
		correct_4 = new BigDecimal("10.00");
		assertEquals("BigDecimal reports 10.00 as 4 - correct", 4,correct_4.precision());
	}

	@Test
	public void test_fixPrecisionOfZero() {
		ScientificDecimal zero_1 = new ScientificDecimal("0.0");
		assertEquals("Should report 0.0 as 1 with decimal point", 1,zero_1.precision());
		
		BigDecimal correct_1 = new BigDecimal("0.0");
		assertEquals("BigDecimal reports 0.0 as 1 - correct for the wrong reason", 1,correct_1.precision());

		
		ScientificDecimal zero_4 = new ScientificDecimal(".0000");
		assertEquals("Should report .0000 as 4 ", 4,zero_4.precision());
		
		BigDecimal wrong_4 = new BigDecimal(".0000");
		assertEquals("BigDecimal reports .0000 as 1 - wrong", 1,wrong_4.precision());

		
		ScientificDecimal zero_3 = new ScientificDecimal("0.000");
		assertEquals("Should report 0.000 as 3 ", 3,zero_3.precision());
		
		BigDecimal wrong_3 = new BigDecimal("0.000");
		assertEquals("BigDecimal reports 0.0000 as 1 - wrong", 1,wrong_3.precision());

		
		ScientificDecimal sigfig_2 = new ScientificDecimal("0.00011");
		assertEquals("Should report 0.00011 as 2 ", 2,sigfig_2.precision());
		
		BigDecimal correct_2 = new BigDecimal("0.00011");
		assertEquals("BigDecimal reports 0.00011 as 2 - correct", 2,correct_2.precision());
	}
	
    @Test
    public void test_fixMathmaticsScientificPrecisionOfOneFigure() {
    	BigDecimal thousand = new ScientificDecimal("1000");
    	BigDecimal one = new BigDecimal("1.111");
    	
    	BigDecimal thousandOne = SigFigMathUtil.sigFigMultiply(thousand, one);
    	
    	assertEquals("This shows that scientific decimal can work with BigDecimal and SigFigMathUtil",
    			// notice that the 1000 having only one sigfig is properly handled
    			"1000", thousandOne.toPlainString());
    }
	
    @Test
	public void test_fixMathmaticsScientificPrecisionOfTwoFigures() {
    	BigDecimal thousand = new ScientificDecimal("1100");
    	BigDecimal one = new BigDecimal("1.010");
    	
    	BigDecimal thousandOne = SigFigMathUtil.sigFigMultiply(thousand, one);
    	
    	assertEquals("This shows that scientific decimal can work with BigDecimal and SigFigMathUtil",
    			// notice that the 1100 having only two sigfig is properly handled with 1.010 having 4
    			"1100", thousandOne.toPlainString());
    }
	
    @Test
    public void test_fixTrailingDecimalPointPrecision() {
    	BigDecimal thousand = new ScientificDecimal("1000.");
    	BigDecimal one = new BigDecimal("1.111");
    	
    	BigDecimal thousandOne = SigFigMathUtil.sigFigMultiply(thousand, one);
    	
    	assertEquals("This shows that scientific decimal can work with BigDecimal and SigFigMathUtil",
    			// notice that the 1000. having 4 sigfigs is properly handled
    			"1111", thousandOne.toPlainString());
    }
    
    @Test
    public void test_handleFractionalZeroPrecision() {
    	BigDecimal thousand_5 = new ScientificDecimal("1000.0");
    	assertEquals(5, thousand_5.precision());
    	
    	BigDecimal correct_5 = new BigDecimal("1000.0");
    	assertEquals(5, correct_5.precision());
    }

    @Test
	public void test_sigfigRules() {
		// this is not how to use the method, just a means to test
		ScientificDecimal hundred = new ScientificDecimal("100");
		// TODO asdf I guess BigDecimal is immutable. This should too?

		hundred.sigfigRules("100");
		assertEquals(1, hundred.precision());
		assertEquals("100", hundred.toPlainString());

		hundred.sigfigRules("100.");
		assertEquals(3, hundred.precision());
		assertEquals("100", hundred.toPlainString());

		hundred.sigfigRules("100.0");
		// BigDecimal already properly assesses 100.0 precision properly and this method does not act on this.
		assertEquals("this should not update the precision (see comment)",3, hundred.precision());
	}
}
