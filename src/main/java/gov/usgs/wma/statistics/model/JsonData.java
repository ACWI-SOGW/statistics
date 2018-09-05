package gov.usgs.wma.statistics.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
	@JsonProperty
	List<String> errors = new LinkedList<>();
	
	public JsonOverall getOverall() {
		return overall;
	}
	public Map<String, JsonMonthly> getMonthly() {
		return monthly;
	}
	public boolean hasMonthly() {
		return ! monthly.isEmpty();
	}
	public String getMedians() {
		return medians;
	}
	public void addError(String msg) {
		errors.add(msg);
	}
	public void addAllErrors(List<String> msgs) {
		errors.addAll(msgs);
	}
	public boolean isOk() {
		return errors.isEmpty();
	}
	public boolean hasErrors() {
		return ! isOk();
	}
}
