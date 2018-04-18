package gov.usgs.ngwmn.control;

//import java.io.IOException;
//import java.io.Writer;
//import java.sql.ResultSet;
//import java.sql.SQLException;
import java.util.List;

//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.springframework.dao.DataAccessException;
//import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import gov.usgs.ngwmn.model.WellDataType;
import gov.usgs.ngwmn.data.WellRegistryDAO;
import gov.usgs.ngwmn.data.WaterLevelDAO;
import gov.usgs.ngwmn.logic.WaterLevelStatisticsControllerHelper;
import gov.usgs.ngwmn.model.WLSample;
import gov.usgs.ngwmn.model.Specifier;

//import com.google.visualization.datasource.DataSourceHelper;
//import au.com.bytecode.opencsv.CSVWriter;
//import gov.usgs.ngwmn.dm.dao.FetchStatsDAO;
//import gov.usgs.ngwmn.dm.visualization.FetchDataAgeGenerator;
//import gov.usgs.ngwmn.dm.visualization.FetchStatsAgencyGenerator;
//import gov.usgs.ngwmn.dm.visualization.StatsTableGenerator;

//@Controller
//@RequestMapping("/waterlevel")
public class WaterlevelStatsController {

//	private FetchStatsDAO dao;
	private WellRegistryDAO wellDao;
	private WaterLevelDAO waterlevelDAO;

	private Logger logger = LoggerFactory.getLogger(getClass());

	// TODO this class is written more than once
	public static class Charter {
		private String agencyCd;

		public String getAgencyCd() {
			return agencyCd;
		}

		public void setAgencyCd(String agencyCd) {
			this.agencyCd = agencyCd;
		}

	}

	// TODO Eliminate this (here only to make form:form tag work)
	@ModelAttribute("charter")
	public Charter getModel() {
		return new Charter();
	}

	@RequestMapping("chart")
	public String showChart(
			@ModelAttribute("agency") String agencyCd
			) {
		return "waterlevel/chart";
	}

	@RequestMapping("timechart")
	public String showTimeChart(
			) {
		return "waterlevel/timechart";
	}

	@ModelAttribute("agencyCodes")
	public List<String> getAgencyCodes() {
		logger.info("getting agency codes");
		return wellDao.agencies();
	}

//	@RequestMapping("table")
//	public void generateTable(
//			HttpServletRequest request,
//			HttpServletResponse response)
//					throws IOException {
//		FetchStatsAgencyGenerator gen = new FetchStatsAgencyGenerator(dao);
//		DataSourceHelper.executeDataSourceServletFlow(request, response, gen, false);
//	}

	// TODO Handle table/all, which should produce a separate data series (i.e. column) for each agency
	// (or use "pivot" operation in data query, https://developers.google.com/chart/interactive/docs/querylanguage#Pivot)

//	@RequestMapping("table/{agency}")
//	public void generateTable(
//			@PathVariable("agency") String agencyCd,
//			HttpServletRequest request,
//			HttpServletResponse response)
//					throws IOException {
//		FetchStatsAgencyGenerator gen = new FetchStatsAgencyGenerator(dao);
//		gen.setAgencyCd(agencyCd);
//		DataSourceHelper.executeDataSourceServletFlow(request, response, gen, false);
//	}

//	@RequestMapping("stats")
//	public void statsTable(
//			HttpServletRequest request,
//			HttpServletResponse response
//			)
//					throws IOException
//					{
//		StatsTableGenerator gen = new StatsTableGenerator(dao);
//		DataSourceHelper.executeDataSourceServletFlow(request, response, gen, false);
//					}

	@RequestMapping("fetchdates")
	public String showFetchDates(
			) {
		return "waterlevel/fetchdates";
	}

//	@RequestMapping("age")
//	public void ageTable(
//			HttpServletRequest request,
//			HttpServletResponse response
//			)
//					throws IOException
//					{
//		FetchDataAgeGenerator gen = new FetchDataAgeGenerator(dao);
//		DataSourceHelper.executeDataSourceServletFlow(request, response, gen, false);
//					}

//	@RequestMapping(value="data/{agency}", produces="text/csv")
//	public void exportData(
//			@PathVariable("agency") String agencyCd,
//			final Writer writer
//			) throws SQLException, IOException {
//		
//		ResultSetExtractor<Void> rse = new ResultSetExtractor<Void>() {
//
//			@Override
//			public Void extractData(ResultSet rs) throws SQLException,
//			DataAccessException {
//				@SuppressWarnings("resource") // managed by pipeline
//				CSVWriter cw = new CSVWriter(writer);
//				try {
//					cw.writeAll(rs, true);
//				} catch (IOException e) {
//					throw new RuntimeException(e);
//				}
//
//				return null;
//			}
//		};
//
//		dao.viewData(agencyCd, rse);
//	}

//	@RequestMapping(value="data", produces="text/csv")
//	public void exportAllData(
//			Writer 		writer
//			)
//					throws SQLException, IOException
//					{
//		exportData(null, writer);
//					}

//	public void setDao(FetchStatsDAO dao) {
//		this.dao = dao;
//	}

	public void setWellDao(WellRegistryDAO wellDao) {
		this.wellDao = wellDao;
	}
	public void setWaterLevelDao(WaterLevelDAO dao) {
		this.waterlevelDAO = dao;
	}

	@RequestMapping(value="datafor/{agency}/{site}", produces="text/html;charset=UTF-8")
	public String waterLevelStatsDataValuesUsed(
			@PathVariable("agency") String agencyCd,
			@PathVariable("site") String siteNo,
			@RequestParam(value="month", required=false) String month,
			@RequestParam(value="median",required=false) String median,
			Model model
	) {
		boolean useMedians = StringUtils.isNotBlank(median);
		
		Specifier spec = new Specifier(agencyCd, siteNo, WellDataType.WATERLEVEL);
		List<WLSample> samples = waterlevelDAO.getTimeSeries(spec);
		
		WaterLevelStatisticsControllerHelper stats = new WaterLevelStatisticsControllerHelper();
		samples = stats.processSamplesUsedToCalculateStats(spec, samples, month, useMedians);
		
		model.addAttribute("samples",samples);
		model.addAttribute("agencyCd", agencyCd);
		model.addAttribute("siteNo", siteNo);

		if (StringUtils.isNotBlank(month)) {
			model.addAttribute("month", "month="+month);
		}
		if (useMedians) {
			model.addAttribute("median", "median values presented");
		}
		return "waterlevel/data";
	}
	
	
}
