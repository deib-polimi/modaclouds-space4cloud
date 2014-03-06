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
/**
 * 
 */
package it.polimi.modaclouds.space4cloud.lqn;


import it.polimi.modaclouds.space4cloud.EMFHelper.EMFHelper;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.common.util.EList;

import LqnCore.ActivityPhasesType;
import LqnCore.EntryType;
import LqnCore.LqnModelType;
import LqnCore.OutputResultType;
import LqnCore.ProcessorType;
import LqnCore.TaskType;
import de.uka.ipd.sdq.pcm.allocation.AllocationContext;
import de.uka.ipd.sdq.pcm.core.composition.AssemblyContext;
import de.uka.ipd.sdq.pcm.repository.BasicComponent;
import de.uka.ipd.sdq.pcm.repository.OperationSignature;
import de.uka.ipd.sdq.pcm.repository.PassiveResource;
import de.uka.ipd.sdq.pcm.repository.Repository;
import de.uka.ipd.sdq.pcm.repository.RepositoryComponent;
import de.uka.ipd.sdq.pcm.resourceenvironment.LinkingResource;
import de.uka.ipd.sdq.pcm.resourceenvironment.ProcessingResourceSpecification;
import de.uka.ipd.sdq.pcm.resourceenvironment.ResourceContainer;
import de.uka.ipd.sdq.pcm.resultdecorator.ResultDecoratorRepository;
import de.uka.ipd.sdq.pcm.resultdecorator.ResultdecoratorFactory;
import de.uka.ipd.sdq.pcm.resultdecorator.repositorydecorator.AllocationServiceResult;
import de.uka.ipd.sdq.pcm.resultdecorator.repositorydecorator.RepositorydecoratorFactory;
import de.uka.ipd.sdq.pcm.resultdecorator.repositorydecorator.ServiceResult;
import de.uka.ipd.sdq.pcm.resultdecorator.resourceenvironmentdecorator.LinkingResourceResults;
import de.uka.ipd.sdq.pcm.resultdecorator.resourceenvironmentdecorator.PassiveResourceResult;
import de.uka.ipd.sdq.pcm.resultdecorator.resourceenvironmentdecorator.ProcessingResourceSpecificationResult;
import de.uka.ipd.sdq.pcm.resultdecorator.resourceenvironmentdecorator.ResourceenvironmentdecoratorFactory;
import de.uka.ipd.sdq.pcm.resultdecorator.resourceenvironmentdecorator.UtilisationResult;
import de.uka.ipd.sdq.pcm.seff.ServiceEffectSpecification;
import de.uka.ipd.sdq.pcm.usagemodel.UsageScenario;
import de.uka.ipd.sdq.pcmsolver.models.PCMInstance;
import de.uka.ipd.sdq.pcmsolver.transformations.pcm2lqn.Pcm2LqnHelper;





/**
 * @author Michele Ciavotta
 *
 */
public class AnalysisLQNResults {

	protected double meanResponseTime;
	protected double throughput;
	protected double maxUtilization;
	private ResultDecoratorRepository results;
	PCMInstance pcm;
	LqnModelType model;

// in this way we'll use the construct
public AnalysisLQNResults(PCMInstance pcm, LqnModelType model)
{
	try {
		this.pcm = pcm;
		this.model = model;
		this.meanResponseTime = retrieveResponseTimeForUsageScenario();
		this.throughput = retrieveThroughputForUsageScenario();
		this.results =  retrieveResults();
		this.maxUtilization = retrieveMaxUtilization(pcm, model, this.results, "CPU");

	} catch (ParseException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	}

public double getMeanResponseTime(){
	return meanResponseTime;
}

public double retreiveTierResponseTime(String tierID){
	
	
	return 0;
}

private double retrieveResponseTimeForUsageScenario()  
{
	List<UsageScenario> scenarios = pcm.getUsageModel().getUsageScenario_UsageModel();
	UsageScenario usageScenario = scenarios.get(0);
	ProcessorType processor = getUsageScenarioProcessor(pcm, model, usageScenario);

	if (processor != null) {

		if (processor.getTask() != null && processor.getTask().size() > 0) {

			// TODO: Can we really assume there is only one task?
			TaskType task = processor.getTask().get(0);

			if (task != null){
				double responseTime = Double.NaN;

				if (task.getResultTask().size() > 0){
					OutputResultType outputResult = task.getResultTask().get(0);

					if (outputResult != null)
						try {
							responseTime = LQNSResultParser.getResponseTimeOfSubActivities(task);
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					} 
					return responseTime;
				}
			}


		}
	


	return Double.NaN;

}

/**
 * Get usage scenario processor for a given {@link UsageScenario}. I am not sure here, 
 * but maybe we have to be careful and  
 * the {@link UsageScenario} must be the one from the actual PCM model that has been analyzed, 
 * not the one referenced from the Criterion. These may differ if they have been loaded 
 * separately several Java objects may represent the same model objects.  
 * @param pcm
 * @param model
 * @param currentUsageScenaro
 * @return
 */
private ProcessorType getUsageScenarioProcessor(PCMInstance pcm,
		LqnModelType model, UsageScenario currentUsageScenaro)
{
	
	// Retrieve the usage scenario's name used within the result
	// Remove the number from the id as that may have changed(?) XXX 
	String scenarioName = Pcm2LqnHelper.getIdForUsageScenario(currentUsageScenaro);
	scenarioName = scenarioName.substring(0, scenarioName.lastIndexOf("_")); 
	// \\d stands for a digital number.
	String processorNameRegex = scenarioName + "_\\d+_Processor"; // see class LqnBuilder#getProcessorTypeFromModel(String)
	
	// Obtain processor, representing the usage scenario's overall resource demand
	ProcessorType processor = null;
	Iterator<ProcessorType> itProcessors = model.getProcessor().iterator();
	while (itProcessors.hasNext()) {
		ProcessorType proc = itProcessors.next();
		if (proc.getName().matches(processorNameRegex)) {
			processor = proc;
			break;
		}
	}
	return processor;
}

/**
 * 
 * @param pcm Can be any PCM instance, only the usage model is used.
 * @param model
 * @param criterion
 * @return
 * @throws AnalysisFailedException 
 * @throws ParseException 
 */
private double retrieveThroughputForUsageScenario() throws  ParseException {
	
	List<UsageScenario> scenarios = pcm.getUsageModel().getUsageScenario_UsageModel();
	UsageScenario usageScenario = scenarios.get(0);
	ProcessorType processor = getUsageScenarioProcessor(pcm, model,usageScenario);
	
	if (processor.getTask() != null && processor.getTask().size() > 0) {
		
		TaskType usageScenarioTask = processor.getTask().get(0);
		if (usageScenarioTask != null && usageScenarioTask.getResultTask().size()>0){
			OutputResultType outputResult = usageScenarioTask.getResultTask().get(0);
			if (outputResult != null){
				return LQNSResultParser.convertStringToDouble(outputResult.getThroughput().toString());
			}
		}
		
	}
	return Double.NaN;
	
}

/**
 * pcm must be the current candidate's PCM model. 
 * 
 * @param pcm
 * @param model
 * @return
 * @throws ParseException
 */
private ResultDecoratorRepository retrieveResults() throws ParseException {
	
	ResultDecoratorRepository repo = ResultdecoratorFactory.eINSTANCE.createResultDecoratorRepository();

	List<ProcessorType> modifiableProcessorList = retrieveResourceEnvironmentResults(
			pcm, model, repo);
	
	retrieveServiceResults(pcm, repo, modifiableProcessorList);
	
	return repo;
}

private void retrieveServiceResults(PCMInstance pcm,
		ResultDecoratorRepository repo,
		List<ProcessorType> modifiableProcessorList) throws ParseException {
	// retrieve response times
	List<Repository> repositories = pcm.getRepositories();
	for (Repository repository : repositories) {
		List<RepositoryComponent> repoComponentList = repository.getComponents__Repository();
		for (RepositoryComponent repositoryComponent : repoComponentList) {
			if (repositoryComponent instanceof BasicComponent){
				BasicComponent basicComponent = (BasicComponent)repositoryComponent;
				// pass the processor list that has already been used, then it does not contain the resources anymore.
				List<ServiceResult> serviceResultList = getServiceResults(basicComponent, modifiableProcessorList);
				repo.getServiceResult_ResultDecoratorRepository().addAll(serviceResultList);
			}
		}
	}
}

private List<ProcessorType> retrieveResourceEnvironmentResults(
		PCMInstance pcm, LqnModelType model, ResultDecoratorRepository repo)
		throws ParseException {
	// Retrieve utilisation
	List<ProcessorType> processors = model.getProcessor();
	List<ResourceContainer> containers = pcm.getResourceEnvironment().getResourceContainer_ResourceEnvironment();
	
	//create a new list from that we can remove things without modifying the model
	List<ProcessorType> modifiableProcessorList = new ArrayList<ProcessorType>(processors.size());
	modifiableProcessorList.addAll(processors);
	
	// get results for active resources
	for (ResourceContainer container : containers) {
		for (ProcessingResourceSpecification pcmResource : container.getActiveResourceSpecifications_ResourceContainer()) {
			String processorID = Pcm2LqnHelper.getIdForProcResource(container, pcmResource.getActiveResourceType_ActiveResourceSpecification())+"_Processor";
			
			ProcessingResourceSpecificationResult result = ResourceenvironmentdecoratorFactory.eINSTANCE.createProcessingResourceSpecificationResult();
			
			retrieveUtilResultFromLQN(modifiableProcessorList, processorID, result);
			//if the resource has not been found, it has not been used and its utilisation is 0 (the default value). 
			
			result.setProcessingResourceSpecification_ProcessingResourceSpecificationResult(pcmResource);
			result.setEntityName("Utilisation of "+processorID);
			
			repo.getUtilisationResults_ResultDecoratorRepository().add(result);
			
		}
	}
	
	List<LinkingResource> links = pcm.getResourceEnvironment().getLinkingResources__ResourceEnvironment();
	
	for (LinkingResource linkingResource : links) {
		String processorID = Pcm2LqnHelper.getIdForCommResource(linkingResource, linkingResource.getCommunicationLinkResourceSpecifications_LinkingResource().getCommunicationLinkResourceType_CommunicationLinkResourceSpecification())+"_Processor";
		
		LinkingResourceResults result = ResourceenvironmentdecoratorFactory.eINSTANCE.createLinkingResourceResults();
		
		retrieveUtilResultFromLQN(modifiableProcessorList, processorID,
				result);
		//if the resource has not been found, it has not been used and its utilisation is 0 (the default value). 
		
		result.setLinkingResource_LinkingResourceResults(linkingResource);
		result.setEntityName("Utilisation of "+processorID);
		
		repo.getUtilisationResults_ResultDecoratorRepository().add(result);
	}
	
	// results for passive resources
	List<AllocationContext> allAssemblyContexts = EMFHelper.getAllUsedAllocationContexts(pcm.getAllocation());
	for (AllocationContext allocContext : allAssemblyContexts) {
		AssemblyContext assemblyContext = allocContext.getAssemblyContext_AllocationContext();
		RepositoryComponent innerComponent = assemblyContext.getEncapsulatedComponent__AssemblyContext();
		if (innerComponent instanceof BasicComponent){
			
			BasicComponent basicComponent = (BasicComponent) innerComponent;
			List<PassiveResource> passiveResourceOfComponentList = basicComponent
					.getPassiveResource_BasicComponent();
			for (PassiveResource passiveResource : passiveResourceOfComponentList) {

				String passiveResourceId = Pcm2LqnHelper.getIdForPassiveResource(passiveResource, allocContext);
				
				PassiveResourceResult result = ResourceenvironmentdecoratorFactory.eINSTANCE.createPassiveResourceResult();
				
				retrieveUtilResultFromLQN(modifiableProcessorList, passiveResourceId,
						result);
				//if the resource has not been found, it has not been used and its utilisation is 0 (the default value). 
				
				result.setPassiveResource_PassiveResourceResult(passiveResource);
				result.setAssemblyContext_PassiveResourceResult(assemblyContext);
				result.setEntityName("Utilisation of "+passiveResourceId);
				
				
				repo.getUtilisationResults_ResultDecoratorRepository().add(result);
				
			}
	
		}
	}
	return modifiableProcessorList;
}

/* Sets utilization, average queue length and average waiting time, if available. 
* @param modifiableProcessorList
* @param processorID
* @param result
* @throws ParseException
*/
private void retrieveUtilResultFromLQN(	List<ProcessorType> modifiableProcessorList, 
										String processorID,
										UtilisationResult result) throws ParseException {
	//use iterator to be allowed to modify the list while searching
	Iterator<ProcessorType> lqnProcessorIterator = modifiableProcessorList.iterator();
	while (lqnProcessorIterator.hasNext()){
		ProcessorType lqnProc = lqnProcessorIterator.next();
		if (lqnProc.getName().contains(processorID)){

			List<OutputResultType> lqnResultProc = lqnProc.getResultProcessor();
			if (lqnResultProc.size() > 0){

				OutputResultType processorResult = lqnResultProc.get(0);

				String utilString = processorResult.getUtilization().toString();
				result.setResourceUtilisation(LQNSResultParser.convertStringToDouble(utilString));

				// determine waiting times and service times by checking all result entries (contained in the first task)
				double waitingTime = 0;
				double serviceTime = 0;

				double totalThroughput = 0;

				List<TaskType> resultTask = lqnProc.getTask();
				if (resultTask.size() > 0){

					List<EntryType> procTaskEntries = resultTask.get(0).getEntry();
					for (EntryType entryType : procTaskEntries) {
						List<OutputResultType> entryResults = entryType.getResultEntry();
						if (entryResults.size() > 0 ){

							OutputResultType entryResult = entryResults.get(0);
							String throughputString = entryResult.getThroughput().toString();

							EList<ActivityPhasesType> entryPhaseActivities = entryType.getEntryPhaseActivities().getActivity();
							if (entryPhaseActivities.size() > 0 ){
								List<OutputResultType> activityResults = entryPhaseActivities.get(0).getResultActivity();
								if (activityResults.size() > 0){
									String waitingTimeString = activityResults.get(0).getProcWaiting().toString();
									String serviceTimeString = activityResults.get(0).getServiceTime().toString();

									double throughput = LQNSResultParser.convertStringToDouble(throughputString);
									double entryWaitingTime = LQNSResultParser.convertStringToDouble(waitingTimeString);
									double entryServiceTime = LQNSResultParser.convertStringToDouble(serviceTimeString);

									// only look at service times greater than one, because the others do not seem to have to wait.
									// weight the current waiting time and service time by the current throughgput to get the overall times. 
									if (entryServiceTime > 0){
										waitingTime = entryWaitingTime * throughput;
										serviceTime = entryServiceTime * throughput;
										totalThroughput += throughput;
									}

								}
							}
						}
					}

					if (totalThroughput > 0 && serviceTime > 0){
						//	weight waiting time and service time by throughput.
						waitingTime = waitingTime / totalThroughput;
						serviceTime = serviceTime / totalThroughput;

						// 	queue length is queue waiting time / queue service time in average
						result.setAverageQueueLength(waitingTime / serviceTime);
						result.setAverageWaitTime(waitingTime);
						if (result instanceof ProcessingResourceSpecificationResult){
							((ProcessingResourceSpecificationResult) result).setDemandedTime(serviceTime);
						}
					}
				}


			}

			//we can remove this element now and do not have to iterate over this one again in the next loop iterations. 
			lqnProcessorIterator.remove();
		}
	}
}
private double retrieveMaxUtilization(PCMInstance pcm, LqnModelType model,
		ResultDecoratorRepository results2, String resourceTypeDescription) {
	double maxUtil = 0;
	for (UtilisationResult utilResult : results2.getUtilisationResults_ResultDecoratorRepository()) {
		if (utilResult instanceof ProcessingResourceSpecificationResult){
			ProcessingResourceSpecificationResult procResult = (ProcessingResourceSpecificationResult) utilResult;
			// check resource type
			if (procResult.getProcessingResourceSpecification_ProcessingResourceSpecificationResult().getActiveResourceType_ActiveResourceSpecification().getEntityName().contains(resourceTypeDescription)){
				if (maxUtil < procResult.getResourceUtilisation()){
					maxUtil = procResult.getResourceUtilisation();
				}
			}
			
		}
		
	}
	return maxUtil;
}
private List<ServiceResult> getServiceResults(BasicComponent basicComponent, List<ProcessorType> modifiableProcessorResultList) throws ParseException {
	
	
	
	List<ServiceEffectSpecification> seffList = basicComponent.getServiceEffectSpecifications__BasicComponent();
	List<ServiceResult> results = new ArrayList<ServiceResult>(seffList.size());
	
	//Create only one service result per SEFF: weight each response time by throughput and then divide by overall throughput.
	for (ServiceEffectSpecification seff : seffList) {
		
		double overallThroughput = 0;
		double responseTimesTimesThroughputSum = 0;
		
		String processorIDRegex = basicComponent.getEntityName() + "_"
			+ ((OperationSignature)seff.getDescribedService__SEFF()).getInterface__OperationSignature().getEntityName() + "_"
			+ seff.getDescribedService__SEFF().getEntityName(); // + "_*_Processor";
		
		// Obtain processor, representing the SEFF's overall resource demand
		ProcessorType processor = null;
		Iterator<ProcessorType> itProcessors = modifiableProcessorResultList.iterator();
		while (itProcessors.hasNext()) {
			ProcessorType proc = itProcessors.next();
			if (proc.getName().contains(processorIDRegex) && proc.getName().contains("_Processor")) {
				processor = proc;
				itProcessors.remove();
				
				List<TaskType> taskList = processor.getTask();
				if (taskList.size() > 0){

					double responseTime = LQNSResultParser.getResponseTimeOfSubActivities(taskList.get(0));
					double throughput = 0;
					
					List<EntryType> procTaskEntries = taskList.get(0)
							.getEntry();
					for (EntryType entryType : procTaskEntries) {
						List<OutputResultType> entryResults = entryType
								.getResultEntry();
						if (entryResults.size() > 0) {

							OutputResultType entryResult = entryResults
									.get(0);
							String throughputString = entryResult.getThroughput().toString();
							throughput += LQNSResultParser.convertStringToDouble(throughputString);
						}
					}
					
					responseTimesTimesThroughputSum += responseTime * throughput;
					overallThroughput += throughput;
				}
				
			}
		}
		
		// create result if service has been used
		if (overallThroughput > 0) {
			double overallResponseTime = responseTimesTimesThroughputSum
					/ overallThroughput;

			AllocationServiceResult serviceResult = RepositorydecoratorFactory.eINSTANCE
					.createAllocationServiceResult();

			// FIXME: currently only works with one allocation per seff
			// serviceResult.setAllocationcontext(value);
			serviceResult.setServiceEffectSpecification_ServiceResult(seff);
			serviceResult.setMeanResponseTime(overallResponseTime);
			results.add(serviceResult);
		}
		
	}
	
	return results;
}



}
