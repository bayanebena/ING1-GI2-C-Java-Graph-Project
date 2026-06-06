package com.example.cysafecampus.model;

/**
 * Represents an exit point of the building.
 * Extends Room so it can connect to Passages via Door objects,
 * making it reachable by PathFinder (which navigates Room <-> Passage).
 * Agents reaching an Exit are considered successfully evacuated.
 */
public class Exit extends Room {

    /**
     * Constructor for Exit.
     * @param name the exit name (e.g. "Sortie Est 1")
     * @param maxCapacity maximum simultaneous agents at this exit
     */
    public Exit(String name, int maxCapacity) {
        super(name, maxCapacity, 0, RoomType.OFFICE);
    }

    /**
     * Checks if this exit is usable (not blocked).
     * @return true if the exit is accessible
     */
    public boolean isUsable() {
        return !isBlocked();
    }
}
