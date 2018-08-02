package gov.usgs.wma.statistics.control;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class InputService {

    @RequestMapping("/statistics/input")
    public String version() {
        return "/index.html";
    }

}
