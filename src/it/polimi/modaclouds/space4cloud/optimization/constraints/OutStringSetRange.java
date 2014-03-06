/**
 * 
 */
package it.polimi.modaclouds.space4cloud.optimization.constraints;

import java.util.HashSet;

/**
 * @author Michele Ciavotta
 * The class that defines a out range of Strings object
 */
public class OutStringSetRange extends HashSet<String> implements IRange {

	/**
	 * 
	 */
	private static final long serialVersionUID = -592457561038094202L;

	/* (non-Javadoc)
	 * @see it.polimi.modaclouds.space4cloud.optimization.constraints.IRange#validate(java.lang.Object)
	 */
	@Override
	public boolean validate(Object value) {
		return !this.contains(value.toString());
	}

}
