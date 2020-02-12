package gov.usgs.wma.statistics.logic;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class BigDecimalDeficienciesTest {


	@Test
	public void test_BigDecimalPrecisionNotScientific() {
		// This is incorrect! 1000./100.00 is 10.00 not 1E+1 (or 10)
		BigDecimal numerator = new BigDecimal("1000.");
		BigDecimal denominator = new BigDecimal("100.00");
		BigDecimal actual = numerator.divide(denominator, RoundingMode.HALF_UP);

		assertEquals("Incorrect! 1000./100.00 should be 10.00", "10", actual.toPlainString());
		// it has a precision of 2 because of the rounding mode, see next assertion.
		assertEquals("Incorrect! 1000./100.00 have 4 precision", 2, actual.precision());

		// note that if the rounding mode is omitted then the precision is 1, not 2. see prior assertion.
		BigDecimal noRoundingMode = numerator.divide(denominator);
		assertEquals("Incorrect! 1000./100.00 have 4 precision", 1, noRoundingMode.precision());

		// MathContext precision does not always help
		MathContext mc = new MathContext(4);
		actual = numerator.divide(denominator, mc);
		assertEquals("Incorrect! 1000./100.00 should be 10.00", "10", actual.toPlainString());

		// can overcome the deficiency by setting scale but you must know what scale to set.
		// also note that BigDecimal instances are immutable and the modification is returned.
		actual = actual.setScale(2);
		assertEquals("Correct! 1000./100.00 should be 10.00", "10.00", actual.toPlainString());
	}

	@Test
	/**
	 * 	This is wrong in the strictest sense of significant figures management.
	 * 	1000 has only one significant figure. 1000.0 has 5 significant figures.
	 * 	For 1000 to have 4 significant figure should be "1000." with a point
	 * 	after the last zero. For 3 figures then scientific notation is required.
	 * 	"1.00 x 10^4" or "1.00e4"
 	 */
	public void test_trailingZerosBeforeMissingDecimalPoint() {
		//This is incorrect! 1000 is really only 1 sig fig
		//proper 4 sig figs is 1000.
		BigDecimal thousand = new BigDecimal("1000");
		assertEquals("1000", thousand.toPlainString());
		assertEquals("Incorrect Precision, should be 1", 4, thousand.precision());

		BigDecimal onePoint111 = new BigDecimal("1.111");
		BigDecimal thousandOneOneOne = thousand.multiply(onePoint111);

		// This must set to the wrong expectation to match the incorrect expectation.
		// However, we know that BigDecimal does not properly implement significant figures rules.
		// see the SigFigMatUtil class for improved compliance.
		assertEquals("This is wrong the real value should be 1000", "1111.000", thousandOneOneOne.toPlainString());
	}

	@Test
	/**
	 * There is no means to set the precision on BigDecimal directly.
	 * Any attempt to set the scale to influence the precision results in a single digit.
	 * Looking at the SDK src, it appears that any time zero is encountered,
	 * the precision is hard coded to a single digit.
	 */
	public void test_precisionOfZeroAlways1() {
		BigDecimal value = new BigDecimal("0.0000");

		assertEquals("actually we want 0.0000 to be 4 but BD it is 1 as of Java 8 thru 13", 1, value.precision() );
		assertEquals("Even though it hard codes the precision as 1, it retains the given digits.","0.0000", value.toPlainString());
		assertEquals("Even the plain string retains the zeros in the digits.","0.0000", value.toPlainString());
		assertEquals("but scale does record the precision of 0.0000 properly", 4, value.scale() );

		// The following tries to use math to cause a zero and test
		// if it is only the constructor that sets zero with 1 precision.
		value = new BigDecimal("1.0001");
		assertEquals(5, value.precision() );

		value = value.add( value.negate() );
		assertEquals("During math it also is not preserving precision.","0.0000", value.toPlainString());

		// Nope. Even after arithmetic operations the BigDecimal implementation
		// sets zero with precision 1. After this, I checked the SDK src.
		// When zero is encountered the precision is set to 1
		assertEquals("Gack, cannot use add to for precision on 0.0000", 1, value.precision() );
	}

	@Test
	public void test_multiplicationPrecisionNotManaged() {
		// Shows how precision is not properly preserved
		BigDecimal fiftyThreeOne = new BigDecimal("53.1");
		BigDecimal oneHundred    = new BigDecimal("100");
		BigDecimal result = fiftyThreeOne.divide(oneHundred);
		assertEquals("Incorrect precision 3 divided by a precision 1 should result in precision 1 result",
				3, result.precision());
		assertEquals("Incorrect precision 3 result number",
				"0.531", result.toPlainString());

		BigDecimal denominator = new BigDecimal("100.00");
		result = result.divide(denominator);
		assertEquals("Incorrect precision 3 divided by a precision 5 should result in precision 3 result",
				3, result.precision());
		assertEquals("Incorrect lesser precision 3","0.00531", result.toPlainString());

		BigDecimal tenth = new BigDecimal("0.1");
		BigDecimal five = new BigDecimal("50").multiply(tenth);

		// BigDecimal tenth x 50 is 5.0
		assertNotEquals("50 and 0.1 only have on sigfig and the result should be 5 not 5.0",
				"5", five.toPlainString());
	}

	@Test
	public void test_scaleAndPrecisionAreNotSignificantFigures() {
		assertEquals(3, new BigDecimal("1.000").scale());
		assertEquals(4, new BigDecimal("1.000").precision());

		assertEquals(0, new BigDecimal("1000").scale());
		assertEquals(4, new BigDecimal("1000").precision());

		assertEquals(0, new BigDecimal("1000.").scale());
		assertEquals(4, new BigDecimal("1000.").precision());

		assertEquals(4, new BigDecimal("0.0001").scale());

		// TODO asdf I have seen the percentile reported as all digits after the decimal point
		assertEquals(1, new BigDecimal("0.0001").precision());
//		assertEquals(4, new BigDecimal("0.0001").precision());
	}
	
}
