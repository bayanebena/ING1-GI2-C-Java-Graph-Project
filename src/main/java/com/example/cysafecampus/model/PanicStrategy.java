package com.example.cysafecampus.model;

import java.util.List;
import java.util.ArrayList;

/**
 * Strategy for panicked agents.
 * Moves faster (1.5× speed multiplier) but ignores density tolerance
 * and may push into congested passages (no congestion wait).
 * Uses fastest path instead of shortest path.
 */
public class PanicStrategy implements MovementStrategy {

    private static final double PANIC_SPEED_MULTIPLIER = 1.5;

    @Override
    public void execute(Agent agent) {

        if (agent.getDestination() == null) return;
        if (agent.hasArrived()) {
            agent.setPath(new ArrayList<>());
            return;
        }

        // Recompute path (fastest, taking congestion into account)
        List<BuildingElement> path = agent.getPath();
        if (path == null || path.isEmpty()) {
            List<BuildingElement> newPath = PathFinder.calculateFastestPath(
                agent.getCurrentLocation(), agent.getDestination(), agent.getMaxSpeed());
            if (newPath.isEmpty()) return;
            agent.setPath(newPath);
            path = newPath;
        }

        // No congestion wait when panicked
        agent.setWaitCycles(0);

        int idx = agent.getPathIndex();
        if (idx + 1 >= path.size()) return;

        BuildingElement next = path.get(idx + 1);
        if (next.isBlocked()) {
            agent.setPath(new ArrayList<>());
            return;
        }

        if (next instanceof Passage) {
            Passage passage = (Passage) next;

            // Panicked agents move faster, ignore congestion check
            double step = (agent.getMaxSpeed() * PANIC_SPEED_MULTIPLIER
                           * passage.getSpeedFactor()) / passage.getDistance();
            agent.setProgress(agent.getProgress() + step);

            if (agent.getProgress() <= step + 0.001) {
                passage.setCurrentOccupancy(passage.getCurrentOccupancy() + 1);
            }

            if (agent.getProgress() >= 1.0) {
                passage.setCurrentOccupancy(Math.max(0, passage.getCurrentOccupancy() - 1));
                agent.setProgress(0.0);
                agent.setCurrentLocation(next);
                agent.setPathIndex(idx + 1);
            }

        } else {
            BuildingElement current = agent.getCurrentLocation();
            current.setCurrentOccupancy(Math.max(0, current.getCurrentOccupancy() - 1));
            next.setCurrentOccupancy(next.getCurrentOccupancy() + 1);
            agent.setCurrentLocation(next);
            agent.setPathIndex(idx + 1);
            agent.setProgress(0.0);
        }
    }

    @Override
    public List<BuildingElement> calculatePath() {
        return new ArrayList<>();
    }
}
