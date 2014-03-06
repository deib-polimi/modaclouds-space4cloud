/*
 * 
 */
package it.polimi.modaclouds.space4cloud.iterfaces;

import java.io.File;
import java.util.List;

import org.w3c.dom.Element;

// TODO: Auto-generated Javadoc
/**
 * The Interface UsageModelXMLBuilder.
 */
public interface UsageModelXMLBuilder {

	/**
	 * Adds an Open Workload to the Usage Model.
	 * 
	 * @param usageScenario
	 *            is the scenario to which has to be associated the workload.
	 * @param interarrivalTime
	 *            is the interarrival time of the requests.
	 * @return the Element representing the generated workload if the operation
	 *         succeeds, null otherwise.
	 */
	public Element addOpenWorkload(Element usageScenario,
			double interarrivalTime);

	/**
	 * Adds an Open Workload to the Usage Model.
	 * 
	 * @param usageScenario
	 *            is the scenario to which has to be associated the workload.
	 * @param population
	 *            is the population of the closed workload.
	 * @param thinkTime
	 *            is the think time related to the closed workload.
	 * @return the Element representing the generated workload if the operation
	 *         succeeds, null otherwise.
	 */
	public Element addClosedWorkload(Element usageScenario, int population,
			double thinkTime);

	/**
	 * Retrieves the list of all the usage scenarios within the Usage Model.
	 * 
	 * @return a list of Element representing usage scenarios.
	 */
	public List<Element> getUsageScenarios();

	/**
	 * Change the href attribute referencing the System and Repository Models.
	 * 
	 * @param newPath
	 *            is the String representing the new path to set.
	 */
	public void changeSystemAndRepositoryModelsPath(String newPath);

	/**
	 * Loads an existent Usage Model.
	 * 
	 * @param model
	 *            is the file containing the XML representation of the model.
	 * @return true if the operation succeeds, false otherwise.
	 */
	public boolean loadModel(File model);

	/**
	 * Serialize the model in the XML format.
	 * 
	 * @return the file containing the XML version of the model.
	 */
	public File serializeModel();

	/**
	 * Serialize the model in the XML format in the specified output file.
	 * 
	 * @param outFile
	 *            is the output file.
	 * @return the generated file if the operation succeeds, null otherwise.
	 */
	public File serializeModel(File outFile);
}
