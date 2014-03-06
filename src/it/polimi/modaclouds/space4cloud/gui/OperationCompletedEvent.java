/*
 * 
 */
package it.polimi.modaclouds.space4cloud.gui;

import java.util.EventObject;

// TODO: Auto-generated Javadoc
/**
 * Represents the Event that is fired when a blocking application window
 * terminates.
 * 
 * @author Davide Franceschelli
 * 
 */
public class OperationCompletedEvent extends EventObject {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 6510332390074819542L;

	/**
	 * Constructor.
	 * 
	 * @param source
	 *            is the Object injected when the event is fired.
	 */
	public OperationCompletedEvent(Object source) {
		super(source);
	}

}