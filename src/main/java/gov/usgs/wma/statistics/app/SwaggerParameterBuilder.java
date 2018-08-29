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

			
	public static Parameter data() {
		return new ParameterBuilder()
				.name("data")
				.description(SwaggerConfig.StatsService_CALCULATE_NOTES)
				.defaultValue(SwaggerConfig.StatsService_EXAMPLE_RAW)
//				.modelRef(new ModelRef("", new ModelRef("string")))
//				.modelRef(new ModelRef("JsonCSV"))
				.parameterType("string")
				.required(true)
				.build();
	}
	
//	@ApiModel(description="CSV Descr",  value="JsonCSV")
//	static class CSV {
//		@ApiModelProperty(name="data1", notes="Data notes asdf", example = "CSV Data Example")
//		@JsonProperty
//		String data;
//	}
	
}
