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
 * The Class EfficiencyProfile.
 */
public class EfficiencyProfile {

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		EfficiencyProfile ep = new EfficiencyProfile();
		EfficiencySpecification es = new EfficiencySpecification(0, 0.01);
		ep.addSpecification(es);
		es.setHour(12);
		es.setEfficiency(0.02);
		ep.addSpecification(es);
		es.setHour(23);
		es.setEfficiency(0.0001);
		ep.addSpecification(es);
		ep.serialize(new File(System.getProperty("user.dir")
				+ "\\Palladio\\eff.efficiencyxml"));
	}

	/** The specifications. */
	private EfficiencySpecification specifications[];

	/** The doc. */
	private Document doc;

	/** The efficiency profile element. */
	private Element efficiencyProfileElement;

	/**
	 * Instantiates a new efficiency profile.
	 */
	public EfficiencyProfile() {
		doc = DOM.getDocument();
		Element e = doc.createElement("Efficiency_Profile");
		doc.appendChild(e);
		initialize(e);
		generateDefaultProfile();
	}

	/**
	 * Instantiates a new efficiency profile.
	 * 
	 * @param e
	 *            the e
	 */
	public EfficiencyProfile(Element e) {
		doc = DOM.getDocument();
		Element x = (Element) doc.importNode(e, true);
		doc.appendChild(x);
		initialize(x);
	}

	/**
	 * Instantiates a new efficiency profile.
	 * 
	 * @param inputModel
	 *            the input model
	 */
	public EfficiencyProfile(File inputModel) {
		doc = DOM.getDocument(inputModel);
		initialize(doc.getDocumentElement());
	}

	/**
	 * Adds the specification.
	 * 
	 * @param es
	 *            the es
	 */
	public void addSpecification(EfficiencySpecification es) {
		specifications[es.getHour()] = es;
		NodeList nl = efficiencyProfileElement
				.getElementsByTagName("Efficiency_Specification");
		Element x = (Element) doc.importNode(
				es.getEfficiencySpecificationElement(), true);
		for (int i = 0; i < nl.getLength(); i++)
			if (((Element) nl.item(i)).getAttribute("hour").equals(
					"" + es.getHour())) {
				efficiencyProfileElement.removeChild(nl.item(i));
				efficiencyProfileElement.insertBefore(x, nl.item(i));
				return;
			}
		efficiencyProfileElement.appendChild(x);
	}

	/**
	 * Generate default profile.
	 */
	private void generateDefaultProfile() {
		for (int i = 0; i < 24; i++)
			addSpecification(new EfficiencySpecification(i, 1.0));
	}

	/**
	 * Gets the efficiency profile element.
	 * 
	 * @return the efficiency profile element
	 */
	public Element getEfficiencyProfileElement() {
		return efficiencyProfileElement;
	}

	/**
	 * Gets the specifications.
	 * 
	 * @return the specifications
	 */
	public EfficiencySpecification[] getSpecifications() {
		return specifications;
	}

	/**
	 * Initialize.
	 * 
	 * @param e
	 *            the e
	 */
	private void initialize(Element e) {
		efficiencyProfileElement = e;
		specifications = new EfficiencySpecification[24];
		NodeList nl = e.getElementsByTagName("Efficiency_Specification");
		if (nl.getLength() <= 24)
			if (nl.getLength() > 0)
				for (int i = 0; i < nl.getLength(); i++) {
					Element x = (Element) nl.item(i);
					int index = Integer.parseInt(x.getAttribute("hour"));
					specifications[index] = new EfficiencySpecification(x);
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
	 * Sets the efficiency profile element.
	 * 
	 * @param efficiencyProfileElement
	 *            the new efficiency profile element
	 */
	public void setEfficiencyProfileElement(Element efficiencyProfileElement) {
		initialize((Element) doc.importNode(efficiencyProfileElement, true));
	}

	/**
	 * Sets the specifications.
	 * 
	 * @param specifications
	 *            the new specifications
	 */
	public void setSpecifications(EfficiencySpecification[] specifications) {
		if (specifications != null)
			if (specifications.length <= 24 && specifications.length >= 0) {
				this.specifications = specifications;
				Element x = (Element) efficiencyProfileElement.cloneNode(false);
				for (EfficiencySpecification es : specifications)
					x.appendChild(doc.importNode(
							es.getEfficiencySpecificationElement(), true));
			}
	}
}
