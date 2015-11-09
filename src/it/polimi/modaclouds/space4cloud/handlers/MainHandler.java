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
package it.polimi.modaclouds.space4cloud.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import it.polimi.modaclouds.space4cloud.mainProgram.Main;

// TODO: Auto-generated Javadoc
/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class MainHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public MainHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 * 
	 * @param event
	 *            the event
	 * @return the object
	 * @throws ExecutionException
	 *             the execution exception
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		//
		// IWorkbenchWindow window =
		// HandlerUtil.getActiveWorkbenchWindowChecked(event);
		// MessageDialog.openInformation( window.getShell(), "Space4cloud",
		// "Start SPACE4CLOUD"); return null;
		//
		Main.main(null);
		return null;
	}
}
