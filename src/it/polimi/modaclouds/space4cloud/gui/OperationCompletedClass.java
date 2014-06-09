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
}
