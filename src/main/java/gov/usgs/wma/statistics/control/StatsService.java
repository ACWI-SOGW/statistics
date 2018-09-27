package gov.usgs.wma.statistics.control;

import static gov.usgs.wma.statistics.app.Properties.*;
import static gov.usgs.wma.statistics.app.SwaggerConfig.*;
import static org.apache.commons.lang.StringUtils.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.usgs.ngwmn.logic.WaterLevelStatistics;
import gov.usgs.ngwmn.model.MediationType;
import gov.usgs.ngwmn.model.Specifier;
import gov.usgs.ngwmn.model.WLSample;
import gov.usgs.wma.statistics.app.Properties;
import gov.usgs.wma.statistics.model.JsonData;
import gov.usgs.wma.statistics.model.JsonDataBuilder;
import gov.usgs.wma.statistics.model.Value;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("/statistics")
@CrossOrigin(origins = "*") // no credentials by default
//@Api("StatisticsService")
public class StatsService {
	private static Logger LOGGER = org.slf4j.LoggerFactory.getLogger(StatsService.class);

	private static final ResponseEntity<String> _404_ = new ResponseEntity<String>(HttpStatus.NOT_FOUND);

	private static final String INCLUDE_MEDIANS = "true";
	
	@Autowired
	Properties env;
	
	@ApiOperation(
			value = "Calculate Statistics Service",
			notes = StatsService_CALCULATE_NOTES
		)
	@PostMapping(value = "/calculate",
			produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
			consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
		)
	public JsonData calculate(
			@ApiParam(
					value  = StatsService_CALCULATE_DATA,
					example= StatsService_EXAMPLE_RAW,
					required = true
				)
			@RequestParam
			String data,
			@ApiParam(
					name="mediation",
					value=StatsService_CALCULATE_MEDIATION,
					defaultValue=StatsService_MEDIATION_DEFAULT,
					allowableValues=StatsService_MEDIATION_VALUES,
					allowEmptyValue=true
					)
			@RequestParam(
					name="mediation",
					defaultValue="BelowLand"
					)
			String mediation,
			@ApiParam(
					value=StatsService_CALCULATE_MEDIANS,
					defaultValue=StatsService_MEDIANS_DEFAULT,
					allowableValues=BOOLEAN_VALUES,
					allowEmptyValue=true
					)
			@RequestParam(defaultValue="false")
			String medians,
			@ApiParam(
					value=StatsService_CALCULATE_PERCENTILES,
					defaultValue=StatsService_PERCENTILES_DEFAULT,
					allowEmptyValue=true
					)
			@RequestParam(defaultValue=StatsService_PERCENTILES_DEFAULT)
			String percentiles) {
		
		try {
			LOGGER.trace("entered");
			JsonDataBuilder builder = new JsonDataBuilder(env);
			
			// parse the CSV data
			List<WLSample> samples = parseData(data, builder);
			
			try {
				// parse the mediation string and setup the builder
				MediationType mediationType = MediationType.valueOf(mediation);
				builder.mediation(mediationType);
			} catch (Exception e) {
				String validMediations = MediationType.validMediations();
				String msg = env.getError(ENV_INVALID_MEDIATION, mediation, validMediations);
				builder.error(msg);
			}
			// parse medians param
			if (isNotBlank(medians)) {
				if ( ! (BOOLEAN_FALSE.equalsIgnoreCase(medians) || BOOLEAN_TRUE.equalsIgnoreCase(medians)) ) {
					String msg = env.getError(ENV_INVALID_MEDIANS, medians);
					builder.error(msg);
				}
			}
			builder.includeIntermediateValues(INCLUDE_MEDIANS.equals(medians));
			// parse custom percentiles
			if ( ! StatsService_PERCENTILES_DEFAULT.equals(percentiles) ) {
				builder.percentiles(percentiles.split(","));
				builder.buildPercentiles();
			}

			// A random identifier for the service unless we parameterize the date set ID.
			Specifier spec = new Specifier();

			// if there are parameter issues, then do not process statistics
			if ( builder.hasErrors() ) {
				JsonData json = builder.build();
				return json;
			}

			JsonData json = new WaterLevelStatistics(env, builder).calculate(spec, samples);

			LOGGER.trace("exited: good");
			return json;
		} catch (Exception e) {
			LOGGER.trace("exited: b");
			return null;
		}
	}
	

	// TODO should this be here, in a csv util class/lib, or someplace else.
	public List<WLSample> parseData(String data, JsonDataBuilder builder){
		String[] rows = data.split("\r?\n");
		return parseData(rows, builder);
	}
	public List<WLSample> parseData(String[] data, JsonDataBuilder builder) {
		
		List<WLSample> samples = new ArrayList<>(data.length);
		
		String msg = "";
		
		for (int r=0; r<data.length; r++) {
			if (isNotBlank(msg)) {
				LOGGER.trace(msg);
				msg = "";
			}
			String row = data[r].trim();
			if ( 0 == row.length() || row.charAt(0) == '#') {
				continue; // skip empty and comment rows
			}
			String[] cols;
			try {
				cols = row.split(",");
				if (cols.length < 2 || cols.length > 3) {
					msg = env.getError(ENV_INVALID_ROW_COLS, r, row);
					builder.error(msg);
					continue;
				}
			} catch (Exception e) {
				msg = env.getError(ENV_INVALID_ROW_FORMAT, r, row);
				builder.error(msg);
				continue;
			}
			
			String time = cols[0].trim();
			
			try {
				BigDecimal value = new BigDecimal(cols[1].trim());
				
				WLSample sample = new WLSample(time, value, "ft", value, "", true, "", value);
				
				if (cols.length == 3) {
					String code = ""+cols[2].charAt(0);
					if (Value.PROVISIONAL_CODE.equalsIgnoreCase(code)) {
						// approved is default
						sample.setProvsional(true);
					} else if ( ! Value.APPROVED_CODE.equalsIgnoreCase(code) ) {
						msg = env.getError(ENV_INVALID_ROW_AGING, r, row);
						builder.error(msg);
					}
				}
				samples.add(sample);
			} catch (NumberFormatException e) {
				msg = env.getError(ENV_INVALID_ROW_VALUE, r, row);
				builder.error(msg);
			} catch (Exception e) {
				msg = env.getError(ENV_INVALID_ROW_OTHER, r, row);
				builder.error(msg);
			}
		}
		
		return samples;
	}

}