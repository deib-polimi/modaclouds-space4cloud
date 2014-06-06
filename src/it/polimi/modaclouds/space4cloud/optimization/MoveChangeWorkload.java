package it.polimi.modaclouds.space4cloud.optimization;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution;
import it.polimi.modaclouds.space4cloud.utils.UsageModelExtensionParser;

public class MoveChangeWorkload extends AbsMove {
	
	private ArrayList<MoveChangeWorkloadHour> hourly;
	
	/**
	 *  Constructor
	 */
	public MoveChangeWorkload(Solution sol) {
		setSolution(sol);
		propertyNames = new ArrayList<String>();
		propertyValues = new ArrayList<Object>();
		
		hourly = new ArrayList<MoveChangeWorkloadHour>();
		for (int i = 0; i < 24;  ++i) {
			hourly.add(new MoveChangeWorkloadHour(sol, i));
		}
	}

	/* (non-Javadoc)
	 * @see it.polimi.modaclouds.space4cloud.optimization.AbsMove#apply()
	 */
	@Override
	public Solution apply() {
		for (MoveChangeWorkloadHour move : hourly)
			move.apply();
		
		return currentSolution;
	}
	
	public IMove modifyWorkload(File usageModelExtension, double[] rates)
			throws ParserConfigurationException, SAXException, IOException, JAXBException {
		UsageModelExtensionParser usageModelParser = new UsageModelExtensionParser(
				usageModelExtension);
		
		Integer populations[];
		
		if (usageModelParser.getPopulations().size() == 1)
			populations = usageModelParser.getPopulations().values().iterator().next();
		else
			return this;
		
		for (int i = 0; i < 24; ++i) {
			hourly.get(i).modifyWorkload(populations[i], rates[i]);
		}
		
		apply();
		
		return this;
		
	}
	
	public IMove modifyWorkload(File usageModelExtension, double rate)
			throws ParserConfigurationException, SAXException, IOException, JAXBException {
		double rates[] = new double[24];
		for (int i = 0; i < 23; ++i)
			rates[i] = rate;
		
		return modifyWorkload(usageModelExtension, rates);
		
	}

}
