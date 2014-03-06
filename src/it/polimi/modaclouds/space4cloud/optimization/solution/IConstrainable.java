/**
 * 
 */
package it.polimi.modaclouds.space4cloud.optimization.solution;

import it.polimi.modaclouds.space4cloud.lqn.LqnResultParser;

/**
 * @author Michele Ciavotta
 * This interface define constrainable classes
 */
public interface IConstrainable {
	 public String getId();
	 
	 
	 public void update(LqnResultParser parser);

}
