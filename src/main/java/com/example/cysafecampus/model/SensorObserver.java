package com.example.cysafecampus.model;

/**
 * Observer interface for the Sensor → AdminAgent notification chain.
 * Only AdminAgent implements this interface: sensors notify the admin,
 * and the admin then decides what to do with supervisors.
 */
public interface SensorObserver {

    /**
     * Called by a Sensor when an event is detected.
     * @param event the sensor event containing type, severity and location
     */
    void onSensorEvent(SensorEvent event);
}
