package gov.usgs.wma.statistics.logic;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.Assert.*;

public class BigDecimalComplianceTest {

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
		assertNotEquals("demonstrates how elevation addition in double does not preserve precision", "12.34", ""+diff);
//		System.out.println(diff); // 12.340000000000034

		diff   = value - mp_elevation + alt_va;
		assertNotEquals("demonstrates how elevation addition in double does not preserve precision", "12.34", ""+diff);
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
	public void test_scale_vs_precision() {
		BigDecimal value = new BigDecimal("1.0000");

		assertEquals(5, value.precision() );
		assertEquals("1.0000", value.toString());
		assertEquals(4, value.scale() );
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
	public void test_BigDecimal_vs_DoubleAndFloat() {
		BigDecimal value = new BigDecimal(0.100);
		assertEquals(55, value.precision() );
		assertEquals("0.1000000000000000055511151231257827021181583404541015625", value.toString() );

		float tenthf = 0.1f;
		float onef = 0;
		for (int i=0; i<10; i++) {
			onef += tenthf;
		}
		assertNotEquals("Double tenth added 10 times is not 1.0 it is ", 1.0f, onef); // 1.0000001

		double fivef = 5f * onef;
		assertNotEquals("Then it propogates the double error to multiplications like 5x it is "
				,"5", ""+fivef); // 5.000000476837158
	}
}
