package com.example.cysafecampus.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class representing a physical sensor attached to a building element.
 * Acts as the Subject in the Sensor → AdminAgent Observer pattern.
 * Subclasses implement the actual detection logic (presence, smoke, etc.)
 */
public abstract class Sensor implements Serializable {

    /** Unique identifier for this sensor */
    private String id;

    /** The building element this sensor is monitoring */
    private BuildingElement monitoredElement;

    /** Real-time people count reported by this sensor */
    private int realTimePeopleCount;

    /** List of observers to notify — in practice, only AdminAgent subscribes */
    private List<SensorObserver> observers;

    /**
     * Constructor for Sensor.
     * @param id unique sensor identifier
     * @param monitoredElement the element this sensor watches
     */
    public Sensor(String id, BuildingElement monitoredElement) {
        this.id = id;
        this.monitoredElement = monitoredElement;
        this.realTimePeopleCount = 0;
        this.observers = new ArrayList<>();
    }

    public String getId() { return id; }
    public BuildingElement getMonitoredElement() { return monitoredElement; }
    public int getRealTimePeopleCount() { return realTimePeopleCount; }
    public void setRealTimePeopleCount(int count) { this.realTimePeopleCount = count; }

    /** Registers an observer (AdminAgent) to receive events from this sensor. */
    public void addObserver(SensorObserver observer) {
        observers.add(observer);
    }

    /** Unregisters an observer. */
    public void removeObserver(SensorObserver observer) {
        observers.remove(observer);
    }

    /**
     * Notifies all registered observers with a sensor event.
     * Called internally by subclasses when detection occurs.
     * @param event the event to broadcast
     */
    protected void notifyObservers(SensorEvent event) {
        for (SensorObserver observer : observers) {
            observer.onSensorEvent(event);
        }
    }

    /**
     * Runs the detection logic specific to this sensor type.
     * Must create a SensorEvent and call notifyObservers() when relevant.
     */
    public abstract void detect();

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + id + "] on " + monitoredElement.getName();
    }
}
