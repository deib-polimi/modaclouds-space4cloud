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
 * The Enum ProcessingResourceT.
 */
public enum ProcessingResourceT {

	/** The cpu. */
	CPU("CPU",
			"pathmap://PCM_MODELS/Palladio.resourcetype#_oro4gG3fEdy4YaaT-RYrLQ"),
	/** The hdd. */
	HDD("HDD",
			"pathmap://PCM_MODELS/Palladio.resourcetype#_BIjHoQ3KEdyouMqirZIhzQ"),
	/** The delay. */
	DELAY("DELAY",
			"pathmap://PCM_MODELS/Palladio.resourcetype#_nvHX4KkREdyEA_b89s7q9w"),
	/** The nd. */
	ND("ND", "");

	/** The pathmap. */
	private final String pathmap;

	/** The name. */
	private final String name;

	/**
	 * Instantiates a new processing resource t.
	 * 
	 * @param name
	 *            the name
	 * @param pathMap
	 *            the path map
	 */
	private ProcessingResourceT(String name, String pathMap) {
		pathmap = pathMap;
		this.name = name;
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the pathmap.
	 * 
	 * @return the pathmap
	 */
	public String getPathmap() {
		return pathmap;
	}
}
