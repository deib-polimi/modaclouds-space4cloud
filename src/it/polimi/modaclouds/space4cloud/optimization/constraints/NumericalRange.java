/**
 * 
 */
package it.polimi.modaclouds.space4cloud.optimization.constraints;

/**
 * @author MODAClouds
 *
 */
public class NumericalRange implements IRange {

	
	
	private Double maxValue = Double.POSITIVE_INFINITY;
	private Double minValue = Double.MIN_VALUE;
	
	/* (non-Javadoc)
	 * @see it.polimi.modaclouds.space4cloud.optimization.constraints.IRange#validate(java.lang.Object)
	 */
	@Override
	public boolean validate(Object value) throws ClassCastException {
		
			double v = (Double) value;
			if (v >= minValue && v <= maxValue) {
				return true;
			}	
			return false;
		
	}

	/**
	 * @return the maxValue
	 */
	public Double getMaxValue() {
		return maxValue;
	}

	/**
	 * @param maxValue the maxValue to set
	 */
	public void setMaxValue(Double maxValue) {
		this.maxValue = maxValue;
	}

	/**
	 * @return the minValue
	 */
	public Double getMinValue() {
		return minValue;
	}

	/**
	 * @param minValue the minValue to set
	 */
	public void setMinValue(Double minValue) {
		this.minValue = minValue;
	}
	
	public boolean hasMax() {
		return maxValue!=Double.MAX_VALUE;		
	}
	
	public boolean hasMin() {
		return minValue != Double.MIN_VALUE;
	}
	
	
	public double checkDistance(Double measurement) {		
		if(hasMax())
			return measurement - getMaxValue();
		else
			return getMinValue() - measurement;
	}
	
	public double checkDistanceWeighted(){
		//Return a function of the distance and the expected value. 
		return 0;
	}
}
