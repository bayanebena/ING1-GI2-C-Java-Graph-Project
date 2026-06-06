package com.example.cysafecampus.model;

import java.util.*;

/**
 * Utility class responsible for computing navigation routes.
 * Implements Dijkstra's algorithm adapted for multiple criteria.
 */
public class PathFinder {

    /**
     * Helper class to store a node and its accumulated cost in the PriorityQueue.
     */
    private static class NodeRecord implements Comparable<NodeRecord> {
        BuildingElement element;
        double cost;

        NodeRecord(BuildingElement element, double cost) {
            this.element = element;
            this.cost = cost;
        }

        @Override
        public int compareTo(NodeRecord other) {
            return Double.compare(this.cost, other.cost);
        }
    }

    /**
     * Calculates the shortest path based strictly on physical distance.
     */
    public static List<BuildingElement> calculateShortestPath(BuildingElement start, BuildingElement destination) {
        return findPath(start, destination, 0.0, false);
    }

    /**
     * Calculates the fastest path factoring in distance, agent speed, and edge congestion.
     */
    public static List<BuildingElement> calculateFastestPath(BuildingElement start, BuildingElement destination, double agentSpeed) {
        return findPath(start, destination, agentSpeed, true);
    }

    /**
     * Core Dijkstra algorithm implementation.
     * * @param start The starting building element.
     * @param destination The target building element.
     * @param agentSpeed The max speed of the agent.
     * @param useTimeAndCongestion Flag: true to use time/congestion costs, false for physical distance.
     * @return A list of {@link BuildingElement} representing the optimal path,
     * or an empty list if no path is found.
     */
    private static List<BuildingElement> findPath(BuildingElement start, BuildingElement destination, double agentSpeed, boolean useTimeAndCongestion) {
        PriorityQueue<NodeRecord> queue = new PriorityQueue<>();
        Map<BuildingElement, Double> minCosts = new HashMap<>();
        Map<BuildingElement, BuildingElement> previousNodes = new HashMap<>();

        queue.add(new NodeRecord(start, 0.0));
        minCosts.put(start, 0.0);

        while (!queue.isEmpty()) {
            NodeRecord currentRecord = queue.poll();
            BuildingElement currentElement = currentRecord.element;

            // Arrivée à destination
            if (currentElement.equals(destination)) {
                return reconstructPath(previousNodes, destination);
            }

            // Pour chaque voisin accessible
            for (BuildingElement neighbor : getNeighbors(currentElement)) {
                // Si l'élément est bloqué (ex: en feu), on l'ignore totalement
                if (neighbor.isBlocked()) continue;

                double edgeCost = calculateCost(currentElement, neighbor, agentSpeed, useTimeAndCongestion);
                double newTotalCost = currentRecord.cost + edgeCost;

                if (newTotalCost < minCosts.getOrDefault(neighbor, Double.MAX_VALUE)) {
                    minCosts.put(neighbor, newTotalCost);
                    previousNodes.put(neighbor, currentElement);
                    queue.add(new NodeRecord(neighbor, newTotalCost));
                }
            }
        }

        // Aucun chemin trouvé
        return new ArrayList<>();
    }

    /**
     * Retrieves all accessible neighboring elements (Room <-> Passage via open Doors).
     */
    private static List<BuildingElement> getNeighbors(BuildingElement current) {
        List<BuildingElement> neighbors = new ArrayList<>();

        if (current instanceof Room) {
            // Room and Exit (subclasses of BuildingElement) share the same door model
            for (Door door : ((Room) current).getDoors()) {
                if (door.isOpen()) {
                    neighbors.add(door.getPassage());
                }
            }
        } else if (current instanceof Passage) {
            for (Door door : ((Passage) current).getConnectedDoors()) {
                if (door.isOpen()) {
                    // The door's room may be a regular Room or a junction Room
                    neighbors.add(door.getRoom());
                }
            }
        }
        // Exit is treated as a terminal node — no outgoing neighbors needed
        return neighbors;
    }

    /**
     * Calculates the cost to move from current to neighbor.
     */
    private static double calculateCost(BuildingElement current, BuildingElement neighbor, double agentSpeed, boolean useTimeAndCongestion) {
        double distance = 1.0; // Coût de base arbitraire pour entrer dans une salle
        double speedFactor = 1.0;
        double congestion = 0.0;

        if (neighbor instanceof Passage) {
            Passage p = (Passage) neighbor;
            distance = p.getDistance();

            if (useTimeAndCongestion) {
                speedFactor = p.getSpeedFactor();
                // Calcul de la congestion : C = occupation / capacité
                congestion = (double) p.getCurrentOccupancy() / p.getMaxCapacity();
                if (congestion >= 1.0) congestion = 0.99; // Évite la division par zéro mathématique
            }
        }

        if (!useTimeAndCongestion) {
            return distance; // Mode Shortest Path
        } else {
            // Mode Fastest Path (Calcul du Temps)
            double effectiveSpeed = agentSpeed * speedFactor * (1.0 - congestion);
            if (effectiveSpeed <= 0) return Double.MAX_VALUE; // Impossible d'avancer

            return distance / effectiveSpeed;
        }
    }

    /**
     * Backtracks from the destination to the start to build the path list.
     */
    private static List<BuildingElement> reconstructPath(Map<BuildingElement, BuildingElement> previousNodes, BuildingElement target) {
        List<BuildingElement> path = new ArrayList<>();
        BuildingElement current = target;
        while (current != null) {
            path.add(0, current); // Ajoute au début de la liste
            current = previousNodes.get(current);
        }
        return path;
    }
}