package gov.usgs.wma.statistics.logic;

import static gov.usgs.wma.statistics.logic.ScientificDecimal.EXACT_SCALE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BinaryOperator;

/**
 * The purpose of this class is to provide a simplistic, consistent method to
 * add, subtract, multiply and divide BigDecimals in a manner that applies the
 * business interpretation of significant figures. In ambiguous cases where the
 * decimal is not present, it is assumed that the decimal point follows the
 * number. Example: 4000 will be stated as containing 4 significant figures.
 * Also, a default RoundingMode, HALF_EVEN, is applied as defined in the Java
 * math spec. Significant Figure rules are to be applied after the calculation
 * is completed. 1) For addition and subtraction, only the decimal place of the
 * last significant figure in each of the terms matters; the total number of
 * significant figures in each term is irrelevant. 2) For multiplication and
 * division, only the total number of significant figures in each of the factors
 * matter.
 *
 * @author smlarson
 * @author duselman
 */
public class SigFigMathUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(SigFigMathUtil.class);
    
    // July 19 2016 - changed for GWW /JL. It was HALF_EVEN for all values and must be round half up for all values.
    // July 30 2018 - Change of specification - they want the default Java behavior back.
    // This custom rounding rule class allows for future rounding changes including the former custom rule from 2017.
    // Jan  31 2020 - the rounding rule from Java, HALF_DOWN, seems to match the rule USGS desires.
    public static RoundingMode DEFAULT_ROUNDING_RULE = RoundingMode.HALF_DOWN;

    public static BigDecimal logMissingValues() {
        LOGGER.warn("Value not provided. Can not determine value.");
        return null;
    }
    public static BigDecimal logMissingRoundingRule() {
        LOGGER.warn("RoundingMode not defined. Use the 2 arg method which uses the default rounding rule.");
        return null;
    }

    protected static int decsendingScale(BigDecimal a, BigDecimal b) {
        Integer aScale = a.scale();
        Integer bScale = b.scale();

        return bScale.compareTo(aScale);
    }

    protected static BigDecimal scaleStreamMath(List<BigDecimal> numbers, RoundingMode roundingRule,
            BinaryOperator<BigDecimal> math) {

        if (numbers == null || numbers.isEmpty()) {
            return logMissingValues();
        }
        if (roundingRule == null) {
            return logMissingRoundingRule();
        }

        try {
            return numbers.stream()
                    .sorted(SigFigMathUtil::decsendingScale)
                    .sequential()
                    .reduce(ScientificDecimal.ZERO, math);
        } catch (NullPointerException npe) {
            return logMissingValues();
        }
    }

    /** Helper class for subtraction list actions where the first number
     *  in a list is considered the base and the shared lambda stream
     *  subtracts the first value from zero.
     * This helper wrapper prevents the creation of a new list.
     * If we could use the first value as the accumulator initial value
     *  then we would not require this wrapper class.
     */
    protected static class SubtractList extends AbstractList<BigDecimal> {
        List<BigDecimal> numbers;

        public SubtractList(List<BigDecimal> numbers) {
            this.numbers = numbers;
        }
        @Override
        public BigDecimal get(int index) {
            if (index == 0) {
                // negate the first value so that a double negative
                // (subtract a negative) from zero make it the base number.
                return numbers.get(index).negate();
            }
            return numbers.get(index);
        }

        @Override
        public int size() {
            if (numbers == null) {
                return 0;
            }
            return numbers.size();
        }
    }

    /**
     * Note that it is assumed business rule that the input datum has a decimal
     * point placed after the number for all ambiguous cases. Example: 5000 will
     * be noted as 4 significant figures.
     *
     * @param numbers - list of BigDecimals to perform addition upon and then
     * post-calculation, apply the rules of significant figures.
     * @param roundingRule - Rounding Method used as defined in Java math.
     * @return BigDecimal with the appropriate rules of significant figures
     * applied to the result. Return null if the list is null or empty.
     */
    public static BigDecimal sum(List<BigDecimal> numbers, RoundingMode roundingRule) {
        return scaleStreamMath(numbers, roundingRule, (a, b) -> add(a, b, roundingRule));
    }

    /**
     * Assumes the default RoundingMode to be HALF_EVEN as defined in the Java
     * math spec when performing addition on the list passed in. Also, note that
     * it is assumed business rule that the input datum has a decimal point
     * placed after the number for all ambiguous cases. Example: 5000 will be
     * noted as 4 significant figures.
     *
     * @param numbers the list of BigDecimals who's values will be added together
     * with the rules of significant figures applied.
     * @return BigDecimal with the rules pertaining to sig fig addition applied
     * post calculation or null if any args passed in are null.
     */
    public static BigDecimal sum(List<BigDecimal> numbers) {
        return sum(numbers, DEFAULT_ROUNDING_RULE);
    }

    /**
     * Note that it is assumed business rule that the input datum has a decimal
     * point placed after the number for all ambiguous cases. Example: 5000 will
     * be noted as 4 significant figures.
     *
     * @param augend BigDecimal to perform addition with. Default Rounding mode
     * of HALF_EVEN will be applied.
     * @param addend BigDecimal to perform addition with.
     * @return BigDecimal with the appropriate sig fig rules applied or null if
     * any args passed in are null.
     */
    public static BigDecimal add(BigDecimal augend, BigDecimal addend) {
        return add(augend, addend, DEFAULT_ROUNDING_RULE);
    }
    public static BigDecimal add(BigDecimal augend, BigDecimal addend, int precision) {
        return add(augend, addend, precision, DEFAULT_ROUNDING_RULE);
    }
    public static BigDecimal add(BigDecimal augend, BigDecimal addend, RoundingMode roundingRule) {
        if (augend == null || addend == null) {
            return logMissingValues();
        }
        int leastPrecision = getLeastPrecise(augend, addend);
        return add(augend, addend, leastPrecision, roundingRule);
    }

    /**
     * Note that it is assumed business rule that the input datum has a decimal
     * point placed after the number for all ambiguous cases. Example: 5000 will
     * be noted as 4 significant figures.
     *
     * @param augend BigDecimal to perform addition with.
     * @param addend BigDecimal to perform addition with.
     * @param roundingRule RoundingMode to apply as defined in Java math.
     * @return BigDecimal with the appropriate sig fig rules applied or null if
     * any args passed in are null.
     */
    public static BigDecimal add(BigDecimal augend, BigDecimal addend, int precision, RoundingMode roundingRule) {
        if (augend == null || addend == null) {
            return logMissingValues();
        }
        if (roundingRule == null) {
            return logMissingRoundingRule();
        }

        BigDecimal sum = augend.add(addend);

        if (BigDecimal.ZERO.compareTo(sum) == 0) {
            return ScientificDecimal.ZERO.setScale(precision);
        }

        // first, addition trims on the least fractional digits
        // as in these rounded to the least places after the decimal point
        //  1.01 + 0.011 = 1.021 rounded to 1.02
        //  1.01 + 0.016 = 1.026 rounded to 1.03
        int leastScale = getLeastScale(augend, addend);
        sum = sum.setScale(leastScale, roundingRule);

        // second, addition trims on the least total digits
        // this is accomplished by finding the difference in digits of the
        // sum and least digits. Example 10.11 + 0.025 = 10.1
        // while there are a minimum of two decimal places in the numbers
        // the addend has only 3 significant figures. 10.11+0.025=10.135 rounded 10.1
        // yes, we added something seemingly precise and lost overall precision.
        int extraPrecision = sum.precision() - precision;     // check if addition increased precision
        int scalePrecision = sum.scale() - extraPrecision;         // removed the extra precision from the scale
        int finalScale     = Math.min(scalePrecision, leastScale); // ensure no increased precision
        sum = sum.setScale(finalScale, roundingRule);              // BigDecimal only offers setting scale to change precision
        return sum;
    }

    /**
     * Note that it is assumed business rule that the input datum has a decimal
     * point placed after the number for all ambiguous cases. Example: 5000 will
     * be noted as 4 significant figures.
     *
     * @param numbers - the list of BigDecimals who's values will be subtracted
     * from the first value in the list with the rules of significant figures
     * applied post-calculation.
     * @param roundingRule - Rounding Method used as defined in Java math.
     * @return BigDecimal with the appropriate rules of significant figures
     * applied to the result or null if any args passed in are null.
     */
    public static BigDecimal subtract(List<BigDecimal> numbers, RoundingMode roundingRule) {
        return scaleStreamMath(new SubtractList(numbers), roundingRule, (a, b) -> subtract(a, b, roundingRule));
    }

    /**
     * Assumes the default RoundingMode to be HALF_EVEN as defined in the
     * BigDecimal spec when performing addition on the list passed in. Also,
     * note that it is assumed business rule that the input datum has a decimal
     * point placed after the number for all ambiguous cases. Example: 5000 will
     * be noted as 4 significant figures.
     *
     * @param numbers the list of BigDecimals who's values will be subtracted
     * from the first value in the list with the rules of significant figures
     * applied post-calculation.
     * @return BigDecimal with the rules pertaining to sig fig addition applied
     * post calculation or null if any args passed in are null.
     */
    public static BigDecimal subtract(List<BigDecimal> numbers) {
        return subtract(numbers, DEFAULT_ROUNDING_RULE);
    }

    /**
     * Note that it is assumed business rule that the input datum has a decimal
     * point placed after the number for all ambiguous cases. Example: 5000 will
     * be noted as 4 significant figures.
     *
     * @param minuend BigDecimal to perform addition with. Default Rounding mode
     * of HALF_EVEN will be applied.
     * @param subtrahend BigDecimal to perform addition with.
     * @return BigDecimal with the appropriate sig fig rules applied or null if
     * any args passed in are null.
     */
    public static BigDecimal subtract(BigDecimal minuend, BigDecimal subtrahend) {
        return subtract(minuend, subtrahend, DEFAULT_ROUNDING_RULE);
    }

    /**
     * Note that it is assumed business rule that the input datum has a decimal
     * point placed after the number for all ambiguous cases. Example: 5000 will
     * be noted as 4 significant figures.
     *
     * @param minuend BigDecimal to perform subtraction operation 'from'.
     * @param subtractend BigDecimal number to be subtracted.
     * @param roundingRule RoundingMode to apply as defined in Java math.
     * @return BigDecimal with the appropriate sig fig rules applied or null if
     * any args passed in are null.
     */
    public static BigDecimal subtract(BigDecimal minuend, BigDecimal subtractend, RoundingMode roundingRule) {
        if (subtractend == null) {
            return logMissingValues();
        }
        // we can use the addition methods; adding a negative is subtraction
        return add(minuend, subtractend.negate(), roundingRule);
    }
    public static BigDecimal subtract(BigDecimal minuend, BigDecimal subtractend, int precision, RoundingMode roundingRule) {
        if (subtractend == null) {
            return logMissingValues();
        }
        // we can use the addition methods; adding a negative is subtraction
        return add(minuend, subtractend.negate(), precision, roundingRule);
    }
    public static BigDecimal subtract(BigDecimal minuend, BigDecimal subtractend, int precision) {
        return subtract(minuend, subtractend, precision, DEFAULT_ROUNDING_RULE);
    }

    /**
     *
     * @param numbers List of BigDecimals that will determine which one has the
     * smallest scale. Finds the BigD with the fewest digits in the mantissa.
     * This is useful for maintaining the rules of sig figs: addition &
     * subtraction.
     * @return int count of the least amount of digits in
     * the mantissa or 0 if any args passed in are null.
     */
    protected static int getLeastScale(List<BigDecimal> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            logMissingValues();
            return 0;
        }
        int lowestScale = numbers.get(0).scale();

        for (BigDecimal num : numbers) {
            lowestScale = Math.min(lowestScale, num.scale());
        }
        LOGGER.trace("smallest scale: {}", lowestScale);
        return lowestScale;
    }
    protected static int getLeastScale(BigDecimal ... numbers) {
        if (numbers == null) {
            logMissingValues();
            return 0;
        }
        return getLeastScale(Arrays.asList(numbers));
    }

    /**
     * This will determine which BigDecimal has the lowest precision. This is useful for
     * maintaining the rules of sig figs: multiplication and division.
     * @param numbers BigDecimals
     * @return the least precision or 0 if
     * any args passed in are null.
     */
    protected static int getLeastPrecise(BigDecimal ... numbers) {
        if (numbers == null || numbers.length == 0) {
            logMissingValues();
            return 0;
        }
        if (numbers[0] == null) {
            return 0;
        }
        int lowestPrecision = numbers[0].precision();

        for (BigDecimal num : numbers) {
            if (num == null) {
                return 0;
            }
            lowestPrecision = Math.min(lowestPrecision, num.precision());
        }
        LOGGER.trace("smallest precision: {}", lowestPrecision);
        return lowestPrecision;
    }

    /**
     * For multiplication and division, only the total number of significant
     * figures in each of the factors matter.
     *
     * @param multiplicand BigDecimal
     * @param multiplier BigDecimal
     * @param roundingRule RoundingMode
     * @return product of given values rounded to significant figures of the least given
     */
    public static BigDecimal multiply(BigDecimal multiplicand, BigDecimal multiplier, int precision, RoundingMode roundingRule) {
        if (multiplicand == null || multiplier == null) {
            return logMissingValues();
        }
        if (roundingRule == null) {
            return logMissingRoundingRule();
        }

        MathContext mc = new MathContext(precision, roundingRule);
        BigDecimal product = multiplicand.multiply(multiplier, mc);
        if (BigDecimal.ZERO.compareTo(product) == 0) {
            product = ScientificDecimal.ZERO.setScale(precision);
        } else {
            product = ScientificDecimal.make(product, precision);
        }
        return product;
    }
    /**
     * For multiplication and division, only the total number of significant
     * figures in each of the factors matter.
     *
     * @param multiplicand BigDecimal
     * @param multiplier BigDecimal
     * @param roundingRule RoundingMode
     * @return product of given values rounded to significant figures of the first value
     */
    public static BigDecimal multiply(BigDecimal multiplicand, BigDecimal multiplier, RoundingMode roundingRule) {
        if (multiplicand == null || multiplier == null) {
            return logMissingValues();
        }
        int precision = getLeastPrecise(multiplicand, multiplier);
        return multiply(multiplicand, multiplier, precision, roundingRule);
    }

    /**
     * For multiplication and division, only the total number of significant
     * figures in each of the factors matter.
     *
     * @param multiplicand BigDecimal
     * @param exactMultiplier BigDecimal
     * @return
     */
    public static BigDecimal multiply(BigDecimal multiplicand, BigDecimal exactMultiplier, int precision) {
        return multiply(multiplicand, exactMultiplier, precision, DEFAULT_ROUNDING_RULE);
    }

    /**
     * For multiplication and division, only the total number of significant
     * figures in each of the factors matter.
     *
     * @param multiplicand BigDecimal
     * @param multiplier BigDecimal
     * @return null if any arg passed in is null.
     */
    public static BigDecimal multiply(BigDecimal multiplicand, BigDecimal multiplier) {
        return multiply(multiplicand, multiplier, DEFAULT_ROUNDING_RULE);
    }

    /**
     * For multiplication and division, only the total number of significant
     * figures in each of the factors matter.
     *
     * @param numerator BigDecimal aka dividend
     * @param denominator BigDecimal aka divsior
     * @param roundingRule RoundingMode
     * @return null if any arg passed in is null.
     */
    public static BigDecimal divide(BigDecimal numerator, BigDecimal denominator, int precision, RoundingMode roundingRule) {
        if (numerator == null || denominator == null) {
            return logMissingValues();
        }
        if (roundingRule == null) {
            return logMissingRoundingRule();
        }

        MathContext mc = new MathContext(precision, roundingRule);
        BigDecimal quotient = numerator.divide(denominator, mc);
        if (BigDecimal.ZERO.compareTo(quotient) == 0) {
            quotient = ScientificDecimal.ZERO.setScale(precision);
        } else {
            quotient = ScientificDecimal.make(quotient, precision);
        }
        return quotient;
    }

    /**
     * For multiplication and division, only the total number of significant
     * figures in each of the factors matter. Uses the Default Rounding mode.
     *
     * @param numerator BigDecimal aka dividend
     * @param denominator BigDecimal aka divisor
     * @return null if any arg passed in is null.
     */
    public static BigDecimal divide(BigDecimal numerator, BigDecimal denominator, int precision) {
        return divide(numerator, denominator, precision, DEFAULT_ROUNDING_RULE);
    }

    /**
     * For multiplication and division, only the total number of significant
     * figures in each of the factors matter.
     *
     * @param numerator BigDecimal aka dividend
     * @param denominator BigDecimal aka divisor
     * @param roundingRule RoundingMode
     * @return null if any arg passed in is null.
     */
    public static BigDecimal divide(BigDecimal numerator, BigDecimal denominator, RoundingMode roundingRule) {
        if (numerator == null || denominator == null) {
            return logMissingValues();
        }
        int leastPrecise = getLeastPrecise(numerator, denominator);
        return divide(numerator, denominator.setScale(EXACT_SCALE), leastPrecise, roundingRule);
    }
    
    /**
     * For multiplication and division, only the total number of significant
     * figures in each of the factors matter.
     *
     * @param numerator BigDecimal aka dividend, or total if used for averages
     * @param denominator BigDecimal aka divisor, or count if used for averages
     * @return null if any arg passed in is null.
     */
    public static BigDecimal divide(BigDecimal numerator, BigDecimal denominator) {
        BigDecimal quotient = divide(numerator, denominator, DEFAULT_ROUNDING_RULE);

        return quotient;
    }
}
