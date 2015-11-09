package it.polimi.modaclouds.space4cloud.optimization;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution;
import it.polimi.modaclouds.space4cloud.utils.Configuration;
import it.polimi.modaclouds.space4cloud.utils.Rounder;
import it.polimi.modaclouds.space4cloud.utils.UsageModelExtensionParser;

public class MoveChangeWorkload extends AbsMove {

	private ArrayList<MoveChangeWorkloadHour> hourly;

	/**
	 * Constructor
	 */
	public MoveChangeWorkload(Solution sol) {
		setSolution(sol);
		propertyNames = new ArrayList<String>();
		propertyValues = new ArrayList<Object>();

		hourly = new ArrayList<MoveChangeWorkloadHour>();
		for (int i = 0; i < 24; ++i) {
			hourly.add(new MoveChangeWorkloadHour(sol, i));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.polimi.modaclouds.space4cloud.optimization.AbsMove#apply()
	 */
	@Override
	public Solution apply() {
		for (MoveChangeWorkloadHour move : hourly)
			move.apply();

		return currentSolution;
	}
	
	public IMove modifyWorkload(double[] rates)
			throws ParserConfigurationException, SAXException, IOException,
			JAXBException {
		
		return modifyWorkload(new File(Configuration.USAGE_MODEL_EXTENSION), rates);
	}

	public IMove modifyWorkload(File usageModelExtension, double[] rates)
			throws ParserConfigurationException, SAXException, IOException,
			JAXBException {
		UsageModelExtensionParser usageModelParser = new UsageModelExtensionParser(
				usageModelExtension);

		Integer populations[];

		if (usageModelParser.getPopulations().size() == 1)
			populations = usageModelParser.getPopulations().values().iterator()
					.next();
		else
			return this;

		for (int i = 0; i < 24; ++i) {
			if (rates[i] < 0)
				rates[i] = 0;
			else if (rates[i] > 1)
				rates[i] = 1;
			
			hourly.get(i).modifyWorkload(populations[i], Rounder.round(rates[i]));
		}

		apply();

		return this;

	}
	
	public IMove modifyWorkload(int hour, double rate)
			throws ParserConfigurationException, SAXException, IOException,
			JAXBException {
		
		return modifyWorkload(new File(Configuration.USAGE_MODEL_EXTENSION), hour, rate);
	}
	
	public IMove modifyWorkload(File usageModelExtension, int hour, double rate)
			throws ParserConfigurationException, SAXException, IOException,
			JAXBException {
		
		if (hour < 0)
			hour = 0;
		else if (hour > 23)
			hour = 23;
		
		if (rate < 0)
			rate = 0;
		else if (rate > 1)
			rate = 1;
		
		
		UsageModelExtensionParser usageModelParser = new UsageModelExtensionParser(
				usageModelExtension);

		Integer populations[];

		if (usageModelParser.getPopulations().size() == 1)
			populations = usageModelParser.getPopulations().values().iterator()
					.next();
		else
			return this;

		hourly.get(hour).modifyWorkload(populations[hour], rate);

		apply();

		return this;

	}

}