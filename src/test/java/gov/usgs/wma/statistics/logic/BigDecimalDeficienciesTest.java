package gov.usgs.wma.statistics.logic;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.Assert.*;

public class BigDecimalDeficienciesTest {


	@Test
	public void test_BigDecimal_1000() {
		BigDecimal thousand = new BigDecimal("1000");
		BigDecimal one = new BigDecimal("1.111");

		BigDecimal thousandOne = SigFigMathUtil.sigFigMultiply(thousand, one);

		assertEquals("This is wrong the real value should be 1000", "1111", thousandOne.toPlainString());
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
	public void test_try_using_add_for_precision_on_zero() {
		BigDecimal value = new BigDecimal("1.0001");

		assertEquals(5, value.precision() );

		value = value.add( value.negate() );

		assertEquals("0.0000", value.toString());
		assertEquals("Gack, cannot use add to for precision on 0.0000", 1, value.precision() );
	}

	@Test
	public void test_BigDecimalHasPrecisionIssuesWithDecimalPoint() {
		//This is incorrect! 1000 is really only 1 sig fig
		//proper 4 sig figs is 1000.
		BigDecimal value = new BigDecimal("1000");
		assertEquals("1000", value.toPlainString());
		assertEquals("Incorrect Precision, should be 1", 4, value.precision());

		BigDecimal denominator = new BigDecimal("100.001");
		BigDecimal result = SigFigMathUtil.sigFigDivide(value, denominator);
		assertEquals("But maybe correct for GWW needs? 1000/100.001 rather than 10", "10.00", result.toString());
		assertEquals("precision should really be 2", 4, result.precision());
	}

	@Test
	public void test_BigDecimalPrecisionNotScientific() {
		//This is incorrect! 1000./100.00 is 10.00 not 1E+1 (or 10)
		BigDecimal numerator = new BigDecimal("1000.");
		BigDecimal denominator = new BigDecimal("100.00");
		BigDecimal result = SigFigMathUtil.sigFigDivide(numerator, denominator);

		assertEquals("Incorrect! 1000./100.00 should be 10.00", "10", result.toPlainString());
		assertEquals("Incorrect! 1000./100.00 should be 10.00", "1E+1", result.toString());
		assertEquals("Incorrect! 1000./100.00 have 4 precision", 1, result.precision());
	}

	@Test
	public void test_BigDecimalHasMorePrecision() {
		//This is incorrect! 1000/100.00 is 10.00 not 1E+1 (or 10)
		BigDecimal numerator = new BigDecimal("53.1").divide(new BigDecimal("100"));
		assertEquals("0.531", numerator.toPlainString());

		BigDecimal denominator = new BigDecimal("100.00");
		BigDecimal result = SigFigMathUtil.sigFigDivide(numerator, denominator);
		assertEquals("0.00531", result.toPlainString());

		BigDecimal scaled = denominator.setScale(10);
		assertEquals("100.0000000000", scaled.toPlainString());
		assertEquals(13, scaled.precision());
	}
	
	@Test
	public void test_multiplicationPrecisionNotManaged() {
		BigDecimal tenth = new BigDecimal("0.1");
		BigDecimal five = new BigDecimal("50").multiply(tenth);
		System.out.println("BigDecimal tenth x 50 is " + five); // 5.0

		assertNotEquals("50 and 0.1 only have on sigfig and the result should be 5 not 5.0",
				"5", five.toPlainString());
	}
}
