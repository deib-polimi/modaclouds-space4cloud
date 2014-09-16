package it.polimi.modaclouds.space4cloud.mainProgram;

import it.polimi.modaclouds.space4cloud.utils.Configuration.Solver;

import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

public class EvaluationException extends ExecutionException {
	
	private Solver solver;
	private Path modelPath;

	/**
	 * 
	 */
	private static final long serialVersionUID = 52078430229581390L;

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
