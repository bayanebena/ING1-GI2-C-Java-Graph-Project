package com.example.cysafecampus.model;

/**
 * Sensor that detects human presence in a building element.
 * Triggers an event when the element is occupied or overcrowded.
 */
public class PresenceSensor extends Sensor {

    /** Whether presence is currently detected */
    private boolean detectedPresence;

    /**
     * Constructor for PresenceSensor.
     * @param id unique sensor identifier
     * @param monitoredElement the element to monitor
     */
    public PresenceSensor(String id, BuildingElement monitoredElement) {
        super(id, monitoredElement);
        this.detectedPresence = false;
    }

    public boolean isDetectedPresence() { return detectedPresence; }

    /**
     * Reads the current occupancy of the monitored element.
     * Fires OVERCROWDING if at capacity, PRESENCE_DETECTED otherwise when occupied.
     */
    @Override
    public void detect() {
        BuildingElement element = getMonitoredElement();
        int occupancy = element.getCurrentOccupancy();
        setRealTimePeopleCount(occupancy);

        if (occupancy > 0) {
            detectedPresence = true;

            SensorEventType type;
            int severity;

            if (element.isFull()) {
                type = SensorEventType.OVERCROWDING;
                severity = 4;
            } else {
                type = SensorEventType.PRESENCE_DETECTED;
                severity = 1;
            }

            notifyObservers(new SensorEvent(type, severity, element));
        } else {
            detectedPresence = false;
        }
    }
}
