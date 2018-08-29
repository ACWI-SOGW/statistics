package gov.usgs.wma.statistics.app;


import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class SwaggerParameterBuilder implements OperationBuilderPlugin {
	
	private static Logger LOGGER = org.slf4j.LoggerFactory.getLogger(SwaggerParameterBuilder.class);
	

	@Override
	public boolean supports(DocumentationType documentationType) {
		return true;
	}

	@Override
	public void apply(OperationContext context) {
		LOGGER.trace("building custom parameters");
		
		List<Parameter> parameters = new ArrayList<>();
		Optional<TimeSeriesData> timeSeriesData = context.findAnnotation(TimeSeriesData.class);

		if (timeSeriesData.isPresent()) {
			parameters.add(data());
		};

		if (!parameters.isEmpty()) {
			context.operationBuilder().parameters(parameters);
		}
	}

	public static final String StatsService_SERVICE_EXAMPLE="data=2005-06-10T04%3A15%3A00-05%3A00%2C+1.000%0D%0A2006-06-10T04%3A15%3A00-05%3A00%2C+22.000%0D%0A2007-06-10T04%3A15%3A00-05%3A00%2C+1.000%0D%0A2008-06-10T04%3A15%3A00-05%3A00%2C+2.000%0D%0A2009-06-10T04%3A15%3A00-05%3A00%2C+1.000%0D%0A2010-06-10T04%3A15%3A00-05%3A00%2C+20.000%0D%0A2011-06-10T04%3A15%3A00-05%3A00%2C+1.000%0D%0A2012-06-10T04%3A15%3A00-05%3A00%2C+2.000%0D%0A2013-06-10T04%3A15%3A00-05%3A00%2C+1.000%0D%0A2014-06-10T04%3A15%3A00-05%3A00%2C+10.000%0D%0A2015-06-10T04%3A15%3A00-05%3A00%2C+1.000%0D%0A2016-06-10T04%3A15%3A00-05%3A00%2C+43.000%0D%0A2017-06-10T04%3A15%3A00-05%3A00%2C+1.000%0D%0A2018-06-10T04%3A15%3A00-05%3A00%2C+11.000%0D%0A2005-07-10T04%3A15%3A00-05%3A00%2C+1.000%0D%0A2006-07-10T04%3A15%3A00-05%3A00%2C+22.000%0D%0A2007-07-10T04%3A15%3A00-05%3A00%2C+1.000%0D%0A2008-07-10T04%3A15%3A00-05%3A00%2C+2.000%0D%0A2009-07-10T04%3A15%3A00-05%3A00%2C+1.000%0D%0A2010-07-10T04%3A15%3A00-05%3A00%2C+20.000%0D%0A2011-07-10T04%3A15%3A00-05%3A00%2C+1.000%0D%0A2012-07-10T04%3A15%3A00-05%3A00%2C+2.000%0D%0A2013-07-10T04%3A15%3A00-05%3A00%2C+1.000%0D%0A2014-07-10T04%3A15%3A00-05%3A00%2C+10.000%0D%0A2015-07-10T04%3A15%3A00-05%3A00%2C+1.000%0D%0A2016-07-10T04%3A15%3A00-05%3A00%2C+43.000%0D%0A2017-07-10T04%3A15%3A00-05%3A00%2C+1.000%0D%0A2018-06-10T04%3A15%3A00-05%3A00%2C+11.000%0D%0A%09%09%09";
	public static final String StatsService_SERVICE_DATA   ="CSV time series data of the format \"UTC, Value, [P]\" separated by new line in the POST body.";// Example:   " + StatsService_SERVICE_EXAMPLE;

	public static final String StatsService_SERVICE_EXAMPLE2
			="2005-06-10T04:15:00-05:00, 1.000\n"
			+"2006-06-10T04:15:00-05:00, 22.000\n"
			+"2007-06-10T04:15:00-05:00, 1.000\n"
			+"2008-06-10T04:15:00-05:00, 2.000\n"
			+"2009-06-10T04:15:00-05:00, 1.000\n"
			+"2010-06-10T04:15:00-05:00, 20.000\n"
			+"2011-06-10T04:15:00-05:00, 1.000\n"
			+"2012-06-10T04:15:00-05:00, 2.000\n"
			+"2013-06-10T04:15:00-05:00, 1.000\n"
			+"2014-06-10T04:15:00-05:00, 10.000\n"
			+"2015-06-10T04:15:00-05:00, 1.000\n"
			+"2016-06-10T04:15:00-05:00, 43.000\n"
			+"2017-06-10T04:15:00-05:00, 1.000\n"
			+"2018-06-10T04:15:00-05:00, 11.000\n"
			+"2005-07-10T04:15:00-05:00, 1.000\n"
			+"2006-07-10T04:15:00-05:00, 22.000\n"
			+"2007-07-10T04:15:00-05:00, 1.000\n"
			+"2008-07-10T04:15:00-05:00, 2.000\n"
			+"2009-07-10T04:15:00-05:00, 1.000\n"
			+"2010-07-10T04:15:00-05:00, 20.000\n"
			+"2011-07-10T04:15:00-05:00, 1.000\n"
			+"2012-07-10T04:15:00-05:00, 2.000\n"
			+"2013-07-10T04:15:00-05:00, 1.000\n"
			+"2014-07-10T04:15:00-05:00, 10.000\n"
			+"2015-07-10T04:15:00-05:00, 1.000\n"
			+"2016-07-10T04:15:00-05:00, 43.000\n"
			+"2017-07-10T04:15:00-05:00, 1.000\n"
			+"2018-06-10T04:15:00-05:00, 11.000\n";
									
			
			
	public static Parameter data() {
		return new ParameterBuilder()
				.name("data")
				.description(StatsService_SERVICE_DATA)
				.defaultValue(StatsService_SERVICE_EXAMPLE2)
//				.modelRef(new ModelRef("", new ModelRef("string")))
				.modelRef(new ModelRef("JsonCSV"))
				.parameterType("body")
				.required(true)
				.build();
	}
	
	@ApiModel(description="CSV Descr",  value="JsonCSV")
	static class CSV {
		@ApiModelProperty(name="data1", notes="Data notes asdf", example = "CSV Data Example")
		@JsonProperty
		String data;
	}
	
}
