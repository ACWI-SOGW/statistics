package gov.usgs.ngwmn.control;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class VersionService {

    @RequestMapping("/statistics/version")
    public String version() {
        return "/version.html";
    }

}
