package gov.usgs.ngwmn.logic;

import static gov.usgs.wma.statistics.app.Properties.*;

import java.util.List;
import java.util.function.Function;

import gov.usgs.ngwmn.model.MediationType;
import gov.usgs.ngwmn.model.WLSample;
import gov.usgs.wma.statistics.app.Properties;
import gov.usgs.wma.statistics.logic.MonthlyStatistics;
import gov.usgs.wma.statistics.logic.StatisticsCalculator;
import gov.usgs.wma.statistics.model.JsonDataBuilder;

public class WaterLevelMonthlyStats extends MonthlyStatistics<WLSample> {
	public WaterLevelMonthlyStats(Properties env, JsonDataBuilder builder) {
		super(env, builder);
	}
	@Override
	public void sortValueByQualifier(List<WLSample> samples) {
		if (builder.mediation() == MediationType.BelowLand || builder.mediation() == MediationType.DESCENDING) {
			sortByValueOrderDescending(samples);
		} else {
			sortByValueOrderAscending(samples);
		}
	}
	@Override
	public Function<List<WLSample>, List<WLSample>> sortFunctionByQualifier() {
		Function<List<WLSample>, List<WLSample>> sortBy = StatisticsCalculator::sortByValueOrderAscending;
		
		if (builder.mediation() == MediationType.BelowLand || builder.mediation() == MediationType.DESCENDING) {
			sortBy = StatisticsCalculator::sortByValueOrderDescending;
		}
		return sortBy;
	}
	@Override
	public boolean doesThisMonthQualifyForStats(List<WLSample> monthSamples) {
		int monthYears = uniqueYears(monthSamples);
		boolean qualified = super.doesThisMonthQualifyForStats(monthSamples)
				&& monthYears >= 10;

		if ( ! qualified && monthYears>0 ) {
			WLSample firstSample = monthSamples.get(0);
			int missingCount = 10 - monthYears;
			String plural = missingCount>1 ? "s" :"";
			String monthName = sampleMonthName(firstSample);
			String msg = env.getMessage(ENV_MESSAGE_MONTHLY_DETAIL, monthName, missingCount, plural);
			builder.message(msg);
		}
		return qualified;
	}
	
}