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
import org.slf4j.LoggerFactory;

public class Logger implements Runnable {
	private BufferedReader in;
	private boolean read = true;
	private boolean running = false;
	private boolean connected = false;
	private Map<String, String> evaluations = new HashMap<String, String>();
	private Map<String, StopWatch> timers = new HashMap<String, StopWatch>();
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Logger.class);
	private static final Object SUBMITTED = "SUBMITTED";
	private static final Object SOLVED = "SOLVED";

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
		return evaluations.containsKey(modelPath)
				&& evaluations.get(modelPath).equals("SOLVED");
		// TODO clear the model form the map?
	}

	private synchronized boolean isRead() {
		return read;
	}

	public synchronized boolean isRunning() {
		return running;
	}

	private void logTime(String modelName, long time) {
		logger.info(modelName + ", " + evaluations.get(modelName) + ", "
				+ time);
	}

	@Override
	public void run() {
		while (isRead())
			try {
				if(!in.ready())
					Thread.sleep(100);
				if (in.ready()) {
					String line = in.readLine();
					logger.debug("LINE " + prefix + ": " + line);

					// set the starting
					if (line.contains("Listening on port"))
						setRunning(true);
					if (line.contains("LINE READY"))
						setConnected(true);
					if (line.contains("LINE STOP"))
						setRunning(false);
					if (line.contains("ERROR"))
						manageError(line);
					else if (line.contains("MODEL"))
						updateModelEvaluation(line);

				}

			} catch (IOException e) {
				if (e.getMessage().equals("Stream closed"))
					logger.debug("LINE " + prefix + ": " + e.getMessage());
				else
					logger.error("Error in reading from LINE output",e);
			} catch (InterruptedException e) {
				logger.error("Error in reading from LINE output",e);
			}
	}

	private void manageError(String line) {
		logger.error("LINE error:",line);		
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
		
		while (modelName.indexOf("xml") == -1) {
			modelName += " " + tokens[2 + offset];
			offset++;
		}
		
		modelName = modelName.replace("_line.xml", ".xml");
		modelName = Paths.get(modelName).toString();
		String status = null;
		if (tokens.length == (4 + offset))
			status = tokens[3 + offset];
		else
			status = tokens[2 + offset];
		evaluations.put(modelName, status);

		if (status.equals(SUBMITTED)) {
			StopWatch timer = new StopWatch();
			timer.start();
			timers.put(modelName, timer);
		} else if (status.equals(SOLVED)) {
			long time = -1;
			try {
				if(timers.containsKey(modelName)){
					timers.get(modelName).stop();
					time = timers.get(modelName).getTime();
				}
			} catch (IllegalStateException e) {
				logger.error("Error in taking the time, will put -1",e);				
			} finally
			{
				logTime(modelName,time);
			}

		}

	}

	/**
	 * Removes the specified model from the list of model waiting from an evaluation
	 * @param modelFilePath
	 */
	public synchronized void reset(String modelFilePath) {
		evaluations.remove(modelFilePath);
		timers.remove(modelFilePath);

	}

	/**
	 * Clears the list of models waiting for an evaluation
	 */
	public void clear() {
		evaluations.clear();
		timers.clear();

	}

}
