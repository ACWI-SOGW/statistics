package gov.usgs.wma.statistics.control;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AliveService {

    @GetMapping("/")
    public String index() {
        return "Service is running!!\n";
    }
    @GetMapping("/statistics")
    public String base() {
        return index();
    }

}
