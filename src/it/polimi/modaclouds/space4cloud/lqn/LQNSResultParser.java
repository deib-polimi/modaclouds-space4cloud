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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class LQNSResultParser implements LqnResultParser, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1523765717223255130L;
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
	private DocumentBuilderFactory dbFactory;
	private DocumentBuilder dBuilder;
	private transient Document resultDOM;
	private Map<String, Double> utilizations = new HashMap<>();

	private Map<String, Double> responseTimes = new HashMap<>();

	private static final Logger logger = LoggerFactory.getLogger(LQNSResultParser.class);

	public LQNSResultParser(Path path) {

		this.filePath = path;
		initDom();

		parse();
	}

	@Override
	protected void finalize() throws Throwable {
		if (filePath.toFile().exists())
			filePath.toFile().delete();
		super.finalize();
	}

	@Override
	public double getResponseTime(String resourceID) {
		if (responseTimes.get(resourceID) != null)
			return responseTimes.get(resourceID);
		return -1;
	}

	@Override
	public Map<String, Double> getResponseTimes() {
		// TODO Auto-generated method stub
		return responseTimes;
	}

	@Override
	public double getUtilization(String resourceID) {
		if (utilizations.get(resourceID) != null)
			return utilizations.get(resourceID);
		return -1;

	}

	@Override
	public Map<String, Double> getUtilizations() {
		// TODO Auto-generated method stub
		return utilizations;
	}

	private void initDom() {
		dbFactory = DocumentBuilderFactory.newInstance();
		try {
			dBuilder = dbFactory.newDocumentBuilder();

			// TODO: sometimes the temporary file has a name ending with a tilde
			if (!filePath.toFile().exists())
				filePath = Paths.get(filePath.toString() + "~");

			resultDOM = dBuilder.parse(filePath.toFile());
			resultDOM.getDocumentElement().normalize();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void parse() {
		// parse Processors
		NodeList processors = resultDOM.getElementsByTagName("processor");
		for (int i = 0; i < processors.getLength(); i++) {

			// search for the result element
			Element processor = (Element) processors.item(i);
			String id = processor.getAttribute("name");
			int cores = 1;
			if (!processor.getAttribute("multiplicity").isEmpty())
				cores = Integer
						.parseInt(processor.getAttribute("multiplicity"));

			// there should be exactly one
			Element resultProcessor = (Element) processors.item(i)
					.getFirstChild().getNextSibling();
			double utilization = Double.parseDouble(resultProcessor
					.getAttribute("utilization")) / cores;
			// LQNS uses values from 0 to 100 we use from 0 to 1
			//TODO:Check if is this true
			utilization /= 100;

			// add the processor utilization to the hashmap
			// System.out.println("proc id: "+id+ " utilization: "+utilization);
			utilizations.put(id, utilization);

			String seffID=id.split("_")[2];
			NodeList resultActivities = null;
			resultActivities = processor.getElementsByTagName("result-activity");
			if(resultActivities.getLength() == 0)
				resultActivities = processor.getElementsByTagName("resultActivity");

			double serviceTime = 0;
			for(int j=0; j<resultActivities.getLength(); j++){
				
				Node serviceTimeNode = resultActivities.item(j).getAttributes().getNamedItem("service-time");
				if(serviceTimeNode == null)
					serviceTimeNode = resultActivities.item(j).getAttributes().getNamedItem("serviceTime");	
				if(serviceTimeNode!=null)
					serviceTime += Double.parseDouble(serviceTimeNode.getTextContent());
			}
			responseTimes.put(seffID, serviceTime);

		}

		// TODO: parse other stuff
		dbFactory = null;
		dBuilder = null;
		resultDOM = null;

	}

	// reconstruct the Path from the string
	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException, SAXException {
		in.defaultReadObject();
		filePath = Paths.get(filePathSerialization);
		initDom();
	}

	// since Path s not serializable we put it into a string
	private void writeObject(ObjectOutputStream out) throws IOException {
		filePathSerialization = filePath.toString();
		out.defaultWriteObject();
	}

}
