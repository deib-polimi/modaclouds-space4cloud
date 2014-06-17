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
/*
 * 
 */
package it.polimi.modaclouds.space4cloud.db;

import it.polimi.modaclouds.space4cloud.utils.LoggerHelper;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;

// TODO: Auto-generated Javadoc
/**
 * Provides the MySQL Database Connector.
 * 
 * @author Davide Franceschelli
 * 
 */
public class DatabaseConnector {


	/** The connection */
	private static Connection conn=null;
	protected static final Logger logger = LoggerHelper.getLogger(DatabaseConnector.class);

	
	
	public static void initConnection(InputStream confFileStream) throws SQLException, IOException{

		//default values overritten by the ones in the configuration file
//		String url = "jdbc:mysql://localhost:3306/";
//		String dbName = "cloud";
		String url = "jdbc:mysql://109.231.122.191:3306/";		
		String dbName = "cloud";
		String driver = "com.mysql.jdbc.Driver";
		String userName = "moda";
		String password = "modaclouds";
		
		
		
		Properties properties = new Properties();
		properties.load(confFileStream);		
		url=properties.getProperty("URL");
		dbName=properties.getProperty("DBNAME");
		driver=properties.getProperty("DRIVER");
		userName=properties.getProperty("USERNAME");
		password=properties.getProperty("PASSWORD");
		
		logger.debug("Data base connestion settings:");
		logger.debug("\turl:"+url);
		logger.debug("\tname:"+dbName);
		logger.debug("\tuser:"+userName);
		logger.debug("\tpass:"+password);
		

		try {
			Class.forName(driver).newInstance();
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e) {
			logger.error("Unable to find the JDBC driver",e);
		}
		conn = DriverManager
				.getConnection(url + dbName, userName, password);
		if(conn != null)
			logger.info("Connection with the database established");
		else
			logger.error("Error in connecting to the database");
	}

	/**
	 * Returns the Connection to the MySQL database.
	 * 
	 * @return the Connection instance.
	 * @throws SQLException 
	 */
	public static Connection getConnection(){
		return conn;
	}
}

