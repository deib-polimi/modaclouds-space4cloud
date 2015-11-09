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
	 * Change the href attribute referencing the System and Repository Models.
	 * 
	 * @param newPath
	 *            is the String representing the new path to set.
	 */
	public void changeSystemAndRepositoryModelsPath(String newPath);

	/**
	 * Retrieves the list of all the usage scenarios within the Usage Model.
	 * 
	 * @return a list of Element representing usage scenarios.
	 */
	public List<Element> getUsageScenarios();

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
