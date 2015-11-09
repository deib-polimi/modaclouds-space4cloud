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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.slf4j.LoggerFactory;

import de.uka.ipd.sdq.pcmsolver.runconfig.MessageStrings;
import it.polimi.modaclouds.space4cloud.exceptions.AnalysisFailureException;
import it.polimi.modaclouds.space4cloud.lqn.LINEResultParser;
import it.polimi.modaclouds.space4cloud.lqn.LQNSResultParser;
import it.polimi.modaclouds.space4cloud.lqn.LqnHandler;
import it.polimi.modaclouds.space4cloud.lqn.LqnResultParser;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Instance;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution;
import it.polimi.modaclouds.space4cloud.utils.Configuration;
import it.polimi.modaclouds.space4cloud.utils.Configuration.Solver;

public class SolutionEvaluator implements Runnable {

	// Should this part go into a helper class?
	// Return values of lqns
	/** The Constant LQNS_RETURN_SUCCESS. */
	private static final int LQNS_RETURN_SUCCESS = 0;

	/** The Constant LQNS_RETURN_MODEL_FAILED_TO_CONVERGE. */
	private static final int LQNS_RETURN_MODEL_FAILED_TO_CONVERGE = 1;

	/** The Constant LQNS_RETURN_INVALID_INPUT. */
	private static final int LQNS_RETURN_INVALID_INPUT = 2;

	/** The Constant LQNS_RETURN_FATAL_ERROR. */
	private static final int LQNS_RETURN_FATAL_ERROR = -1;

	private LineServerHandler handler;
	
	public static final org.slf4j.Logger logger = LoggerFactory.getLogger(SolutionEvaluator.class);

	String filePath;
	String resultfilePath;
	Instance instance;
	LqnHandler lqnHandler;
	LqnResultParser resultParser;
	Solution solution;
	ArrayList<ActionListener> listeners = new ArrayList<>();

	public SolutionEvaluator(Instance instance, Solution sol) {		
		this.instance = instance;
		this.lqnHandler = instance.getLqnHandler();
		filePath = lqnHandler.getLqnFilePath().toAbsolutePath().toString();
		this.solution = sol;
	}

	public void addListener(ActionListener listener) {
		listeners.add(listener);
	}

	public String getFilePath() {
		return filePath;
	}

	public Solution getSolution() {
		return solution;
	}

	public void parseResults() throws AnalysisFailureException {
		if (Configuration.SOLVER == Solver.LQNS) {
			resultfilePath = filePath.substring(0, filePath.lastIndexOf('.'))
					+ ".lqxo";
			//use temporary file if lqns did produce it but crashed after finish. Results here might be inaccurate
			if(!Paths.get(resultfilePath).toFile().exists())
				resultfilePath = resultfilePath+"~";
			if(!Paths.get(resultfilePath).toFile().exists())
				throw new AnalysisFailureException(resultfilePath);
			resultParser = new LQNSResultParser(Paths.get(resultfilePath));
		} else {
			String randomEnvironmentName = "";						
			if (Configuration.RANDOM_ENV_FILE != null && !Configuration.RANDOM_ENV_FILE.equals(""))
				randomEnvironmentName="+"+Paths.get(Configuration.RANDOM_ENV_FILE).getFileName().toString().replace(".xml","");
			resultfilePath = filePath.substring(0, filePath.lastIndexOf('.'))
					+randomEnvironmentName+Configuration.LINE_SOLUTION_TAG+".xml";
			resultParser = new LINEResultParser(Paths.get(resultfilePath));
		}

		instance.updateResults(resultParser);
		for (ActionListener l : listeners)
			l.actionPerformed(new ActionEvent(this, 0, "ResultsUpdated"));
	}

	private void readStream(InputStream is, boolean show) {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				if (show)
					logger.debug("Pb: " + line);
			}

		} catch (IOException ioe) {
			logger.error("Error in reading stream",ioe);
		}
	}

	public void removeListener(ActionListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void run() {
		// Update and write the lqn model
		instance.updateLqn();
		lqnHandler.saveToFile();

		logger.trace("Evaluating Model: "+filePath);
		// run the evaluator
		if (Configuration.SOLVER == Solver.LQNS)
			runWithLQNS();
		// Evaluate with LINE
		else
			runWithLINE();
		
		instance.setEvaluated(true);
		try {
			parseResults();
		} catch (AnalysisFailureException e) {
			logger.error("Error in the evaluation of model: "+e.getFilePath());
			for(ActionListener l:listeners)
				l.actionPerformed(new ActionEvent(this, 0, "EvaluationError"));
		}		
	}

	private void runWithLINE() {
		if (handler == null) {
			logger.error("LINE server handle not initialized");
			return;
		}		

		String randomEnvFile = null;
		if (Configuration.RANDOM_ENV_FILE != null && !Configuration.RANDOM_ENV_FILE.equals(""))
			randomEnvFile = Configuration.RANDOM_ENV_FILE;
		handler.solve(filePath,randomEnvFile);		
		// wait for the model to be solved
		while (!handler.isSolved(filePath))
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				logger.error("Error watiting for LINE solution",e);
			}

	}

	private void runWithLQNS() {
		try {
			String solverProgram = "lqns";			

			String command = solverProgram + " " + filePath + /*" -f"+*/ " -z iteration-limit=1000";
			// String command = solverProgram+" "+filePath; //without using the
			// fast option			
			ProcessBuilder pb = new ProcessBuilder(splitToCommandArray(command));
			Process proc = pb.start();
			readStream(proc.getInputStream(), true);
			readStream(proc.getErrorStream(), true);
			int exitVal = -1;
			try{
				exitVal = proc.waitFor();
			}catch (InterruptedException e){
				logger.debug("Evaluation of model: "+filePath+" was interrupted");
			}
			proc.destroy();

			if (exitVal == LQNS_RETURN_SUCCESS) {
				instance.setEvaluated(true);
			} else if (exitVal == LQNS_RETURN_MODEL_FAILED_TO_CONVERGE) {
				logger.error(MessageStrings.LQNS_SOLVER
								+ " exited with "
								+ exitVal
								+ ": The model failed to converge. Results are most likely inaccurate. ");
				logger.error("Analysis Result has been written to: "
						+ resultfilePath);
				instance.setEvaluated(false);
			} else {
				String message = "";
				if (exitVal == LQNS_RETURN_INVALID_INPUT) {
					message = solverProgram + " exited with " + exitVal
							+ ": Invalid Input.";
				} else if (exitVal == LQNS_RETURN_FATAL_ERROR) {
					message = solverProgram + " exited with " + exitVal
							+ ": Fatal error";
				} else {
					message = solverProgram
							+ " returned an unrecognised exit value "
							+ exitVal
							+ ". Key: 0 on success, 1 if the model failed to meet the convergence criteria, 2 if the input was invalid, 4 if a command line argument was incorrect, 8 for file read/write problems and -1 for fatal errors. If multiple input files are being processed, the exit code is the bit-wise OR of the above conditions.";
				}
				logger.error(message);
				instance.setEvaluated(false);
			}
		} catch (IOException e) {
			logger.error("Error in evaluting the  solution ",e);
		}
	}

	public void setLineServerHandler(LineServerHandler handler) {
		this.handler = handler;
	}

	private String[] splitToCommandArray(String command) {
		return command.split("\\s");
	}

}
