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
package it.polimi.modaclouds.space4cloud.lqn;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import it.polimi.modaclouds.space4cloud.optimization.solution.impl.CloudService;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Compute;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.DelayCenter;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.PaaS;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Tier;



/**
 * @author Michele Ciavotta The aim of this class is to have a wrapper around
 *         the DOM object in order to make transparent the searching and
 *         replacing processes
 */

public class LqnHandler implements Cloneable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3298316101458681404L;
	private transient Document lqnDOM = null;
	private transient Path lqnFilePath = null;
	private String lqnFilePathSerialization;
	private static final Logger logger = LoggerFactory.getLogger(LqnHandler.class);


	/**
	 * Instantiates a new lqn handler.
	 * 
	 * @param lqnFile
	 *            the lqn file
	 */
	public LqnHandler(File lqnFile) {
		initLQN(lqnFile.toPath());
	}

	public LqnHandler(Path lqnFilePath) {

		this.lqnFilePath = lqnFilePath;

		initDom();

	}
	/**
	 * Changes the value of attribute with name {@attref elementAttribute} to the value {@attref value}. The element to look for is identified by element type and its name attribute
	 * @param elementType type of the element to change
	 * @param name name of the element to change
	 * @param elementAttribute name of the attribute to change
	 * @param multiplicity new double value
	 */
	private void changeElementbyName(String elementType, String name,
			String elementAttribute, double value) {
		if (lqnDOM == null)
			initDom();
		NodeList elements = lqnDOM.getElementsByTagName(elementType);
		for (int i = 0; i < elements.getLength(); i++) {
			Node n = elements.item(i);
			Element e = (Element) n;
			String element_id = e.getAttribute("name");
			// we found the right element
			if (element_id.equals(name)) {
				if (e.getAttribute(elementAttribute) != null) {
					e.setAttribute(elementAttribute, "" + value);
				}
			}
		}
	}

	@Override
	public LqnHandler clone() {

		File lqnClone = cloneLqnFile();
		LqnHandler lqnH;
		try {
			lqnH = (LqnHandler) super.clone();
			lqnH.setLqnDOM(null);
			lqnH.setLqnFilePath(lqnClone.toPath());
		} catch (CloneNotSupportedException e) {
			logger.error("Error cloning the lqn handler",e);
			lqnH = new LqnHandler(lqnClone);
		}

		// that's all, we don't need to create the DOM object it is created
		// automatically
		return lqnH;

	}

	/**
	 * The aim of this method is to copy the content of one lqn file into
	 * another with a random UUID name.
	 * 
	 */
	private File cloneLqnFile() {
		String from = lqnFilePath.toString();
		String fromName = lqnFilePath.getFileName().toString();
		String toName = UUID.randomUUID().toString() + ".xml";
		String to = from.replace(fromName, toName);
		Path pathTo = null;
		try {
			// lets create a new file
			pathTo = Files.createFile(Paths.get(to));

			// copy the content
			Files.copy(lqnFilePath, pathTo, REPLACE_EXISTING);
		} catch (IOException e) {
			logger.error("Error cloning the lqn file",e);
		}
		return pathTo.toFile();
	}

	@Override
	protected void finalize() throws Throwable {
		if (lqnFilePath.toFile().exists())
			lqnFilePath.toFile().delete();
		super.finalize();
	}

	/**
	 * @return the path to the lqn model file
	 */
	public Path getLqnFilePath() {
		return lqnFilePath;
	}

	/**
	 * Initialized the Dom from the lqnFilePath
	 * */
	private void initDom() {
		try {

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			lqnDOM = dBuilder.parse(lqnFilePath.toFile());
			lqnDOM.getDocumentElement().normalize();

		} catch (IOException | SAXException | ParserConfigurationException e) {
			logger.error("Error initializing lqn dom",e);
		}
	}

	/**
	 * sets the lqn file path
	 * */
	private void initLQN(Path lqnFilePath) {
		this.lqnFilePath = lqnFilePath;
	}

	/**
	 *  reconstruct the Path from the string
	 * @param in the input stream to read the path from
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		lqnFilePath = Paths.get(lqnFilePathSerialization);
		initDom();
	}

	/**
	 * Saves the LQN model file
	 */
	public void saveToFile() {
		if (lqnDOM == null)
			initDom();
		try {
			//Workaropund for LQNS, it doesn't like \r so we force the use of \n
			String separator = System.getProperty("line.separator");
			String newSeparator = "\n";
			System.setProperty("line.separator", newSeparator);
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer;
			transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(lqnDOM);
			StreamResult result = new StreamResult(lqnFilePath.toFile());
			transformer.transform(source, result);
			System.setProperty("line.separator", separator);
		} catch (TransformerException e) {
			logger.error("Error saving the lqn file",e);
		}
	}

	/**
	 * @param lqnDOM
	 *            the lqnDOM to set
	 */
	public void setLqnDOM(Document lqnDOM) {
		this.lqnDOM = lqnDOM;
	}

	private void setLqnFilePath(Path lqnFilePath) {
		this.lqnFilePath = lqnFilePath;
	}

	public void setPopulation(int pop) {
		if (lqnDOM == null)
			initDom();
		NodeList processors = lqnDOM.getElementsByTagName("processor");
		for (int i = 0; i < processors.getLength(); i++) {
			Node processorNode = processors.item(i);
			Node nameNode = processorNode.getAttributes().getNamedItem("name");
			if (nameNode != null
					&& nameNode.getNodeValue().contains("UsageScenario")
					&& !nameNode.getNodeValue().contains("Loop")) {
				// we assume there is only one task for usage scenario
				Node taskNode = ((Element) processorNode).getElementsByTagName(
						"task").item(0);
				((Element) taskNode).setAttribute("multiplicity", "" + pop);
			}
		}
	}

	public void setThinktime(double time) {
		if (lqnDOM == null)
			initDom();
		NodeList processors = lqnDOM.getElementsByTagName("processor");
		for (int i = 0; i < processors.getLength(); i++) {
			Node processorNode = processors.item(i);
			Node nameNode = processorNode.getAttributes().getNamedItem("name");
			if (nameNode != null
					&& nameNode.getNodeValue().contains("UsageScenario")
					&& !nameNode.getNodeValue().contains("Loop")) {
				// we assume there is only one task for usage scenario
				Node taskNode = ((Element) processorNode).getElementsByTagName(
						"task").item(0);
				((Element) taskNode).setAttribute("think-time", "" + time);
			}
		}
	}

	/**
	 * Updates the element corresponding to the resource in the dom
	 */
	public void updateElement(Tier tier) {

		CloudService service = tier.getCloudService();
		// Iaas-Compute
		if (service instanceof Compute) {
			Compute c_resource = (Compute) service;
			int multiplicity = c_resource.getNumberOfCores()
					* c_resource.getReplicas();
			changeElementbyName("processor", tier.getPcmName(),
					"multiplicity", multiplicity);
			changeElementbyName("processor", tier.getPcmName(),
					"speed-factor", c_resource.getSpeedFactor());
		} else if (service instanceof PaaS) {
			if (service instanceof DelayCenter) {
				// TODO: we have to consider the delay!!!! this is wrong!!
				PaaS p = (PaaS) service;
				Compute c_resource = p.getCompute();
				int multiplicity = c_resource.getNumberOfCores()
						* c_resource.getReplicas() * p.getReplicas();
				
				@SuppressWarnings("unused")
				double delay = ((DelayCenter)p).getDelay();
				
				changeElementbyName("processor", tier.getPcmName(),
						"multiplicity", multiplicity);
				changeElementbyName("processor", tier.getPcmName(),
						"speed-factor", c_resource.getSpeedFactor());
			} else {
				PaaS p = (PaaS) service;
				Compute c_resource = p.getCompute();
				int multiplicity = c_resource.getNumberOfCores()
						* c_resource.getReplicas() * p.getReplicas();
				changeElementbyName("processor", tier.getPcmName(),
						"multiplicity", multiplicity);
				changeElementbyName("processor", tier.getPcmName(),
						"speed-factor", c_resource.getSpeedFactor());
			}
		}

		// TODO add other cloud resource types.

		else
			logger.error("resource type "+service.getClass()+" not supported.");

	}

	// since Path s not serializable we put it into a string
	private void writeObject(ObjectOutputStream out) throws IOException {
		lqnFilePathSerialization = lqnFilePath.toString();
		out.defaultWriteObject();
	}

	/**
	 * Build a map between the processorId in the lqn model and the corresponding functionality ID in the Palladio model, using a mapping between the start action id of a functionality and the id of the functionality itself
	 * @param startActionId2FunId mapping between the start action id of a functionality and its functionality id
	 * @return
	 */
	public Map<String, String> getProcessorIdMap(Map<String, String> startActionId2FunId) {
		
		Map<String,String> procId2FunId = new HashMap<String, String>();
		Map<String,String> activityName2procId = new HashMap<String, String>();
		//get from the lqn model the mapping between procerssor name and startActivity names
		NodeList processors = lqnDOM.getElementsByTagName("processor");
		for (int i = 0; i < processors.getLength(); i++) {
			Element processor = (Element) processors.item(i);
			String processorId = processor.getAttribute("name");				
			NodeList activities = processor.getElementsByTagName("activity");
			String functionalityId = null;
			for(int j=0; j<activities.getLength(); j++){
				String activityName = activities.item(j).getAttributes().getNamedItem("name").getNodeValue();
				if(activityName.startsWith("StartAction")){
					functionalityId = activityName;
				}

			}
			if(processorId != null && functionalityId != null)
				activityName2procId.put(functionalityId,processorId);
		}
		
		//merge the mapping according to the fact that the activityName contains the activity ID
		for(String startActivityID:startActionId2FunId.keySet()){
			for(String activityName:activityName2procId.keySet()){
				if(activityName.contains(startActivityID)){
					String procId = activityName2procId.get(activityName);
					String funId = startActionId2FunId.get(startActivityID);
					procId2FunId.put(procId,funId);
				}
			}
		}

		return procId2FunId;
	}

	public void setArrivalRate(double arrivalRate) {
		if (lqnDOM == null)
			initDom();
		NodeList processors = lqnDOM.getElementsByTagName("processor");
		for (int i = 0; i < processors.getLength(); i++) {
			Node processorNode = processors.item(i);
			Node nameNode = processorNode.getAttributes().getNamedItem("name");
			if (nameNode != null
					&& nameNode.getNodeValue().contains("UsageScenario")
					&& !nameNode.getNodeValue().contains("Loop")) {
				// we assume there is only one task for usage scenario
				Node taskNode = ((Element) processorNode).getElementsByTagName(
						"task").item(0);
				//and a single entry
				Node entryNode = ((Element) taskNode).getElementsByTagName(
						"entry").item(0);
				((Element) entryNode).setAttribute("open-arrival-rate", "" + arrivalRate);
			}
		}
	}

}

