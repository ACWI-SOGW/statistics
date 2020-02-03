package gov.usgs.wma.statistics.logic;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import static org.junit.Assert.*;

public class BigDecimalComplianceTest {

	/*
	 * Examples and tests of the inadequacies of Double and Float
	 */

	@Test
	public void test_floatDeficient() {
		// "demonstrates how base 2 fractional addition is not always accurate
		float tenthFloat = 0.1f;
		float oneFloat = 0;
		for (int i=0; i<10; i++) {
			oneFloat += tenthFloat;
		}
		assertNotEquals("Float tenth added 10 times is not 1.0 ", 1.0f, oneFloat); // 1.0000001
//		System.out.println(oneFloat); // 1.0000001

		BigDecimal one = BigDecimal.ZERO;
		BigDecimal tenth = new BigDecimal("0.1"); // requires string
		for (int i = 0; i < 10; i++) {
			one = one.add(tenth);
		}
		assertEquals("demonstrates how BigDecimal addition is more accurate", "1.0", one.toString());
//		System.out.println(one); // 1.0

		double fiveFloat = 5f * oneFloat;
		assertNotEquals("demonstrates binary math is inaccurate ", "5.0", ""+fiveFloat);
//		System.out.println(fiveFloat); // 5.000000476837158
	}

	@Test
	public void test_doubleDeficient() {
		// represents a site elevation calculation
		// site: ISWS-P360675   date: 1995-03-01
		double alt_va = 454.6;
		double mp_elevation = 456.2;
		double value  = 13.94;
		double diff   = value - (mp_elevation - alt_va);
		assertNotEquals("demonstrates how elevation addition in double does not preserve precision", "12.34", ""+diff);
//		System.out.println(diff); // 12.340000000000034

		diff   = value - mp_elevation + alt_va;
		assertNotEquals("demonstrates how elevation addition in double does not preserve precision", "12.34", ""+diff);
//		System.out.println(diff); // 12.340000000000032
	}

	@Test
	public void test_BigDecimalCannotBeConstructedWithDouble() {
		// demonstrates how 0.1(base 2) is not equal to 0.1(base 10)
		BigDecimal tenth = new BigDecimal(0.100);
		assertEquals(55, tenth.precision() );
		assertEquals("Using Double to construct a BigDecimal introduces the Double error into BigDecimal",
				"0.1000000000000000055511151231257827021181583404541015625", tenth.toString() );
//		System.out.println(tenth); // 0.1000000000000000055511151231257827021181583404541015625

		tenth = new BigDecimal("0.1");
		assertEquals("demonstrates how BigDecimal can precisely represent fractions ", "0.1", "0.1");
//		System.out.println(tenth); // 0.1
	}



	/*
	 * Testing the BigDecimal API to understand what its capabilities are.
	 */
	@Test
	public void test_scale_vs_precision() {
		BigDecimal value = new BigDecimal("1.0000");

		assertEquals("Scale if the number of digits for the fraction ", 4, value.scale() );

		assertEquals("Precision is all relevant or significant digits",5, value.precision() );
		assertEquals("1.0000", value.toString());
	}

	@Test
	public void test_setScale() {
		BigDecimal value = new BigDecimal("42.0");
		value = value.setScale(3);

		assertEquals("Setting the scale sets the plainString fraction digits",
				"42.000", value.toPlainString());
		assertEquals("Setting the scale effects the precision also.",5, value.precision() );
	}

	@Test
	public void test_BigDecimalScaleInPractice() {
		// testing if understanding of divide and rounding to the expect scale as expected

		BigDecimal twelve = new BigDecimal(12);
		BigDecimal eight  = new BigDecimal(8);
		BigDecimal six    = new BigDecimal(6);
		BigDecimal four   = new BigDecimal(4);

		String val = six.divide(twelve, 1, RoundingMode.HALF_UP).toString();
		assertEquals("0.5", val);

		val = six.divide(twelve, 3, RoundingMode.HALF_UP).toString();
		assertEquals("0.500", val);

		val = four.divide(twelve, 1, RoundingMode.HALF_UP).toString();
		assertEquals("","0.3", val);

		val = four.divide(twelve, 10, RoundingMode.HALF_UP).toString();
		assertEquals("","0.3333333333", val);

		val = eight.divide(six, 1, RoundingMode.HALF_UP).toString();
		assertEquals("Only one fractional digit calculated","1.3", val);

		val = eight.divide(six, 2, RoundingMode.HALF_UP).toString();
		assertEquals("scale is not the full precision. it is the figures of the fraction","1.33", val);
	}

	@Test
	public void test_percision() {
		String message = "Precision should be compliant with ";
		BigDecimal value;
		int actual;
		int expect;


		expect = 7;
		value = new BigDecimal("130001.0");
		actual = value.precision();
		assertEquals(message+expect, expect, actual);
		assertEquals(value.toString(), value.toPlainString());

		expect = 30;
		value = new BigDecimal("12345678901234567890.1234567890");
		actual = value.precision();
		assertEquals(message+expect, expect, actual);
		assertEquals(value.toString(), value.toPlainString());

		expect = 2;
		value = new BigDecimal("0.25");
		actual = value.precision();
		assertEquals(message+expect, expect, actual);
		assertEquals(value.toString(), value.toPlainString());

		expect = 3;
		value = new BigDecimal("0.250");
		actual = value.precision();
		assertEquals(message+expect, expect, actual);
		assertEquals(value.toString(), value.toPlainString());

		expect = 4;
		value = new BigDecimal("-0.2502");
		actual = value.precision();
		assertEquals(message+expect, expect, actual);
		assertEquals(value.toString(), value.toPlainString());

		expect = 3;
		value = new BigDecimal("-1.23E-10");
		actual = value.precision();
		assertEquals(message+expect, expect, actual);
		assertNotSame("plain string should present as .000000000123", value.toString(), value.toPlainString());
	}

	@Test
	public void test_arithmeticPrecision() {

		// This could be in both compliance or deficiency tests.
		// It demonstrates that figures are preserved but does not
		// properly manage significant figures

		String expect = "1010.00";

		BigDecimal actual = new BigDecimal("1000.")
				.add( new BigDecimal("10.00") );
		assertEquals(6, actual.precision());

		assertEquals(expect, actual.toString());
	}
	@Test
	public void test_leadingZerosPrecision() {
		BigDecimal value = new BigDecimal("0.0001");
		assertEquals(1, value.precision() );
		assertEquals(4, value.scale() );

		value = new BigDecimal("000.0001");
		assertEquals(1, value.precision() );
		assertEquals(4, value.scale() );
	}

	@Test
	public void test_trailingFractionZerosPrecision() {
		BigDecimal value = new BigDecimal("0.00010");
		assertEquals(2, value.precision() );
		assertEquals(5, value.scale() );

		value = new BigDecimal("000.000100");
		assertEquals(3, value.precision() );
		assertEquals(6, value.scale() );
	}

	@Test
	public void test_BigDecimalScientificPrecision() {
		// This is correct! 1000./100.0 is 10.00 but you must know to set the proper scale
		BigDecimal numerator = new BigDecimal("1000.");
		BigDecimal denominator = new BigDecimal("100.0");
		BigDecimal result = numerator.divide(denominator, 1, RoundingMode.HALF_UP);
		assertEquals("Correct! 1000./100.0 should be 10.00", "10.0", result.toPlainString());
	}

	@Test
	public void test_BigDecimalRounding() {
		BigDecimal roundDown = new BigDecimal("1.0500");
		BigDecimal roundUpUp = new BigDecimal("1.0501");
		BigDecimal actualDown = roundDown.round(new MathContext(2, RoundingMode.HALF_DOWN));
		BigDecimal actualUpUp = roundDown.round(new MathContext(2, RoundingMode.HALF_UP));
		BigDecimal actualUpDown = roundUpUp.round(new MathContext(2, RoundingMode.HALF_DOWN));
		assertEquals("", "1.0", actualDown.toPlainString());
		assertEquals("", "1.1", actualUpUp.toPlainString());
		assertEquals("", "1.1", actualUpDown.toPlainString());
	}

}
