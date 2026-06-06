package com.example.cysafecampus.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a passage in the building (corridor or staircase).
 * Passages connect building elements and contain physical constraints
 * that affect agent movement.
 */
public class Passage extends BuildingElement {

    /** Speed multiplier for agents moving through this passage.
     * < 1.0 = slower (e.g., stairs), > 1.0 = faster (e.g., moving walkway) */
    private double speedFactor;

    /** Floor number where this passage is located */
    private int floor;

    /** Type of passage: CORRIDOR or STAIRCASE */
    private PassageType type;

    /** Physical length/distance of the passage, required for shortest path calculation */
    private double distance;

    /** Doors connecting this passage to rooms (allows bidirectional navigation) */
    private List<Door> connectedDoors;

    /** Defines if the passage can only be traversed in one specific direction */
    private boolean isOneWay = false;

    /** Number of parallel lanes. 1 lane = no overtaking, > 1 = overtaking possible */
    private int lanes = 1;

    /**
     * Constructor for Passage.
     * @param name The passage name
     * @param maxCapacity Maximum number of agents
     * @param floor Floor number
     * @param speedFactor Speed multiplier
     * @param type The PassageType (CORRIDOR or STAIRCASE)
     * @param distance Physical length of the passage
     */
    public Passage(String name, int maxCapacity, int floor,
                   double speedFactor, PassageType type, double distance) {
        super(name, maxCapacity);
        this.floor = floor;
        this.speedFactor = speedFactor;
        this.type = type;
        this.distance = distance;
        this.connectedDoors = new ArrayList<>();
    }

    public double getSpeedFactor() { return speedFactor; }
    public int getFloor() { return floor; }
    public PassageType getType() { return type; }
    public double getDistance() { return distance; }
    public List<Door> getConnectedDoors() { return connectedDoors; }

    public void setSpeedFactor(double speedFactor) { this.speedFactor = speedFactor; }
    public void setDistance(double distance) { this.distance = distance; }

    public boolean isOneWay() { return isOneWay; }
    public void setOneWay(boolean isOneWay) { this.isOneWay = isOneWay; }

    public int getLanes() { return lanes; }
    public void setLanes(int lanes) { this.lanes = lanes; }

    /**
     * Links a door to this passage to allow movement.
     * @param door The door to connect to the passage
     */
    public void addDoor(Door door) {
        this.connectedDoors.add(door);
    }
}