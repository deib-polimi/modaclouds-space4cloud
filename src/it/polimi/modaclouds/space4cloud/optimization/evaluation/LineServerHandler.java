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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;

import org.slf4j.LoggerFactory;

import it.polimi.modaclouds.space4cloud.mainProgram.Space4Cloud;
import it.polimi.modaclouds.space4cloud.utils.Configuration;

public class LineServerHandler {
	/** LINE connection handlers **/
	private String host = null;
	private int port=-1;
	private Socket lineSocket = null;
	private PrintWriter out = null;
	private BufferedReader processIn = null;
	private BufferedReader socketIn = null;
	private Logger processLog;
	private Logger socketLog;
	private boolean localInstance = false;
	private Process proc;
	private File directory = null;
	private String MCR_dir = null;
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(LineServerHandler.class);

	public LineServerHandler() {
		
	}


	public void closeConnections() {

		//Send LINE command to close the connection
		if(out!= null && lineSocket != null && !lineSocket.isClosed()){
			String command = "CLOSE";
			out.println(command);
			out.flush();
		}
		if (out != null)
			out.close();

		try {
			if (socketLog != null && socketIn != null) {
				socketLog.close();
				socketIn.close();
				socketIn = null;
				socketLog = null;				
			}
			if (lineSocket != null){
				lineSocket.close();
				socketLog = null;	
			}
		} catch (IOException e) {
			logger.error("Error in closing LINE connection");
		}
	}
	
	private void loadProperties(){
		Properties lineProperties = new Properties();

		
		try {
			FileInputStream propInput = new FileInputStream(Configuration.LINE_PROP_FILE);
			lineProperties.load(propInput);
			propInput.close();
			if (host == null)
				host = lineProperties.getProperty("host", "localhost");
			if (port == -1)
				port = Integer.parseInt(lineProperties.getProperty("port",
						"5463"));
			directory = new File(lineProperties.getProperty("directory", null));
			MCR_dir = lineProperties.getProperty("MCR_dir","/usr/local/MATLAB/MATLAB_Compiler_Runtime/v81");
		}catch(IOException e){
			logger.error("Could not load LINE connection properties",e);
		}
	}

	public void connectToLINEServer() {

		
		loadProperties();
		
		try {			
			// try to connect
			initLINEConnection();
		} catch (UnknownHostException e) {
			// fallback to local host and retry
			if (host != "localhost") {
				closeConnections();
				logger.info("Don't know about host:" + host
						+ ". Switching to localhost and trying reconnection.");
				host = "localhost";
				connectToLINEServer();
			}else{
				logger.error("Error while connecting to localhost",e);
			}
		} catch (IOException e) {
			closeConnections();
			// fall back to local host and launch LINE
			Space4Cloud.consoleLogger.info("Could not connect to LINE on host: "
					+ host
					+ " on port: "
					+ port
					+ " trying to launch line locally and connect to localhost. This might take a while..");
			//launch line locally
			host = "localhost";
			launchLine();			
			try {
				initLINEConnection();
			} catch (IOException e1) {
				closeConnections();
				logger.error("Could not connect to local instance of LINE",e1);				
			}
		}
	}

	private void reConnect(){
		try {
			initLINEConnection();
		} catch (IOException e) {
			logger.error("Error in re-connecting to LINE",e);
		}
	}

	private void initLINEConnection() throws IOException {
		lineSocket = new Socket(host, port);

		out = new PrintWriter(lineSocket.getOutputStream());
		if (socketIn == null)
			socketIn = new BufferedReader(new InputStreamReader(
					lineSocket.getInputStream()));
		if (socketLog == null) {
			socketLog = new Logger(socketIn, "socket");
			(new Thread(socketLog)).start();
		}
		while (!socketLog.isConnected())
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				logger.error("Error while waiting for LINE connection",e);
			}
		Space4Cloud.consoleLogger.info("Connected to LINE on " + host + ":" + port);
	}

	public boolean isSolved(String modelFile) {
		return socketLog.isModelEvaluated(modelFile);
	}

	private final static String LINE_UNIX = "run_LINE.sh";
	private final static String LINE_WINDOWS = "LINE";
	
	public boolean launchLine() {
		try {
//			String lineInvocation = "LINE" + " " + "\""
//					+ propFile.getAbsolutePath().replace('\\', '/') + "\"";
			
			String lineInvocation = /*"LINE" +*/ " " + "\""
					+ Configuration.LINE_PROP_FILE.replace('\\', '/') + "\"";
			
			if (System.getProperty("os.name").indexOf("Windows") > -1)
				lineInvocation = LINE_WINDOWS + lineInvocation;
			else
				lineInvocation = LINE_UNIX+" "+MCR_dir+" "+Configuration.LINE_PROP_FILE.replace('\\', '/');
			
			logger.debug(lineInvocation);
			ProcessBuilder pb = new ProcessBuilder(lineInvocation.split("\\s"));
			pb.directory(directory);
			pb.redirectErrorStream(true);
			proc = pb.start();
			processIn = new BufferedReader(new InputStreamReader(
					proc.getInputStream()));
			processLog = new Logger(processIn, "process");
			(new Thread(processLog)).start();
			while (!processLog.isRunning())
				Thread.sleep(100);

			localInstance = true;

			// the startup has ended
			Space4Cloud.consoleLogger.info("Local instance of LINE launched");

			return true;
		} catch (IOException | InterruptedException e) {
			logger.error("Error in launching LINE",e);
			return false;
		}

	}

	public void solve(String modelFilePath, String REfilePath) {

		//check that the connection is alive
		if(out.checkError()){
			logger.info("Connection with LINE server has encoutner a problem, trying to reconnect...");
			reConnect();
		}

		modelFilePath = modelFilePath.replace('\\', '/');
		if (REfilePath != null)
			REfilePath = REfilePath.replace('\\', '/');
		// build the command
		String command = "SOLVE " + modelFilePath;
		if (REfilePath != null)
			command += " " + REfilePath;

		// "D://line_test//ofbiz ("+numExp+").xml D://line_test//ofbizRE ("+numExp+").xml";
		logger.debug("Sending: " + command);

		//reset the Logger in case a previous model with the same name have been specified
		socketLog.reset(modelFilePath);

		// send the command
		out.println(command);
		out.flush();

		return;

	}



	public void terminateLine() {
		if (localInstance) {
			//If LINE was launched by SPACE4Cloud close it
			out.println("QUIT");
			out.flush();
			try {
				proc.waitFor();
			} catch (InterruptedException e) {
				logger.error("Error in Quitting LINE",e);
			}
			
			if (processLog != null && processIn != null) {
				processLog.close();
				try {
					processIn.close();
				} catch (IOException e) {
					logger.error("Error in closing the process reader of LINE",e);
				}
			}
		}

		//otherwise just close the connection
		closeConnections();

	}

	/**
	 * Clear the handler from events of previous evaluations
	 */
	public void clear() {
		socketLog.clear();

	}


}
