package gov.usgs.wma.statistics.control;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VersionService {

    @GetMapping("/statistics/version")
    public String version() {
        return "/version.html";
    }

}
