package gov.usgs.wma.statistics.control;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import io.swagger.annotations.ApiOperation;

@Controller
public class VersionService {

	@ApiOperation(
			value = "Version Service - via root path",
			notes = "${VersionService.version.notes}")
    @GetMapping("/version")
    public String version() {
        return "/version.html";
    }

	@ApiOperation(
			value = "Version Service - via application path",
			notes = "${VersionService.appPath.notes}")
	@GetMapping("/statistics/version")
    public String appPath() {
        return version();
    }

}
