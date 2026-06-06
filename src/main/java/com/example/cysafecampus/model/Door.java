package com.example.cysafecampus.model;
import java.io.Serializable;
/**
 * Represents a door connecting a room to a passage.
 * A door is a composition of a Room and links it to a Passage.
 */
public class Door implements Serializable {

    /** Whether the door is currently open */
    private boolean isOpen;

    /** The room this door belongs to */
    private Room room;

    /** The passage this door connects to */
    private Passage passage;

    /**
     * Constructor for Door.
     * @param room the room this door belongs to
     * @param passage the passage this door connects to
     */
    public Door(Room room, Passage passage) {
        this.room = room;
        this.passage = passage;
        this.isOpen = true;
    }

    public boolean isOpen() { return isOpen; }
    public Room getRoom() { return room; }
    public Passage getPassage() { return passage; }

    /**
     * Opens the door.
     */
    public void open() { this.isOpen = true; }

    /**
     * Closes the door.
     */
    public void close() { this.isOpen = false; }

    @Override
    public String toString() {
        return "Door[" + room.getName() + " <-> " + passage.getName() + "]";
    }
}