package it.polimi.modaclouds.space4cloud.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import it.polimi.modaclouds.qos_models.schema.ClosedWorkloadElement;
import it.polimi.modaclouds.qos_models.schema.OpenWorkloadElement;
import it.polimi.modaclouds.qos_models.schema.UsageModelExtension;
import it.polimi.modaclouds.qos_models.schema.UsageModelExtensions;
import it.polimi.modaclouds.qos_models.util.XMLHelper;

public class UsageModelExtensionParser {

	private Map<String, Integer[]> populations = new HashMap<String, Integer[]>();
	private Map<String, Double[]> thinkTimes = new HashMap<String, Double[]>();
	private Map<String, Double[]> arrivalRates = new HashMap<String, Double[]>();
	private File extensionFile;
	private UsageModelExtension extension = null;


	
	public UsageModelExtensionParser(File extensionFile)
			throws ParserConfigurationException, SAXException, IOException,
			JAXBException {
		this.extensionFile = extensionFile;
		// load from the XML
		UsageModelExtensions loadedExtension = XMLHelper.deserialize(
				getExtension().toURI().toURL(), UsageModelExtensions.class);

		extension = loadedExtension.getUsageModelExtension();
		Integer[] population = new Integer[24];
		Double[] rates = new Double[24];
		if (extension.getClosedWorkload() != null) {
			Double[] thinkTime = new Double[24];
			for (ClosedWorkloadElement wlElement : extension
					.getClosedWorkload().getWorkloadElement()) {
				thinkTime[wlElement.getHour()] = Double.parseDouble(new Float(
						wlElement.getThinkTime()).toString());
				;
				population[wlElement.getHour()] = wlElement.getPopulation();
			}
			thinkTimes.put(extension.getScenarioId(), thinkTime);			
			populations.put(extension.getScenarioId(), population);	
		} else if (extension.getOpenWorkload() != null) {
			for (OpenWorkloadElement wlElement : extension.getOpenWorkload()
					.getWorkloadElement()) {
				rates[wlElement.getHour()] = (double) wlElement.getPopulation();
			}
			arrivalRates.put(extension.getScenarioId(), rates);
		}

		
	}

	protected File getExtension() {
		return extensionFile;
	}

	public Map<String, Integer[]> getPopulations() {
		return populations;
	}

	public Map<String, Double[]> getThinkTimes() {
		return thinkTimes;
	}

	
	public Map<String, Double[]> getArrivalRates() {
		return arrivalRates;
	}

	public boolean isClosedWorkload() {
		return extension.getClosedWorkload() != null;
	}
}
