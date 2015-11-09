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
package it.polimi.modaclouds.space4cloud.types;

// TODO: Auto-generated Javadoc
/**
 * Enumeration representing the Palladio Scheduling Types.
 * 
 * @author Davide Franceschelli
 * 
 */
public enum SchedulingT {

	/** The fcfs. */
	FCFS("First-Come-First-Served",
			"pathmap://PCM_MODELS/Palladio.resourcetype#FCFS"),
	/** The ps. */
	PS("Processor Sharing",
			"pathmap://PCM_MODELS/Palladio.resourcetype#ProcessorSharing"),
	/** The delay. */
	DELAY("Delay", "pathmap://PCM_MODELS/Palladio.resourcetype#Delay"),
	/** The nd. */
	ND("ND", "");

	/** The pathmap. */
	private final String pathmap;

	/** The name. */
	private final String name;

	/**
	 * Instantiates a new scheduling t.
	 * 
	 * @param name
	 *            the name
	 * @param pathMap
	 *            the path map
	 */
	SchedulingT(String name, String pathMap) {
		pathmap = pathMap;
		this.name = name;
	}

	/**
	 * Returns the name of the current scheduling type.
	 * 
	 * @return the String representing the current scheduling type.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the pathmap corresponding to the "href" attribute value for this
	 * scheduling type.
	 * 
	 * @return a String representing the pathmap of this scheduling type.
	 */
	public String getPathmap() {
		return pathmap;
	}

}
