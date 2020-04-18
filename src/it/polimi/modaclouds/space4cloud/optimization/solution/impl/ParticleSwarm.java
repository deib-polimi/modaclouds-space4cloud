package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import it.polimi.modaclouds.space4cloud.exceptions.ConstraintEvaluationException;
import it.polimi.modaclouds.space4cloud.exceptions.EvaluationException;
import it.polimi.modaclouds.space4cloud.exceptions.OptimizationException;
import it.polimi.modaclouds.space4cloud.optimization.OptimizationEngineDPSO;

import it.polimi.modaclouds.space4cloud.optimization.constraints.Constraint;
import it.polimi.modaclouds.space4cloud.optimization.constraints.RamConstraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ParticleSwarm implements Cloneable, Serializable, Iterable<Particle> {

    private final OptimizationEngineDPSO engine;
    private Logger logger = LoggerFactory.getLogger(ParticleSwarm.class);

    private final Double cognitiveScale;

    private Particle swarmBestParticle = null;

    private double temp;
    private Double inertia;
    private int iteration;
    private final Double socialScale;
    private List<Particle> particleSet;
    private boolean bestParticleUpdated;

    private ParticleSwarm(List<Particle> particleSet, OptimizationEngineDPSO engine) {
        this.particleSet = particleSet;
        this.engine = engine;
        this.cognitiveScale = OptimizationEngineDPSO.COGNITIVE_SCALE;
        this.socialScale = OptimizationEngineDPSO.SOCIAL_SCALE;
        updateBestParticle();

    }

    /**
     * Create a quasi-random swarm of feasible solutions. Every solution is checked for feasibility and
     * in case it is not feasible it is made so using the makefeasible function
     *
     * @return ParticleSwarm
     * @throws OptimizationException
     * @throws EvaluationException 
     */
    public static ParticleSwarm createRandomFeasibleSwarm(OptimizationEngineDPSO engine) throws OptimizationException, ConstraintEvaluationException, EvaluationException {

        ParticleFactory factory = new ParticleFactory(engine); //engine is important because it has the makefeasible method

        ArrayList<Particle> swarm = new ArrayList<>(OptimizationEngineDPSO.SWARM_SIZE);
        for (int i = 0; i < OptimizationEngineDPSO.SWARM_SIZE - 1; i++)
            swarm.add(factory.buildRandomFeasibleParticle());

        swarm.add(factory.buildParticle(engine.getInitialSolution()));

        ParticleSwarm particleSwarm = new ParticleSwarm(swarm, engine);

        return particleSwarm;
    }


    public boolean checkRamConstraints() {
        for (Particle p : this.particleSet) {
            for (Solution s : p.getPosition().getAll()) {
                for (Constraint constraint : s.getViolatedConstraints()) {
                    if (constraint instanceof RamConstraint) {
                        return false;
                    }
                }
            }
        }
        return true;
    }


    public boolean isBestParticleUpdated() {
        return bestParticleUpdated;
    }

    public Particle getSwarmBestParticle() {
        return swarmBestParticle;
    }

    @Override
    public Iterator<Particle> iterator() {
        return particleSet.iterator();
    }

    public double getWorstFitness() {
        return Collections.max(particleSet).getFitness();
    }

    public double getBestFitness() {
        return swarmBestParticle.getFitness();
    }

    /**
     * this is the main method of the swarm. given a certain inertia and temperature a the swarm is updated.
     * the iteration parameter is just for monitoring purposes
     *
     * @param inertia
     * @param temp
     * @param iteration
     * @throws OptimizationException
     * @throws EvaluationException
     */
    public void evolve(Double inertia, double temp, int iteration) throws OptimizationException, EvaluationException {
        this.temp = temp;
        this.iteration = iteration;
        this.inertia = inertia;
        updateVelocity();
        updatePositionEvaluateAccept();
        updateBestParticle();

    }

    /**
     * this method calculate the convergence rate of a certain swarm
     * the convergence rate is the ratio between the maximum number of particles withe the same fitness and the total
     * number of particles in the swarm
     *
     * @return
     */
    public double getConvergencePercentage() {
        List<PairFitness> fitnessList = new ArrayList<>(particleSet.size());
        for (Particle p : particleSet) fitnessList.add(new PairFitness(1, p.getPosition().getCost()));

        Collections.sort(fitnessList);
        int i = 0;
        int j = 1;
        while (i < fitnessList.size() - 1) {
            while (j < fitnessList.size()) {
                PairFitness pi = fitnessList.get(i);
                PairFitness pj = fitnessList.get(j);
                if (pi.compareTo(pj) == 0) {
                    fitnessList.remove(pj);
                    pi.num++;
                } else {
                    i++;
                    j++;
                }
            }
        }
        int maxEquals = 0;
        for (PairFitness p : fitnessList) if (p.num > maxEquals) maxEquals = p.num;

        return (double) maxEquals / particleSet.size();
    }

    private void updateBestParticle() {

        boolean res = false;
        for (Particle p : particleSet) {
            res = res | updateBestParticle(p);
        }
        this.bestParticleUpdated = res;
    }

    /**
     * @param particle
     */
    private boolean updateBestParticle(Particle particle) {
        boolean res = false;
        if (particle.betterThan(swarmBestParticle)) {
            swarmBestParticle = particle;
            res = true;
//
//            updateCostLogImage(bestParticle, "Best Solution");
//            engine.firePropertyChange(OptimizationEngine.BEST_SOLUTION_UPDATED, false, true);

            // Add to the list of best solutions for logging

//            bestParticles.add(bestParticle.clone());
//            engine.firePropertyChange(BestSolutionExplorer.PROPERTY_ADDED_VALUE, false, true);
        }
        logger.info("updated best solution within the swarm");
        return res;
    }

    private void updateCostLogImage(Particle particle, String seriesHandler) {
        engine.getCostLogger().add(seriesHandler,
                TimeUnit.MILLISECONDS.toSeconds(particle.getPosition().getGenerationTime()),
                particle.getPosition().getCost());
    }

    /**
     * this method updates the velocity of every single particle in the swarm
     *
     * @throws OptimizationException
     */
    private void updateVelocity() throws OptimizationException {
        for (Particle particle : particleSet)
            particle.updateVelocity(swarmBestParticle, inertia, cognitiveScale, socialScale);

    }

    private void updatePositionEvaluateAccept() throws EvaluationException, OptimizationException {
        
    	List<Particle> particleToRemove = new ArrayList<>();
    	List<Particle> particleToAdd = new ArrayList<>();
    	for (Particle oldParticle : particleSet) {
            Particle newParticle = oldParticle.clone();
            newParticle.updatePosition();
            engine.getEvalServer().EvaluateSolution(newParticle.getPosition());

            if (!newParticle.isFeasible()) engine.makeFeasible(newParticle.getPosition());

            if (acceptMetropolisLocal(oldParticle, newParticle)) {
            	particleToRemove.add(oldParticle);
            	particleToAdd.add(newParticle);
                newParticle.updateBestAntecedent();
                engine.getTimer().split();
                long time = engine.getTimer().getSplitTime();
                oldParticle.setGenerationTime(time);
                oldParticle.setGenerationIteration(iteration);
            }
        }
    	particleSet.removeAll(particleToRemove);
    	particleSet.addAll(particleToAdd);
    }

    /**
     * @param newParticle
     * @return
     */
    private boolean acceptMetropolisGlobal(Particle newParticle) {

        boolean res = newParticle.betterThan(swarmBestParticle);
        if (res) return true;
        else {
            double delta = newParticle.getFitness() - swarmBestParticle.getFitness();
            return delta <= 0 || engine.getRandom().nextDouble() <= Math.exp(-delta / this.temp);
        }
    }

    private boolean acceptMetropolisLocal(Particle oldParticle, Particle newParticle) {

        boolean res = newParticle.betterThan(oldParticle);
        if (res) return true;
        else {
            double delta = newParticle.getFitness() - oldParticle.getFitness();
            return delta <= 0 || engine.getRandom().nextDouble() <= Math.exp(-delta / this.temp);
        }
    }


    private int getSwarmSize() {
        return particleSet.size();
    }


}

class PairFitness implements Comparable<PairFitness> {
    final double THRESHOLD = .0001;
    int num = 1;
    Double fitness;

    public PairFitness(int num, Double fitness) {
        this.num = num;
        this.fitness = fitness;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    @Override
    public int compareTo(PairFitness o) {
        if (Math.abs(this.fitness - o.fitness) < THRESHOLD) return 0;
        else if (this.fitness > o.fitness) return 1;
        else return -1;
    }
}
