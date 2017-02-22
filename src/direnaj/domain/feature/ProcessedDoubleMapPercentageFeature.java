package direnaj.domain.feature;

import java.util.List;
import java.util.Map;

public class ProcessedDoubleMapPercentageFeature {
	
	private List<String> limits; 
	private Map<Double, Double> rangePercentages;
	
	public ProcessedDoubleMapPercentageFeature(List<String> limits, Map<Double, Double> rangePercentages){
		this.limits = limits;
		this.rangePercentages = rangePercentages;
	}
	
	public List<String> getLimits() {
		return limits;
	}
	public void setLimits(List<String> limits) {
		this.limits = limits;
	}
	public Map<Double, Double> getRangePercentages() {
		return rangePercentages;
	}
	public void setRangePercentages(Map<Double, Double> rangePercentages) {
		this.rangePercentages = rangePercentages;
	}

}
