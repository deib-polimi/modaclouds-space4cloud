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
package it.polimi.modaclouds.space4cloud.optimization.evaluation;

import it.polimi.modaclouds.space4cloud.lqn.LqnResultParser;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Instance;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.SolutionMulti;

import java.util.HashMap;
import java.util.Map;


/*
 * This class define a proxy object able to verify if a application has been already evaluated
 * in order to speed up the whole evaluation process
 * */
public class EvaluationProxy extends EvaluationServer {

	private final Map<String,LqnResultParser> map = new HashMap<String,LqnResultParser>();
		
	int proxiedSolutions = 0;
	
	public EvaluationProxy(String solver) {
		super(solver);
	}
	
	public void ProxyIn(Solution sol){
		if (!enabled)
			return;

		for (Instance instance : sol.getApplications()) {
			if(!instance.isEvaluated()){
				String str = instance.getHashString();
				if (map.containsKey(str)) {	
					proxiedSolutions++;
					LqnResultParser results = map.get(str);
					instance.updateResults(results);
					instance.setEvaluated(true);
				}
			}
		}
	}
	public Solution ProxyOut(Solution sol){
		if (!enabled)
			return sol;
		
		for(Instance instance : sol.getApplications()){
			String hashStr = instance.getHashString();
			if (!map.containsKey(hashStr))
				if(instance.getResultParser()==null)
					System.out.println();
			map.put(hashStr, instance.getResultParser());
		}	
		return sol;
	}

	// This enable or disable the proxy! If set to "false", the proxy won't act as a proxy, evaluating
	// each and every solution requested. Useful!
	private boolean enabled = true;
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	



	public void EvaluateSolution(Solution sol) {
		this.ProxyIn(sol);
		super.EvaluateSolution(sol);
		this.ProxyOut(sol);
	}
	
	public int getProxiedSolutions() {
		return proxiedSolutions;
	}
	
	@Override	
	public void showStatistics() {
		super.showStatistics();
		System.out.println("Number of proxied solutions "+getProxiedSolutions());
	}
	
	public void EvaluateSolution(SolutionMulti sol) {
		for (Solution s : sol.getAll())
			EvaluateSolution(s);
		sol.updateEvaluation();
	}

}
