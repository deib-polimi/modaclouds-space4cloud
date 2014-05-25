package it.polimi.modaclouds.space4cloud.optimization;

import it.polimi.modaclouds.qos_models.schema.ClosedWorkloadElement;
import it.polimi.modaclouds.qos_models.schema.OpenWorkloadElement;
import it.polimi.modaclouds.qos_models.schema.UsageModelExtension;
import it.polimi.modaclouds.qos_models.schema.UsageModelExtensions;
import it.polimi.modaclouds.qos_models.util.XMLHelper;
import it.polimi.modaclouds.space4cloud.utils.UsageModelExtensionParser;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class UsageModelExtensionLoader extends UsageModelExtensionParser {

	public UsageModelExtensionLoader(File extensionFile)
			throws ParserConfigurationException, SAXException, IOException, JAXBException {
		super(extensionFile,false);
		//load from the XML
		UsageModelExtensions  loadedExtension = XMLHelper.deserialize(
				getExtension().toURI().toURL(), UsageModelExtensions.class);
		
		UsageModelExtension extension = loadedExtension.getUsageModelExtension();
		Integer[] population = new Integer[24];
		if(extension.getClosedWorkload()!=null){
			Double[] thinkTime = new Double[24];			
			for(ClosedWorkloadElement wlElement: extension.getClosedWorkload().getWorkloadElement()){
				thinkTime[wlElement.getHour() - 1]=Double.parseDouble(new Float(wlElement.getThinkTime()).toString());;
				population[wlElement.getHour() - 1]=wlElement.getPopulation();
			}
			thinkTimes.put(extension.getScenarioId(), thinkTime);
		}
		else if(extension.getOpenWorkload()!=null){						
			for(OpenWorkloadElement wlElement: extension.getOpenWorkload().getWorkloadElement()){
				population[wlElement.getHour() - 1]=wlElement.getPopulation();
			}
		}
		
		populations.put(extension.getScenarioId(), population);
	}
	
}
