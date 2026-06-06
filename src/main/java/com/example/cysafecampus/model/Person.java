package com.example.cysafecampus.model;

/**
 * Represents an anonymous occupant (student, staff, teacher).
 * Persons do NOT move on their own — they wait until a SupervisorAgent
 * or AdminAgent triggers evacuation. This is realistic: people wait
 * for instructions before leaving.
 *
 * On FIRE alert : switches to PANICKED state (but still needs an order to move)
 * On guide order: receives EvacuateStrategy and starts moving
 */
public class Person extends Agent {

    /**
     * Constructor — no default strategy, Person is idle until ordered.
     */
    public Person(String name, BuildingElement currentLocation,
                  double maxSpeed, Behavior behavior, double densityTolerance) {
        super(name, currentLocation, maxSpeed, behavior, densityTolerance);
        // No strategy by default — Person waits for an order
        this.setStrategy(null);
    }

    /**
     * Reacts to building-wide alert.
     * Switches to PANICKED state but does NOT start moving automatically.
     * A supervisor must call guideOccupants() to trigger actual movement.
     *
     * Exception: if already has a strategy (was already guided), keep moving.
     */
    @Override
    public void update(String alert) {
        if (alert.equals("FIRE")) {
            setState(AgentState.PANICKED);
            // Switch to panic strategy if already moving
            if (getStrategy() != null) {
                setStrategy(new PanicStrategy());
                setPath(new java.util.ArrayList<>());
            }
        } else if (alert.equals("NORMAL")) {
            setState(AgentState.CALM);
            // Reset to evacuate strategy — do NOT set null (would freeze agents)
            if (getStrategy() != null) {
                setStrategy(new EvacuateStrategy());
                setPath(new java.util.ArrayList<>());
            }
        }
    }
}
