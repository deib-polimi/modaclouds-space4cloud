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

import it.polimi.modaclouds.resourcemodel.cloud.CloudResource;
import it.polimi.modaclouds.resourcemodel.cloud.Cost;
import it.polimi.modaclouds.resourcemodel.cloud.V_Memory;
import it.polimi.modaclouds.resourcemodel.cloud.VirtualHWResource;
import it.polimi.modaclouds.resourcemodel.cloud.VirtualHWResourceType;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.CloudService;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Compute;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.IaaS;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;

/**
 * @author Michele Ciavotta
 * the aim of this class is to provide an object able to retrieve the data related to a certain provider and service
 */
public class DataHandler {

	private final CloudProvidersDictionary cloudProviders;

	/**
	 * Instantiates a new data handler.
	 * it also charges data from the database
	 * @param provider the provider
	 */
	public DataHandler(){

		/*This initialization loads all the info about the providers from the database*/
		cloudProviders = new CloudProvidersDictionary();
	}


	/* questi commenti andranno cancellati:
	 * Compito del DataHandler è quello di gestire i dati che sono stati caricati dalla base di dati
	 * utilizza un accesso Lazy. I provider vengono tutti caricati ma i loro servizi e le relative risorse vengono 
	 * caricati soltanto quando si esegue un accesso a quel provider. 
	 * */


	/**
	 * Gets the processing rate of the cpus.
	 *
	 * @param provider the id provider
	 * @param serviceName the id iass service
	 * @param resourceName the id resource
	 * @return the speed
	 */
	public double getProcessingRate(String provider, String serviceName,  String resourceName){

		try {
			List<CloudResource> cr_list = cloudProviders
					.providerDBConnectors
					.get(provider)  // provider
					.getIaaSServicesHashMap()
					.get(serviceName) //service
					.getComposedOf();

			CloudResource cr = null;
			//here we lose the OS type distinction
			for(CloudResource tmp:cr_list)
				if(tmp.getName().equals(resourceName))
					cr=tmp;			

			for (VirtualHWResource i : cr.getComposedOf()) {

				if (i.getType() == VirtualHWResourceType.CPU) {

					return i.getProcessingRate();
				}
			}

			return -1; /*In case of errors*/
		} catch (Exception e) {

			e.printStackTrace();
			return -1;
		}

	}

	/**
	 * returns a list of IaaS services with the provider and sarvice name equals to those of provided CloudService 
	 * @param service - the original cloud service
	 * @param region - the region
	 * @return a list of IaaS services
	 */
	public List<IaaS> getSameServiceResource(CloudService service, String region){
		ArrayList<IaaS> resources = new ArrayList<>();
		for(CloudResource cr:cloudProviders
				.providerDBConnectors
				.get(service.getProvider())  // provider
				.getIaaSServicesHashMap()
				.get(service.getServiceName()) //service
				.getComposedOf()){

			IaaS iaas = IaaSfromCloudResource(service, cr);
			for(Cost cost:cr.getHasCost()){
				if(cost.getRegion().equals(region) && !resources.contains(iaas))
					resources.add(iaas);
			}
		}
		return resources;
	}


	/**
	 * Build a IaaS resource from a cloud resource, currently supports only Compute instances
	 * @param service 
	 * @param cr
	 * @return IaaS representation of the cloud resource object
	 */
	private IaaS IaaSfromCloudResource(CloudService service, CloudResource cr) {

		double speed= 0;
		int numberOfCores = 0;
		int ram = 0; 

		EList<VirtualHWResource> hw = cr.getComposedOf();
		for (VirtualHWResource vhw : hw) {
			if (vhw.getType() == VirtualHWResourceType.CPU ) {
				speed = vhw.getProcessingRate(); //speed
				numberOfCores = vhw.getNumberOfReplicas();// number
			} else if(vhw.getType() == VirtualHWResourceType.MEMORY)
				ram = ((V_Memory) vhw).getSize();

		}
		return new Compute(service.getName(), 
				service.getId(), 
				service.getProvider(), 
				service.getServiceType(),
				service.getServiceName(), 
				cr.getName(), 
				((IaaS)service).getReplicas(), 
				numberOfCores, 
				speed, 
				ram);
	}


	/**
	 * Gets the number of replicas.
	 *
	 * @param provider the provider
	 * @param iassServiceName the iass service name
	 * @param resourceName the resource name
	 * @return the number of replicas
	 */
	public Integer getNumberOfReplicas(String provider, String serviceName, String resourceName){
		try {
			CloudResource cr = getCloudResource(provider, serviceName, resourceName);

			for (VirtualHWResource i : cr.getComposedOf()) {

				if (i.getType() == VirtualHWResourceType.CPU) {

					return i.getNumberOfReplicas();
				}
			}

			return -1; /*In case of errors*/
		} catch (Exception e) {

			e.printStackTrace();
			return -1;
		}
	}
	/**
	 * Gets the amount of memory of the the cloud resource.
	 *
	 * @param provider the provider
	 * @param serviceName the iass service name
	 * @param resourceName the resource name
	 * @return the amount of ram 
	 */
	public Integer getAmountMemory(String provider, String serviceName, String resourceName){
		try {

			CloudResource cr = getCloudResource(provider, serviceName, resourceName);

			for (VirtualHWResource i : cr.getComposedOf()) {

				if (i.getType() == VirtualHWResourceType.MEMORY) {

					return ((V_Memory) i).getSize() ; /*a cast to V_Memory interface is needed*/ 
				}
			}

			return -1; /*In case of errors*/
		} catch (Exception e) {

			e.printStackTrace();
			return -1;
		}
	}


	/**
	 * Gets the cost specification of the resource.
	 *
	 * @param provider the provider
	 * @param serviceName the iass service name
	 * @param resourceName the resource name
	 * @return the cost
	 */
	public EList<Cost> getCost(String provider, String serviceName, String resourceName){
		CloudResource cr = getCloudResource(provider, serviceName, resourceName);			
		return cr.getHasCost();

	}


	public CloudResource getCloudResource(String provider, String serviceName,
			String resourceName) {

		List<CloudResource> cr_list = cloudProviders
				.providerDBConnectors
				.get(provider)  // provider
				.getIaaSServicesHashMap()
				.get(serviceName) //service
				.getComposedOf();

		for(CloudResource cr:cr_list)
			if(cr.getName().equals(resourceName))
				return cr;

		return null;
	}


	public List<String> getCloudResourceSizes(String provider, String serviceName) {
		List<CloudResource> cr_list = cloudProviders
				.providerDBConnectors
				.get(provider)  // provider
				.getIaaSServicesHashMap()
				.get(serviceName) //service
				.getComposedOf();

		List<String> names = new ArrayList<String>(cr_list.size()); 
		for(CloudResource tmp:cr_list)			
			names.add(tmp.getName());
		return names;
	}




}
