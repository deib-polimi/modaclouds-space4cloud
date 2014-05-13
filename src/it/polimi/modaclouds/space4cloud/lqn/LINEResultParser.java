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

import it.polimi.modaclouds.space4cloud.line.CmcqnModel;
import it.polimi.modaclouds.space4cloud.line.Processor;
import it.polimi.modaclouds.space4cloud.line.SEFF;
import it.polimi.modaclouds.space4cloud.line.Workload;
import it.polimi.modaclouds.space4cloud.utils.LoggerHelper;
import it.polimi.modaclouds.space4cloud.utils.XMLHelper;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;

import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class LINEResultParser implements LqnResultParser, Serializable {

	private static final long serialVersionUID = -1456536857995387200L;
	private static final String SCHEMA_LOCATION = "http://www.modaclouds.eu/xsd/2013/6/lineResult lineResult.xsd";
	private static final String NAMESPACE = "http://www.modaclouds.eu/xsd/2013/6/lineResult";
	
	private static final Logger logger = LoggerHelper
			.getLogger(LINEResultParser.class);

	public static double convertStringToDouble(String toConvert)
			throws ParseException {
		double ret;

		toConvert = toConvert.replaceAll("e", "E");
		toConvert = toConvert.replaceAll("\\+", "");
		DecimalFormat format = new DecimalFormat("0.0E000",
				DecimalFormatSymbols.getInstance(Locale.ENGLISH));
		ret = format.parse(toConvert).doubleValue();

		return ret;
	}

	private transient Path filePath;
	private String filePathSerialization;

	private HashMap<String, Double> utilizations = new HashMap<>();
	private HashMap<String, Double> responseTimes = new HashMap<>();
	private CmcqnModel result;

	public LINEResultParser(Path path) {

		this.filePath = path;
		// parse the document
		parse();
	}

	@Override
	public double getResponseTime(String resourceID) {
		if (responseTimes.get(resourceID) != null)
			return responseTimes.get(resourceID);
		return -1;
	}

	public HashMap<String, Double> getResponseTimes() {
		return responseTimes;
	}

	@Override
	public double getUtilization(String resourceID) {
		if (utilizations.get(resourceID) != null)
			return utilizations.get(resourceID);
		return -1;

	}

	public HashMap<String, Double> getUtilizations() {
		return utilizations;
	}
	
	private void loadXML(){
		try {
			result = XMLHelper.deserialize(filePath.toUri().toURL(),
					CmcqnModel.class);			
		} catch (MalformedURLException | JAXBException e) {
			
			if (e instanceof UnmarshalException) {
				fixSchemaReference(filePath.toFile());
				logger.warn("Fixed xml index in file: "+filePath.toString());
				loadXML();
			}else{
				logger.error("Unable to parse LINE results of mofel: "+filePath.toString(), e);
			}
		}

	}

	private void parse() {
		
		loadXML();

		for (Processor p : result.getProcessor())
			utilizations.put(p.getName(), p.getUtil());

		for (Workload wl : result.getWorkload())
			responseTimes.put(wl.getName().split("_")[2], wl.getResponseTime());

		for (SEFF sf : result.getSEFF())
			responseTimes.put(sf.getName().split("_")[2], sf.getResponseTime());
		System.out.println();

	}

	// reconstruct the Path from the string
	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException, SAXException {
		in.defaultReadObject();
		filePath = Paths.get(filePathSerialization);
		parse();

	}

	// since Path s not serializable we put it into a string
	private void writeObject(ObjectOutputStream out) throws IOException {
		filePathSerialization = filePath.toString();
		out.defaultWriteObject();
	}

	private void fixSchemaReference(File solutionFile){
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		Document doc = null;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(solutionFile);

		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
		doc.getDocumentElement().normalize();
		Element root = (Element) doc.getElementsByTagName("cmcqn-model").item(0);
		root.setAttribute("xsi:schemaLocation", SCHEMA_LOCATION);
		root.setAttribute("xmlns", NAMESPACE);

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		javax.xml.transform.Transformer transformer = null;
		try {
			transformer = transformerFactory.newTransformer();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(solutionFile);
		try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
