package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import java.util.*;

/**
 * Class that represent the velocity of a certain particle
 */
public class ParticleVelocity implements Cloneable {
    /**
     * the velocity of a particle is defined a number in R for each provider, tier and hour of the day
     * the number means if the associated element (vm type and replica has to increase or decrease.
     */

    private Map<String, Map<String, Double>> tierComponent = new HashMap<>();
    private Map<String, Map<String, List<Double>>> hourComponent = new HashMap<>();

    public ParticleVelocity() {
    }

    public ParticleVelocity(Particle particle) {


        for (Solution sol : particle.getPosition().getAll()) {
            String provider = sol.getProvider();
            Map<String, Double> tierVelocity = new HashMap<>();
            for (Tier tier : sol.getApplication(0).getTiers()) {
                tierVelocity.put(tier.getId(), 0.0);
            }
            tierComponent.put(provider, tierVelocity);

            Map<String, List<Double>> hourVelocity = new HashMap<>();
            for (Tier tier : sol.getApplication(0).getTiers()) {
                List<Double> velocityList = new ArrayList<>(24);
                for (int i = 0; i < 24; i++) {
                    velocityList.add(0.0);
                }
                hourVelocity.put(tier.getId(), velocityList);
            }
            hourComponent.put(provider, hourVelocity);
        }
    }


    public ParticleVelocity clone() {
        ParticleVelocity clonedVelocity = new ParticleVelocity();

        for (String provider : tierComponent.keySet()) {
            Map<String, Double> tierVelocityMap = new HashMap<>();
            Map<String, Double> tierVelocityToClone = tierComponent.get(provider);

            for (String tierID : tierVelocityToClone.keySet()) {
                tierVelocityMap.put(tierID, tierVelocityToClone.get(tierID));
            }
            clonedVelocity.tierComponent.put(provider, tierVelocityMap);

            Map<String, List<Double>> hourVelocity = new HashMap<>();
            Map<String, List<Double>> hourVelocityToClone = hourComponent.get(provider);
            for (String tierID : hourVelocityToClone.keySet()) {
                List<Double> velocityList = new ArrayList<>(hourVelocityToClone.get(tierID));
                hourVelocity.put(tierID, velocityList);
            }
            clonedVelocity.hourComponent.put(provider, hourVelocity);
        }

        return clonedVelocity;
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
                for (int i = 0; i < 24; i++) replicaList.add(i, random.nextDouble() * 2.0 - 1);
            }
        }
    }

    public Double getVelocityTierComponent(String provider, String tierID) {
        return tierComponent.get(provider).get(tierID);
    }

    public Double getVelocityReplicaComponent(String provider, String tierID, int hour) {
        return hourComponent.get(provider).get(tierID).get(hour);
    }

    public void updateVelocityTierComponent(String provider, String tierID, Double value) {
        tierComponent.get(provider).put(tierID, value);
    }

    public void updateVelocityReplicaComponent(String provider, String tierID, int hour, Double value) {
        List<Double> velocityValues = hourComponent.get(provider).get(tierID);
        velocityValues.add(hour, value);
    }

    public ParticleVelocity sum(ParticleVelocity otherVelocity) {
        return sum(otherVelocity, true);
    }

    public ParticleVelocity diff(ParticleVelocity particleVelocity) {
        return sum(particleVelocity, false);
    }

    public ParticleVelocity scalarMultiplication(double c) {

        ParticleVelocity resultVelocity = this.clone();

        for (String provider : resultVelocity.tierComponent.keySet()) {
            Map<String, Double> tierVelocity = tierComponent.get(provider);

            for (String tierID : tierVelocity.keySet()) {

                Double valueToUpdate = c * this.getVelocityTierComponent(provider, tierID);
                resultVelocity.updateVelocityTierComponent(provider, tierID, valueToUpdate);

                for (int i = 0; i < 24; i++) {
                    Double valueReplicaToUpdate = c * this.getVelocityReplicaComponent(provider, tierID, i);
                    resultVelocity.updateVelocityReplicaComponent(provider, tierID, i, valueReplicaToUpdate);
                }
            }
        }

        return resultVelocity;

    }

    private ParticleVelocity sum(ParticleVelocity otherVelocity, boolean sum) {

        ParticleVelocity resultVelocity = this.clone();

        for (String provider : resultVelocity.tierComponent.keySet()) {
            Map<String, Double> tierVelocity = tierComponent.get(provider);

            for (String tierID : tierVelocity.keySet()) {

                Double valueToUpdate;
                Double t1 = this.getVelocityTierComponent(provider, tierID);
                Double t2 = otherVelocity.getVelocityTierComponent(provider, tierID);
                if (sum) valueToUpdate = t1 = t2;
                else valueToUpdate = t1 - t2;

                resultVelocity.updateVelocityTierComponent(provider, tierID, valueToUpdate);

                for (int i = 0; i < 24; i++) {
                    Double valueReplicaToUpdate;
                    Double r1 = this.getVelocityReplicaComponent(provider, tierID, i);
                    Double r2 = otherVelocity.getVelocityReplicaComponent(provider, tierID, i);
                    if (sum) valueReplicaToUpdate = r1 + r2;
                    else valueReplicaToUpdate = r1 - r2;
                    resultVelocity.updateVelocityReplicaComponent(provider, tierID, i, valueReplicaToUpdate);

                }


            }
        }

        return resultVelocity;

    }


}
