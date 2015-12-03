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
/**
 * 
 */
package it.polimi.modaclouds.space4cloud.db;

import it.polimi.modaclouds.resourcemodel.cloud.CloudFactory;
import it.polimi.modaclouds.resourcemodel.cloud.CloudPlatform;
import it.polimi.modaclouds.resourcemodel.cloud.CloudProvider;
import it.polimi.modaclouds.resourcemodel.cloud.CloudResource;
import it.polimi.modaclouds.resourcemodel.cloud.IaaS_Service;
import it.polimi.modaclouds.resourcemodel.cloud.PaaS_Service;
import it.polimi.modaclouds.space4cloud.optimization.evaluation.EvaluationServer;
import it.polimi.modaclouds.space4cloud.utils.EMF;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michele Ciavotta This class loads the providers from the database and
 *         puts them into an HashMap dictionary.
 * 
 * @see CloudProvidersList
 */
public class CloudProvidersDictionary {

	/** The provider db connectors. */
	private final Map<String, ProviderDBConnector> providerDBConnectors;

	protected static final Logger logger = LoggerFactory.getLogger(CloudProvidersDictionary.class);
	/**
	 * Initialize the instance retrieving the available Cloud Providers from the
	 * database. For each Cloud Provider, a Provider DB Connector is created and
	 * added to the HashMap of available Provider DB Connectors.
	 * 
	 * @throws SQLException
	 * 
	 * @see ProviderDBConnector
	 * @see CloudProvider
	 */
	public CloudProvidersDictionary() throws SQLException {
		/* dictionary creation */
		Map<String, ProviderDBConnector> dict = new HashMap<>();

		Connection db = DatabaseConnector.getConnection();
		ResultSet rs = db.createStatement().executeQuery(
				"select * from cloudprovider");
		CloudFactory cf = new EMF().getCloudFactory();
		CloudProvider cp;
		while (rs.next()) {
			cp = cf.createCloudProvider();
			cp.setId(rs.getInt(1));
			cp.setName(rs.getString(2));
			/* watch out: two providers cannot have the same name */
			dict.put(cp.getName(), new ProviderDBConnector(cp));
		}

		providerDBConnectors = dict;
		
		//force creation of sets
		for(String providerName:providerDBConnectors.keySet()){
			ProviderDBConnector connector = providerDBConnectors.get(providerName);
			connector.getIaaSServices();
			connector.getPaaSServices();
		}
		
		//debug purposes
		for(String providerName:providerDBConnectors.keySet()){
			ProviderDBConnector connector = providerDBConnectors.get(providerName);
			for( IaaS_Service service:connector.getIaaSServices()){
				for( CloudResource resource:service.getComposedOf()){
					if((resource.getHasCost() == null || resource.getHasCost().size() == 0) && resource.getHasCostProfile() == null)
						logger.error("Resource with id: "+resource.getId()+ "and type: "+resource.getType()+" does not have any cost or cost profile associated!");
				}
			}
		}

	}

	/**
	 * Returns the HashMap of the available Provider DB Connectors.
	 * 
	 * @return a HashMap of ProviderDBConnector elements.
	 * @see ProviderDBConnector
	 */
	public Map<String, ProviderDBConnector> getProviderDBConnectors() {
		return providerDBConnectors;
	}

}
