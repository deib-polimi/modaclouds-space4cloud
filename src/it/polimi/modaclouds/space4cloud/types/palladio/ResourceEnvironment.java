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
package it.polimi.modaclouds.space4cloud.types.palladio;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.polimi.modaclouds.space4cloud.types.ProcessingResourceT;
import it.polimi.modaclouds.space4cloud.types.SchedulingT;
import it.polimi.modaclouds.space4cloud.utils.DOM;

// TODO: Auto-generated Javadoc
/**
 * The Class ResourceEnvironment.
 */
public class ResourceEnvironment {

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		ResourceEnvironment re = new ResourceEnvironment(new File(
				System.getProperty("user.dir")
						+ "\\Palladio\\MyRE.resourceenvironment"));
		System.out.println(re.toString());
		ProcessingResource cpu = new ProcessingResource(
				ProcessingResourceT.CPU, SchedulingT.PS, 10000, 4, 10000, 2000);
		ProcessingResource hdd = new ProcessingResource(
				ProcessingResourceT.HDD, SchedulingT.FCFS, 100000000, 1,
				10000000, 2000);
		ResourceContainer rc1 = new ResourceContainer("AppServer");
		rc1.addProcessingResource(cpu);
		ResourceContainer rc2 = new ResourceContainer("DBServer");
		rc2.addProcessingResource(hdd);
		ResourceContainer rc3 = new ResourceContainer("XXX");
		// LinkingResource lr = new LinkingResource("LAN", LinkT.LAN,
		// 1000000000,
		// 0.002, 0.000001, rc1, rc2);
		re = new ResourceEnvironment();
		re = new ResourceEnvironment();
		// re.addLinkingResource(lr);
		re.addResourceContainer(rc1);
		re.addResourceContainer(rc2);
		re.addResourceContainer(rc3);
		System.out.println(re.toString());
		re.serialize(new File(System.getProperty("user.dir")
				+ "\\Palladio\\xxx.resourceenvironment"));
	}

	/** The resource environment element. */
	private Element resourceEnvironmentElement;

	/** The resource containers. */
	private List<ResourceContainer> resourceContainers;

	/** The linking resources. */
	private List<LinkingResource> linkingResources;

	/** The model. */
	private File model = null;

	/** The doc. */
	private Document doc;

	/**
	 * Instantiates a new resource environment.
	 */
	public ResourceEnvironment() {
		try {
			doc = DOM.getDocument();
			Element el = doc
					.createElement("resourceenvironment:ResourceEnvironment");
			el.setAttribute("xmi:version", "2.0");
			el.setAttribute("xmlns:xmi", "http://www.omg.org/XMI");
			el.setAttribute("xmlns:resourceenvironment",
					"http://sdq.ipd.uka.de/PalladioComponentModel/ResourceEnvironment/5.0");
			doc.appendChild(el);
			initialize(el);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	/**
	 * Instantiates a new resource environment.
	 * 
	 * @param e
	 *            the e
	 */
	public ResourceEnvironment(Element e) {
		doc = DOM.getDocument();
		Element x = (Element) doc.importNode(e, true);
		doc.appendChild(x);
		initialize(x);
	}

	/**
	 * Instantiates a new resource environment.
	 * 
	 * @param inputModel
	 *            the input model
	 */
	public ResourceEnvironment(File inputModel) {
		try {
			model = inputModel; // in the object there is the pointer to the
								// File inputModel
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(inputModel);

			initialize(doc.getDocumentElement());
			// filling some information as the list of linking Resources and
			// list of resourceContainers

		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	/**
	 * Adds the linking resource.
	 * 
	 * @param lr
	 *            the lr
	 */
	public void addLinkingResource(LinkingResource lr) {
		linkingResources.add(lr);
		Element x = (Element) doc.importNode(lr.getLinkingResourceElement(),
				true);
		resourceEnvironmentElement.appendChild(x);
	}

	/**
	 * Adds the resource container.
	 * 
	 * @param rc
	 *            the rc
	 */
	public void addResourceContainer(ResourceContainer rc) {
		resourceContainers.add(rc);
		Element x = (Element) doc.importNode(rc.getResourceContainerElement(),
				true);
		resourceEnvironmentElement.appendChild(x);
	}

	/**
	 * Gets the container by id.
	 * 
	 * @param ID
	 *            the id
	 * @return the container by id
	 */
	private Node getContainerByID(String ID) {
		NodeList nl = resourceEnvironmentElement
				.getElementsByTagName("resourceContainer_ResourceEnvironment");
		for (int i = 0; i < nl.getLength(); i++)
			if (((Element) nl.item(i)).getAttribute("id").equals(ID))
				return nl.item(i);
		return null;
	}

	/**
	 * Gets the link by id.
	 * 
	 * @param ID
	 *            the id
	 * @return the link by id
	 */
	private Node getLinkByID(String ID) {
		NodeList nl = resourceEnvironmentElement
				.getElementsByTagName("linkingResources__ResourceEnvironment");
		for (int i = 0; i < nl.getLength(); i++)
			if (((Element) nl.item(i)).getAttribute("id").equals(ID))
				return nl.item(i);
		return null;
	}

	/**
	 * Gets the linking resources.
	 * 
	 * @return the linking resources
	 */
	public List<LinkingResource> getLinkingResources() {
		return linkingResources;
	}

	/**
	 * Gets the resource containers.
	 * 
	 * @return the resource containers
	 */
	public List<ResourceContainer> getResourceContainers() {
		return resourceContainers;
	}

	/**
	 * Gets the resource environment element.
	 * 
	 * @return the resource environment element
	 */
	public Element getResourceEnvironmentElement() {
		return resourceEnvironmentElement;
	}

	/**
	 * Initialize.
	 * 
	 * @param e
	 *            the e
	 */
	private void initialize(Element e) {
		resourceEnvironmentElement = e;
		linkingResources = new ArrayList<LinkingResource>();
		resourceContainers = new ArrayList<ResourceContainer>();
		NodeList rc = resourceEnvironmentElement
				.getElementsByTagName("resourceContainer_ResourceEnvironment");

		// Resource container charging
		if (rc != null)
			for (int i = 0; i < rc.getLength(); i++)
				resourceContainers.add(new ResourceContainer((Element) rc
						.item(i)));

		rc = resourceEnvironmentElement
				.getElementsByTagName("linkingResources__ResourceEnvironment");
		if (rc != null)
			for (int i = 0; i < rc.getLength(); i++)
				linkingResources.add(new LinkingResource((Element) rc.item(i)));
	}

	/**
	 * Replace linking resource.
	 * 
	 * @param oldC
	 *            the old c
	 * @param newC
	 *            the new c
	 */
	public void replaceLinkingResource(LinkingResource oldC,
			LinkingResource newC) {
		Node el = getLinkByID(oldC.getId());
		if (el != null) {
			resourceEnvironmentElement.removeChild(el);
			initialize(resourceEnvironmentElement);
			addLinkingResource(newC);
		}
	}

	/**
	 * Replace resource container.
	 * 
	 * @param oldC
	 *            the old c
	 * @param newC
	 *            the new c
	 */
	public void replaceResourceContainer(ResourceContainer oldC,
			ResourceContainer newC) {
		Node el = getContainerByID(oldC.getId());
		if (el != null) {
			List<LinkingResource> newlinks = new ArrayList<LinkingResource>();
			for (LinkingResource lr : linkingResources) {
				lr.substitueConnection(oldC.getId(), newC.getId());
				newlinks.add(lr);
			}
			Element x = (Element) doc.importNode(
					newC.getResourceContainerElement(), true);
			resourceEnvironmentElement.replaceChild(x, el);
			initialize(resourceEnvironmentElement);
			setLinkingResources(newlinks);
		}
	}

	/**
	 * Serialize.
	 */
	public void serialize() {
		serialize(model);
	}

	/**
	 * Serialize.
	 * 
	 * @param f
	 *            the f
	 */
	public void serialize(File f) {
		try {
			if (model == null)
				model = new File(System.getProperty("user.dir")
						+ "\\Palladio\\MyRE.resourceenvironment");
			DOM.serialize(doc, f);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	/**
	 * Sets the linking resources.
	 * 
	 * @param linkingResources
	 *            the new linking resources
	 */
	public void setLinkingResources(List<LinkingResource> linkingResources) {
		this.linkingResources = new ArrayList<LinkingResource>();
		NodeList nl = resourceEnvironmentElement
				.getElementsByTagName("linkingResources__ResourceEnvironment");
		for (int i = 0; i < nl.getLength(); i++)
			resourceEnvironmentElement.removeChild(nl.item(i));
		for (LinkingResource lr : linkingResources)
			addLinkingResource(lr);
	}

	/**
	 * Sets the resource containers.
	 * 
	 * @param resourceContainers
	 *            the new resource containers
	 */
	public void setResourceContainers(List<ResourceContainer> resourceContainers) {
		this.resourceContainers = new ArrayList<ResourceContainer>();
		NodeList nl = resourceEnvironmentElement
				.getElementsByTagName("resourceContainer_ResourceEnvironment");
		for (int i = 0; i < nl.getLength(); i++)
			resourceEnvironmentElement.removeChild(nl.item(i));
		for (ResourceContainer rc : resourceContainers)
			addResourceContainer(rc);
	}

	/**
	 * Sets the resource environment element.
	 * 
	 * @param resourceEnvironmentElement
	 *            the new resource environment element
	 */
	public void setResourceEnvironmentElement(Element resourceEnvironmentElement) {
		initialize(resourceEnvironmentElement);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String output = "";
		List<ResourceContainer> lrc = getResourceContainers();
		output += "\n#ResourceContainers: " + lrc.size();
		output += "\n------------------------------------";
		for (ResourceContainer rc : lrc) {
			output += "\nResource Container Name: " + rc.getName();
			output += "\nResource Container ID: " + rc.getId();
			List<ProcessingResource> lpr = rc.getProcessingResources();
			output += "\n#ProcessigResource within the container: "
					+ lpr.size();
			for (ProcessingResource pr : lpr) {
				output += "\n\n\tProcessing Resource Type: " + pr.getType();
				output += "\n\tProcessing Resource MTTF: " + pr.getMTTF();
				output += "\n\tProcessing Resource MTTR: " + pr.getMTTR();
				output += "\n\tProcessing Resource numberOfReplicas: "
						+ pr.getNumberOfReplicas();
				output += "\n\tProcessing Resource processingRate: "
						+ pr.getProcessingRate();
				output += "\n\tProcessing Resource scheduling: "
						+ pr.getSchedulingPolicyString();
			}
			output += "\n------------------------------------";
		}
		List<LinkingResource> llr = getLinkingResources();
		for (LinkingResource lr : llr) {
			output += "\nLinking Resource ID" + lr.getId();
			output += "\nLinking Resource Type: " + lr.getType().getName();
			output += "\nLinking Resource throughput: " + lr.getThroughput();
			output += "\nLinking Resource latency: " + lr.getLatency();
			output += "\n------------------------------------";
		}
		output += "\n######################################";
		return output;
	}
}
