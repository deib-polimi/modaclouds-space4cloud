/*
 * 
 */
package it.polimi.modaclouds.space4cloud.handlers;

import it.polimi.modaclouds.space4cloud.mainProgram.Main;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

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
	 * @param event the event
	 * @return the object
	 * @throws ExecutionException the execution exception
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		/*
		 * IWorkbenchWindow window =
		 * HandlerUtil.getActiveWorkbenchWindowChecked(event);
		 * MessageDialog.openInformation( window.getShell(), "Space4cloud",
		 * "Start SPACE4CLOUD"); return null;
		 */
		Main.main(null);
		return null;
	}
}
