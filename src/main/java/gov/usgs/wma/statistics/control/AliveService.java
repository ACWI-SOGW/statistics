package gov.usgs.wma.statistics.control;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

@RestController
public class AliveService {

	@ApiOperation(
			value = "Alive Service - via root URL",
			notes = "${AliveService.alive.notes}")
	@GetMapping("/")
    public String alive() {
        return "Service is running!!\n";
    }
	
	@ApiOperation(
			value = "Alive Service - via application root URL",
			notes = "${AliveService.appRoot.notes}")
    @GetMapping("/statistics")
    public String appRoot() {
        return alive();
    }

}
