/**
 * 
 */
package it.polimi.modaclouds.space4cloud.db;

import it.polimi.modaclouds.cloudmetamodel.cloud.CloudFactory;
import it.polimi.modaclouds.cloudmetamodel.cloud.CloudProvider;
import it.polimi.modaclouds.space4cloud.utils.EMF;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;

/**
 * @author Michele Ciavotta
 * This class loads the providers from the database and puts them into an HashMap dictionary.
 * 
 * @see CloudProvidersList
 */
public class CloudProvidersDictionary {
	
	/** The provider db connectors. */
	public final HashMap<String, ProviderDBConnector> providerDBConnectors;
	
	/**
	 * Initialize the instance retrieving the available Cloud Providers from the
	 * database. For each Cloud Provider, a Provider DB Connector is created and
	 * added to the HashMap of available Provider DB Connectors.
	 * 
	 * @see ProviderDBConnector
	 * @see CloudProvider
	 */
	public CloudProvidersDictionary() {
		/*dictionary creation*/
		HashMap<String, ProviderDBConnector> dict = new HashMap<>();
		
		try {
			Connection db = new DatabaseConnector().getConnection();
			ResultSet rs = db.createStatement().executeQuery(
					"select * from cloudprovider");
			CloudFactory cf = new EMF().getCloudFactory();
			CloudProvider cp;
			while (rs.next()) {
				cp = cf.createCloudProvider();
				cp.setId(rs.getInt(1));
				cp.setName(rs.getString(2));
				dict.put(cp.getName(), new ProviderDBConnector(cp)); /*watch out: two providers cannot have the same name*/
			}
		} catch (Exception e) {
			e.printStackTrace();
			dict = null;
		}
		providerDBConnectors = dict;
	}

	/**
	 * Returns the HashMap of the available Provider DB Connectors.
	 * 
	 * @return a HashMap of ProviderDBConnector elements.
	 * @see ProviderDBConnector
	 */
	public HashMap<String, ProviderDBConnector> getProviderDBConnectors() {
		return providerDBConnectors;
	}


}
