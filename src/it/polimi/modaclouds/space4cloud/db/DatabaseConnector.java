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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
	private Connection conn;

	protected static final Logger logger = LoggerHelper.getLogger(DatabaseConnector.class);

	/**
	 * Creates a new Database Connector instance.
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public DatabaseConnector() throws SQLException{
		 String url = "jdbc:mysql://localhost:3306/";
		 String dbName = "cloud_full";
		 String driver = "com.mysql.jdbc.Driver";
		 String userName = "moda";
		 String password = "modaclouds";
//		String url = "jdbc:mysql://109.231.122.191:3306/";
//		String dbName = "cloud";
//		String driver = "com.mysql.jdbc.Driver";
//		String userName = "moda";
//		String password = "modaclouds";

		try {
			Class.forName(driver).newInstance();
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e) {
			logger.error("Unable to find the JDBC driver",e);
		}
		conn = DriverManager
				.getConnection(url + dbName, userName, password);
	}

	/**
	 * Returns the Connection to the MySQL database.
	 * 
	 * @return the Connection instance.
	 */
	public Connection getConnection() {
		return conn;
	}
}
