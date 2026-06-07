package com.example.cysafecampus.controller;

import com.example.cysafecampus.model.*;
import java.util.function.Consumer;

import javafx.application.Platform;

/**
 * Controller linking the Graph model to the GraphView (MVC pattern).
 * Also owns the SimulationEngine and exposes play/pause/step/speed controls.
 */
public class GraphController {

    private final Graph graph;
    
    private final SimulationEngine engine;

    /** Called when a sensor fires an event — used by AdminView to update the log */
    private Consumer<SensorEvent> sensorEventCallback;

    /**
     * Constructor — builds the default campus graph and wires the simulation engine.
     * @param graph the graph model
     * @param view the main view
     */
    public GraphController(Graph graph) {
        this.graph = graph;
        
        this.engine = new SimulationEngine(graph);

        // On each tick, redraw on the JavaFX thread
        

        initGraph();
    }

    // ── Simulation Controls ───────────────────────────────

    /** Starts the simulation loop. */
    public void play() { engine.play(); }

    /** Pauses the simulation loop. */
    public void pause() { engine.pause(); }

    /** Executes one tick (step mode). */
    public void step() { engine.step(); }

    /** Sets the tick interval in ms (50–2000). */
    public void setSpeed(int intervalMs) { engine.setIntervalMs(intervalMs); }

    public boolean isRunning() { return engine.isRunning(); }
    public long getTickCount() { return engine.getTickCount(); }

    // ── Alert Controls ────────────────────────────────────

    /**
     * Triggers a fire alert: notifies all agents, updates the view.
     */
    public void triggerFireAlert() {
        graph.triggerAlert("FIRE");
        // Redirect all agents toward nearest exit
        for (Agent agent : graph.getAgents()) {
            Exit nearest = findNearestExit(agent.getCurrentLocation());
            if (nearest != null) {
                agent.setDestination(nearest);
                agent.setPath(new java.util.ArrayList<>());
            }
        }
        
    }

    /**
     * Resets all agents to CALM state and clears fire alert.
     */
    public void reset() {
        graph.triggerAlert("NORMAL");
        engine.pause();
        
    }

    // ── Save / Load ───────────────────────────────────────

    /**
     * Saves the current simulation state to a binary file.
     * @param filePath destination path
     */
    public void saveSimulation(String filePath) {
        try {
            engine.pause();
            SimulationSerializer.save(graph, filePath);
            System.out.println("Simulation saved to: " + filePath);
        } catch (Exception e) {
            System.err.println("Save failed: " + e.getMessage());
        }
    }

    /**
     * Loads a simulation state from a binary file.
     * Note: creates a new graph from file — view must be refreshed after.
     * @param filePath source path
     */
    public void loadSimulation(String filePath) {
        try {
            engine.pause();
            Graph loaded = SimulationSerializer.load(filePath);
            graph.getElements().clear();
            graph.getPassages().clear();
            graph.getAgents().clear();
            graph.getSensors().clear();
            graph.getElements().addAll(loaded.getElements());
            graph.getPassages().addAll(loaded.getPassages());

            for (BuildingElement el : graph.getElements()) {
                el.setCurrentOccupancy(0);
            }

            for (Agent a : loaded.getAgents()) {
                graph.addAgent(a);
            }

            for (Sensor s : loaded.getSensors()) {
                graph.addSensor(s);
            }
            
        } catch (Exception e) {
            System.err.println("Load failed: " + e.getMessage());
        }
    }

    // ── Node Management ───────────────────────────────────

    /**
     * Adds a node to the graph model.
     */
    public void addNode(String name, String type, double x, double y) {
        if (type.equalsIgnoreCase("Salle")) {
            graph.addElement(new Room(name, 30, 1));
        } else if (type.equalsIgnoreCase("Sortie")) {
            Exit exit = new Exit(name, 100);
            graph.addElement(exit);
        } else if (type.equalsIgnoreCase("Couloir")) {
            Passage passage = new Passage(name, 50, 1, 1.0, PassageType.CORRIDOR, 10.0);
            graph.addElement(passage);
            graph.addPassage(passage);
        }
    }

    /**
     * Removes a node and safely relocates agents that were on it.
     */
    public void removeNode(String name) {
        BuildingElement toRemove = findElementByName(name);
        if (toRemove == null) return;

        // 1. Find adjacent element for agent relocation BEFORE removing
        BuildingElement fallback = findAdjacentElement(toRemove);

        // 2. Relocate all agents currently on this element
        for (Agent agent : new java.util.ArrayList<>(graph.getAgents())) {
            if (agent.getCurrentLocation().equals(toRemove)) {
                if (fallback != null) {
                    toRemove.agentLeaves();
                    fallback.agentEnters(agent.getMaxSpeed());
                    agent.setCurrentLocation(fallback);
                    agent.setProgress(0.0);
                    agent.setPath(new java.util.ArrayList<>());
                    agent.setDestination(null);
                } else {
                    graph.removeAgent(agent);
                    continue;
                }
            }
            // Clear path if it references the removed node
            if (agent.getPath() != null && agent.getPath().contains(toRemove)) {
                agent.setPath(new java.util.ArrayList<>());
                agent.setDestination(null);
                agent.setProgress(0.0);
            }
        }

        // 3. Remove all connected doors/edges
        if (toRemove instanceof Room) {
            Room room = (Room) toRemove;
            for (Door door : new java.util.ArrayList<>(room.getDoors())) {
                door.getPassage().getConnectedDoors().remove(door);
            }
            room.getDoors().clear();
        } else if (toRemove instanceof Passage) {
            Passage passage = (Passage) toRemove;
            for (Door door : new java.util.ArrayList<>(passage.getConnectedDoors())) {
                door.getRoom().getDoors().remove(door);
            }
            passage.getConnectedDoors().clear();
            graph.removePassage(passage);
        }

        // 4. Also remove junction nodes linked to this element
        graph.getElements().removeIf(el ->
            el.getName().contains("↔") && el.getName().contains(name));

        graph.removeElement(toRemove);
    }

    /** Updates a node's name and capacity. */
    public void updateNode(String oldName, String newName, int newCapacity) {
        BuildingElement el = findElementByName(oldName);
        if (el != null) {
            el.setName(newName);
            el.setMaxCapacity(newCapacity);
        }
    }

    /** Adds random nodes and connects them. */
    public void addRandomNodes(int count) {
        for (int i = 0; i < count; i++) {
            String name = "Node_" + System.currentTimeMillis() + "_" + i;
            int randomType = (int) (Math.random() * 3);
            if (randomType == 0) {
                graph.addElement(new Room(name, 30, 1));
            } else if (randomType == 1) {
                Passage p = new Passage(name, 50, 1, 1.0, PassageType.CORRIDOR, 10.0);
                graph.addElement(p);
                graph.addPassage(p);
            } else {
                graph.addElement(new Exit(name, 100));
            }
        }
        // Random edges
        var rooms = graph.getElements().stream()
            .filter(e -> e instanceof Room).map(e -> (Room) e).toList();
        var passages = graph.getPassages();
        for (Room room : rooms) {
            if (!passages.isEmpty() && Math.random() > 0.5) {
                Passage rp = passages.get((int) (Math.random() * passages.size()));
                Door door = new Door(room, rp);
                room.addDoor(door);
                rp.addDoor(door);
            }
        }
    }

    // ── Edge Management ───────────────────────────────────

    /** Adds an edge (Door) between a Room and a Passage. */
    public void addEdge(String roomName, String passageName) {
        Room room = null;
        Passage passage = null;
        for (BuildingElement el : graph.getElements()) {
            if (el instanceof Room && el.getName().equals(roomName)) room = (Room) el;
            if (el instanceof Passage && el.getName().equals(passageName)) passage = (Passage) el;
        }
        if (room != null && passage != null) {
            Door door = new Door(room, passage);
            room.addDoor(door);
            passage.addDoor(door);
        }
    }

    /**
     * Removes the edge (Door) between a Room and a Passage.
     * Agents currently in the Passage are relocated to the Room source node,
     * as required by the specification.
     * @param roomName the room side of the edge
     * @param passageName the passage side of the edge
     */
    public void removeEdge(String roomName, String passageName) {
        BuildingElement roomEl = findElementByName(roomName);
        BuildingElement passageEl = findElementByName(passageName);

        // Relocate agents currently in the passage back to the room (source node)
        if (passageEl != null && roomEl != null) {
            for (Agent agent : new java.util.ArrayList<>(graph.getAgents())) {
                if (agent.getCurrentLocation().equals(passageEl)) {
                    passageEl.agentLeaves();
                    roomEl.agentEnters(agent.getMaxSpeed());
                    agent.setCurrentLocation(roomEl);
                    agent.setPath(new java.util.ArrayList<>());
                    agent.setDestination(null);
                }
                // Clear paths that go through this passage
                if (agent.getPath() != null && agent.getPath().contains(passageEl)) {
                    agent.setPath(new java.util.ArrayList<>());
                    agent.setDestination(null);
                }
            }
        }

        // Remove the door from both sides
        for (BuildingElement el : graph.getElements()) {
            if (el instanceof Passage && el.getName().equals(passageName)) {
                ((Passage) el).getConnectedDoors().removeIf(
                    door -> door.getRoom().getName().equals(roomName));
            }
        }
        // Remove from room side too
        if (roomEl instanceof Room) {
            ((Room) roomEl).getDoors().removeIf(
                door -> door.getPassage().getName().equals(passageName));
        }
    }

    // ── Agent Management ──────────────────────────────────

    /** Adds a Person agent at the given location. */
    public void addPersonAgent(String name, String locationName,
                               double maxSpeed, Behavior behavior, double densityTolerance) {
        BuildingElement location = findElementByName(locationName);
        if (location != null) {
            Person person = new Person(name, location, maxSpeed, behavior, densityTolerance);
            person.setStrategy(new EvacuateStrategy());
            graph.addAgent(person);
        }
    }

    /** Removes an agent by name. */
    public void removeAgent(String agentName) {
        graph.getAgents().stream()
            .filter(a -> a.getName().equals(agentName))
            .findFirst()
            .ifPresent(graph::removeAgent);
    }

    /** Updates agent properties. */
    public void updateAgent(String oldName, String newName, String locationName,
                            double maxSpeed, Behavior behavior, double densityTolerance) {
        BuildingElement location = findElementByName(locationName);
        graph.getAgents().stream()
            .filter(a -> a.getName().equals(oldName))
            .findFirst()
            .ifPresent(agent -> {
                agent.setName(newName);

                if (location != null && agent.getCurrentLocation() != location) {
                    if (agent.getCurrentLocation() != null) {
                        agent.getCurrentLocation().agentLeaves();
                    }
                    location.agentEnters(maxSpeed);
                    agent.setCurrentLocation(location);
                }

                agent.setMaxSpeed(maxSpeed);
                agent.setBehavior(behavior);
                agent.setDensityTolerance(densityTolerance);
                agent.setPath(new java.util.ArrayList<>());
                agent.setDestination(null);
                agent.setProgress(0.0);
            });
    }

    /** Adds random agents with speed and tolerance within given ranges. */
    public void addRandomAgents(int count, double minSpeed, double maxSpeed,
                                double minTolerance, double maxTolerance) {
        var elements = graph.getElements();
        if (elements.isEmpty()) return;
        Behavior[] behaviors = Behavior.values();
        for (int i = 0; i < count; i++) {
            String name = "Agent_" + System.currentTimeMillis() + "_" + i;
            BuildingElement loc = elements.get((int) (Math.random() * elements.size()));
            double speed = minSpeed + Math.random() * (maxSpeed - minSpeed);
            double tolerance = minTolerance + Math.random() * (maxTolerance - minTolerance);
            Behavior b = behaviors[(int) (Math.random() * behaviors.length)];
            Person rp = new Person(name, loc, speed, b, tolerance);
            rp.setStrategy(new EvacuateStrategy());
            graph.addAgent(rp);
        }
    }

    // ── Destination management ───────────────────────────

    /**
     * Assigns a random accessible destination to an agent.
     * Called after arrival to keep agents wandering during non-alert simulation.
     * @param agent the agent needing a new destination
     */
    public void assignRandomDestination(Agent agent) {
        var elements = graph.getElements().stream()
            .filter(el -> !el.isBlocked() && !el.getName().contains("↔"))
            .filter(el -> !el.equals(agent.getCurrentLocation()))
            .toList();
        if (!elements.isEmpty()) {
            BuildingElement dest = elements.get((int)(Math.random() * elements.size()));
            agent.setDestination(dest);
            agent.setPath(new java.util.ArrayList<>());
            // Always ensure agent has a strategy to actually move
            if (agent.getStrategy() == null) {
                agent.setStrategy(new EvacuateStrategy());
            }
        }
    }

    // ── Getters ───────────────────────────────────────────

    public Graph getGraph() { return graph; }

    // ── Helpers ───────────────────────────────────────────

    private BuildingElement findElementByName(String name) {
        return graph.getElements().stream()
            .filter(el -> el.getName().equals(name))
            .findFirst().orElse(null);
    }

    /**
     * Finds any accessible element adjacent to the given one via passages.
     */
    private BuildingElement findAdjacentElement(BuildingElement element) {
        for (Passage p : graph.getPassages()) {
            for (Door d : p.getConnectedDoors()) {
                if (d.getRoom().equals(element)) return p;
                if (d.getPassage().equals(element)) return d.getRoom();
            }
        }
        return null;
    }

    /**
     * Finds the nearest Exit from a given element using PathFinder.
     */
    private Exit findNearestExit(BuildingElement from) {
        Exit nearest = null;
        int best = Integer.MAX_VALUE;
        for (BuildingElement el : graph.getElements()) {
            if (el instanceof Exit) {
                var path = PathFinder.calculateShortestPath(from, el);
                if (!path.isEmpty() && path.size() < best) {
                    best = path.size();
                    nearest = (Exit) el;
                }
            }
        }
        return nearest;
    }

    /**
     * Registers a callback called on each sensor event.
     * Used by AdminView to display real-time sensor alerts.
     */
    public void setSensorEventCallback(Consumer<SensorEvent> callback) {
        this.sensorEventCallback = callback;
        // Wire AdminAgent as SensorObserver on all sensors
        graph.getSensors().forEach(sensor ->
            sensor.addObserver(event -> {
                if (callback != null) callback.accept(event);
            }));
    }

    /**
     * Initializes the campus graph from the real CY Tech floor plan.
     */
    private void initGraph() {
        // ── Rooms ──────────────────────────────────────────
        Room storage     = new Room("Réserve",       20, 1, RoomType.OFFICE);
        Room office1     = new Room("Bureau 1",      15, 1, RoomType.OFFICE);
        Room office2     = new Room("Bureau 2",      15, 1, RoomType.OFFICE);
        Room office3     = new Room("Bureau 3",      15, 1, RoomType.OFFICE);
        Room serverRoom  = new Room("LT Serveurs",   10, 1, RoomType.OFFICE);
        Room amphitheater       = new Room("Amphithéâtre", 200, 1, RoomType.AMPHITHEATER);
        Room housing    = new Room("Logement",      10, 1, RoomType.OFFICE);

        // ── Passages ───────────────────────────────────────
        Passage mainHall = new Passage("Hall Central",  100, 1, 1.0, PassageType.HALL,     20.0);
        Passage northCorridor    = new Passage("Couloir Nord",   40, 1, 1.0, PassageType.CORRIDOR, 10.0);
        Passage southCorridor    = new Passage("Couloir Sud",    40, 1, 1.0, PassageType.CORRIDOR, 10.0);
        Passage staircase1   = new Passage("Escalier 1",    20, 1, 0.6, PassageType.STAIRCASE,  8.0);
        Passage staircase2   = new Passage("Escalier 2",    20, 1, 0.6, PassageType.STAIRCASE,  8.0);

        // ── Exits ──────────────────────────────────────────
        Exit exitEast1 = new Exit("Sortie Est 1", 50);
        Exit exitEast2 = new Exit("Sortie Est 2", 50);
        Exit exitEast3 = new Exit("Sortie Est 3", 50);
        Exit exitWest = new Exit("Sortie Ouest", 50);

        // ── Add to graph ───────────────────────────────────
        for (BuildingElement el : new BuildingElement[]{
            storage, office1, office2, office3, serverRoom, amphitheater, housing,
            mainHall, northCorridor, southCorridor, staircase1, staircase2,
            exitEast1, exitEast2, exitEast3, exitWest
        }) { graph.addElement(el); }

        graph.addPassage(mainHall);
        graph.addPassage(northCorridor);
        graph.addPassage(southCorridor);
        graph.addPassage(staircase1);
        graph.addPassage(staircase2);

        // ── Doors (connections) ────────────────────────────
        connectRoomToPassage(storage,    staircase1);
        connectRoomToPassage(office1,    northCorridor);
        connectRoomToPassage(office2,    northCorridor);
        connectRoomToPassage(office3,    mainHall);
        connectRoomToPassage(serverRoom, mainHall);
        connectRoomToPassage(amphitheater, southCorridor);
        connectRoomToPassage(housing,    southCorridor);

        connectRoomToPassage(exitEast1,  mainHall);
        connectRoomToPassage(exitEast2,  mainHall);
        connectRoomToPassage(exitEast3,  mainHall);
        connectRoomToPassage(exitWest,   southCorridor);

        // Hall central connects to all corridors
        connectPassageToPassage(mainHall, northCorridor);
        connectPassageToPassage(mainHall, southCorridor);
        connectPassageToPassage(mainHall, staircase2);
        connectPassageToPassage(northCorridor, staircase1);

        // ── Sensors (one per room)
        PresenceSensor ps1 = new PresenceSensor("PS-Amphi",   amphitheater);
        PresenceSensor ps2 = new PresenceSensor("PS-B1",      office1);
        SmokeSensor    ss1 = new SmokeSensor("SS-LT",         serverRoom, 30.0);
        SmokeSensor    ss2 = new SmokeSensor("SS-Hall",       mainHall, 40.0);
        for (var s : new Sensor[]{ps1, ps2, ss1, ss2}) graph.addSensor(s);

        // ── Agents ─────────────────────────────────────────
        Person p1 = new Person("Lucas",  office1, 1.0, Behavior.POLITE,    0.6);
        Person p2 = new Person("Samia",  office2, 1.2, Behavior.FOLLOWER,  0.8);
        Person p3 = new Person("Theo",   amphitheater,   0.9, Behavior.RUDE,      0.4);
        Person p4 = new Person("Ines",   office3, 1.1, Behavior.POLITE,    0.7);
        SecurityAgent sec = new SecurityAgent("Agent 01", mainHall, mainHall);

        // Give agents a strategy and starting destination so they move immediately
        p1.setStrategy(new EvacuateStrategy()); p1.setDestination(exitEast1);
        p2.setStrategy(new EvacuateStrategy()); p2.setDestination(exitEast2);
        p3.setStrategy(new EvacuateStrategy()); p3.setDestination(exitWest);
        p4.setStrategy(new EvacuateStrategy()); p4.setDestination(exitEast3);

        for (Agent a : new Agent[]{p1, p2, p3, p4, sec}) graph.addAgent(a);
    }

    /**
     * Helper: creates a bidirectional door between a room-like element and a passage.
     */
    private void connectRoomToPassage(BuildingElement roomLike, Passage passage) {
        // Exit now extends Room, so this handles both Room and Exit correctly
        if (roomLike instanceof Room) {
            Door door = new Door((Room) roomLike, passage);
            ((Room) roomLike).addDoor(door);
            passage.addDoor(door);
        }
    }

    /**
     * Helper: connects two passages via a shared virtual door (corridor junction).
     * Stored as a Room-less door to indicate passage adjacency.
     */
    private void connectPassageToPassage(Passage a, Passage b) {
        // Passages connect to each other when they share a physical junction.
        // We model this by adding each passage to the other's neighbor list.
        // PathFinder.getNeighbors() already handles Passage→Passage adjacency
        // if we add a junction Room, OR we extend PathFinder to check passage lists.
        // Simplest: add a tiny junction room as intermediary.
        String junctionName = a.getName() + "↔" + b.getName();
        Room junction = new Room(junctionName, 999, 1);
        junction.setStatus(BlockStatus.ACCESSIBLE);
        graph.addElement(junction);
        Door d1 = new Door(junction, a);
        Door d2 = new Door(junction, b);
        junction.addDoor(d1);
        junction.addDoor(d2);
        a.addDoor(d1);
        b.addDoor(d2);
    }
}
