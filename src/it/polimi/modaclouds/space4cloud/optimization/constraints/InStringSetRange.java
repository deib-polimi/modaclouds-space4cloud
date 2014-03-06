/**
 * 
 */
package it.polimi.modaclouds.space4cloud.optimization.constraints;

import java.util.HashSet;

/**
 * @author Michele Ciavotta
 * The class that defines a range of Strings object
 */
public class InStringSetRange extends HashSet<String> implements IRange {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 682730244372183648L;

	/* (non-Javadoc)
	 * @see it.polimi.modaclouds.space4cloud.optimization.constraints.IRange#validate(java.lang.Object)
	 */
	@Override
	public boolean validate(Object value) {
		return this.contains(value.toString()) ;
	}
	
	
}
