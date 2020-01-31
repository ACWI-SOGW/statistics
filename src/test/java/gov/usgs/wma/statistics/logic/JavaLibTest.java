package gov.usgs.wma.statistics.logic;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.Test;

public class JavaLibTest {


	@Test
	public void test_BigDecimal_1000() {
		BigDecimal thousand = new BigDecimal("1000");
		BigDecimal one = new BigDecimal("1.111");

		BigDecimal thousandOne = SigFigMathUtil.sigFigMultiply(thousand, one);

		assertEquals("This is wrong the real value should be 1000", "1111", thousandOne.toPlainString());
	}

	@Test
	public void testBigDecimalScale() {
		// testing if understanding of divide and rounding to the expect scale as expected

		BigDecimal twelve = new BigDecimal(12);

		int a = 10;

		int b = 6;
		String val = new BigDecimal(a).subtract(new BigDecimal(b))
		.divide(twelve, 1, RoundingMode.HALF_EVEN).toString();

		assertEquals("0.3", val);

		b = 4;
		val = new BigDecimal(a).subtract(new BigDecimal(b))
		.divide(twelve, 1, RoundingMode.HALF_EVEN).toString();

		assertEquals("0.5", val);
	}

	  
	/**
	 * Test of sigFig, of class SigFigMathUtil.
	 *
	 */
	@Test
	public void testSigFigs() {
		BigDecimal bd1 = new BigDecimal("130001.0");
		int precision1 = 7;
		assertEquals(precision1, bd1.precision());
		assertEquals(bd1.toString(), bd1.toPlainString());

		BigDecimal bd2 = new BigDecimal("12345678901234567890.1234567890");
		int precision2 = 30;
		assertEquals(precision2, bd2.precision());
		assertEquals(bd2.toString(), bd2.toPlainString());

		BigDecimal bd3 = new BigDecimal("0.25");
		int precision3 = 2;
		assertEquals(precision3, bd3.precision());
		assertEquals(bd3.toString(), bd3.toPlainString());

		BigDecimal bd4 = new BigDecimal("0.250");
		int precision4 = 3;
		assertEquals(precision4, bd4.precision());
		assertEquals(bd4.toString(), bd4.toPlainString());
		
		BigDecimal bd5 = new BigDecimal("-0.2502");
		int precision5 = 4;
		assertEquals(precision5, bd5.precision());
		assertEquals(bd5.toString(), bd5.toPlainString());

		BigDecimal bd6 = new BigDecimal("-1.23E-10");
		int precision6 = 3;
		assertEquals(precision6, bd6.precision());
		assertNotSame(bd6.toString(), bd6.toPlainString());

		BigDecimal bd7 = new BigDecimal("1.23E-10");
		int precision7 = 3;
		assertEquals(precision7, bd7.precision());
		assertNotSame(bd7.toString(), bd6.toPlainString()); //note that plain string will present as .000000000123
	}
	
	@Test
	public void test_big_decimal_vs_binary_representation() {
		BigDecimal tenth = new BigDecimal(0.1);
		assertNotEquals("demonstrates how 0.1(base 2) is not equal to 0.1(base 10)", "0.1", tenth.toString());
//		System.out.println(tenth); // 0.1000000000000000055511151231257827021181583404541015625

		tenth = new BigDecimal("0.1");
		assertEquals("demonstrates how BigDecimal can represent fractions", "0.1", "0.1");
//		System.out.println(tenth); // 0.1
	}
	
	@Test
	public void test_BigDecimal_vs_binary_addition() {
		float tenthf = 0.1f;
		float onef = 0;
		for (int i=0; i<10; i++) {
		  onef += tenthf;
		}
		assertNotEquals("demonstrates how base 2 fractional addition is not always accurate", "1.0", ""+onef);
//		System.out.println(onef); // 1.0000001

		BigDecimal one = BigDecimal.ZERO;
		BigDecimal tenth = new BigDecimal("0.1"); // requires string
		for (int i=0; i<10; i++) {
		  one = one.add(tenth);
		}
		assertEquals("demonstrates how BigDecimal addition is more accurate", "1.0", one.toString());
//		System.out.println(one); // 1.0
	}
	
	@Test
	public void test_BigDecimal_vs_binary_multiplication() {
		float tenthf = 0.1f;
		float onef = 0;
		for (int i=0; i<10; i++) {
		  onef += tenthf;
		}
		
		double fivef = 5f * onef;
		assertNotEquals("demonstrates how inaccurate math is propogated", "5.0", ""+fivef);
//		System.out.println(fivef); // 5.000000476837158

		BigDecimal five = new BigDecimal("50");
		BigDecimal tenth = new BigDecimal("0.1"); // requires string
		five = five.multiply(tenth);
		assertEquals("demonstrates how BigDecimal math propogates accuracy", "5.0", five.toString());
//		System.out.println(five); // 5.0
	}
	
	@Test
	public void test_representation_of_site_elevation_math_concerns() {
		// site: ISWS-P360675   date: 1995-03-01
		double alt_va = 454.6;
		double mp_elevation = 456.2;
		double value  = 13.94;
		double diff   = value - (mp_elevation - alt_va);
		assertNotEquals("demonstrates how elevation addition in double does not precision", "12.34", ""+diff);
//		System.out.println(diff); // 12.340000000000034
		
		diff   = value - mp_elevation + alt_va;
		assertNotEquals("demonstrates how elevation addition in double does not precision", "12.34", ""+diff);
//		System.out.println(diff); // 12.340000000000032
	}
	
	@Test
	public void test_addition_and_multiplication_precision() {
		String expect = "1010.00";
		
		BigDecimal result = new BigDecimal("1000.")
				.add( new BigDecimal("10.00") );
		assertEquals(6, result.precision());

		String actual = result.toString();
		assertEquals(expect, actual);
		
		BigDecimal resu1t = new BigDecimal("1000.")
				.add( new BigDecimal("10.00") );
		assertEquals(6, resu1t.precision());
		
		String actua1 = new BigDecimal("1000.")
						.add( new BigDecimal("10.00") )
						.toString();
		assertEquals(expect, actua1);		
	}
	
	@Test
	public void test_precision_of_zero() {
		BigDecimal value = new BigDecimal("0.0000");
		
		assertEquals("actually we want 0.0000 to be 4 but BD it is 1 as of Java 8", 1, value.precision() );
		assertEquals("0.0000", value.toString());
		assertEquals("but scale does record the precition of 0.0000 properly", 4, value.scale() );
		assertEquals("and precision is still 1", 1, value.precision() );
	}
	
	@Test
	public void test_scale_vs_precision() {
		BigDecimal value = new BigDecimal("1.0000");
		
		assertEquals(5, value.precision() );
		assertEquals("1.0000", value.toString());
		assertEquals(4, value.scale() );
	}
	
	@Test
	public void test_try_using_add_for_precision_on_zero() {
		BigDecimal value = new BigDecimal("1.0001");
		
		assertEquals(5, value.precision() );

		value = value.add( value.negate() );
		
		assertEquals("0.0000", value.toString());
		assertEquals("Gack, cannot use add to for precision on 0.0000", 1, value.precision() );
	}

	@Test
	public void test_proper_precision_of_leading_zeros() {
		BigDecimal value = new BigDecimal("0.0001");
		
		assertEquals(1, value.precision() );
	}
	
	@Test
	public void test_setScale_sets_decimal_places() {
		BigDecimal value = new BigDecimal("42.0");
		value = value.setScale(3);
		assertEquals("42.000", value.toPlainString());
		assertEquals(5, value.precision() );
	}
	
	@Test
	public void test_BigDecimal_missingDecimalPoint() {
		//This is incorrect! 1000 is really only 1 sig fig
		//proper 4 sig figs is 1000.
		BigDecimal value = new java.math.BigDecimal("1000");
		assertEquals("1000", value.toPlainString());
		assertEquals("Incorrect Precision, should be 1", 4, value.precision());
		
		BigDecimal denominator = new BigDecimal("100.001");
		BigDecimal result = SigFigMathUtil.sigFigDivide(value, denominator);
		assertEquals("But maybe correct for GWW needs? 1000/100.001 rather than 10", "10.00", result.toString());
		assertEquals("precision should really be 2", 4, result.precision());
	}
	
	@Test
	public void test_BigDecimal_has_more_precision_issues() {
		//This is incorrect! 1000./100.00 is 10.00 not 1E+1 (or 10)
		BigDecimal numerator = new java.math.BigDecimal("1000.");
		BigDecimal denominator = new java.math.BigDecimal("100.00");
		BigDecimal result = SigFigMathUtil.sigFigDivide(numerator, denominator);

		assertEquals("Incorrect! 1000./100.00 should be 10.00", "10", result.toPlainString());
		assertEquals("Incorrect! 1000./100.00 should be 10.00", "1E+1", result.toString());
		assertEquals("Incorrect! 1000./100.00 have 4 precision", 1, result.precision());
	}

	@Test
	public void test_BigDecimal_has_more_precision() {
		//This is incorrect! 1000/100.00 is 10.00 not 1E+1 (or 10)
		BigDecimal numerator = new BigDecimal("53.1").divide(new BigDecimal("100"));;
		assertEquals("0.531", numerator.toPlainString());

		BigDecimal denominator = new BigDecimal("100.00");
		BigDecimal result = SigFigMathUtil.sigFigDivide(numerator, denominator);
		assertEquals("0.00531", result.toPlainString());

		BigDecimal scaled = denominator.setScale(10);
		assertEquals("100.0000000000", scaled.toPlainString());
		assertEquals(13, scaled.precision());
	}
	
	@Test
	public void test_BigDecimal_has_more_precision_from_Double() {
		BigDecimal value = new BigDecimal(0.100);
		assertEquals(55, value.precision() );
		assertEquals("0.1000000000000000055511151231257827021181583404541015625", value.toString() );
	}

	@Test
	public void test_string_format() {
		int count = 1;
		String msg = String.format("Removed %d provisional sample%s", count, count==1?"":"s");
		
		assertEquals("Removed 1 provisional sample", msg);
		
		count = 2;
		msg = String.format("Removed %d provisional sample%s", count, count==1?"":"s");
		
		assertEquals("Removed 2 provisional samples", msg);
	}


	@Test
	public void test_multiplication() {
		BigDecimal five = new BigDecimal("50");
		BigDecimal tenth = new BigDecimal("0.1");
		five = five.multiply(tenth);
		System.out.println("BigDecimal tenth x 50 is " + five); // 5.0
	}


	public static void main(String[] args) {
		float tenthf = 0.1f;
		float onef = 0;
		for (int i=0; i<10; i++) {
			onef += tenthf;
		}
		System.out.println("Double tenth added 10 times is not 1.0 it is " + onef); // 1.0000001

		double fivef = 5f * onef;
		System.out.println("Then it propogates the double error to "
				+ "multiplications like 5x it is " + fivef); // 5.000000476837158
	}
	
}
