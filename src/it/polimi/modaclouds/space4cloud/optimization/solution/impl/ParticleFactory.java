package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import it.polimi.modaclouds.space4cloud.exceptions.ConstraintEvaluationException;
import it.polimi.modaclouds.space4cloud.exceptions.EvaluationException;
import it.polimi.modaclouds.space4cloud.exceptions.OptimizationException;
import it.polimi.modaclouds.space4cloud.optimization.MoveOnVM;
import it.polimi.modaclouds.space4cloud.optimization.MoveTypeVM;
import it.polimi.modaclouds.space4cloud.optimization.OptimizationEngineDPSO;
import it.polimi.modaclouds.space4cloud.optimization.constraints.Constraint;
import it.polimi.modaclouds.space4cloud.optimization.constraints.RamConstraint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParticleFactory {
    private final OptimizationEngineDPSO engine;
    private final Map<String, Map<String, List<CloudService>>> resMapPerSolutionPerTier;


    public ParticleFactory(OptimizationEngineDPSO engine) throws ConstraintEvaluationException {
        this.engine = engine;
        resMapPerSolutionPerTier = createResMapPerSolutionPerTier();
    }

    public Particle buildParticle(SolutionMulti sol) throws EvaluationException, OptimizationException {

        makeRamConstraintsSatisfied(sol);
        engine.getEvalServer().EvaluateSolution(sol);
        engine.makeFeasible(sol);
        Particle particle = new Particle(sol);

        engine.getTimer().split();
        long time = engine.getTimer().getSplitTime();
        particle.setGenerationTime(time);
        particle.setGenerationIteration(engine.getIteration());
        particle.setCloudResourceMap(resMapPerSolutionPerTier);
        particle.randomizeVelocity(engine.getRandom());
        return particle;
    }

    /**
     * This method create a solution for the swarm starting from the initial one and altering the resource type and
     * randomly preplicating the tiers. At the end the resulting solution is made feasible
     *
     * @return
     * @throws OptimizationException
     * @throws EvaluationException
     */
    public Particle buildRandomFeasibleParticle() throws OptimizationException, EvaluationException {

        SolutionMulti randomSolution = engine.getInitialSolution().clone();
        // the new solution has the same providers of the initial one

        //every solution has an application
        //every application has a set of tiers
        //every tier is associated with a vmType but the vmtype is the same for each application

        // Step 1 altering the cloud resource type for each tier of each solution

        updateWithRandomFeasibleCloudResources(randomSolution);

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
                    moveOnVM.scale(tier, engine.getRandom().nextInt(10*maxReplicas) + 1);
                }
            }
        }

        //step 3 the solutions have to be made feasible

        engine.getEvalServer().EvaluateSolution(randomSolution);
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

    private void makeRamConstraintsSatisfied(SolutionMulti sol) {

        for (Solution s : sol.getAll())
            if (!areRamConstraintsSatisfied(s))
                updateWithRandomFeasibleCloudResources(s);
    }

    private boolean areRamConstraintsSatisfied(Solution s) {
        for (Constraint constraint : s.getViolatedConstraints()) {
            return !(constraint instanceof RamConstraint);
        }
        return false;
    }

    private void updateWithRandomFeasibleCloudResources(Solution sol) {
        MoveTypeVM moveVM = new MoveTypeVM(sol);
        Instance application = sol.getApplication(0);

        for (Tier tier : application.getTiers()) {

            List<CloudService> cloudServices = resMapPerSolutionPerTier.get(sol.getProvider()).get(tier.getId());

            CloudService randomCloudResource = cloudServices.get(engine.getRandom().nextInt(cloudServices.size()));

            //this should change the type of resource into all instances contained into the solution
            moveVM.changeMachine(tier.getId(), randomCloudResource);
        }
    }

    private void updateWithRandomFeasibleCloudResources(SolutionMulti solution) {
        for (Solution sol : solution.getAll()) updateWithRandomFeasibleCloudResources(sol);
    }

    private Map<String, Map<String, List<CloudService>>> createResMapPerSolutionPerTier() throws ConstraintEvaluationException {

        SolutionMulti solution = engine.getInitialSolution();
        // the new solution has the same providers of the initial one

        //every solution has an application
        //every application has a set of tiers
        //every tier is associated with a vmType but the vmtype is the same for each application

        // Step 1 altering the cloud resource type for each tier of each solution
        Map<String, Map<String, List<CloudService>>> resMapPerSolutionPerTier = new HashMap<String, Map<String, List<CloudService>>>();
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
