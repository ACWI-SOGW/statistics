package gov.usgs.wma.statistics.control;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import gov.usgs.wma.statistics.app.SwaggerConfig;
import io.swagger.annotations.ApiOperation;

@Controller
public class InputService {

	@ApiOperation(
			value = "Input Form Service",
			notes = SwaggerConfig.InputService_FORM_NOTES)
    @GetMapping("/statistics/input")
    public String form() {
        return "/index.html";
    }

}
