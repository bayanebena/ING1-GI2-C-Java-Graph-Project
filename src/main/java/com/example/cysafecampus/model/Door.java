package com.example.cysafecampus.model;

import java.io.Serializable;

/**
 * Represents a corridor segment connecting one room-like node to one passage
 * node. In the user interface this object is displayed as one selectable edge.
 */
public class Door implements Serializable {

    /** Whether the corridor segment is currently open. */
    private boolean isOpen;

    /** The room, exit or virtual junction connected to the passage. */
    private Room room;

    /** The passage connected to the room-like node. */
    private Passage passage;

    /** Maximum number of agents that can be inside this corridor segment. */
    private int maxCapacity;

    /** Number of agents that have completed this corridor segment. */
    private int totalAgentsPassed;

    /** Sum of speeds of agents that completed this corridor segment. */
    private double totalSpeedSum;

    /**
     * Constructor for a corridor segment with a default capacity.
     *
     * @param room the room-like node connected to the passage
     * @param passage the passage connected to the room-like node
     */
    public Door(Room room, Passage passage) {
        this(room, passage, 10);
    }

    /**
     * Constructor for a corridor segment with a custom capacity.
     *
     * @param room the room-like node connected to the passage
     * @param passage the passage connected to the room-like node
     * @param maxCapacity maximum number of agents allowed inside the segment
     */
    public Door(Room room, Passage passage, int maxCapacity) {
        this.room = room;
        this.passage = passage;
        this.isOpen = true;
        this.maxCapacity = Math.max(1, maxCapacity);
        this.totalAgentsPassed = 0;
        this.totalSpeedSum = 0.0;
    }

    /**
     * Returns whether this corridor segment is open.
     *
     * @return true if the segment is open
     */
    public boolean isOpen() { return isOpen; }

    /**
     * Returns the room-like node connected to this corridor segment.
     *
     * @return connected room-like node
     */
    public Room getRoom() { return room; }

    /**
     * Returns the passage connected to this corridor segment.
     *
     * @return connected passage
     */
    public Passage getPassage() { return passage; }

    /**
     * Returns the maximum capacity of this corridor segment.
     *
     * @return maximum capacity
     */
    public int getMaxCapacity() { return maxCapacity; }

    /**
     * Returns the number of agents that have completed this segment.
     *
     * @return total agents passed
     */
    public int getTotalAgentsPassed() { return totalAgentsPassed; }

    /**
     * Returns the average speed of agents that completed this segment.
     *
     * @return average speed, or zero if no agent has passed yet
     */
    public double getAverageSpeed() {
        return totalAgentsPassed > 0 ? totalSpeedSum / totalAgentsPassed : 0.0;
    }

    /**
     * Sets the maximum capacity of this corridor segment.
     *
     * @param maxCapacity maximum capacity, must be greater than zero
     */
    public void setMaxCapacity(int maxCapacity) {
        if (maxCapacity <= 0) {
            throw new IllegalArgumentException("Corridor capacity must be greater than zero.");
        }
        this.maxCapacity = maxCapacity;
    }

    /**
     * Records that an agent completed this corridor segment.
     *
     * @param agentSpeed speed of the agent that passed
     */
    public void recordAgentPassage(double agentSpeed) {
        totalAgentsPassed++;
        totalSpeedSum += Math.max(0.0, agentSpeed);
    }

    /** Opens the corridor segment. */
    public void open() { this.isOpen = true; }

    /** Closes the corridor segment. */
    public void close() { this.isOpen = false; }

    @Override
    public String toString() {
        return "Corridor[" + room.getName() + " <-> " + passage.getName()
            + ", capacity=" + maxCapacity + "]";
    }
}
