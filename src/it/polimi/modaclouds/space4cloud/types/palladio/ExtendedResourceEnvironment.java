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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.polimi.modaclouds.space4cloud.types.ProcessingResourceT;
import it.polimi.modaclouds.space4cloud.types.SchedulingT;
import it.polimi.modaclouds.space4cloud.utils.DOM;

// TODO: Auto-generated Javadoc
/**
 * This class is a wrapper for an extended Palladio Resource Environment
 * containing Linking Resources and Extended Resource Containers.
 * 
 * @author Davide Franceschelli
 * @see ExtendedResourceContainer
 */
public class ExtendedResourceEnvironment {

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		ExtendedResourceEnvironment re = new ExtendedResourceEnvironment();
		System.out.println(re.toString());
		ProcessingResource cpu = new ProcessingResource(
				ProcessingResourceT.CPU, SchedulingT.PS, 10000, 4, 10000, 2000);
		ProcessingResource hdd = new ProcessingResource(
				ProcessingResourceT.HDD, SchedulingT.FCFS, 100000000, 1,
				10000000, 2000);
		ExtendedProcessingResource epr1 = new ExtendedProcessingResource(cpu);
		ExtendedProcessingResource epr2 = new ExtendedProcessingResource(hdd);
		ExtendedResourceContainer rc1 = new ExtendedResourceContainer(
				"AppServer", new AllocationProfile());
		rc1.addExtendedProcessingResource(epr1);
		ExtendedResourceContainer rc2 = new ExtendedResourceContainer(
				"DBServer", new AllocationProfile());
		rc2.addExtendedProcessingResource(epr2);
		ExtendedResourceContainer rc3 = new ExtendedResourceContainer("XXX",
				new AllocationProfile());
		System.out.println(rc3.getAllocationProfile());
		rc3.addExtendedProcessingResource(epr1);
		re.addExtendedResourceContainer(rc1);
		System.out.println(re.getExtendedResourceContainers().get(0)
				.getAllocationProfile());
		re.addExtendedResourceContainer(rc2);
		System.out.println(re.getExtendedResourceContainers().get(1)
				.getAllocationProfile());
		re.replaceExtendedResourceContainer(rc2, rc3);
		System.out.println(re.getExtendedResourceContainers().get(1)
				.getAllocationProfile());
		System.out.println(re.toString());
		re.serialize(new File(System.getProperty("user.dir")
				+ "\\Palladio\\xxx.resourceenvironment"));
	}

	/** The extended resource environment element. */
	private Element extendedResourceEnvironmentElement;

	/** The extended resource containers. */
	private List<ExtendedResourceContainer> extendedResourceContainers;

	/** The linking resources. */
	private List<LinkingResource> linkingResources;

	/** The model. */
	private File model = null;

	/** The doc. */
	private Document doc;

	private Map<String, Integer> tierAllocation;

	/**
	 * Creates a new Extended Resource Environment Model.
	 */
	public ExtendedResourceEnvironment() {
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
	 * Loads an existing Extended Resource Environment Model.
	 * 
	 * @param root
	 *            is the DOM root Element representing the model.
	 */
	public ExtendedResourceEnvironment(Element root) {
		doc = DOM.getDocument();
		Element x = (Element) doc.importNode(root, true);
		doc.appendChild(x);
		initialize(x);
	}

	/**
	 * Loads an existing Extended Resource Environment Model.
	 * 
	 * @param inputModel
	 *            is the File containing the model.
	 */
	public ExtendedResourceEnvironment(File inputModel) {
		try {
			model = inputModel;
			doc = DOM.getDocument(inputModel);
			initialize(doc.getDocumentElement());
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	/**
	 * Adds an Extended Resource Container to the model.
	 * 
	 * @param rc
	 *            is the Extended Resource Container to add.
	 * @see ExtendedResourceContainer
	 */
	public void addExtendedResourceContainer(ExtendedResourceContainer rc) {
		extendedResourceContainers.add(rc);
		Element x = (Element) doc.importNode(
				rc.getExtendedResourceContainerElement(), true);
		extendedResourceEnvironmentElement.appendChild(x);
	}

	/**
	 * Adds a new Linking Resource to the model.
	 * 
	 * @param lr
	 *            is the new Linking Resource to add.
	 * @see LinkingResource
	 */
	public void addLinkingResource(LinkingResource lr) {
		linkingResources.add(lr);
		Element x = (Element) doc.importNode(lr.getLinkingResourceElement(),
				true);
		extendedResourceEnvironmentElement.appendChild(x);
	}

	/**
	 * Returns the Node representing the Extended Resource Container identified
	 * by the specified id.
	 * 
	 * @param ID
	 *            is the id of the Node.
	 * @return the Node representing the Resource Container with the specified
	 *         id, null otherwise.
	 */
	private Node getContainerByID(String ID) {
		NodeList nl = extendedResourceEnvironmentElement
				.getElementsByTagName("resourceContainer_ResourceEnvironment");
		for (int i = 0; i < nl.getLength(); i++)
			if (((Element) nl.item(i)).getAttribute("id").equals(ID))
				return nl.item(i);
		return null;
	}

	/**
	 * Gets the extended resource containers.
	 * 
	 * @return the List of the Extended Resource Containers within the model.
	 * @see ExtendedResourceContainer
	 */
	public List<ExtendedResourceContainer> getExtendedResourceContainers() {
		return extendedResourceContainers;
	}

	/**
	 * Returns the Node representing the Linking Resource identified by the
	 * specified id.
	 * 
	 * @param ID
	 *            is the id of the Node.
	 * @return the Node representing the Linking Resource with the specified id,
	 *         null otherwise.
	 */
	private Node getLinkByID(String ID) {
		NodeList nl = extendedResourceEnvironmentElement
				.getElementsByTagName("linkingResources__ResourceEnvironment");
		for (int i = 0; i < nl.getLength(); i++)
			if (((Element) nl.item(i)).getAttribute("id").equals(ID))
				return nl.item(i);
		return null;
	}

	/**
	 * Gets the linking resources.
	 * 
	 * @return the List of the Linking Resources within the model.
	 * @see LinkingResource
	 */
	public List<LinkingResource> getLinkingResources() {
		return linkingResources;
	}

	/**
	 * Gets the resource environment element.
	 * 
	 * @return the root Element representing the model.
	 */
	public Element getResourceEnvironmentElement() {
		return extendedResourceEnvironmentElement;
	}

	// GIBBO
	public Map<String, Integer> getTierAllocation() {

		if (tierAllocation == null) {
			tierAllocation = new HashMap<String, Integer>();

			List<ExtendedResourceContainer> lrc = getExtendedResourceContainers();
			for (ExtendedResourceContainer rc : lrc) {
				String id = rc.getId();
				int replicas = rc.getExtendedProcessingResources().get(0)
						.getNumberOfReplicas();
				tierAllocation.put(id, replicas);
			}
		}

		return tierAllocation;
	}

	/**
	 * Initialize the class attributes with the information contained in the
	 * Extended Resource Environment model.
	 * 
	 * @param root
	 *            is the DOM root Element representing the model.
	 */
	private void initialize(Element root) {
		extendedResourceEnvironmentElement = root;
		linkingResources = new ArrayList<LinkingResource>();
		extendedResourceContainers = new ArrayList<ExtendedResourceContainer>();
		NodeList rc = extendedResourceEnvironmentElement
				.getElementsByTagName("resourceContainer_ResourceEnvironment");
		if (rc != null)
			for (int i = 0; i < rc.getLength(); i++)
				extendedResourceContainers.add(new ExtendedResourceContainer(
						(Element) rc.item(i)));
		rc = extendedResourceEnvironmentElement
				.getElementsByTagName("linkingResources__ResourceEnvironment");
		if (rc != null)
			for (int i = 0; i < rc.getLength(); i++)
				linkingResources.add(new LinkingResource((Element) rc.item(i)));
	}

	/**
	 * Replaces an existing Extended Resource Container within the model with a
	 * new one. All the Linking Resources related to the old Extended Resource
	 * Container are updated and linked to the new Extended Resource Container.
	 * 
	 * @param oldC
	 *            is the existing Extended Resource Container to replace.
	 * @param newC
	 *            is the new Extended Resource Container.
	 * @see ExtendedResourceContainer
	 */
	public void replaceExtendedResourceContainer(
			ExtendedResourceContainer oldC, ExtendedResourceContainer newC) {
		Node el = getContainerByID(oldC.getId());
		if (el != null) {
			List<LinkingResource> newlinks = new ArrayList<LinkingResource>();
			for (LinkingResource lr : linkingResources) {
				lr.substitueConnection(oldC.getId(), newC.getId());
				newlinks.add(lr);
			}
			Element x = (Element) doc.importNode(
					newC.getExtendedResourceContainerElement(), true);
			extendedResourceEnvironmentElement.replaceChild(x, el);
			initialize(extendedResourceEnvironmentElement);
			setLinkingResources(newlinks);
		}
	}

	/**
	 * Replaces an existing Linking Resource within the model with a new one.
	 * 
	 * @param oldC
	 *            is the existing Linking Resource to replace.
	 * @param newC
	 *            is the new Linking Resource.
	 * @see LinkingResource
	 */
	public void replaceLinkingResource(LinkingResource oldC,
			LinkingResource newC) {
		Node el = getLinkByID(oldC.getId());
		if (el != null) {
			extendedResourceEnvironmentElement.removeChild(el);
			initialize(extendedResourceEnvironmentElement);
			addLinkingResource(newC);
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
	 * Replaces the Extended Resource Containers within the model with the ones
	 * contained in the provided List.
	 * 
	 * @param extendedResourceContainers
	 *            is the List of the new Extended Resource Containers.
	 * @see ExtendedResourceContainer
	 */
	public void setExtendedResourceContainers(
			List<ExtendedResourceContainer> extendedResourceContainers) {
		this.extendedResourceContainers = new ArrayList<ExtendedResourceContainer>();
		NodeList nl = extendedResourceEnvironmentElement
				.getElementsByTagName("resourceContainer_ResourceEnvironment");
		for (int i = 0; i < nl.getLength(); i++)
			extendedResourceEnvironmentElement.removeChild(nl.item(i));
		for (ExtendedResourceContainer rc : extendedResourceContainers)
			addExtendedResourceContainer(rc);
	}

	/**
	 * Re-initialize the model using the provided DOM root Element.
	 * 
	 * @param extendedResourceEnvironmentElement
	 *            is the DOM root Element representing the new model.
	 */
	public void setExtendedResourceEnvironmentElement(
			Element extendedResourceEnvironmentElement) {
		initialize(extendedResourceEnvironmentElement);
	}

	/**
	 * Replaces the Linking Resources within the model with the ones contained
	 * in the provided List.
	 * 
	 * @param linkingResources
	 *            is the List of the new Linking Resources.
	 * @see LinkingResource
	 */
	public void setLinkingResources(List<LinkingResource> linkingResources) {
		this.linkingResources = new ArrayList<LinkingResource>();
		NodeList nl = extendedResourceEnvironmentElement
				.getElementsByTagName("linkingResources__ResourceEnvironment");
		for (int i = 0; i < nl.getLength(); i++)
			extendedResourceEnvironmentElement.removeChild(nl.item(i));

		for (LinkingResource lr : linkingResources)
			addLinkingResource(lr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String output = "";
		List<ExtendedResourceContainer> lrc = getExtendedResourceContainers();
		output += "\n#ResourceContainers: " + lrc.size();
		output += "\n------------------------------------";
		for (ExtendedResourceContainer rc : lrc) {
			output += "\nExtendedResource Container Name: " + rc.getName();
			output += "\nExtendedResource Container ID: " + rc.getId();
			List<ExtendedProcessingResource> lpr = rc
					.getExtendedProcessingResources();
			output += "\n#ExtendedProcessigResource within the container: "
					+ lpr.size();
			for (ExtendedProcessingResource pr : lpr) {
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
