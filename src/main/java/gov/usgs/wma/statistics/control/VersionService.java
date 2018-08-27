package gov.usgs.wma.statistics.control;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import gov.usgs.wma.statistics.app.SwaggerConfig;
import io.swagger.annotations.ApiOperation;

@Controller
public class VersionService {

	@ApiOperation(
			value = "Version Service - via root path",
			notes = SwaggerConfig.VersionService_VERSION_NOTES)
    @GetMapping("/version")
    public String version() {
        return "/version.html";
    }

	@ApiOperation(
			value = "Version Service - via application path",
			notes = SwaggerConfig.VersionService_APPPATH_NOTES)
	@GetMapping("/statistics/version")
    public String appPath() {
        return version();
    }

}
