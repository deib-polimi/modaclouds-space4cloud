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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.emf.common.util.EList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import LqnCore.ActivityDefType;
import LqnCore.OutputResultType;
import LqnCore.TaskType;

public class LINEResultParser implements LqnResultParser, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1456536857995387200L;
	private transient Path  filePath;
	private String filePathSerialization;
	private DocumentBuilderFactory dbFactory;
	private DocumentBuilder dBuilder;
	private transient Document resultDOM;	



	private HashMap<String, Double> utilizations = new HashMap<>();
	private HashMap<String, Double> responseTimes = new HashMap<>();

	//since Path s not serializable we put it into a string 
	private void writeObject(ObjectOutputStream out) throws IOException
	{
		filePathSerialization = filePath.toString();
		out.defaultWriteObject(); 
	}
	//reconstruct the Path from the string
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException, SAXException
	{		
		in.defaultReadObject();
		filePath = Paths.get(filePathSerialization);
		initDom();
	}

	private void initDom(){
		dbFactory = DocumentBuilderFactory.newInstance();
		try {
			dBuilder = dbFactory.newDocumentBuilder();
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


	public LINEResultParser(Path path) {


		this.filePath=path; 
		initDom();

		//parse the document
		parse();
	}

	private void parse() {
		//parse Processors
		NodeList elements = resultDOM.getElementsByTagName("processor");
		for(int i=0; i< elements.getLength();i++){						
			//search for the result element						
			Element processor = (Element) elements.item(i);
			String id = processor.getAttribute("name");			
			double utilization = Double.parseDouble(processor.getAttribute("util"));
			utilizations.put(id, utilization);
		}


		//parse response times
		elements = resultDOM.getElementsByTagName("workload");
		for(int i=0; i< elements.getLength();i++){						
			//search for the result element						
			Element workload = (Element) elements.item(i);
			String id = workload.getAttribute("name");			
			double respTime = Double.parseDouble(workload.getAttribute("responseTime"));
			responseTimes.put(id, respTime);
		}
	}






	@Override
	public double getResponseTime(String resourceID) {
		if(responseTimes.get(resourceID)!=null)
			return responseTimes.get(resourceID);
		return -1;
	}

	@Override
	public double getUtilization(String resourceID) {
		if(utilizations.get(resourceID)!=null)
			return utilizations.get(resourceID);
		return -1;

	}


	public static double getResponseTimeOfSubActivities(TaskType task) throws ParseException {
		// We add all result service times of the usage scenario to compute
		// the response time
		// TODO: check whether this works correctly if the usage scenario
		// contains branches
		double time = 0;
		EList<ActivityDefType> activities = task.getTaskActivities()
				.getActivity();
		for (ActivityDefType activity : activities) {
			EList<OutputResultType> results = activity.getResultActivity();
			for (OutputResultType outputResultType : results) {

				time += convertStringToDouble(outputResultType.getServiceTime().toString());
			}

		}
		return time;
	}
	public static double convertStringToDouble(String toConvert) throws ParseException {
		double ret;

		toConvert = toConvert.replaceAll("e", "E");
		toConvert = toConvert.replaceAll("\\+", "");
		DecimalFormat format = new DecimalFormat("0.0E000",
				DecimalFormatSymbols.getInstance(Locale.ENGLISH));
		ret = format.parse(toConvert).doubleValue();

		return ret;
	}

	public HashMap<String, Double> getUtilizations() {
		return utilizations;
	}

	public HashMap<String, Double> getResponseTimes() {
		return responseTimes;
	}

}
