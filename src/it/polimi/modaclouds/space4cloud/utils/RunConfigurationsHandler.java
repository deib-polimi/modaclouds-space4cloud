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
package it.polimi.modaclouds.space4cloud.utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.uka.ipd.sdq.pcmsolver.runconfig.MessageStrings;


// TODO: Auto-generated Javadoc
/**
 * The Class RunConfigurationsDerivation.
 */
public class RunConfigurationsHandler 
{

	/** The launch configs. */
	private ILaunchConfiguration launchConfig;

	/**	The launch configuration file**/
	private File launchConfigurationFile;

	/** The doc. */
	private Document doc;

	/** The root. */
	private Element root;

	/** The c. */
	private Constants constant = Constants.getInstance();

	/**
	 * Instantiates a new run configurations derivation.
	 */
	public RunConfigurationsHandler() 
	{

		// create the launch configuration
		Path launchConfigPath = Paths.get(constant.ABSOLUTE_WORKING_DIRECTORY, constant.LAUNCH_CONFIG);
		URL template;			

		//choose the template according to the solver
		if (constant.SOLVER.equals(MessageStrings.LQNS_SOLVER))
			template = getClass().getResource("/launch_configs/LQNS.launch");
		else if (constant.SOLVER.equals(MessageStrings.PERFENGINE_SOLVER))
			template = getClass().getResource("/launch_configs/PerformanceEngine.launch");
		else
			template = getClass().getResource("/launch_configs/SimuCom.launch");

		//copy the template to be modified
		try {
			Path templatePath = Paths.get(FileLocator.toFileURL(template).toURI());
			Files.copy(templatePath, launchConfigPath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		launchConfigurationFile = launchConfigPath.toFile();
		doc = DOM.getDocument(launchConfigurationFile);
		if (doc != null)
			root = doc.getDocumentElement();		

		//fill the template with the data from Constants
		NodeList nl = root.getElementsByTagName("stringAttribute");
		Element e_alloc = null, 
				e_usage = null, 
				e_repo = null, 
				e_out = null,
				e_line_prop = null;			

		for (int i = 0; i < nl.getLength(); i++) 
		{
			Element x = (Element) nl.item(i);
			if (x.getAttribute("key").equals("allocationFile"))
				e_alloc = x;
			else if (x.getAttribute("key").equals("mwRepositoryFile"))
				e_repo = x;
			else if (x.getAttribute("key").equals("usageFile"))
				e_usage = x;
			else if (x.getAttribute("key").equals(MessageStrings.LQNS_OUTPUT_DIR) || x.getAttribute("key").equals(MessageStrings.PERF_ENG_OUT_DIR))
				e_out = x;
			else if (x.getAttribute("key").equals("perfEngPropFile"))
				e_line_prop = x;
		}
		e_repo.setAttribute("value", constant.REPOSITORY_MODEL);
		e_alloc.setAttribute("value", constant.ALLOCATION_MODEL);
		e_usage.setAttribute("value", constant.USAGE_MODEL);
		e_out.setAttribute("value", constant.ABSOLUTE_WORKING_DIRECTORY+constant.PERFORMANCE_RESULTS_FOLDER);
		if(e_line_prop != null){
			e_line_prop.setAttribute("value", constant.LINE_PROPERTIES_FILE);
		}

		//save the modified configuration
		DOM.serialize(doc, launchConfigurationFile);



		//refresh the project in the workspace
		try {
			ResourcesPlugin
			.getWorkspace()
			.getRoot()
			.getProject(constant.PROJECT_NAME)
			.refreshLocal(IResource.DEPTH_INFINITE,new NullProgressMonitor());
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}




	/**
	 * Launch.
	 */
	public void launch() 
	{	
		IWorkspace workspace= ResourcesPlugin.getWorkspace();    
		IPath location= org.eclipse.core.runtime.Path.fromOSString(Paths.get(constant.ABSOLUTE_WORKING_DIRECTORY, constant.LAUNCH_CONFIG).toString()); 
		IFile ifile= workspace.getRoot().getFileForLocation(location);
		launchConfig = DebugPlugin.getDefault().getLaunchManager().getLaunchConfiguration(ifile);
		try {
			launchConfig.launch("run", new NullProgressMonitor()); 

			//refresh the project in the workspace
			ResourcesPlugin
			.getWorkspace()
			.getRoot()
			.getProject(constant.PROJECT_NAME)
			.refreshLocal(IResource.DEPTH_INFINITE,new NullProgressMonitor());
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}





}
