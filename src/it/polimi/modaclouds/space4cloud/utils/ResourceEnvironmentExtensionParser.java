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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ResourceEnvironmentExtensionParser {

	protected File extension;
	protected Map<String, String> serviceTypes = new HashMap<>();
	protected Map<String, String> providers = new HashMap<>();
	protected Map<String, String> serviceNames = new HashMap<>();
	protected Map<String, String> instanceSizes = new HashMap<>();
	protected Map<String, int[]> instanceReplicas = new HashMap<>();
	protected Map<String, String> serviceLocations = new HashMap<>();
	protected static final int HOURS = 24;

	DocumentBuilderFactory dbFactory;
	DocumentBuilder dBuilder;
	Document doc;


	public ResourceEnvironmentExtensionParser(File extensionFile,boolean parse)
			throws ParserConfigurationException, SAXException, IOException {

		this.extension = extensionFile;
		if(parse)
			parse();
	}

	public ResourceEnvironmentExtensionParser(File extensionFile) throws ParserConfigurationException, SAXException, IOException{
		this(extensionFile,true);
	}

	private void parse() throws ParserConfigurationException, SAXException, IOException {
		dbFactory = DocumentBuilderFactory.newInstance();
		dBuilder = dbFactory.newDocumentBuilder();
		doc = dBuilder.parse(extension);
		doc.getDocumentElement().normalize();
		// parse resource containers
		NodeList list = doc.getElementsByTagName("resourceContainer");
		for (int i = 0; i < list.getLength(); i++) {
			Node n = list.item(i);
			Element n_elem = (Element) n;

			// get the resource ID
			String resourceId = n_elem.getAttribute("id");

			// get the provider
			String provider = null;
			if (n_elem.hasAttribute("provider"))
				provider = n_elem.getAttribute("provider");

			// get the service type
			String type = null;
			String serviceName = null;
			if (n_elem.getElementsByTagName("cloudResource").getLength() == 1) {
				Element cloudResourceElement = (Element) n_elem
						.getElementsByTagName("cloudResource").item(0);
				type = cloudResourceElement.getAttributes()
						.getNamedItem("serviceType").getNodeValue();
				if (cloudResourceElement.hasAttribute("serviceName")) {
					serviceName = cloudResourceElement.getAttributes()
							.getNamedItem("serviceName").getNodeValue();
				}
				// get the location if provided
				if (cloudResourceElement.hasChildNodes()) {
					NodeList resourceElementChilds = cloudResourceElement
							.getChildNodes();
					for (int j = 0; j < resourceElementChilds.getLength(); j++)
						if (resourceElementChilds.item(j).getNodeName()
								.equals("location"))
							serviceLocations.put(
									resourceId,
									resourceElementChilds.item(j)
									.getAttributes()
									.getNamedItem("region")
									.getNodeValue());
				}

			} else {
				Node cloudPlatformElement = n_elem.getElementsByTagName(
						"cloudPlatform").item(0);
				type = cloudPlatformElement.getAttributes()
						.getNamedItem("serviceType").getNodeValue();
				serviceName = cloudPlatformElement.getAttributes()
						.getNamedItem("serviceName").getNodeValue();
			}

			// get the instance size
			String size = null;
			if (n_elem.getElementsByTagName("resourceSizeID").getLength() == 1)
				size = n_elem.getElementsByTagName("resourceSizeID").item(0)
				.getTextContent();

			// get the number of replicas if specified
			int[] replicas = new int[HOURS];
			for (int j = 0; j < HOURS; j++)
				replicas[j] = 1;

			if (n_elem.getElementsByTagName("replicas").getLength() == HOURS) {
				NodeList replicaNodes = ((Element) n_elem.getElementsByTagName(
						"replicas").item(0)).getElementsByTagName("replica");
				for (int j = 0; j < replicaNodes.getLength(); j++) {
					int hour = Integer.parseInt(replicaNodes.item(j)
							.getAttributes().getNamedItem("hour")
							.getTextContent());
					int value = Integer.parseInt(replicaNodes.item(j)
							.getAttributes().getNamedItem("value")
							.getTextContent());
					replicas[hour - 1] = value;
				}
			}
			instanceReplicas.put(resourceId, replicas);
			providers.put(resourceId, provider);
			serviceTypes.put(resourceId, type);
			serviceNames.put(resourceId, serviceName);
			instanceSizes.put(resourceId, size);
		}
	}

	public Map<String, String> getServiceType() {
		return serviceTypes;
	}

	public Map<String, String> getProviders() {
		return providers;
	}

	public Map<String, String> getInstanceSize() {
		return instanceSizes;
	}

	public void addResourceContainer(String id, String ServiceType,
			boolean IaaS, String InstanceType, String provider) {

		NodeList list = doc.getElementsByTagName("ResourceContainerExtensions");

		// create the resource container
		Element resourceContainer = doc.createElement("ResourceContainer");
		list.item(0).appendChild(resourceContainer);

		// fill the attributes
		resourceContainer.setAttribute("id", id);
		resourceContainer.setAttribute("Provider", provider);

		// add the servicetype
		if (IaaS) {
			Element service = doc.createElement("Infrastructure");
			service.appendChild(doc.createTextNode(ServiceType));
			resourceContainer.appendChild(service);
		} else {
			Element service = doc.createElement("Platform");
			service.appendChild(doc.createTextNode(ServiceType));
			resourceContainer.appendChild(service);
		}

		// add the instanceType
		Element instance = doc.createElement("ResourceSizeID");
		instance.appendChild(doc.createTextNode(InstanceType));
		resourceContainer.appendChild(instance);

		// TODO: what if the container is already there?

		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		try {
			Transformer transformer = transformerFactory.newTransformer();

			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(extension);

			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);

			transformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Map<String, String> getServiceName() {
		return serviceNames;
	}

	public Map<String, int[]> getInstanceReplicas() {
		return instanceReplicas;
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

}
