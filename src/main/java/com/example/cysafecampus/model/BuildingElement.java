package com.example.cysafecampus.model;

import java.io.Serializable;

/**
 * Abstract class representing a physical space in the building.
 * All building elements share common properties like capacity, occupancy,
 * attractiveness, and accessibility status.
 */
public abstract class BuildingElement implements Serializable {

    /** Unique name of the element (e.g., "Room 101", "Corridor A") */
    private String name;

    /** Maximum number of agents allowed in this element */
    private int maxCapacity;

    /** Current number of agents present */
    private int currentOccupancy;

    /**
     * Accessibility status of this element.
     * Replaces the old boolean isBlocked for finer-grained control.
     */
    private BlockStatus status;

    /** Score representing how attractive (>0) or repulsive (<0) the element is for navigation */
    private double attractivenessScore;

    /**
     * Constructor for BuildingElement.
     * @param name the name of the element
     * @param maxCapacity the maximum capacity of the element
     */
    public BuildingElement(String name, int maxCapacity) {
        this.name = name;
        this.maxCapacity = maxCapacity;
        this.currentOccupancy = 0;
        this.status = BlockStatus.ACCESSIBLE;
        this.attractivenessScore = 0.0;
    }

    public String getName() { return name; }
    public int getMaxCapacity() { return maxCapacity; }
    public int getCurrentOccupancy() { return currentOccupancy; }
    public BlockStatus getStatus() { return status; }
    public double getAttractivenessScore() { return attractivenessScore; }

    public void setName(String name) { this.name = name; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }
    public void setCurrentOccupancy(int occupancy) { this.currentOccupancy = occupancy; }
    public void setAttractivenessScore(double score) { this.attractivenessScore = score; }

    /**
     * Updates the accessibility status of this element.
     * @param status the new BlockStatus
     */
    public void setStatus(BlockStatus status) { this.status = status; }

    /**
     * Convenience method — true if status is BLOCKED.
     * Used by PathFinder to skip impassable elements.
     */
    public boolean isBlocked() {
        return status == BlockStatus.BLOCKED;
    }

    /**
     * Checks if the element is at full capacity.
     * @return true if current occupancy reaches or exceeds max capacity
     */
    public boolean isFull() {
        return currentOccupancy >= maxCapacity;
    }

    @Override
    public String toString() {
        return name + " (" + currentOccupancy + "/" + maxCapacity + ") [" + status + "]";
    }
}
