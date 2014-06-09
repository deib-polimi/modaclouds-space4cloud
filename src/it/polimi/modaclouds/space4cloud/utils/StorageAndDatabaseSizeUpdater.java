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

import it.polimi.modaclouds.resourcemodel.cloud.CloudElement;
import it.polimi.modaclouds.resourcemodel.cloud.CloudPlatform;
import it.polimi.modaclouds.resourcemodel.cloud.CloudResource;
import it.polimi.modaclouds.resourcemodel.cloud.CloudStorage;
import it.polimi.modaclouds.resourcemodel.cloud.Cost;
import it.polimi.modaclouds.resourcemodel.cloud.CostProfile;
import it.polimi.modaclouds.resourcemodel.cloud.Database;
import it.polimi.modaclouds.resourcemodel.cloud.V_Storage;
import it.polimi.modaclouds.resourcemodel.cloud.VirtualHWResource;
import it.polimi.modaclouds.resourcemodel.cloud.VirtualHWResourceType;

import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * Provides utility methods to update the size of Cloud Storage or Database
 * elements.
 * 
 * @author Davide Franceschelli
 * @see CloudStorage
 * @see Database
 */
public class StorageAndDatabaseSizeUpdater {

	/**
	 * Updates the size of the V_Storage Virtual Hardware Resources associated
	 * to Cloud Resources or Cloud Platforms. It recognizes automatically
	 * whether the Cloud Element is a Cloud Resource or a Cloud Platform.
	 * 
	 * @param ce
	 *            is the generic CloudElement.
	 * @param size
	 *            is the size value to set.
	 * @param deep
	 *            if the specified CloudElement is an instance of a
	 *            CloudPlatform, it indicates whether to consider or not also
	 *            the CloudResources associated to the specified CloudPlatform.
	 * @see V_Storage
	 * @see CloudElement
	 * @see CloudPlatform
	 * @see CloudResource
	 * @see #updateSize(CloudResource, int)
	 * @see #updateSize(CloudPlatform, int)
	 */
	public static void updateSize(CloudElement ce, int size, boolean deep) {
		if (ce instanceof CloudPlatform) {
			updateSize((CloudPlatform) ce, size, deep);
			return;
		} else if (ce instanceof CloudResource) {
			updateSize((CloudResource) ce, size);
			return;
		} else
			return;
	}

	/**
	 * Updates the size of V_Storage Virtual Hardware Resources associated to
	 * the specified Cloud Platform (directly through costs or indirectly
	 * through Cloud Resources).
	 * 
	 * @param cp
	 *            is the CloudPlatform object.
	 * @param size
	 *            is the size value to set.
	 * @param deep
	 *            indicates whether to consider or not also the CloudResources
	 *            associated to the specified CloudPlatform.
	 * @see CloudPlatform
	 * @see V_Storage
	 * @see #updateSize(CloudResource, int)
	 */
	private static void updateSize(CloudPlatform cp, int size, boolean deep) {
		List<Cost> lc = cp.getHasCost();
		CostProfile cpro = cp.getHasCostProfile();
		if (lc != null)
			for (Cost c : lc) {
				VirtualHWResource v = c.getDefinedOn();
				if (v != null)
					if (v.getType().equals(VirtualHWResourceType.STORAGE)) {
						((V_Storage) v).setSize(size);
					}
			}
		if (cpro != null)
			for (Cost c : cpro.getComposedOf()) {
				VirtualHWResource v = c.getDefinedOn();
				if (v != null)
					if (v.getType().equals(VirtualHWResourceType.STORAGE)) {
						((V_Storage) v).setSize(size);
					}
			}
		if (deep) {
			List<CloudResource> lcr = cp.getRunsOnCloudResource();
			if (lcr != null)
				for (CloudResource cr : lcr)
					updateSize(cr, size);
		}
	}

	/**
	 * Updates the size of V_Storage Virtual Hardware Resources which are
	 * directly or indirectly associated to the specified Cloud Resource. Also
	 * the V_Storage Virtual Hardware Resources related to the Costs associated
	 * to the specified Cloud Resource are updated.
	 * 
	 * @param cr
	 *            is the CloudResource object.
	 * @param size
	 *            is the size value to set.
	 * @see V_Storage
	 * @see CloudResource
	 * @see #updateVHRs(CloudResource, int)
	 */
	private static void updateSize(CloudResource cr, int size) {
		List<Cost> lc = cr.getHasCost();
		CostProfile cp = cr.getHasCostProfile();
		if (lc != null)
			for (Cost c : lc) {
				VirtualHWResource v = c.getDefinedOn();
				if (v != null)
					if (v.getType().equals(VirtualHWResourceType.STORAGE)) {
						((V_Storage) v).setSize(size);
					}
			}
		if (cp != null)
			for (Cost c : cp.getComposedOf()) {
				VirtualHWResource v = c.getDefinedOn();
				if (v != null)
					if (v.getType().equals(VirtualHWResourceType.STORAGE)) {
						((V_Storage) v).setSize(size);
					}
			}
		updateVHRs(cr, size);
	}

	/**
	 * Updates the size of V_Storage Virtual Hardware Resources directly
	 * associated to the specified Cloud Resource.
	 * 
	 * @param cr
	 *            is the CloudResource object.
	 * @param size
	 *            is the size value to set.
	 * @see V_Storage
	 * @see CloudResource
	 */
	private static void updateVHRs(CloudResource cr, int size) {
		List<VirtualHWResource> lvhr = cr.getComposedOf();
		if (lvhr != null)
			for (VirtualHWResource v : lvhr)
				if (v.getType().equals(VirtualHWResourceType.STORAGE))
					((V_Storage) v).setSize(size);
	}

	/**
	 * Instantiates a new storage and database size updater.
	 */
	private StorageAndDatabaseSizeUpdater() {
	}
}
