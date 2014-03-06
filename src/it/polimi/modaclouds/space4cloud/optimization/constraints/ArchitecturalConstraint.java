/**
 * 
 */
package it.polimi.modaclouds.space4cloud.optimization.constraints;

import it.polimi.modaclouds.space4cloud.optimization.solution.impl.CloudService;

import java.util.Set;


/**
 * @author Michele Ciavotta
 *
 */
public abstract class ArchitecturalConstraint extends Constraint {

	/**
	 * @param id
	 * @param metric
	 * @param priority
	 * @param unit
	 */
	public ArchitecturalConstraint(String id, Metric metric, int priority,
			Unit unit) {
		super(id, metric, priority, unit);		
		// TODO Auto-generated constructor stub
	}

	public void setInSet(Set<String> set) {
		// TODO Auto-generated method stub
		
	}

	public String getInSet() {
		// TODO Auto-generated method stub
		return null;
	}
	public void setOutSet(Set<String> set) {
		// TODO Auto-generated method stub
		
	}
	public String getOutSet() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public abstract boolean checkConstraint(CloudService resource);




}
