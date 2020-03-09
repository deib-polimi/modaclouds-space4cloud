package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import it.polimi.modaclouds.space4cloud.exceptions.OptimizationException;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Particle implements Cloneable, Serializable {

    private long generationTime = 0;
    private int generationIteration = 0;
    private Map<String, Map<String, List<CloudService>>> resMapPerSolutionPerTier;
    private SolutionMulti solutionMulti = null;
    private SolutionMulti bestSolution = null;

    private ParticleVelocity velocity = null;

    public Particle(SolutionMulti solutionMulti) {
        this.solutionMulti = solutionMulti;
        this.bestSolution = solutionMulti.clone();
        this.velocity = new ParticleVelocity(this);

    }

    public long getGenerationTime() {
        return generationTime;
    }

    public void setGenerationTime(long generationTime) {
        this.generationTime = generationTime;
    }

    public int getGenerationIteration() {
        return generationIteration;
    }

    public void setGenerationIteration(int generationIteration) {
        this.generationIteration = generationIteration;
    }

    public SolutionMulti getSolutionMulti() {
        return solutionMulti;
    }

    public void setSolutionMulti(SolutionMulti solutionMulti) {
        this.solutionMulti = solutionMulti;
    }

    public SolutionMulti getBestSolution() {
        return bestSolution;
    }

    public void setBestSolution(SolutionMulti bestSolution) {
        this.bestSolution = bestSolution;
    }

    public boolean greaterThan(Particle other) {
        if (other == null) return true;
        if (solutionMulti == null) return false;
        return  solutionMulti.greaterThan(other.getSolutionMulti());

    }

    public Particle clone(){
        Particle clonedParticle = new Particle(solutionMulti.clone());
        clonedParticle.setGenerationIteration(generationIteration);
        clonedParticle.setGenerationTime(generationTime);
        return clonedParticle;
    }

    /**
     * The distance is calculate as the sum of the distances
     * taking into account the resource type and the number of replicas per tier
     * @param otherParticle
     */
    public int distance(Particle otherParticle) throws OptimizationException {

        int dist = 0;
        for ( Solution sol : solutionMulti.getAll()) {
            Instance application = sol.getApplication(0);

            Instance otherApplication = otherParticle.getSolutionMulti().get(sol.getProvider()).getApplication(0);

            for (Tier tier : application.getTiers()) {
                Tier otherTier = otherApplication.getTierById(tier.getId());
                if (otherTier == null)
                    throw new OptimizationException("Error trying to calculate the distance between two particles");
                List<CloudService> resList = resMapPerSolutionPerTier.get(sol.getProvider()).get(tier.getId());
                int pos = resList.indexOf(tier.getCloudService());
                int otherPos = resList.indexOf(otherTier.getCloudService());
                if ( pos == -1 || otherPos == -1)
                   throw new OptimizationException("Error trying to calculate the distance between two particles: resource not found");
                dist+=Math.abs(pos-otherPos);
            }

            for (int i = 0; i < 24; i++) {
                Instance appl = sol.getApplication(i);
                Instance otherAppl = otherParticle.getSolutionMulti().get(sol.getProvider()).getApplication(i);
                for (Tier tier : appl.getTiers()) {
                    Tier otherTier = otherAppl.getTierById(tier.getId());
                    dist += Math.abs(tier.getCloudService().getReplicas() - otherTier.getCloudService().getReplicas());
                }
            }


        }
        return dist;
    }


    public void setCloudResourceMap(Map<String, Map<String, List<CloudService>>> resMapPerSolutionPerTier) {
        this.resMapPerSolutionPerTier = resMapPerSolutionPerTier;

    }

    public void randomizeVelocity(Random random) {
        if (velocity != null) velocity.randomize(random);
    }

    public void updateVelocity(Particle bestParticle) {

    }

    public ParticleVelocity difference(Particle p1, Particle p2) throws OptimizationException {
        /**
         * velocity between p1 and p2 (p1 - p2)
         *
         */
        ParticleVelocity particleDiff = new ParticleVelocity(p1.clone());

        for (Solution sol : p1.getSolutionMulti().getAll()) {
            Instance application = sol.getApplication(0);

            Instance otherApplication = p2.getSolutionMulti().get(sol.getProvider()).getApplication(0);

            for (Tier tier : application.getTiers()) {
                Tier otherTier = otherApplication.getTierById(tier.getId());
                if (otherTier == null)
                    throw new OptimizationException("Error trying to calculate the velocity between two particles");
                List<CloudService> resList = resMapPerSolutionPerTier.get(sol.getProvider()).get(tier.getId());
                int pos1 = resList.indexOf(tier.getCloudService());
                int pos2 = resList.indexOf(otherTier.getCloudService());
                if (pos1 == -1 || pos2 == -1)
                    throw new OptimizationException("Error trying to calculate the distance between two particles: resource not found");
                particleDiff.updateVelocityTierComponent(sol.getProvider(), tier.getId(), (double) (pos1 - pos2));
            }

            for (int i = 0; i < 24; i++) {
                Instance appl = sol.getApplication(i);
                Instance otherAppl = p2.getSolutionMulti().get(sol.getProvider()).getApplication(i);
                for (Tier tier : appl.getTiers()) {
                    Tier otherTier = otherAppl.getTierById(tier.getId());
                    int r1 = tier.getCloudService().getReplicas();
                    int r2 = otherTier.getCloudService().getReplicas();
                    particleDiff.updateVelocityReplicaComponent(sol.getProvider(), tier.getId(), i, (double) (r1 - r2));
                }
            }


        }


    }


}
