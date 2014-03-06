package it.polimi.modaclouds.space4cloud.optimization.evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;


public class LineServerHandler {
	/** LINE connection handlers**/
	private Socket lineSocket = null;
	private PrintWriter out = null;
	private BufferedReader processIn = null;
	private BufferedReader socketIn = null;
	private String linePropFilePath;
	private Logger processLog;
	private Logger socketLog;
	private boolean localInstance = false;
	private Process proc;

	public void connectToLINEServer() {
		connectToLINEServer(null,-1);		
	}

	public void connectToLINEServer(String configFilePath){
		linePropFilePath = configFilePath;
		connectToLINEServer();
	}


	public void connectToLINEServer(String host, int port){				

		Properties lineProperties = new Properties();
		
		URL linePropFileURL = null;		
		File linePropFile = null;
		try {
			linePropFileURL = FileLocator.toFileURL(new URL(linePropFilePath));
			linePropFile = new File(linePropFileURL.getFile());
		} catch (MalformedURLException e2) {
			//if it is not a workspace url but a path to a file
			linePropFile = new File(linePropFilePath);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		 
		File directory = null;
		try{

			FileInputStream propInput = new FileInputStream(linePropFile);
			lineProperties.load(propInput);
			propInput.close();
			if(host == null)
				host = lineProperties.getProperty("host", "localhost");
			if (port == -1)
				port = Integer.parseInt(lineProperties.getProperty("port", "5463"));			
			directory = new File(lineProperties.getProperty("directory",null));			

			//try to connect
			initLINEConnection(host,port);
		} catch (UnknownHostException e) {
			//fallback to local host and retry
			if(host != "localhost"){
				closeConnections();
				System.out.println("Don't know about host:"+host+". Switching to localhost and trying reconnection.");
				host = "localhost";		
				connectToLINEServer(host, port);
			}
		} catch (IOException e) {
			closeConnections();
			//fall back to local host and launch LINE
			System.out.println("Could not connect to LINE on host: "+host+" on port: "+port+
					"\ntrying to launch line locally and connect to localhost.");			
			launchLine(linePropFile,directory);
			host = "localhost";
			try {
				initLINEConnection(host, port);
			} catch (IOException e1) {
				closeConnections();
				System.err.println("Could not connect to local instance of LINE");
				e1.printStackTrace();
			}
		}
	}


	private void initLINEConnection(String host, int port) throws IOException{
		lineSocket = new Socket(host, port); 
		
		out = new PrintWriter(lineSocket.getOutputStream());
		if(socketIn==null)
			socketIn = new BufferedReader(new InputStreamReader(lineSocket.getInputStream()));
		if(socketLog ==null){
			socketLog = new Logger(socketIn,"socket");
			(new Thread(socketLog)).start();
		}
		while(!socketLog.isConnected())
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		System.out.println("Connected to LINE on "+host+":"+port);
	}

	private boolean launchLine(File linePropFile, File directory){
		try {
			String lineInvocation = "LINE"+" "+"\""+linePropFile.getAbsolutePath().replace('\\', '/')+"\""; 
			System.out.println(lineInvocation);
			ProcessBuilder pb = new ProcessBuilder(lineInvocation.split("\\s"));
			pb.directory(directory);
			pb.redirectErrorStream(true);			
			proc = pb.start();
			processIn = new BufferedReader (new InputStreamReader(proc.getInputStream()));			
			processLog = new Logger(processIn,"process");
			(new Thread(processLog)).start();
			while(!processLog.isRunning()) Thread.sleep(100);;
			localInstance = true;
			
			//the startup has ended
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	public void terminateLine(){
		if(localInstance){				
			out.println("QUIT");
			out.flush();
			try {
				proc.waitFor();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		closeConnections();

	}

	public void closeConnections(){
		if(out != null)
			out.close();

		try {
			if(processLog != null && processIn!=null){
				processLog.close();
				processIn.close();				
			}
			if(socketLog != null && socketIn != null){
				socketLog.close();
				socketIn.close();
			}
			if(lineSocket!=null)
				lineSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void solve(String modelFilePath, String REfilePath) {
		
		modelFilePath = modelFilePath.replace('\\', '/');
		if(REfilePath != null)
			REfilePath = REfilePath.replace('\\', '/');
		//build the command
		String command = "SOLVE " + modelFilePath;
		if(REfilePath != null)
			command +=" "+REfilePath;				

		//"D://line_test//ofbiz ("+numExp+").xml D://line_test//ofbizRE ("+numExp+").xml";
		System.out.println("Sending: "+command);

		//send the command
		out.println(command);
		out.flush();

		return;

	}
	
	public boolean isSolved(String modelFile){
		return socketLog.isModelEvaluated(modelFile);		
	}
}
