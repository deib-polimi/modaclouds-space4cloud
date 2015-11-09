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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import it.polimi.modaclouds.space4cloud.utils.DOM;

// TODO: Auto-generated Javadoc
/**
 * The Class AllocationProfile.
 */
public class AllocationProfile {

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		AllocationProfile ap = new AllocationProfile();
		ap.serialize(new File(System.getProperty("user.dir")
				+ "\\Palladio\\all.xml"));
		AllocationSpecification sp1 = new AllocationSpecification(10, 12);
		ap.addSpecification(sp1);
		ap.serialize(new File(System.getProperty("user.dir")
				+ "\\Palladio\\all1.xml"));
	}

	/** The specifications. */
	private AllocationSpecification specifications[];

	/** The doc. */
	private Document doc;

	/** The allocation profile element. */
	private Element allocationProfileElement;

	/**
	 * Instantiates a new allocation profile.
	 */
	public AllocationProfile() {
		doc = DOM.getDocument();
		Element e = doc.createElement("Allocation_Profile");
		doc.appendChild(e);
		initialize(e);
		generateDefaultProfile();
	}

	/**
	 * Instantiates a new allocation profile.
	 * 
	 * @param e
	 *            the e
	 */
	public AllocationProfile(Element e) {
		doc = DOM.getDocument();
		Element x = (Element) doc.importNode(e, true);
		doc.appendChild(x);
		initialize(x);
	}

	/**
	 * Instantiates a new allocation profile.
	 * 
	 * @param inputModel
	 *            the input model
	 */
	public AllocationProfile(File inputModel) {
		doc = DOM.getDocument(inputModel);
		initialize(doc.getDocumentElement());
	}

	/**
	 * Adds the specification.
	 * 
	 * @param asp
	 *            the asp
	 */
	public void addSpecification(AllocationSpecification asp) {
		specifications[asp.getHour()] = asp;
		NodeList nl = allocationProfileElement
				.getElementsByTagName("Allocation_Specification");
		Element x = (Element) doc.importNode(
				asp.getAllocationSpecificationElement(), true);
		for (int i = 0; i < nl.getLength(); i++)
			if (((Element) nl.item(i)).getAttribute("hour").equals(
					"" + asp.getHour())) {
				allocationProfileElement.removeChild(nl.item(i));
				allocationProfileElement.insertBefore(x, nl.item(i));
				return;
			}
		allocationProfileElement.appendChild(x);
	}

	/**
	 * Generate default profile.
	 */
	private void generateDefaultProfile() {
		for (int i = 0; i < 24; i++)
			addSpecification(new AllocationSpecification(i, 1));
	}

	/**
	 * Gets the allocation profile element.
	 * 
	 * @return the allocation profile element
	 */
	public Element getAllocationProfileElement() {
		return allocationProfileElement;
	}

	/**
	 * Gets the specifications.
	 * 
	 * @return the specifications
	 */
	public AllocationSpecification[] getSpecifications() {
		return specifications;
	}

	/**
	 * Initialize.
	 * 
	 * @param e
	 *            the e
	 */
	private void initialize(Element e) {
		allocationProfileElement = e;
		specifications = new AllocationSpecification[24];
		NodeList nl = e.getElementsByTagName("Allocation_Specification");
		if (nl.getLength() <= 24)
			if (nl.getLength() > 0)
				for (int i = 0; i < nl.getLength(); i++) {
					Element x = (Element) nl.item(i);
					int index = Integer.parseInt(x.getAttribute("hour"));
					specifications[index] = new AllocationSpecification(x);
				}
	}

	/**
	 * Serialize.
	 * 
	 * @param outputFile
	 *            the output file
	 */
	public void serialize(File outputFile) {
		DOM.serialize(doc, outputFile);
	}

	/**
	 * Sets the allocation profile element.
	 * 
	 * @param allocationProfileElement
	 *            the new allocation profile element
	 */
	public void setAllocationProfileElement(Element allocationProfileElement) {
		initialize((Element) doc.importNode(allocationProfileElement, true));
	}

	/**
	 * Sets the specifications.
	 * 
	 * @param specifications
	 *            the new specifications
	 */
	public void setSpecifications(AllocationSpecification[] specifications) {
		if (specifications != null)
			if (specifications.length <= 24 && specifications.length >= 0) {
				this.specifications = specifications;
				Element x = (Element) allocationProfileElement.cloneNode(false);
				for (AllocationSpecification es : specifications)
					x.appendChild(doc.importNode(
							es.getAllocationSpecificationElement(), true));
			}
	}
}
