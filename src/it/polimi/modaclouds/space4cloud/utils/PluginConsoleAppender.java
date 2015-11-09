package it.polimi.modaclouds.space4cloud.utils;

import java.io.OutputStreamWriter;

import org.apache.log4j.WriterAppender;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class PluginConsoleAppender extends WriterAppender {

	public static final String CONSOLE_NAME = "Space 4Clouds";
	MessageConsoleStream out = null;

	public PluginConsoleAppender() {
		super();
		MessageConsole myConsole = findConsole(CONSOLE_NAME);
		out = myConsole.newMessageStream();
		setWriter(new OutputStreamWriter(out));
	}

	@Override
	public void close() {

	}

	private MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}

}
