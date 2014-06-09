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
import it.polimi.modaclouds.space4cloud.optimization.constraints.ConstraintHandler;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Instance;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution;
import it.polimi.modaclouds.space4cloud.utils.Constants;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uka.ipd.sdq.pcmsolver.runconfig.MessageStrings;

/**
 * @author Michele Ciavotta
 * 
 */
public class EvaluationServer implements ActionListener {

	protected ThreadPoolExecutor executor;
	protected BlockingQueue<Runnable> queue = new SynchronousQueue<>();
	protected int nMaxThreads = 24 * 100; // 24*100
	protected HashMap<Solution, Integer> counters = new HashMap<>();
	protected CostEvaluator costEvaulator = new CostEvaluator();
	protected ConstraintHandler constraintHandler;
	protected int totalNumberOfEvaluations = 0;
	protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	protected Logger logger = LoggerFactory.getLogger(EvaluationServer.class);

	protected Logger2JFreeChartImage log2png;
	protected Logger2JFreeChartImage logVm;
	protected Logger2JFreeChartImage logConstraint;
	protected String solver = MessageStrings.LQNS_SOLVER;

	protected SeriesHandle seriesHandleExecution;
	protected SeriesHandle seriesHandleTier1;
	protected SeriesHandle seriesHandleTier2;
	protected SeriesHandle seriesHandleConstraint;
	protected StopWatch timer = new StopWatch();
	protected long costEvaluationTime = 0;
	protected int costEvaluations = 0;
	protected long evaluationTime = 0;
	protected long plottingTime = 0;
	protected long fullEvaluationTime = 0;
	private LineServerHandler handler;
	private boolean instanceEvaluationTerminated = false;

	/**
	 * 
	 */
	public EvaluationServer(String solver) {
		// set min to 24
		executor = new ThreadPoolExecutor(24, nMaxThreads, 200,
				TimeUnit.MILLISECONDS, queue);
		if (solver.equals(MessageStrings.PERFENGINE_SOLVER)) {
			handler = new LineServerHandler();
			handler.connectToLINEServer(Constants.getInstance().LINE_PROPERTIES_FILE);
		}
		timer.start();
		timer.split();
		this.solver = solver;

	}

	@Override
	public synchronized void actionPerformed(ActionEvent e) {

		if (e.getActionCommand() != null
				&& e.getActionCommand().equals("ResultsUpdated")) {
			instanceEvaluationTerminated = true;
		} else {

			Solution sol = ((SolutionEvaluator) e.getSource()).getSolution();
			counters.put(sol, counters.get(sol) + 1);
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

	public void evaluateInstance(Instance instance, String solver) {
		instanceEvaluationTerminated = false;
		if (!instance.isEvaluated()) {
			SolutionEvaluator eval = new SolutionEvaluator(instance, solver,
					null);
			eval.addListener(this);
			if (solver.equals(MessageStrings.LQNS_SOLVER))
				eval.parseResults();
			else {
				eval.setLineServerHandler(handler);
				eval.parseResults();
			}
		}
		while (!instanceEvaluationTerminated)
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}

	public void EvaluateSolution(Solution sol) {
		long startTime = -1;
		if (!sol.isEvaluated()) {

			startTime = System.nanoTime();

			ArrayList<Instance> instanceList = sol.getHourApplication();
			counters.put(sol, 0);
			// evaluate hourly solutions

			for (Instance i : instanceList) {
				// we need to reevaluate it only if something has changed.
				if (!i.isEvaluated()) {
					SolutionEvaluator eval = new SolutionEvaluator(i, solver,
							sol);
					eval.addListener(this);
					if (solver.equals(MessageStrings.LQNS_SOLVER))
						executor.execute(eval);
					else {
						eval.setLineServerHandler(handler);
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

			while (counters.get(sol) < 24)
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} // loop until everything has been evaluated

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

		logger.info("" + sol.getCost() + ", "
				+ TimeUnit.MILLISECONDS.toSeconds(timer.getSplitTime()) + ", "
				+ sol.isFeasible());

		long middleTime = System.nanoTime();
		if (log2png != null && logVm != null && logConstraint != null
				&& timer != null) {
			timer.split();
			log2png.addPoint2Series(seriesHandleExecution,
					TimeUnit.MILLISECONDS.toSeconds(timer.getSplitTime()),
					sol.getCost());
			logVm.addPoint2Series(seriesHandleTier1,
					TimeUnit.MILLISECONDS.toSeconds(timer.getSplitTime()),
					sol.getVmNumberPerTier(0));
			logVm.addPoint2Series(seriesHandleTier2,
					TimeUnit.MILLISECONDS.toSeconds(timer.getSplitTime()),
					sol.getVmNumberPerTier(1));
			logConstraint.addPoint2Series(seriesHandleConstraint,
					TimeUnit.MILLISECONDS.toSeconds(timer.getSplitTime()),
					sol.getNumberOfViolatedConstraints());
		}
		long endTime = System.nanoTime();
		if (startTime != -1) {
			evaluationTime += (middleTime - startTime);
			plottingTime += (endTime - middleTime);
			fullEvaluationTime += (endTime - startTime);
		}
		sol.setEvaluationTime(timer.getSplitTime());

	}

	/**
	 * Gets the n max thread.
	 * 
	 * @return the n max thread
	 */
	public int getnMaxThread() {
		return nMaxThreads;
	}

	public String getSolver() {
		return solver;
	}

	public int getTotalNumberOfEvaluations() {
		return totalNumberOfEvaluations;
	}

	private void incrementTotalNumberOfEvaluations() {
		int temp = totalNumberOfEvaluations;
		totalNumberOfEvaluations++;
		pcs.firePropertyChange("totalNumberOfEvaluations", temp,
				totalNumberOfEvaluations);
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
		seriesHandleTier1 = logVm.newSeries("Tier1VMs");
		seriesHandleTier2 = logVm.newSeries("Tier2VMs");
	}

	public void setSolver(String solver) {
		this.solver = solver;

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
						/ totalNumberOfEvaluations, TimeUnit.NANOSECONDS));
		logger.info("Avg full evaluation time: "
				+ TimeUnit.MILLISECONDS.convert(fullEvaluationTime
						/ totalNumberOfEvaluations, TimeUnit.NANOSECONDS));
		logger.info("Avg evaluation time: "
				+ TimeUnit.MILLISECONDS.convert(evaluationTime
						/ totalNumberOfEvaluations, TimeUnit.NANOSECONDS));
		logger.info("Number of fully evaluated solutions: "
				+ totalNumberOfEvaluations);
		logger.info("Number of total cost evaluations: " + costEvaluations);
		logger.info("Number of solutions with only cost evaluations "
				+ (costEvaluations - totalNumberOfEvaluations));

	}

	public void terminateServer() {
		handler.terminateLine();
	}
}
