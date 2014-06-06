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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.slf4j.Logger;
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
	private static final Logger logger = LoggerHelper.getLogger(RunConfigurationsHandler.class);
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
		logger.debug("Initializing run configuration from: "+constant.ABSOLUTE_WORKING_DIRECTORY+" and "+constant.LAUNCH_CONFIG);
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
		
		logger.debug("Using template: "+template);

		//copy the template to be modified
		Path templatePath = null;
		try {						
			
//			URI templateURI = URI.create(URLEncoder.encode(FileLocator.toFileURL(template).toString(), "UTF-8"));
//			logger.debug("Template URI: "+templateURI);
//			templatePath = Paths.get(templateURI);
//			logger.debug("Template Path: "+templatePath);
			Files.copy(template.openStream(), launchConfigPath, StandardCopyOption.REPLACE_EXISTING);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("error in copying the launch configuration from: "+templatePath+" to "+launchConfigPath,e);
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
			logger.error("error in refreshing the workspace",e);
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
