package gov.usgs.wma.statistics.logic;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public static BigDecimal sigFigAdd(List<BigDecimal> numbers, RoundingMode roundingRule) {

        if (numbers == null || numbers.isEmpty()) {
            LOGGER.warn("Missing BigDecimal list or list was empty. Can not determine scale.");
            return null;
        }
        if (roundingRule == null) {
            LOGGER.warn("RoundingMode not defined. Did you mean to use the 2 arg method which uses the default rounding mode?");
            return null;
        }

        // TODO asdf must sort in decreasing scale

        Iterator<BigDecimal> addends = numbers.iterator();
        BigDecimal sum = addends.next();
        while (addends.hasNext()) {
            sum = sigFigAdd(sum, addends.next(), roundingRule);
            if (sum == null) {
                return null;
            }
        }

        return sum;
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
    public static BigDecimal sigFigAdd(List<BigDecimal> numbers) {
        return sigFigAdd(numbers, DEFAULT_ROUNDING_RULE);
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
    public static BigDecimal sigFigAdd(BigDecimal augend, BigDecimal addend) {
        return sigFigAdd(augend, addend, DEFAULT_ROUNDING_RULE);
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
    public static BigDecimal sigFigAdd(BigDecimal augend, BigDecimal addend, RoundingMode roundingRule) {
        if (augend == null || addend == null) {
            LOGGER.warn("Missing BigDecimal value(s) to add.");
            return null;
        }
        if (roundingRule == null) {
            LOGGER.warn("RoundingMode not defined. Did you mean to use the 2 arg method which uses the default rounding mode?");
            return null;
        }

        BigDecimal result = augend.add(addend);

        BigDecimal finalAnswer = null;
        int leastScale = getLeastScale(augend, addend);

        finalAnswer = result.setScale(leastScale, roundingRule);
        return finalAnswer;
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
    public static BigDecimal sigFigSubtract(List<BigDecimal> numbers, RoundingMode roundingRule) {

        if (numbers == null || numbers.isEmpty()) {
            LOGGER.warn("Missing BigDecimal list or list was empty. Can not determine scale.");
            return null;
        }
        if (roundingRule == null) {
            LOGGER.warn("RoundingMode not defined. Did you mean to use the 2 arg method which uses the default rounding mode?");
            return null;
        }
        BigDecimal finalAnswer;
        // TODO asdf must sort in decreasing scale
        // TODO either list or array not both
        BigDecimal[] bigDecimalList = numbers.toArray(new BigDecimal[numbers.size()]);
        BigDecimal result = bigDecimalList[0];

        if (result != null) {
            for (int i = 1; i < bigDecimalList.length; i++) { // intentionally set array at 1 and not the first element
                BigDecimal bd = bigDecimalList[i];
                if (bd != null) {
                    result = result.subtract(bigDecimalList[i]);
                    // TODO asdf scale and sigfigs must be set per action
                } else {
                    LOGGER.warn("A BigDecimal in the list was found to be null. Returning null.");
                    return null;
                }
            }
            int leastScale = getLeastScale(numbers);

            finalAnswer = result.setScale(leastScale, roundingRule);
        } else {
            LOGGER.warn("The first BigDecimal in the list was found to be null. Returning null.");
            return null;
        }
        return finalAnswer;
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
    public static BigDecimal sigFigSubtract(List<BigDecimal> numbers) {
        return sigFigSubtract(numbers, DEFAULT_ROUNDING_RULE);
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
    public static BigDecimal sigFigSubtract(BigDecimal minuend, BigDecimal subtrahend) {
        if (minuend == null || subtrahend == null) {
            LOGGER.warn("BigDecimal arg was null. Can not determine scale.");
            return null;
        }

        List<BigDecimal> numbers = new ArrayList<>(2);
        numbers.add(minuend);
        numbers.add(subtrahend);

        return sigFigSubtract(numbers);
    }

    /**
     * Note that it is assumed business rule that the input datum has a decimal
     * point placed after the number for all ambiguous cases. Example: 5000 will
     * be noted as 4 significant figures.
     *
     * @param minuend BigDecimal to perform subtraction operation 'from'.
     * @param subtrahend BigDecimal number to be subtracted.
     * @param rm RoundingMode to apply as defined in Java math.
     * @return BigDecimal with the appropriate sig fig rules applied or null if
     * any args passed in are null.
     */
    public static BigDecimal sigFigSubtract(BigDecimal minuend, BigDecimal subtrahend, RoundingMode rm) {
        // TODO ASDF this should be the base method not the list one

        if ((minuend == null || subtrahend == null)) {
            LOGGER.warn("BigDecimal arg was null. Can not determine scale.");
            return null;
        }

        if (rm == null) {
            LOGGER.warn("RoundingMode arg was null. Did you mean to use the 2 arg method that applies the default rounding mode?");
            return null;
        }

        List<BigDecimal> numbers = new ArrayList<>(2);
        numbers.add(minuend);
        numbers.add(subtrahend);

        return sigFigSubtract(numbers, rm);
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
            LOGGER.warn("Missing BigDecimal list or list was empty. Can not determine scale.");
            return 0;
        }
        int lowestScaleFound = numbers.get(0).scale();

        for (BigDecimal num : numbers) {
            if (num.scale() < lowestScaleFound) {
                lowestScaleFound = num.scale();
            }
        }
        LOGGER.trace("smallest scale: {}", lowestScaleFound);
        return lowestScaleFound;
    }
    protected static int getLeastScale(BigDecimal ... numbers) {

        if (numbers == null || numbers.length == 0) {
            LOGGER.warn("Missing BigDecimal or was empty. Can not determine scale.");
            return 0;
        }
        int lowestScaleFound = numbers[0].scale();

        for (BigDecimal num : numbers) {
            if (num.scale() < lowestScaleFound) {
                lowestScaleFound = num.scale();
            }
        }
        LOGGER.trace("smallest scale: {}", lowestScaleFound);
        return lowestScaleFound;
    }

    /**
     * This will determine which BigDecimal has the lowest precision. This is useful for
     * maintaining the rules of sig figs: multiplication and division.
     * @param bd1 one BigDecimal
     * @param bd2 another BigDecimal
     * @return BigDecimal that is found to have the least precision or null if
     * any args passed in are null.
     */
    protected static BigDecimal getLeastPrecise(BigDecimal bd1, BigDecimal bd2) {

        if (bd1 == null || bd2 == null) {
            LOGGER.warn("Missing BigDecimal for comparison. Can not determine precision.");
            return null;
        }

        return (bd1.precision()) < (bd2.precision()) ? bd1 : bd2;
    }

    /**
     * For multiplication and division, only the total number of significant
     * figures in each of the factors matter.
     *
     * @param multiplicand BigDecimal
     * @param multiplier BigDecimal
     * @param roundingRule RoundingMode
     * @return
     */
    public static BigDecimal sigFigMultiply(BigDecimal multiplicand, BigDecimal multiplier, RoundingMode roundingRule) {
        if (multiplicand == null || multiplier == null) {
            LOGGER.warn("BigDecimal was null. Can not determine precision.");
            return null;
        }
        if (roundingRule == null) {
            LOGGER.warn("RoundingMode arg was null. Did you mean to use the 2 arg method that applies the default rounding mode?");
            return null;
        }
        BigDecimal leastPreciseBD = getLeastPrecise(multiplicand, multiplier);
        int sigFigs = leastPreciseBD.precision();

        MathContext mc = new MathContext(sigFigs, roundingRule);
        BigDecimal product = multiplicand.multiply(multiplier, mc);
        return product;
    }
    /**
     * For multiplication and division, only the total number of significant
     * figures in each of the factors matter.
     *
     * @param multiplicand BigDecimal
     * @param exactMultiplier BigDecimal
     * @param roundingRule RoundingMode
     * @return
     */
    public static BigDecimal sigFigMultiplyByExact(BigDecimal multiplicand, BigDecimal exactMultiplier, RoundingMode roundingRule) {
        if (multiplicand == null || exactMultiplier == null) {
            LOGGER.warn("BigDecimal was null. Can not determine precision.");
            return null;
        }
        if (roundingRule == null) {
            LOGGER.warn("RoundingMode arg was null. Did you mean to use the 2 arg method that applies the default rounding mode?");
            return null;
        }
        int sigFigs = multiplicand.precision();
        MathContext mc = new MathContext(sigFigs, roundingRule);
        BigDecimal product = multiplicand.multiply(exactMultiplier, mc);

        return product;
    }

    /**
     * For multiplication and division, only the total number of significant
     * figures in each of the factors matter.
     *
     * @param multiplicand BigDecimal
     * @param exactMultiplier BigDecimal
     * @return
     */
    public static BigDecimal sigFigMultiplyByExact(BigDecimal multiplicand, BigDecimal exactMultiplier) {
        if (multiplicand == null || exactMultiplier == null) {
            LOGGER.warn("BigDecimal was null. Can not determine precision.");
            return null;
        }
        BigDecimal product = sigFigMultiplyByExact(multiplicand, exactMultiplier, DEFAULT_ROUNDING_RULE);
        return product;
    }

    /**
     * For multiplication and division, only the total number of significant
     * figures in each of the factors matter.
     *
     * @param multiplicand BigDecimal
     * @param multiplier BigDecimal
     * @return null if any arg passed in is null.
     */
    public static BigDecimal sigFigMultiply(BigDecimal multiplicand, BigDecimal multiplier) {
        if (multiplicand == null || multiplier == null) {
            LOGGER.warn("BigDecimal was null. Can not determine precision.");
            return null;
        }
        BigDecimal product = sigFigMultiply(multiplicand, multiplier, DEFAULT_ROUNDING_RULE);
        return product;
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
    public static BigDecimal sigFigDivide(BigDecimal numerator, BigDecimal denominator, RoundingMode roundingRule) {
        if (numerator == null || denominator == null) {
            LOGGER.warn("BigDecimal was null. Can not determine precision."); // divisor that is 0 will throw the arithmetic exception
            return null;
        }
        if (roundingRule == null) {
            LOGGER.warn("RoundingMode arg was null. Did you mean to use the 2 arg method that applies the default rounding mode?");
            return null;
        }

        BigDecimal leastPreciseBD = getLeastPrecise(numerator, denominator);
        int sigFigs = leastPreciseBD.precision();

        MathContext mc = new MathContext(sigFigs, roundingRule);
        BigDecimal quotient = numerator.divide(denominator, mc);
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
    public static BigDecimal sigFigDivide(BigDecimal numerator, BigDecimal denominator) {
        if (numerator == null || denominator == null) {
            LOGGER.warn("BigDecimal was null. Can not determine precision."); // divisor that is 0 will throw the arithmetic exception
            return null;
        }
        BigDecimal quotient = sigFigDivide(numerator, denominator, DEFAULT_ROUNDING_RULE);
        return quotient;
    }

    /**
     * For multiplication and division, only the total number of significant
     * figures in each of the factors matter.
     *
     * @param numerator BigDecimal aka dividend
     * @param exactDenominator BigDecimal aka divisor
     * @param roundingRule RoundingMode
     * @return null if any arg passed in is null.
     */
    public static BigDecimal sigFigDivideByExact(BigDecimal numerator, BigDecimal exactDenominator, RoundingMode roundingRule) {
        if (numerator == null || exactDenominator == null) {
            LOGGER.warn("BigDecimal was null. Can not determine precision."); // divisor that is 0 will throw the arithmetic exception
            return null;
        }
        if (roundingRule == null) {
            LOGGER.warn("RoundingMode arg was null. Did you mean to use the 2 arg method that applies the default rounding mode?");
            return null;
        }
        int sigFigs = numerator.precision();

        MathContext mc = new MathContext(sigFigs, roundingRule);
        BigDecimal quotient = numerator.divide(exactDenominator, mc);

        return quotient;
    }
    
    /**
     * For multiplication and division, only the total number of significant
     * figures in each of the factors matter.
     *
     * @param numerator BigDecimal aka dividend, or total if used for averages
     * @param exactDenominator BigDecimal aka divisor, or count if used for averages
     * @return null if any arg passed in is null.
     */
    public static BigDecimal sigFigDivideByExact(BigDecimal numerator, BigDecimal exactDenominator) {
        if (numerator == null || exactDenominator == null) {
            LOGGER.warn("BigDecimal was null. Can not determine precision."); // divisor that is 0 will throw the arithmetic exception
            return null;
        }
        BigDecimal quotient = sigFigDivideByExact(numerator, exactDenominator, DEFAULT_ROUNDING_RULE);

        return quotient;
    }
}
