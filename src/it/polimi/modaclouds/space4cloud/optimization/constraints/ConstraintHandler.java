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


import it.polimi.modaclouds.space4cloud.optimization.solution.IConstrainable;
import it.polimi.modaclouds.space4cloud.optimization.solution.IResponseTimeConstrainable;
import it.polimi.modaclouds.space4cloud.optimization.solution.IUtilizationConstrainable;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.CloudService;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Compute;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.IaaS;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Instance;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author GiovanniPaolo
 *
 */
public class ConstraintHandler {

	ArrayList<Constraint> constraints = new ArrayList<>();

	public void addConstraint(Constraint constraint){
		this.constraints.add(constraint);
	}	


	public void loadConstraints(File constraintFile) throws ParserConfigurationException, SAXException, IOException {

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(constraintFile);
		doc.getDocumentElement().normalize();

		NodeList list = doc.getElementsByTagName("Constraint");

		for(int i=0;i<list.getLength();i++){
			Node n=list.item(i);
			//System.out.println("Node: "+n.getNodeName()+" Type: "+n.getNodeType());
			Element n_elem = (Element) n;			

			//get the resource ID
			String resourceId = n_elem.getElementsByTagName("targetResourceIDRef").item(0).getTextContent();		

			//get the metric of the constraint
			Metric metric = Metric.getMetricFromTag(
					n_elem.getElementsByTagName("metric").item(0).getTextContent());				
			//get the unit
			Unit unit = Unit.getUnitFromTag(
					n_elem.getElementsByTagName("unit").item(0).getTextContent());

			//get the strictness level
			int priority = 0;
			if(n_elem.getElementsByTagName("priority").getLength() > 0)
				priority = Integer.parseInt(n_elem.getElementsByTagName("priority").item(0).getTextContent());

			double max = Double.POSITIVE_INFINITY;
			if(n_elem.getElementsByTagName("hasMaxValue").getLength() > 0)
				max = Double.parseDouble(n_elem.getElementsByTagName("hasMaxValue").item(0).getTextContent());

			double min = Double.NEGATIVE_INFINITY;
			if(n_elem.getElementsByTagName("hasMinValue").getLength() > 0)
				min = Double.parseDouble(n_elem.getElementsByTagName("hasMinValue").item(0).getTextContent());

			Set<String> inSet;
			if(n_elem.getElementsByTagName("inSet").getLength() > 0)
				inSet = parseSet(n_elem.getElementsByTagName("inSet").item(0).getTextContent());

			Set<String> outSet;
			if(n_elem.getElementsByTagName("outSet").getLength() > 0)
				outSet = parseSet(n_elem.getElementsByTagName("outSet").item(0).getTextContent());

			//Create the constraint													
			Constraint constraint = null;				
			switch (metric) {
			case RESPONSETIME:
				constraint = new ResponseTimeConstraint(resourceId, metric, priority, unit);
				((ResponseTimeConstraint) constraint).setMax(max);
				break;
			case CPU:
				constraint = new UsageConstraint(resourceId, metric, priority, unit);	
				((UsageConstraint)constraint).setMax(max);
				break;
			case RAM:
				constraint = new RamConstraint(resourceId,metric , priority, unit);
				((RamConstraint)constraint).setMin(min);
				break;
				//add other constraints
			default:
				System.err.println("Type of constraint: "+metric+" not defined");
			}	
			addConstraint(constraint);
		}

		//debug
		for(Constraint c:constraints){

			System.out.println("Constraint:");
			System.out.println("\tResource: "+c.getResourceID());
			System.out.println("\tpriority: "+c.getPriority());
			System.out.println("\tunit: "+c.getUnit());

			System.out.println("\tmetric: "+c.getMetric());
			if(c instanceof ResponseTimeConstraint){
				System.out.println("\tmax: "+((ResponseTimeConstraint)c).getMax());			
			}else if(c instanceof UsageConstraint){
				System.out.println("\tmax: "+((UsageConstraint)c).getMax());			
			}else if(c instanceof RamConstraint){
				System.out.println("\tmin: "+((RamConstraint)c).getMin());			
			}

		}
	}



	public  ArrayList<HashMap<Constraint, Double>> evaluateFeasibility(Solution sol){
		ArrayList<HashMap<Constraint,Double>> result = new ArrayList<>();
		for(Instance i:sol.getApplications())
			result.add(evaluateApplication(i));
		return result;
	}

	public HashMap<Constraint,Double> evaluateApplication(Instance app){
		HashMap<Constraint, Double> result = new HashMap<>();
		for(Constraint c:constraints){
			if(app.getConstrainableResources().containsKey(c.getResourceID())){
				//evaluate the constraint
				IConstrainable resource = app.getConstrainableResources().get(c.getResourceID());
				//check the type of resource and retrieve the value. 
				//we also check the type of constraint here at the same level but we could also discriminate between numerical and set. 
				if(resource instanceof IResponseTimeConstrainable && c instanceof ResponseTimeConstraint)
					result.put(c, c.checkConstraintDistance(((IResponseTimeConstrainable)resource).getResponseTime()));
				else if(resource instanceof IUtilizationConstrainable && c instanceof UsageConstraint)
					result.put(c, c.checkConstraintDistance(((IUtilizationConstrainable)resource).getUtilization()));
				//System.out.println("utilization: "+)

				//else
				//System.err.println("Resource with id: "+resource.getId()+" of class "+resource.getClass()+" not yet supported");
			}else
				System.err.println("No resource found with id: "+c.getResourceID()+" while evaluating constraints.");
		}

		return result;
	}

	private Set<String> parseSet(String value){
		HashSet<String> set= new HashSet<>();
		String[] tokens = value.split(",");
		for(String s:tokens)
			set.add(s);		
		return set;
	}



	/**
	 * Filters the applicable resources against the constraints defined on the original resource
	 * @param resHasMap
	 * @param origRes
	 */
	public void filterResources(List<IaaS> resList, CloudService origRes) {

		for(IaaS res:resList)
			if(res.equals(origRes)){
				resList.remove(res);
				break;
			}

		List<IaaS> result = new ArrayList<>();
		result.addAll(resList);
		for(Constraint c:constraints)
			//if the constraint affected the original resource
			if(c instanceof ArchitecturalConstraint && c.getResourceID().equals(origRes.getId()))
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

	public List<Constraint> getConstraintsByService(CloudService service){
		List<Constraint> constList = new ArrayList<>();
		for(Constraint c:constraints)
			if(c.getResourceID().equals(service.getId()))
				constList.add(c);
		return constList;
	}


}
