package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import it.polimi.modaclouds.space4cloud.exceptions.ConstraintEvaluationException;
import it.polimi.modaclouds.space4cloud.exceptions.OptimizationException;
import it.polimi.modaclouds.space4cloud.optimization.MoveOnVM;
import it.polimi.modaclouds.space4cloud.optimization.MoveTypeVM;
import it.polimi.modaclouds.space4cloud.optimization.OptimizationEngineDPSO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParticleFactory {
    private final OptimizationEngineDPSO engine;

    public ParticleFactory(OptimizationEngineDPSO engine) {
        this.engine = engine;
    }

    public Particle buildParticle(SolutionMulti sol) throws ConstraintEvaluationException {
        Particle randomFeasibleParticle = new Particle(sol);
        engine.getTimer().split();
        long time = engine.getTimer().getSplitTime();
        randomFeasibleParticle.setGenerationTime(time);
        randomFeasibleParticle.setGenerationIteration(engine.getIteration());
        randomFeasibleParticle.setCloudResourceMap(createResMapPerSolutionPerTier());
        randomFeasibleParticle.randomizeVelocity(engine.getRandom());
    }

    /**
     * This method create a solution for the swarm starting from the initial one and altering the resource type and
     * randomly preplicating the tiers. At the end the resulting solution is made feasible
     *
     * @return
     * @throws ConstraintEvaluationException
     * @throws OptimizationException
     */
    public Particle buildRandomFeasibleParticle() throws ConstraintEvaluationException, OptimizationException {

        //todo: pay attention to the single directory where lqn files are saved
        SolutionMulti randomSolution = engine.getInitialSolution().clone();
        // the new solution has the same providers of the initial one

        //every solution has an application
        //every application has a set of tiers
        //every tier is associated with a vmType but the vmtype is the same for each application

        // Step 1 altering the cloud resource type for each tier of each solution
        Map<String, Map<String, List<CloudService>>> resMapPerSolutionPerTier = new HashMap();
        for (Solution sol : randomSolution.getAll()) {
            MoveTypeVM moveVM = new MoveTypeVM(sol);
            Instance application = sol.getApplication(0);

            Map<String, List<CloudService>> resListMap = new HashMap<>();
            for (Tier tier : application.getTiers()) {
                CloudService originalCloudResource = tier.getCloudService();

                List<CloudService> resList = engine.getDataBaseHandler().getSameService(originalCloudResource, sol.getRegion());

                //just in case
                if (!resList.contains(originalCloudResource)) resList.add(originalCloudResource);

                // filter resources according to architectural constraints
                try {
                    engine.getConstraintHandler().filterResources(resList, tier);
                } catch (ConstraintEvaluationException e) {
                    throw new ConstraintEvaluationException("Error filtering resources for creating a random solution", e);
                }

                CloudService randomCloudResource = resList.get(engine.getRandom().nextInt(resList.size()));

                //this should change the type of resource into all instances contained into the solution //todo: check
                moveVM.changeMachine(tier.getId(), randomCloudResource);
                resListMap.put(tier.getId(), resList);
            }
            resMapPerSolutionPerTier.put(sol.getProvider(), resListMap);
        }

        // Step 2 altering the number of replicas for each tier and each our
        //for each provider let's calculate the maximum number of replicas per over all tiers

        int maxReplicas = 0;
        for (Solution sol : randomSolution.getAll()) {
            for (int i = 0; i < 24; i++) {
                for (Tier tier : sol.getApplication(i).getTiers()) {
                    maxReplicas = Math.max(maxReplicas, tier.getCloudService().getReplicas());
                }
            }
        }


        for (Solution sol : randomSolution.getAll()) {
            for (int i = 0; i < 24; i++) {
                MoveOnVM moveOnVM = new MoveOnVM(sol, i);
                for (Tier tier : sol.getApplication(i).getTiers()) {
                    moveOnVM.scale(tier, engine.getRandom().nextInt(maxReplicas + 1));
                }
            }
        }

        //step 3 the solutions have to be made feasible

        engine.makeFeasible(randomSolution);

        Particle randomFeasibleParticle = new Particle(randomSolution);
        engine.getTimer().split();
        long time = engine.getTimer().getSplitTime();
        randomFeasibleParticle.setGenerationTime(time);
        randomFeasibleParticle.setGenerationIteration(engine.getIteration());
        randomFeasibleParticle.setCloudResourceMap(resMapPerSolutionPerTier);
        randomFeasibleParticle.randomizeVelocity(engine.getRandom());
        return randomFeasibleParticle;
    }

    private Map<String, Map<String, List<CloudService>>> createResMapPerSolutionPerTier() throws ConstraintEvaluationException {

        //todo: pay attention to the single directory where lqn files are saved
        SolutionMulti solution = engine.getInitialSolution();
        // the new solution has the same providers of the initial one

        //every solution has an application
        //every application has a set of tiers
        //every tier is associated with a vmType but the vmtype is the same for each application

        // Step 1 altering the cloud resource type for each tier of each solution
        Map<String, Map<String, List<CloudService>>> resMapPerSolutionPerTier = new HashMap();
        for (Solution sol : solution.getAll()) {
            Instance application = sol.getApplication(0);

            Map<String, List<CloudService>> resListMap = new HashMap<>();
            for (Tier tier : application.getTiers()) {
                CloudService originalCloudResource = tier.getCloudService();

                List<CloudService> resList = engine.getDataBaseHandler().getSameService(originalCloudResource, sol.getRegion());

                //just in case
                if (!resList.contains(originalCloudResource)) resList.add(originalCloudResource);

                // filter resources according to architectural constraints
                try {
                    engine.getConstraintHandler().filterResources(resList, tier);
                } catch (ConstraintEvaluationException e) {
                    throw new ConstraintEvaluationException("Error filtering resources for creating a random solution", e);
                }

                resListMap.put(tier.getId(), resList);
            }
            resMapPerSolutionPerTier.put(sol.getProvider(), resListMap);
        }

        return resMapPerSolutionPerTier;
    }
}
