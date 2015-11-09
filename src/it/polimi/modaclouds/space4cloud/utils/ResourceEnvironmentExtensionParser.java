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
package it.polimi.modaclouds.space4cloud.utils;

import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import it.polimi.modaclouds.qos_models.schema.CloudService;
import it.polimi.modaclouds.qos_models.schema.ReplicaElement;
import it.polimi.modaclouds.qos_models.schema.ResourceContainer;
import it.polimi.modaclouds.qos_models.schema.ResourceModelExtension;
import it.polimi.modaclouds.qos_models.util.XMLHelper;

public class ResourceEnvironmentExtensionParser {	
	protected Map<String, String> serviceTypes = new HashMap<>();
	protected Map<String, String> providers = new HashMap<>();
	protected Map<String, String> serviceNames = new HashMap<>();
	protected Map<String, String> containerNames = new HashMap<>();
	protected Map<String, String> instanceSizes = new HashMap<>();
	protected Map<String, int[]> instanceReplicas = new HashMap<>();
	protected Map<String, String> serviceLocations = new HashMap<>();
	protected static final int HOURS = 24;
	//private static final Logger logger = Logger.getLogger(ResourceEnvironmentExtensionParser.class);


	public ResourceEnvironmentExtensionParser() throws ResourceEnvironmentLoadingException {


		// load the model
		ResourceModelExtension loadedExtension;
		try {
			loadedExtension = XMLHelper.deserialize(Paths.get(Configuration.RESOURCE_ENVIRONMENT_EXTENSION).toUri().toURL(),ResourceModelExtension.class);
		} catch (MalformedURLException | JAXBException | SAXException e) {
			throw new ResourceEnvironmentLoadingException("Could not load the resource enviroment extension file: "+Configuration.RESOURCE_ENVIRONMENT_EXTENSION,e);
		}

		// fill structures
		for (ResourceContainer container : loadedExtension
				.getResourceContainer()) {
			// String id = container.getId();
			String provider = container.getProvider();
			String id = container.getId() + (provider!=null?provider:"");
			providers.put(id, provider);
			String name = container.getName();
			containerNames.put(id, name);	
			if (container.getCloudElement() != null) {
				CloudService resource = container.getCloudElement();
				serviceTypes.put(id, resource.getServiceType());
				serviceNames.put(id, resource.getServiceName());		
				instanceSizes.put(id, resource.getResourceSizeID());

				if (resource.getLocation() != null) {
					String location = resource.getLocation().getRegion();
					setRegion(container.getProvider(), location);
				}

				int[] replicas = new int[HOURS];
				for (int i = 0; i < HOURS; i++) {
					replicas[i] = 1;
				}
				if (resource.getReplicas() != null) {
					for (ReplicaElement element : resource.getReplicas()
							.getReplicaElement()) {
						replicas[element.getHour()] = element.getValue();
					}
				}
				instanceReplicas.put(id, replicas);
			}

		}
	}


	public Map<String, int[]> getInstanceReplicas() {
		return instanceReplicas;
	}

	public Map<String, String> getInstanceSize() {
		return instanceSizes;
	}

	public Map<String, String> getProviders() {
		return providers;
	}

	public String getRegion() {
		if (serviceLocations.isEmpty())
			return null;

		String location = serviceLocations.values().iterator().next();

		for (Iterator<String> locationsIter = serviceLocations.values()
				.iterator(); locationsIter.hasNext();)
			if (!location.equals(locationsIter.next())) {
				System.err
						.println("Multiple regions specified in the resource container extension!");
				return null;
			}
		return location;
	}

	public String getRegion(String provider) {
		String value = serviceLocations.get(provider);
		if (value != null && value.equals("not-valid"))
			return null;
		return value;
	}

	public Map<String, String> getServiceName() {
		return serviceNames;
	}

	public Map<String, String> getServiceType() {
		return serviceTypes;
	}
	

	public void setRegion(String provider, String value) {
		serviceLocations.put(provider, value);
	}


	public Map<String, String> getContainerNames() {
		return containerNames;
	}


}
