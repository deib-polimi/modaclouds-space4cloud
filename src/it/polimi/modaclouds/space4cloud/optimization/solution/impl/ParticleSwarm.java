package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import it.polimi.modaclouds.space4cloud.chart.GenericChart;
import it.polimi.modaclouds.space4cloud.exceptions.EvaluationException;
import it.polimi.modaclouds.space4cloud.exceptions.OptimizationException;
import it.polimi.modaclouds.space4cloud.gui.BestSolutionExplorer;
import it.polimi.modaclouds.space4cloud.optimization.OptimizationEngine;
import it.polimi.modaclouds.space4cloud.optimization.evaluation.EvaluationServer;
import org.apache.commons.lang.time.StopWatch;
import org.jfree.data.xy.XYSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ParticleSwarm implements Cloneable, Serializable, Iterable<Particle> {

    private Logger logger = LoggerFactory.getLogger(ParticleSwarm.class);

    private Set<Particle> swarm = null;

    private Particle bestParticle = null;

    private List<Particle> bestParticles = new ArrayList<>();
    private GenericChart<XYSeriesCollection> costLogImage;
    private SwingWorker<Void, Void> swingWorker;
    private StopWatch timer;
    private EvaluationServer evaluationServer;
    private double cognitiveScale = 1.0;
    private double socialScale = 1.0

    public void setCognitiveScale(double cognitiveScale) {
        this.cognitiveScale = cognitiveScale;
    }

    public void setSocialScale(double socialScale) {
        this.socialScale = socialScale;
    }

    public ParticleSwarm(Set<Particle> swarm, SwingWorker<Void, Void> swingWorker) {
        this.swarm = swarm;
        this.swingWorker = swingWorker;
        updateBestParticle();

    }

    public void setCostLogImage(GenericChart<XYSeriesCollection> costLogImage) {
        this.costLogImage = costLogImage;
    }

    @Override
    public Iterator<Particle> iterator() {
        return swarm.iterator();
    }

    public void setTimer(StopWatch timer) {
        this.timer = timer;
    }

    public void setEvaluationServer(EvaluationServer evalServer) {
        this.evaluationServer = evalServer;
    }


    private void updateBestParticle() {
        bestParticle = null;
        for (Particle p : swarm) updateBestParticle(p);
    }

    private void updateBestParticle(Particle particle) {
        if (particle.greaterThan(bestParticle)) {
            bestParticle = particle.clone();

            updateCostLogImage(bestParticle, "Best Solution");
            swingWorker.firePropertyChange(OptimizationEngine.BEST_SOLUTION_UPDATED, false, true);

            // Add to the list of best solutions for logging

            bestParticles.add(bestParticle.clone());
            swingWorker.firePropertyChange(BestSolutionExplorer.PROPERTY_ADDED_VALUE, false, true);
        }
        logger.info("updated best solution");
    }


    private void updateCostLogImage(Particle particle, String seriesHandler) {
        costLogImage.add(seriesHandler,
                TimeUnit.MILLISECONDS.toSeconds(particle.getSolutionMulti().getGenerationTime()),
                particle.getSolutionMulti().getCost());
    }


    public void updateSwarm(double v, Double inertia, int iteration) throws OptimizationException, EvaluationException {
        updateVelocity(inertia);
        updatePositionAndEvaluate();
        updateBestParticle();
        //todo evaluate population

    }

    public double getConvergencePercentage() {
        List<PairFitness> fitnesslist = new ArrayList<>(swarm.size());
        for (Particle p : swarm) {
            fitnesslist.add(new PairFitness(1, p.getSolutionMulti().getCost());
        }
        Collections.sort(fitnesslist);
        int i = 0;
        int j = 1;
        while (i < fitnesslist.size() - 1) {
            while (j < fitnesslist.size()) {
                PairFitness pi = fitnesslist.get(i);
                PairFitness pj = fitnesslist.get(j);
                if (pi.compareTo(pj) == 0) {
                    fitnesslist.remove(pj);
                    pi.num++;
                } else {
                    i++;
                    j++;
                }
            }
        }
        int maxEquals = 0;
        for (PairFitness p : fitnesslist) if (p.num > maxEquals) maxEquals = p.num;

        return (double) maxEquals / swarm.size();
    }

    private void updateVelocity(double inertia) throws OptimizationException {
        for (Particle particle : swarm) particle.updateVelocity(bestParticle, inertia, 1.0, 1.0);

    }

    private void updatePositionAndEvaluate() throws EvaluationException {
        for (Particle particle : swarm) {
            Particle newParticle = particle.clone();
            newParticle.updatePosition();
            evaluationServer.EvaluateSolution(newParticle.getSolutionMulti());
            if (acceptMetropolis(newParticle)) {
                //todo update local best particle
                swarm.remove(particle);
                swarm.add(newParticle);
//                particle.setGenerationTime();
//                particle.setGenerationIteration();
            }
        }
    }

    private boolean acceptMetropolis(Particle newParticle) {
        return false;
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
