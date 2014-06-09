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
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.CloudService;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Compute;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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

	/**
	 * Instantiates a new lqn handler.
	 * 
	 * @param lqnDOM
	 *            the lqn dom
	 */
	public LqnHandler(Document lqnDOM) {

		this.setLqnDOM(lqnDOM);
	}

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

	private void changeElementbyName(String elementType, String name,
			String elementAttribute, double multiplicity) {
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
					e.setAttribute(elementAttribute, "" + multiplicity);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	 * @return the lqnDOM
	 */
	//
	// public Document getLqnDOM() {
	// return lqnDOM;
	// }
	//
	public Path getLqnFilePath() {
		return lqnFilePath;
	}

	private void initDom() {
		try {

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			lqnDOM = dBuilder.parse(lqnFilePath.toFile());
			lqnDOM.getDocumentElement().normalize();

		} catch (IOException | SAXException | ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void initLQN(Path lqnFilePath) {
		this.lqnFilePath = lqnFilePath;
	}

	// reconstruct the Path from the string
	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		lqnFilePath = Paths.get(lqnFilePathSerialization);
		initDom();
	}

	public void saveToFile() {
		if (lqnDOM == null)
			initDom();
		try {
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer;
			transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(lqnDOM);
			StreamResult result = new StreamResult(lqnFilePath.toFile());
			transformer.transform(source, result);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	 * Updateds the element corresponding to the resource in the dom
	 */
	public void updateElement(CloudService service) {

		// Iaas-Compute
		if (service instanceof Compute) {
			Compute c_resource = (Compute) service;
			int multiplicity = c_resource.getNumberOfCores()
					* c_resource.getReplicas();
			changeElementbyName("processor", c_resource.getName(),
					"multiplicity", multiplicity);
			changeElementbyName("processor", c_resource.getName(),
					"speed-factor", c_resource.getSpeedFactor());
		}

		// TODO add other cloud resource types.

		else
			System.err
					.println("Error! No Code found for this type of resource");

	}

	// since Path s not serializable we put it into a string
	private void writeObject(ObjectOutputStream out) throws IOException {
		lqnFilePathSerialization = lqnFilePath.toString();
		out.defaultWriteObject();
	}

}
