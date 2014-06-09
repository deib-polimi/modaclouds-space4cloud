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
package it.polimi.modaclouds.space4cloud.optimization;

import it.polimi.modaclouds.space4cloud.optimization.constraints.ConstraintHandler;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.IaaS;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PartialEvaluationOptimizationEngine extends OptEngine {

	private static final double DEFAULT_SCALE_IN_FACTOR = 2;
	private static final double MAX_NUMBER_OF_ITERATIONS = 25;
	private static final double MAX_NUMBER_OF_ITERATIONS_NO_IMPR = 7;
	private static final Logger logger = LoggerFactory
			.getLogger(PartialEvaluationOptimizationEngine.class);

	public PartialEvaluationOptimizationEngine(ConstraintHandler handler) {
		super(handler);
	}

	public PartialEvaluationOptimizationEngine(ConstraintHandler handler,
			boolean batch) {
		super(handler, batch);
	}

	@Override
	protected void IteratedRandomScaleInLS(Solution sol) {
		logger.info("initializing scale in phase");
		optimLogger.trace("scaleIn phase");

		/* first phase: overall descent optimization */
		// logger.warn("Descent Optimization first phase - start");
		// logger.warn(sol.isFeasible()+","+sol.numberOfUnfeasibleHours());
		// for(Instance i:sol.getApplications())
		// if(!i.isFeasible())
		// logger.warn("\thour: "+sol.getApplications().indexOf(i)+" violated constraints: "+i.getNumerOfViolatedConstraints());

		logger.info(sol.showStatus());
		resetNoImprovementCounter();
		Solution restartSol = sol.clone();

		while (this.numIterNoImprov < MAX_NUMBER_OF_ITERATIONS_NO_IMPR) {
			boolean scaled = noImprovementPhase(sol);
			if (!scaled)
				sol = restartSol;
			logger.debug("No improvement iterations: " + numIterNoImprov);
		}

		logger.info("scale in ended");// while number of iterations

	}

	/**
	 * For each hourly application perform a scale in of a random resource with
	 * a descending factor that starts from the default value and ends to 1.
	 * 
	 * @param sol
	 * @return true is the solution has been improved, false if no resources
	 *         could be scaled
	 */
	private boolean noImprovementPhase(Solution sol) {

		// initialize the factors (one for each hour) to the default value
		double[] factors = new double[24];
		for (int i = 0; i < 24; i++)
			factors[i] = DEFAULT_SCALE_IN_FACTOR;
		boolean noScaleIn = true;

		for (int iterations = 0; iterations < MAX_NUMBER_OF_ITERATIONS; iterations++) {
			String scalingFactors = "";
			for (int i = 0; i < 24; i++)
				scalingFactors += " h: " + i + " val: " + factors[i];
			logger.debug("Scaling factors: " + scalingFactors);
			Solution previousSol = sol.clone();
			ArrayList<ArrayList<IaaS>> vettResTot = generateVettResTot(sol);
			MoveOnVM[] moveArray = generateArrayMoveOnVM(sol);
			// scale in each hour
			for (int hour = 0; hour < 24; hour++) {
				IaaS res = null;
				// remove resources with minimum number of replicas
				for (int j = 0; j < vettResTot.get(hour).size(); j++)
					if (vettResTot.get(hour).get(j).getReplicas() == 1)
						vettResTot.get(hour).remove(j);

				// if no resource can be scaled in in this hour then jump to the
				// next one
				if (vettResTot.get(hour).size() == 0)
					continue;

				// if there are resources that can be scale then chose one
				// randomly
				res = vettResTot.get(hour).get(
						random.nextInt(vettResTot.get(hour).size()));

				// scale the resource by the factor
				moveArray[hour].scaleIn(res, factors[hour]);
				noScaleIn = false;

			}

			// if there was no scale in for any of the 24 hours increase the
			// number of iterations with no improvement and exit
			if (noScaleIn) {
				numIterNoImprov++;
				return false;
			}

			// evaluate the solution
			evalProxy.EvaluateSolution(sol);
			updateBestSolution(sol);

			// if an application has become feasible, revert it and try again
			// with a smaller factor
			for (int i = 0; i < 24; i++) {
				if (!sol.getApplication(i).isFeasible()) {
					sol.copyApplication(previousSol.getApplication(i), i);
					factors[i] = DEFAULT_SCALE_IN_FACTOR
							- ((iterations + 1) / MAX_NUMBER_OF_ITERATIONS)
							* (DEFAULT_SCALE_IN_FACTOR - 1);
				}
			}

		}
		return true;

	}

}
