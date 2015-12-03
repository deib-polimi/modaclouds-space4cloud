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
package it.polimi.modaclouds.space4cloud.optimization.evaluation;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.polimi.modaclouds.resourcemodel.cloud.Cost;
import it.polimi.modaclouds.resourcemodel.cloud.CostProfile;
import it.polimi.modaclouds.resourcemodel.cloud.V_Storage;
import it.polimi.modaclouds.resourcemodel.cloud.VirtualHWResource;
import it.polimi.modaclouds.space4cloud.db.DataHandler;
import it.polimi.modaclouds.space4cloud.db.DataHandlerFactory;
import it.polimi.modaclouds.space4cloud.db.DatabaseConnectionFailureExteption;
import it.polimi.modaclouds.space4cloud.optimization.bursting.Host;
import it.polimi.modaclouds.space4cloud.optimization.bursting.PrivateCloud;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.CloudService;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.IaaS;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Instance;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.PaaS;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Queue;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Tier;
import it.polimi.modaclouds.space4cloud.types.palladio.AllocationProfile;


/**
 * Provides utility methods to derive system costs.
 * 
 * @author Giovanni Paolo Gibilisco
 * 
 */
public class CostEvaluator {

	DataHandler dataHandler = null;
	private static final Logger logger = LoggerFactory.getLogger(CostEvaluator.class);
	public CostEvaluator() throws DatabaseConnectionFailureExteption {
		dataHandler = DataHandlerFactory.getHandler();
	}

//	public double deriveCosts(Instance application, int hour) {
//		double cost = 0;
//		// sum up costs for each tier
//		for (Tier t : application.getTiers()) {
//			CloudService service = t.getCloudService();
//			if (service instanceof IaaS) {
//				IaaS iaasResource = (IaaS) service;
//				it.polimi.modaclouds.resourcemodel.cloud.CloudResource cloudResource = dataHandler
//						.getCloudResource(iaasResource.getProvider(),
//								iaasResource.getServiceName(),
//								iaasResource.getResourceName());
//
//				if (cloudResource == null) {
//					logger.error("ERROR: The found resource is null!");
//					cost += 1;
//					continue;
//				}
//
//				List<Cost> lc = cloudResource.getHasCost();
//				List<Cost> onDemandLc = new ArrayList<Cost>();
//
//				// filter only on-demand
//				for (Cost c : lc)
//					if (!c.getDescription().contains("Reserved"))
//						onDemandLc.add(c);
//
//				lc.clear();
//				// filter by region
//				for (Cost c : onDemandLc)
//					if (c.getRegion() == null || c.getRegion() == ""
//					|| application.getRegion() == null
//					|| c.getRegion().equals(application.getRegion()))
//						lc.add(c);
//
//				CostProfile cp = cloudResource.getHasCostProfile();
//				cost += deriveCosts(lc, cp, iaasResource.getReplicas(), hour);
//			}
//			// TODO Add Platform costs
//		}
//		return cost;
//	}
	
	private double deriveCosts(it.polimi.modaclouds.resourcemodel.cloud.CloudResource cloudResource, String region, int replicas, int requests, double gbConsumed, double gbOut, int hour) throws CostEvaluationException {
		if (cloudResource == null) {
			throw new CostEvaluationException(cloudResource);
		}
		
		double cost = 0;
		
		CostProfile cp = cloudResource.getHasCostProfile();
		List<Cost> lc = cloudResource.getHasCost();		
		if(cp == null && (lc==null || lc.size()==0))
			logger.error("Resource with no costs!");
		List<Cost> onDemandLc = new ArrayList<Cost>();

		// filter only on-demand
		for (Cost c : lc)
			if (!c.getDescription().contains("Reserved"))
				onDemandLc.add(c);

		lc.clear();
		// filter by region
		for (Cost c : onDemandLc)
			if (c.getRegion() == null || c.getRegion() == ""
			|| region == null
			|| c.getRegion().equals(region))
				lc.add(c);
		
		
		try {
			cost = deriveCosts(lc, cp, replicas, requests, gbConsumed, gbOut, hour);
		} catch (EmptyCostException e) {
			throw new CostEvaluationException(e, cloudResource);
		}
		
		return cost;
	}
	
	private double deriveCosts(it.polimi.modaclouds.resourcemodel.cloud.CloudPlatform cloudPlatform, String region, int replicas, int requests, double gbConsumed, double gbOut, int hour) throws CostEvaluationException {
		if (cloudPlatform == null) {
			throw new CostEvaluationException(cloudPlatform);
		}
		
		double cost = 0;
		
		List<Cost> lc = cloudPlatform.getHasCost();
		List<Cost> onDemandLc = new ArrayList<Cost>();

		// filter only on-demand
		for (Cost c : lc)
			if (!c.getDescription().contains("Reserved"))
				onDemandLc.add(c);

		lc.clear();
		// filter by region
		for (Cost c : onDemandLc)
			if (c.getRegion() == null || c.getRegion() == ""
			|| region == null
			|| c.getRegion().equals(region))
				lc.add(c);
		
		CostProfile cp = cloudPlatform.getHasCostProfile();
		if (lc.size() > 0 || cp != null)
			try {
				cost += deriveCosts(lc, cp, replicas, requests, gbConsumed, gbOut, hour);
			} catch (EmptyCostException e) {
				throw new CostEvaluationException(e, cloudPlatform);
			}
		
		List<it.polimi.modaclouds.resourcemodel.cloud.CloudResource> resources = cloudPlatform.getRunsOnCloudResource();
		for (it.polimi.modaclouds.resourcemodel.cloud.CloudResource cr : resources)
			cost += deriveCosts(cr, region, replicas, requests, gbConsumed, gbOut, hour);
		
		return cost;
	}
	
	public double deriveCosts(Instance application, int hour) throws CostEvaluationException, EmptyCostException {
		if (application.getFather().getProvider().indexOf(PrivateCloud.BASE_PROVIDER_NAME) > -1) {
			return derivePrivateCosts(application, hour);
		}
		
		double cost = 0.0;
		
		Solution solution = application.getFather();
		int providers = solution.getTotalProviders();
		
		// sum up costs for each tier
		for (Tier t : application.getTiers()) {
			double tierCost = 0.0;
			
			CloudService service = t.getCloudService();
			
			int storage = dataHandler.getStorage(service.getProvider(),
					service.getServiceName(),
					service.getResourceName());
			int requests = 0;
			
			if (service instanceof IaaS) {
				//TODO:Missing operating system in Compute!
				IaaS iaasResource = (IaaS) service;
				it.polimi.modaclouds.resourcemodel.cloud.CloudResource cloudResource = dataHandler
						.getCloudResource(iaasResource.getProvider(),
								iaasResource.getServiceName(),
								iaasResource.getResourceName());
				
				tierCost += deriveCosts(cloudResource, application.getRegion(), iaasResource.getReplicas(), 0/*t.getTotalRequests()*/, iaasResource.getDataConsumed(storage, requests, providers), iaasResource.getDataOut(storage, requests, providers), hour);
			} else if (service instanceof PaaS) {
				PaaS paasResource = (PaaS) service;
				it.polimi.modaclouds.resourcemodel.cloud.CloudPlatform cloudPlatform = dataHandler
						.getCloudPlatform(paasResource.getProvider(),
								paasResource.getServiceName(),
								paasResource.getResourceName());
				
				int replicas = paasResource.areReplicasPayedSingularly() ? paasResource.getReplicas() : 1;
				requests = solution.getDailyRequestsByTier(t.getId());
				if (paasResource instanceof Queue)
					requests *= ((Queue)paasResource).getMultiplyingFactor();

				tierCost += deriveCosts(cloudPlatform, application.getRegion(), replicas, requests, paasResource.getDataConsumed(storage, requests, providers), paasResource.getDataOut(storage, requests, providers), hour);
			}
			
//			t.setCost(tierCost);
			solution.setCost(t.getId(), hour, tierCost);
			
			cost += tierCost;
		}
		return cost;
	}
	
	public double derivePrivateCosts(Instance application, int hour) throws CostEvaluationException, EmptyCostException {
		if (application.getFather().getProvider().indexOf(PrivateCloud.BASE_PROVIDER_NAME) == -1) {
			return deriveCosts(application, hour);
		}
		
		double cost = 0;
		
		List<Host> usedHosts = PrivateCloud.getInstance().getUsedHosts(hour);
		for (Host h : usedHosts) {
			cost += deriveCosts(null, h.energyCost, 1, 0, 0.0, 0.0, hour);
		}
		
		// TODO: add VM cost, because we consider now a fixed cost either if the
		// 		 host is on or not, independently on the number of vms it's running
		
		return cost;
	}

	public double getResourceAverageCost(IaaS iaasResource, String region) throws CostEvaluationException{
		it.polimi.modaclouds.resourcemodel.cloud.CloudResource cloudResource = dataHandler
				.getCloudResource(iaasResource.getProvider(),
						iaasResource.getServiceName(),
						iaasResource.getResourceName());

		if (cloudResource == null) {
			throw new CostEvaluationException(cloudResource);
		}

		try {
			return getResourceAverageCost(cloudResource, region);
		} catch (EmptyCostException e) {
			throw new CostEvaluationException(e, cloudResource);
		}
	}
	
	private double getResourceAverageCost(it.polimi.modaclouds.resourcemodel.cloud.CloudResource cloudResource, String region) throws EmptyCostException{
		if (cloudResource == null)
			return 0;
		
		double cost = 0;

		List<Cost> lc = cloudResource.getHasCost();
		List<Cost> onDemandLc = new ArrayList<Cost>();

		// filter only on-demand
		for (Cost c : lc)
			if (!c.getDescription().contains("Reserved"))
				onDemandLc.add(c);

		lc.clear();
		// filter by region
		for (Cost c : onDemandLc)
			if (c.getRegion() == null || c.getRegion() == ""
			|| region == null
			|| c.getRegion().equals(region))
				lc.add(c);

		CostProfile cp = cloudResource.getHasCostProfile();
		for(int i=0;i<24;i++)
			cost += deriveCosts(lc, cp, 1, 0, 0.0, 0.0, i)/24;
		return cost;
	}
	
	public double getPlatformAverageCost(PaaS paasResource, String region) throws EmptyCostException{
		double cost = 0;
		it.polimi.modaclouds.resourcemodel.cloud.CloudPlatform cloudPlatform = dataHandler
				.getCloudPlatform(paasResource.getProvider(),
						paasResource.getServiceName(),
						paasResource.getResourceName());

		if (cloudPlatform == null) {
			logger.error("ERROR: The found platform is null!");
			return 0;
		}

		List<Cost> lc = cloudPlatform.getHasCost();
		List<Cost> onDemandLc = new ArrayList<Cost>();

		// filter only on-demand
		for (Cost c : lc)
			if (!c.getDescription().contains("Reserved"))
				onDemandLc.add(c);

		lc.clear();
		// filter by region
		for (Cost c : onDemandLc)
			if (c.getRegion() == null || c.getRegion() == ""
			|| region == null
			|| c.getRegion().equals(region))
				lc.add(c);

		CostProfile cp = cloudPlatform.getHasCostProfile();
		for(int i=0;i<24;i++)
			cost += deriveCosts(lc, cp, 1, 0, 0.0, 0.0, i)/24;
		
		List<it.polimi.modaclouds.resourcemodel.cloud.CloudResource> resources = cloudPlatform.getRunsOnCloudResource();
		for (it.polimi.modaclouds.resourcemodel.cloud.CloudResource cr : resources)
			cost += getResourceAverageCost(cr, region);
		
		return cost;
	}

	// /**
	// * Derives the system costs analyzing the mapping between Extended
	// Resource
	// * Containers and Cloud Elements. Information about costs is saved within
	// * the cost model serialized within the "costs.xml" file. Cloud Platforms
	// * and Cloud Resources are automatically recognized and separately
	// treated.
	// *
	// * @param map
	// * is the Map object containing key-value elements, where the key
	// * is an ExtendedResourceContainer object, while the value is a
	// * CloudElement object.
	// * @see CloudElement
	// * @see ExtendedResourceContainer
	// * @see #deriveCostsForCloudPlatform(ExtendedResourceContainer,
	// * CloudPlatform)
	// * @see #deriveCostsForCloudResource(ExtendedResourceContainer,
	// * CloudService)
	// */
	// public void derive(Map<ExtendedResourceContainer, CloudElement> map) {
	// for (Map.Entry<ExtendedResourceContainer, CloudElement> e : map
	// .entrySet())
	// if (e.getValue() instanceof CloudService)
	// deriveCostsForCloudResource(e.getKey(),
	// (CloudService) e.getValue());
	// else if (e.getValue() instanceof CloudPlatform)
	// deriveCostsForCloudPlatform(e.getKey(),
	// (CloudPlatform) e.getValue());
	// }

	// /**
	// * Derives costs from the mapping between an Extended Resource Container
	// and
	// * a Cloud Platform.
	// *
	// * @param erc
	// * is the ExtendedResourceContainer object derived from the
	// * CloudPlatform.
	// * @param cp
	// * is the CloudPlatform object.
	// * @see ExtendedResourceContainer
	// * @see CloudPlatform
	// */
	// private void deriveCostsForCloudPlatform(ExtendedResourceContainer erc,
	// CloudPlatform cp) {
	// List<Cost> lc = cp.getHasCost();
	// CostProfile costp = cp.getHasCostProfile();
	// List<CloudService> lcr = cp.getRunsOnCloudResource();
	// double cost = deriveCosts(lc, costp, costList, as);
	// if (lcr != null)
	// for (CloudService cr : lcr) {
	// List<Cost> lc1 = cr.getHasCost();
	// CostProfile cp1 = cr.getHasCostProfile();
	// Element costList1 = doc.createElement("Cost_List");
	// double temp = deriveCosts(lc1, cp1, costList1, as);
	// // Update the total system cost
	// cost += temp;
	// }
	// totalCost += cost;
	// }

	/**
	 * Helper method used to derive costs starting from a list of Costs, a Cost
	 * Profile and an Allocation Profile. The cost model is updated accordingly.
	 * 
	 * @param lc
	 *            is the List of Cost elements.
	 * @param cp
	 *            is the CostProfile object.
	 * @param costList
	 *            is the Element representing the root of the cost list within
	 *            the cost model.
	 * @param as
	 *            is the AllocationProfile object.
	 * @return the total cost derived from the input parameters.
	 * @throws EmptyCostException 
	 * @see Cost
	 * @see CostProfile
	 * @see AllocationProfile
	 */
	private double deriveCosts(List<Cost> lc, CostProfile cp, int replicas, int requests, double gbConsumed, double gbOut,
			int hour) throws EmptyCostException {
		double cost = 0.0, temp;
		
		if ((lc == null || lc.size() == 0) && cp == null){
			throw new EmptyCostException(); 
		}

		// Consider the costs which do not belong to a cost profile.
		if (lc != null && lc.size() > 0 && cp == null)
			for (Cost c : lc) {
				// if the resource has a cost for each hour skip those that are
				// different the "hour", if it has only 1 cost (for the whole
				// day) use it
				temp = 0.0;
				switch (c.getUnit()) {

				// Instances have per hour costs
				case PER_HOUR:
					// Instances (VM) scale according to the Allocation Profile,
					// so for each hour we consider the allocation size and we
					// multiply it for the cost value.
					temp += c.getValue() * replicas;
					break;

					// Storage systems (storage/DB) have per GB-month costs
				case PER_GBMONTH:
					// Storage resources do not scale, so we don't have to take
					// into account allocation when deriving costs! We must
					// consider the hourly cost, so we multiply the cost for the
					// size in GB, divide by (365*24)/12=730 hours/month and
					VirtualHWResource v = c.getDefinedOn();
					int size;
					if (v != null)
						switch (v.getType()) {
						case MEMORY:
							/*
							 * size = ((V_Memory) v).getSize();
							 * vel.setAttribute("size", "" + size); temp =
							 * getIntervalCost(c, size / 1024) / 730;
							 */
							break;
						case STORAGE:
							size = ((V_Storage) v).getSize();
							temp += getIntervalCost(c, size) / 730;
							break;
						case CPU:
						default:
							break;
						}
					break;
				case PER_MILLION_IO:
					if (requests > 0)
						temp += getIntervalCost(c, (int)Math.round(requests/1000000.0)) / 24.0;
					break;
				case PER_GBOUT:
					if (gbOut > 0.0)
						temp += getIntervalCost(c, (int)Math.round(gbOut));
					break;
				case PER_GBMONTH_CONSUMED:
					if (gbConsumed > 0.0)
						temp += getIntervalCost(c, (int)Math.round(gbConsumed)) / 730;
					break;
				case PER_GB_CONSUMED:
					if (gbConsumed > 0.0)
						temp += getIntervalCost(c, (int)Math.round(gbConsumed));
					break;
				default:
					break;
				}

				// Update the total cost
				cost += temp;
			}

		// Consider the Cost Profile
		if (cp != null && cp != null) {
//		if (lc == null && cp != null) {
			lc = cp.getComposedOf();
			if (lc != null)
				for (Cost c : lc) {
					if (lc.size() > 1 && lc.indexOf(c) != hour)
						continue;
					temp = 0.0;
					switch (c.getUnit()) {

					// Instances have per hour costs
					case PER_HOUR:
						// Instances (VM) scale according to the Allocation
						// Profile, so we have to take into account the
						// allocation size corresponding to the actual cost
						// reference period.
						temp = c.getValue() * replicas;
						break;

						// Storage systems (storage/DB) have per GB-month costs
					case PER_GBMONTH:
						// Storage resources do not scale, so we don't have to
						// take into account allocation when deriving costs! We
						// must consider only the per hour cost because the cost
						// profile is supposed to be composed of 24 cost
						// definitions, one for each hour of the day. So we
						// multiply for the size in GB and divide by
						// (365*24)/12=730 hours/month.
						VirtualHWResource v = c.getDefinedOn();
						int size;
						if (v != null)
							switch (v.getType()) {
							case MEMORY:
								/*
								 * size = ((V_Memory) v).getSize();
								 * System.out.println("Storage Size: " + size);
								 * temp = getIntervalCost(c, size / 1024) / 730;
								 */
								break;
							case STORAGE:
								size = ((V_Storage) v).getSize();
								temp = getIntervalCost(c, size) / 730;
								break;
							case CPU:
							default:
								break;
							}
						break;
					case PER_MILLION_IO:
					default:
						break;
					}

					// Update the total cost
					cost += temp;
				}
		}
		return cost;
	}

	/**
	 * Derive the cost relative to the specified size considering the cost
	 * intervals defined within the cost specification.
	 * 
	 * @param c
	 *            is the Cost specification.
	 * @param size
	 *            is the size to check against the intervals.
	 * @return the cost associated to the specified size, relative to the
	 *         intervals within the specified cost specification.
	 * @see Cost
	 */
	private double getIntervalCost(Cost c, int size) {
		int a = c.getLowerBound();
		int b = c.getUpperBound();

		if (size < 0)
			return 0.0;

		if (a < 0)
			if (b < 0)
				// case: |0|----|size|
				return c.getValue() * size;
			else {
				if (size < b)
					// case: |0|--|size|----|b|
					return c.getValue() * size;
				else
					// case: |0|---|b|------|size|
					return c.getValue() * b;
			}
		else {
			if (b < a)
				if (size > a)
					// case: |b|---|a|----|size|
					return c.getValue() * (size - a);
				else
					// case: |b|---|size|------|a|
					// OR
					// case: |size|---|b|----|a|
					return 0.0;
			else {
				if (size < b)
					if (size > a)
						// case: |a|----|size|-------|b|
						return c.getValue() * (size - a);
					else
						// case: |size|---|a|----|b|
						return 0.0;
				else
					// case: |a|-----|b|----|size|
					return c.getValue() * (b - a);
			}
		}
	}
}
