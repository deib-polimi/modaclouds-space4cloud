package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Map<String, Map<String, List<Double>>> HourComponent = new HashMap<>();

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
            HourComponent.put(provider, hourVelocity);
        }
    }

}
