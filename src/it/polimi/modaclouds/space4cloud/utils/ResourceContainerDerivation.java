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

import java.util.List;

import it.polimi.modaclouds.resourcemodel.cloud.CloudPlatform;
import it.polimi.modaclouds.resourcemodel.cloud.CloudResource;
import it.polimi.modaclouds.resourcemodel.cloud.V_CPU;
import it.polimi.modaclouds.resourcemodel.cloud.V_Storage;
import it.polimi.modaclouds.resourcemodel.cloud.VirtualHWResource;
import it.polimi.modaclouds.space4cloud.types.ProcessingResourceT;
import it.polimi.modaclouds.space4cloud.types.SchedulingT;
import it.polimi.modaclouds.space4cloud.types.palladio.ProcessingResource;
import it.polimi.modaclouds.space4cloud.types.palladio.ResourceContainer;

// TODO: Auto-generated Javadoc
/**
 * Provides the methods needed to transform Cloud Resources and Cloud Platforms
 * into a Palladio Resource Containers.
 * 
 * @author Davide Franceschelli
 * @see CloudPlatform
 * @see CloudResource
 * @see ResourceContainer
 */
public class ResourceContainerDerivation {

	/** The Constant DEFAULT_MTTF. */
	private static final double DEFAULT_MTTF = 10000;

	/** The Constant DEFAULT_MTTR. */
	private static final double DEFAULT_MTTR = 10;

	/**
	 * Updates an existing Palladio Resource Container with the Processing
	 * Resources derived from the specified Cloud Platform. The derived Palladio
	 * Processing Resources will have default MTTF and MTTR attribute values.
	 * 
	 * @param rc
	 *            is the existing ResourceContainer.
	 * @param cp
	 *            is the CloudPlatform object.
	 * @return the updated ResourceContainer object.
	 * @see ResourceContainer
	 * @see CloudPlatform
	 */
	public static ResourceContainer completeResourceContainer(
			ResourceContainer rc, CloudPlatform cp) {
		return completeResourceContainer(rc, cp, DEFAULT_MTTF, DEFAULT_MTTR);
	}

	/**
	 * Updates an existing Palladio Resource Container with the Processing
	 * Resources derived from the specified Cloud Platform. The derived Palladio
	 * Processing Resources will have the specified MTTF and MTTR attribute
	 * values.
	 * 
	 * @param rc
	 *            is the existing ResourceContainer.
	 * @param cp
	 *            is the CloudPlatform object.
	 * @param MTTF
	 *            is the Mean Time To Failure to assign to the Processing
	 *            Resources within the container.
	 * @param MTTR
	 *            is the Mean Time To Repair to assign to the Processing
	 *            Resources within the container.
	 * @return a new ResourceContainer object.
	 * @see CloudPlatform
	 * @see ResourceContainer
	 */
	public static ResourceContainer completeResourceContainer(
			ResourceContainer rc, CloudPlatform cp, double MTTF, double MTTR) {
		boolean hasCPU = false;
		List<CloudResource> lcr = cp.getRunsOnCloudResource();
		ProcessingResource pr;
		if (lcr != null)
			if (lcr.size() > 0) {
				for (CloudResource cr : lcr) {
					List<VirtualHWResource> lvh = cr.getComposedOf();
					if (lvh != null)
						for (VirtualHWResource v : lvh)
							switch (v.getType()) {
							case CPU:
								V_CPU cpu = (V_CPU) v;
								hasCPU = true;
								pr = new ProcessingResource(
										ProcessingResourceT.CPU,
										SchedulingT.PS,
										cpu.getProcessingRate(),
										cpu.getNumberOfReplicas(), MTTF, MTTR);
								rc.addProcessingResource(pr);
								break;
							case STORAGE:
								V_Storage sto = (V_Storage) v;
								pr = new ProcessingResource(
										ProcessingResourceT.HDD,
										SchedulingT.FCFS,
										sto.getProcessingRate(),
										sto.getNumberOfReplicas(), MTTF, MTTR);
								rc.addProcessingResource(pr);
								break;
							case MEMORY:
							default:
								break;
							}
				}
				if (!hasCPU)
					rc.addProcessingResource(new ProcessingResource(
							ProcessingResourceT.DELAY, SchedulingT.DELAY, 0, 1,
							0, 0));
				return rc;
			}
		pr = new ProcessingResource(ProcessingResourceT.DELAY,
				SchedulingT.DELAY, 0, 1, 0, 0);
		rc.addProcessingResource(pr);
		return rc;
	}

	/**
	 * Updates an existing Palladio Resource Container with the Processing
	 * Resources derived from the specified Cloud Resource. The derived Palladio
	 * Processing Resources will have default MTTF and MTTR attribute values.
	 * 
	 * @param rc
	 *            is the existing ResourceContainer.
	 * @param cr
	 *            is the CloudResource object.
	 * @return the updated ResourceContainer object.
	 * @see ResourceContainer
	 * @see CloudResource
	 */
	public static ResourceContainer completeResourceContainer(
			ResourceContainer rc, CloudResource cr) {
		return completeResourceContainer(rc, cr, DEFAULT_MTTF, DEFAULT_MTTR);
	}

	/**
	 * Updates an existing Palladio Resource Container with the Processing
	 * Resources derived from the specified Cloud Resource. The derived Palladio
	 * Processing Resources will have the specified MTTF and MTTR attribute
	 * values.
	 * 
	 * @param rc
	 *            is the existing ResourceContainer.
	 * @param cr
	 *            is the CloudResource object.
	 * @param MTTF
	 *            is the Mean Time To Failure to assign to the Processing
	 *            Resources within the container.
	 * @param MTTR
	 *            is the Mean Time To Repair to assign to the Processing
	 *            Resources within the container.
	 * @return a new ResourceContainer object.
	 * @see CloudResource
	 * @see ResourceContainer
	 */
	public static ResourceContainer completeResourceContainer(
			ResourceContainer rc, CloudResource cr, double MTTF, double MTTR) {
		List<VirtualHWResource> lvh = cr.getComposedOf();
		ProcessingResource pr;
		if (lvh != null)
			for (VirtualHWResource v : lvh)
				switch (v.getType()) {
				case CPU:
					V_CPU cpu = (V_CPU) v;
					pr = new ProcessingResource(ProcessingResourceT.CPU,
							SchedulingT.PS, cpu.getProcessingRate(),
							cpu.getNumberOfReplicas(), MTTF, MTTR);
					rc.addProcessingResource(pr);
					break;
				case STORAGE:
					V_Storage sto = (V_Storage) v;
					pr = new ProcessingResource(ProcessingResourceT.HDD,
							SchedulingT.FCFS, sto.getProcessingRate(),
							sto.getNumberOfReplicas(), MTTF, MTTR);
					rc.addProcessingResource(pr);
					break;
				case MEMORY:
				default:
					break;
				}
		return rc;
	}

	/**
	 * Derives a NEW Palladio Resource Container from the specified Cloud
	 * Platform, with the specified name. Palladio Processing Resources within
	 * the created Resource Container will have default MTTF and MTTR attribute
	 * values.
	 * 
	 * @param name
	 *            is the name of the container.
	 * @param cp
	 *            is the CloudPlatform object.
	 * @return a new ResourceContainer object.
	 * @see CloudPlatform
	 * @see ResourceContainer
	 */
	public static ResourceContainer deriveResourceContainer(String name,
			CloudPlatform cp) {
		return deriveResourceContainer(name, cp, DEFAULT_MTTF, DEFAULT_MTTR);
	}

	/**
	 * Derives a NEW Palladio Resource Container from the specified Cloud
	 * Platform, with the specified name. Palladio Processing Resources within
	 * the created Resource Container will have the specified MTTF and MTTR
	 * attribute values.
	 * 
	 * @param name
	 *            is the name of the container.
	 * @param cp
	 *            is the CloudPlatform object.
	 * @param MTTF
	 *            is the Mean Time To Failure to assign to the Processing
	 *            Resources within the container.
	 * @param MTTR
	 *            is the Mean Time To Repair to assign to the Processing
	 *            Resources within the container.
	 * @return a new ResourceContainer object.
	 * @see CloudPlatform
	 * @see ResourceContainer
	 */
	public static ResourceContainer deriveResourceContainer(String name,
			CloudPlatform cp, double MTTF, double MTTR) {
		ResourceContainer rc = new ResourceContainer(name);
		return completeResourceContainer(rc, cp, MTTF, MTTR);
	}

	/**
	 * Derives a NEW Palladio Resource Container from the specified Cloud
	 * Resource, with the specified name. Palladio Processing Resources within
	 * the created Resource Container will have default MTTF and MTTR attribute
	 * values.
	 * 
	 * @param name
	 *            is the name of the container.
	 * @param cr
	 *            is the CloudResource object.
	 * @return a new ResourceContainer object.
	 * @see CloudResource
	 * @see ResourceContainer
	 */
	public static ResourceContainer deriveResourceContainer(String name,
			CloudResource cr) {
		return deriveResourceContainer(name, cr, DEFAULT_MTTF, DEFAULT_MTTR);
	}

	/**
	 * Derives a NEW Palladio Resource Container from the specified Cloud
	 * Resource, with the specified name. Palladio Processing Resources within
	 * the created Resource Container will have the specified MTTF and MTTR
	 * attribute values.
	 * 
	 * @param name
	 *            is the name of the container.
	 * @param cr
	 *            is the CloudResource object.
	 * @param MTTF
	 *            is the Mean Time To Failure to assign to the Processing
	 *            Resources within the container.
	 * @param MTTR
	 *            is the Mean Time To Repair to assign to the Processing
	 *            Resources within the container.
	 * @return a new ResourceContainer object.
	 * @see CloudResource
	 * @see ResourceContainer
	 */
	public static ResourceContainer deriveResourceContainer(String name,
			CloudResource cr, double MTTF, double MTTR) {
		ResourceContainer rc = new ResourceContainer(name);
		return completeResourceContainer(rc, cr, MTTF, MTTR);
	}

	/**
	 * Instantiates a new resource container derivation.
	 */
	private ResourceContainerDerivation() {
	}

}
