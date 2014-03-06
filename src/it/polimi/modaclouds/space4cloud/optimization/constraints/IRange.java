/**
 * 
 */
package it.polimi.modaclouds.space4cloud.optimization.constraints;

/**
 * @author Michele Ciavotta
 * Define the range of the constraint
 */
public interface IRange {

	public boolean validate(Object value);
}
