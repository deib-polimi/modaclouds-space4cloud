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
package it.polimi.modaclouds.space4cloud.optimization.constraints;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import it.polimi.modaclouds.qos_models.schema.Constraints;
import it.polimi.modaclouds.qos_models.schema.QosMetricAggregation;
import it.polimi.modaclouds.qos_models.schema.Range;
import it.polimi.modaclouds.qos_models.util.XMLHelper;
import it.polimi.modaclouds.space4cloud.db.DataHandler;
import it.polimi.modaclouds.space4cloud.db.DataHandlerFactory;
import it.polimi.modaclouds.space4cloud.db.DatabaseConnectionFailureExteption;
import it.polimi.modaclouds.space4cloud.exceptions.ConstraintEvaluationException;
import it.polimi.modaclouds.space4cloud.optimization.solution.IConstrainable;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.CloudService;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Compute;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.IaaS;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Instance;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.PaaS;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.SolutionMulti;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Tier;
import it.polimi.modaclouds.space4cloud.utils.Configuration;

/**
 * @author GiovanniPaolo
 *
 */
public class ConstraintHandler {

	List<Constraint> constraints = new ArrayList<>();
	private Map<String,List<Constraint>> resourceId2Constraint = new HashMap<>();
	private static final Logger logger = LoggerFactory.getLogger(ConstraintHandler.class);
	public  static final String AVERAGE_AGGREGATION = "Average";
	public static final String PERCENTILE_AGGREGATION = "Percentile";
	
	private DataHandler dataHandler;

	/**
	 * The Constraint handler should be accessed by its factory  
	 */
	public ConstraintHandler() {
		try {
			dataHandler = DataHandlerFactory.getHandler();
		} catch (DatabaseConnectionFailureExteption e) {
			logger.error("Error while getting the data handler.", e);
		}
	}

	/**
	 * Parses the constraints from the xml file and initializes the handler
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws JAXBException
	 */
	public void loadConstraints()
			throws ConstraintLoadingException {		
		//load from the XML
		Constraints loadedConstraints;
		try {
			loadedConstraints = XMLHelper.deserialize(Paths.get(Configuration.CONSTRAINTS).toUri().toURL(),Constraints.class);
		} catch (MalformedURLException | JAXBException | SAXException e) {
			throw new ConstraintLoadingException("Could not load the constraint file: "+Configuration.CONSTRAINTS,e);			
		}
		for(it.polimi.modaclouds.qos_models.schema.Constraint cons:loadedConstraints.getConstraints()){
			//first get the metric
			Metric metric = Metric.getMetricFromTag(cons.getMetric());			
			//then create the appropriate constraint
			Constraint constraint = null;	
			if(metric == null){
				logger.warn("Metric: "+cons.getMetric()+" on constraint "+cons.getName()+" id: "+cons.getId()+" not available."
						+ " Supported metrics are: "+Metric.getSupportedMetricNames());
				continue;
			}
			switch (metric) {
			case RESPONSETIME:
				if(cons.getMetricAggregation().getAggregateFunction().equals(AVERAGE_AGGREGATION))
					constraint = new AvgRTConstraint(cons);
				else if(cons.getMetricAggregation().getAggregateFunction().equals(PERCENTILE_AGGREGATION))
					constraint = new PercentileRTconstraint(cons);
				break;
			case CPU:
				constraint = new UsageConstraint(cons);					
				break;
			case RAM:
				constraint = new RamConstraint(cons);
				break;

			case REPLICATION:
				constraint = new ReplicasConstraint(cons);
				break;
				
			case WORKLOADPERCENTAGE:
				constraint = new WorkloadPercentageConstraint(cons);
				break;
			case AVAILABILITY:
				constraint = new AvailabilityConstraint(cons);
				break;
				//add other constraints
			case MACHINETYPE:
				constraint = new MachineTypeConstraint(cons);
				break;
			case PROGRAMMINGLANGUAGE:
				constraint = new LanguageConstraint(cons);
				break;
			case DBTYPE:
				constraint = new DBTypeConstraint(cons);
				break;
			case DBTECHNOLOGY:
				constraint = new DBTechnologyConstraint(cons);
				break;
			case NUMBERPROVIDERS:
				constraint = new NumberProvidersConstraint(cons);
				break;
			default:
				logger.warn("Metric: "+metric+" not yet supported, the constraint will be ignored");
			}
			addConstraint(constraint);
		}
		
		cpuConstraintsInitialized = false;

		//debug
		for(Constraint c:constraints){

			logger.info("Constraint:");
			logger.info("\tResource: "+c.getResourceID());
			logger.info("\tpriority: "+c.getPriority());

			logger.info("\tmetric: "+c.getMetric());

			if(c instanceof AvgRTConstraint){
				logger.info("\tmetric: "+AVERAGE_AGGREGATION);
				logger.info("\tmax: "+((AvgRTConstraint)c).getMax());			
			}else if(c instanceof PercentileRTconstraint){
				logger.info("\tmetric: "+PERCENTILE_AGGREGATION);
				logger.info("\tlevel: "+((PercentileRTconstraint)c).getLevel());
				logger.info("\tmax: "+((PercentileRTconstraint)c).getMax());
			}
			else if(c instanceof UsageConstraint){
				logger.info("\tmax: "+((UsageConstraint)c).getMax());			
			} else if(c instanceof RamConstraint) {
				logger.info("\tmin: "+((RamConstraint)c).getMin());			

			}else if(c instanceof ReplicasConstraint){
				logger.info("\tmin: "+((ReplicasConstraint)c).getMin());
				logger.info("\tmax: "+((ReplicasConstraint)c).getMax());


			} else if (c instanceof WorkloadPercentageConstraint) {
				logger.info("\tmin: "+((WorkloadPercentageConstraint)c).getMin());
			} else if (c instanceof AvailabilityConstraint) {
				logger.info("\tmin: "+((AvailabilityConstraint)c).getMin());

			}

		}

	}





	/**
	 * evaluate the feasibility the solution against the constrained loaded in the initialization of the handler. 
	 * @param sol the solution to be evaluated
	 * @return a list of maps, each element in the list represent one hourly solution and the map contains all the constrained and their evaluation value
	 * @throws ConstraintEvaluationException 
	 */
	public  List<HashMap<Constraint, Boolean>> evaluateFeasibility(Solution sol) throws ConstraintEvaluationException{
		List<HashMap<Constraint,Boolean>> result = new ArrayList<HashMap<Constraint,Boolean>>();
		for(Instance i:sol.getApplications())
			result.add(evaluateApplication(i));
		return result;
	}
	
	private boolean cpuConstraintsInitialized;
	
	private void initializeCPUConstraints(Instance app) {
		if (cpuConstraintsInitialized)
			return;
		
		List<Tier> tiers = app.getTiers();
		for (Tier t : tiers) {
			List<Constraint> constraints = getConstraintByResourceId(t.getId());
			boolean hasUsageConstraint = false;
			for (int i = 0; i < constraints.size() && !hasUsageConstraint; ++i) {
				Constraint c = constraints.get(i);
				if (c instanceof UsageConstraint)
					hasUsageConstraint = true;
			}
			if (!hasUsageConstraint)
				addConstraint(UsageConstraint.getStandardUsageConstraint(t.getId()));
		}
		
		cpuConstraintsInitialized = true;
	}
	
	public static File generateConstraintsForVariabilityTest(File initialConstraints, File solution) throws Exception {
		Path path = null;
		
		try {
			path = Files.createTempFile("constraints", ".xml");
		} catch (Exception e) {
			throw new Exception("Could not create a temporary file.", e);
		}
		
		Constraints constraints = new Constraints();
		
		Constraints loadedConstraints;
		try {
			loadedConstraints = XMLHelper.deserialize(Paths.get(Configuration.CONSTRAINTS).toUri().toURL(),Constraints.class);
		} catch (MalformedURLException | JAXBException | SAXException e) {
			throw new ConstraintLoadingException("Could not load the constraint file: "+Configuration.CONSTRAINTS,e);			
		}
		for (it.polimi.modaclouds.qos_models.schema.Constraint constraint : loadedConstraints.getConstraints()) {
			Metric metric = Metric.getMetricFromTag(constraint.getMetric());			
			
			if (metric == null){
				logger.warn("Metric: "+constraint.getMetric()+" on constraint "+constraint.getName()+" id: "+constraint.getId()+" not available."
						+ " Supported metrics are: "+Metric.getSupportedMetricNames());
				continue;
			}
			
			if (metric == Metric.REPLICATION || metric == Metric.MACHINETYPE)
				continue;
			
			constraints.getConstraints().add(constraint);
		}
		
		Map<String, List<String>> map = SolutionMulti.getResourceSizesByTier(solution);
		
		for (String s : map.keySet()) {
			String tierId = s.substring(s.indexOf('@') + 1);
			List<String> resourceSizes = map.get(s);
			
			it.polimi.modaclouds.qos_models.schema.Constraint constraint = new it.polimi.modaclouds.qos_models.schema.Constraint();
			
			String id = UUID.randomUUID().toString();
			
			constraint.setId(id);
			constraint.setName("MachineType " + id);
			constraint.setMetric(Metric.MACHINETYPE.getXmlTag());
			Range r = new Range();
			it.polimi.modaclouds.qos_models.schema.Set set = new it.polimi.modaclouds.qos_models.schema.Set();
			for (String resourceSize : resourceSizes)
				set.getValues().add(resourceSize);
			r.setInSet(set);
			constraint.setRange(r);
			constraint.setTargetResourceIDRef(tierId);
			constraint.setTargetClass("VM");
			QosMetricAggregation aggr = new QosMetricAggregation();
			aggr.setAggregateFunction("Average");
			constraint.setMetricAggregation(aggr);
			
			constraints.getConstraints().add(constraint);
		}
		
		try {
			XMLHelper.serialize(constraints, Constraints.class, new FileOutputStream(path.toFile()));
		} catch (Exception e) {
			throw new Exception("Could not save the constraint file: "+path.toString(),e);			
		}
		
		return path.toFile();
	}

	/**
	 * evaluates the feasibility of an application against the constrained loaded in the initialization of the handler.  
	 * @param app the application to be evaluated
	 * @return a map containing all the loaded constraints and their evaluation value
	 * @throws ConstraintEvaluationException 
	 */
	public HashMap<Constraint,Boolean> evaluateApplication(Instance app) throws ConstraintEvaluationException{
		HashMap<Constraint, Boolean> result = new HashMap<>();
		
		initializeCPUConstraints(app);
		
		for(Constraint c:constraints){
			if(app.getConstrainableResources().containsKey(c.getResourceID())){
				//evaluate the constraint
				IConstrainable resource = app.getConstrainableResources().get(c.getResourceID());
				result.put(c, c.checkConstraint(resource));
			}else
				logger.warn("No resource found with id: "+c.getResourceID()+" while evaluating constraints. It will be ignored");
		}

		return result;
	}




	/**
	 * Filters the applicable resources against the constraints defined on the original resource
	 * @param <T>
	 * @param <T>
	 * @param resHasMap
	 * @param origRes
	 * @throws ConstraintEvaluationException 
	 */
	public <T extends CloudService> void filterResources(List<T> resList, Tier tier) throws ConstraintEvaluationException {

		//remove the resource that is currently used
		for(CloudService res:resList){
			if(res.equals(tier.getCloudService())){
				resList.remove(res);
				break;
			}
		}

		//check other resources against constraints
		List<T> result = new ArrayList<>();
		result.addAll(resList);
		for(Constraint c:constraints)
			//if the constraint affected the original resource
			if(c instanceof ArchitecturalConstraint && c.getResourceID().equals(tier.getId()))
				for(CloudService resource:resList){
					if(!((ArchitecturalConstraint)c).checkConstraint(resource))
						result.remove(resource);
				}			
		//filter resources in the DB which have no number of cpus, this should not be necessary if the DB is good
		for(CloudService resource:resList)
			if(resource instanceof Compute && ((Compute)resource).getNumberOfCores()==0)
				result.remove(resource);

		if (Configuration.BENCHMARK != Configuration.Benchmark.None && resList.size() > 0) {
			Set<String> resourcesWithBenchmark = dataHandler.getSimilarResourcesWithBenchmarkValue(resList.get(0), Configuration.BENCHMARK.toString());
			
			if (resourcesWithBenchmark.size() == 0) {
				logger.warn("No resource has a valid benchmark value for the benchmark {}. Disabling it.", Configuration.BENCHMARK.toString());
				Configuration.BENCHMARK = Configuration.Benchmark.None;
			} else {
				for(CloudService resource:resList)
					if (!resourcesWithBenchmark.contains(resource.getResourceName()))
						result.remove(resource);
			}
		}


		resList.clear();
		resList.addAll(result);		



	}
	

	/**
	 * Remove from the tier list the tier for which the maximum replica constraint has been reached
	 * @param affectedTiers
	 * @return
	 */
	public Set<Tier> filterResourcesForScaleOut(Set<Tier> affectedTiers) {
		Set<Tier> tiers = new HashSet<Tier>(affectedTiers);
		for(Tier t:affectedTiers){
			for(Constraint c:getConstraintByResourceId(t.getId(), ReplicasConstraint.class)){				
				if(((ReplicasConstraint) c).hasMaxReplica(t.getCloudService())){
					tiers.remove(t);
					continue;
				}
			}
		}
		return tiers;
	}


	/**
	 * Remove resources that can not be scaled in, those are the resources with just 1 replica or with the minimum number according to the cosntraints
	 * @param vettResTot
	 * @param hour
	 * @return 
	 */
	public List<ArrayList<Tier>> filterResourcesForScaleDown(List<ArrayList<Tier>> vettResTot, int hour) {

		List<ArrayList<Tier>> resources = new ArrayList<>();
		for(ArrayList<Tier> hourResource:vettResTot)
			resources.add(new ArrayList<Tier>(hourResource));

		for (Tier t:vettResTot.get(hour)){
			if (t.getCloudService() instanceof IaaS || t.getCloudService() instanceof PaaS) {
				//filter resources with just 1 replica
				if ( 
						(t.getCloudService() instanceof IaaS && (t.getCloudService().getReplicas() == 1)) ||
						(t.getCloudService() instanceof PaaS && (t.getCloudService().getReplicas() == 1))
						)
					resources.get(hour).remove(t);
				else{
					//filter resources with minimum number of replicas
					for(Constraint c:getConstraintByResourceId(t.getId(), ReplicasConstraint.class)){
						if(((ReplicasConstraint) c).hasMinReplica(t.getCloudService()))
							resources.get(hour).remove(t);
					}
				}
			}
		}
		return resources;		
	}


	private void addConstraint(Constraint constraint){
		constraints.add(constraint);
		if(!resourceId2Constraint.containsKey(constraint.getResourceID()))
			resourceId2Constraint.put(constraint.getResourceID(),new ArrayList<Constraint>());
		resourceId2Constraint.get(constraint.getResourceID()).add(constraint);
	}	

	/**
	 * Get the list of constraints that affect the resource with the specified id, if no constraint has been defined returns an empty list
	 * @param resourceId
	 * @return
	 */
	public List<Constraint> getConstraintByResourceId(String resourceId){
		if(resourceId2Constraint.containsKey(resourceId))
			return resourceId2Constraint.get(resourceId);
		return new ArrayList<>();

	}

	/**
	 * Get the list of constraints of type clazz that affect the resource with the specified id, if no constraint has been defined returns an empty list
	 * @param <clazz>
	 * @param resourceId
	 * @param clazz the class of the constraint
	 * @return
	 */
	public  <clazz> List<Constraint> getConstraintByResourceId(String resourceId, Class<? extends Constraint> clazz){
		List<Constraint> constraints = new ArrayList<Constraint>();
		if(resourceId2Constraint.containsKey(resourceId)){
			for(Constraint c:resourceId2Constraint.get(resourceId))
				if(c.getClass() == clazz)					
					constraints.add(c);
		}
		return constraints;
	}


}