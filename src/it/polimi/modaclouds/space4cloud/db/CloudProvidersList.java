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

import it.polimi.modaclouds.cloudmetamodel.cloud.CloudFactory;
import it.polimi.modaclouds.cloudmetamodel.cloud.CloudProvider;
import it.polimi.modaclouds.space4cloud.utils.EMF;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

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
	public final List<ProviderDBConnector> providerDBConnectors;

	/**
	 * Initialize the instance retrieving the available Cloud Providers from the
	 * database. For each Cloud Provider, a Provider DB Connector is created and
	 * added to the list of available Provider DB Connectors.
	 * 
	 * @see ProviderDBConnector
	 * @see CloudProvider
	 */
	public CloudProvidersList() {
		List<ProviderDBConnector> list = new ArrayList<ProviderDBConnector>();
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
				list.add(new ProviderDBConnector(cp));
			}
		} catch (Exception e) {
			e.printStackTrace();
			list = null;
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

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		CloudProvidersList cpl = new CloudProvidersList();
		for (ProviderDBConnector pdb : cpl.getProviderDBConnectors())
			System.out.println(pdb.getProvider());
	}
}
