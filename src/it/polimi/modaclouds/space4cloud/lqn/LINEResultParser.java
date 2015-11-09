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
import org.xml.sax.SAXException;

import it.polimi.modaclouds.space4cloud.schemas.line.CmcqnModel;
import it.polimi.modaclouds.space4cloud.schemas.line.Percentile;
import it.polimi.modaclouds.space4cloud.schemas.line.Processor;
import it.polimi.modaclouds.space4cloud.schemas.line.ResponseTimeDistribution;
import it.polimi.modaclouds.space4cloud.schemas.line.SEFF;
import it.polimi.modaclouds.space4cloud.schemas.line.Station;
import it.polimi.modaclouds.space4cloud.schemas.line.Workload;
import it.polimi.modaclouds.space4cloud.utils.XMLHelper;

public class LINEResultParser extends LqnResultParser implements Serializable {


	private static final long serialVersionUID = -1456536857995387200L;
	private static final String SCHEMA_LOCATION = "http://www.modaclouds.eu/xsd/2013/6/lineResult lineResult.xsd";
	private static final String NAMESPACE = "http://www.modaclouds.eu/xsd/2013/6/lineResult";
	private static final Logger logger = LoggerFactory.getLogger(LINEResultParser.class);
	private Map<String,Map<Integer,Double>> percentiles = new HashMap<String,Map<Integer,Double>>();
	private transient Path filePath;
	private String filePathSerialization;
	private Map<String, Double> utilizations = new HashMap<>();
	private Map<String, Double> responseTimes = new HashMap<>();
	private Map<String, Double> throughputs = new HashMap<>();
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
			logger.error("Performance Result file not computed");
		}
		doc.getDocumentElement().normalize();
		Element root = (Element) doc.getElementsByTagName("cmcqn-model")
				.item(0);
		root.setAttribute("xsi:schemaLocation", SCHEMA_LOCATION);
		root.setAttribute("xmlns", NAMESPACE);

		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		javax.xml.transform.Transformer transformer = null;
		String separator = System.getProperty("line.separator");
		String newSeparator = "\n";
		System.setProperty("line.separator", newSeparator);

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
		System.setProperty("line.separator", separator);
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
			if(idSubstitutionMap.containsKey(seffName)){
				responseTimes.put(idSubstitutionMap.get(seffName), sf.getResponseTime());
				throughputs.put(idSubstitutionMap.get(seffName), sf.getThroughput());
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
		
		return;


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

	public double getThroughput(String resourceID) {
		if (throughputs.get(resourceID) != null)
			return throughputs.get(resourceID);
		return -1;
	}

	public Map<String, Double> getThroughputs() {
		return throughputs;
	}

}
