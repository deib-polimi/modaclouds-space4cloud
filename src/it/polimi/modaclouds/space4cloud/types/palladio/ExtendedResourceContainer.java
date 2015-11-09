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

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.polimi.modaclouds.space4cloud.utils.DOM;
import it.polimi.modaclouds.space4cloud.utils.IdHandler;

// TODO: Auto-generated Javadoc
/**
 * Represents a Palladio Resource Container extended with an Allocation Profile.
 * 
 * @author Davide Franceschelli
 * @see AllocationProfile
 */
public class ExtendedResourceContainer {

	/** The extended resource container element. */
	private Element extendedResourceContainerElement;

	/** The name. */
	private String name;

	/** The id. */
	private String id;

	/** The allocation profile. */
	private AllocationProfile allocationProfile;

	/** The extended processing resources. */
	private List<ExtendedProcessingResource> extendedProcessingResources;

	/** The doc. */
	private Document doc;

	/**
	 * Loads an existing Extended Resource Container Model.
	 * 
	 * @param root
	 *            is the DOM root Element representing the model.
	 */
	public ExtendedResourceContainer(Element root) {
		doc = DOM.getDocument();
		Element x = (Element) doc.importNode(root, true);
		doc.appendChild(x);
		initialize(x);
	}

	/**
	 * Creates a new Extended Resource Container with the specified name and
	 * Allocation Profile.
	 * 
	 * @param name
	 *            is the name of the container.
	 * @param ap
	 *            is the Allocation Profile associated to the container.
	 * @see AllocationProfile
	 */
	public ExtendedResourceContainer(String name, AllocationProfile ap) {
		this(name, null, ap);
	}

	/**
	 * Creates a new Extended Resource Container with the specifed name,
	 * Extended Processing Resources and Allocation Profile.
	 * 
	 * @param name
	 *            is the name of the container.
	 * @param procRes
	 *            is the List of the Extended Processing Resources within the
	 *            container.
	 * @param ap
	 *            is the Allocation Profile of the container.
	 * @see AllocationProfile
	 * @see ExtendedProcessingResource
	 */
	public ExtendedResourceContainer(String name,
			List<ExtendedProcessingResource> procRes, AllocationProfile ap) {
		try {
			doc = DOM.getDocument();
			Element el = doc
					.createElement("resourceContainer_ResourceEnvironment");
			el.setAttribute("entityName", name);
			el.setAttribute("id", IdHandler.getId(el));
			doc.appendChild(el);
			initialize(el);
			if (procRes != null)
				setExtendedProcessingResources(procRes);
			if (ap != null)
				setAllocationProfile(ap);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	/**
	 * Adds an Extended Processing Resource to the container.
	 * 
	 * @param epr
	 *            is the Extended Processing Resource to add
	 * @see ExtendedProcessingResource
	 */
	public void addExtendedProcessingResource(ExtendedProcessingResource epr) {
		extendedProcessingResources.add(epr);
		Element x = (Element) doc.importNode(
				epr.getProcessingResourceElement(), true);
		extendedResourceContainerElement.appendChild(x);
	}

	/**
	 * Gets the allocation profile.
	 * 
	 * @return the Allocation Profile associated to the container.
	 */
	public AllocationProfile getAllocationProfile() {
		return allocationProfile;
	}

	/**
	 * Gets the doc.
	 * 
	 * @return the Document representing the container model.
	 */
	public Document getDoc() {
		return doc;
	}

	/**
	 * Returns the Node representing the Extended Processing Resource identified
	 * by the specified id.
	 * 
	 * @param ID
	 *            is the id of the Node.
	 * @return the Node representing the Extended Processing Resource with the
	 *         specified id, null otherwise.
	 * @see ExtendedProcessingResource
	 */
	private Node getExtendedProcessingResourceByID(String ID) {
		NodeList nl = extendedResourceContainerElement
				.getElementsByTagName("activeResourceSpecifications_ResourceContainer");
		if (nl != null)
			for (int i = 0; i < nl.getLength(); i++)
				if (((Element) nl.item(i)).getAttribute("id").equals(ID))
					return nl.item(i);
		return null;
	}

	/**
	 * Gets the extended processing resources.
	 * 
	 * @return the List of the Extended Processing Resources within the
	 *         container.
	 */
	public List<ExtendedProcessingResource> getExtendedProcessingResources() {
		return extendedProcessingResources;
	}

	/**
	 * Gets the extended resource container element.
	 * 
	 * @return the DOM Element representing the container.
	 */
	public Element getExtendedResourceContainerElement() {
		return extendedResourceContainerElement;
	}

	/**
	 * Gets the id.
	 * 
	 * @return the String representing the id of the container.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Gets the name.
	 * 
	 * @return the String representing the name of the container.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Initializes the attributes of the model using the information provided by
	 * the root Element.
	 * 
	 * @param root
	 *            is the DOM root Element representing the model.
	 */
	private void initialize(Element root) {
		try {
			extendedResourceContainerElement = root;
			name = root.getAttribute("entityName");
			id = root.getAttribute("id");
			extendedProcessingResources = new ArrayList<ExtendedProcessingResource>();
			NodeList nl = root
					.getElementsByTagName("activeResourceSpecifications_ResourceContainer");
			for (int i = 0; i < nl.getLength(); i++) {
				Element x = (Element) nl.item(i);
				NodeList nls = x.getElementsByTagName("Efficiency_Profile");
				EfficiencyProfile ep = null;
				if (nls != null)
					if (nls.getLength() > 0)
						ep = new EfficiencyProfile((Element) nls.item(0));
				extendedProcessingResources.add(new ExtendedProcessingResource(
						x, ep));
			}
			nl = root.getElementsByTagName("Allocation_Profile");
			if (nl != null)
				if (nl.getLength() > 0)
					allocationProfile = new AllocationProfile(
							(Element) nl.item(0));
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	/**
	 * Replaces an existing Extended Processing Resource within the model with a
	 * new one.
	 * 
	 * @param oldPR
	 *            is the existing Extended Processing Resource to replace.
	 * @param newPR
	 *            is the new Extended Processing Resource.
	 * @see ExtendedProcessingResource
	 */
	public void replaceExtendedProcessingResource(
			ExtendedProcessingResource oldPR, ExtendedProcessingResource newPR) {
		Node n = getExtendedProcessingResourceByID(oldPR.getId());
		if (n != null) {
			Element x = (Element) doc.importNode(
					newPR.getProcessingResourceElement(), true);
			extendedResourceContainerElement.replaceChild(x, n);
			initialize(extendedResourceContainerElement);
		}
	}

	/**
	 * Sets an Allocation Profile for the Resource Container. Only one
	 * Allocation Profile per container is allowed. If there's already an
	 * Allocation Profile, it is replaced by the new one.
	 * 
	 * @param allocationProfile
	 *            is the Allocation Profile to set.
	 * @see AllocationProfile
	 */
	public void setAllocationProfile(AllocationProfile allocationProfile) {
		this.allocationProfile = allocationProfile;
		NodeList nl = extendedResourceContainerElement
				.getElementsByTagName("Allocation_Profile");
		if (nl != null)
			for (int i = 0; i < nl.getLength(); i++)
				extendedResourceContainerElement.removeChild(nl.item(i));
		Element x = (Element) doc.importNode(
				allocationProfile.getAllocationProfileElement(), true);
		extendedResourceContainerElement.appendChild(x);
	}

	/**
	 * Sets the Extended Processing Resources within the container. The existing
	 * Extended Processing Resources are replaced with the new ones.
	 * 
	 * @param extendedProcessingResources
	 *            is the List of the Extended Processing Resources to set.
	 * @see ExtendedProcessingResource
	 */
	public void setExtendedProcessingResources(
			List<ExtendedProcessingResource> extendedProcessingResources) {
		NodeList nl = extendedResourceContainerElement.getChildNodes();
		this.extendedProcessingResources = new ArrayList<ExtendedProcessingResource>();
		if (nl != null) {
			for (int i = 0; i < nl.getLength(); i++)
				extendedResourceContainerElement.removeChild(nl.item(i));
			for (ExtendedProcessingResource epr : extendedProcessingResources)
				addExtendedProcessingResource(epr);
		}
	}

	/**
	 * Re-initialize the model using the provided DOM root Element.
	 * 
	 * @param extendedResourceContainerElement
	 *            is the DOM root Element representing the new model.
	 */
	public void setExtendedResourceContainerElement(
			Element extendedResourceContainerElement) {
		initialize(extendedResourceContainerElement);
	}

	/**
	 * Sets the container id.
	 * 
	 * @param id
	 *            is the String representing the id to set.
	 */
	public void setId(String id) {
		this.id = id;
		extendedResourceContainerElement.setAttribute("id", id);
	}

	/**
	 * Sets the name of the container.
	 * 
	 * @param name
	 *            is the String representing the name to set.
	 */
	public void setName(String name) {
		this.name = name;
		extendedResourceContainerElement.setAttribute("entityName", name);
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
