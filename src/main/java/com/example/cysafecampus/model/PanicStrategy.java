package com.example.cysafecampus.model;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/**
 * Strategy for panicked agents — 1.5x speed, ignores congestion.
 * Uses fastest path (Dijkstra by time + congestion).
 */
public class PanicStrategy extends EvacuateStrategy implements Serializable {

    private static final double PANIC_MULTIPLIER = 1.5;

    @Override
    public void execute(Agent agent) {
        if (agent.getDestination() == null) return;
        if (agent.hasArrived()) { agent.setPath(new ArrayList<>()); agent.setDestination(null); return; }

        List<BuildingElement> path = agent.getPath();
        if (path == null || path.isEmpty()) {
            List<BuildingElement> newPath = PathFinder.calculateFastestPath(
                agent.getCurrentLocation(), agent.getDestination(), agent.getMaxSpeed());
            if (newPath.isEmpty()) return;
            agent.setPath(newPath);
            path = newPath;
        }

        // Panicked agents skip congestion wait
        agent.setWaitCycles(0);

        int idx = agent.getPathIndex();
        if (idx + 1 >= path.size()) return;

        BuildingElement next = path.get(idx + 1);
        if (next.isBlocked()) { agent.setPath(new ArrayList<>()); return; }

        if (next instanceof Passage) {
            Passage passage = (Passage) next;

            if (agent.getProgress() == 0.0) {
                agent.getCurrentLocation().agentLeaves();
                passage.agentEnters(agent.getMaxSpeed());
                agent.setCurrentLocation(passage);
                agent.setPathIndex(idx + 1);
                idx = idx + 1;
            }

            double dist = Math.max(0.1, passage.getDistance());
            double step = (agent.getMaxSpeed() * PANIC_MULTIPLIER * passage.getSpeedFactor()) / dist;
            agent.setProgress(agent.getProgress() + step);

            if (agent.getProgress() >= 1.0) {
                agent.setProgress(0.0);
                int nextIdx = idx + 1;
                if (nextIdx < path.size()) {
                    BuildingElement dest = path.get(nextIdx);
                    passage.agentLeaves();
                    dest.agentEnters(agent.getMaxSpeed());
                    agent.setCurrentLocation(dest);
                    agent.setPathIndex(nextIdx);
                } else {
                    passage.agentLeaves();
                    agent.setCurrentLocation(passage);
                }
            }
        } else {
            agent.getCurrentLocation().agentLeaves();
            next.agentEnters(agent.getMaxSpeed());
            agent.setCurrentLocation(next);
            agent.setPathIndex(idx + 1);
            agent.setProgress(0.0);
        }
    }
}
