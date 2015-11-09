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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.polimi.modaclouds.space4cloud.types.ProcessingResourceT;
import it.polimi.modaclouds.space4cloud.types.SchedulingT;
import it.polimi.modaclouds.space4cloud.utils.DOM;
import it.polimi.modaclouds.space4cloud.utils.IdHandler;

// TODO: Auto-generated Javadoc
/**
 * The Class ResourceContainer.
 */
public class ResourceContainer {

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		ResourceEnvironment resenv = new ResourceEnvironment();
		ResourceContainer rc = new ResourceContainer("AppServer");
		ProcessingResource cpu = new ProcessingResource(
				ProcessingResourceT.CPU, SchedulingT.PS, 1000, 4, 100000, 2000);
		ProcessingResource cpu1 = new ProcessingResource(
				ProcessingResourceT.CPU, SchedulingT.PS, 10000, 6, 100000, 2000);
		ProcessingResource hdd = new ProcessingResource(
				ProcessingResourceT.HDD, SchedulingT.FCFS, 1000, 1, 100000,
				2000);
		ProcessingResource hdd1 = new ProcessingResource(
				ProcessingResourceT.HDD, SchedulingT.FCFS, 1000000, 1, 100000,
				2000);
		ProcessingResource delay = new ProcessingResource(
				ProcessingResourceT.DELAY, SchedulingT.DELAY, 1000, 1, 100000,
				2000);
		rc.addProcessingResource(cpu);
		rc.addProcessingResource(hdd);
		rc.addProcessingResource(delay);
		rc.replaceProcessingResource(cpu, cpu1);
		rc.replaceProcessingResource(hdd, hdd1);
		resenv.addResourceContainer(rc);
		resenv.serialize(new File(System.getProperty("user.dir")
				+ "\\Palladio\\slh.xml"));
	}

	/** The resource container element. */
	private Element resourceContainerElement;

	/** The name. */
	private String name;

	/** The id. */
	private String id;

	/** The processing resources. */
	private List<ProcessingResource> processingResources;

	/** The doc. */
	private Document doc;

	/**
	 * Instantiates a new resource container.
	 * 
	 * @param e
	 *            the e
	 */
	public ResourceContainer(Element e) {
		doc = DOM.getDocument();
		Element x = (Element) doc.importNode(e, true);
		doc.appendChild(x);
		initialize(x);
	}

	/**
	 * Instantiates a new resource container.
	 * 
	 * @param name
	 *            the name
	 */
	public ResourceContainer(String name) {
		this(name, null);
	}

	/**
	 * Instantiates a new resource container.
	 * 
	 * @param name
	 *            the name
	 * @param procRes
	 *            the proc res
	 */
	public ResourceContainer(String name, List<ProcessingResource> procRes) {
		try {
			doc = DOM.getDocument();
			Element el = doc
					.createElement("resourceContainer_ResourceEnvironment");
			el.setAttribute("entityName", name);
			el.setAttribute("id", IdHandler.getId(el));
			doc.appendChild(el);
			initialize(el);
			if (procRes != null)
				setProcRes(procRes);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	/**
	 * Adds the processing resource.
	 * 
	 * @param res
	 *            the res
	 */
	public void addProcessingResource(ProcessingResource res) {
		processingResources.add(res);
		Element x = (Element) doc.importNode(
				res.getProcessingResourceElement(), true);
		resourceContainerElement.appendChild(x);
	}

	/**
	 * Gets the document.
	 * 
	 * @return the document
	 */
	public Document getDocument() {
		return doc;
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the processing resource node by id.
	 * 
	 * @param ID
	 *            the id
	 * @return the processing resource node by id
	 */
	private Node getProcessingResourceNodeByID(String ID) {
		NodeList nl = resourceContainerElement
				.getElementsByTagName("activeResourceSpecifications_ResourceContainer");
		for (int i = 0; i < nl.getLength(); i++)
			if (((Element) nl.item(i)).getAttribute("id").equals(ID))
				return nl.item(i);
		return null;
	}

	/**
	 * Gets the processing resources.
	 * 
	 * @return the processing resources
	 */
	public List<ProcessingResource> getProcessingResources() {
		return processingResources;
	}

	/**
	 * Gets the resource container element.
	 * 
	 * @return the resource container element
	 */
	public Element getResourceContainerElement() {
		return resourceContainerElement;
	}

	/**
	 * Initialize.
	 * 
	 * @param e
	 *            the e
	 */
	private void initialize(Element e) {
		try {
			resourceContainerElement = e;
			name = e.getAttribute("entityName");
			id = e.getAttribute("id");
			processingResources = new ArrayList<ProcessingResource>();
			NodeList nl = e
					.getElementsByTagName("activeResourceSpecifications_ResourceContainer");
			for (int i = 0; i < nl.getLength(); i++)
				processingResources.add(new ProcessingResource((Element) nl
						.item(i)));
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	/**
	 * Replace processing resource.
	 * 
	 * @param oldC
	 *            the old c
	 * @param newC
	 *            the new c
	 */
	public void replaceProcessingResource(ProcessingResource oldC,
			ProcessingResource newC) {
		Node el = getProcessingResourceNodeByID(oldC.getId());
		if (el != null) {
			Element x = (Element) doc.importNode(
					newC.getProcessingResourceElement(), true);
			resourceContainerElement.replaceChild(x, el);
			initialize(resourceContainerElement);
		}
	}

	/**
	 * Sets the name.
	 * 
	 * @param name
	 *            the new name
	 */
	public void setName(String name) {
		this.name = name;
		resourceContainerElement.setAttribute("entityName", name);
	}

	/**
	 * Sets the proc res.
	 * 
	 * @param procRes
	 *            the new proc res
	 */
	public void setProcRes(List<ProcessingResource> procRes) {
		this.processingResources = new ArrayList<ProcessingResource>();
		NodeList nl = resourceContainerElement
				.getElementsByTagName("activeResourceSpecifications_ResourceContainer");
		for (int i = 0; i < nl.getLength(); i++)
			resourceContainerElement.removeChild(nl.item(i));
		for (ProcessingResource x : procRes)
			addProcessingResource(x);
	}

	/**
	 * Sets the resource container element.
	 * 
	 * @param resourceContainerElement
	 *            the new resource container element
	 */
	public void setResourceContainerElement(Element resourceContainerElement) {
		initialize(resourceContainerElement);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getName() + " [" + getId() + "]";
	}
}
