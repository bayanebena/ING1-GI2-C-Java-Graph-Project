package com.example.cysafecampus.model;

import java.util.List;

/**
 * Interface defining the movement strategy for an agent.
 * Different strategies implement different behaviors
 * (evacuate, panic, guide, etc.)
 */
public interface MovementStrategy {

    /**
     * Executes the movement strategy for the given agent.
     * @param agent the agent to move
     */
    void execute(Agent agent);

    /**
     * Calculates the best path for the agent to follow.
     * @return list of building elements representing the path
     */
    List<BuildingElement> calculatePath();
}