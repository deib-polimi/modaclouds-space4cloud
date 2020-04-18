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
package it.polimi.modaclouds.space4cloud.optimization;

import it.polimi.modaclouds.space4cloud.db.DatabaseConnectionFailureExteption;
import it.polimi.modaclouds.space4cloud.exceptions.ConstraintEvaluationException;
import it.polimi.modaclouds.space4cloud.exceptions.EvaluationException;
import it.polimi.modaclouds.space4cloud.exceptions.OptimizationException;
import it.polimi.modaclouds.space4cloud.optimization.constraints.ConstraintHandler;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.ParticleSwarm;
import it.polimi.modaclouds.space4cloud.utils.Configuration;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeListener;
import java.io.IOException;

/**
 * @author Michele Ciavotta
 * Class defining the optimization engine.
 * This class implements a Discrete Particle Swarm Optimization Algorithm
 */
public class OptimizationEngineDPSO extends OptimizationEngine implements PropertyChangeListener {

    public static final int SWARM_SIZE = 5;
    public static final double COGNITIVE_SCALE = 2.0;
    public static final double SOCIAL_SCALE = 2.0;
    public static final double MAX_CONVERGENCE_PERCENTAGE = 0.95;
    public static final double CR = 0.9;
    public static final double INITIAL_INERTIA = 0.9;
    private int iteration;
    private int MAX_ITERATIONS = 200;

    private double inertia;
    private ParticleSwarm swarm;
    private double temp;
    private double convergencePercentage;

    /**
     * Instantiates a new opt engine using as timer the provided one. The
     * provided timer should already be started
     *
     * @param handler : the constraint handler
     * @throws DatabaseConnectionFailureExteption
     */
    public OptimizationEngineDPSO(ConstraintHandler handler,
                                  boolean batch,
                                  StopWatch timer) throws DatabaseConnectionFailureExteption {
        super(handler, batch, timer);
    }

    public int getIteration() {
        return iteration;
    }

    public double getInertia() {
        return inertia;
    }

    public double getTemp() {
        return temp;
    }

    public StopWatch getTimer() {
        return timer;
    }

    public int getMaxIterations() {
        return MAX_ITERATIONS;
    }

    /**
     * This is the bulk of the optimization process in case of a single provider
     * problem.
     *
     * @return the integer -1 an error has happened.
     * @throws OptimizationException
     * @throws ConstraintEvaluationException
     * @throws IOException
     */
    public Integer optimize() throws OptimizationException, ConstraintEvaluationException, IOException {

        bestSolutionSerieHandler = "Best Solution";
        localBestSolutionSerieHandler = "Local Best Solution";

        // 1: check if an initial solution has been set
        if (this.initialSolution == null) return -1;

        logger.info("starting the optimization");

        // start the timer
        if (!providedTimer) timer.start();

        try {
            if(!initialSolution.isEvaluated()) evalServer.EvaluateSolution(initialSolution);
            if(!initialSolution.isFeasible()) makeFeasible(initialSolution);
            bestSolution = initialSolution.clone();
        } catch (EvaluationException e) {
            throw new OptimizationException("", "initialEvaluation", e);
        } // evaluate the current

        try {
			swarm = ParticleSwarm.createRandomFeasibleSwarm(this);
		} catch (EvaluationException e1) {
			throw new OptimizationException("", "createRandomFeasibleSwarm", e1);
		} // all the elements of the swarm are evaluated in creation
        if (swarm.isBestParticleUpdated())
            updateBestSolution(swarm.getSwarmBestParticle().getPosition()); //update best solution and charts


        // temperature, inertia and iteration define the evolution of the swarm
        temp = setInitialTemperature();
        inertia = INITIAL_INERTIA;
        iteration = 1;


        //if the total number of iteration has not been reached and so teh max coverage percentage
        while (!isMaxNumberOfIterations() && !isMaxConvergencePercentage()) {

            logger.info("PSO Iteration: " + iteration);
            logger.info("PSO Convergence: " + convergencePercentage);
            logger.info("PSO temperature: " + temp);
            logger.info("PSO inertia: " + inertia);
            try {
                logger.info("PSO avg distance: " + swarm.getAverageDistance());
            } catch (Exception e) {
                e.printStackTrace();
                throw new OptimizationException("Problem with the calculation fo the average distance in the swarm");
            }
            // logger.trace( currentSolution.showStatus());


            if (Configuration.isPaused()) waitForResume();

            //1. swarm evolution
            if (!swarm.checkRamConstraints()) {
                logger.debug("Ram constraints not satisfied");
            }

            try {
                swarm.evolve(inertia, temp, iteration); // the evolution depends on the temperature, inertia and iteration
                if (!swarm.checkRamConstraints()) {
                    logger.debug("Ram constraints not satisfied");
                }
                if (swarm.isBestParticleUpdated())
                    updateBestSolution(swarm.getSwarmBestParticle().getPosition()); //update best solution and charts

            } catch (EvaluationException e) {
                throw new OptimizationException(e.getMessage());
            }

            updateLocalBestSolution(swarm.getSwarmBestParticle().getPosition());

            if (Configuration.isPaused()) waitForResume();

            setProgress((int) (Math.ceil((double) iteration / MAX_ITERATIONS * 100)));

            // increment the number of iterations, inertia and temperature
            updateInertiaWeight();
            updateTemperature();
            updateIteration();

            firePropertyChange("iteration", iteration - 1, iteration);

        }


        try {
            costLogImage.save2png();
            logVm.save2png();
            logConstraints.save2png();
        } catch (IOException e) {
            logger.error("Unable to create charts", e);
        }

        // exportSolution(); TODO: this is always called twice, because
        // Space4Cloud calls it at the end of the process! Disabled here.
        evalServer.showStatistics();

        // if (!batch)
        // SolutionWindow.show(bestSolution);

        exportBestSolutionsTrace();

        return -1;

    }


    private void updateIteration() {
        iteration += 1;
    }

    /**
     * this function set the initial temperature for the simulated annealing.
     * I found this approach somewhere, it does need parameter tweaking
     *
     * @return
     */
    private double setInitialTemperature() {
        double delta = Math.abs(swarm.getWorstFitness() - swarm.getBestFitness());
        return delta * ((double) SWARM_SIZE / 2);
    }

    private void updateTemperature() {
        temp = temp * CR;
    }


    private boolean isMaxConvergencePercentage() {
        this.convergencePercentage = swarm.getConvergencePercentage();
        return convergencePercentage > MAX_CONVERGENCE_PERCENTAGE;

    }

    private boolean isMaxNumberOfIterations() {
        return iteration >= MAX_ITERATIONS;
    }

    private void updateInertiaWeight() {
        double wMax = 0.9;
        double wMin = 0.4;
        this.inertia = wMax - (wMax - wMin) * ((double) iteration / MAX_ITERATIONS);
    }





}
