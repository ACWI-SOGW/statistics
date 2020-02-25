package gov.usgs.wma.statistics.logic;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A more proper implementation of significant figures.
 * The BigDecimal counts 1000 as having 4 figures when it only has 1.
 * Also, 0.0000 is reported as 1 even though it has at least 4.
 * 
 * This implementation removes leading zeros and corrects for the 
 * presence of the decimal point in zero and integers. While a stand 
 * alone 1000 only has 1 sig fig, it is my experience from college 
 * science classes that 1000. has 4 figures. Note that there is no 
 * way to report 1000 with 2 or 3 figures. Because of this there is a 
 * constructor to set the known precision.
 * 
 * Should we discover that this assertion was a singular professor
 * interpretation, we can revisit the rules.
 *
 * The BigDecimal compareTo method uses the intCompact directly accessed.
 * This means that the bigDecimal local value must match the superclass.
 * To manage this, the factory method is invoked to evaluate.
 * 
 * new ScientificDecimal("1000", 3) will create set 3 figures.
 * new ScientificDecimal("1000").setSigfigs(3) will also work.
 * 
 * @author duselman
 *
 */
@SuppressWarnings("serial")
public class ScientificDecimal extends BigDecimal {
	// Feb  10 2020 - BigDecimal always reports zero as 1 digit precision.
	public static final BigDecimal ZERO = new ScientificDecimal("0.000000000");
	public static final BigDecimal ONE  = new BigDecimal("1.000000000");

	// some numbers are exact, like the count of values in a list.
	// use this extra large scale to ensure exact numbers do not reduce significant figures
	public static final int EXACT_SCALE = 9; // scale to use for exact values.

	final Integer sigfigs;
	// need to extend the BigDecimal so that this class IS a BigDecimal
	// need to keep a local copy because setScale returns a new instance
	// while this class will also be immutable, during construction we need to set scale.
	// also, calls to superclass methods where not reliable
	BigDecimal bigDecimal;


	/**
	 * Constructs a BigDecimal for values that BigDecimal is sufficient
	 * and ScientificDecimal when necessary. It turns out that BigDecimal.compareTo
	 * make an internal reference to intCompact that cannot be managed by this class.
	 * When a number enters the constructor for ScientificDecimal it calls the super
	 * constructor. That instance will have a different compact nature than the
	 * locally resolved precision. This factory method returns a BigDecimal when
	 * ever possible. The only time this class is necessary is when the precision
	 * cannot match the desired. That usually means ZERO only. It even turns out that
	 * BigDecimal can have a negative scale to allow precision management for integers
	 * that is fewer than the digits provided.
	 *
	 * @param value
	 * @param precision
	 * @return
	 */
	public static BigDecimal make(String value, int precision) {
		BigDecimal number = new BigDecimal(value);

		int deltaPrecision = precision - number.precision();
		int disparity = number.scale() + deltaPrecision;

		if (number.precision() != precision) {
			number = number.setScale(disparity, SigFigMathUtil.DEFAULT_ROUNDING_RULE);
		}
		if (number.precision() != precision) {
			number = new ScientificDecimal(value, precision);
		}

		return number;
	}
	public static BigDecimal make(BigDecimal value, int precision) {
		if (value.precision() == precision) {
			return value;
		}
		return make(value.toPlainString(), precision);
	}

	public ScientificDecimal(String value, int specifiedSigfigs) {
		this(value, specifiedSigfigs, SigFigMathUtil.DEFAULT_ROUNDING_RULE);
	}
	public ScientificDecimal(String value, int specifiedSigfigs, RoundingMode roundingRule) {
		super(value);

		bigDecimal = new BigDecimal(value);
        sigfigs = specifiedSigfigs;
        if (sigfigs < 1) {
            return;
        }

        int scale = bigDecimal.scale();
		int precision = bigDecimal.precision();
        int integerDigits = precision - scale;
		int missingPrecision = sigfigs - precision;

        if (integerDigits < 0 || BigDecimal.ZERO.compareTo(bigDecimal) == 0) {
            bigDecimal = bigDecimal.setScale(sigfigs);
        }  else if (missingPrecision != 0) {
			int scaleShouldBe = sigfigs - integerDigits;
			if (scaleShouldBe >= 0) {
                bigDecimal = bigDecimal.setScale(scaleShouldBe, roundingRule);
            }
        }
	}
	public ScientificDecimal(String value) {
		super(value);
		bigDecimal = new BigDecimal(value);
		sigfigs = sigfigRules(value);
	}
	public ScientificDecimal(long value) {
		this(""+value);
	}
	public ScientificDecimal(long value, int sigfigs) {
		this(""+value, sigfigs);
	}

	/**
	 * Use this helper to set the scale to satisfy the significant figures.
	 * @param number   the number to set  significant figures
	 * @param sigfigs  significant figures
	 * @return
	 */
	public ScientificDecimal(BigDecimal number, int sigfigs) {
		this(number, sigfigs, SigFigMathUtil.DEFAULT_ROUNDING_RULE);
	}
	public ScientificDecimal(BigDecimal number, int sigfigs, RoundingMode roundingRule) {
		this(number.toPlainString(), sigfigs, roundingRule);
	}



	@Override
	public String toPlainString() {
		return bigDecimal.toPlainString();
	}

	@Override
	public String toString() {
		return bigDecimal.toPlainString();
	}

	@Override
	public int precision() {
		return sigfigs==null ?bigDecimal.precision() :sigfigs;
	}

	public BigDecimal setPrecision(int newPrecision) {
		return setSigfigs(newPrecision);
	}

	@Override
	public int scale() {
		return bigDecimal.scale();
	}

	@Override
	public BigDecimal setScale(int newScale) {
		return setScale(newScale, SigFigMathUtil.DEFAULT_ROUNDING_RULE);
	}

	@Override
	public BigDecimal setScale(int newScale, int oldMode) {
		return setScale(newScale, RoundingMode.valueOf(oldMode));
	}

	@Override
	public BigDecimal setScale(int newScale, RoundingMode roundingMode) {
		int deltaScale = newScale - scale();
		int newPrecision = deltaScale + precision();
		if (BigDecimal.ZERO.compareTo(this) == 0) {
			return new ScientificDecimal("0.0", newPrecision);
		}
		return setSigfigs(newPrecision);
	}

	public ScientificDecimal setSigfigs(int sigfigs) {
		return new ScientificDecimal(bigDecimal.toPlainString(), sigfigs);
	}

	// the proper handling of 0.000...
	// add more rules here if/when discovered.
	protected int sigfigRules(String value) {
		int sigfigs = bigDecimal.precision();

		// this is for 1000 vs 1000.
		if ( ! value.contains(".") ) {
			String figs = value.replaceAll("0+$", "");
			sigfigs = figs.length();
		}
		// this is for 0.0000 vs 0
		if (bigDecimal.compareTo(BigDecimal.ZERO) == 0 ) {
			sigfigs = bigDecimal.scale();
		}
		return sigfigs;
	}
}
