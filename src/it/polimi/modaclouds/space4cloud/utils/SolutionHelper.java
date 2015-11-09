package it.polimi.modaclouds.space4cloud.utils;

import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Tier;

public class SolutionHelper {
	
	/**
	 * Builds the solution type ID looking at all tiers and all type of services that are selected. 
	 * it substitutes the id the cloud service used in the selectedTier with the specified token			
	 * @param sol
	 * @param selectedTier
	 * @param token
	 * @return
	 */
	public static String buildSolutionTypeID(Solution sol, Tier selectedTier, String token){
		String solutionTypeID = "";
		for(Tier t:sol.getApplication(0).getTiers()){
			if(selectedTier != null && token!=null && t.equals(selectedTier)){
				solutionTypeID += t.getId()+","+token+",";
			}else
				solutionTypeID += t.getId()+","+t.getCloudService().getResourceName()+",";
		}
		return solutionTypeID;
	}

	/**
	 * Builds the solution type ID looking at all tiers and all type of services that are selected.
	 * @param sol
	 * @return
	 */
	public static String buildSolutionTypeID(Solution sol){
		return buildSolutionTypeID(sol, null, null);
	}
	
	/**
	 * Returns an id built using the tier ID and the name of the cloud service used in the tier.
	 * @param t the Tier
	 * @return the id
	 */
	public static String buildTierTypeID(Tier t){
		return t.getId()+","+t.getCloudService().getResourceName();
	}
	
	/**
	 * retireve the resource name from the tierTypeID
	 * @param typerTypeID
	 * @return the resource name
	 */
	public static String getResourceNameFromTierTypeID(String typerTypeID){
		return typerTypeID.split(",")[1];
	}

}
