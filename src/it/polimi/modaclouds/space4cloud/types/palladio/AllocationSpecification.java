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
 * The Class AllocationSpecification.
 */
public class AllocationSpecification {

	/** The allocation specification element. */
	private Element allocationSpecificationElement;

	/** The hour. */
	private int hour = 0;

	/** The size. */
	private int size = 1;

	/** The doc. */
	private Document doc;

	/** The Constant MIN_SIZE. */
	private final static int MIN_SIZE = 0;

	/**
	 * Instantiates a new allocation specification.
	 * 
	 * @param e
	 *            the e
	 */
	public AllocationSpecification(Element e) {
		doc = DOM.getDocument();
		Element x = (Element) doc.importNode(e, true);
		doc.appendChild(x);
		initialize(x);
	}

	/**
	 * Instantiates a new allocation specification.
	 * 
	 * @param hour
	 *            the hour
	 * @param size
	 *            the size
	 */
	public AllocationSpecification(int hour, int size) {
		doc = DOM.getDocument();
		Element x = doc.createElement("Allocation_Specification");
		if (!isValidHour(hour)) {
			hour = 0;
		}
		if (!isValidSize(size)) {
			size = 1;
		}
		x.setAttribute("hour", "" + hour);
		x.setAttribute("size", "" + size);
		doc.appendChild(x);
		initialize(x);
	}

	/**
	 * Gets the allocation specification element.
	 * 
	 * @return the allocation specification element
	 */
	public Element getAllocationSpecificationElement() {
		return allocationSpecificationElement;
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
	 * Gets the size.
	 * 
	 * @return the size
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Initialize.
	 * 
	 * @param x
	 *            the x
	 */
	private void initialize(Element x) {
		allocationSpecificationElement = x;
		hour = Integer.parseInt(x.getAttribute("hour"));
		size = Integer.parseInt(x.getAttribute("size"));
	}

	/**
	 * Checks if is valid hour.
	 * 
	 * @param hour
	 *            the hour
	 * @return true, if is valid hour
	 */
	private boolean isValidHour(int hour) {
		return hour >= 0 && hour <= 23;
	}

	/**
	 * Checks if is valid size.
	 * 
	 * @param size
	 *            the size
	 * @return true, if is valid size
	 */
	private boolean isValidSize(int size) {
		return size >= MIN_SIZE;
	}

	/**
	 * Sets the allocation specification element.
	 * 
	 * @param allocationSpecificationElement
	 *            the new allocation specification element
	 */
	public void setAllocationSpecificationElement(
			Element allocationSpecificationElement) {
		initialize((Element) doc.importNode(allocationSpecificationElement,
				true));
	}

	/**
	 * Sets the hour.
	 * 
	 * @param hour
	 *            the new hour
	 */
	public void setHour(int hour) {
		if (isValidHour(hour)) {
			this.hour = hour;
			allocationSpecificationElement.setAttribute("hour", "" + hour);
		}
	}

	/**
	 * Sets the size.
	 * 
	 * @param size
	 *            the new size
	 */
	public void setSize(int size) {
		if (isValidSize(size)) {
			this.size = size;
			allocationSpecificationElement.setAttribute("size", "" + size);
		}
	}
}
