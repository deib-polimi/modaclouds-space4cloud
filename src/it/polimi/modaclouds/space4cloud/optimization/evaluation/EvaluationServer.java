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

import it.polimi.modaclouds.space4cloud.chart.Logger2JFreeChartImage;
import it.polimi.modaclouds.space4cloud.chart.SeriesHandle;
import it.polimi.modaclouds.space4cloud.db.DatabaseConnectionFailureExteption;
import it.polimi.modaclouds.space4cloud.optimization.constraints.ConstraintHandler;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Instance;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.SolutionMulti;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Tier;
import it.polimi.modaclouds.space4cloud.utils.Cache;
import it.polimi.modaclouds.space4cloud.utils.Configuration;
import it.polimi.modaclouds.space4cloud.utils.Configuration.Solver;
import it.polimi.modaclouds.space4cloud.utils.SolutionHelper;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	protected Logger2JFreeChartImage log2png;
	protected Logger2JFreeChartImage logVm;
	protected Logger2JFreeChartImage logConstraint;
	

	protected SeriesHandle seriesHandleExecution;
	protected Map<String,SeriesHandle> seriesHandleTiers;
	protected SeriesHandle seriesHandleConstraint;
	protected StopWatch timer = new StopWatch();
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
		timer.start();
		timer.split();

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

	public double deriveCosts(Solution sol) {
		long startTime = System.nanoTime();
		// costs
		double totalCost = 0;
		for (Instance i : sol.getHourApplication()) {
			double cost = costEvaulator.deriveCosts(i, sol.getHourApplication()
					.indexOf(i));
			totalCost += cost;
		}
		sol.setCost(totalCost);
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

	public void EvaluateSolution(Solution sol) {
		long startTime = -1;		
		requestedEvaluations++;
		logger.debug("Starting evaluation");
		error = false;
		if (!sol.isEvaluated()) {

			ArrayList<Instance> instanceList = sol.getHourApplication();
			counters.put(sol, 0);
			
			
			//reset the logger for line server handler
			if(Configuration.SOLVER!=Solver.LQNS){
				lineHandler.clear();
			}

			// evaluate hourly solutions
			for (Instance i : instanceList) {				
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
		if (constraintHandler != null)
			sol.setEvaluation(constraintHandler.evaluateFeasibility(sol));
		sol.updateEvaluation();

		
		
		
		// evaluate costs		
		deriveCosts(sol);
		timer.split();
		logger.trace("" + sol.getCost() + ", "
				+ TimeUnit.MILLISECONDS.toSeconds(timer.getSplitTime()) + ", "
				+ sol.isFeasible());
		long middleTime = System.nanoTime();
		if (log2png != null && logVm != null && logConstraint != null
				&& timer != null) {
			log2png.addPoint2Series(seriesHandleExecution,
					TimeUnit.MILLISECONDS.toSeconds(timer.getSplitTime()),
					sol.getCost());
			if(seriesHandleTiers==null){
				seriesHandleTiers = new HashMap<>();
				for(Tier t:sol.getApplication(0).getTiers()){					
					seriesHandleTiers.put(t.getId(),logVm.newSeries(t.getName()));					
				}
			}
				
			for(Tier t:sol.getApplication(0).getTiers()){
			logVm.addPoint2Series(seriesHandleTiers.get(t.getId()),
					TimeUnit.MILLISECONDS.toSeconds(timer.getSplitTime()),
					sol.getVmNumberPerTier(t.getId()));			
			}
			logConstraint.addPoint2Series(seriesHandleConstraint,
					TimeUnit.MILLISECONDS.toSeconds(timer.getSplitTime()),
					sol.getNumberOfViolatedConstraints());
		}
		timer.unsplit();
		long endTime = System.nanoTime();
		if (startTime != -1) {
			evaluationTime += (middleTime - startTime);
			plottingTime += (endTime - middleTime);
			fullEvaluationTime += (endTime - startTime);
			logger.debug("Evaluation number: "+actualNumberOfEvaluations+" Time: "+(middleTime-startTime));
		}else
			logger.debug("Evaluation number: "+actualNumberOfEvaluations+" hitted proxy");
		
		
		
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

	public void setConstraintLog(Logger2JFreeChartImage logConstraint) {
		this.logConstraint = logConstraint;
		seriesHandleConstraint = logConstraint.newSeries("violatedConstraints");
	}

	public void setLog2png(Logger2JFreeChartImage log2png) {
		this.log2png = log2png;
		seriesHandleExecution = log2png.newSeries("Current Solutions");

	}

	public void setMachineLog(Logger2JFreeChartImage logVM) {
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

	public void terminateServer() {
		if(lineHandler!= null)
			lineHandler.terminateLine();
		if(executor!=null)
			executor.shutdownNow();
	}

	public void EvaluateSolution(SolutionMulti sol) {
		for (Solution s : sol.getAll())
			EvaluateSolution(s);
		sol.updateEvaluation();
	}
}
