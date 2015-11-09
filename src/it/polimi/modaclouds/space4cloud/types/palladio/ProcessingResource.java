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

import it.polimi.modaclouds.space4cloud.types.ProcessingResourceT;
import it.polimi.modaclouds.space4cloud.types.SchedulingT;
import it.polimi.modaclouds.space4cloud.utils.DOM;
import it.polimi.modaclouds.space4cloud.utils.IdHandler;
import it.polimi.modaclouds.space4cloud.utils.PalladioTypesUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class ProcessingResource.
 */
public class ProcessingResource {

	/** The processing resource element. */
	private Element processingResourceElement;

	/** The number of replicas. */
	private int numberOfReplicas;

	/** The processing rate. */
	private double MTTF, MTTR, processingRate;

	/** The type. */
	private ProcessingResourceT type;

	/** The scheduling. */
	private SchedulingT scheduling;

	/** The id. */
	private String id;

	/** The doc. */
	private Document doc;

	/**
	 * Instantiates a new processing resource.
	 * 
	 * @param e
	 *            the e
	 */
	public ProcessingResource(Element e) {
		doc = DOM.getDocument();
		Element x = (Element) doc.importNode(e, true);
		doc.appendChild(x);
		initialize(x);
	}

	/**
	 * Instantiates a new processing resource.
	 * 
	 * @param type
	 *            the type
	 * @param schedulingPolicy
	 *            the scheduling policy
	 * @param processingRate
	 *            the processing rate
	 * @param numberOfReplicas
	 *            the number of replicas
	 * @param MTTF
	 *            the mttf
	 * @param MTTR
	 *            the mttr
	 */
	public ProcessingResource(ProcessingResourceT type,
			SchedulingT schedulingPolicy, double processingRate,
			int numberOfReplicas, double MTTF, double MTTR) {
		try {
			doc = DOM.getDocument();
			Element el = doc
					.createElement("activeResourceSpecifications_ResourceContainer");
			el.setAttribute("id", IdHandler.getId(el));
			el.setAttribute("MTTR", "" + MTTR);
			el.setAttribute("MTTF", "" + MTTF);
			el.setAttribute("numberOfReplicas", "" + numberOfReplicas);
			Element x = doc.createElement("schedulingPolicy");
			x.setAttribute("href", schedulingPolicy.getPathmap());
			el.appendChild(x);
			x = doc.createElement("activeResourceType_ActiveResourceSpecification");
			x.setAttribute("href", type.getPathmap());
			el.appendChild(x);
			x = doc.createElement("processingRate_ProcessingResourceSpecification");
			x.setAttribute("specification", "" + processingRate);
			el.appendChild(x);
			doc.appendChild(el);
			initialize(el);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
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
	 * Gets the mttf.
	 * 
	 * @return the mttf
	 */
	public double getMTTF() {
		return MTTF;
	}

	/**
	 * Gets the mttr.
	 * 
	 * @return the mttr
	 */
	public double getMTTR() {
		return MTTR;
	}

	/**
	 * Gets the number of replicas.
	 * 
	 * @return the number of replicas
	 */
	public int getNumberOfReplicas() {
		return numberOfReplicas;
	}

	/**
	 * Gets the processing rate.
	 * 
	 * @return the processing rate
	 */
	public double getProcessingRate() {
		return processingRate;
	}

	/**
	 * Gets the processing resource element.
	 * 
	 * @return the processing resource element
	 */
	public Element getProcessingResourceElement() {
		return processingResourceElement;
	}

	/**
	 * Gets the scheduling.
	 * 
	 * @return the scheduling
	 */
	public SchedulingT getScheduling() {
		return scheduling;
	}

	/**
	 * Gets the scheduling policy string.
	 * 
	 * @return the scheduling policy string
	 */
	public String getSchedulingPolicyString() {
		return scheduling.getName();
	}

	/**
	 * Gets the string type.
	 * 
	 * @return the string type
	 */
	public String getStringType() {
		return type.getName();
	}

	/**
	 * Gets the type.
	 * 
	 * @return the type
	 */
	public ProcessingResourceT getType() {
		return type;
	}

	/**
	 * Initialize.
	 * 
	 * @param e
	 *            the e
	 */
	private void initialize(Element e) {
		processingResourceElement = e;
		id = e.getAttribute("id");
		try {
			processingRate = Double
					.parseDouble(((Element) processingResourceElement
							.getElementsByTagName(
									"processingRate_ProcessingResourceSpecification")
							.item(0)).getAttribute("specification"));
		} catch (Exception exc) {
			exc.printStackTrace();
			processingRate = 0;
		}
		try {
			MTTF = Double.parseDouble(e.getAttribute("MTTF"));
		} catch (Exception exc) {
			exc.printStackTrace();
			MTTF = 0.0;
		}
		try {
			MTTR = Double.parseDouble(e.getAttribute("MTTR"));
		} catch (Exception exc) {
			exc.printStackTrace();
			MTTR = 0.0;
		}
		try {
			numberOfReplicas = Integer.parseInt(e
					.getAttribute("numberOfReplicas"));
		} catch (Exception exc) {
			exc.printStackTrace();
			numberOfReplicas = 1;
		}
		try {
			type = PalladioTypesUtils.getProcessingResourceTypeByElement(e);
		} catch (Exception exc) {
			exc.printStackTrace();
			type = ProcessingResourceT.ND;
		}
		try {
			scheduling = PalladioTypesUtils.getSchedulingTypeByElement(e);
		} catch (Exception exc) {
			exc.printStackTrace();
			scheduling = SchedulingT.ND;
		}
	}

	/**
	 * Sets the id.
	 * 
	 * @param id
	 *            the new id
	 */
	public void setId(String id) {
		this.id = id;
		processingResourceElement.setAttribute("id", id);
	}

	/**
	 * Sets the mttf.
	 * 
	 * @param mTTF
	 *            the new mttf
	 */
	public void setMTTF(double mTTF) {
		MTTF = mTTF;
		processingResourceElement.setAttribute("MTTF", "" + mTTF);
	}

	/**
	 * Sets the mttr.
	 * 
	 * @param mTTR
	 *            the new mttr
	 */
	public void setMTTR(double mTTR) {
		MTTR = mTTR;
		processingResourceElement.setAttribute("MTTR", "" + mTTR);
	}

	/**
	 * Sets the number of replicas.
	 * 
	 * @param numberOfReplicas
	 *            the new number of replicas
	 */
	public void setNumberOfReplicas(int numberOfReplicas) {
		this.numberOfReplicas = numberOfReplicas;
		processingResourceElement.setAttribute("numberOfReplicas", ""
				+ numberOfReplicas);
	}

	/**
	 * Sets the processing rate.
	 * 
	 * @param processingRate
	 *            the new processing rate
	 */
	public void setProcessingRate(double processingRate) {
		this.processingRate = processingRate;
		((Element) processingResourceElement.getElementsByTagName(
				"processingRate_ProcessingResourceSpecification").item(0))
				.setAttribute("specification", "" + processingRate);
	}

	/**
	 * Sets the processing resource element.
	 * 
	 * @param processingResourceElement
	 *            the new processing resource element
	 */
	public void setProcessingResourceElement(Element processingResourceElement) {
		initialize(processingResourceElement);
	}

	/**
	 * Sets the scheduling.
	 * 
	 * @param scheduling
	 *            the new scheduling
	 */
	public void setScheduling(SchedulingT scheduling) {
		this.scheduling = scheduling;
		setScheduling(scheduling.getName());
	}

	/**
	 * Sets the scheduling.
	 * 
	 * @param schedulingPolicy
	 *            the new scheduling
	 */
	public void setScheduling(String schedulingPolicy) {
		String s = null;
		if (schedulingPolicy.equals("Processor Sharing")) {
			s = SchedulingT.PS.getPathmap();
			scheduling = SchedulingT.PS;
		} else if (schedulingPolicy.equals("First-Come-First-Served")) {
			s = SchedulingT.FCFS.getPathmap();
			scheduling = SchedulingT.FCFS;
		} else if (schedulingPolicy.equals("Delay")) {
			s = SchedulingT.DELAY.getPathmap();
			scheduling = SchedulingT.DELAY;
		}
		if (s != null)
			((Element) processingResourceElement.getElementsByTagName(
					"schedulingPolicy").item(0)).setAttribute("href", s);
	}

	/**
	 * Sets the type.
	 * 
	 * @param type
	 *            the new type
	 */
	public void setType(ProcessingResourceT type) {
		this.type = type;
		setType(type.getName());
	}

	/**
	 * Sets the type.
	 * 
	 * @param type
	 *            the new type
	 */
	public void setType(String type) {
		Element x = (Element) processingResourceElement.getElementsByTagName(
				"activeResourceType_ActiveResourceSpecification").item(0);
		if (type.equals("CPU")) {
			x.setAttribute("href", ProcessingResourceT.CPU.getPathmap());
			this.type = ProcessingResourceT.CPU;
		} else if (type.equals("HDD")) {
			x.setAttribute("href", ProcessingResourceT.HDD.getPathmap());
			this.type = ProcessingResourceT.HDD;
		} else if (type.equals("DELAY")) {
			x.setAttribute("href", ProcessingResourceT.DELAY.getPathmap());
			this.type = ProcessingResourceT.DELAY;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getStringType() + " [" + getId() + "]";
	}
}
