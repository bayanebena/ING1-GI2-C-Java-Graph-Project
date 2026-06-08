package com.example.cysafecampus.model;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/**
 * Strategy for calm evacuation — moves agent node by node toward destination.
 * Uses shortest path (Dijkstra by distance).
 */
public class EvacuateStrategy implements MovementStrategy, Serializable {

    @Override
    public void execute(Agent agent) {
        if (agent.getDestination() == null) return;
        if (agent.hasArrived()) { onArrival(agent); return; }

        // Compute path if needed
        List<BuildingElement> path = agent.getPath();
        if (path == null || path.isEmpty()) {
            List<BuildingElement> newPath = PathFinder.calculateShortestPath(
                agent.getCurrentLocation(), agent.getDestination());
            if (newPath.isEmpty()) return;
            agent.setPath(newPath);
            path = newPath;
        }

        // Congestion wait
        if (agent.getWaitCycles() > 0) {
            agent.setWaitCycles(agent.getWaitCycles() - 1);
            return;
        }

        int idx = agent.getPathIndex();
        if (idx + 1 >= path.size()) {
            // End of known path — mark arrived
            agent.setCurrentLocation(path.get(path.size() - 1));
            return;
        }

        BuildingElement next = path.get(idx + 1);

        // Strong congestion rule: if a forced relocation placed too many agents
        // in the current node, the agent loses two simulation cycles before it
        // can enter an available corridor. The marker prevents infinite waiting
        // while the node is still overcrowded.
        BuildingElement current = agent.getCurrentLocation();

        if (current != null && !current.isOvercrowded()) {
            agent.clearStrongCongestionDelayMarker();
        }

        if (current != null
                && current.isOvercrowded()
                && next instanceof Passage
                && !agent.hasCompletedStrongCongestionDelayFor(current)) {
            agent.setWaitCycles(2);
            agent.markStrongCongestionDelayCompletedFor(current);
            return;
        }

        // Blocked node → recompute path
        if (next.isBlocked()) {
            agent.setPath(new ArrayList<>());
            agent.setProgress(0.0);
            return;
        }

        // If the next element is a passage, apply congestion rules before entering it.
        if (next instanceof Passage) {
            Passage passage = (Passage) next;

            int maxLanes = Math.max(1, passage.getLanes());
            double density = passage.getMaxCapacity() > 0
                ? (double) passage.getCurrentOccupancy() / passage.getMaxCapacity()
                : 0.0;

            if (agent.getBehavior() == Behavior.POLITE && density > agent.getDensityTolerance()) {
                agent.setWaitCycles(2);
                return;
            }

            if (agent.getBehavior() == Behavior.FOLLOWER && passage.getCurrentOccupancy() >= maxLanes) {
                agent.setWaitCycles(1);
                return;
            }

            if (agent.getBehavior() == Behavior.RUDE && passage.getCurrentOccupancy() >= passage.getMaxCapacity()) {
                agent.setWaitCycles(1);
                return;
            }

            if (agent.getBehavior() != Behavior.RUDE && passage.getCurrentOccupancy() >= passage.getMaxCapacity()) {
                agent.setWaitCycles(2);
                return;
            }
        }

        // Move progressively between the current element and the next element.
        // The current location and path index are updated only when progress reaches 1.
        double distance = 10.0;

        if (next instanceof Passage) {
            Passage passage = (Passage) next;
            distance = Math.max(0.1, passage.getDistance());
        }

        double speedFactor = next instanceof Passage
            ? ((Passage) next).getSpeedFactor()
            : 1.0;

        double step = Math.max(0.05, (agent.getMaxSpeed() * speedFactor) / distance);
        double newProgress = agent.getProgress() + step;

        if (newProgress >= 1.0) {
            BuildingElement previous = agent.getCurrentLocation();

            if (previous != null) {
                previous.agentLeaves();
            }

            next.agentEnters(agent.getMaxSpeed());
            recordCorridorPassage(previous, next, agent.getMaxSpeed());

            agent.setCurrentLocation(next);
            agent.setPathIndex(idx + 1);
            agent.setProgress(0.0);

            if (next.equals(agent.getDestination())) {
                onArrival(agent);
            }

            return;
        }

        agent.setProgress(newProgress);
        return;
    }

    /**
     * Records edge-level statistics for the corridor segment that connects two
     * consecutive path elements. This keeps edge statistics separate from room,
     * exit and junction statistics.
     *
     * @param previous element left by the agent
     * @param next element reached by the agent
     * @param agentSpeed speed of the agent
     */
    protected void recordCorridorPassage(BuildingElement previous, BuildingElement next, double agentSpeed) {
        Door door = findConnectingDoor(previous, next);

        if (door != null) {
            door.recordAgentPassage(agentSpeed);
        }
    }

    /**
     * Finds the door object that represents the corridor segment between two
     * neighboring graph elements.
     *
     * @param first first graph element
     * @param second second graph element
     * @return matching door, or null when the elements are not directly linked
     */
    private Door findConnectingDoor(BuildingElement first, BuildingElement second) {
        if (first instanceof Room && second instanceof Passage) {
            return findDoor((Room) first, (Passage) second);
        }

        if (first instanceof Passage && second instanceof Room) {
            return findDoor((Room) second, (Passage) first);
        }

        return null;
    }

    /**
     * Finds the door linking a specific room-like node to a specific passage.
     *
     * @param room room-like node
     * @param passage passage node
     * @return matching door, or null if no matching segment exists
     */
    private Door findDoor(Room room, Passage passage) {
        for (Door door : room.getDoors()) {
            if (door.getPassage() == passage) {
                return door;
            }
        }

        return null;
    }

    protected void onArrival(Agent agent) {
        agent.setPath(new ArrayList<>());
        agent.setDestination(null);
        agent.setProgress(0.0);
    }

    @Override
    public List<BuildingElement> calculatePath() { return new ArrayList<>(); }
}
