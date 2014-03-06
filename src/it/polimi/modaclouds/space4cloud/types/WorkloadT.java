/*
 * 
 */
package it.polimi.modaclouds.space4cloud.types;

// TODO: Auto-generated Javadoc
/**
 * The Enum WorkloadT.
 */
public enum WorkloadT {

	/** The open. */
	OPEN("usagemodel:OpenWorkload"), /** The closed. */
 CLOSED("usagemodel:ClosedWorkload");

	/** The type. */
	private final String type;

	/**
	 * Instantiates a new workload t.
	 *
	 * @param t the t
	 */
	private WorkloadT(String t) {
		type = t;
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public String getType() {
		return type;
	}
}
