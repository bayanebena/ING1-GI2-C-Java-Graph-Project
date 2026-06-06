package com.example.cysafecampus.model;

/**
 * Enum representing the emotional/physical state of an agent.
 * The state influences movement strategy selection.
 */
public enum AgentState {
    /** Agent is calm and follows instructions normally */
    CALM,

    /** Agent is panicking — may ignore optimal routes */
    PANICKED
}
