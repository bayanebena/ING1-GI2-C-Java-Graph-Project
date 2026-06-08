package com.example.cysafecampus.model;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/**
 * Strategy for panicked agents.
 * Panicked agents move faster and tolerate congestion better, but their motion
 * still remains progressive on screen: no instant jump into the next passage.
 */
public class PanicStrategy extends EvacuateStrategy implements Serializable {

    /** Speed multiplier applied to panicked agents. */
    private static final double PANIC_MULTIPLIER = 1.5;

    @Override
    public void execute(Agent agent) {
        if (agent.getDestination() == null) return;
        if (agent.hasArrived()) {
            agent.setPath(new ArrayList<>());
            agent.setDestination(null);
            agent.setProgress(0.0);
            return;
        }

        List<BuildingElement> path = agent.getPath();
        if (path == null || path.isEmpty()) {
            List<BuildingElement> newPath = PathFinder.calculateFastestPath(
                agent.getCurrentLocation(), agent.getDestination(), agent.getMaxSpeed());
            if (newPath.isEmpty()) return;
            agent.setPath(newPath);
            path = newPath;
        }

        agent.setWaitCycles(0);

        int index = agent.getPathIndex();
        if (index + 1 >= path.size()) return;

        BuildingElement next = path.get(index + 1);
        if (next.isBlocked()) {
            agent.setPath(new ArrayList<>());
            agent.setProgress(0.0);
            return;
        }

        double distance = 10.0;
        double speedFactor = 1.0;

        if (next instanceof Passage) {
            Passage passage = (Passage) next;
            distance = Math.max(0.1, passage.getDistance());
            speedFactor = passage.getSpeedFactor();
        }

        double step = Math.max(0.05,
            (agent.getMaxSpeed() * PANIC_MULTIPLIER * speedFactor) / distance);
        double newProgress = agent.getProgress() + step;

        if (newProgress >= 1.0) {
            BuildingElement previous = agent.getCurrentLocation();

            if (previous != null) {
                previous.agentLeaves();
            }

            next.agentEnters(agent.getMaxSpeed());
            agent.setCurrentLocation(next);
            agent.setPathIndex(index + 1);
            agent.setProgress(0.0);

            if (next.equals(agent.getDestination())) {
                agent.setPath(new ArrayList<>());
                agent.setDestination(null);
            }
            return;
        }

        agent.setProgress(newProgress);
    }
}
