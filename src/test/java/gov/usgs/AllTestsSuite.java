package gov.usgs;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import gov.usgs.ngwmn.logic.WaterLevelStatisticsTest;
import gov.usgs.wma.statistics.control.StatsServiceTest;
import gov.usgs.wma.statistics.logic.MonthlyStatisticsTest;
import gov.usgs.wma.statistics.logic.OverallStatisticsTest;
import gov.usgs.wma.statistics.logic.SigFigMathUtilTest;
import gov.usgs.wma.statistics.logic.StatisticsCalculatorTest;
import gov.usgs.wma.statistics.model.JsonDataBuilderTest;

@RunWith(Suite.class)

@Suite.SuiteClasses({
	   SigFigMathUtilTest.class,
	   StatisticsCalculatorTest.class,
	   OverallStatisticsTest.class,
	   MonthlyStatisticsTest.class,
	   WaterLevelStatisticsTest.class,
	   JsonDataBuilderTest.class,
	   StatsServiceTest.class
	})

public class AllTestsSuite {}
