/*
 * 
 */
package it.polimi.modaclouds.space4cloud.db;

import java.sql.Connection;
import java.sql.DriverManager;

// TODO: Auto-generated Javadoc
/**
 * Provides the MySQL Database Connector.
 * 
 * @author Davide Franceschelli
 * 
 */
public class DatabaseConnector {

	/** The conn. */
	private Connection conn;

	/**
	 * Creates a new Database Connector instance.
	 */
	public DatabaseConnector() {
//		String url = "jdbc:mysql://localhost:3306/";
//		String dbName = "cloud";
//		String driver = "com.mysql.jdbc.Driver";
//		String userName = "moda";
//		String password = "modaclouds";
		String url = "jdbc:mysql://109.231.122.191:3306/";
		String dbName = "cloud";
		String driver = "com.mysql.jdbc.Driver";
		String userName = "moda";
		String password = "modaclouds";

		try {			
			Class.forName(driver).newInstance();
			conn = DriverManager
					.getConnection(url + dbName, userName, password);
		} catch (Exception e) {
			e.printStackTrace();
			conn = null;
		}
	}

	/**
	 * Returns the Connection to the MySQL database.
	 * 
	 * @return the Connection instance.
	 */
	public Connection getConnection() {
		return conn;
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		DatabaseConnector dbc = new DatabaseConnector();
		try {
			System.out.println(dbc.getConnection().getCatalog());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
