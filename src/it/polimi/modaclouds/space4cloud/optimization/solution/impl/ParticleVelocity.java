package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import java.util.*;

/**
 * Class that represent the velocity of a certain particle
 */
public class ParticleVelocity {
/**
 * the velocity of a particle is defined a number in R for each provider, tier and hour of the day
 * the number means if the associated element (vm type and replica has to increase or decrease.
 *
 */
    private Particle particle;

    private Map<String, Map<String, Double>> tierComponent = new HashMap<>();
    private Map<String, Map<String, List<Double>>> hourComponent = new HashMap<>();

    public ParticleVelocity(Particle particle) {

        this.particle = particle;

        for ( Solution sol: particle.getSolutionMulti().getAll()) {
            String provider = sol.getProvider();
            Map<String, Double> tierVelocity = new HashMap<>();
            for (Tier tier : sol.getApplication(0).getTiers()) {
                tierVelocity.put(tier.getId(), 0.0);
            }
            tierComponent.put(provider, tierVelocity);

            Map<String, List<Double>> hourVelocity = new HashMap<>();
            for (Tier tier : sol.getApplication(0).getTiers()){
                List<Double> velocityList = new ArrayList<>(24);
                for (int i = 0; i < 24; i++) {
                    velocityList.add(0.0);
                }
                hourVelocity.put(tier.getId(), velocityList);
            }
            hourComponent.put(provider, hourVelocity);
        }
    }


    public void randomize(Random random) {
        for (String provider : tierComponent.keySet()) {
            Map<String, Double> mapTiers = tierComponent.get(provider);
            for (String tierID : mapTiers.keySet()) mapTiers.put(tierID, random.nextDouble() * 2.0 - 1);
        }

        for (String provider : hourComponent.keySet()) {
            Map<String, List<Double>> mapTiers = hourComponent.get(provider);
            for (String tierID : mapTiers.keySet()) {
                List<Double> replicaList = mapTiers.get(tierID);
                for (int i = 0; i < 24; i++) {
                    replicaList.add(i, random.nextDouble() * 2.0 - 1);
                }

            }
        }
    }

    public void updateVelocityTierComponent(String provider, String tierID, Double value) {
        tierComponent.get(provider).put(tierID, value);
    }

    public void updateVelocityReplicaComponent(String provider, String tierID, int hour, Double value) {
        List<Double> velocityValues = hourComponent.get(provider).get(tierID);
        velocityValues.add(hour, value);
    }

}
