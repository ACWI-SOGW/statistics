package gov.usgs.wma.statistics.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
//import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class JsonData {
	@JsonProperty
	JsonOverall overall;
	@JsonProperty
	Map<String, JsonMonthly> monthly = new HashMap<>();
	@JsonProperty
	String medians;
	
	public JsonOverall getOverall() {
		return overall;
	}
	public Map<String, JsonMonthly> getMonthly() {
		return monthly;
	}
	public String getMedians() {
		return medians;
	}
}
