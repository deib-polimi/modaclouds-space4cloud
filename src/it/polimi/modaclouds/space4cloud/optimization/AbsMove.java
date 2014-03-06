/**
 * 
 */
package it.polimi.modaclouds.space4cloud.optimization;

import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution;

import java.util.ArrayList;



/**
 * @author Michele Ciavotta
 *
 */
public abstract class AbsMove implements IMove {

	/** The current solution. */
	protected Solution currentSolution = null;
	
	/*the generic move can change a set of properties at once as, for example, 
	 * in the case of changing the type of VM*/
	
	/** The list of property names. */
	protected ArrayList<String> propertyNames = new ArrayList<>();
	
	/** The list of property values. */
	protected ArrayList<Object> propertyValues = new ArrayList<>();
	
	/* (non-Javadoc)
	 * @see it.polimi.modaclouds.space4cloud.optimization.IMove#setSolution(it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution)
	 */
	@Override
	public IMove setSolution(Solution solution) {
		currentSolution = solution;
		return this;
	}

	/* (non-Javadoc)
	 * @see it.polimi.modaclouds.space4cloud.optimization.IMove#apply()
	 */
	@Override
	public abstract Solution apply();

}
