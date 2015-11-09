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

import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Instance;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution;

/**
 * The Class AbsMoveHour.
 * 
 * @author Michele Ciavotta Abstract class implementing the most general methods
 *         of a Move
 */
public abstract class AbsMoveHour extends AbsMove implements IMoveHour {

	/** The hour. */
	protected int hour = 1;

	/** The application for the considered hour. */
	protected Instance application = null;

	/*
	 * (non-Javadoc)
	 * 
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.polimi.modaclouds.space4cloud.optimization.IMove#setSolution(it.polimi
	 * .modaclouds.space4cloud.optimization.solution.Solution)
	 */
	@Override
	public IMoveHour setSolution(Solution solution) {

		this.currentSolution = solution;
		this.application = this.currentSolution.getApplication(this.hour);
		return this;
	}
}
