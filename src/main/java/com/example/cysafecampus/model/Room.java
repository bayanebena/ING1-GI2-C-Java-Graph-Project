package com.example.cysafecampus.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a room in the building (classroom, amphitheater, office, etc.)
 * A room is composed of one or more doors connecting it to passages.
 */
public class Room extends BuildingElement {

    /** Floor number where the room is located */
    private int floor;

    /** Type of room (classroom, amphitheater, office) */
    private RoomType type;

    /** List of doors connected to this room (composition) */
    private List<Door> doors;

    /**
     * Constructor for Room.
     * @param name the room name (e.g. "Salle 101")
     * @param maxCapacity maximum number of agents
     * @param floor floor number
     * @param type the room type
     */
    public Room(String name, int maxCapacity, int floor, RoomType type) {
        super(name, maxCapacity);
        this.floor = floor;
        this.type = type;
        this.doors = new ArrayList<>();
    }

    /** Backwards-compatible constructor without RoomType. */
    public Room(String name, int maxCapacity, int floor) {
        this(name, maxCapacity, floor, RoomType.CLASSROOM);
    }

    public int getFloor() { return floor; }
    public RoomType getType() { return type; }
    public List<Door> getDoors() { return doors; }

    /**
     * Adds a door to this room.
     * @param door the door to add
     */
    public void addDoor(Door door) {
        doors.add(door);
    }
}
