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

import it.polimi.modaclouds.space4cloud.utils.LoggerHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.time.StopWatch;

public class Logger implements Runnable {
	private BufferedReader in;
	private boolean read = true;
	private boolean running = false;
	private boolean connected = false;
	private Map<String,String> evaluations = new HashMap<String, String>();
	private Map<String, StopWatch> timers= new HashMap<String,StopWatch>();
	private static final org.slf4j.Logger logger = LoggerHelper.getLogger(Logger.class);
	private static final Object SUBMITTED = "SUBMITTED";
	private static final Object SOLVED = "SOLVED";

	String prefix="";
	public Logger(BufferedReader in, String prefix) {
		this.in = in;
		if(prefix != null)
			this.prefix = prefix; 
	}
	public synchronized void close(){
		read = false;
	}

	private synchronized boolean isRead() {
		return read;
	}

	public synchronized boolean isRunning() {
		return running;
	}

	public synchronized boolean isConnected(){
		return connected;
	}

	@Override
	public void run() {
		while(isRead())
			try {
				Thread.sleep(100);
				if(in.ready()){
					String line = in.readLine();				
					System.out.println("LINE "+prefix+": "+line);

					//set the starting
					if(line.contains("Listening on port"))
						setRunning(true);
					if(line.contains("LINE READY"))
						setConnected(true);
					if(line.contains("LINE STOP"))
						setRunning(false);
					if(line.contains("MODEL"))
						updateModelEvaluation(line);
				}

			} catch (IOException e) {
				if(e.getMessage().equals("Stream closed"))
					System.out.println("LINE "+prefix+": "+e.getMessage());
				else 
					e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	private synchronized void setRunning(boolean running){
		this.running = running;
	}

	private synchronized void setConnected(boolean connected){
		this.connected = connected;
	}


	private synchronized void updateModelEvaluation(String message){
		message = message.trim().replaceAll(" +", " ");		
		String[] tokens = message.split(" ");
		String modelName = tokens[1];		
		modelName = modelName.replace("_res.xml", ".xml");
		modelName = Paths.get(modelName).toString();
		String status = null;		
		if(tokens.length == 4)
			status = tokens[3];
		else
			status = tokens[2];		
		evaluations.put(modelName,status);
		
		if(status.equals(SUBMITTED)){
			StopWatch timer = new StopWatch();
			timer.start();
			timers.put(modelName, timer);
		}else if(status.equals(SOLVED)){
			timers.get(modelName).stop();
			logTime(modelName);
		}
		
		
	}
	
	public synchronized boolean isModelEvaluated(String modelPath){
		modelPath = Paths.get(modelPath).toString();
		return evaluations.containsKey(modelPath) && evaluations.get(modelPath).equals("SOLVED");		
		//TODO clear the model form the map?
	}
	
	private void logTime(String modelName){
		logger.info(modelName+", "+evaluations.get(modelName)+", "+timers.get(modelName).getTime());
	}



}
