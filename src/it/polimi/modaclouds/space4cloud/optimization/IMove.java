/*******************************************************************************
 * Copyright 2014 Giovanni Paolo Gibilisco
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/**
 * 
 */
package it.polimi.modaclouds.space4cloud.optimization;

import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution;

/**
 * @author Michele Ciavotta Some move don't act on a specific hour but rather on
 *         every hour
 */
public interface IMove {

	/**
	 * Method that applies the move.
	 * 
	 * @return the modified solution
	 */
	public Solution apply();

	/**
	 * Sets the solution.
	 * 
	 * @param solution
	 *            the solution
	 * @return the IMove interface
	 */
	public IMove setSolution(Solution solution);

}
