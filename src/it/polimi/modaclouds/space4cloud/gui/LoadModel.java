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

import it.polimi.modaclouds.space4cloud.utils.LoggerHelper;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.eclipse.core.resources.ResourcesPlugin;
import org.slf4j.Logger;

// TODO: Auto-generated Javadoc
/**
 * Application Window used to load model files.
 * 
 * @author Davide Franceschelli
 * 
 */
public class LoadModel extends OperationCompletedClass {

	private static final Logger programLogger = LoggerHelper
			.getLogger(LoadModel.class);

	public static List<File> explore(File root, final String ext) {
		return explore(root, ext, Integer.MIN_VALUE);
	}

	/**
	 * Retrieves the list of all the file with the specified extension which are
	 * within a sub-path of the specified root.
	 * 
	 * @param root
	 *            is the root of the recursive search.
	 * @param ext
	 *            is the file extension to search.
	 * @return the list of all the files with the specified extension which are
	 *         in a sub-path of the specified root.
	 */
	public static List<File> explore(File root, final String ext, int depth) {

		List<File> lf = new ArrayList<File>();
		if (depth > 2)
			return lf;

		if (root.isFile() && root.getName().endsWith(ext)) {
			lf.add(root);
			return lf;
		}

		File[] list = root.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return (dir.isFile() && name.endsWith(ext))
						|| dir.isDirectory();
			}
		});

		if (list == null)
			return lf;
		for (File f : list) {
			lf.addAll(explore(f, ext, depth + 1));
		}
		return lf;
	}

	/**
	 * Launch the application.
	 * 
	 * @param args
	 *            the arguments
	 * @wbp.parser.entryPoint
	 */
	public static void main(String[] args) {
		LoadModel lm = new LoadModel(null, "Resource Model",
				".resourceenvironment");
		System.out.println(lm.getModelFile());
	}

	/** The frm load model. */
	private JFrame frmLoadModel;

	/** The extension. */
	private String modelName, extension;

	/** The models. */
	private List<File> models;

	/** The list. */
	private JList list;

	/** true is the user performed a choiche */
	private boolean chosen = false;

	/** The output. */
	private File output;

	/** The lock. */
	private Object lock = new Object();

	/** The oplist. */
	private OperationCompletedListener oplist = null;

	/**
	 * Creates the application window.
	 * 
	 * @param listener
	 *            is the OperationCompletedListener waiting for this application
	 *            windows termination. The listener is expected to receive the
	 *            File corresponding to the chosen model when the
	 *            OperationCompletedEvent is fired. If the listener is null,
	 *            then the application window behaves like a blocking GUI (the
	 *            execution of the caller can continue only after its
	 *            termination). <br/>
	 *            DO NOT use null listener when the caller is another
	 *            application window.
	 * @param modelName
	 *            is the name of the model which will be displayed as the window
	 *            title.
	 * @param ext
	 *            is the extension of the model file to search.
	 */
	public LoadModel(OperationCompletedListener listener, String modelName,
			String ext) {
		if (listener != null)
			addMyEventListener(listener);
		oplist = listener;
		this.extension = ext;
		this.modelName = modelName;
		programLogger.info("initializing projects window");
		try {
			initialize();
		} catch (InterruptedException e) {
			programLogger.error("Error in loading projects", e);
		}
	}

	/**
	 * Centers the specified frame.
	 * 
	 * @param jf
	 *            is the frame to center.
	 */
	private void center(JFrame jf) {
		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension screenSize = tk.getScreenSize();
		int screenHeight = screenSize.height;
		int screenWidth = screenSize.width;
		int height = jf.getHeight();
		int width = jf.getWidth();
		jf.setLocation((screenWidth - width) / 2, (screenHeight - height) / 2);
	}

	/**
	 * Getter method for the chosen file.
	 * 
	 * @return the file which has been chosen in the form.
	 */
	public File getModelFile() {
		return output;
	}

	/**
	 * Initialize the contents of the frame.
	 * 
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	private void initialize() throws InterruptedException {

		// Frame creation and initialization
		frmLoadModel = new JFrame();

		setResourceModelIcon(extension, frmLoadModel);
		frmLoadModel.setTitle("Load");
		frmLoadModel.setResizable(false);
		frmLoadModel.setBounds(100, 100, 300, 300);
		frmLoadModel.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

		// questa cosa dovrebbe dipendere da come gestisce java gli eventi
		// passiamo un listener que creamo e gli diciamo come attuare quando
		// succede un evento
		frmLoadModel.addWindowListener(new WindowListener() {

			@Override
			public void windowActivated(WindowEvent arg0) {

			}

			@Override
			public void windowClosed(WindowEvent e) {

			}

			@Override
			public void windowClosing(WindowEvent e) {
				if (oplist != null)
					fireMyEvent(null);
				else
					synchronized (lock) {
						lock.notify();
					}
				frmLoadModel.dispose();
			}

			@Override
			public void windowDeactivated(WindowEvent e) {

			}

			@Override
			public void windowDeiconified(WindowEvent e) {

			}

			@Override
			public void windowIconified(WindowEvent e) {

			}

			@Override
			public void windowOpened(WindowEvent e) {
			}
		});

		// Panel Creation
		JPanel panel = new JPanel();
		frmLoadModel.getContentPane().add(panel, BorderLayout.SOUTH);

		// Button creation and its associate listener and handler functions
		JButton btnLoad = new JButton("Load");
		btnLoad.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Remember that output is a File.
				output = models.get(list.getSelectedIndex());
				chosen = true;

				if (LoadModel.this.oplist == null)
					synchronized (lock) {
						lock.notify();
					}
				else
					LoadModel.this.fireMyEvent(new OperationCompletedEvent(
							output));

				frmLoadModel.dispose(); // close the window
			}
		});
		panel.add(btnLoad); // add the button to the panel

		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				output = null;
				if (LoadModel.this.oplist == null)
					synchronized (lock) {
						lock.notify();
					}
				else
					LoadModel.this.fireMyEvent(null);
				frmLoadModel.dispose(); // close the window
			}
		});
		panel.add(btnCancel);

		String workspaceDir = "";
		try {
			// workspace directory
			workspaceDir = ResourcesPlugin.getWorkspace().getRoot()
					.getLocation().toOSString();
			programLogger.debug("Workspace dir:" + workspaceDir);
		} catch (Exception e) {
			workspaceDir = System.getProperty("user.dir");
			workspaceDir = workspaceDir.substring(0,
					workspaceDir.lastIndexOf("\\"));
			programLogger.debug("Workspace dir:" + workspaceDir, e);
		}

		// retrieving all the files with a certain extension in the workspace
		// directory
		models = explore(new File(workspaceDir), extension, 0);

		List<String> ls = new ArrayList<String>();
		for (File f : models)
			ls.add(f.getAbsolutePath().replace(workspaceDir, "").substring(1));

		// JList is a list where the elements cannot be added or removed.
		// It is only possible to add element through the constructor
		list = new JList(ls.toArray(new String[0]));
		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
			}
		}); //

		list.setBorder(new TitledBorder(null, modelName, TitledBorder.LEADING,
				TitledBorder.TOP, null, null));

		frmLoadModel.getContentPane().add(new JScrollPane(list),
				BorderLayout.CENTER);

		if (list.getModel().getSize() == 0) {
			list.setListData(new String[] { "", "ERROR", "",
					"There isn't any " + modelName + " in the project!" });
			list.setEnabled(false);
			btnLoad.setEnabled(false);
		} else
			list.setSelectedIndex(0); // selection of the first element of the
										// list

		center(frmLoadModel);
		frmLoadModel.setVisible(true); // set visible the frame
		if (oplist == null)
			synchronized (lock) {
				lock.wait();
			}
	}

	public boolean isChosen() {
		return chosen;
	}

	/**
	 * Sets special icons for recognized models.
	 * 
	 * @param ext
	 *            is the model file extension
	 * @param jf
	 *            id the main JFrame of the application.
	 */
	private void setResourceModelIcon(String ext, JFrame jf) {
		try {
			String filename = "";
			if (ext.equals(".resourceenvironment"))
				filename = "/icons/ResourceenvironmentDiagramFile.gif";
			else if ((ext.equals(".usagemodel")) || (ext.equals(".usagexml")))
				filename = "/icons/UsagemodelDiagramFile.gif";
			else if ((ext.equals(".allocation")))
				filename = "/icons/AllocationDiagramFile.gif";
			else if ((ext.equals(".efficiencyxml")))
				filename = "/icons/Efficiency.gif";
			else if ((ext.equals(".allocationxml")))
				filename = "/icons/Cloud.png";
			else
				return;
			jf.setIconImage(Toolkit.getDefaultToolkit().getImage(
					LoadModel.class.getResource(filename)));
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
}
