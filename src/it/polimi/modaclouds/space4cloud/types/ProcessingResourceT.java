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
 HDD(
			"HDD",
			"pathmap://PCM_MODELS/Palladio.resourcetype#_BIjHoQ3KEdyouMqirZIhzQ"), 
 /** The delay. */
 DELAY(
			"DELAY",
			"pathmap://PCM_MODELS/Palladio.resourcetype#_nvHX4KkREdyEA_b89s7q9w"), 
 /** The nd. */
 ND(
			"ND", "");

	/** The pathmap. */
	private final String pathmap;
	
	/** The name. */
	private final String name;

	/**
	 * Instantiates a new processing resource t.
	 *
	 * @param name the name
	 * @param pathMap the path map
	 */
	private ProcessingResourceT(String name, String pathMap) {
		pathmap = pathMap;
		this.name = name;
	}

	/**
	 * Gets the pathmap.
	 *
	 * @return the pathmap
	 */
	public String getPathmap() {
		return pathmap;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}
}
