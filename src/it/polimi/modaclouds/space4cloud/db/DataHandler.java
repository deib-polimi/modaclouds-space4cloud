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
import it.polimi.modaclouds.resourcemodel.cloud.CloudResource;
import it.polimi.modaclouds.resourcemodel.cloud.Cost;
import it.polimi.modaclouds.resourcemodel.cloud.IaaS_Service;
import it.polimi.modaclouds.resourcemodel.cloud.V_Memory;
import it.polimi.modaclouds.resourcemodel.cloud.VirtualHWResource;
import it.polimi.modaclouds.resourcemodel.cloud.VirtualHWResourceType;
import it.polimi.modaclouds.space4cloud.optimization.bursting.PrivateCloud;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.CloudService;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Compute;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.IaaS;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michele Ciavotta the aim of this class is to provide an object able
 *         to retrieve the data related to a certain provider and service
 */
public class DataHandler {
	//	private static final Logger logger = LoggerHelper.getLogger(DataHandler.class);

	private static final Logger logger=LoggerFactory.getLogger(DataHandler.class);
	private CloudProvidersDictionary cloudProviders;

	/**
	 * Instantiates a new data handler. it also charges data from the database
	 * 
	 * @param provider
	 *            the provider
	 * @throws SQLException
	 */
	public DataHandler() throws SQLException {
		//connect to the database
		cloudProviders = new CloudProvidersDictionary();
	}
	public static void main(String[] args) {
		DataHandler handler = null; 

		try {
			DatabaseConnector.initConnection(null);
			handler = DataHandlerFactory.getHandler();
		} catch (DatabaseConnectionFailureExteption | SQLException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//String provider="Amazon";
		Set<String> providers = handler.getCloudProviders();
		for(String provider:providers){
			logger.info("Provider: "+provider);
			List<String> services = handler.getServices(provider, "Compute");
			for(String service:services){
				logger.info("\t"+service);
			}
		}

		logger.info("End");


	}
	
	/**
	 * Gets the amount of memory of the the cloud resource.
	 * 
	 * @param provider
	 *            the provider
	 * @param serviceName
	 *            the iass service name
	 * @param resourceName
	 *            the resource name
	 * @return the amount of ram
	 */
	public Integer getAmountMemory(String provider, String serviceName,
			String resourceName) {
		logger.trace("Getting amount of memory provider: "+provider+" serviceName: "+serviceName+" resourceName: "+resourceName);
		CloudResource cr = getCloudResource(provider, serviceName, resourceName);

		for (VirtualHWResource i : cr.getComposedOf()) {
			if (i.getType() == VirtualHWResourceType.MEMORY) {
				/* a cast to V_Memory interface is needed */
				return ((V_Memory) i).getSize();
			}
		}
		/* In case of errors */
		return -1;

	}

	public Set<String> getCloudProviders() {
		return cloudProviders.getProviderDBConnectors().keySet();
	}
	
	public static final int MAX_ATTEMPTS = 2;
	
	public static final int MAX_TOTAL_RESETS = 10;
	
	private int resets = 0;
	
	private void resetDatabase() {
		if (resets < MAX_TOTAL_RESETS) {
			try {
				cloudProviders = new CloudProvidersDictionary();
				logger.debug("Database resetted!");
				resets++;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public CloudResource getCloudResource(String provider, String serviceName,
			String resourceName) {
		logger.trace("Getting cloud resource provider: "+provider+" serviceName: "+serviceName+" resourceName: "+resourceName);
		for (int attempt = 1; attempt <= MAX_ATTEMPTS; ++attempt) {
			
			CloudResource res = getCloudResourceInternal(provider, serviceName, resourceName);
			if (res != null)
				return res;
		
			resetDatabase();
		}
		
		return null;
	}
	
	private CloudResource getCloudResourceInternal(String provider, String serviceName,
			String resourceName) {
		
		ProviderDBConnector pdb = cloudProviders
				.getProviderDBConnectors().get(provider); // provider
		
		List<CloudResource> cloudResourceList = pdb
				.getIaaSServicesHashMap().get(serviceName) // service
				.getComposedOf();

		// TODO: Controllare questa cosa :(
		for (CloudResource cr : cloudResourceList) {
			if (cr.getName().equals(resourceName)
					&&(cr.getHasCost() != null)
					&& (cr.getHasCost().size() > 0))
					return cr;
		}
		
		return null;
	}
	
	public List<String> getCloudResourceSizes(String provider,
			String serviceName) {
		logger.trace("Getting resource sized provider: "+provider+" serviceName: "+serviceName);
		for (int attempt = 1; attempt <= MAX_ATTEMPTS; ++attempt) {
			
			List<String> res = getCloudResourceSizesInternal(provider, serviceName);
			if (res != null && res.size() > 0)
				return res;
		
			resetDatabase();
		}
		
		return new ArrayList<>();
	}

	private List<String> getCloudResourceSizesInternal(String provider,
			String serviceName) {
		List<CloudResource> cloudResourceList = cloudProviders
				.getProviderDBConnectors().get(provider) // provider
				.getIaaSServicesHashMap().get(serviceName) // service
				.getComposedOf();

		List<String> names = new ArrayList<String>(cloudResourceList.size());
		for (CloudResource tmp : cloudResourceList) {
			names.add(tmp.getName());
		}
		return names;
	}

	/**
	 * Gets the cost specification of the resource.
	 * 
	 * @param provider
	 *            the provider
	 * @param serviceName
	 *            the iass service name
	 * @param resourceName
	 *            the resource name
	 * @return the cost
	 */
	public EList<Cost> getCost(String provider, String serviceName,
			String resourceName) {
		CloudResource cr = getCloudResource(provider, serviceName, resourceName);
		return cr.getHasCost();

	}

	/**
	 * Build a IaaS resource from a cloud resource, currently supports only
	 * Compute instances
	 * 
	 * @param service
	 * @param cr
	 * @return IaaS representation of the cloud resource object
	 */
	private IaaS getIaaSfromCloudResource(CloudService service, CloudResource cr) {

		double speed = 0;
		int numberOfCores = 0;
		int ram = 0;

		EList<VirtualHWResource> hw = cr.getComposedOf();
		for (VirtualHWResource vhw : hw) {
			if (vhw.getType() == VirtualHWResourceType.CPU) {
				speed = vhw.getProcessingRate(); // speed
				numberOfCores = vhw.getNumberOfReplicas();// number
			} else if (vhw.getType() == VirtualHWResourceType.MEMORY) {
				ram = ((V_Memory) vhw).getSize();
			}

		}
		return new Compute(	service.getProvider(), service.getServiceType(),
				service.getServiceName(), cr.getName(),
				((IaaS) service).getReplicas(), numberOfCores, speed, ram);
	}
	
	/**
	 * Gets the number of replicas.
	 * 
	 * @param provider
	 *            the provider
	 * @param iassServiceName
	 *            the iass service name
	 * @param resourceName
	 *            the resource name
	 * @return the number of replicas
	 */
	public Integer getNumberOfReplicas(String provider, String serviceName,
			String resourceName) {
		logger.trace("Getting number of replicas with provider: "+provider+" serviceName: "+serviceName+" resourceName: "+resourceName);
		CloudResource cr = getCloudResource(provider, serviceName, resourceName);
		for (VirtualHWResource i : cr.getComposedOf()) {
			if (i.getType() == VirtualHWResourceType.CPU) {
				return i.getNumberOfReplicas();
			}
		}
		/* In case of errors */
		return -1;

	}
	
	
	public double getProcessingRate(String provider, String serviceName,
			String resourceName) {
		logger.trace("Getting Processing rate for resource with provider: "+provider+" serviceName: "+serviceName+" resourceName: "+resourceName);
		for (int attempt = 1; attempt <= MAX_ATTEMPTS; ++attempt) {
			
			double res = getProcessingRateInternal(provider, serviceName, resourceName);
			if (res > -1)
				return res;
		
			resetDatabase();
		}
		
		return -1;
	}
	
	/**
	 * Gets the processing rate of the cpus.
	 * 
	 * @param provider
	 *            the id provider
	 * @param serviceName
	 *            the id iass service
	 * @param resourceName
	 *            the id resource
	 * @return the speed
	 */
	private double getProcessingRateInternal(String provider, String serviceName,
			String resourceName) {		
		List<CloudResource> cloudResourceList = cloudProviders
				.getProviderDBConnectors().get(provider) // provider
				.getIaaSServicesHashMap().get(serviceName) // service
				.getComposedOf();

		CloudResource cr = null;
		// here we lose the OS type distinction
		for (CloudResource tmp : cloudResourceList) {
			if (tmp.getName().equals(resourceName)) {
				cr = tmp;
			}
		}

		for (VirtualHWResource i : cr.getComposedOf()) {
			if (i.getType() == VirtualHWResourceType.CPU) {
				return i.getProcessingRate();
			}
		}
		return -1;

	}
	
	public List<IaaS> getSameServiceResource(CloudService service, String region) {
		logger.trace("Getting same service resource for provider: "+service.getProvider()+" serviceName: "+service.getServiceName()+" region: "+region);
		for (int attempt = 1; attempt <= MAX_ATTEMPTS; ++attempt) {
			
			List<IaaS> res = getSameServiceResourceInternal(service, region);
			if (res != null && res.size() > 0)
				return res;
		
			resetDatabase();
		}
		
		return new ArrayList<>();
	}

	/**
	 * returns a list of IaaS services with the provider and sarvice name equals
	 * to those of provided CloudService
	 * 
	 * @param service
	 *            - the original cloud service
	 * @param region
	 *            - the region
	 * @return a list of IaaS services
	 */
	private List<IaaS> getSameServiceResourceInternal(CloudService service, String region) {
		List<IaaS> resources = new ArrayList<>();
		for (CloudResource cr : cloudProviders.getProviderDBConnectors()
				.get(service.getProvider()) // provider
				.getIaaSServicesHashMap().get(service.getServiceName()) // service
				.getComposedOf()) {

			IaaS iaas = getIaaSfromCloudResource(service, cr);
			for (Cost cost : cr.getHasCost()) {
				// if the iaas has not been already inserted AND (the region has
				// not been specified OR it has the same region)
				if (!resources.contains(iaas)
						&& (region == null || cost.getRegion() == null || cost
						.getRegion().equals(region))) {
					resources.add(iaas);
				}
			}
		}
		return resources;
	}
	
	public List<String> getServices(String provider, String serviceType) {
		logger.trace("Getting Services : "+provider+" serviceType: "+serviceType);
		for (int attempt = 1; attempt <= MAX_ATTEMPTS; ++attempt) {
			
			List<String> res = getServicesInternal(provider, serviceType);
			if (res != null && res.size() > 0)
				return res;
		
			resetDatabase();
		}
		
		return new ArrayList<>();
	}

	private List<String> getServicesInternal(String provider, String serviceType) {

		CloudResource expectedResource = null;
		switch (serviceType) {
		case "Compute":
			expectedResource = CloudFactory.eINSTANCE.createCompute();
			break;
		default:
			break;
		}
		List<IaaS_Service> iaasServices = cloudProviders
				.getProviderDBConnectors().get(provider).getIaaSServices();
		logger.trace("getting services of type "+serviceType+" from "+provider);
		logger.trace("found "+iaasServices.size()+" services");
		for (IaaS_Service service : iaasServices) 
			logger.trace("service: "+service.getName()+" class: "+service.getClass());
		List<String> filteredServices = new ArrayList<String>();
		for (IaaS_Service service : iaasServices) {
			List<CloudResource> resources = service.getComposedOf();
			// if there is any resource and it is of a subtype of the expected
			// one add the service to the list of filtered services
			// (we assume here that each service provide resources of the same
			// type
			logger.trace("Empty resources: "+resources.isEmpty());
			if (!resources.isEmpty()){
				logger.trace("fisrt element class: "+resources.get(0).getClass());
				logger.trace("Expected Class: "+expectedResource.getClass());
				logger.trace("is assignable: "+resources.get(0).getClass()
						.isAssignableFrom(expectedResource.getClass()));
			}
			if (!resources.isEmpty()
					&& resources.get(0).getClass()
					.isAssignableFrom(expectedResource.getClass())) {
				logger.trace("adding service: "+service.getName());
				filteredServices.add(service.getName());
			}
		}
		return filteredServices;
	}
	
	public double getAvailability(String provider) {
		logger.trace("Getting availability provider: "+provider);
		if (provider.indexOf(PrivateCloud.BASE_PROVIDER_NAME) > -1)
			return 0.95;
		
		ProviderDBConnector pdb = cloudProviders
				.getProviderDBConnectors().get(provider); // provider
		
		return pdb.getAvailability();
	}
	
}
