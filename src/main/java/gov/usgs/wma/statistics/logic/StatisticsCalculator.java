package gov.usgs.wma.statistics.logic;

import static gov.usgs.wma.statistics.app.Properties.*;
import static gov.usgs.wma.statistics.logic.SigFigMathUtil.*;
import static gov.usgs.wma.statistics.logic.ScientificDecimal.*;
import static gov.usgs.wma.statistics.model.Value.*;
import static org.apache.commons.lang.StringUtils.*;

import java.io.Reader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.ngwmn.logic.WaterLevelStatistics;
import gov.usgs.ngwmn.model.Specifier;
import gov.usgs.wma.statistics.app.Properties;
import gov.usgs.wma.statistics.model.JsonData;
import gov.usgs.wma.statistics.model.JsonDataBuilder;
import gov.usgs.wma.statistics.model.Value;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * The base statistics does nothing as place holder instances.
 * Each subclass will implement statistics calculations specific to the data type.
 * 
 * An example implementation is {@link WaterLevelStatistics}
 * 
 * @author duselman
 */
public class StatisticsCalculator<S extends Value> {
	private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsCalculator.class);
	
	public static final BigDecimal MEDIAN_PERCENTILE = new BigDecimal("0.500000000");
	//protected static final BigDecimal HUNDRED = new BigDecimal("100");
	protected static final BigDecimal TWELVE  = new BigDecimal("12");
	// Calendar returns millis for days and after a diff we need the number of days (ms * sec * min * hr == ms/day)
	protected static final BigDecimal MILLISECONDS_PER_DAY = new BigDecimal(1000 * 60 * 60 * 24);

	private static final String[] MONTH_NAMES;
	static {
		MONTH_NAMES = new DateFormatSymbols().getMonths();
		MONTH_NAMES[12] = "unknown";
	}

	protected final JsonDataBuilder builder;
	protected final Properties env;
	
	public StatisticsCalculator(Properties env) {
		this(env, new JsonDataBuilder(env));
	}
	public StatisticsCalculator(Properties env, JsonDataBuilder builder) {
		this.env = env;
		this.builder = builder;
	}
	
	
	/**
	 * Calculates statistics for a specifier where data must be supplied as an XML string reader.
	 * 
	 * This is used in the DatabaseXMLCache to supply data without requiring a unnecessary
	 * read from the database. This optimization saves about 5 sec per site. This may not sound like
	 * much; however, it only takes 3600 sites to save 5 hours!
	 * 
	 * @param spec the specifier only checks the agency and site. It ignores the data type.
	 * @param xmlData the Reader supplying XML data samples.
	 * @throws Exception Thrown if there is an issue parsing data from the XML reader.
	 */
	public JsonData calculate(Specifier spec, Reader xmlData) throws Exception {
		// TODO asdf I suppose this class should be abstract
		throw new NotImplementedException();
	}

	/**
	 * Calculates statistics for a specifier where data must be supplied as a list of Samples.
	 * 
	 * @param spec the specifier only checks the agency and site. It ignores the data type.
	 * @param samples list of DAO samples. For example, {@link Value} for {@link WaterLevelStatistics}.
	 * @throws Exception Thrown if there is an issue calculating statisics
	 */
	public JsonData calculate(Specifier spec, List<S> samples)  {
		// TODO asdf I suppose this class should be abstract
		throw new NotImplementedException();
	}
	
	
	public List<S> conditioning(Specifier spec, List<S> samples) {
		removeNulls(samples, spec.getAgencyCd()+":"+spec.getSiteNo());
		removeProvisional(samples, spec.toString());
		checkAllDates(samples);
		sortByDateOrder(samples); // ensure date order
		return samples;
	}
	
	
	public void checkAllDates(List<S> samples) {

		// java does not have stream().forEachWithIndex()
		String today = today();

		for (int i=0; i<samples.size(); i++) {
			S sample = samples.get(i);
			String utc = sample.time;
			String msg;
			if ( isBlank(utc) ) {
				msg = env.getError(ENV_INVALID_ROW_DATE_BLANK, i+1);
				builder.error(msg);
			} else if ( today.compareTo(utc) == -1 ) {
				msg = env.getError(ENV_INVALID_ROW_DATE_FUTURE, i+1, utc);
				builder.error(msg);
			} else {
				String fixed = fixMissingMonthAndDay(utc);
				int delta = fixed.length() - utc.length();
				sample.time = fixed;
				if (delta <= 0) {
					continue; // date is fine and was not fixed
				} else if (delta <= 3) {
					msg = env.getMessage(ENV_MESSAGE_DATE_FIX_DAY, i+1, sample.toCSV());
					builder.message(msg);
				} else {
					msg = env.getMessage(ENV_MESSAGE_DATE_FIX_MONTH, i+1, sample.toCSV());
					builder.message(msg);
				}
			}
			LOGGER.trace(msg);
		}
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
		if (sample==null || samples==null || samples.size()==0 || valueOf == null || valueOf.apply(sample)==null ) {
			return ScientificDecimal.ZERO;
		}
		BigDecimal sampleValue = valueOf.apply(sample);

		// add one because of java zero based index vs the one based index of mathematics
		BigDecimal index = new BigDecimal( samples.indexOf(sample) + 1 );
		BigDecimal n     = new BigDecimal(samples.size());
		BigDecimal n1    = n.add(ScientificDecimal.ONE);
		BigDecimal n1inv = ScientificDecimal.ONE.divide(n1, 10, DEFAULT_ROUNDING_RULE);

		BigDecimal n1invRnd = ScientificDecimal.ONE.divide(n1, sampleValue.precision(), DEFAULT_ROUNDING_RULE);
		// comment this precision
		BigDecimal pct   = index.divide(n1, precision, DEFAULT_ROUNDING_RULE);
		
		// manage near   0 percentile
		if (pct.compareTo(n1invRnd) <= 0 ) {
			return ScientificDecimal.ZERO.setScale(1);
		}
		// manage near 100 percentile
		MathContext mc = new MathContext(sampleValue.precision(), DEFAULT_ROUNDING_RULE);
		if (pct.compareTo(n.multiply(n1inv, mc)) >= 0) {
			return ScientificDecimal.ONE.setScale(3);
		}
		
		return pct;
	}
	public static <T> BigDecimal percentileOfValue(List<T> samples, T sample, Function<T, BigDecimal> valueOf) {
		if (sample == null || valueOf == null) {
			return ScientificDecimal.ZERO;
		}
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
	public BigDecimal valueOfPercentile(List<S> samples, BigDecimal percentileAsFraction, int precision,
			Function<S, BigDecimal> valueOf) {
		
		// protection from null and ranges
		if (   samples == null                                            // samples are required (no null nor zero sample count)
			|| samples.size()==0                                          // avoid ArrayIndexOutOfBoundsException
			// proper fraction range
			|| percentileAsFraction == null                               // percentile  avoid NullPointerException
			|| percentileAsFraction.compareTo(BigDecimal.ZERO) < 0        // less than 0% is undefined
			|| percentileAsFraction.compareTo(BigDecimal.ONE) > 0 ) {     // greater than 100% is just as foolish
			return BigDecimal.ZERO;                                       // no precision in this response
		}
		// TODO asdf some math in here should be raw calls to BigDecimal and some should be sigfigutils calls
		// total records, n, n+1, and its inverse, 1/(n+1)
		BigDecimal n     = new ScientificDecimal(samples.size(),EXACT_SCALE);// the number of records
		BigDecimal n1    = n.add(ScientificDecimal.ONE);                  // one more than the number of records
		BigDecimal n1inv = ScientificDecimal.ONE.divide(n1, EXACT_SCALE, DEFAULT_ROUNDING_RULE); // 1/(n+1) presume 10 digits

		// manage boundary condition near   0 percentile
		if (percentileAsFraction.compareTo(n1inv) <= 0 ) {
			return valueOf.apply(samples.get(0));
		}
		// manage boundary condition near 100 percentile
		if (percentileAsFraction.compareTo(n.multiply(n1inv)) >= 0) {
			return valueOf.apply(samples.get( samples.size()-1 ));
		}
		
		// pct float index, p, and its parts. the int index, k, and the decimal fraction, d.
		BigDecimal p     = percentileAsFraction.setScale(EXACT_SCALE)
												.multiply(n1); // raw index to be used with faction
		int k            = p.intValue();                                  // the integer index value
		
		// Y[k] and Y[k+1] (but java is zero based indexing thus k-1 and k)
		BigDecimal yk    = valueOf.apply(samples.get(k-1));               // first index value
		BigDecimal yk1   = valueOf.apply(samples.get(k));                 // second index value
		int leastPrecision = SigFigMathUtil.getLeastPrecise(yk,yk1);

		// percentile calculation Y(p) = Y[k] + d(Y[k+1] - Y[k])
		BigDecimal diff  = sigFigSubtract(yk1, yk).setScale(EXACT_SCALE);           // delta between the two values

		if (BigDecimal.ZERO.compareTo(diff) == 0) {
			return ScientificDecimal.updateScaleForSigFigs(yk, leastPrecision);
		}
		BigDecimal d     = p.subtract(new BigDecimal(k));                 // the decimal index value (or fraction between two indexes)
		if (BigDecimal.ZERO.compareTo(d) == 0) {
			d = ScientificDecimal.ZERO.setScale(leastPrecision);
		}
		BigDecimal delta = diff.multiply(d);                              // the fraction of the difference of two values k and k+1
		if (BigDecimal.ZERO.compareTo(delta) == 0) {
			delta = ScientificDecimal.ZERO.setScale(leastPrecision-1);
		}
		BigDecimal yp  = yk.add(delta);                                   // and finally, the percentile value
		MathContext mc = new MathContext(leastPrecision, DEFAULT_ROUNDING_RULE);
		return yp.round(mc);
	}
	public BigDecimal valueOfPercentile(List<S> samples, BigDecimal percentileAsFraction,
			Function<S, BigDecimal> valueOf) {
		return valueOfPercentile(samples, percentileAsFraction, percentileAsFraction.precision(), valueOf);
	}
	/**
	 * @param samples for a given sample set in order 
	 * @return map of 10th 25th 50th 75th and 90th percentiles for the given list
	 */
	protected JsonDataBuilder generatePercentiles(List<S> samples, Map<String, BigDecimal> percentiles) {
		for(String percentile : percentiles.keySet()) {
			BigDecimal pct = percentiles.get(percentile);
			BigDecimal pctValue = valueOfPercentile(samples, pct, Value::valueOf);
			builder.putPercentile(percentile, pctValue.toString());
		}
		return builder;
	}
	
	
	/**
	 * Returns the number of unique years in samples. It is used to determine if a month qualifies
	 * for statistical evaluation and it is used for the number of years on record for the month.
	 * It looks like GWW does this rather than a date difference. NGWMN used the date difference and
	 * was off by being less a year on some monthly data.
	 * 
	 * This, rounding method, data to used, and significant figures are prime examples of the difficulty
	 * in trying to reverse engineer a series of calculations. First, you presume the method was done
	 * with due diligence, then you compare results. If results match then you presume you have the a
	 * matching method. This method is only the most recent guess as to how GWW deviates for expectations.
	 * 
	 * @param samples
	 * @return count of years
	 */
	protected int uniqueYears(List<S> samples) {
		// if the data is empty (we should not have gotten this far but) there are zero years. 
		if (samples == null || samples.size() == 0) {
			return 0;
		}
		Set<String> uniqueYears = new HashSet<>();
		for (S sample : samples) {
			uniqueYears.add( yearUTC(sample.time) );
		}
		return uniqueYears.size();
	}

		
	/**
	 * This removes provisional value samples from a collection of samples.
	 * @param samples the samples to examine in temporal order
	 * @param mySiteId for logging purposes if there are nulls removed to ID the site with nulls
	 */
	protected void removeProvisional(List<S> samples, String mySiteId) {
		List<S> provisionalSamples = new LinkedList<>();
		
		for (int s=0; s<samples.size(); s++) {
			S sample = samples.get(s);
			if ( sample == null || sample.isProvisional()) {
				provisionalSamples.add(sample);
			}
		}
		samples.removeAll(provisionalSamples);
		
		if (provisionalSamples.size() > 0) {
			int count = provisionalSamples.size();
			String msg = env.getMessage(ENV_MESSAGE_OMIT_PROVISIONAL, count, count==1?"":"s");
			builder.message(msg);
		}
	}
	
	/**
	 * This removes null value samples from a collection of samples.
	 * @param samples the samples to examine
	 * @param mySiteId for logging purposes if there are nulls removed to ID the site with nulls
	 */
	public void removeNulls(List<S> samples, String mySiteId) {
		List<S> nullSamples = new LinkedList<>();
		
		for (S sample : samples) {
			// TODO decide on actual rules and understand why there are nulls
			// actually, I now think that the DAO filters out nulls 
			if (sample == null || sample.value==null || sample.time==null) {
				nullSamples.add(sample);
			}
		}
		
		if (nullSamples.size() > 0) {
			String plural = nullSamples.size()!=1 ?"s" :"";
			// tried to use Java Streams but did not compile
			String sep = "";
			StringBuilder buff = new StringBuilder();
			for (S sample: nullSamples) {
				buff.append(sep).append(""+(samples.indexOf(sample)+1));
				sep = ", ";
			}
			String msg = env.getMessage(ENV_MESSAGE_OMIT_NULL, nullSamples.size(), plural, plural, buff.toString());
			builder.message(msg);
		}
		
		samples.removeAll(nullSamples);
	}
	
	
	public static String today() {
		return DATE_FORMAT_FULL.format(new Date());
	}
	
	public static <V extends Value> void sortByDateOrder(List<V> samples) {
		Collections.sort(samples, Value.TIME_COMPARATOR);
	}
	
	public static <S extends Value> List<S> sortByValueOrderAscending(List<S> samples) {
		Collections.sort(samples, SORT_VALUE_ASCENDING);
		return samples;
	}
	public static <S extends Value> List<S> sortByValueOrderDescending(List<S> samples) {
		Collections.sort(samples, SORT_VALUE_DESCENDING);
		return samples;
	}

	
	// This method must be overridden by subclasses that do not use Value for samples
	@SuppressWarnings("unchecked")
	protected S makeMedian(List<S> samples) {
		// years median in the this month
		BigDecimal median = valueOfPercentile(samples, MEDIAN_PERCENTILE, Value::valueOf);
		S base = samples.get( (int)(samples.size()/2) );
		Value medianSample = new Value(base.time, median);
		return (S)medianSample;
	}
	
	
	public Map<String, List<S>> sortSamplesByYear(List<S> monthSamples) {
		Map<String,List<S>>yearSamples = new HashMap<>();
		for (S sample : monthSamples) {
			String year = yearUTC(sample.time);
			List<S> samples = yearSamples.get(year);
			if (samples == null) {
				samples = new LinkedList<>();
				yearSamples.put(year,samples);
			}
			samples.add(sample);
		}
		return yearSamples;
	}
	
	public static BigDecimal yearDiff(String maxDate, String minDate) {
		BigDecimal diff = new BigDecimal(yearUTC(maxDate))
				.subtract( new BigDecimal(yearUTC(minDate)) )
				.add( new BigDecimal(monthUTC(maxDate))
						.subtract(new BigDecimal(monthUTC(minDate)))
						.divide(TWELVE, 1, RoundingMode.HALF_EVEN)
					);
		return diff;
	}
	
	
	public static BigDecimal daysDiff(String maxDate, String minDate) {
		
		try {
			Date begin   = DATE_FORMAT_FULL.parse(minDate);
			Date end     = DATE_FORMAT_FULL.parse(maxDate);
			
			Calendar cal = Calendar.getInstance();

			cal.setTime(begin);
			long start   = cal.getTimeInMillis();
			
			cal.setTime(end);
			long stop    = cal.getTimeInMillis();
			
			long diff    = stop - start;
			BigDecimal days = new BigDecimal(diff).divide(MILLISECONDS_PER_DAY);
			return days;
		} catch (ParseException e) {
			throw new IllegalArgumentException("Bad dates: '" + minDate + "' or '" + maxDate +"'");
		}
	}
	
	/**
	 * returns empty string if there is no date at all.
	 * returns the date if there is one containing 10 chars
	 * returns the 15th of the month if day of month is missing
	 * returns June 30th if the year is all that is specified
	 * returns empty string as default
	 * @param date the date string to refine
	 * @return refined date
	 */
	public static String fixMissingMonthAndDay(String date) {
		if ( isBlank(date) ) {
			return "";
		}
		if (date.length() >= 10) { // YYYY-MM-DD
			return date.substring(0,10);
		}
		if (date.length() >= 7) { // YYYY-MM
			return date.substring(0,7) + "-15";
		}
		if (date.length() >= 4) { // YYYY
			return date.substring(0,4) + "-06-30";
		}
		
		return "";
	}
	

	public String sampleMonthName(Value sample) {
		try {
			String monthStr = monthUTC(sample.time);
			int month = Integer.parseInt(monthStr);
			return MONTH_NAMES[month-1];
		} catch (Exception e) {
			// errors will return "none"
			return MONTH_NAMES[12];
		}
	}
}
