package gov.usgs.ngwmn.control;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.usgs.ngwmn.logic.WaterLevelStatistics;
import gov.usgs.ngwmn.model.Specifier;
import gov.usgs.ngwmn.model.WLSample;
import gov.usgs.ngwmn.model.WellDataType;
import gov.usgs.ngwmn.model.WellRegistry;

@RestController
@CrossOrigin(origins = "*") // no credentials by default
public class StatsService {
	private static Logger LOGGER = org.slf4j.LoggerFactory.getLogger(StatsService.class);

	private static final ResponseEntity<String> _404_ = new ResponseEntity<String>(HttpStatus.NOT_FOUND);
	
	/**
	 * Example call: curl -X GET --header 'Accept: application/json' "http://localhost:8766/qualifiers/lookup/q"
	 * Example call: curl -X GET --header 'Accept: application/json' "http://localhost:8766/qualifiers/lookup/Eqp"
	 * Example call: curl -X GET --header 'Accept: application/json' "http://localhost:8766/qualifiers/lookup/Equipment"
	 * 
	 * @param agencyCd
	 * @param siteNo
	 * @return The JSON containing the statistics calculations.<br>
	 * 	This is an example output of a sample site:<br>
	 */
	@RequestMapping(value = "/statistics/calculate/agencyCd/siteNo",
			method   = RequestMethod.GET,
			produces = "application/json; charset=utf-8")
	public ResponseEntity<String> service(@PathVariable("qualifierCode") String agencyCd, @PathVariable("qualifierCode") String siteNo) {
		
		if ( StringUtils.isBlank(agencyCd) || StringUtils.isBlank(siteNo) ) {
			return _404_;
		}
		
		String json = "placeholder";
//		json = new JsonResponseBuilder().build(codes);
		
		
		return ResponseEntity.ok(json);
	}
	
	@PostMapping(value = "/statistics/calculate",
			produces = "application/json; charset=utf-8",
			consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
			)
	public ResponseEntity<String> service(@RequestParam Map<String, String> body) {
		
		try {
			List<WLSample> samples = parseData(body);
			
			WellRegistry site = new WellRegistry("USGS","12340000");
			Specifier spec = new Specifier(site, WellDataType.WATERLEVEL);
			
			String json = new WaterLevelStatistics().calculate(spec,samples);
			
			return ResponseEntity.ok(json);
		} catch (Exception e) {
			return ResponseEntity.ok("{'status':400,'message':'"+e.getMessage()+"'");
		}
	}
	
	
	public List<WLSample> parseData(Map<String, String> body) {
		String payload = body.get("data");
		
		String[] rows = payload.split("\r?\n");
		List<WLSample> samples = new ArrayList<>(rows.length);
		
		for (String row : rows) {
			row = row.trim();
			if ( 0 == row.length() ) {
				continue;
			}
			
			String[] cols = row.split(",");
			if (cols.length != 2) {
				throw new RuntimeException("All rows must have two values: date,value. ");
			}
			
			String time = cols[0].trim();
			if ( time.length() < 10 ) {
				throw new RuntimeException("The date must be valid (yyyy-mm-dd). " + time);
			}
			
			try {
				BigDecimal value = new BigDecimal(cols[1].trim());
				
				WLSample sample = new WLSample(time, value, "ft", value, "", true, "", value);
				samples.add(sample);
			} catch (NumberFormatException e) {
				throw new RuntimeException("The water value must be valid. " + cols[1]);
			}
		}
		
		return samples;
	}


}