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

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.time.StopWatch;
import org.palladiosimulator.solver.transformations.pcm2lqn.Pcm2LqnStrategy;
import org.slf4j.LoggerFactory;

public class Logger implements Runnable {
	private BufferedReader in;
	private boolean read = true;
	private boolean running = false;
	private boolean connected = false;
	private Map<String, String> evaluations = new HashMap<String, String>();
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Logger.class);
	private static final String SOLVED = "SOLVED";
	private static final String READY = "LINE READY";
	private static final String LISTENING = "Listening on port";
	private static final String STOP = "LINE STOP";
	private static final String ERROR = "ERROR";
	private static final String MODEL = "MODEL";
	private static final String SUBMITTED = "SUBMITTED";

	String prefix = "";

	public Logger(BufferedReader in, String prefix) {
		this.in = in;
		if (prefix != null)
			this.prefix = prefix;
	}

	public synchronized void close() {
		read = false;
	}

	public synchronized boolean isConnected() {
		return connected;
	}

	public synchronized boolean isModelEvaluated(String modelPath) {
		modelPath = Paths.get(modelPath).toString();
		return evaluations.containsKey(modelPath) && evaluations.get(modelPath).equals(SOLVED);
		// TODO clear the model form the map?
	}

	private synchronized boolean isRead() {
		return read;
	}

	public synchronized boolean isRunning() {
		return running;
	}

	private void logTime(String modelName, long time) {
		logger.info(modelName + ", " + evaluations.get(modelName) + ", " + time);
	}

	@Override
	public void run() {
		while (isRead())
			try {
				if (!in.ready())
					Thread.sleep(100);
				if (in.ready()) {
					String line = in.readLine();
					logger.debug("LINE " + prefix + ": " + line);

					// set the starting
					if (line.contains(LISTENING))
						setRunning(true);
					if (line.contains(READY))
						setConnected(true);
					if (line.contains(STOP))
						setRunning(false);
					if (line.contains(ERROR))
						manageError(line);
					else if (line.contains(MODEL))
						updateModelEvaluation(line);

				}

			} catch (IOException e) {
				if (e.getMessage().equals("Stream closed"))
					logger.debug("LINE " + prefix + ": " + e.getMessage());
				else
					logger.error("Error in reading from LINE output", e);
			} catch (InterruptedException e) {
				logger.error("Error in reading from LINE output", e);
			}
	}

	private void manageError(String line) {
		logger.error("LINE error:", line);
	}

	private synchronized void setConnected(boolean connected) {
		this.connected = connected;
	}

	private synchronized void setRunning(boolean running) {
		this.running = running;
	}

	private synchronized void updateModelEvaluation(String message) {
		message = message.trim().replaceAll(" +", " ");
		String[] tokens = message.split(" ");

		int offset = 0;

		String modelName = tokens[1];

		while (modelName.indexOf(Pcm2LqnStrategy.LQN_FILE_EXTENSION) == -1) {
			modelName = " " + tokens[2 + offset];
			offset++;
		}

		modelName = modelName.replace("_line." + Pcm2LqnStrategy.LQN_FILE_EXTENSION,
				"." + Pcm2LqnStrategy.LQN_FILE_EXTENSION);
		modelName = Paths.get(modelName).toString();
		String status = null;
		if (tokens.length == (4 + offset))
			status = tokens[3 + offset];
		else
			status = tokens[2 + offset];
		evaluations.put(modelName, status);

	}

	/**
	 * Removes the specified model from the list of model waiting from an
	 * evaluation
	 * 
	 * @param modelFilePath
	 */
	public synchronized void reset(String modelFilePath) {
		evaluations.remove(modelFilePath);

	}

	/**
	 * Clears the list of models waiting for an evaluation
	 */
	public void clear() {
		evaluations.clear();

	}

}
