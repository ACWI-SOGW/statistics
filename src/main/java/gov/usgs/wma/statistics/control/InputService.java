package gov.usgs.wma.statistics.control;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import io.swagger.annotations.ApiOperation;

@Controller
public class InputService {

	@ApiOperation(
			value = "Input Form Service",
			notes = "${InputService.form.notes}")
    @GetMapping("/statistics/input")
    public String form() {
        return "/index.html";
    }

}
