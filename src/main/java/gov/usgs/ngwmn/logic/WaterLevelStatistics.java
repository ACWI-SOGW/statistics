package gov.usgs.ngwmn.logic;

import static gov.usgs.wma.statistics.logic.OverallStatistics.*;
import static org.apache.commons.lang.StringUtils.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.usgs.ngwmn.model.DepthDatum;
import gov.usgs.ngwmn.model.PCode;
import gov.usgs.ngwmn.model.Specifier;
import gov.usgs.ngwmn.model.WLSample;
import gov.usgs.wma.statistics.logic.MonthlyStatistics;
import gov.usgs.wma.statistics.logic.OverallStatistics;
import gov.usgs.wma.statistics.logic.StatisticsCalculator;

public class WaterLevelStatistics extends StatisticsCalculator<WLSample> {
	
	protected static class WLMonthlyStats extends MonthlyStatistics<WLSample, WaterLevelStatistics.MediationType> {
		public WLMonthlyStats(MediationType qualifier) {
			super(qualifier);
		}
		@Override
		public void sortValueByQualifier(List<WLSample> samples) {
			if (qualifier == MediationType.BelowLand) {
				sortByValueOrderDescending(samples);
			} else {
				sortByValueOrderAscending(samples);
			}
		}
		@Override
		public Function<List<WLSample>, List<WLSample>> sortFunctionByQualifier() {
			Function<List<WLSample>, List<WLSample>> sortBy = StatisticsCalculator::sortByValueOrderAscending;
			
			if (qualifier == MediationType.BelowLand) {
				sortBy = StatisticsCalculator::sortByValueOrderDescending;
			}
			return sortBy;
		}
		@Override
		public boolean doesThisMonthQualifyForStats(List<WLSample> monthSamples) {
			if (monthSamples.size()<10) {
				return false;
			}
			return uniqueYears(monthSamples)>=10;
		}
		
	};
	
	
	OverallStatistics<WLSample> overallStatistics = new OverallStatistics<WLSample>() {
		@Override
		public Map<String,String> findMinMaxDatesAndDateRange(List<WLSample> samples, List<WLSample> sortedByValue) {
			Map<String,String>  stats = super.findMinMaxDatesAndDateRange(samples, sortedByValue);
			removeMostRecentProvisional(samples, sortedByValue);
			return stats;
		}
	};
	
	MonthlyStatistics<WLSample, MediationType> monthlyStats;
	
	/**
	 * This is the agreed upon days window for a recent value. It is computed from
	 * 1 year + 1 month + 1.5 weeks or 365 + 30 + 7 + 4 because of how samples are taken and eventually entered.
	 */
	protected static final BigDecimal Days406 = new BigDecimal("406");
	public static final String MONTHLY_WARNING =  "Too few data values for monthly statistics."
			+ " Ten years required with no gaps and most recent value within " + Days406 + " days.";

	private final transient Logger logger = LoggerFactory.getLogger(getClass());
	
	public static enum MediationType {
		BelowLand, 
		AboveDatum;
	}
	
	private MediationType mediation;
	public void setMediation(MediationType mediation) {
		this.mediation = mediation;
		monthlyStats = new WLMonthlyStats(mediation);
	}
	
	
	@Override
	public List<WLSample> conditioning(Specifier spec, List<WLSample> samples) {
		super.conditioning(spec, samples);
		MediationType mediation = findMostPrevalentMediation(spec, samples);
		setMediation(mediation);
		
		List<WLSample> samplesByDate = useMostPrevalentPCodeMediatedValue(spec, samples, mediation); 
		return samplesByDate;
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
		}
	}
	
	// TODO business rule for any one value error? continue without value or error entire statistics calculation?

	@Override
	public String calculate(Specifier spec, List<WLSample> samples) {
		logger.info("Executing WaterLevel Stats calculations.");
		
		List<WLSample> samplesByDate = conditioning(spec, samples);
		List<WLSample> sortedByValue  = new ArrayList<>(samplesByDate);
		monthlyStats.sortValueByQualifier(sortedByValue);
		
		Map<String, String> overall = overallStats(samplesByDate, sortedByValue);
		Map<String, Map<String, String>> monthly = null; // Could use empty collection rather than null?
		
		if ( isNotBlank( overall.get(RECORD_YEARS) ) ) {
			BigDecimal years = new BigDecimal( overall.get(RECORD_YEARS) );
			String recent = overall.get(MAX_DATE);
			String today = today();
			
			try {
				if ( doesThisSiteQualifyForMonthlyStats(years, recent, today) ) {
					overall.put(IS_RANKED, "Y");
					monthly = monthlyStats.monthlyStats(sortedByValue);
				}
			} catch (Exception e) {
				// if anything goes wrong here we still want the overall
				// TODO should we log it?
			}
		} else {
			logger.warn("Record Years is null for {}:{}, by passing monthly stats.", spec.getAgencyCd(), spec.getSiteNo());
		}
		Map<String, Object> stats = new HashMap<>();
		stats.put("overall", overall);
		
		if (monthly == null) {
			stats.put("monthly", MONTHLY_WARNING);
		} else {
			stats.put("monthly", monthly);
		}
		
		String json = "";
		try {
			json = new ObjectMapper().writeValueAsString(stats);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.err.println(json);
		
		return json;
	}

	protected Map<String,String> overallStats(List<WLSample> samples, List<WLSample> sortedByValue) {
		Map<String,String> stats = overallStatistics.overallStats(samples, sortedByValue);
		if (stats.size() == 0) {
			return stats;
		}
		
		String latestPercentile = monthlyStats.percentileBasedOnMonthlyData(samples.get(samples.size()-1), samples);
		stats.put(LATEST_PCTILE, latestPercentile);
		
		stats.put(MEDIATION, mediation.toString());
		return stats;
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
	protected List<WLSample> useMostPrevalentPCodeMediatedValue(Specifier spec, List<WLSample> samples, MediationType mediation) {
		// all non-USGS sites have one mediation
		if (! "USGS".equals(spec.getAgencyCd())) {
			return samples;
		}
		// default value will be depth below land surface as given from WaterLevelDAO.extractSamples()
		// all samples are pre-mediated in both directions and the value is depth below land surface
		// the will determine if value should be changed to the height above a datum (like sea floor)
		// all values will use the same mediation of the most prevalent.
		List<WLSample> mostPrevalent = samples;
		
		if (mediation == MediationType.AboveDatum) {
			mostPrevalent = new ArrayList<>(samples.size());
			for (WLSample sample : samples) {
				mostPrevalent.add(new WLSample(sample.time, sample.valueAboveDatum, sample.units, sample.originalValue, sample.comment, sample.up, sample.pcode, sample.valueAboveDatum));
			}
		}
		
		return mostPrevalent;
	}
	

	/**
	 * This convenience method is suppost to be self documenting by its name. But just to be clear it
	 * ensures at least ten years of data are required and the most recent value must be within the past year
	 * 
	 * Not that this method could have computed all thes values by a given data set of overall data. However,
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
		BigDecimal medianAbove = valueOfPercentile(samples, MEDIAN_PERCENTIAL, WLSample::valueOfAboveDatum);
		WLSample base = samples.get( (int)(samples.size()/2) );
		WLSample medianSample = new WLSample(medianValue, medianAbove, base);
		return medianSample;
	}
	
}
