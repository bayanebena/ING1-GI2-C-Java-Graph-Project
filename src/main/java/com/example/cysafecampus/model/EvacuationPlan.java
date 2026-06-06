package com.example.cysafecampus.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the full evacuation plan built by AdminAgent.
 * Aggregates all EvacuationOrders, one per room to evacuate.
 * Calling execute() dispatches each order to the relevant SupervisorAgent.
 */
public class EvacuationPlan implements Serializable {

    /** All individual evacuation orders composing this plan */
    private List<EvacuationOrder> orders;

    public EvacuationPlan() {
        this.orders = new ArrayList<>();
    }

    /**
     * Adds an order to the plan.
     * @param order the evacuation order to include
     */
    public void addOrder(EvacuationOrder order) {
        orders.add(order);
    }

    public List<EvacuationOrder> getOrders() { return orders; }

    /**
     * Executes all orders in the plan — prints a summary of each.
     * In a full implementation, this would dispatch orders to supervisors.
     */
    public void execute() {
        System.out.println("=== Executing evacuation plan (" + orders.size() + " orders) ===");
        for (EvacuationOrder order : orders) {
            System.out.println("  Evacuating: " + order);
        }
    }

    @Override
    public String toString() {
        return "EvacuationPlan[" + orders.size() + " orders]";
    }
}
