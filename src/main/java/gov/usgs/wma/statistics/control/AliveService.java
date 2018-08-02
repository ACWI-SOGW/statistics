package gov.usgs.wma.statistics.control;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AliveService {

    @RequestMapping("/")
    public String index() {
        return "Service is running!!\n";
    }
    @RequestMapping("/statistics")
    public String base() {
        return index();
    }

}
