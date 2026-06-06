package com.example.cysafecampus.model;

import java.util.List;
import java.util.ArrayList;

/**
 * Strategy for calm evacuation.
 * Each tick, advances the agent along its computed path toward the destination.
 *
 * Movement model (node-to-node via passages):
 *   1. If agent has no path or path is empty → compute one via PathFinder.
 *   2. If agent is waiting (congestion) → decrement wait counter, skip tick.
 *   3. If next element is a Passage → accumulate progress (speed / distance).
 *      When progress >= 1.0 → move to next node, reset progress.
 *   4. If next element is a Room/Exit → move instantly (nodes are points).
 *   5. If agent has arrived → pick a new random destination or stay.
 */
public class EvacuateStrategy implements MovementStrategy {

    @Override
    public void execute(Agent agent) {

        // ── 1. No destination → nothing to do ────────────────
        if (agent.getDestination() == null) return;

        // ── 2. Already arrived ────────────────────────────────
        if (agent.hasArrived()) {
            onArrival(agent);
            return;
        }

        // ── 3. Compute / recompute path if needed ─────────────
        List<BuildingElement> path = agent.getPath();
        if (path == null || path.isEmpty()) {
            List<BuildingElement> newPath = PathFinder.calculateShortestPath(
                agent.getCurrentLocation(), agent.getDestination());
            if (newPath.isEmpty()) return; // no route found
            agent.setPath(newPath);
            path = newPath;
        }

        // ── 4. Congestion wait ────────────────────────────────
        if (agent.getWaitCycles() > 0) {
            agent.setWaitCycles(agent.getWaitCycles() - 1);
            return;
        }

        // ── 5. Determine next element ─────────────────────────
        int idx = agent.getPathIndex();
        if (idx + 1 >= path.size()) {
            // Reached end of path
            agent.setCurrentLocation(path.get(path.size() - 1));
            return;
        }

        BuildingElement next = path.get(idx + 1);

        // Skip blocked elements → recompute path
        if (next.isBlocked()) {
            agent.setPath(new ArrayList<>());
            return;
        }

        // ── 6. Move through Passage (progress-based) ──────────
        if (next instanceof Passage) {
            Passage passage = (Passage) next;

            // Check strong congestion: occupancy > capacity → 2 wait cycles
            if (passage.isFull()) {
                agent.setWaitCycles(2);
                return;
            }

            // Advance progress: speed × speedFactor / distance per tick
            double step = (agent.getMaxSpeed() * passage.getSpeedFactor())
                          / passage.getDistance();
            agent.setProgress(agent.getProgress() + step);

            // First tick entering the passage → update occupancy
            if (agent.getProgress() <= step + 0.001) {
                passage.setCurrentOccupancy(passage.getCurrentOccupancy() + 1);
            }

            if (agent.getProgress() >= 1.0) {
                // Leave passage
                passage.setCurrentOccupancy(Math.max(0, passage.getCurrentOccupancy() - 1));
                agent.setProgress(0.0);
                agent.setCurrentLocation(next);
                agent.setPathIndex(idx + 1);
            }

        } else {
            // ── 7. Move instantly into Room / Exit ────────────
            // Update occupancy of previous location
            BuildingElement current = agent.getCurrentLocation();
            current.setCurrentOccupancy(Math.max(0, current.getCurrentOccupancy() - 1));

            next.setCurrentOccupancy(next.getCurrentOccupancy() + 1);
            agent.setCurrentLocation(next);
            agent.setPathIndex(idx + 1);
            agent.setProgress(0.0);
        }
    }

    /**
     * Called when the agent reaches its destination.
     * Default: pick a new random destination from the graph (wander behavior).
     * Override or extend for specific behaviors (evacuate to exit, stay, etc.)
     */
    protected void onArrival(Agent agent) {
        // Reset — agent stays at destination until reassigned
        agent.setPath(new ArrayList<>());
    }

    @Override
    public List<BuildingElement> calculatePath() {
        return new ArrayList<>();
    }
}
