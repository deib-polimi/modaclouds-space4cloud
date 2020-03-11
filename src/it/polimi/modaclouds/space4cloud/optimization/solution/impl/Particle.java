package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import it.polimi.modaclouds.space4cloud.exceptions.OptimizationException;
import it.polimi.modaclouds.space4cloud.optimization.MoveOnVM;
import it.polimi.modaclouds.space4cloud.optimization.MoveTypeVM;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Particle implements Cloneable, Serializable {

    private long generationTime = 0;
    private int generationIteration = 0;
    private Map<String, Map<String, List<CloudService>>> resMapPerSolutionPerTier;
    private SolutionMulti solutionMulti = null;
    private Particle bestParticle = null;

    private ParticleVelocity velocity = null;

    public Particle(SolutionMulti solutionMulti) {
        this.solutionMulti = solutionMulti;
        this.velocity = new ParticleVelocity(this);
        this.bestParticle = this;
    }

    public boolean isFeasible() {
        return solutionMulti.isFeasible();
    }

    public boolean isEvaluated() {
        return solutionMulti.isEvaluated();
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


    public boolean greaterThan(Particle other) {
        if (other == null) return true;
        if (solutionMulti == null) return false;
        return solutionMulti.greaterThan(other.getSolutionMulti());

    }

    public Particle clone() {
        Particle clonedParticle = new Particle(solutionMulti.clone());
        clonedParticle.bestParticle = bestParticle;
        clonedParticle.setGenerationIteration(generationIteration);
        clonedParticle.setGenerationTime(generationTime);
        clonedParticle.velocity = this.velocity.clone();
        clonedParticle.velocity.setParticle(clonedParticle);
        return clonedParticle;
    }

    /**
     * The distance is calculate as the sum of the distances
     * taking into account the resource type and the number of replicas per tier
     *
     * @param otherParticle
     */
    public int distance(Particle otherParticle) throws OptimizationException {

        int dist = 0;
        for (Solution sol : solutionMulti.getAll()) {
            Instance application = sol.getApplication(0);

            Instance otherApplication = otherParticle.getSolutionMulti().get(sol.getProvider()).getApplication(0);

            for (Tier tier : application.getTiers()) {
                Tier otherTier = otherApplication.getTierById(tier.getId());
                if (otherTier == null)
                    throw new OptimizationException("Error trying to calculate the distance between two particles");
                List<CloudService> resList = resMapPerSolutionPerTier.get(sol.getProvider()).get(tier.getId());
                int pos = resList.indexOf(tier.getCloudService());
                int otherPos = resList.indexOf(otherTier.getCloudService());
                if (pos == -1 || otherPos == -1)
                    throw new OptimizationException("Error trying to calculate the distance between two particles: resource not found");
                dist += Math.abs(pos - otherPos);
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

    /**
     * this method updates the particle's velocity according to the pso main formula
     * v_t+1 = c1 * v_t + c2 * (p_it - x_t) + c3 * (p_gt -x-t)
     * p_it is the best position seen by the particle
     * p_gt is tbe best global position
     *
     * @param pg
     */
    public void updateVelocity(Particle pg, Double c1, Double c2, Double c3) throws OptimizationException {

        Particle pi = bestParticle;
        this.velocity = this.velocity.scalarMultiplication(c1)
                .sum(pi.difference(this)
                        .scalarMultiplication(c2))
                .sum(pg.difference(this)
                        .scalarMultiplication(c3));


    }

    public void updatePosition() {

        for (Solution sol : solutionMulti.getAll()) {
            MoveTypeVM moveVM = new MoveTypeVM(sol);
            Instance application = sol.getApplication(0);
            String provider = sol.getProvider();

            for (Tier tier : application.getTiers()) {
                List<CloudService> resList = resMapPerSolutionPerTier.get(provider).get(tier.getId());
                int initialPos = resList.indexOf(tier.getCloudService());
                double delta = velocity.getVelocityTierComponent(provider, tier.getId());

                int finalPos = (int) Math.floor(initialPos + delta);

                if (finalPos < 0) finalPos = 0;
                if (resList.size() <= finalPos) finalPos = resList.size() - 1;
                CloudService newCloudService = resList.get(finalPos);
                //this should change the type of resource into all instances contained into the solution //todo: check
                moveVM.changeMachine(tier.getId(), newCloudService);
            }


            for (int i = 0; i < 24; i++) {
                MoveOnVM moveOnVM = new MoveOnVM(sol, i);
                for (Tier tier : sol.getApplication(i).getTiers()) {
                    double deltaReplica = velocity.getVelocityReplicaComponent(provider, tier.getId(), i);
                    moveOnVM.scaleDelta(tier, (int) Math.floor(deltaReplica));
                }
            }


        }

    }

    public ParticleVelocity difference(Particle p1, Particle p2) throws OptimizationException {
        /**
         * difference between p1 and p2 (p1 - p2)
         * it is by all means a velocity
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
        return particleDiff;

    }

    public ParticleVelocity difference(Particle otherParticle) throws OptimizationException {
        /**
         * difference between p1 and p2 (p1 - p2)
         * it is by all means a velocity
         */
        ParticleVelocity particleDiff = new ParticleVelocity(this.clone());

        for (Solution sol : this.getSolutionMulti().getAll()) {
            Instance application = sol.getApplication(0);

            Instance otherApplication = otherParticle.getSolutionMulti().get(sol.getProvider()).getApplication(0);

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
                Instance otherAppl = otherParticle.getSolutionMulti().get(sol.getProvider()).getApplication(i);
                for (Tier tier : appl.getTiers()) {
                    Tier otherTier = otherAppl.getTierById(tier.getId());
                    int r1 = tier.getCloudService().getReplicas();
                    int r2 = otherTier.getCloudService().getReplicas();
                    particleDiff.updateVelocityReplicaComponent(sol.getProvider(), tier.getId(), i, (double) (r1 - r2));
                }
            }


        }
        return particleDiff;

    }


}
