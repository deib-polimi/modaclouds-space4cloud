/**
 * 
 */
package it.polimi.modaclouds.space4cloud.optimization.constraints;


/**
 * @author Michele Ciavotta
 *
 */
public abstract class QoSConstraint extends Constraint {

	/**
	 * @param id
	 * @param metric
	 * @param priority
	 * @param unit
	 */
	public QoSConstraint(String id, Metric metric, int priority, Unit unit) {
		super(id, metric, priority, unit);
		this.range = new NumericalRange();		
		// TODO Auto-generated constructor stub
	}
	
	public void setMax(double maxValue){
		((NumericalRange) range).setMaxValue(maxValue);
	}
	
	public void setMin(double minValue){
		((NumericalRange) range).setMinValue(minValue);
	}

	public double getMin(){
		return ((NumericalRange) range).getMinValue();
	}
	

	public double getMax(){
		return ((NumericalRange) range).getMaxValue();
	}
	
	@Override
	//Positive distance if the constraint has not been fulfilled.
	public double checkConstraintDistance(Object measurement) {		
		return ((NumericalRange) range).checkDistance((Double)measurement);		
	}
	
}
