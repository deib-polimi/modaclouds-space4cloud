package it.polimi.modaclouds.space4cloud.exceptions;

import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

import it.polimi.modaclouds.space4cloud.utils.Configuration.Solver;

public class EvaluationException extends ExecutionException {
	
	private Solver solver;
	private Path modelPath;

	/**
	 * 
	 */
	private static final long serialVersionUID = 52078430229581390L;

	public EvaluationException(String string) {
		super(string);
	}
	
	public EvaluationException(String string, Exception e) {
		super(string,e);
	}

	public Solver getSolver() {
		return solver;
	}

	public void setSolver(Solver solver) {
		this.solver = solver;
	}

	public Path getModelPath() {
		return modelPath;
	}

	public void setModelPath(Path modelPath) {
		this.modelPath = modelPath;
	}

}
