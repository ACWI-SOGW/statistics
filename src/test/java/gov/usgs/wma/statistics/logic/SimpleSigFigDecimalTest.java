package gov.usgs.wma.statistics.logic;

import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.Test;

public class SimpleSigFigDecimalTest {

	@Test
	public void test_decimal_point_precision() {
		SimpleSigFigDecimal thousand_4 = new SimpleSigFigDecimal("1000");
		assertEquals("Should report 1000 as 4 without decimal point", 4,thousand_4.precision());
		
		thousand_4 = new SimpleSigFigDecimal("1000.");
		assertEquals("Should report 1000 as 1 with decimal point", 4,thousand_4.precision());
		
		SimpleSigFigDecimal ten_4 = new SimpleSigFigDecimal("10.00");
		assertEquals("Should report 1000 as 1 without decimal point", 4,ten_4.precision());
	}

	@Test
	public void test_zero_precision() {
		SimpleSigFigDecimal zero_2 = new SimpleSigFigDecimal("0.0");
		assertEquals("Should report 0.0 as 2 with decimal point because of 2 digits", 2,zero_2.precision());
		
		SimpleSigFigDecimal zero_4 = new SimpleSigFigDecimal(".0000");
		assertEquals("Should report .0000 as 4 ", 4,zero_4.precision());
		
		zero_4 = new SimpleSigFigDecimal("0.000");
		assertEquals("Should report 0.000 as 4", 4,zero_4.precision());
		
		SimpleSigFigDecimal sigfig_2 = new SimpleSigFigDecimal("0.00011");
		assertEquals("Should report 0.00011 as 2 ", 2,sigfig_2.precision());
	}
	
    @Test
    public void test_BigDecimal_1000() {
    	BigDecimal thousand = new SimpleSigFigDecimal("1000");
    	BigDecimal oneIsh = new BigDecimal("1.111");
    	
    	BigDecimal thousandOne = SigFigMathUtil.sigFigMultiply(thousand, oneIsh);
    	
    	assertEquals("This shows that SimpleSigFigsDecimal can work with BigDecimal and SigFigMathUtil",
    			// notice that the 1000 having 4 sigfigs in the simple impl
    			"1111", thousandOne.toPlainString());
    }
	
    @Test
    public void test_BigDecimal_1100() {
    	BigDecimal thousand = new SimpleSigFigDecimal("1100");
    	BigDecimal one = new BigDecimal("1.010");
    	
    	BigDecimal thousandOne = SigFigMathUtil.sigFigMultiply(thousand, one);
    	
    	assertEquals("This shows that scientific decimal can work with BigDecimal and SigFigMathUtil",
    			// notice that the 1100 has 4 sigfigs under simple sigfigs model 
    			"1111", thousandOne.toPlainString());
    }
	
    @Test
    public void test_BigDecimal_1000_decimal_point() {
    	BigDecimal thousand = new SimpleSigFigDecimal("1000.");
    	BigDecimal oneIsh = new BigDecimal("1.111");
    	
    	BigDecimal thousandOne = SigFigMathUtil.sigFigMultiply(thousand, oneIsh);
    	
    	assertEquals("This shows that scientific decimal can work with BigDecimal and SigFigMathUtil",
    			// notice that the 1000. having 4 sigfigs is properly handled
    			"1111", thousandOne.toPlainString());
    }
    
    @Test
    public void test_BigDecimal_1000_0() {
    	BigDecimal thousand_5 = new SimpleSigFigDecimal("1000.0");
    	assertEquals(5, thousand_5.precision());
    	
    	BigDecimal correct_5 = new BigDecimal("1000.0");
    	assertEquals(5, correct_5.precision());
    }
}
