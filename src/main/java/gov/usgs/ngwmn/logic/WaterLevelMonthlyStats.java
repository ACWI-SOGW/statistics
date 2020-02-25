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
import gov.usgs.wma.statistics.model.Value;

public class WaterLevelMonthlyStats extends MonthlyStatistics<WLSample> {
	public WaterLevelMonthlyStats(Properties env, JsonDataBuilder builder) {
		super(env, builder);
	}
	@Override
	public void sortValueByQualifier(List<WLSample> samples) {
		if (MediationType.BelowLand .equalSortOrder(builder.mediation())) {
			sortByValueOrderDescending(samples);
		} else {
			sortByValueOrderAscending(samples);
		}
	}
	@Override
	public Function<List<WLSample>, List<WLSample>> sortFunctionByQualifier() {
		Function<List<WLSample>, List<WLSample>> sortBy = StatisticsCalculator::sortByValueOrderAscending;
		
		if (MediationType.BelowLand .equalSortOrder(builder.mediation())) {
			sortBy = StatisticsCalculator::sortByValueOrderDescending;
		}
		return sortBy;
	}

}
