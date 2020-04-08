package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import it.polimi.modaclouds.space4cloud.chart.GenericChart;
import it.polimi.modaclouds.space4cloud.exceptions.EvaluationException;
import it.polimi.modaclouds.space4cloud.exceptions.OptimizationException;
import it.polimi.modaclouds.space4cloud.gui.BestSolutionExplorer;
import it.polimi.modaclouds.space4cloud.optimization.OptimizationEngine;
import it.polimi.modaclouds.space4cloud.optimization.OptimizationEngineDPSO;
import it.polimi.modaclouds.space4cloud.optimization.evaluation.EvaluationServer;
import org.apache.commons.lang.time.StopWatch;
import org.jfree.data.xy.XYSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ParticleSwarm implements Cloneable, Serializable, Iterable<Particle> {

    private Logger logger = LoggerFactory.getLogger(ParticleSwarm.class);

    private Set<Particle> swarm = null;

    private OptimizationEngineDPSO engine;

    private Particle bestParticle = null;

    private List<Particle> bestParticles = new ArrayList<>();
    private GenericChart<XYSeriesCollection> costLogImage;
    private double socialScale = 1.0
    private Random rando
    private StopWatch timer;
    private EvaluationServer evaluationServer;
    private double cognitiveScale = 1.0;
    private double temp;
    Random random;
    private double v;
    private Double inertia;
    private int iteration;

    public ParticleSwarm(Set<Particle> swarm, OptimizationEngineDPSO engine) {
        this.swarm = swarm;
        this.engine = engine;
        this.random = engine.getRandom();
        this.costLogImage = engine.getCostLogger();
        this.cognitiveScale = OptimizationEngineDPSO.getCognitiveScale();
        this.socialScale = OptimizationEngineDPSO.getSocialScale();
        this.timer = engine.getTimer();
        this.evaluationServer = engine.getEvalServer();
        updateBestParticle();

    }



    public Particle getBestParticle() {
        return bestParticle;
    }


    @Override
    public Iterator<Particle> iterator() {
        return swarm.iterator();
    }


    public double getWorstFitness() {
        return Collections.max(swarm).getFitness();
    }

    public double getBestFitness() {
        return bestParticle.getFitness();
    }

    private void updateBestParticle() {
        bestParticle = null;
        for (Particle p : swarm) updateBestParticle(p);
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
        updatePositionAndEvaluate();
        updateBestParticle();

    }

    public double getConvergencePercentage() {
        List<PairFitness> fitnessList = new ArrayList<>(swarm.size());
        for (Particle p : swarm) fitnessList.add(new PairFitness(1, p.getPosition().getCost());

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

        return (double) maxEquals / swarm.size();
    }

    /**
     * @param particle
     */
    private void updateBestParticle(Particle particle) {
        if (particle.greaterThan(bestParticle)) {
            bestParticle = particle.clone();

            updateCostLogImage(bestParticle, "Best Solution");
            engine.firePropertyChange(OptimizationEngine.BEST_SOLUTION_UPDATED, false, true);

            // Add to the list of best solutions for logging

            bestParticles.add(bestParticle.clone());
            engine.firePropertyChange(BestSolutionExplorer.PROPERTY_ADDED_VALUE, false, true);
        }
        logger.info("updated best solution");
    }

    private void updateCostLogImage(Particle particle, String seriesHandler) {
        costLogImage.add(seriesHandler,
                TimeUnit.MILLISECONDS.toSeconds(particle.getPosition().getGenerationTime()),
                particle.getPosition().getCost());
    }

    /**
     * this method updates the velocity of every single particle in the swarm
     *
     * @throws OptimizationException
     */
    private void updateVelocity() throws OptimizationException {
        for (Particle particle : swarm) particle.updateVelocity(bestParticle, inertia, cognitiveScale, socialScale);

    }

    private void updatePositionAndEvaluate() throws EvaluationException, OptimizationException {
        for (Particle particle : swarm) {
            Particle newParticle = particle.clone();
            newParticle.updatePosition();
            evaluationServer.EvaluateSolution(newParticle.getPosition());

            if (!newParticle.isFeasible()) engine.makeFeasible(newParticle.getPosition());

            if (acceptMetropolis(newParticle)) {
                swarm.remove(particle);
                swarm.add(newParticle);
                newParticle.updateLocalBest();
                timer.split();
                long time = timer.getSplitTime();
                particle.setGenerationTime(time);
                particle.setGenerationIteration(iteration);
            }
        }
    }

    /**
     * @param newParticle
     * @return
     */
    private boolean acceptMetropolis(Particle newParticle) {
        double delta = newParticle.getFitness() - bestParticle.getFitness();
        return delta <= 0 || random.nextDouble() <= Math.exp(-delta / this.temp);
    }

    private int getSwarmSize() {
        return swarm.size();
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
