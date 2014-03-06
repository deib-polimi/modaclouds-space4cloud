package it.polimi.modaclouds.space4cloud.lqn;

import java.io.File;
import java.io.IOException;
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

public class LQNSResultParser implements LqnResultParser {

	String  filePath;
	private DocumentBuilderFactory dbFactory;
	private DocumentBuilder dBuilder;
	private Document resultDOM;	
	private File resultFile;


	private HashMap<String, Double> utilizations = new HashMap<>();
	private HashMap<String, Double> responseTimes = new HashMap<>();


	public LQNSResultParser(String filePath) {

		try {

			this.filePath=filePath;
			resultFile = new File(filePath);
			dbFactory = DocumentBuilderFactory.newInstance();
			dBuilder = dbFactory.newDocumentBuilder();
			resultDOM = dBuilder.parse(resultFile);
			resultDOM.getDocumentElement().normalize();
		}

		catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		parse();
	}

	private void parse() {
		//parse Processors
		NodeList processors = resultDOM.getElementsByTagName("processor");
		for(int i=0; i< processors.getLength();i++){						

			//search for the result element						
			Element processor = (Element) processors.item(i);
			String id = processor.getAttribute("name");
			int cores = 1;
			if(!processor.getAttribute("multiplicity").isEmpty())
				cores = Integer.parseInt(processor.getAttribute("multiplicity"));

		
			//there should be exactly one
			Element resultProcessor = (Element)processors.item(i).getFirstChild().getNextSibling();
			double utilization = Double.parseDouble(resultProcessor.getAttribute("utilization"))/cores;

			//add the processor utilization to the hashmap
			//System.out.println("proc id: "+id+ " utilization: "+utilization);
			utilizations.put(id, utilization);

			String seffID=id.split("_")[2];
			NodeList resultActivities = processor.getElementsByTagName("result-activity");
			double serviceTime = 0;
			for(int j=0; j<resultActivities.getLength(); j++)
				serviceTime += Double.parseDouble(resultActivities.item(j).getAttributes().getNamedItem("service-time").getTextContent());
			responseTimes.put(seffID, serviceTime);


		}


		//TODO: parse other stuff
		dbFactory=null;
		resultFile = null;
		dBuilder = null;
		resultDOM = null;

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

	@Override
	public HashMap<String, Double> getUtilizations() {
		// TODO Auto-generated method stub
		return utilizations;
	}

	@Override
	public HashMap<String, Double> getResponseTimes() {
		// TODO Auto-generated method stub
		return responseTimes;
	}
	
	@Override
	protected void finalize() throws Throwable {
		if(resultFile.exists())
			resultFile.delete();
		super.finalize();
	}


}
