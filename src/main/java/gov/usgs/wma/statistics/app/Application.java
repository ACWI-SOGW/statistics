package gov.usgs.wma.statistics.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
@SpringBootApplication(scanBasePackages = "gov.usgs.wma.statistics")
public class Application extends SpringBootServletInitializer {
	
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
//	@Bean
//	public Properties instance() {
//		return new Properties();
//	}
	
}
