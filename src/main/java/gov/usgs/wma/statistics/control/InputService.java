package gov.usgs.wma.statistics.control;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InputService {

    @GetMapping("/statistics/input")
    public String version() {
        return "/index.html";
    }

}
