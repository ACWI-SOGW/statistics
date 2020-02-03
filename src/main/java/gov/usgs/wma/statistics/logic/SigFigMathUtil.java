package gov.usgs.wma.statistics.logic;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
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
    public static CustomRoundingRule DEFAULT_ROUNDING_RULE = new UsgsRoundingRule();

    public interface CustomRoundingRule {
    	RoundingMode valueRule(BigDecimal value, int sigFigs);
        RoundingMode productRule(BigDecimal a, BigDecimal b, int sigFigs);
        RoundingMode divisionRule(BigDecimal a, BigDecimal b, int sigFigs);
    }

    // March 30 2017 - discovered that Java round negative values away from zero towards positive and negative infinity.
    //     this update will round towards positive infinity always.
    // March 30 2017 - Java round all values based on the absolute value of the number rather than
    // relative to infinity. As in, 4.1/2.0 = 2.1 (half up) and -4.1/2.0 = -2.1 (half up away from zero)
    // However, it seems math suggests this should be the case -4.1/2.0 = -2.0 (half up to greater value)
    // Now, when supplying a new rounding mode, the handing of positive must be specified
    // along with negative handling. For example: (v)->v.doublevalue()>=0 ?CEILING :FLOOR
    // Should the user want only one that is manageable too.
    // For example: (v)->HALF_EVEN will always use HALF EVEN UP
    //
    // July  30 2018 - Change of specification - they want the default Java behavior back.
    //     This is not currently used anywhere but there is a high likelihood that is will
    //     be called back into use by any level of statistics.
    //
    // Jan   30 2020 - New revision to rounding. The default rounding rule for Java is nearly correct.
    //     The rounding rule for fives is to ensure there is a digit other than 0 to the right to round up.
    //     IE: 1.051 should round to 2.0 while 1.050 should round to 1.0 and 1.05000000001 rounds to 2.0
    public static final class UsgsRoundingRule implements CustomRoundingRule {
        private static final RoundingMode ROUNDING_MODE_UP = RoundingMode.HALF_UP;
        private static final RoundingMode ROUNDING_MODE_DOWN = RoundingMode.HALF_DOWN;
        private static final BigDecimal TWO = new BigDecimal("2");

        /**
         * Creates a number one tenth smaller than the scale provided.
         * IE if sale is 2 (which means two decimal points) then the number returned would be 0.001
         * because scale 2 mean some number .xx like 0.08 for example.
         *
         * Background:
         * In a USGS document that explicitly states how to round, it states that exact halves should be rounded down.
         * For example, 1.0500 should be rounded to 1.0 rather than 1.1
         *
         * Use of this function:
         * This is used in this class to figure out if we have an exact half between for rounding.
         * Suppose we have two numbers that might need rounding to the nearest tenth: 1.0500 and 1.0501 for example.
         * Then 1.0501 should be rounded to 1.1 as one would expect; however, 1.0500 should be rounded to 1.0 instead.
         * The method to detect the half way between two numbers (the 0.0500 in the example above) is accomplished
         * by if to extract the fraction to be rounded (0.0500 and 0.0501 in the examples) and then subtract the tenth
         * beyond those values scale. In the example, if 0.00001 is subtracted from both fractions then the results are
         * 0.0500-0.00001=0.04999 and 1.0501-0.00001=0.05009 respectively. Notice that the half became less than
         * half way and is less then 0.05 while the other remains greater than 0.05
         *
         * This function creates the number 0.00001 for use in detecting the halfway value.
         *
         * @param scale the scale of the number you want an epsilon
         * @return the epsilon number given the scale.
         */
        BigDecimal createEpsilon(int scale) {
            return BigDecimal.ONE; // TODO actually return a fraction
        }

        @Override
    	public RoundingMode valueRule(BigDecimal value, int sigFigs) {
    		RoundingMode mode = ROUNDING_MODE_UP;
        	if (value.precision() > sigFigs) {
                BigDecimal rounded = value.round(new MathContext(sigFigs, ROUNDING_MODE_DOWN));
                BigDecimal remainder = value.subtract(rounded);
                BigDecimal epsilon  = createEpsilon(value.scale());
                if (remainder.compareTo(BigDecimal.ZERO) == 0) {
                    mode = ROUNDING_MODE_DOWN;
                }
            }
        	return mode;
    	}
    	@Override
        // This is a helper for multiplication and division, the resulting sign determines the rounding mode to use.
    	// The rounding mode must be provided as the calculation is being performed. This solves the catch 22.
    	// If both signs are the same them the resulting value will be positive and if different then negative.
    	// When the rounding rule, like this one, is product value sign specific this can accommodate it.
    	public RoundingMode productRule(BigDecimal a, BigDecimal b, int sigFigs) {
        	return valueRule(a.multiply(b), sigFigs);
    	}
    	@Override
        public RoundingMode divisionRule(BigDecimal a, BigDecimal b, int sigFigs) {
            RoundingMode mode = ROUNDING_MODE_UP;
            if (b.equals(TWO.setScale(b.scale()))) {
                mode = ROUNDING_MODE_DOWN;
            }
            return mode;
        }
    }

    // July  30 2018 - Change of specification - they want the default Java behavior back.
    // Simply put, this helper returns the given mode during construction.
    public static final class JavaDefaultRoundingRule implements CustomRoundingRule {
    	final RoundingMode ROUNDING_MODE;
    	public JavaDefaultRoundingRule(RoundingMode roundingMode) {
    		ROUNDING_MODE = roundingMode;
		}
    	@Override
    	public RoundingMode valueRule(BigDecimal value, int sigFigs) {
    		return ROUNDING_MODE;
    	}
        @Override
        public RoundingMode productRule(BigDecimal a, BigDecimal b, int sigFigs) {
            return ROUNDING_MODE;
        }
        @Override
        public RoundingMode divisionRule(BigDecimal a, BigDecimal b, int sigFigs) {
            return ROUNDING_MODE;
        }
    }

    /**
     * Note that it is assumed business rule that the input datum has a decimal
     * point placed after the number for all ambiguous cases. Example: 5000 will
     * be noted as 4 significant figures.
     *
     * @param bdList - list of BigDecimals to perform addition upon and then
     * post-calculation, apply the rules of significant figures.
     * @param rm - Rounding Method used as defined in Java math.
     * @return BigDecimal with the appropriate rules of significant figures
     * applied to the result. Return null if the list is null or empty.
     */
    public static BigDecimal sigFigAdd(List<BigDecimal> bdList, CustomRoundingRule rm) {

        if (bdList == null || bdList.isEmpty()) {
            LOGGER.warn("Missing BigDecimal list or list was empty. Can not determine scale.");
            return null;
        }
        if (rm == null) {
            LOGGER.warn("RoundingMode not defined. Did you mean to use the 2 arg method which uses the default rounding mode?");
            return null;
        }
        BigDecimal result = BigDecimal.ZERO;

        for (BigDecimal bd : bdList) {
            if (bd == null) {
                LOGGER.warn("A BigDecimal in the list was found to be null. Returning null.");
                return null;
            }
            result = result.add(bd);
        }

        BigDecimal finalAnswer = null;
        int leastScale = getLeastScale(bdList);

        finalAnswer = result.setScale(leastScale, rm.valueRule(result, leastScale));
        return finalAnswer;
    }

    /**
     * Assumes the default RoundingMode to be HALF_EVEN as defined in the Java
     * math spec when performing addition on the list passed in. Also, note that
     * it is assumed business rule that the input datum has a decimal point
     * placed after the number for all ambiguous cases. Example: 5000 will be
     * noted as 4 significant figures.
     *
     * @param bdList the list of BigDecimals who's values will be added together
     * with the rules of significant figures applied.
     * @return BigDecimal with the rules pertaining to sig fig addition applied
     * post calculation or null if any args passed in are null.
     */
    public static BigDecimal sigFigAdd(List<BigDecimal> bdList) {
        return sigFigAdd(bdList, DEFAULT_ROUNDING_RULE);
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
        if (augend == null || addend == null) {
            LOGGER.warn("BigDecimal arg was null.");
            return null;
        }

        List<BigDecimal> bdList = new ArrayList<>(2);
        bdList.add(augend);
        bdList.add(addend);

        return sigFigAdd(bdList);
    }

    /**
     * Note that it is assumed business rule that the input datum has a decimal
     * point placed after the number for all ambiguous cases. Example: 5000 will
     * be noted as 4 significant figures.
     *
     * @param augend BigDecimal to perform addition with.
     * @param addend BigDecimal to perform addition with.
     * @param rm RoundingMode to apply as defined in Java math.
     * @return BigDecimal with the appropriate sig fig rules applied or null if
     * any args passed in are null.
     */
    public static BigDecimal sigFigAdd(BigDecimal augend, BigDecimal addend, CustomRoundingRule rm) {
        if (augend == null || addend == null) {
            LOGGER.warn("BigDecimal arg was null.");
            return null;
        }

        if (rm == null) {
            LOGGER.warn("RoundingMode arg was null. Did you mean to use the 2 arg method that applies the default rounding mode?");
            return null;
        }
        List<BigDecimal> bdList = new ArrayList<>(2);
        bdList.add(augend);
        bdList.add(addend);

        return sigFigAdd(bdList, rm);
    }

    /**
     * Note that it is assumed business rule that the input datum has a decimal
     * point placed after the number for all ambiguous cases. Example: 5000 will
     * be noted as 4 significant figures.
     *
     * @param bdList - the list of BigDecimals who's values will be subtracted
     * from the first value in the list with the rules of significant figures
     * applied post-calculation.
     * @param rm - Rounding Method used as defined in Java math.
     * @return BigDecimal with the appropriate rules of significant figures
     * applied to the result or null if any args passed in are null.
     */
    public static BigDecimal sigFigSubtract(List<BigDecimal> bdList, CustomRoundingRule rm) {

        if (bdList == null || bdList.isEmpty()) {
            LOGGER.warn("Missing BigDecimal list or list was empty. Can not determine scale.");
            return null;
        }
        if (rm == null) {
            LOGGER.warn("RoundingMode not defined. Did you mean to use the 2 arg method which uses the default rounding mode?");
            return null;
        }
        BigDecimal finalAnswer;
        BigDecimal[] bigDecimalList = bdList.toArray(new BigDecimal[bdList.size()]);
        BigDecimal result = bigDecimalList[0];

        if (result != null) {
            for (int i = 1; i < bigDecimalList.length; i++) { // intentionally set array at 1 and not the first element
                BigDecimal bd = bigDecimalList[i];
                if (bd != null) {
                    result = result.subtract(bigDecimalList[i]);
                } else {
                    LOGGER.warn("A BigDecimal in the list was found to be null. Returning null.");
                    return null;
                }
            }
            int leastScale = getLeastScale(bdList);

            finalAnswer = result.setScale(leastScale, rm.valueRule(result, leastScale));
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
     * @param bdList the list of BigDecimals who's values will be subtracted
     * from the first value in the list with the rules of significant figures
     * applied post-calculation.
     * @return BigDecimal with the rules pertaining to sig fig addition applied
     * post calculation or null if any args passed in are null.
     */
    public static BigDecimal sigFigSubtract(List<BigDecimal> bdList) {
        return sigFigSubtract(bdList, DEFAULT_ROUNDING_RULE);
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

        List<BigDecimal> bdList = new ArrayList<>(2);
        bdList.add(minuend);
        bdList.add(subtrahend);

        return sigFigSubtract(bdList);
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
    public static BigDecimal sigFigSubtract(BigDecimal minuend, BigDecimal subtrahend, CustomRoundingRule rm) {
        if ((minuend == null || subtrahend == null)) {
            LOGGER.warn("BigDecimal arg was null. Can not determine scale.");
            return null;
        }

        if (rm == null) {
            LOGGER.warn("RoundingMode arg was null. Did you mean to use the 2 arg method that applies the default rounding mode?");
            return null;
        }
        List<BigDecimal> bdList = new ArrayList<>(2);
        bdList.add(minuend);
        bdList.add(subtrahend);

        return sigFigSubtract(bdList, rm);
    }

    /**
     *
     * @param bdList List of BigDecimals that will determine which one has the
     * smallest scale. Finds the BigD with the fewest digits in the mantissa.
     * This is useful for maintaining the rules of sig figs: addition &
     * subtraction.
     * @return int count of the least amount of digits in
     * the mantissa or 0 if any args passed in are null.
     */
    protected static int getLeastScale(List<BigDecimal> bdList) {

        if (bdList == null || bdList.isEmpty()) {
            LOGGER.warn("Missing BigDecimal list or list was empty. Can not determine scale.");
            return 0;
        }
        int lowestScaleFound = bdList.get(0).scale();

        for (BigDecimal num : bdList) {
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
     * @param rm RoundingMode
     * @return
     */
    public static BigDecimal sigFigMultiply(BigDecimal multiplicand, BigDecimal multiplier, CustomRoundingRule rm) {
        if (multiplicand == null || multiplier == null) {
            LOGGER.warn("BigDecimal was null. Can not determine precision.");
            return null;
        }
        if (rm == null) {
            LOGGER.warn("RoundingMode arg was null. Did you mean to use the 2 arg method that applies the default rounding mode?");
            return null;
        }
        BigDecimal leastPreciseBD = getLeastPrecise(multiplicand, multiplier);
        int sigFigs = leastPreciseBD.precision();

        MathContext mc = new MathContext(sigFigs, rm.productRule(multiplicand, multiplier, sigFigs));
        BigDecimal product = multiplicand.multiply(multiplier, mc);
        return product;
    }
    /**
     * For multiplication and division, only the total number of significant
     * figures in each of the factors matter.
     *
     * @param multiplicand BigDecimal
     * @param exactMultiplier BigDecimal
     * @param rm RoundingMode
     * @return
     */
    public static BigDecimal sigFigMultiplyByExact(BigDecimal multiplicand, BigDecimal exactMultiplier, CustomRoundingRule rm) {
        if (multiplicand == null || exactMultiplier == null) {
            LOGGER.warn("BigDecimal was null. Can not determine precision.");
            return null;
        }
        if (rm == null) {
            LOGGER.warn("RoundingMode arg was null. Did you mean to use the 2 arg method that applies the default rounding mode?");
            return null;
        }
        int sigFigs = multiplicand.precision();
        MathContext mc = new MathContext(sigFigs, rm.productRule(multiplicand, exactMultiplier, sigFigs));
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
     * @param rm RoundingMode
     * @return null if any arg passed in is null.
     */
    public static BigDecimal sigFigDivide(BigDecimal numerator, BigDecimal denominator, CustomRoundingRule rm) {
        if (numerator == null || denominator == null) {
            LOGGER.warn("BigDecimal was null. Can not determine precision."); // divisor that is 0 will throw the arithmetic exception
            return null;
        }
        if (rm == null) {
            LOGGER.warn("RoundingMode arg was null. Did you mean to use the 2 arg method that applies the default rounding mode?");
            return null;
        }

        BigDecimal leastPreciseBD = getLeastPrecise(numerator, denominator);
        int sigFigs = leastPreciseBD.precision();

        MathContext mc = new MathContext(sigFigs, rm.divisionRule(numerator, denominator, sigFigs));
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
     * @param rm RoundingMode
     * @return null if any arg passed in is null.
     */
    public static BigDecimal sigFigDivideByExact(BigDecimal numerator, BigDecimal exactDenominator, CustomRoundingRule rm) {
        if (numerator == null || exactDenominator == null) {
            LOGGER.warn("BigDecimal was null. Can not determine precision."); // divisor that is 0 will throw the arithmetic exception
            return null;
        }
        if (rm == null) {
            LOGGER.warn("RoundingMode arg was null. Did you mean to use the 2 arg method that applies the default rounding mode?");
            return null;
        }
        int sigFigs = numerator.precision();

        MathContext mc = new MathContext(sigFigs, rm.divisionRule(numerator, exactDenominator, sigFigs));
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
