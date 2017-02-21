package direnaj.domain;

public class ExtractedFeature {
	
	private String featureKey;
	private String featureValue;
	
	
	public ExtractedFeature(String featureKey, String featureValue){
		this.featureKey = featureKey;
		this.featureValue = featureValue;
	}
	
	public String getFeatureKey() {
		return featureKey;
	}
	public void setFeatureKey(String featureKey) {
		this.featureKey = featureKey;
	}
	public String getFeatureValue() {
		return featureValue;
	}
	public void setFeatureValue(String featureValue) {
		this.featureValue = featureValue;
	}

}
