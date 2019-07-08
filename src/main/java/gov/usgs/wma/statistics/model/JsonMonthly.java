package gov.usgs.wma.statistics.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;

import io.swagger.annotations.ApiModel;

@ApiModel
public class JsonMonthly extends JsonStats {

	/**
	 * The key is the percentile of interest and the value is the measured value at that
	 * percentile. However, in the case where the percentile falls between two values,
	 * it is then the fraction of the distance between two values for the given percentile
	 * and the sample count.
	 * 
	 * P50_MAX, P50_MIN, and any other percentile keyed as P## as in 50th percentile is P50.
	 * The values are strings to preserve and convey accurate significant figures. 
	 * Floating point notation is inaccurate due to the binary nature and does not properly
	 * preserve or signify significant trailing figures. For example, 0.100 in float might
	 * be 0.1, 0.10000000 for float, or 0.100000000000000 for double. And to make matter worse,
	 * 0.1 is a repeating decimal in binary. 0.0001100110011...  Thus for a double representation
	 * the result will likely be 0.10000000000000001 further confusing the accuracy of the 
	 * measured value.
	 */
	@JsonProperty("PERCENTILES")
	public final Map<String, String> percentiles;

	public JsonMonthly(String recordYears, int sampleCount, Map<String, String> percentiles) {
		super(recordYears, sampleCount);
		this.percentiles = ImmutableMap.copyOf(percentiles);
	}
}
