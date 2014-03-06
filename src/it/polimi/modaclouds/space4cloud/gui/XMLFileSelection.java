/*
 * 
 */
package it.polimi.modaclouds.space4cloud.gui;

import it.polimi.modaclouds.space4cloud.utils.Constants;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

// TODO: Auto-generated Javadoc
/**
 * Application Window used to show the content of a Resource Model (Resource
 * Containers + Processing Resources). It allows to complete empty containers
 * with Processing Resources derived from Cloud Elements chosen with the
 * application window CloudResourceSelection. It allows to specify Efficiency
 * Profiles for the existing Processing Resources using the application window
 * EfficiencyProfileSelection.
 * 
 * @author Davide Franceschelli
 * @see CloudResourceSelection
 * @see EfficiencyProfileSpecification
 */
public class XMLFileSelection {


	File file = null;

	/**
	 * Creates the window and asks to search for the file.
	 * @param string 
	 */
	public  XMLFileSelection(String title) 
	{		
		Constants c = Constants.getInstance();

		JFileChooser fileChooser = new JFileChooser(c.ABSOLUTE_WORKING_DIRECTORY);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("XML files", "xml");
		fileChooser.setFileFilter(filter);
		fileChooser.setDialogTitle(title);
		int returnVal = fileChooser.showOpenDialog(null);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = fileChooser.getSelectedFile();			
		} else {
			System.out.println("Open command cancelled by user.");
		}

	}

	public File getFile() {
		return file;
	}
}

