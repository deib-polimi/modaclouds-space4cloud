package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import it.polimi.modaclouds.space4cloud.chart.GenericChart;
import it.polimi.modaclouds.space4cloud.gui.BestSolutionExplorer;
import it.polimi.modaclouds.space4cloud.optimization.OptimizationEngine;
import org.jfree.data.xy.XYSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ParticleSwarm implements Cloneable, Serializable, Iterable<Particle> {

    private Logger logger = LoggerFactory.getLogger(ParticleSwarm.class);

    private Set<Particle> swarm = null;

    private Particle bestParticle = null;

    private List<Particle> bestParticles = new ArrayList<>();
    private GenericChart<XYSeriesCollection> costLogImage;
    private SwingWorker<Void, Void> swingWorker;

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


    public void updateVelocity() {
        for (Particle particle : swarm) particle.updateVelocity(bestParticle);
    }


}
