package direnaj.domain.feature;

import java.util.List;
import java.util.Map;

public class ProcessedPercentageFeature {
	
	private List<String> limits; 
	private Map<String, Double> rangePercentages;
	
	public ProcessedPercentageFeature(List<String> limits, Map<String, Double> rangePercentages){
		this.limits = limits;
		this.rangePercentages = rangePercentages;
	}
	
	public List<String> getLimits() {
		return limits;
	}
	public void setLimits(List<String> limits) {
		this.limits = limits;
	}
	public Map<String, Double> getRangePercentages() {
		return rangePercentages;
	}
	public void setRangePercentages(Map<String, Double> rangePercentages) {
		this.rangePercentages = rangePercentages;
	}

}
