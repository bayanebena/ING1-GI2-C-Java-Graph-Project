package com.example.cysafecampus.model;

import java.util.List;
import java.util.ArrayList;

/**
 * Strategy for security agents guiding people.
 * Moves at normal speed, uses shortest path, waits politely in congestion.
 * Identical to EvacuateStrategy but semantically distinct for security agents.
 */
public class GuideStrategy extends EvacuateStrategy implements java.io.Serializable {

    @Override
    protected void onArrival(Agent agent) {
        // Security agents stay at their assigned zone when they arrive
        agent.setPath(new ArrayList<>());
        agent.setDestination(agent.getCurrentLocation());
    }

    @Override
    public List<BuildingElement> calculatePath() {
        return new ArrayList<>();
    }
}
