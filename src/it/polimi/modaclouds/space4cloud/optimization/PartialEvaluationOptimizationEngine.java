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

import it.polimi.modaclouds.space4cloud.optimization.constraints.Constraint;
import it.polimi.modaclouds.space4cloud.optimization.constraints.ConstraintHandler;
import it.polimi.modaclouds.space4cloud.optimization.constraints.Metric;
import it.polimi.modaclouds.space4cloud.optimization.constraints.UsageConstraint;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.IaaS;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PartialEvaluationOptimizationEngine extends OptEngine{

	private static final double DEFAULT_SCALE_IN_FACTOR = 2;
	private static final double MAX_NUMBER_OF_ITERATIONS = 20;
	private static final Logger logger = LoggerFactory.getLogger(PartialEvaluationOptimizationEngine.class);

	public PartialEvaluationOptimizationEngine(ConstraintHandler handler) {
		super(handler);
	}
	
	public PartialEvaluationOptimizationEngine(ConstraintHandler handler, boolean batch) {
		super(handler, batch);
	}

	@Override
	protected void IteratedRandomScaleInLS(Solution sol) {
		logger.info("initializing scale in phase");
		optimLogger.trace("scaleIn phase");
		MoveOnVM[] moveArray = generateArrayMoveOnVM(sol);

		ArrayList<ArrayList<IaaS>> vettResTot = generateVettResTot(sol);
		/* first phase: overall descent optimization */
		//		logger.warn("Descent Optimization first phase - start");
		//		logger.warn(sol.isFeasible()+","+sol.numberOfUnfeasibleHours());
		//		for(Instance i:sol.getApplications())
		//			if(!i.isFeasible())
		//				logger.warn("\thour: "+sol.getApplications().indexOf(i)+" violated constraints: "+i.getNumerOfViolatedConstraints());
		boolean done = false;
		logger.info(sol.showStatus());
		resetNoImprovementCounter();
		Solution restartSol = sol.clone();
		IaaS res;
 		while (this.numIterNoImprov < MAX_NUMBER_OF_ITERATIONS) {
 			optimLogger.trace("iteration "+numIterNoImprov);
			done = false;
			List<Integer> hoursList = new ArrayList<Integer>();
			for(int p=0;p<24;p++) hoursList.add(p);

			while (!done) {
				res = null;
				boolean noScaleIn = true;
				int pos = random.nextInt(hoursList.size());
				Integer hour = hoursList.get(pos);

				//remove resources with minimum number of replicas
				for (int j = 0; j < vettResTot.get(hour).size(); j++)
					if (vettResTot.get(hour).get(j).getReplicas() == 1)
						vettResTot.get(hour).remove(j);


				//if there is a resource that can be scaled so be it.
				if (vettResTot.get(hour).size() > 0) {
					res = vettResTot.get(hour).get(
							random.nextInt(vettResTot.get(hour).size()));

					//if a utilization constraint is defined the factor is the utilization threshold over the actual utilization, otherwise it is 2
					double factor = DEFAULT_SCALE_IN_FACTOR;
					//adaptive factor disabled.
//					List<Constraint> constraints = getConstraintHandler().getConstraintsByService(res);
//					for(Constraint c:constraints)
//						if(c.getMetric().equals(Metric.CPU))
//							factor = res.getUtilization()/((UsageConstraint)c).getMax();

					moveArray[hour].scaleIn(res,factor);
					//moveArray[hour].scaleIn(res);
					noScaleIn = false;
				}
				else{ 
					hoursList.remove(pos);
					if (hoursList.size() == 0){
						numIterNoImprov++;
						done = true;
					}

					continue;
				}


				//evaluate the feasibility only if the cost is better than the best solution cost
				//System.out.println("Count: "+count);
				evalProxy.deriveCosts(sol);
				logger.info("proposed solution: "+sol.showStatus());
				if(sol.getCost() < bestSolution.getCost()){
					evalProxy.EvaluateSolution(sol);
					updateBestSolution(sol);
					if(!sol.isFeasible())
						done = true;							

				}


				// without any scaleIn the solution is stuck
				if (noScaleIn) {
					numIterNoImprov++;
					//break;
					done = true;	
				}

				optimLogger.trace(sol.showStatus());
			}

			// here we have to implement the restart

			// the clone could be avoided if we save the original state of the
			// tiers
			
			logger.info("restarting scale in");
			sol = restartSol.clone();
			moveArray = generateArrayMoveOnVM(sol);
			vettResTot = generateVettResTot(sol);

			
		}
 		
 		logger.info("scale in ended");// while number of iterations

	}

}
