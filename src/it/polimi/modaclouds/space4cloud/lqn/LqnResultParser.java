package it.polimi.modaclouds.space4cloud.lqn;

import java.util.HashMap;

public interface LqnResultParser {
	
	public double getResponseTime(String resourceID);
	public double getUtilization(String resourceID);
	public HashMap<String, Double> getUtilizations();
	public HashMap<String, Double> getResponseTimes();

}
