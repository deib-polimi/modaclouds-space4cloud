package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import it.polimi.modaclouds.space4cloud.optimization.solution.IResponseTimeConstrainable;

import java.util.Map;

public interface IPercentileRTConstrainable extends IResponseTimeConstrainable {
	
	/**
	 * Returns a map of the percentiles of the responsetimes that have been evaluated
	 * @return
	 */
	public Map<Integer, Double> getRtPercentiles();

}
