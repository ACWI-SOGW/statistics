package gov.usgs.wma.statistics.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jdk.nashorn.internal.runtime.regexp.joni.Regex;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static springfox.documentation.builders.PathSelectors.*;

@SpringBootApplication(scanBasePackages = "gov.usgs.wma.statistics.control")
@Configuration
@EnableSwagger2
public class Application extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }
    
	@Bean
	public Docket qwPortalServicesApi() {
		Docket docket = new Docket(DocumentationType.SWAGGER_2)
//			.protocols(new HashSet<>(Arrays.asList("https")))
//			.useDefaultResponseMessages(false)
//			.host(configurationService.getSwaggerDisplayHost())
//			.pathProvider(pathProvider())
//			.additionalModels(typeResolver.resolve(PostParms.class),
//					typeResolver.resolve(OrganizationCountJson.class),
//					typeResolver.resolve(StationCountJson.class),
//					typeResolver.resolve(ActivityCountJson.class),
//					typeResolver.resolve(ActivityMetricCountJson.class),
//					typeResolver.resolve(ResultCountJson.class),
//					typeResolver.resolve(ResDetectQntLmtCountJson.class),
//					typeResolver.resolve(ProjectCountJson.class))
			.select()
			.paths(PathSelectors.any())
//			.paths(regex("/calculate.*"))
			.apis(RequestHandlerSelectors.basePackage("gov.usgs"))
			.build();
		return docket;
	}
}





