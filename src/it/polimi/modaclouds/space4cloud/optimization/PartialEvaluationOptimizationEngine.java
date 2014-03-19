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
import java.util.List;


public class PartialEvaluationOptimizationEngine extends OptEngine{

	private static final double DEFAULT_SCALE_IN_FACTOR = 2;
	private static final double MAX_NUMBER_OF_ITERATIONS = 20;

	public PartialEvaluationOptimizationEngine(ConstraintHandler handler) {
		super(handler);
	}

	@Override
	protected void IteratedRandomScaleInLS(Solution sol) {
		MoveOnVM[] moveArray = generateArrayMoveOnVM(sol);

		ArrayList<ArrayList<IaaS>> vettResTot = generateVettResTot(sol);
		/* first phase: overall descent optimization */
		//		logger.warn("Descent Optimization first phase - start");
		//		logger.warn(sol.isFeasible()+","+sol.numberOfUnfeasibleHours());
		//		for(Instance i:sol.getApplications())
		//			if(!i.isFeasible())
		//				logger.warn("\thour: "+sol.getApplications().indexOf(i)+" violated constraints: "+i.getNumerOfViolatedConstraints());
		boolean done = false;
		System.out.println("\t Descent Optimization second phase");
		resetNoImprovementCounter();
		Solution restartSol = sol.clone();
		IaaS res;
 		while (this.numIterNoImprov < MAX_NUMBER_OF_ITERATIONS) {
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
//							factor = ((UsageConstraint)c).getMax()/(100*res.getUtilization());

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

			}

			// here we have to implement the restart

			// the clone could be avoided if we save the original state of the
			// tiers
			sol = restartSol.clone();
			moveArray = generateArrayMoveOnVM(sol);
			vettResTot = generateVettResTot(sol);

		}// while number of iterations

	}

}
