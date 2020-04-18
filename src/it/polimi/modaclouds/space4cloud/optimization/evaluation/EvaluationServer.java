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

package it.polimi.modaclouds.space4cloud.optimization.evaluation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.time.StopWatch;
import org.jfree.data.xy.XYSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.polimi.modaclouds.resourcemodel.cloud.CloudElement;
import it.polimi.modaclouds.space4cloud.chart.GenericChart;
import it.polimi.modaclouds.space4cloud.db.DatabaseConnectionFailureExteption;
import it.polimi.modaclouds.space4cloud.exceptions.ConstraintEvaluationException;
import it.polimi.modaclouds.space4cloud.exceptions.EvaluationException;
import it.polimi.modaclouds.space4cloud.optimization.bursting.PrivateCloud;
import it.polimi.modaclouds.space4cloud.optimization.constraints.Constraint;
import it.polimi.modaclouds.space4cloud.optimization.constraints.ConstraintHandler;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Instance;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.SolutionMulti;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Tier;
import it.polimi.modaclouds.space4cloud.utils.Cache;
import it.polimi.modaclouds.space4cloud.utils.Configuration;
import it.polimi.modaclouds.space4cloud.utils.Configuration.Solver;
import it.polimi.modaclouds.space4cloud.utils.SolutionHelper;

/**
 * @author Michele Ciavotta
 * 
 */
public class EvaluationServer implements ActionListener {

	private static final int DEFAULT_TIER_MEMORY_MAX_SIZE = 30;	
	protected ThreadPoolExecutor executor;
	protected BlockingQueue<Runnable> queue = new SynchronousQueue<>();
	protected int nMaxThreads = 24 * 100; // 24*100
	protected HashMap<Solution, Integer> counters = new HashMap<>();
	protected CostEvaluator costEvaulator;
	protected ConstraintHandler constraintHandler;
	protected int actualNumberOfEvaluations = 0;
	protected int requestedEvaluations = 0; 
	protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	protected Logger logger = LoggerFactory.getLogger(EvaluationServer.class);

	protected String seriesHandleExecution;
	protected Map<String,String> seriesHandleTiers;
	protected String seriesHandleConstraint;

	protected GenericChart<XYSeriesCollection> logCost;
	protected GenericChart<XYSeriesCollection> logVm;
	protected GenericChart<XYSeriesCollection> logConstraint;

	protected StopWatch timer;
	protected long costEvaluationTime = 0;
	protected int costEvaluations = 0;
	protected long evaluationTime = 0;
	protected long plottingTime = 0;
	protected long fullEvaluationTime = 0;
	private LineServerHandler lineHandler;
	private boolean instanceEvaluationTerminated = false;
	private Map<String,Cache<String,Integer>> longTermFrequencyMemory;
	private boolean error=false;

	/**
	 * @throws DatabaseConnectionFailureExteption 
	 * 
	 */
	public EvaluationServer() throws DatabaseConnectionFailureExteption {
		// set min to 24
		costEvaulator = new CostEvaluator();
		executor = new ThreadPoolExecutor(24, nMaxThreads, 200,
				TimeUnit.MILLISECONDS, queue);
		if (Configuration.SOLVER==Solver.LINE) {
			lineHandler = LineServerHandlerFactory.getHandler();
			lineHandler.connectToLINEServer();
		}

	}

	@Override
	public synchronized void actionPerformed(ActionEvent e) {

		if (e.getActionCommand() != null
				&& e.getActionCommand().equals("ResultsUpdated")) {
			Solution sol = ((SolutionEvaluator) e.getSource()).getSolution();
			if(sol!=null)
				counters.put(sol, counters.get(sol) + 1);
			instanceEvaluationTerminated = true;
		} else if (e.getActionCommand() != null
				&& e.getActionCommand().equals("EvaluationError")) {
			logger.error("An error occured in the evaluation of a performance model");
			error = true;
		} 
		else {

			logger.warn("Unknown Action from: "+e.getSource());
		}

	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	public double deriveCosts(Solution sol) throws EvaluationException {
		long startTime = System.nanoTime();
		// costs
        double totalCost = 0;
        for (int h = 0; h < 24; ++h) {
            Instance i = sol.getApplication(h);
            double cost;
			try {
				cost = costEvaulator.deriveCosts(i, h);
			} catch (CostEvaluationException | EmptyCostException e) {
				String resourceMessage="";
				if(e instanceof CostEvaluationException )
					resourceMessage = " on resource with id: "+((CostEvaluationException) e).getElement().getId()+" Name: "+((CostEvaluationException) e).getElement().getName()+" Type: "+((CostEvaluationException) e).getElement().getType();
				logger.error("Cost Evaluation Exception"+resourceMessage,e);
				throw new EvaluationException("Cost Evaluation Exception"+resourceMessage, e);
			}
            totalCost += cost;
//            sol.setCost(h, cost);
        }
        if(totalCost > 50)
			System.out.println("Stop");
		logger.info("Cost>"+totalCost);		
        long stopTime = System.nanoTime();

		costEvaluationTime += (stopTime - startTime);
		costEvaluations++;
		return totalCost;
	}

	public void evaluateInstance(Instance instance) {
		instanceEvaluationTerminated = false;
		if (!instance.isEvaluated()) {
			SolutionEvaluator eval = new SolutionEvaluator(instance, null);
			eval.addListener(this);

			//reset the logger for line server handler
			if(Configuration.SOLVER!=Solver.LQNS){
				lineHandler.clear();
			}

			SolutionEvaluator.logger.trace("Evaluating instance "+instance);
			if (Configuration.SOLVER == Solver.LQNS)
				executor.execute(eval);
			else {
				eval.setLineServerHandler(lineHandler);
				executor.execute(eval);
			}
		} else
			instanceEvaluationTerminated = true;
		while (!instanceEvaluationTerminated)
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				logger.error("Error while evaluating an instance",e);
			}

	}

	//	public void EvaluateSolution(Solution sol) {
	//		long startTime = -1;		
	//		requestedEvaluations++;
	//		logger.debug("Starting evaluation");
	//		error = false;
	//		if (!sol.isEvaluated()) {
	//
	//			startTime = System.nanoTime();
	//
	//			ArrayList<Instance> instanceList = sol.getHourApplication();
	//			counters.put(sol, 0);
	//			
	//			
	//			//reset the logger for line server handler
	//			if(Configuration.SOLVER!=Solver.LQNS){
	//				lineHandler.clear();
	//			}
	//
	//			// evaluate hourly solutions
	//			for (Instance i : instanceList) {
	//				// we need to reevaluate it only if something has changed.
	//				if (!i.isEvaluated()) {
	//					SolutionEvaluator eval = new SolutionEvaluator(i,sol);
	//					eval.addListener(this);
	//					if (Configuration.SOLVER == Solver.LQNS)
	//						executor.execute(eval);
	//					else {
	//						eval.setLineServerHandler(lineHandler);
	//						executor.execute(eval);
	//					}
	//				} else
	//					counters.put(sol, counters.get(sol) + 1);// if the
	//																// application
	//																// has already
	//																// been
	//																// evaluated
	//																// increment the
	//																// counter
	//			}
	//
	//			while (counters.get(sol) < 24 && !error)
	//				try {
	//					Thread.sleep(100);
	//				} catch (InterruptedException e) {
	//					logger.error("Error waiting for the evaluation of a solution",e);
	//				} // loop until everything has been evaluated
	//
	//			if(error){
	//				logger.error("Error evaluating a solution");
	//				pcs.firePropertyChange("EvaluationError", false, true);
	//				return;
	//			}
	//			
	//			// remove the counters for the evaluated solution
	//			counters.remove(sol);
	//			incrementTotalNumberOfEvaluations();
	//		}
	//
	//		// evaluate feasibility
	//		if (constraintHandler != null)
	//			sol.setEvaluation(constraintHandler.evaluateFeasibility(sol));
	//		sol.updateEvaluation();
	//
	//		// evaluate costs		
	//		deriveCosts(sol);
	//
	//		logger.trace("" + sol.getCost() + ", "
	//				+ TimeUnit.MILLISECONDS.toSeconds(timer.getSplitTime()) + ", "
	//				+ sol.isFeasible());
	//
	//		long middleTime = System.nanoTime();
	//		if (log2png != null && logVm != null && logConstraint != null
	//				&& timer != null) {
	//			timer.split();
	//			log2png.addPoint2Series(seriesHandleExecution,
	//					TimeUnit.MILLISECONDS.toSeconds(timer.getSplitTime()),
	//					sol.getCost());
	//			if(seriesHandleTiers==null){
	//				seriesHandleTiers = new HashMap<>();
	//				for(Tier t:sol.getApplication(0).getTiers()){					
	//					seriesHandleTiers.put(t.getId(),logVm.newSeries(t.getName()));					
	//				}
	//			}
	//				
	//			for(Tier t:sol.getApplication(0).getTiers()){
	//			logVm.addPoint2Series(seriesHandleTiers.get(t.getId()),
	//					TimeUnit.MILLISECONDS.toSeconds(timer.getSplitTime()),
	//					sol.getVmNumberPerTier(t.getId()));			
	//			}
	//			logConstraint.addPoint2Series(seriesHandleConstraint,
	//					TimeUnit.MILLISECONDS.toSeconds(timer.getSplitTime()),
	//					sol.getNumberOfViolatedConstraints());
	//		}
	//		long endTime = System.nanoTime();
	//		if (startTime != -1) {
	//			evaluationTime += (middleTime - startTime);
	//			plottingTime += (endTime - middleTime);
	//			fullEvaluationTime += (endTime - startTime);
	//			logger.debug("Evaluation number: "+actualNumberOfEvaluations+" Time: "+(middleTime-startTime));
	//		}else
	//			logger.debug("Evaluation number: "+actualNumberOfEvaluations+" hitted proxy");
	//		sol.setEvaluationTime(timer.getSplitTime());
	//		
	//		
	//		
	//		logger.debug("Update long term memory");
	//		if(longTermFrequencyMemory==null){
	//			longTermFrequencyMemory = new HashMap<String, Cache<String,Integer>>();			
	//		}
	//		
	//		for(Tier t:sol.getApplication(0).getTiers()){			
	//			Cache<String, Integer> tierMemory = longTermFrequencyMemory.get(t.getId());
	//			if(tierMemory == null){
	//				tierMemory = new Cache<String, Integer>(DEFAULT_TIER_MEMORY_MAX_SIZE);
	//				longTermFrequencyMemory.put(t.getId(), tierMemory);
	//			}
	//			//get the tierTypeID
	//			String tierTypeID = SolutionHelper.buildTierTypeID(t);
	//			int counter = 0;
	//			//if the tierTypeID already appears in the memory get the counter and update it
	//			if(tierMemory.get(tierTypeID)!= null)
	//				counter = tierMemory.get(tierTypeID);
	//			counter++;
	//			//if the tier is does not appear a new entry is created otherwise the old one is overritten.
	//			tierMemory.put(tierTypeID, counter);
	//		}
	//		logger.debug("Evaluation ended");
	//
	//	}



//	/**
//	 * Evaluates a single cloud solution and updates the image loggers
//	 * @param sol
//	 * @throws EvaluationException
//	 */
//	public void EvaluateSolution(Solution sol) throws EvaluationException{
//		try{
//			runEvaluation(sol);
//		}catch(EvaluationException e){
//			throw e;
//		}		
//		updateLogImage(sol);		
//	}


	/**
	 * Evaluates a multicloud solution and updates the image loggers
	 * @param sol
	 * @throws EvaluationException
	 */
	public void EvaluateSolution(SolutionMulti sol) throws EvaluationException {
		for (Solution s : sol.getAll())
			runEvaluation(s);	
		sol.updateEvaluation();
		updateLogImage(sol);
	}

	/**
	 * Evaluate a general solution, the solution might be empty, public or private
	 * @param sol
	 * @throws EvaluationException
	 */
	private void runEvaluation(Solution sol) throws EvaluationException  {		
		if (!sol.hasAtLeastOneReplicaInOneHour()) {
			runEmptyEvaluation(sol);
			return;
		}

		if (sol.getProvider().indexOf(PrivateCloud.BASE_PROVIDER_NAME) > -1) {
			runPrivateEvaluation(sol);
			return;
		}

		logger.debug("Starting evaluation");
		error = false;
		if (!sol.isEvaluated()) {

			ArrayList<Instance> instanceList = sol.getHourApplication();
			counters.put(sol, 0);


			//reset the logger for line server handler
			if(Configuration.SOLVER==Solver.LINE){
				lineHandler.clear();
			}

			// evaluate hourly solutions
			for (Instance i : instanceList) {

				//if the instance has no user then skip the evaluation
				if(i.getWorkload()==0)
					i.setEvaluated(true);



				// we need to reevaluate it only if something has changed.
				if (!i.isEvaluated()) {
					SolutionEvaluator eval = new SolutionEvaluator(i,sol);
					SolutionEvaluator.logger.trace("Evaluating instance "+sol.getHourApplication().indexOf(i)+" of solution "+sol);
					eval.addListener(this);
					if (Configuration.SOLVER == Solver.LQNS)
						executor.execute(eval);
					else {
						eval.setLineServerHandler(lineHandler);
						executor.execute(eval);
					}
				} else
					counters.put(sol, counters.get(sol) + 1);// if the
				// application
				// has already
				// been
				// evaluated
				// increment the
				// counter
			}

			while (counters.get(sol) < 24 && !error)
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					logger.error("Error waiting for the evaluation of a solution",e);
				} // loop until everything has been evaluated

			if(error){
				logger.error("Error evaluating a solution");
				pcs.firePropertyChange("EvaluationError", false, true);
				return;
			}

			// remove the counters for the evaluated solution
			counters.remove(sol);
			incrementTotalNumberOfEvaluations();
		}



		// evaluate feasibility
		if (constraintHandler != null){
			List<HashMap<Constraint, Boolean>> evaluations;
			try {
				evaluations = constraintHandler.evaluateFeasibility(sol);
			} catch (ConstraintEvaluationException e) {
				throw new EvaluationException("An error occured in the constraint evaluation",e);
			}
			sol.setEvaluation(evaluations);
		}
		sol.updateEvaluation();

		// evaluate costs		
		deriveCosts(sol);


		timer.split();
		sol.setGenerationTime(timer.getSplitTime());

		logger.debug("Update long term memory");
		if(longTermFrequencyMemory==null){
			longTermFrequencyMemory = new HashMap<String, Cache<String,Integer>>();			
		}

		for(Tier t:sol.getApplication(0).getTiers()){			
			Cache<String, Integer> tierMemory = longTermFrequencyMemory.get(t.getId());
			if(tierMemory == null){
				tierMemory = new Cache<String, Integer>(DEFAULT_TIER_MEMORY_MAX_SIZE);
				longTermFrequencyMemory.put(t.getId(), tierMemory);
			}
			//get the tierTypeID
			String tierTypeID = SolutionHelper.buildTierTypeID(t);
			int counter = 0;
			//if the tierTypeID already appears in the memory get the counter and update it
			if(tierMemory.get(tierTypeID)!= null)
				counter = tierMemory.get(tierTypeID);
			counter++;
			//if the tier is does not appear a new entry is created otherwise the old one is overritten.
			tierMemory.put(tierTypeID, counter);
		}
		logger.debug("Evaluation ended");

	}

	/**
	 * Evaluates and empty solution (a solution with no replicas)
	 * @param sol
	 * @throws EvaluationException
	 */
	private void runEmptyEvaluation(Solution sol) throws EvaluationException {
		
		error = false;
		if (!sol.isEvaluated()) {
			ArrayList<Instance> instanceList = sol.getHourApplication();

			// evaluate hourly solutions
			for (Instance i : instanceList) {
				// the solution is empty, thus there's no point in actually calling LQNS
				i.setEvaluated(true);
			}
			incrementTotalNumberOfEvaluations();
		}


		try {
			// evaluate feasibility
			if (constraintHandler != null)
				sol.setEvaluation(constraintHandler.evaluateFeasibility(sol));
		} catch (ConstraintEvaluationException e) {
			throw new EvaluationException("Error in evaluating the empty solution ",e);
		}
		sol.updateEvaluation();

		// evaluate costs		
		deriveCosts(sol);
		timer.split();
		sol.setGenerationTime(timer.getSplitTime());		
		logger.debug("Update long term memory");
		if(longTermFrequencyMemory==null){
			longTermFrequencyMemory = new HashMap<String, Cache<String,Integer>>();			
		}

		for(Tier t:sol.getApplication(0).getTiers()){			
			Cache<String, Integer> tierMemory = longTermFrequencyMemory.get(t.getId());
			if(tierMemory == null){
				tierMemory = new Cache<String, Integer>(DEFAULT_TIER_MEMORY_MAX_SIZE);
				longTermFrequencyMemory.put(t.getId(), tierMemory);
			}
			//get the tierTypeID
			String tierTypeID = SolutionHelper.buildTierTypeID(t);
			int counter = 0;
			//if the tierTypeID already appears in the memory get the counter and update it
			if(tierMemory.get(tierTypeID)!= null)
				counter = tierMemory.get(tierTypeID);
			counter++;
			//if the tier is does not appear a new entry is created otherwise the old one is overritten.
			tierMemory.put(tierTypeID, counter);
		}
		logger.debug("Evaluation ended");
	}

	/**
	 * Evaluiates a solutioin in the Provate Cloud
	 * @param sol
	 * @throws EvaluationException
	 */
	private void runPrivateEvaluation(Solution sol)  throws EvaluationException {
		if (sol.getProvider().indexOf(PrivateCloud.BASE_PROVIDER_NAME) == -1) {
			runEvaluation(sol);
			logger.error("Evaluating a public cloud solution in the wrong function, this should never happen");
			return;
		}

		error = false;
		if (!sol.isEvaluated()) {


			ArrayList<Instance> instanceList = sol.getHourApplication();
			counters.put(sol, 0);


			//reset the logger for line server handler
			if(Configuration.SOLVER==Solver.LINE){
				lineHandler.clear();
			}

			// evaluate hourly solutions
			for (Instance i : instanceList) {
				// we need to reevaluate it only if something has changed.
				if (!i.isEvaluated()) {
					SolutionEvaluator eval = new SolutionEvaluator(i,sol);
					eval.addListener(this);
					if (Configuration.SOLVER == Solver.LQNS)
						executor.execute(eval);
					else {
						eval.setLineServerHandler(lineHandler);
						executor.execute(eval);
					}
				} else
					counters.put(sol, counters.get(sol) + 1);// if the
				// application
				// has already
				// been
				// evaluated
				// increment the
				// counter
			}

			while (counters.get(sol) < 24 && !error)
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					throw new EvaluationException("An error occured during the evaluation of the solution",e); 
				} // loop until everything has been evaluated

			if(error){
				logger.error("Error evaluating a solution");
				pcs.firePropertyChange("EvaluationError", false, true);
				return;
			}

			// remove the counters for the evaluated solution
			counters.remove(sol);
			incrementTotalNumberOfEvaluations();
		}
		try {
			// evaluate feasibility
			if (constraintHandler != null)
				sol.setEvaluation(constraintHandler.evaluateFeasibility(sol));
		} catch (ConstraintEvaluationException e) {
			throw new EvaluationException("Erro evaluatinga private solution",e);
		}
		sol.updateEvaluation();




		// evaluate costs		
		deriveCosts(sol);
		timer.split();
		sol.setGenerationTime(timer.getSplitTime());
		logger.debug("Update long term memory");
		if(longTermFrequencyMemory==null){
			longTermFrequencyMemory = new HashMap<String, Cache<String,Integer>>();			
		}

		for(Tier t:sol.getApplication(0).getTiers()){			
			Cache<String, Integer> tierMemory = longTermFrequencyMemory.get(t.getId());
			if(tierMemory == null){
				tierMemory = new Cache<String, Integer>(DEFAULT_TIER_MEMORY_MAX_SIZE);
				longTermFrequencyMemory.put(t.getId(), tierMemory);
			}
			//get the tierTypeID
			String tierTypeID = SolutionHelper.buildTierTypeID(t);
			int counter = 0;
			//if the tierTypeID already appears in the memory get the counter and update it
			if(tierMemory.get(tierTypeID)!= null)
				counter = tierMemory.get(tierTypeID);
			counter++;
			//if the tier is does not appear a new entry is created otherwise the old one is overritten.
			tierMemory.put(tierTypeID, counter);
		}
		logger.debug("Evaluation ended");

	}

	/**
	 * Gets the n max thread.
	 * 
	 * @return the n max thread
	 */
	public int getnMaxThread() {
		return nMaxThreads;
	}

	public CostEvaluator getCostEvaulator() {
		return costEvaulator;
	}

	public int getTotalNumberOfEvaluations() {
		return actualNumberOfEvaluations;
	}

	private void incrementTotalNumberOfEvaluations() {
		int temp = actualNumberOfEvaluations;
		actualNumberOfEvaluations++;
		pcs.firePropertyChange("totalNumberOfEvaluations", temp,
				actualNumberOfEvaluations);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

	public void setConstraintHandler(ConstraintHandler constraintHandler) {
		this.constraintHandler = constraintHandler;
	}

	public void setConstraintLog(GenericChart<XYSeriesCollection> logConstraint) {
		this.logConstraint = logConstraint;
		seriesHandleConstraint = "violatedConstraints";
	}

	public void setLog2png(GenericChart<XYSeriesCollection> log2png) {
		this.logCost = log2png;
		seriesHandleExecution = "Current Solutions";

	}

	public void setMachineLog(GenericChart<XYSeriesCollection> logVM) {
		this.logVm = logVM;		
	}

	public Map<String, Cache<String, Integer>> getLongTermFrequencyMemory() {
		return longTermFrequencyMemory;
	}

	public void setLongTermFrequencyMemory(
			Map<String, Cache<String, Integer>> longTermFrequencyMemory) {
		this.longTermFrequencyMemory = longTermFrequencyMemory;
	}

	public void setTimer(StopWatch timer) {
		this.timer = timer;

	}

	/**
	 * logs the statistics of the evaluations performed so far
	 */
	public void showStatistics() {
		logger.info("Avg cost evaluation time: "
				+ (TimeUnit.MILLISECONDS.convert(costEvaluationTime
						/ costEvaluations, TimeUnit.NANOSECONDS)));
		logger.info("Avg plotting time: "
				+ TimeUnit.MILLISECONDS.convert(plottingTime
						/ actualNumberOfEvaluations, TimeUnit.NANOSECONDS));
		logger.info("Avg full evaluation time: "
				+ TimeUnit.MILLISECONDS.convert(fullEvaluationTime
						/ actualNumberOfEvaluations, TimeUnit.NANOSECONDS));
		logger.info("Avg evaluation time: "
				+ TimeUnit.MILLISECONDS.convert(evaluationTime
						/ actualNumberOfEvaluations, TimeUnit.NANOSECONDS));
		logger.info("Number of fully evaluated solutions: "
				+ actualNumberOfEvaluations);
		logger.info("Number of total cost evaluations: " + costEvaluations);
		logger.info("Number of solutions with only cost evaluations "
				+ (costEvaluations - actualNumberOfEvaluations));

	}

	/**
	 * Terminates the evaluation server shtting down the threads and quitting LINE, if necessary
	 */
	public void terminateServer() {
		if(lineHandler!= null)
			lineHandler.terminateLine();
		if(executor!=null)
			executor.shutdownNow();
	}


	/**
	 * Update image loggers for number of VMs per tier, cost of the solution and number of violations by summing up all the values for each hour
	 * @param sol
	 */
	private void updateLogImage(Solution sol) {
		long time = timer.getSplitTime();		
		logger.trace("" + sol.getCost() + ", "
				+ sol.isFeasible());
		if (logCost != null && logVm != null && logConstraint != null
				&& timer != null) {

			logCost.add(seriesHandleExecution,
					TimeUnit.MILLISECONDS.toSeconds(time),
					sol.getCost());
			if(seriesHandleTiers==null){
				seriesHandleTiers = new HashMap<>();
				for(Tier t:sol.getApplication(0).getTiers()){					
					seriesHandleTiers.put(t.getId(), t.getPcmName());					
				}
			}

			for(Tier t:sol.getApplication(0).getTiers()){
				logVm.add(seriesHandleTiers.get(t.getId()),
						TimeUnit.MILLISECONDS.toSeconds(time),
						sol.getVmNumberPerTier(t.getId()));			
			}
			logConstraint.add(seriesHandleConstraint,
					TimeUnit.MILLISECONDS.toSeconds(time),
					sol.getNumberOfViolatedConstraints());
		}
	}

	/**
	 * Update image loggers for number of VMs per tier, cost of the solution and number of violations by summing up all the values for each hour and each cloud
	 * @param sol
	 */
	private void updateLogImage(SolutionMulti sol) {
		long time = timer.getSplitTime();		
		logger.trace("" + sol.getCost() + ", "
				+ sol.isFeasible());
		if (logCost != null && logVm != null && logConstraint != null
				&& timer != null) {

			//update the cost
			logCost.add(seriesHandleExecution,
					TimeUnit.MILLISECONDS.toSeconds(time),
					sol.getCost());


			//build a list of tiers with their ids
			if(seriesHandleTiers==null){
				seriesHandleTiers = new HashMap<>();
				//Tier names and ids are the same for each cloud and each hours
				for(Tier t:sol.get(0).getApplication(0).getTiers()){					
					seriesHandleTiers.put(t.getId(), t.getPcmName());					
				}
			}

			//sum up the vms for all the tiers over all the solutions (by hour and by cloud)\
			for(String tierId:seriesHandleTiers.keySet()){
				int vmCounter = 0;
				for(Solution cloudSolution:sol.getAll()){
					vmCounter += cloudSolution.getVmNumberPerTier(tierId);
				}	
				logVm.add(seriesHandleTiers.get(tierId),TimeUnit.MILLISECONDS.toSeconds(time),vmCounter);
			}

			//sum up the violations of all solutions (by hour and by cloud)
			int violationCounters = 0;
			for(Solution cloudSolution:sol.getAll()){
				violationCounters += cloudSolution.getNumberOfViolatedConstraints();			
			}
			logConstraint.add(seriesHandleConstraint,
					TimeUnit.MILLISECONDS.toSeconds(time),
					violationCounters);


		}
	}



}
