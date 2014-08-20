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
import it.polimi.modaclouds.space4cloud.line.Percentile;
import it.polimi.modaclouds.space4cloud.line.Processor;
import it.polimi.modaclouds.space4cloud.line.ResponseTimeDistribution;
import it.polimi.modaclouds.space4cloud.line.SEFF;
import it.polimi.modaclouds.space4cloud.line.Station;
import it.polimi.modaclouds.space4cloud.line.Workload;
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
import java.util.Map;

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
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class LINEResultParser implements LqnResultParser, Serializable {

	private static Map<String,String> idSubstitutionMap = new HashMap<String,String>();
	private static final long serialVersionUID = -1456536857995387200L;
	private static final String SCHEMA_LOCATION = "http://www.modaclouds.eu/xsd/2013/6/lineResult lineResult.xsd";
	private static final String NAMESPACE = "http://www.modaclouds.eu/xsd/2013/6/lineResult";
	private static final Logger logger = LoggerFactory.getLogger(LINEResultParser.class);
	private Map<String,Map<Integer,Double>> percentiles = new HashMap<String,Map<Integer,Double>>();
	private transient Path filePath;
	private String filePathSerialization;
	private Map<String, Double> utilizations = new HashMap<>();
	private Map<String, Double> responseTimes = new HashMap<>();
	private CmcqnModel result;


	/**
	 * Retrieves the value of the {@paramref percentileLevel}th response time percentile of the functionality with id {@paramref functionalityID} 
	 * @param functionalityID the id of the functionality
	 * @param percentileLevel the level of the percentile
	 * @return the value of the response time percentile
	 */
	public double getPercentile(String functionalityID, int percentileLevel){
		if(percentiles.containsKey(functionalityID) && percentiles.get(functionalityID).containsKey(percentileLevel))
			return percentiles.get(functionalityID).get(percentileLevel);
		return -1;
	}


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

	public LINEResultParser(Path path) {

		if(idSubstitutionMap.isEmpty()){
			logger.error("Parser not initialized, mapping between functionality ids and LQN processor names are missing");
			return;
		}

		this.filePath = path;
		// parse the document
		parse();
	}

	/**
	 * Attach to the first element of the xml file the schema name and location with the default values if not already present
	 * @param solutionFile
	 */
	private void fixSchemaReference(File solutionFile) {
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
		Element root = (Element) doc.getElementsByTagName("cmcqn-model")
				.item(0);
		root.setAttribute("xsi:schemaLocation", SCHEMA_LOCATION);
		root.setAttribute("xmlns", NAMESPACE);

		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		javax.xml.transform.Transformer transformer = null;
		try {
			transformer = transformerFactory.newTransformer();
		} catch (TransformerConfigurationException e) {
			logger.error("Error while fixing schema reference",e);
		}
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(solutionFile);
		try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			logger.error("Error while fixing schema reference",e);
		}
	}

	@Override
	public double getResponseTime(String resourceID) {
		if (responseTimes.get(resourceID) != null)
			return responseTimes.get(resourceID);
		return -1;
	}

	@Override
	public Map<String, Double> getResponseTimes() {
		return responseTimes;
	}

	@Override
	public double getUtilization(String resourceID) {
		if (utilizations.get(resourceID) != null)
			return utilizations.get(resourceID);
		System.out.println("");
		return -1;

	}

	@Override
	public Map<String, Double> getUtilizations() {
		return utilizations;
	}

	/**
	 * deserializes the xml file
	 */
	private void loadXML() {
		try {	
			fixSchemaReference(filePath.toFile());
			result = XMLHelper.deserialize(filePath.toUri().toURL(),
					CmcqnModel.class);
		} catch (MalformedURLException | JAXBException e) {

			if (e instanceof UnmarshalException) {				
				logger.error("Error loading solutions from LINE");
			} else {
				logger.error("Unable to parse LINE results of mofel: "
						+ filePath.toString(), e);
			}
		}

	}

	/**
	 * Deserialize the xlm file and loads information in the hash maps. 
	 */
	private void parse() {		

		loadXML();

		for (Processor p : result.getProcessor())
			utilizations.put(p.getName(), p.getUtil());

		for (Workload wl : result.getWorkload()){			
			//copy average response times
			for(Station st:wl.getStation())
				responseTimes.put(st.getName().split("_")[2], st.getResponseTime());
			//copy the percentile
			ResponseTimeDistribution percentiles= wl.getResponseTimeDistribution();
			if(percentiles!= null){
				Map<Integer, Double> percentilesMap = new HashMap<Integer,Double>();
				for(Percentile perc:percentiles.getPercentile())
					percentilesMap.put((int)Math.round(10*perc.getLevel()), perc.getValue());
				this.percentiles.put(wl.getName(),percentilesMap);
			}
		}

		for (SEFF sf : result.getSEFF()){
			String seffName=sf.getName();
			responseTimes.put(idSubstitutionMap.get(seffName), sf.getResponseTime());
			//copy the percentile
			ResponseTimeDistribution percentiles= sf.getResponseTimeDistribution();
			if(percentiles!= null){
				Map<Integer, Double> percentilesMap = new HashMap<Integer,Double>();
				for(Percentile perc:percentiles.getPercentile())
					//LINE exports response time level as a double between 0 and 1
					percentilesMap.put((int)Math.round(100*perc.getLevel()), perc.getValue());
				this.percentiles.put(idSubstitutionMap.get(seffName),percentilesMap);
			}
		}


	}

	/**
	 *  reconstruct the Path from the string
	 * @param in
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws SAXException
	 */
	private void readObject(ObjectInputStream in) throws IOException,
	ClassNotFoundException, SAXException {
		in.defaultReadObject();
		filePath = Paths.get(filePathSerialization);
		parse();

	}

	/**
	 *  since Path s not serializable we put it into a string
	 * @param out
	 * @throws IOException
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		filePathSerialization = filePath.toString();
		out.defaultWriteObject();
	}

	/**
	 * Retrieves the available response time percentiles of the functionality with id {@paramref name}
	 * @param name
	 * @return the map having the integer level as id and the percentile value as value
	 */
	public Map<Integer, Double> getPercentiles(String name) {
		return percentiles.get(name);
	}


	public static void initIds(File lqnFile) {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document resultDOM = dBuilder.parse(lqnFile);
			resultDOM.getDocumentElement().normalize();
			NodeList processors = resultDOM.getElementsByTagName("processor");
			for (int i = 0; i < processors.getLength(); i++) {
				Element processor = (Element) processors.item(i);
				String processorName = processor.getAttribute("name");				
				NodeList activities = processor.getElementsByTagName("activity");
				String functionalityId = null;
				for(int j=0; j<activities.getLength(); j++){
					String activityName = activities.item(j).getAttributes().getNamedItem("name").getNodeValue();
					if(activityName.startsWith("StartAction")){
						String[] tokens = activityName.split("_");
						functionalityId = tokens[2]+"_"+tokens[3]+"_"+tokens[5];
					}

				}
				if(processorName != null && functionalityId != null)
					idSubstitutionMap.put(processorName, functionalityId);
			}
		}catch(IOException e){
			logger.error("Error initializing the LINE result parser, could not access the LQN file",e);
		} catch (ParserConfigurationException e) {
			logger.error("Error initializing the LINE result parser, could not parse the LQN file",e);
		} catch (SAXException e) {
			logger.error("Error initializing the LINE result parser, could not parse the LQN file",e);
		}
	}
}
