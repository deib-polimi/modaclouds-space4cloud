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
package it.polimi.modaclouds.space4cloud.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.polimi.modaclouds.space4cloud.iterfaces.UsageModelXMLBuilder;
import it.polimi.modaclouds.space4cloud.types.WorkloadT;

// TODO: Auto-generated Javadoc
/**
 * The Class UsageModelXMLBuilderImpl.
 */
public class UsageModelXMLBuilderImpl implements UsageModelXMLBuilder {
	
	private static final Logger logger=LoggerFactory.getLogger(UsageModelXMLBuilderImpl.class);

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		File file = new File(System.getProperty("user.dir")
				+ "\\Palladio\\default.usagemodel");
		UsageModelXMLBuilderImpl usb = new UsageModelXMLBuilderImpl(file);
		List<Element> uslist = usb.getUsageScenarios();
		usb.addClosedWorkload(uslist.get(0), 200, 20.5);
		usb.serializeModel();
	}

	/** The usmodel. */
	private Document usmodel;

	/** The root. */
	private Element root;

	/** The model file. */
	private File modelFile;

	/** The model ok. */
	private boolean modelOK;

	/**
	 * Creates the builder for the Usage Model XML specification.
	 * 
	 * @param model
	 *            the model
	 */
	public UsageModelXMLBuilderImpl(File model) {
		modelFile = model;
		modelOK = loadModel(model);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.polimi.franceschelli.space4cloud.iterfaces.UsageModelXMLBuilder#
	 * addClosedWorkload(org.w3c.dom.Element, int, double)
	 */
	@Override
	public Element addClosedWorkload(Element usageScenario, int population,
			double thinkTime) {
		if (!modelOK)
			return null;
		Element closedw = usmodel.createElement("workload_UsageScenario");
		closedw.setAttribute("xsi:type", WorkloadT.CLOSED.getType());
		closedw.setAttribute("population", "" + population);
		Element ttime = usmodel.createElement("thinkTime_ClosedWorkload");
		ttime.setAttribute("specification", "" + thinkTime);
		closedw.appendChild(ttime);
		usageScenario.appendChild(closedw);
		return closedw;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.polimi.franceschelli.space4cloud.iterfaces.UsageModelXMLBuilder#
	 * addOpenWorkload(org.w3c.dom.Element, double)
	 */
	@Override
	public Element addOpenWorkload(Element usageScenario,
			double interarrivalTime) {
		if (!modelOK)
			return null;
		Element openw = usmodel.createElement("workload_UsageScenario");
		openw.setAttribute("xsi:type", WorkloadT.OPEN.getType());
		Element inttime = usmodel
				.createElement("interArrivalTime_OpenWorkload");
		inttime.setAttribute("specification", "" + interarrivalTime);
		openw.appendChild(inttime);
		usageScenario.appendChild(openw);
		return openw;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.polimi.franceschelli.space4cloud.iterfaces.UsageModelXMLBuilder#
	 * changeSystemAndRepositoryModelsPath(java.lang.String)
	 */
	@Override
	public void changeSystemAndRepositoryModelsPath(String newPath) {
		List<Element> scenarios = getUsageScenarios();
		for (Element e : scenarios) {
			Element x = (Element) e.getElementsByTagName(
					"scenarioBehaviour_UsageScenario").item(0);
			NodeList nl1 = x
					.getElementsByTagName("providedRole_EntryLevelSystemCall");
			NodeList nl2 = x
					.getElementsByTagName("operationSignature__EntryLevelSystemCall");
			if (nl1 != null)
				for (int i = 0; i < nl1.getLength(); i++) {
					Element y1 = (Element) nl1.item(i);
					y1.setAttribute("href", newPath + y1.getAttribute("href"));
				}
			if (nl2 != null)
				for (int i = 0; i < nl2.getLength(); i++) {
					Element y2 = (Element) nl2.item(i);
					y2.setAttribute("href", newPath + y2.getAttribute("href"));
				}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.polimi.franceschelli.space4cloud.iterfaces.UsageModelXMLBuilder#
	 * getUsageScenarios()
	 */
	@Override
	public List<Element> getUsageScenarios() {
		List<Element> res = new ArrayList<Element>();
		if (!modelOK)
			return res;
		NodeList list = root.getElementsByTagName("usageScenario_UsageModel");
		for (int i = 0; i < list.getLength(); i++)
			if (list.item(i).getNodeType() == Node.ELEMENT_NODE)
				res.add((Element) list.item(i));
		return res;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.polimi.franceschelli.space4cloud.iterfaces.UsageModelXMLBuilder#loadModel
	 * (java.io.File)
	 */
	@Override
	public boolean loadModel(File model) {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			usmodel = docBuilder.parse(model);
			root = usmodel.getDocumentElement();
			return true;
		} catch (Exception e) {
			logger.error("Error while loading the model.", e);
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.polimi.franceschelli.space4cloud.iterfaces.UsageModelXMLBuilder#
	 * serializeModel()
	 */
	@Override
	public File serializeModel() {
		if (!modelOK)
			return null;
		return serializeModel(modelFile);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.polimi.franceschelli.space4cloud.iterfaces.UsageModelXMLBuilder#
	 * serializeModel(java.io.File)
	 */
	@Override
	public File serializeModel(File outFile) {
		if (!modelOK)
			return null;
		try {
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "5");
			DOMSource source = new DOMSource(usmodel);
			StreamResult result = new StreamResult(outFile);
			transformer.transform(source, result);
			return outFile;
		} catch (Exception exc) {
			logger.error("Error while saving the model.", exc);
			return null;
		}
	}
}
