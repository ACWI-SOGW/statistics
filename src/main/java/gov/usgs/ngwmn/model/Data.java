package gov.usgs.ngwmn.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class Data {
	@ApiModelProperty(example="csv example")
	@JsonProperty
	String csv;
}
