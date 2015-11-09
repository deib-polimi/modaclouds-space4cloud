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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.modaclouds.resourcemodel.cloud.CloudFactory;
import it.polimi.modaclouds.resourcemodel.cloud.CloudProvider;
import it.polimi.modaclouds.space4cloud.utils.EMF;

// TODO: Auto-generated Javadoc
/**
 * Provides the list of the Database Connectors associated to the available
 * Cloud Providers.
 * 
 * @author Davide Franceschelli
 * @see ProviderDBConnector
 * @see CloudProvider
 */
public class CloudProvidersList {

	/** The provider db connectors. */
	private final List<ProviderDBConnector> providerDBConnectors;

	/**
	 * Initialize the instance retrieving the available Cloud Providers from the
	 * database. For each Cloud Provider, a Provider DB Connector is created and
	 * added to the list of available Provider DB Connectors.
	 * 
	 * @throws SQLException
	 * 
	 * @see ProviderDBConnector
	 * @see CloudProvider
	 */
	public CloudProvidersList() throws SQLException {
		List<ProviderDBConnector> list = new ArrayList<ProviderDBConnector>();

		Connection db = DatabaseConnector.getConnection();
		ResultSet rs = db.createStatement().executeQuery(
				"select * from cloudprovider");
		CloudFactory cf = new EMF().getCloudFactory();
		CloudProvider cp;
		while (rs.next()) {
			cp = cf.createCloudProvider();
			cp.setId(rs.getInt(1));
			cp.setName(rs.getString(2));
			list.add(new ProviderDBConnector(cp));
		}

		providerDBConnectors = list;
	}

	/**
	 * Returns the list of the available Provider DB Connectors.
	 * 
	 * @return a List of ProviderDBConnector elements.
	 * @see ProviderDBConnector
	 */
	public List<ProviderDBConnector> getProviderDBConnectors() {
		return providerDBConnectors;
	}

}
