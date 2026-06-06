package com.example.cysafecampus.model;

/**
 * Represents an exit point of the building.
 * Agents reaching an exit are considered successfully evacuated.
 */
public class Exit extends BuildingElement {

    /**
     * Constructor for Exit.
     * @param name the exit name (e.g. "Exit B")
     * @param maxCapacity maximum flow of agents per simulation cycle
     */
    public Exit(String name, int maxCapacity) {
        super(name, maxCapacity);
    }

    /**
     * Checks if this exit is usable (not blocked).
     * @return true if the exit is open and not blocked
     */
    public boolean isUsable() {
        return !isBlocked();
    }
}