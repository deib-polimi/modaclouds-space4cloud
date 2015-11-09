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

import java.util.Collections;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import it.polimi.modaclouds.resourcemodel.cloud.CloudFactory;

// TODO: Auto-generated Javadoc
/**
 * Provides utility methods to work with Cloud Meta-Model EMF instances.
 * 
 * @author Davide Franceschelli
 */
public class EMF {

	/** The cloud factory. */
	private final CloudFactory cloudFactory;

	/**
	 * Initialize the class.
	 */
	public EMF() {
		cloudFactory = CloudFactory.eINSTANCE;
	}

	/**
	 * Returns the Cloud Factory needed to instantiate new EMF objects belonging
	 * to the Cloud Meta-Model.
	 * 
	 * @return a CloudFactory instance.
	 * @see CloudFactory
	 */
	public CloudFactory getCloudFactory() {
		return cloudFactory;
	}

	/**
	 * Serialize the EMF object in a File created within the specified path,
	 * with the specified name.
	 * 
	 * @param emfObject
	 *            is the EMF Object to serialize.
	 * @param pathName
	 *            is the String representing the absolute path of the file.
	 * @param fileName
	 *            is the name of the file.
	 * @return true if the operation succeeds, false otherwise.
	 */
	public boolean serialize(EObject emfObject, String pathName, String fileName) {
		try {
			String ext = fileName.substring(fileName.lastIndexOf(".") + 1);
			Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
			Map<String, Object> m = reg.getExtensionToFactoryMap();
			m.put(ext, new XMIResourceFactoryImpl());
			ResourceSet resSet = new ResourceSetImpl();
			Resource resource = resSet.createResource(URI
					.createFileURI(pathName + fileName));
			resource.getContents().add(emfObject);
			resource.save(Collections.EMPTY_MAP);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
