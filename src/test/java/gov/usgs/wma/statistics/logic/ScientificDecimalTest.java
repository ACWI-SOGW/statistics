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
    	
    	BigDecimal thousandOne = SigFigMathUtil.multiply(thousand, one);
    	
    	assertEquals("This shows that scientific decimal can work with BigDecimal and SigFigMathUtil",
    			// notice that the 1000 having only one digit is properly handled
    			"1000", thousandOne.toPlainString());
    }
	
    @Test
	public void test_fixMathmaticsScientificPrecisionOfTwoFigures() {
    	BigDecimal thousand = new ScientificDecimal("1100");
    	BigDecimal one = new BigDecimal("1.010");
    	
    	BigDecimal thousandOne = SigFigMathUtil.multiply(thousand, one);
    	
    	assertEquals("This shows that scientific decimal can work with BigDecimal and SigFigMathUtil",
    			// notice that the 1100, having only two digits, is properly handled with 1.010 having 4
    			"1100", thousandOne.toPlainString());
    }
	
    @Test
    public void test_fixTrailingDecimalPointPrecision() {
    	BigDecimal thousand = new ScientificDecimal("1000.");
    	BigDecimal one = new BigDecimal("1.111");
    	
    	BigDecimal thousandOne = SigFigMathUtil.multiply(thousand, one);
    	
    	assertEquals("This shows that scientific decimal can work with BigDecimal and SigFigMathUtil",
    			// notice that the "1000." having 4 digit precision is properly handled
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
		// BigDecimal is immutable. ScientificDecimal also now immutable

		int actual = hundred.sigfigRules("100");
		assertEquals(1, actual);
		assertEquals("100", hundred.toPlainString());

		actual = hundred.sigfigRules("100.");
		assertEquals(3, actual);
		assertEquals("100", hundred.toPlainString());

		actual = hundred.sigfigRules("100.0");
		// BigDecimal already properly assesses 100.0 precision properly and this method does not act on this.
		assertEquals("this should not update the precision (see comment)",4, actual);
	}

	@Test
	public void test_constructor_long() {
		BigDecimal actual = new ScientificDecimal(1L, 2);
		assertEquals(2, actual.precision());
		assertEquals(1, actual.scale());
		assertEquals("1.0", actual.toPlainString());

		actual = new ScientificDecimal(1000);
		assertEquals(1, actual.precision());
		assertEquals(0, actual.scale());
		assertEquals("1000", actual.toPlainString());

		actual = new ScientificDecimal(1001);
		assertEquals(4, actual.precision());
		assertEquals(0, actual.scale());
		assertEquals("1001", actual.toPlainString());
	}

	@Test
	public void test_constructor_string() {
		BigDecimal actual = new ScientificDecimal("1.0", 2);
		assertEquals(2, actual.precision());
		assertEquals("1.0", actual.toPlainString());

		actual = new ScientificDecimal("1.00", 2);
		assertEquals(2, actual.precision());
		assertEquals("1.0", actual.toPlainString());

		actual = new ScientificDecimal("1", 2);
		assertEquals(2, actual.precision());
		assertEquals("1.0", actual.toPlainString());

		actual = new ScientificDecimal("1.00", 1);
		assertEquals(1, actual.precision());
		assertEquals("1", actual.toPlainString());

		actual = new ScientificDecimal("1000", 3);
		assertEquals("1000", actual.toPlainString());
		assertEquals(3, actual.precision());

		actual = new ScientificDecimal("1000", 4);
		assertEquals("1000", actual.toPlainString());
		assertEquals(4, actual.precision());

		actual = new ScientificDecimal("1000", 5);
		assertEquals("1000.0", actual.toPlainString());
		assertEquals(5, actual.precision());
	}
	@Test
	public void test_construct_zero() {
		BigDecimal actual = new ScientificDecimal("0.0", 1);
		assertEquals(1, actual.precision());
		assertEquals("0.0", actual.toPlainString());

		// for the next two tests, it does not matter what the string is if the precision is correct.
		actual = new ScientificDecimal("0.00", 1);
		assertEquals(1, actual.precision());
		assertEquals("0.00", actual.toPlainString());

		actual = new ScientificDecimal("0", 1);
		assertEquals(1, actual.precision());
		assertEquals("0", actual.toPlainString());


		actual = new ScientificDecimal("0.0", 3);
		assertEquals(3, actual.precision());
		assertEquals("0.000", actual.toPlainString());

		actual = new ScientificDecimal("0.00", 3);
		assertEquals(3, actual.precision());
		assertEquals("0.000", actual.toPlainString());

		actual = new ScientificDecimal("0", 3);
		assertEquals(3, actual.precision());
		assertEquals("0.000", actual.toPlainString());
	}

	@Test
	public void test_updateScaleForSigFigs() {
		BigDecimal actual = new ScientificDecimal(BigDecimal.ZERO, 2);
		assertEquals(2, actual.precision());
		assertEquals("0.00", actual.toPlainString());
	}

	@Test
	public void test_setPrecision() {
		ScientificDecimal number = new ScientificDecimal("1.1");
		assertEquals(2, number.precision());

		BigDecimal actual = number.setPrecision(3);
		assertEquals("1.10", actual.toPlainString());
	}

	@Test
	public void test_setScale_oldModeInt() {
		ScientificDecimal number = new ScientificDecimal("1.1");
		assertEquals(1, number.scale());

		BigDecimal actual = number.setScale(2, BigDecimal.ROUND_HALF_DOWN);
		assertEquals("1.10", actual.toPlainString());
	}

	@Test
	public void test_constructionReducingPrecision() {
		ScientificDecimal nine31_2 = new ScientificDecimal("9.31", 2);
		ScientificDecimal nine31_3 = new ScientificDecimal("9.31", 3);
		ScientificDecimal zero_2 = new ScientificDecimal("0.00", 2);
		ScientificDecimal zero_3 = new ScientificDecimal("0.00", 3);
		BigDecimal nine3bd = new BigDecimal("9.3");
		BigDecimal nine31bd = new BigDecimal("9.31");
		BigDecimal five6 = new BigDecimal("5.6");

		// these should be equal and compareTo return zero
		assertEquals(-1, nine31_2.compareTo(nine3bd));
		assertEquals(1, nine3bd.compareTo(nine31_2));

		// this should return -l like the following two compare
		assertEquals(1, five6.compareTo(nine31_2));
		assertEquals(-1, five6.compareTo(nine31bd));
		assertEquals(-1, five6.compareTo(nine3bd));

		assertEquals(0, BigDecimal.ZERO.compareTo(zero_2));
		assertEquals(0, BigDecimal.ZERO.compareTo(zero_3));
		assertEquals(1, nine3bd.compareTo(zero_2));
		assertEquals(1, nine31bd.compareTo(zero_3));
	}

	@Test
	public void test_make() {
		BigDecimal actual;

		actual = ScientificDecimal.make("9.31", 3);
		assertEquals(BigDecimal.class, actual.getClass());
		assertEquals("9.31", actual.toPlainString());
		assertEquals(2, actual.scale());
		assertEquals(3, actual.precision());

		actual = ScientificDecimal.make("9.31", 2);
		assertEquals(BigDecimal.class, actual.getClass());
		assertEquals("9.3", actual.toPlainString());
		assertEquals(1, actual.scale());
		assertEquals(2, actual.precision());

		actual = ScientificDecimal.make("0.0031", 2);
		assertEquals(BigDecimal.class, actual.getClass());
		assertEquals("0.0031", actual.toPlainString());
		assertEquals(4, actual.scale());
		assertEquals(2, actual.precision());

		actual = ScientificDecimal.make("900", 3);
		assertEquals(BigDecimal.class, actual.getClass());
		assertEquals("900", actual.toPlainString());
		assertEquals(0, actual.scale());
		assertEquals(3, actual.precision());

		actual = ScientificDecimal.make("900", 2);
		assertEquals(BigDecimal.class, actual.getClass());
		assertEquals("900", actual.toPlainString());
		assertEquals(-1, actual.scale());
		assertEquals(2, actual.precision());

		actual = ScientificDecimal.make("900", 1);
		assertEquals(BigDecimal.class, actual.getClass());
		assertEquals("900", actual.toPlainString());
		assertEquals(-2, actual.scale());
		assertEquals(1, actual.precision());

		actual = ScientificDecimal.make("0.00", 2);
		assertEquals(ScientificDecimal.class, actual.getClass());
		assertEquals("0.00", actual.toPlainString());
		assertEquals(2, actual.scale());
		assertEquals(2, actual.precision());


	}

}
