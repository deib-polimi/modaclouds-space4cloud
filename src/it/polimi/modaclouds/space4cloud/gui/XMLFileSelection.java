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
	boolean canceled = true;
	Constants c;
	JFileChooser fileChooser;

	/**
	 * Creates the window and asks to search for the file.
	 * 
	 * @param string
	 */
	public XMLFileSelection(String title) {
		c = Constants.getInstance();
		fileChooser = new JFileChooser(c.ABSOLUTE_WORKING_DIRECTORY);
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"XML files", "xml");
		fileChooser.setFileFilter(filter);
		fileChooser.setDialogTitle(title);
	}

	public void askFile() {
		int returnVal = fileChooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = fileChooser.getSelectedFile();
			canceled = false;
		}
		else if(returnVal == JFileChooser.CANCEL_OPTION){
			file= null;
			canceled = true;
		}

	}

	public File getFile() {
		return file;
	}

	public boolean isCanceled() {
		return canceled;
	}
}
