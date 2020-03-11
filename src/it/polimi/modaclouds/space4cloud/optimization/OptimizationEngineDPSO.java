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

import it.polimi.modaclouds.space4cloud.db.DataHandler;
import it.polimi.modaclouds.space4cloud.db.DatabaseConnectionFailureExteption;
import it.polimi.modaclouds.space4cloud.exceptions.ConstraintEvaluationException;
import it.polimi.modaclouds.space4cloud.exceptions.EvaluationException;
import it.polimi.modaclouds.space4cloud.exceptions.OptimizationException;
import it.polimi.modaclouds.space4cloud.optimization.constraints.ConstraintHandler;
import it.polimi.modaclouds.space4cloud.optimization.evaluation.EvaluationServer;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.*;
import it.polimi.modaclouds.space4cloud.utils.Cache;
import it.polimi.modaclouds.space4cloud.utils.Configuration;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.*;

/**
 * @author Michele Ciavotta
 * Class defining the optimization engine.
 * This class implements a Discrete Particle Swarm Optimization Algorithm
 */
public class OptimizationEngineDPSO extends OptimizationEngine implements PropertyChangeListener {

    public static final String BEST_SOLUTION_UPDATED = "bestSolutionUpdated";
    private static final int SWARM_SIZE = 100;
    private static final double COGNITIVE_SCALE = 2.0;
    private static final double SOCIAL_SCALE = 2.0;
    private SolutionMulti initialSolution = null;
    private SolutionMulti bestSolution = null;
    private SolutionMulti currentSolution = null;
    private List<SolutionMulti> bestSolutions = new ArrayList<SolutionMulti>();
    private ConstraintHandler constraintHandler;
    private DataHandler dataBaseHandler;
    private int iteration;
    private int MAX_ITERATIONS;
    /**
     * This is the long term memory of the tabu search used in the scramble
     * process. Each Tier (key of the Map) has its own memory which uses the
     * resource name as ID. This memory is used to restart the search process
     * after the full exploration of a local optimum by building a solution out
     * of components with low frequency
     */
    private Map<String, Cache<String, Integer>> longTermFrequencyMemory;


    private StopWatch timer = new StopWatch();

    private EvaluationServer evalServer;

    private Logger logger = LoggerFactory.getLogger(OptimizationEngineDPSO.class);

    private boolean batch = false;

    private boolean providedTimer = false;
    /**
     * Indicates if the bestSolution did change or not.
     */
    private boolean bestSolutionUpdated = true;
    private double inertia = 1.0;
    private double wMax = 0.9;
    private double wMin = 0.4;
    private ParticleSwarm swarm;
    private double temp;

    /**
     * Instantiates a new opt engine using as timer the provided one. the
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

        // 1: check if an initial solution has been set
        if (this.initialSolution == null) return -1;

        logger.info("starting the optimization");

        // start the timer
        if (!providedTimer) timer.start();

        try {
            evalServer.EvaluateSolution(initialSolution);
        } catch (EvaluationException e) {
            throw new OptimizationException("", "initialEvaluation", e);
        } // evaluate the current


        swarm = createRandomFeasibleSwarm(); // all the elements of the swarm have been evaluated in creation


        iteration = 1;

        boolean solutionChanged = true;

        while (!isMaxNumberOfIterations() && !isMaxConvergencePercentage()) {

            logger.info("PSO Iteration: " + iteration);
            // logger.trace( currentSolution.showStatus());


            if (Configuration.isPaused()) waitForResume();

            // //////////////////
            if (bestSolutionUpdated && Configuration.REDISTRIBUTE_WORKLOAD) {
                logger.debug("The best solution did change, so let's redistribuite the workload...");
                logger.debug("TBD");
            }
            // //////////////////

            //1. swarm evolution
            updateInertiaWeight();
            swarm.updateSwarm(inertia, temp, iteration);


            // 2: Internal Optimization process
            internalOptimizationScaleIn(currentSolution);

            // 3: check whether the best solution has changed
            // If the current solution is better than the best one it becomes the new best solution.
            logger.info("Updating best solutions");
            updateLocalBestSolution(currentSolution);
            updateBestSolution(currentSolution);

            // 3b: clone the best solution to start the scramble from it
            currentSolution = localBestSolution.clone();

            // 4 Scrambling the current solution: altering the VM type for each provider
            logger.info("Executing: Scramble");

            if (Configuration.isPaused()) waitForResume();

            solutionChanged = tsMoveScramble(currentSolution);

            // if a local optimum with respect to the type of machine has been
            // found we need to perform some diversification (using the long-term memory)
            if (!solutionChanged) {
                logger.info("Stuck in a local optimum, using long term memory");
                currentSolution = longTermMemoryRestart(currentSolution);
                logger.info("Long term memory statistics:");

                for (Tier t : currentSolution.get(0).getApplication(0).getTiers()) {
                    logger.info("\tTier: " + t.getPcmName() + " Size: " + longTermFrequencyMemory.get(t.getId()).size());
                }
                resetLocalBestSolution(currentSolution);
            }

            setProgress((iteration * 100 / MAX_SCRUMBLE_ITERS));

            // increment the number of iterations
            iteration += 1;
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

        if (Configuration.CONTRACTOR_TEST) bestSolution.generateOptimizedCosts();

        exportBestSolutionsTrace();

        return -1;

    }

    private boolean isMaxConvergencePercentage() {
        return swarm.getConvergencePercentage() > 0.95;

    }

    private boolean isMaxNumberOfIterations() {

        return iteration >= MAX_ITERATIONS;
    }

    private void updateInertiaWeight() {
        this.inertia = this.wMax - (this.wMax - this.wMin) * (double) (iteration / MAX_ITERATIONS);
    }


    /**
     * Create a quasi-random swarm of feasible solutions
     *
     * @return
     * @throws OptimizationException
     */
    private ParticleSwarm createRandomFeasibleSwarm() throws OptimizationException {

        Set<Particle> swarm = new HashSet<>(SWARM_SIZE);
        for (int i = 0; i < SWARM_SIZE; i++) swarm.add(createRandomFeasibleParticle());

        ParticleSwarm particleSwarm = new ParticleSwarm(swarm, this);
        particleSwarm.setCostLogImage(this.getCostLogger());
        particleSwarm.setCognitiveScale(COGNITIVE_SCALE);
        particleSwarm.setSocialScale(SOCIAL_SCALE);
        particleSwarm.setTimer(timer);
        particleSwarm.setEvaluationServer(evalServer);
        return particleSwarm;
    }

    /**
     * This method create a solution for the swarm starting from the initial one and altering the resource type and
     * randomly preplicating the tiers. At the end the resulting solution is made feasible
     *
     * @return
     * @throws OptimizationException
     */
    private Particle createRandomFeasibleParticle() throws OptimizationException {

        //todo: pay attention to the single directory where lqn files are saved
        SolutionMulti randomSolution = initialSolution.clone();
        // the new solution has the same providers of the initial one

        //every solution has an application
        //every application has a set of tiers
        //every tier is associated with a vmType but the vmtype is the same for each application

        // Step 1 altering the cloud resource type for each tier of each solution
        Map<String, Map<String, List<CloudService>>> resMapPerSolutionPerTier = new HashMap();
        for (Solution sol : randomSolution.getAll()) {
            MoveTypeVM moveVM = new MoveTypeVM(sol);
            Instance application = sol.getApplication(0);

            Map<String, List<CloudService>> resListMap = new HashMap<>();
            for (Tier tier : application.getTiers()) {
                CloudService originalCloudResource = tier.getCloudService();
                List<CloudService> resList = dataBaseHandler.getSameService(originalCloudResource, sol.getRegion());

                //just in case
                if (!resList.contains(originalCloudResource)) resList.add(originalCloudResource);

                // filter resources according to architectural constraints
                try {
                    constraintHandler.filterResources(resList, tier);
                } catch (ConstraintEvaluationException e) {
                    throw new OptimizationException("Error filtering resources for creating a random solution", e);
                }

                CloudService randomCloudResource = resList.get(random.nextInt(resList.size()));

                //this should change the type of resource into all instances contained into the solution //todo: check
                moveVM.changeMachine(tier.getId(), randomCloudResource);
                resListMap.put(tier.getId(), resList);
            }
            resMapPerSolutionPerTier.put(sol.getProvider(), resListMap);
        }

        // Step 2 altering the number of replicas for each tier and each our
        //for each provider let's calculate the maximum number of replicas per over all tiers

        int maxReplicas = 0;
        for (Solution sol : randomSolution.getAll()) {
            for (int i = 0; i < 24; i++) {
                for (Tier tier : sol.getApplication(i).getTiers()) {
                    maxReplicas = Math.max(maxReplicas, tier.getCloudService().getReplicas());
                }
            }
        }


        for (Solution sol : randomSolution.getAll()) {
            for (int i = 0; i < 24; i++) {
                MoveOnVM moveOnVM = new MoveOnVM(sol, i);
                for (Tier tier : sol.getApplication(i).getTiers()) {
                    moveOnVM.scale(tier, random.nextInt(maxReplicas + 1));
                }
            }
        }

        //step 3 the solutions have to be made feasible

        makeFeasible(randomSolution);

        Particle randomFeasibleParticle = new Particle(randomSolution);
        timer.split();
        long time = timer.getSplitTime();
        randomFeasibleParticle.setGenerationTime(time);
        randomFeasibleParticle.setCloudResourceMap(resMapPerSolutionPerTier);
        randomFeasibleParticle.randomizeVelocity(random);
        return randomFeasibleParticle;
    }


}
