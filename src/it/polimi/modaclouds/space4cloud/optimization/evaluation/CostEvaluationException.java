package it.polimi.modaclouds.space4cloud.optimization.evaluation;

import it.polimi.modaclouds.resourcemodel.cloud.CloudElement;
import it.polimi.modaclouds.resourcemodel.cloud.CloudResource;

public class CostEvaluationException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7614035779640119896L;
	CloudElement element;
	
	public CloudElement getElement() {
		return element;
	}
	
	public CostEvaluationException( Exception e, CloudElement element) {
		super(e);
		this.element = element;
	}

	public CostEvaluationException(CloudElement element) {
		this.element = element;
	}

}
