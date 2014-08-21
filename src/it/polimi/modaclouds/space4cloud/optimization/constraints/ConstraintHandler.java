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


import it.polimi.modaclouds.qos_models.schema.Constraints;
import it.polimi.modaclouds.qos_models.util.XMLHelper;
import it.polimi.modaclouds.space4cloud.optimization.solution.IConstrainable;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Compute;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.IaaS;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Instance;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Tier;
import it.polimi.modaclouds.space4cloud.utils.Configuration;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * @author GiovanniPaolo
 *
 */
public class ConstraintHandler {

	List<Constraint> constraints = new ArrayList<>();
	private static final Logger logger = LoggerFactory.getLogger(ConstraintHandler.class);
	public  static final String AVERAGE_AGGREGATION = "Average";
	public static final String PERCENTILE_AGGREGATION = "Percentile";
	public void addConstraint(Constraint constraint){
		constraints.add(constraint);
	}	


	/**
	 * Parses the constraints from the xml file and initializes the handler
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws JAXBException
	 */
	public void loadConstraints()
			throws ParserConfigurationException, SAXException, IOException, JAXBException {		
		//load from the XML
		Constraints  loadedConstraints = XMLHelper.deserialize(Paths.get(Configuration.CONSTRAINTS).toUri().toURL(),Constraints.class);
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
				//add other constraints
			default:
				logger.warn("Metric: "+metric+" not yet supported, the constraint will be ignored");
			}
			addConstraint(constraint);
		}

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
			}else if(c instanceof RamConstraint){
				logger.info("\tmin: "+((RamConstraint)c).getMin());			
			}else if(c instanceof ReplicasConstraint){
				logger.info("\tmin: "+((ReplicasConstraint)c).getMin());
				logger.info("\tmax: "+((ReplicasConstraint)c).getMax());
				
			}

		}

	}





	/**
	 * evaluate the feasibility the solution against the constrained loaded in the initialization of the handler. 
	 * @param sol the solution to be evaluated
	 * @return a list of maps, each element in the list represent one hourly solution and the map contains all the constrained and their evaluation value
	 */
	public  ArrayList<HashMap<Constraint, Double>> evaluateFeasibility(Solution sol){
		ArrayList<HashMap<Constraint,Double>> result = new ArrayList<>();
		for(Instance i:sol.getApplications())
			result.add(evaluateApplication(i));
		return result;
	}

	/**
	 * evaluates the feasibility of an application against the constrained loaded in the initialization of the handler.  
	 * @param app the application to be evaluated
	 * @return a map containing all the loaded constraints and their evaluation value
	 */
	public HashMap<Constraint,Double> evaluateApplication(Instance app){
		HashMap<Constraint, Double> result = new HashMap<>();
		for(Constraint c:constraints){
			if(app.getConstrainableResources().containsKey(c.getResourceID())){
				//evaluate the constraint
				IConstrainable resource = app.getConstrainableResources().get(c.getResourceID());
				result.put(c, c.checkConstraintDistance(resource));
			}else
				logger.error("No resource found with id: "+c.getResourceID()+" while evaluating constraints.");
		}

		return result;
	}




	/**
	 * Filters the applicable resources against the constraints defined on the original resource
	 * @param resHasMap
	 * @param origRes
	 */
	public void filterResources(List<IaaS> resList, Tier tier) {

		//remove the resource that is currently used
		for(IaaS res:resList){
			if(res.equals(tier.getCloudService())){
				resList.remove(res);
				break;
			}
		}

		//check other resources against constraints
		List<IaaS> result = new ArrayList<>();
		result.addAll(resList);
		for(Constraint c:constraints)
			//if the constraint affected the original resource
			if(c instanceof ArchitecturalConstraint && c.getResourceID().equals(tier.getId()))
				for(IaaS resource:resList){
					if(!((ArchitecturalConstraint)c).checkConstraint(resource))
						result.remove(resource);
				}
		//filter resources in the DB which have no number of cpus, this should not be necessary if the DB is good
		for(IaaS resource:resList)
			if(resource instanceof Compute && ((Compute)resource).getNumberOfCores()==0)
				result.remove(resource);



		resList.clear();
		resList.addAll(result);		



	}



}
