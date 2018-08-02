package gov.usgs.ngwmn.logic;

import static gov.usgs.ngwmn.logic.SigFigMathUtil.*;
import static gov.usgs.ngwmn.model.WLSample.*;

import java.io.Reader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.ngwmn.model.WellDataType;
import gov.usgs.ngwmn.model.WLSample;
import gov.usgs.ngwmn.logic.WaterLevelStatistics.MediationType;
import gov.usgs.ngwmn.model.Specifier;

/**
 * The base statistics does nothing as place holder instances.
 * Each subclass will implement statistics calculations specific to the data type, {@link WellDataType}.
 * 
 * An example implementation is {@link WaterLevelStatistics}
 * 
 * @author duselman
 */
public class StatisticsCalculator<S> {
	public static final String RECORD_YEARS  = "RECORD_YEARS";
	public static final String MIN_DATE      = "MIN_DATE";
	public static final String MAX_DATE      = "MAX_DATE";
	public static final String MIN_VALUE     = "MIN_VALUE";
	public static final String MAX_VALUE     = "MAX_VALUE";
	public static final String P50_MIN       = "P50_MIN";
	public static final String P50_MAX       = "P50_MAX";
	public static final String SAMPLE_COUNT  = "SAMPLE_COUNT";
	public static final String P10           = "P10";
	public static final String P25           = "P25";
	public static final String P50           = "P50";
	public static final String P75           = "P75";
	public static final String P90           = "P90";
	public static final String MEDIAN        = "MEDIAN"; // P50
	public static final String LATEST_PCTILE = "LATEST_PCTILE";
	public static final String IS_RANKED     = "IS_RANKED";
	public static final String LATEST_VALUE  = "LATEST_VALUE";
	public static final String MEDIATION     = "MEDIATION";
	public static final String CALC_DATE     = "CALC_DATE";

	public static final SimpleDateFormat YEAR_MONTH_DAY = new SimpleDateFormat("yyyy-MM-dd"); 
	protected static final SimpleDateFormat YEAR_MONTH = new SimpleDateFormat("yyyy-MM"); 
	//protected static final BigDecimal HUNDRED = new BigDecimal("100");
	protected static final BigDecimal TWELVE  = new BigDecimal("12");
	
	public static final Map<String, BigDecimal> PERCENTILES;
	static {
		PERCENTILES =  new HashMap<>();
		// these are exact percentiles and should not limit measured precision
		addPercentile(P10, "0.100000000");
		addPercentile(P25, "0.250000000");
		addPercentile(P50, "0.500000000");
		addPercentile(P75, "0.750000000");
		addPercentile(P90, "0.900000000");
	}
	// Setter to allow for extra percentiles in string format
	public static void addPercentile(String name, String value) {
		addPercentile(name, new BigDecimal(value));
	}
	// Setter to allow for extra percentiles
	private static void addPercentile(String name, BigDecimal percentile) {
		PERCENTILES.put(name, percentile);
	}

	private final transient Logger logger = LoggerFactory.getLogger(getClass());
	
	/**
	 * Calculates statistics for a specifier where data must be fetched from the database.
	 * 
	 * @param spec the specifier only checks the agency and site. It ignores the data type.
	 * @throws Exception Thrown if there is an issue fetching data from the database.
	 */
	public String calculate(Specifier spec) throws Exception {
		return "";
	}
	
	/**
	 * Calculates statistics for a specifier where data must be supplied as an XML string reader.
	 * 
	 * This is used in the {@link DatabaseXMLCache} to supply data without requiring a unnecessary
	 * read from the database. This optimization saves about 5 sec per site. This may not sound like
	 * much; however, it only takes 3600 sites to save 5 hours!
	 * 
	 * @param spec the specifier only checks the agency and site. It ignores the data type.
	 * @param reader the {@link Reader} supplying XML data samples.
	 * @throws Exception Thrown if there is an issue parsing data from the XML reader.
	 */
	public String calculate(Specifier spec, Reader xmlData) throws Exception {
		return "";
	}
	
	/**
	 * Calculates statistics for a specifier where data must be supplied as a list of Samples.
	 * 
	 * @param spec the specifier only checks the agency and site. It ignores the data type.
	 * @param samples list of DAO samples. For example, {@link WLSample} for {@link WaterLevelStatistics}.
	 * @throws Exception Thrown if there is an issue calculating statisics
	 */
	public String calculate(Specifier spec, List<S> samples)  {
		return "";
	}
	


	/**
	 * This returns the percentile of a given a sample in a sample "set"
	 * @param samples the given samples must be ordered by the value
	 * @param sample the sample to determine percentile
	 * @return percentile fraction. 50th percentile would  return 0.50
	 */
	public static <T> BigDecimal percentileOfValue(List<T> samples, T sample, int precision,
			Function<T, BigDecimal> valueOf) {
		// protection - TODO is this the behavior we want? - returning 0
		if (sample==null || samples==null || samples.size()==0 || valueOf.apply(sample)==null ) {
			return BigDecimal.ZERO;
		}
		BigDecimal sampleValue = valueOf.apply(sample);
		
		// add one because of java zero based index vs the one based index of mathematics
		BigDecimal index = new BigDecimal( samples.indexOf(sample) + 1 );
		BigDecimal n     = new BigDecimal(samples.size());
		BigDecimal n1    = n.add(BigDecimal.ONE);
		BigDecimal n1inv = BigDecimal.ONE.divide(n1, 10, RoundingMode.HALF_EVEN);

		BigDecimal n1invRnd = BigDecimal.ONE.divide(n1, sampleValue.precision(), RoundingMode.HALF_EVEN);
		// comment this precision
		BigDecimal pct   = index.divide(n1, sampleValue.precision(), RoundingMode.HALF_EVEN);
		
		// manage near   0 percentile
		if (pct.compareTo(n1invRnd) <= 0 ) {
			return BigDecimal.ZERO;
		}
		// manage near 100 percentile
		MathContext mc = new MathContext(sampleValue.precision(),RoundingMode.HALF_EVEN);
		if (pct.compareTo(n.multiply(n1inv, mc)) >= 0) {
			return BigDecimal.ONE;
		}
		
		return pct;
	}
	public static <T> BigDecimal percentileOfValue(List<T> samples, T sample, Function<T, BigDecimal> valueOf) {
		BigDecimal sampleValue = valueOf.apply(sample);
		return percentileOfValue(samples, sample, sampleValue.precision(), valueOf);
	}
	
	/**
	 * This returns the interpolated sample value of a given percentile
	 * http://www.itl.nist.gov/div898/handbook/prc/section2/prc262.htm
	 * For 0 < k < N, Y(p) = Y[k] + d(Y[k+1] - Y[k])
	 * For k = 0, Y(p) = Y[1]
	 * For k = N, Y(p) = Y[N]
	 * @param samples the samples ordered by the value
	 * @param percentileAsFraction a value between 0 and 1 inclusive for the fractional percent to compute. 50% is .5
	 * @return the interpolated value of the requested percentile
	 */
	public static <T> BigDecimal valueOfPercentile(List<T> samples, BigDecimal percentileAsFraction, int precision,
			Function<T, BigDecimal> valueOf) {
		
		// protection from null and ranges
		if (   samples == null                                          // samples are required (no null nor zero sample count)
			|| samples.size()==0                                        // avoid ArrayIndexOutOfBoundsException
			// proper fraction range
			|| percentileAsFraction == null                             // percentile  avoid NullPointerException
			|| percentileAsFraction.compareTo(BigDecimal.ZERO) < 0      // less than 0% is undefined
			|| percentileAsFraction.compareTo(BigDecimal.ONE) > 0 ) {   // greater than 100% is just as foolish
			return BigDecimal.ZERO; //- TODO is this the behavior we want? - returning 0
		}
		
		// total records, n, n+1, and its inverse, 1/(n+1)
		BigDecimal n     = new BigDecimal(samples.size());              // the number of records
		BigDecimal n1    = n.add(BigDecimal.ONE);                       // one more than the number of records
		BigDecimal n1inv = BigDecimal.ONE.divide(n1, 10, RoundingMode.HALF_UP); // 1/(n+1) presume 10 digits

		// manage boundary condition near   0 percentile
		if (percentileAsFraction.compareTo(n1inv) <= 0 ) {
			return valueOf.apply(samples.get(0));
		}
		// manage boundary condition near 100 percentile
		if (percentileAsFraction.compareTo(n.multiply(n1inv)) >= 0) {
			return valueOf.apply(samples.get( samples.size()-1 ));
		}
		
		// pct float index, p, and its parts. the int index, k, and the decimal fraction, d.
		BigDecimal p     = percentileAsFraction.multiply(n1);           // raw index to be used with faction
		BigDecimal k     = new BigDecimal( p.intValue() );              // the integer index value
		
		// Y[k] and Y[k+1] (but java is zero based indexing thus k-1 and k)
		BigDecimal yk    = valueOf.apply(samples.get(k.intValue()-1));  // first index value
		BigDecimal yk1   = valueOf.apply(samples.get(k.intValue()));    // second index value
		
		// percentile calculation Y(p) = Y[k] + d(Y[k+1] - Y[k])
		BigDecimal diff  = sigFigSubtract(yk1, yk);                     // delta between the two values
		
		BigDecimal d     = p.subtract(k);                               // the decimal index value (or fraction between two indexes)
		BigDecimal delta = sigFigMultiply(diff, d);                     // the fraction of the difference of two values k and k+1
		BigDecimal yp    = sigFigAdd(yk, delta);                        // and finally, the percentile value 
		return yp;
	}
	public static <T> BigDecimal valueOfPercentile(List<T> samples, BigDecimal percentileAsFraction,
			Function<T, BigDecimal> valueOf) {
		return valueOfPercentile(samples, percentileAsFraction, percentileAsFraction.precision(), valueOf);
	}
	
	
	/**
	 * This removes null value samples from a collection of samples.
	 * @param samples the samples to examine
	 * @param mySiteId for logging purposes if there are nulls removed to ID the site with nulls
	 */
	protected void removeNulls(List<WLSample> samples, String mySiteId) {
		List<WLSample> nullSamples = new LinkedList<>();
		
		for (WLSample sample : samples) {
			// TODO decide on actual rules and understand why there are nulls
			// actually, I now think that the DAO filters out nulls 
			if (sample == null || sample.value==null || sample.time==null) {
				nullSamples.add(sample);
			}
		}
		samples.removeAll(nullSamples);
		
		if (nullSamples.size() > 0) {
			logger.warn("Removed {} samples from {}",nullSamples.size(), mySiteId);
		}
	}
	
	
	protected String today() {
		return YEAR_MONTH_DAY.format(new Date());
	}
	
	protected void sortByDateOrder(List<WLSample> samples) {
		Collections.sort(samples, WLSample.TIME_COMPARATOR);
	}

	public List<WLSample> normalizeMutlipleYearlyValues(List<WLSample> monthSamples, MediationType mediation) {
		List<WLSample> normalizedSamples = new LinkedList<>();

		Map<String, List<WLSample>> yearSamples = sortSamplesByYear(monthSamples);
		for (String year : yearSamples.keySet()) {
			List<WLSample> samples = yearSamples.get(year);
			if (samples.size() > 1) {
				// have to remove the averaged values from the monthly list
				monthSamples.removeAll(samples);
				// years median in the this month
				BigDecimal median = valueOfPercentile(samples, PERCENTILES.get(P50), WLSample::valueOf);
				WLSample base = samples.get( (int)(samples.size()/2) );
				
				WLSample medianSample = new WLSample(base.time, median, base.units, median, 
						base.comment, base.up, base.pcode, base.valueAboveDatum);
				
				normalizedSamples.add(medianSample);
			}
			else {
				normalizedSamples.addAll(samples);
			}
		}
		
		return normalizedSamples;
	}
	
	public Map<String, List<WLSample>> sortSamplesByYear(List<WLSample> monthSamples) {
		Map<String,List<WLSample>>yearSamples = new HashMap<>();
		for (WLSample sample : monthSamples) {
			String year = yearUTC(sample.time);
			List<WLSample> samples = yearSamples.get(year);
			if (samples == null) {
				samples = new LinkedList<>();
				yearSamples.put(year,samples);
			}
			samples.add(sample);
		}
		return yearSamples;
	}
	
}
