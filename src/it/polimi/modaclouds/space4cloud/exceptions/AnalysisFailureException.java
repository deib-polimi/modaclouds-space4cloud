package it.polimi.modaclouds.space4cloud.exceptions;

public class AnalysisFailureException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7869806532128231436L;
	private String filePath;


	public AnalysisFailureException(String resultfilePath) {
		filePath = resultfilePath;
	}
	public String getFilePath() {
		return filePath;
	}

}
