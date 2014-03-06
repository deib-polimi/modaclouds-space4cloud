/**
 * 
 */
package it.polimi.modaclouds.space4cloud.optimization;

import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution;


/**
 * @author Michele Ciavotta
 * Some move don't act on a specific hour but rather on every hour
 */
public interface IMove {
	
	/**
	 * Sets the solution.
	 *
	 * @param solution the solution
	 * @return the IMove interface
	 */
	public IMove setSolution(Solution solution);
	
	
	
	/**
	 * Method that applies the move.
	 *
	 * @return the modified solution
	 */
	public Solution apply();

}
