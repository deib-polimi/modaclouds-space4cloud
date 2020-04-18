package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import it.polimi.modaclouds.space4cloud.exceptions.OptimizationException;
import it.polimi.modaclouds.space4cloud.optimization.MoveOnVM;
import it.polimi.modaclouds.space4cloud.optimization.MoveTypeVM;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Particle implements Cloneable, Serializable, Comparable<Particle> {

	
	protected static Logger logger = LoggerFactory.getLogger(Particle.class);
	
    private long generationTime = 0;
    private int generationIteration = 0;
    private Map<String, Map<String, List<CloudService>>> resMapPerSolutionPerTier;
    private SolutionMulti position;
    private Particle bestAntecedent;
    private ParticleVelocity velocity;

    public Particle(SolutionMulti position) {
        this.position = position;
        this.velocity = new ParticleVelocity(this);
        this.bestAntecedent = this;
    }

    public Particle getBestAntecedent() {
        return bestAntecedent;
    }

    public boolean isFeasible() {
        return position.isFeasible();
    }

    public boolean isEvaluated() {
        return position.isEvaluated();
    }


    public double getFitness() {
        return position.getCost();
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

    public SolutionMulti getPosition() {
        return position;
    }



    public boolean betterThan(Particle other) {
        if (other == null) return true;
        if (position == null) return false;
        return position.greaterThan(other.getPosition());

    }

    public Particle clone() {
        Particle clonedParticle = new Particle(position.clone());
        clonedParticle.bestAntecedent = bestAntecedent; // no clone
        clonedParticle.setGenerationIteration(generationIteration);
        clonedParticle.setGenerationTime(generationTime);
        clonedParticle.setCloudResourceMap(resMapPerSolutionPerTier);
        clonedParticle.velocity = this.velocity.clone();
        return clonedParticle;
    }

    /**
     * The distance is calculate as the sum of the distances
     * taking into account the resource type and the number of replicas per tier
     *
     * @param otherParticle
     */
    public int distance(Particle otherParticle) throws Exception {

        int dist = 0;
        for (Solution sol : position.getAll()) {
            Instance application = sol.getApplication(0);

            Instance otherApplication = otherParticle.getPosition().get(sol.getProvider()).getApplication(0);

            for (Tier tier : application.getTiers()) {
                Tier otherTier = otherApplication.getTierById(tier.getId());
                if (otherTier == null)
                    throw new Exception("Error trying to calculate the distance between two particles");
                List<CloudService> resList = resMapPerSolutionPerTier.get(sol.getProvider()).get(tier.getId());
                int pos = resList.indexOf(tier.getCloudService());
                int otherPos = resList.indexOf(otherTier.getCloudService());
                if (pos == -1 || otherPos == -1)
                    throw new Exception("Error trying to calculate the distance between two particles: resource not found");
                dist += Math.abs(pos - otherPos);
            }

            for (int i = 0; i < 24; i++) {
                Instance appl = sol.getApplication(i);
                Instance otherAppl = otherParticle.getPosition().get(sol.getProvider()).getApplication(i);
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

        Particle pi = bestAntecedent;
        this.velocity = this.velocity
                .scalarMultiplication(c1)
                .sum(this.difference(pi)
                        .scalarMultiplication(c2))
                .sum(this.difference(pg)
                        .scalarMultiplication(c3));


    }

    /**
     * this method updates the particle's position based on its velocity
     * remember that the velocity has two components. Both are used to update the position
     */
    public void updatePosition() {

        for (Solution sol : position.getAll()) {
            MoveTypeVM moveVM = new MoveTypeVM(sol);
            Instance application = sol.getApplication(0);
            String provider = sol.getProvider();

            // component tier's resource
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


            //component tier's replica
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

        for (Solution sol : p1.getPosition().getAll()) {
            Instance application = sol.getApplication(0);

            Instance otherApplication = p2.getPosition().get(sol.getProvider()).getApplication(0);

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
                Instance otherAppl = p2.getPosition().get(sol.getProvider()).getApplication(i);
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

        for (Solution sol : this.getPosition().getAll()) {
            Instance application = sol.getApplication(0);

            Instance otherApplication = otherParticle.getPosition().get(sol.getProvider()).getApplication(0);

            for (Tier tier : application.getTiers()) {
                Tier otherTier = otherApplication.getTierById(tier.getId());
                if (otherTier == null)
                    throw new OptimizationException("Error trying to calculate the velocity between two particles");
                
                List<CloudService> resList;
                try{
                resList = resMapPerSolutionPerTier.get(sol.getProvider()).get(tier.getId());
                }
                catch(Exception e){
                	logger.debug("error that should not be here 1");
                	 throw new OptimizationException("Error trying to calculate the velocity between two particles");
                }
                
                if(resList == null){
                	logger.debug("resList is null");
                	throw new OptimizationException("reslist is null");
                }
                
                try {
                int pos1 = resList.indexOf(tier.getCloudService());
                int pos2 = resList.indexOf(otherTier.getCloudService());
                double delta = 0;
                if (pos1 == -1 || pos2 == -1){
                	 if(pos1 == -1 && pos2 !=-1 )delta = 0;
                	 if (pos1 != -1 && pos2 == -1 )delta = pos1 == -1 ? -pos2: pos1; 
                	 else delta = 0;
                }
                else delta = (pos1 - pos2);
                	
                particleDiff.updateVelocityTierComponent(sol.getProvider(), tier.getId(), delta );
                }
                catch(Exception e){
                	logger.debug("error that should not be here 1");
                	
                }
            }

            for (int i = 0; i < 24; i++) {
            	
            	try {
                Instance appl = sol.getApplication(i);
                Instance otherAppl = otherParticle.getPosition().get(sol.getProvider()).getApplication(i);
                for (Tier tier : appl.getTiers()) {
                    Tier otherTier = otherAppl.getTierById(tier.getId());
                    int r1 = tier.getCloudService().getReplicas();
                    int r2 = otherTier.getCloudService().getReplicas();
                    particleDiff.updateVelocityReplicaComponent(sol.getProvider(), tier.getId(), i, (double) (r1 - r2));
                }
                }
                catch(Exception e){
                	logger.debug("error that should not be here 2");
                	
                }
            }


        }
        return particleDiff;

    }


    public void updateBestAntecedent() {
        if (this.getFitness() <= bestAntecedent.getFitness()) bestAntecedent = this;
    }


    @Override
    public int compareTo(Particle o) {

        if (!this.isFeasible() && o.isFeasible()) {
            return -1; //o is better
        } else if (this.isFeasible() && !o.isFeasible()) {
            return 1; //this is better
        } else { //xnor
            if (Math.abs(this.getFitness() - o.getFitness()) < 0.001) return 0; // equivalent
            else if (this.getFitness() > o.getFitness()) return 1; // this is greater
            else return -1; //o  is greater
        }
    }
}
