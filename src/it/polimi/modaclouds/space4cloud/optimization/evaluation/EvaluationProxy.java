package it.polimi.modaclouds.space4cloud.optimization.evaluation;

import it.polimi.modaclouds.space4cloud.lqn.LqnResultParser;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Instance;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution;
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
		for(Instance instance : sol.getApplications()){
			String hashStr = instance.getHashString();
			if (!map.containsKey(hashStr))
				if(instance.getResultParser()==null)
					System.out.println();
			map.put(hashStr, instance.getResultParser());
		}	
		return sol;
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

}
