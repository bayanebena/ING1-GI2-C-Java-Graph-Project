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
            // Do not make every person panicked: a realistic crowd contains calm
            // and stressed occupants. RUDE agents panic more often, POLITE agents
            // usually stay calm, and FOLLOWER agents are in between.
            if (shouldPanic()) {
                setState(AgentState.PANICKED);
            } else {
                setState(AgentState.CALM);
            }

            // Keep the progressive movement strategy. PanicStrategy used to move
            // the agent directly into the next passage before drawing progress,
            // which created visual jumps when the alarm was triggered.
            setStrategy(new EvacuateStrategy());
            setPath(new java.util.ArrayList<>());
            setProgress(0.0);
        } else if (alert.equals("NORMAL")) {
            setState(AgentState.CALM);
            setStrategy(new EvacuateStrategy());
            setPath(new java.util.ArrayList<>());
            setProgress(0.0);
        }
    }

    /**
     * Decides whether this person becomes panicked during an alert.
     * @return true when the person should be displayed as panicked
     */
    private boolean shouldPanic() {
        double panicProbability;

        if (getBehavior() == Behavior.RUDE) {
            panicProbability = 0.70;
        } else if (getBehavior() == Behavior.FOLLOWER) {
            panicProbability = 0.45;
        } else {
            panicProbability = 0.25;
        }

        return Math.random() < panicProbability;
    }
}
