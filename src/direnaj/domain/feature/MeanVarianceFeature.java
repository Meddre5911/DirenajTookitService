package direnaj.domain.feature;

public class MeanVarianceFeature {

	private double mean;
	private double variance;
	private double standard_deviation;
	private double minValue;
	private double maxValue;

	public MeanVarianceFeature(String mean, String variance, String standard_deviation, String minValue,
			String maxValue) {
		this.mean = Double.valueOf(mean);
		this.variance = Double.valueOf(variance);
		this.standard_deviation = Double.valueOf(standard_deviation);
		this.minValue = Double.valueOf(minValue);
		this.maxValue = Double.valueOf(maxValue);
	}

	public double getMean() {
		return mean;
	}

	public void setMean(double mean) {
		this.mean = mean;
	}

	public double getVariance() {
		return variance;
	}

	public void setVariance(double variance) {
		this.variance = variance;
	}

	public double getStandard_deviation() {
		return standard_deviation;
	}

	public void setStandard_deviation(double standard_deviation) {
		this.standard_deviation = standard_deviation;
	}

	public double getMinValue() {
		return minValue;
	}

	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}

	public double getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}

}
