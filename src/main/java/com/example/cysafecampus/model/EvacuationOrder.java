package com.example.cysafecampus.model;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/**
 * Represents a single evacuation order targeting a specific room.
 * Created by AdminAgent and sent to the SupervisorAgent assigned to that room.
 * Contains the target room and the recommended exit route.
 */
public class EvacuationOrder implements Serializable {

    /** The room that must be evacuated */
    private Room targetRoom;

    /**
     * Ordered list of building elements the occupants should follow
     * to reach the exit (e.g. Room → Corridor → Staircase → Exit).
     */
    private List<BuildingElement> exitRoute;

    /**
     * Urgency level from 1 (low) to 5 (immediate).
     * Mirrors the sensor event severity that triggered this order.
     */
    private int urgencyLevel;

    /**
     * Constructor for EvacuationOrder.
     * @param targetRoom the room to evacuate
     * @param exitRoute the recommended path to the exit
     * @param urgencyLevel urgency from 1 to 5
     */
    public EvacuationOrder(Room targetRoom, List<BuildingElement> exitRoute, int urgencyLevel) {
        this.targetRoom = targetRoom;
        this.exitRoute = exitRoute != null ? exitRoute : new ArrayList<>();
        this.urgencyLevel = urgencyLevel;
    }

    public Room getTargetRoom() { return targetRoom; }
    public List<BuildingElement> getExitRoute() { return exitRoute; }
    public int getUrgencyLevel() { return urgencyLevel; }

    @Override
    public String toString() {
        return "EvacuationOrder[room=" + targetRoom.getName()
            + ", urgency=" + urgencyLevel
            + ", route=" + exitRoute.size() + " steps]";
    }
}
