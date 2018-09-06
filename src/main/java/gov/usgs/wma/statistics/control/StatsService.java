package gov.usgs.wma.statistics.control;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.usgs.ngwmn.logic.WaterLevelStatistics;
import gov.usgs.ngwmn.logic.WaterLevelStatistics.MediationType;
import gov.usgs.ngwmn.model.Specifier;
import gov.usgs.ngwmn.model.WLSample;
import gov.usgs.wma.statistics.app.SwaggerConfig;
import gov.usgs.wma.statistics.model.JsonData;
import gov.usgs.wma.statistics.model.JsonDataBuilder;
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
	
	
	@ApiOperation(
			value = "Calculate Statistics Service",
			notes = SwaggerConfig.StatsService_CALCULATE_NOTES
		)
	@PostMapping(value = "/calculate",
			produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
			consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
		)
	public JsonData calculate(
			@ApiParam(
					value  = SwaggerConfig.StatsService_CALCULATE_DATA,
					example= SwaggerConfig.StatsService_EXAMPLE_RAW,
					required = true
				)
			@RequestParam
			String data,
			@ApiParam(
					name="mediation",
					value=SwaggerConfig.StatsService_CALCULATE_MEDIATION,
					defaultValue=SwaggerConfig.StatsService_MEDIATION_DEFAULT,
					allowableValues=SwaggerConfig.StatsService_MEDIATION_VALUES,
					allowEmptyValue=true
					)
			@RequestParam(
					name="mediation",
					defaultValue="BelowLand"
					)
			String mediation,
			@ApiParam(
					value=SwaggerConfig.StatsService_CALCULATE_MEDIANS,
					defaultValue=SwaggerConfig.StatsService_MEDIANS_DEFAULT,
					allowableValues=SwaggerConfig.BOOLEAN_VALUES,
					allowEmptyValue=true
					)
			@RequestParam(defaultValue="false")
			String medians,
			@ApiParam(
					value=SwaggerConfig.StatsService_CALCULATE_PERCENTILES,
					defaultValue=SwaggerConfig.StatsService_PERCENTILES_DEFAULT,
					allowEmptyValue=true
					)
			@RequestParam(defaultValue=SwaggerConfig.StatsService_PERCENTILES_DEFAULT)
			String percentiles) {
		
		try {
			LOGGER.trace("entered");
			
			// parse the CSV data
			List<WLSample> samples = parseData(data);
			
			// parse the mediation string and setup the builder
			MediationType mediationType = MediationType.valueOf(mediation);
			JsonDataBuilder builder = new JsonDataBuilder();
			builder.mediation(mediationType)
				.includeIntermediateValues(INCLUDE_MEDIANS.equals(medians)); // parse medians param
			if ( ! SwaggerConfig.StatsService_PERCENTILES_DEFAULT.equals(percentiles) ) {
				builder.percentiles(percentiles.split(",")); // parse custom percentiles
			}
			
			// A random identifier for the service unless we parameterize the date set ID.
			Specifier spec = new Specifier();
			
			JsonData json = new WaterLevelStatistics(builder).calculate(spec, samples);
			
			LOGGER.trace("exited: good");
			return json;
		} catch (Exception e) {
			LOGGER.trace("exited: b");
			return null;
		}
	}
	

	// TODO should this be here, in a csv util class/lib, or someplace else.
	public List<WLSample> parseData(String data){
		String[] rows = data.split("\r?\n");
		return parseData(rows);
	}
	public List<WLSample> parseData(String[] data) {
		
		List<WLSample> samples = new ArrayList<>(data.length);
		
		for (String row : data) {
			row = row.trim();
			if ( 0 == row.length() || row.charAt(0) == '#') {
				continue; // skip empty and comment rows
			}
			
			String[] cols = row.split(",");
			if (cols.length < 2 || cols.length > 3) {
				throw new RuntimeException("All rows must have two values and optional provisional code: date,value,P. ");
			}
			
			String time = cols[0].trim();
			if ( time.length() < 10 ) {
				throw new RuntimeException("The date must be valid (yyyy-mm-dd). " + time);
			}
			
			try {
				BigDecimal value = new BigDecimal(cols[1].trim());
				
				WLSample sample = new WLSample(time, value, "ft", value, "", true, "", value);
				
				if (cols.length == 3) {
					sample.setProvsional(true);
				}
				samples.add(sample);
			} catch (NumberFormatException e) {
				throw new RuntimeException("The water value must be valid. " + cols[1]);
			}
		}
		
		return samples;
	}

}