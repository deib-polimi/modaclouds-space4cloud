package it.polimi.modaclouds.space4cloud.optimization;

import java.util.ArrayList;

import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution;

public class MoveChangeWorkloadHour extends AbsMoveHour {

	/**
	 * 
	 * @param sol - The Solution in which the resource is located
	 * @param i the hour of the solution
	 */
	public MoveChangeWorkloadHour(Solution sol, int i) {
		this.propertyNames = new ArrayList<>();
		this.propertyValues = new ArrayList<>();
		
		setSolution(sol);
		setHour(i);
	}
	
	private double rate = 0.0;
	
	public IMoveHour modifyWorkload(int population, double rate) {
		propertyNames.clear();
		propertyValues.clear();
		propertyNames.add("workload");
		propertyValues.add((int)Math.ceil(population * rate));
		this.rate = rate;
		return this;
	}

	public Solution apply() {		
		application.changeValues(null, this.propertyNames, this.propertyValues);
		if (rate > 0.0) {
			currentSolution.setPercentageWorkload(hour, rate);
			rate = 0.0;
		}
		
		return this.currentSolution;
	}

}
