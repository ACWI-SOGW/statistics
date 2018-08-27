package gov.usgs.wma.statistics.control;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import gov.usgs.wma.statistics.app.SwaggerConfig;
import io.swagger.annotations.ApiOperation;

@RestController
public class AliveService {

	@ApiOperation(
			value = "Alive Service - via root URL",
			notes = SwaggerConfig.AliveService_ALIVE_NOTES)
	@GetMapping("/")
    public String alive() {
        return "Service is running!!\n";
    }
	
	@ApiOperation(
			value = "Alive Service - via application root URL",
			notes = SwaggerConfig.AliveService_APPROOT_NOTES)
    @GetMapping("/statistics")
    public String appRoot() {
        return alive();
    }

}
