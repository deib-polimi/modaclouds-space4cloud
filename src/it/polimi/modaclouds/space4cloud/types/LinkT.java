/*
 * 
 */
package it.polimi.modaclouds.space4cloud.types;

// TODO: Auto-generated Javadoc
/**
 * The Enum LinkT.
 */
public enum LinkT {
	
	/** The lan. */
	LAN("LAN",
			"pathmap://PCM_MODELS/Palladio.resourcetype#_o3sScH2AEdyH8uerKnHYug"),
	
	/** The nd. */
	ND("ND", "");

	/** The pathmap. */
	private final String pathmap;
	
	/** The name. */
	private final String name;

	/**
	 * Instantiates a new link t.
	 *
	 * @param name the name
	 * @param p the p
	 */
	LinkT(String name, String p) {
		pathmap = p;
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
