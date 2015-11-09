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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import it.polimi.modaclouds.space4cloud.utils.DOM;

// TODO: Auto-generated Javadoc
/**
 * The Class EfficiencySpecification.
 */
public class EfficiencySpecification {

	/** The efficiency specification element. */
	private Element efficiencySpecificationElement;

	/** The hour. */
	private int hour = 0;

	/** The efficiency. */
	private double efficiency = 1.0;

	/** The doc. */
	private Document doc;

	/** The Constant MIN_EFFICIENCY. */
	private final static double MIN_EFFICIENCY = 0.0;

	/** The Constant MAX_EFFICIENCY. */
	private final static double MAX_EFFICIENCY = 1.0;

	/**
	 * Instantiates a new efficiency specification.
	 * 
	 * @param item
	 *            the item
	 */
	public EfficiencySpecification(Element item) {
		doc = DOM.getDocument();
		Element x = (Element) doc.importNode(item, true);
		doc.appendChild(x);
		initialize(x);
	}

	/**
	 * Instantiates a new efficiency specification.
	 * 
	 * @param hour
	 *            the hour
	 * @param efficiency
	 *            the efficiency
	 */
	public EfficiencySpecification(int hour, double efficiency) {
		doc = DOM.getDocument();
		Element e = doc.createElement("Efficiency_Specification");
		if (!isValid(hour))
			hour = 0;
		if (!isValid(efficiency))
			efficiency = 1.0;
		e.setAttribute("hour", "" + hour);
		e.setAttribute("efficiency", "" + efficiency);
		doc.appendChild(e);
		initialize(e);
	}

	/**
	 * Gets the efficiency.
	 * 
	 * @return the efficiency
	 */
	public double getEfficiency() {
		return efficiency;
	}

	/**
	 * Gets the efficiency specification element.
	 * 
	 * @return the efficiency specification element
	 */
	public Element getEfficiencySpecificationElement() {
		return efficiencySpecificationElement;
	}

	/**
	 * Gets the hour.
	 * 
	 * @return the hour
	 */
	public int getHour() {
		return hour;
	}

	/**
	 * Initialize.
	 * 
	 * @param e
	 *            the e
	 */
	private void initialize(Element e) {
		efficiencySpecificationElement = e;
		try {
			hour = Integer.parseInt(e.getAttribute("hour"));
			efficiency = Double.parseDouble(e.getAttribute("efficiency"));
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	/**
	 * Checks if is valid.
	 * 
	 * @param eff
	 *            the eff
	 * @return true, if is valid
	 */
	private boolean isValid(double eff) {
		return eff >= MIN_EFFICIENCY && eff <= MAX_EFFICIENCY;
	}

	/**
	 * Checks if is valid.
	 * 
	 * @param hour
	 *            the hour
	 * @return true, if is valid
	 */
	private boolean isValid(int hour) {
		return hour >= 0 && hour <= 23;
	}

	/**
	 * Sets the efficiency.
	 * 
	 * @param efficiency
	 *            the new efficiency
	 */
	public void setEfficiency(double efficiency) {
		if (isValid(efficiency)) {
			this.efficiency = efficiency;
			efficiencySpecificationElement.setAttribute("efficiency", ""
					+ efficiency);
		}
	}

	/**
	 * Sets the efficiency specification element.
	 * 
	 * @param efficiencySpecificationElement
	 *            the new efficiency specification element
	 */
	public void setEfficiencySpecificationElement(
			Element efficiencySpecificationElement) {
		initialize((Element) doc.importNode(efficiencySpecificationElement,
				true));
	}

	/**
	 * Sets the hour.
	 * 
	 * @param hour
	 *            the new hour
	 */
	public void setHour(int hour) {
		if (isValid(hour)) {
			this.hour = hour;
			efficiencySpecificationElement.setAttribute("hour", "" + hour);
		}
	}
}
