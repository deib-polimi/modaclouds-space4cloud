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
package it.polimi.modaclouds.space4cloud.iterfaces;

import it.polimi.modaclouds.cloudmetamodel.cloud.Backend;
import it.polimi.modaclouds.cloudmetamodel.cloud.BlobStorage;
import it.polimi.modaclouds.cloudmetamodel.cloud.CloudPlatform;
import it.polimi.modaclouds.cloudmetamodel.cloud.CloudResource;
import it.polimi.modaclouds.cloudmetamodel.cloud.CloudStorage;
import it.polimi.modaclouds.cloudmetamodel.cloud.Compute;
import it.polimi.modaclouds.cloudmetamodel.cloud.Database;
import it.polimi.modaclouds.cloudmetamodel.cloud.FilesystemStorage;
import it.polimi.modaclouds.cloudmetamodel.cloud.Frontend;
import it.polimi.modaclouds.cloudmetamodel.cloud.IaaS_Service;
import it.polimi.modaclouds.cloudmetamodel.cloud.Middleware;
import it.polimi.modaclouds.cloudmetamodel.cloud.PaaS_Service;

import java.util.HashMap;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * Interface for a Provider Database Connaector.
 * @author Davide
 *
 */
public interface GenericDBConnector {

	/**
	 * Retrieves the list of IaaS Cloud Services.
	 * 
	 * @return a List of IaaS_Service.
	 * @see IaaS_Service
	 */
	public List<IaaS_Service> getIaaSServices();
	
	/**
	 * Gets the hash map of the Iaas Cloud Services.
	 *
	 * @return the hash map
	 */
	public HashMap<String, IaaS_Service> getIaaSServicesHashMap();

	/**
	 * Retrieves the list of Cloud Resources provided by the specified IaaS
	 * Service.
	 * 
	 * @param iaas is the IaaS_Service element.
	 * @return a List of Cloudresource elements.
	 * @see IaaS_Service
	 * @see CloudResource
	 */
	public List<CloudResource> getCloudResources(IaaS_Service iaas);

	/**
	 * Retrieves the list of the Compute Cloud Resources provided by the
	 * specified IaaS Service.
	 * 
	 * @param iaas
	 *            is the specified IaaS_Service element.
	 * @return a List of Compute elements.
	 * @see IaaS_Service
	 * @see Compute
	 */
	public List<Compute> getComputeCloudResources(IaaS_Service iaas);

	/**
	 * Retrieves the list of the Cloud Storage Cloud Resources provided by the
	 * specified IaaS Service.
	 * 
	 * @param iaas
	 *            is the specified IaaS_Service element.
	 * @return a List of CloudStorage elements.
	 * @see IaaS_Service
	 * @see CloudStorage
	 */
	public List<CloudStorage> getCloudStorageCloudResources(IaaS_Service iaas);

	/**
	 * Retrieves the list of the Filesystem Storage Cloud Resources provided by
	 * the specified IaaS Service.
	 * 
	 * @param iaas
	 *            is the specified IaaS_Service element.
	 * @return a List of FilesystemStorage elements.
	 * @see IaaS_Service
	 * @see FilesystemStorage
	 */
	public List<FilesystemStorage> getFileSystemStorageCloudResources(
			IaaS_Service iaas);

	/**
	 * Retrieves the list of the Blob Storage Cloud Resources provided by the
	 * specified IaaS Service.
	 * 
	 * @param iaas
	 *            is the specified IaaS_Service element.
	 * @return a List of BlobStorage elements.
	 * @see IaaS_Service
	 * @see BlobStorage
	 */
	public List<BlobStorage> getBlobStorageCloudResources(IaaS_Service iaas);

	/**
	 * Retrieves the list of PaaS Cloud Services.
	 * 
	 * @return a List of PaaS_Service elements.
	 * @see PaaS_Service
	 */
	public List<PaaS_Service> getPaaSServices();
	
	/**
	 * Gets the paas services hash map.
	 *
	 * @return the paas services hash map
	 */
	public HashMap<String, PaaS_Service> getPaaSServicesHashMap();

	/**
	 * Retrieves the list of Cloud Platforms provided by the specified PaaS
	 * Service.
	 * 
	 * @param paas
	 *            is the PaaS-Service element.
	 * @return a List of CloudPlatform elements.
	 * @see PaaS_Service
	 * @see CloudPlatform
	 */
	public List<CloudPlatform> getCloudPlatforms(PaaS_Service paas);

	/**
	 * Retrieves the list of the Frontend Cloud Platforms provided by the
	 * specified PaaS Service.
	 * 
	 * @param paas
	 *            is the specified PaaS_Service element.
	 * @return a List of Frontend elements.
	 * @see PaaS_Service
	 * @see Frontend
	 */
	public List<Frontend> getFrontendCloudPlatforms(PaaS_Service paas);

	/**
	 * Retrieves the list of the Middleware Cloud Platforms provided by the
	 * specified PaaS Service.
	 * 
	 * @param paas
	 *            is the specified PaaS_Service element.
	 * @return a List of Middleware elements.
	 * @see PaaS_Service
	 * @see Middleware
	 */
	public List<Middleware> getMiddlewareCloudPlatforms(PaaS_Service paas);

	/**
	 * Retrieves the list of the Backend Cloud Platforms provided by the
	 * specified PaaS Service.
	 * 
	 * @param paas
	 *            is the specified PaaS_Service element.
	 * @return a List of Backend elements.
	 * @see PaaS_Service
	 * @see Backend
	 */
	public List<Backend> getBackendCloudPlatforms(PaaS_Service paas);

	/**
	 * Retrieves the list of the Database Cloud Platforms provided by the
	 * specified PaaS Service.
	 * 
	 * @param paas
	 *            is the specified PaaS_Service element.
	 * @return a List of Database elements.
	 * @see PaaS_Service
	 * @see Database
	 */
	public List<Database> getDatabaseCloudPlatforms(PaaS_Service paas);

}
