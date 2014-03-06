/*
 * 
 */
package it.polimi.modaclouds.space4cloud.gui;

import java.util.EventListener;

// TODO: Auto-generated Javadoc
/**
 * Interface for the listeners of the OperationCompletedEvent.
 * 
 * @author Davide Franceschelli
 * @see OperationCompletedEvent
 * 
 */
public interface OperationCompletedListener extends EventListener {

	/**
	 * Operation completed event occurred.
	 *
	 * @param evt the evt
	 */
	public void operationCompletedEventOccurred(OperationCompletedEvent evt);

}
