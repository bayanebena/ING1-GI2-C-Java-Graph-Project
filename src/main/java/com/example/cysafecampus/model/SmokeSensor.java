package com.example.cysafecampus.model;

/**
 * Sensor that detects smoke levels in a building element.
 * Triggers a SMOKE_DETECTED event when smoke exceeds the threshold.
 * Severity scales with smoke level relative to threshold.
 */
public class SmokeSensor extends Sensor {

    /** Current measured smoke level (arbitrary units, e.g. ppm) */
    private double smokeLevel;

    /** Level above which an alert is triggered */
    private double threshold;

    /**
     * Constructor for SmokeSensor.
     * @param id unique sensor identifier
     * @param monitoredElement the element to monitor
     * @param threshold smoke level triggering an alert
     */
    public SmokeSensor(String id, BuildingElement monitoredElement, double threshold) {
        super(id, monitoredElement);
        this.smokeLevel = 0.0;
        this.threshold = threshold;
    }

    public double getSmokeLevel() { return smokeLevel; }
    public double getThreshold() { return threshold; }

    /** Updates the current smoke reading (called each simulation tick). */
    public void setSmokeLevel(double smokeLevel) { this.smokeLevel = smokeLevel; }

    /**
     * Checks if smoke exceeds threshold and notifies observers.
     * Severity is proportional to how far above the threshold the reading is:
     * 1x → severity 2, 2x → severity 4, 3x+ → severity 5 (critical).
     */
    @Override
    public void detect() {
        if (smokeLevel >= threshold) {
            double ratio = smokeLevel / threshold;
            int severity = Math.min(5, (int) Math.ceil(ratio * 2));

            notifyObservers(new SensorEvent(
                SensorEventType.SMOKE_DETECTED,
                severity,
                getMonitoredElement()
            ));
        }
    }
}
