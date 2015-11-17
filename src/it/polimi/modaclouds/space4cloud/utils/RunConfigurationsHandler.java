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
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.uka.ipd.sdq.pcmsolver.runconfig.MessageStrings;
import it.polimi.modaclouds.space4cloud.utils.Configuration.Solver;

// TODO: Auto-generated Javadoc
/**
 * The Class RunConfigurationsDerivation.
 */
public class RunConfigurationsHandler {
	private static final Logger logger = LoggerFactory.getLogger(RunConfigurationsHandler.class);
	/** The launch configs. */
	private ILaunchConfiguration launchConfig;

	/** The launch configuration file **/
	private File launchConfigurationFile;

	/** The doc. */
	private Document doc;

	/** The root. */
	private Element root;

	/**
	 * Instantiates a new run configurations derivation.
	 */
	public RunConfigurationsHandler() {
		
		// create the launch configuration
		Path launchConfigPath = Paths.get(Configuration.PROJECT_BASE_FOLDER,Configuration.WORKING_DIRECTORY,Configuration.LAUNCH_CONFIG);		
		logger.debug("Initializing run configuration from: "+launchConfigPath);
		URL template = null;

		// choose the template according to the solver
		if (Configuration.SOLVER == Solver.LQNS)
			template = getClass().getResource("/launch_configs/LQNS.launch");
		else if (Configuration.SOLVER == Solver.LINE)
			template = getClass().getResource("/launch_configs/PerformanceEngine.launch");
		else			
			logger.error("Wrong Solver Selection");
		
//			template = getClass().getResource("/launch_configs/SimuCom.launch");

		logger.debug("Using template: " + template);

		// copy the template to be modified
		Path templatePath = null;
		try {

			// URI templateURI =
			// URI.create(URLEncoder.encode(FileLocator.toFileURL(template).toString(),
			// "UTF-8"));
			// logger.debug("Template URI: "+templateURI);
			// templatePath = Paths.get(templateURI);
			// logger.debug("Template Path: "+templatePath);
			Files.copy(template.openStream(), launchConfigPath,
					StandardCopyOption.REPLACE_EXISTING);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("error in copying the launch configuration from: "
					+ templatePath + " to " + launchConfigPath, e);
		}
		launchConfigurationFile = launchConfigPath.toFile();
		doc = DOM.getDocument(launchConfigurationFile);
		if (doc != null)
			root = doc.getDocumentElement();

		// fill the template with the data from Constants
		NodeList nl = root.getElementsByTagName("stringAttribute");
		Element e_alloc = null, e_usage = null, e_repo = null, e_out = null, e_line_prop = null;

		for (int i = 0; i < nl.getLength(); i++) {
			Element x = (Element) nl.item(i);
			if (x.getAttribute("key").equals("allocationFile"))
				e_alloc = x;
			else if (x.getAttribute("key").equals("mwRepositoryFile"))
				e_repo = x;
			else if (x.getAttribute("key").equals("usageFile"))
				e_usage = x;
			else if (x.getAttribute("key").equals(
					MessageStrings.LQNS_OUTPUT_DIR)
					|| x.getAttribute("key").equals(
							MessageStrings.LINE_OUT_DIR))
				//TODO:check patch versions
				e_out = x;
			else if (x.getAttribute("key").equals("perfEngPropFile"))
				e_line_prop = x;
		}
		e_repo.setAttribute("value", Configuration.PALLADIO_REPOSITORY_MODEL);
		e_alloc.setAttribute("value", Configuration.PALLADIO_ALLOCATION_MODEL);
		e_usage.setAttribute("value", Configuration.PALLADIO_USAGE_MODEL);
		String outPath = Paths.get(Configuration.PROJECT_BASE_FOLDER,Configuration.WORKING_DIRECTORY,Configuration.PERFORMANCE_RESULTS_FOLDER).toString();
//		if(!outPath.endsWith("\\")){
//			outPath += "\\";
//		}
		e_out.setAttribute("value", outPath.toString());
		if (e_line_prop != null) {
			e_line_prop.setAttribute("value", Configuration.LINE_PROP_FILE);
		}

		// save the modified configuration
		DOM.serialize(doc, launchConfigurationFile);
	}

	/**
	 * Launch.
	 */
	public void launch() throws PalladioRunException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		Path launchConfigPath = Paths.get(Configuration.PROJECT_BASE_FOLDER,Configuration.WORKING_DIRECTORY,Configuration.LAUNCH_CONFIG);
		IPath location = org.eclipse.core.runtime.Path.fromOSString(launchConfigPath.toString());
		@SuppressWarnings("deprecation")
		IFile ifile = workspace.getRoot().findFilesForLocation(location)[0]; // getFileForLocation(location);
		logger.debug("launching from:"+location.toPortableString()+" ifile: "+ifile);
		launchConfig = DebugPlugin.getDefault().getLaunchManager()
				.getLaunchConfiguration(ifile);
		try {
			launchConfig.launch("run", new NullProgressMonitor());		
		} catch (CoreException | RuntimeException e) {
			throw new PalladioRunException("Error while running launch configuration "+launchConfig.getName(),e);			
		}
	}

}
