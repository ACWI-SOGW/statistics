package gov.usgs.wma.statistics.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

	public static final String AliveService_ALIVE_NOTES    ="Informs that the service is running from the root path.";
	public static final String AliveService_APPROOT_NOTES  ="Responds in the same way as alive only from the application path.";
		
	public static final String InputService_FORM_NOTES     ="Returns an interactive HTML Form page.";
	
	public static final String StatsService_SERVICE_NOTES  ="Returns the overal and montly statistics for the POST CSV data. The overal statistics include MIN, MAX, and Median values, MIN and MAX dates, Period of Record in years, Sample Count, Latest Percential. The monthly statistics are assigned using the month number and inlcude the 10th, 25th, 50th, 75th, and 90th percentials, the MIN and MAX 50th percetial, count of years with data.";
	public static final String StatsService_SERVICE_DATA   ="CSV time series data of the format \"UTC, Value, [P]\" separated by new line in the POST body.";
	public static final String StatsService_SERVICE_EXAMPLE="1, 10, P\\n2,11\\n,3, 15";
	public static final String StatsService_MEDIANS_NOTES  ="Like the calculate endpoint, this returns statistics. This also returns the median montly data used in the statistics calculations. In order to weight each month equally, if there is more than one sample supplied in a give month/year then the median of that data will be used. For example, if there are three values for 2000-10: 10, 3, and 1, then 3 will be used for Oct 2000 statistics.";
	
	public static final String VersionService_VERSION_NOTES="Returns information about the application version and release.";
	public static final String VersionService_APPPATH_NOTES="Responds in the same way as version only from the application path.";
	
	
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
			.apis(RequestHandlerSelectors.basePackage("gov.usgs.wma.statistics.control"))
			.build();
		return docket;
	}
	
}
