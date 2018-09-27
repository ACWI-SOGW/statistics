package gov.usgs;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import gov.usgs.ngwmn.logic.WaterLevelStatisticsTest;
import gov.usgs.wma.statistics.control.StatsServiceTest;
import gov.usgs.wma.statistics.logic.JavaLibTest;
import gov.usgs.wma.statistics.logic.MonthlyStatisticsTest;
import gov.usgs.wma.statistics.logic.OverallStatisticsTest;
import gov.usgs.wma.statistics.logic.ScientificDecimalTest;
import gov.usgs.wma.statistics.logic.SigFigMathUtilTest;
import gov.usgs.wma.statistics.logic.StatisticsCalculatorTest;
import gov.usgs.wma.statistics.logic.ValdiationMessagesTest;
import gov.usgs.wma.statistics.model.JsonDataBuilderTest;
import gov.usgs.wma.statistics.model.ValueTest;

@RunWith(Suite.class)

@Suite.SuiteClasses({
	   WaterLevelStatisticsTest.class,
	   StatsServiceTest.class,
	   JavaLibTest.class,
	   MonthlyStatisticsTest.class,
	   OverallStatisticsTest.class,
	   ScientificDecimalTest.class,
	   SigFigMathUtilTest.class,
	   StatisticsCalculatorTest.class,
	   ValdiationMessagesTest.class,
	   JsonDataBuilderTest.class,
	   ValueTest.class
	})

public class AllTestsSuite {}
