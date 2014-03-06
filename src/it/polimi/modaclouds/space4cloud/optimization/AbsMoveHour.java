/**
 * 
 */
package it.polimi.modaclouds.space4cloud.optimization;

import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Instance;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution;

/**
 * The Class AbsMoveHour.
 *
 * @author Michele Ciavotta
 * Abstract class implementing the most general methods of a Move
 */
public abstract class AbsMoveHour extends AbsMove implements IMoveHour {

	/** The hour. */
	protected int hour = 1;
	
	/** The application for the considered hour. */
	protected Instance application = null;
	
	/* (non-Javadoc)
	 * @see it.polimi.modaclouds.space4cloud.optimization.IMove#setSolution(it.polimi.modaclouds.space4cloud.optimization.solution.Solution)
	 */
	@Override
	public IMoveHour setSolution(Solution solution) {
		
		this.currentSolution = solution;
		this.application = this.currentSolution.getApplication(this.hour);
		return this;
	}

	/* (non-Javadoc)
	 * @see it.polimi.modaclouds.space4cloud.optimization.IMove#setHour(int)
	 */
	@Override
	public IMoveHour setHour(int hour) {
		
		if (this.hour != hour) {
			this.hour = hour;
			this.application = this.currentSolution.getApplication(this.hour);
		}
		return this;
	}
}
