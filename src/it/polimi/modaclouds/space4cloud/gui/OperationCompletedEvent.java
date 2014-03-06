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
