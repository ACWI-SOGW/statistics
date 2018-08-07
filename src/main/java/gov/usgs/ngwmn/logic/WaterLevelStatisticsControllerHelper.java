package gov.usgs.ngwmn.logic;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import gov.usgs.ngwmn.logic.WaterLevelStatistics.MediationType;
import gov.usgs.ngwmn.model.WLSample;
import gov.usgs.ngwmn.model.Specifier;

/**
 * This class adds a method used by the statistics controller to get values used in the 
 * calculation of statistics. It is nice to have the methods this helper calls with limited
 * access to self document the entry methods.
 * 
 * @author duselman
 */
public class WaterLevelStatisticsControllerHelper {

	public List<WLSample> processSamplesUsedToCalculateStats(
			Specifier spec, List<WLSample> samples, String month, boolean median) {
		WaterLevelStatistics stats = new WaterLevelStatistics();
		
		// this section calls all the statistics methods required to condition the data
		MediationType mediation = stats.findMostPrevalentMediation(spec, samples);
		stats.setMediation(mediation);
		
		List<WLSample> sortedByDate = stats.useMostPrevalentPCodeMediatedValue(spec, samples, mediation); 
		stats.removeProvisionalButNotMostRecent(sortedByDate, spec.getAgencyCd()+":"+spec.getSiteNo());
		stats.removeNulls(sortedByDate, spec.getAgencyCd()+":"+spec.getSiteNo());
		List<WLSample> sortedByValue  = new ArrayList<>(sortedByDate);
		stats.monthlyStats.sortValueByQualifier(sortedByValue);
		
		samples = fetchMonthOrAll(sortedByValue, month);
		return sortAndMedian(samples, median, mediation);
	}
	
	
	protected List<WLSample> fetchMonthOrAll(List<WLSample> sortedByValue, String month) {
		WaterLevelStatistics stats = new WaterLevelStatistics();
		// filter out the possible selected month
		if (isValidMonth(month)) {
			return stats.monthlyStats.filterValuesByGivenMonth(sortedByValue, month);
		}
		return sortedByValue;
	}
	

	protected boolean isValidMonth(String month) {
		try {
			// only accept numbers that correspond to months
			int monthNumber = Integer.parseInt(month);
			return month.length()<=2 && monthNumber >= 1 && monthNumber <= 12;
		} catch (Exception e) {
			// on bad number or no number just give them every month
			return false;
		}
	}
	
	
	protected List<WLSample> sortAndMedian(List<WLSample> sortedByValue, boolean median, MediationType mediation )  {
		WaterLevelStatistics stats = new WaterLevelStatistics();
		// we want the values grouped by month
		List<WLSample> monthlyGroupedSamplesOrderedByValue = new LinkedList<>();
		for (int m=1; m<=12; m++) {
			// the normalizer does not check the month -- only the year
			List<WLSample> monthlySamples = stats.monthlyStats.filterValuesByGivenMonth(sortedByValue, ""+m);
			if (monthlySamples.size() > 1) {
				stats.removeMostRecentProvisional(monthlySamples, sortedByValue);
				// if medians is wanted then process all months (could be one) for median values
				if (median) {
					monthlyGroupedSamplesOrderedByValue.addAll(
							stats.normalizeMutlipleYearlyValues(monthlySamples, 
									stats.monthlyStats.sortFunctionByQualifier()));
				} else {
					monthlyGroupedSamplesOrderedByValue.addAll( monthlySamples );
				}
			}
		}
		return monthlyGroupedSamplesOrderedByValue;
	}

}