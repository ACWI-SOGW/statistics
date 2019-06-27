package gov.usgs.ngwmn.logic;

import static gov.usgs.wma.statistics.app.Properties.*;
import static gov.usgs.wma.statistics.model.JsonDataBuilder.*;
import static org.apache.commons.lang.StringUtils.*;

import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.ngwmn.model.DepthDatum;
import gov.usgs.ngwmn.model.MediationType;
import gov.usgs.ngwmn.model.PCode;
import gov.usgs.ngwmn.model.Specifier;
import gov.usgs.ngwmn.model.WLSample;
import gov.usgs.wma.statistics.app.Properties;
import gov.usgs.wma.statistics.logic.MonthlyStatistics;
import gov.usgs.wma.statistics.logic.OverallStatistics;
import gov.usgs.wma.statistics.logic.SigFigMathUtil;
import gov.usgs.wma.statistics.logic.StatisticsCalculator;
import gov.usgs.wma.statistics.model.JsonData;
import gov.usgs.wma.statistics.model.JsonDataBuilder;
import gov.usgs.wma.statistics.model.Value;

public class WaterLevelStatistics extends StatisticsCalculator<WLSample> {
	private static final Logger LOGGER = LoggerFactory.getLogger(WaterLevelStatistics.class);
	/**
	 * This is the agreed upon days window for a recent value. It is computed from
	 * 1 year + 1 month + 1.5 weeks or 365 + 30 + 7 + 4 because of how samples are taken and eventually entered.
	 */
	protected static final BigDecimal Days406 = new BigDecimal("406");

	/**
	 * The number 100 used to make a decimal percentile into a %100 based value.
	 * It has many decimal places so that this number preserves the precision of the original value.
	 */
	protected static final BigDecimal NUM_100 = new BigDecimal("100.000000");

	// Package level access for unit testing
	MonthlyStatistics<WLSample> monthlyStats;
	OverallStatistics<WLSample> overallStatistics;
	
	
	public WaterLevelStatistics(Properties env, JsonDataBuilder builder) {
		super(env, builder);
		
		monthlyStats = new WaterLevelMonthlyStats(env, builder);
		
		overallStatistics = new OverallStatistics<WLSample>(env, builder) {
			@Override
			public void findMinMaxDatesAndDateRange(List<WLSample> samples, List<WLSample> sortedByValue) {
				super.findMinMaxDatesAndDateRange(samples, sortedByValue);
				removeMostRecentProvisional(samples, sortedByValue);
			}
		};
	}
	
	public void setMediation(MediationType mediation) {
		this.builder.mediation(mediation);
	}
	
	public MonthlyStatistics<WLSample> getMonthlyStats() {
		return monthlyStats;
	}
	
	@Override
	protected void removeProvisional(List<WLSample> samplesByDate, String dataSetId) {
		removeProvisionalButNotMostRecent(samplesByDate, dataSetId);
	}
	/**
	 * This removes provisional value samples from a collection of samples.
	 * However, it is required that the most recent sample be retained regardless of status
	 * @param samples the samples to examine in temporal order
	 * @param mySiteId for logging purposes if there are nulls removed to ID the site with nulls
	 */
	protected void removeProvisionalButNotMostRecent(List<WLSample> samples, String mySiteId) {
		// retain most recent sample
		WLSample latestSample = samples.get(samples.size()-1);
		
		super.removeProvisional(samples, mySiteId);
		
		// but only add it back in if it is provisional
		if (latestSample.isProvisional()) {
			samples.add(latestSample);
		}
	}
	protected void removeMostRecentProvisional(List<WLSample> samples, List<WLSample> sortedByValue) {
		WLSample maxDate =samples.get(samples.size()-1);
		
		if (maxDate.isProvisional()) {
			samples.remove(maxDate);
			sortedByValue.remove(maxDate);
			String msg = env.getMessage(ENV_MESSAGE_PROVISIONAL_RULE);
			builder.message(msg);
		}
	}
	
	// TODO business rule for any one value error? continue without value or error entire statistics calculation?
	@Override
	public JsonData calculate(Specifier spec, Reader xmlData) throws Exception {
		List<WLSample> samples = WLSample.extractSamples(xmlData, spec.getAgencyCd(), spec.getSiteNo(), spec.getElevation());
		return calculate(spec, samples);
	}

	@Override
	public JsonData calculate(Specifier spec, List<WLSample> samples) {
		LOGGER.trace("Executing WaterLevel Stats calculations.");
		
		List<WLSample> samplesByDate = conditioning(spec, samples);

		if (builder.hasErrors()) {
			return builder.build();
		}
		if (spec.hasAgency()) {
			MediationType mediation = findMostPrevalentMediation(spec, samples);
			builder.mediation(mediation);
		}
		convertToMediatedValue(spec, samples, builder.mediation());
		List<WLSample> sortedByValue  = new ArrayList<>(samplesByDate);
		monthlyStats.sortValueByQualifier(sortedByValue);
		
		overallStats(samplesByDate, sortedByValue);
		
		if ( isNotBlank( builder.get(RECORD_YEARS) ) ) {
			BigDecimal years = new BigDecimal( builder.get(RECORD_YEARS) );
			String recent = builder.get(MAX_DATE);
			String today = today();
			
			try {
				// removed the overall qualification "optimization" because overall requires the monthly medians
				builder.collect(); // collect all stats into a monthly obj
				monthlyStats.monthlyStats(sortedByValue);
			} catch (Exception e) {
				// if anything goes wrong here we still want the overall
				LOGGER.warn("Data for this ID {}:{}, had an unhandled exception. {}", spec.getAgencyCd(), spec.getSiteNo(), e);
			}
		} else {
			LOGGER.warn("Record Years is null for {}:{}, by passing monthly stats.", spec.getAgencyCd(), spec.getSiteNo());
		}
		
		normalizedOverallMedian();
		
		if ( ! builder.hasMonthly() ) {
			String msg = env.getMessage(ENV_MESSAGE_MONTHLY_RULE, Days406.intValue());
			builder.message(msg);
		}
		
		return builder.build();
	}

	/**
	 * This overrides the median of all values with a median of monthly median values
	 */
	protected void normalizedOverallMedian() {
		// get a local list of values that will not destroy the original and be of WLSample instance
		List<WLSample> normalized = builder.getIntermediateValuesList().stream()
				.map(value -> new WLSample(value.value))
				.collect(Collectors.toList());
		// sort the values by the qualifier order - typically BelowLand, and register the median
		monthlyStats.sortValueByQualifier(normalized);
		BigDecimal medianValue = valueOfPercentile(normalized, MEDIAN_PERCENTILE, Value::valueOf);
		builder.newOverallMedian(medianValue.toPlainString());
	}

	protected void overallLatestPercentile(List<WLSample> samplesByDate) {
		// get the latest (most recent) sample
		int last = samplesByDate.size()-1;
		WLSample latestSample = samplesByDate.get(last);
		// get the sample for the same month as the latest sample
		String month = Value.padMonth(Value.monthUTC(latestSample.time));
		List<WLSample> monthSamples = monthlyStats.filterValuesByGivenMonth(samplesByDate, month);
		// get the medians for each year-month
		List<WLSample> normalizeMutlipleYearlyValues = 
				monthlyStats.medianMonthlyValues(monthSamples,  monthlyStats.sortFunctionByQualifier());
		if (normalizeMutlipleYearlyValues.size()<9) {
			// do not calculate latest percentile for months with low data
			builder.latestPercentile(null);
			return;
		}
		// the most recent must now be added into the collection and replace of the year-month it represents
		replaceLatestSample(normalizeMutlipleYearlyValues, latestSample);
		// get the percentile of the latest sample
		BigDecimal latestPercentile = percentileOfValue(normalizeMutlipleYearlyValues, latestSample, Value::valueOf);
		latestPercentile = SigFigMathUtil.sigFigMultiply(latestPercentile, NUM_100.setScale(latestPercentile.scale()));
		builder.latestPercentile(latestPercentile.toPlainString());
	}

	protected void replaceLatestSample(List<WLSample> normalizeMutlipleYearlyValues, WLSample latestSample) {
		if (normalizeMutlipleYearlyValues.contains(latestSample)) {
			return; // if it happens to be in there leave it be
		}
		// initial conditions and values
		int        compareToVal  = MediationType.BelowLand.equalSortOrder(builder.mediation()) ?1 :-1;
		int        elements      = normalizeMutlipleYearlyValues.size();
		String     latestYrMonth = latestSample.getTime().substring(0, 7);
		BigDecimal latestValue   = latestSample.getValue();
		boolean    isInserting   = true;
		boolean    isRemoving    = true;
		// search for the removing current month and the inserting location for latest sample
		for (int s=0; s<elements && (isRemoving || isInserting); s++) {
			String yearMonth = ((Value)normalizeMutlipleYearlyValues.get(s)).getTime().substring(0, 7);
			BigDecimal value = ((Value)normalizeMutlipleYearlyValues.get(s)).getValue();
			// the data is already sorted, if we exceed the value in the comapreTo direction then insert
			if (isInserting && latestValue.compareTo(value) == compareToVal) {
				normalizeMutlipleYearlyValues.add(s, latestSample);
				elements++; s++;
				isInserting = false;
			}
			// if year-month of the latest sample is found then remove it
			if (yearMonth.equals(latestYrMonth)) {
				normalizeMutlipleYearlyValues.remove(s);
				elements--; s--;
				isRemoving = false;
			}
		}
		if (isInserting) {
			normalizeMutlipleYearlyValues.add(latestSample);
		}
	}	
	protected void overallStats(List<WLSample> samplesByDate, List<WLSample> sortedByValue) {
		if (samplesByDate == null || samplesByDate.size() == 0) {
			builder.recordYears("0");
			builder.sampleCount(0);
			builder.collect();
			return;
		}
		overallLatestPercentile(samplesByDate);
		overallStatistics.overallStats(samplesByDate, sortedByValue);
		// it might make sense to put the builder.collect() call here, it could;
		// however, most of the tests were written without it here to inspect the
		// the result prior to collect. If the tests are rewritten to inspect the
		// JsonOverall object then collect() could be called here. It is call after this.
	}
	

	/**
	 * Helper method that checks all USGS site PCODEs for the most prevalent mediation.
	 * @param spec site specification because only USGS site have PCODEs and the default is below surface
	 * @param samples the samples to potentially change mediation type
	 * @return Above or Below mediation type based on sample PCODE counts
	 */
	protected MediationType findMostPrevalentMediation(Specifier spec, List<WLSample> samples) {
		if ( ! "USGS".equals(spec.getAgencyCd())) {
			return MediationType.BelowLand;
		}
		int countBelowLand  = 0;
		int countAboveDatum = 0;
		int half = samples.size()/2+1; // add one for rounding up simply
		
		for (WLSample sample : samples) {
			PCode pcode = PCode.get(sample.pcode);
			
			if (pcode.isUnrecognized() || pcode.isUnspecified() // default is below
					// and there are below datum
					|| DepthDatum.BLS.equals(pcode.getDatum()) 
					|| DepthDatum.LAND_SURFACE.equals(pcode.getDatum()) ) {
				countBelowLand++;
			} else { // otherwise we are above
				countAboveDatum++;
			}
			// we need not look any more if one is more than half
			if (countBelowLand > half || countAboveDatum > half) {
				break;
			}
		}
		if (countAboveDatum > countBelowLand) {
			return MediationType.AboveDatum;
		}
		
		return MediationType.BelowLand; // default
	}
	/**
	 * Helper method to change out the value (below surface) to valueAboveDatum if above datum mediation is most prevalent.
	 * Otherwise, it will return the default which is below surface mediated.
	 * 
	 * @param spec site specification because only USGS site have PCODEs and the default is below surface
	 * @param samples the samples to potentially change mediation type
	 * @param mediation the determined majority PCODE mediation direction.
	 * @return the original list if below surface mediation and a new sample list if above datum mediation
	 */
	protected List<WLSample> convertToMediatedValue(Specifier spec, List<WLSample> samples, MediationType mediation) {
		// default value will be depth below land surface as given from WaterLevelDAO.extractSamples()
		// all samples are pre-mediated in both directions and the value is depth below land surface
		// the will determine if value should be changed to the height above a datum (like sea floor)
		// all values will use the same mediation of the most prevalent.
		for (WLSample sample : samples) {
			if (mediation == MediationType.AboveDatum) {
				sample.value = sample.valueAboveDatum;
			} else if (mediation == MediationType.BelowLand) {
				sample.value = sample.valueBelowLand;
			}
		}
		
		return samples;
	}
	

	/**
	 * This convenience method is supposed to be self documenting by its name. But just to be clear it
	 * ensures at least ten years of data are required and the most recent value must be within the past year
	 * 
	 * Not that this method could have computed all the values by a given data set of overall data. However,
	 * it is easier to test the conditions if the values are given. For example, if today was determined inside
	 * then the test could not use a given date for today.
	 * 
	 * @param years  the years of data
	 * @param recent the date of the most recent value
	 * @param today  today's date in the same UTC format
	 * @return
	 */
	protected boolean doesThisSiteQualifyForMonthlyStats(BigDecimal years, String recent, String today) {
		return BigDecimal.TEN.compareTo(years) <= 0 && Days406.compareTo(daysDiff(today, recent)) >= 0;
	}

	protected WLSample makeMedian(List<WLSample> samples) {
		// years median in the this month
		BigDecimal medianValue = super.makeMedian(samples).value;
		BigDecimal medianAbove = valueOfPercentile(samples, MEDIAN_PERCENTILE, WLSample::valueOfAboveDatum);
		WLSample base = samples.get( (int)(samples.size()/2) );
		WLSample medianSample = new WLSample(medianValue, medianAbove, base);
		return medianSample;
	}
	
}
