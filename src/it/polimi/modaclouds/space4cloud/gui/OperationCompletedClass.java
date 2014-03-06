/*
 * 
 */
package it.polimi.modaclouds.space4cloud.gui;

// TODO: Auto-generated Javadoc
/**
 * Represents the class related to the OperationCompletedEvent. It provides the
 * methods needed to add/remove OperationCompletedListeners and to fire the
 * OperationCompletedEvent.
 * 
 * @author Davide Franceschelli
 * @see OperationCompletedEvent
 * @see OperationCompletedListener
 */
public class OperationCompletedClass {

	/** The listener list. */
	protected javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();

	/**
	 * Adds the specified listener to the list of listeners.
	 * 
	 * @param listener
	 *            is the Object extending the OperationCompletedListener
	 *            interface to add to the listeners list.
	 * @see OperationCompletedListener
	 */
	public void addMyEventListener(OperationCompletedListener listener) {
		listenerList.add(OperationCompletedListener.class, listener);
	}

	/**
	 * Removes the specified listener from the list of all listeners.
	 * 
	 * @param listener
	 *            is the Object extending the OperationCompletedListener
	 *            interface to remove from the listeners list.
	 * @see OperationCompletedListener
	 */
	public void removeMyEventListener(OperationCompletedListener listener) {
		listenerList.remove(OperationCompletedListener.class, listener);
	}

	/**
	 * Fires the OperationCompletedEvent.
	 * 
	 * @param evt
	 *            is the OperationCompletedEvent object.
	 * @see OperationCompletedEvent
	 */
	void fireMyEvent(OperationCompletedEvent evt) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = 0; i < listeners.length; i += 2) {
			if (listeners[i] == OperationCompletedListener.class) {
				((OperationCompletedListener) listeners[i + 1])
						.operationCompletedEventOccurred(evt);
			}
		}
	}
}
