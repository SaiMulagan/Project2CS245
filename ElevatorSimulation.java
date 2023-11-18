import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class ElevatorSimulation {
    private int numberOfFloors;
    private int numberOfElevators;
    private double passengerProbability;
    private int simulationDuration;
    private List<Floor> floors;
    private List<Elevator> elevators;
    private Random random;
    private int currentTick;
    private int elevatorCapacity;

    public ElevatorSimulation(String propertiesFilePath) {
        PropertyManager propertyManager = new PropertyManager(propertiesFilePath);
        this.numberOfFloors = Integer.parseInt(propertyManager.getProperty("floors"));
        this.numberOfElevators = Integer.parseInt(propertyManager.getProperty("elevators"));
        this.passengerProbability = Double.parseDouble(propertyManager.getProperty("passengers"));
        this.simulationDuration = Integer.parseInt(propertyManager.getProperty("duration"));
        this.elevatorCapacity = Integer.parseInt(propertyManager.getProperty("elevatorCapacity"));
        this.floors = new ArrayList<>();
        this.elevators = new ArrayList<>();
        this.random = new Random();
        this.currentTick = 0;

        // Initialize floors and elevators
        for (int i = 0; i < numberOfFloors; i++) {
            floors.add(new Floor(i));
        }
        for (int i = 0; i < numberOfElevators; i++) {
            elevators.add(new Elevator(elevatorCapacity, numberOfFloors));
        }
    }

    // Method to start the simulation
    public void startSimulation() {
        System.out.println("Starting simulation.");
        while (currentTick < simulationDuration) {
            System.out.println("Tick: " + currentTick);
            generatePassengers();
            determineElevatorDirections();
            moveElevators();
            processElevatorStops();
            currentTick++;
        }
        System.out.println("Simulation ended.");
    }

    private void generatePassengers() {
        for (Floor floor : floors) {
            if (random.nextDouble() < passengerProbability) {
                int destinationFloor;
                do {
                    destinationFloor = random.nextInt(numberOfFloors);
                } while (destinationFloor == floor.getFloorNumber());

                Passenger passenger = new Passenger(floor.getFloorNumber(), destinationFloor, currentTick);
                floor.addPassenger(passenger);
                System.out.println("New passenger at floor " + floor.getFloorNumber() + " going to " + destinationFloor);
            }
        }
    }

    private void determineElevatorDirections() {
        for (Elevator elevator : elevators) {
            // If the elevator is not idle or has passengers, skip setting new direction
            if (elevator.getDirection() != Elevator.Direction.IDLE || !elevator.getPassengers().isEmpty()) {
                continue;
            }

            // Check if there are any passengers waiting on any floor
            if (noPassengersWaiting()) {
                elevator.setDirection(Elevator.Direction.IDLE);
                continue;
            }

            int nearestFloor = findNearestFloorWithPassengers(elevator.getCurrentFloor());
            if (nearestFloor == -1) {
                // No passengers waiting, keep the elevator idle or decide on a default behavior
                continue;
            }

            if (nearestFloor > elevator.getCurrentFloor()) {
                elevator.setDirection(Elevator.Direction.UP);
            } else if (nearestFloor < elevator.getCurrentFloor()) {
                elevator.setDirection(Elevator.Direction.DOWN);
            }
        }
    }
    private boolean noPassengersWaiting() {
        for (Floor floor : floors) {
            if (!floor.getUpQueue().isEmpty() || !floor.getDownQueue().isEmpty()) {
                return false;
            }
        }
        return true;
    }
    private int findNearestFloorWithPassengers(int currentFloor) {
        for (int i = 0; i < floors.size(); i++) {
            if (!floors.get(i).getUpQueue().isEmpty() || !floors.get(i).getDownQueue().isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    private void moveElevators() {
        for (Elevator elevator : elevators) {
            System.out.println("Elevator at floor " + elevator.getCurrentFloor() + " moving " + elevator.getDirection());
            elevator.move();
            System.out.println("Elevator now at floor " + elevator.getCurrentFloor());
        }
    }

    private void processElevatorStops() {
        for (Elevator elevator : elevators) {
            Floor currentFloor = floors.get(elevator.getCurrentFloor());
            System.out.println("Before unloading/loading: " + elevator.toString());

            // Unload passengers
            Queue<Passenger> unloadedPassengers = elevator.unloadPassengers();
            for (Passenger passenger : unloadedPassengers) {
                passenger.setTickArrived(currentTick);
            }

            // Load passengers
            if (elevator.getDirection() == Elevator.Direction.UP) {
                loadPassengers(elevator, currentFloor.getUpQueue());
            } else if (elevator.getDirection() == Elevator.Direction.DOWN) {
                loadPassengers(elevator, currentFloor.getDownQueue());
            }

            System.out.println("After unloading/loading: " + elevator.toString());
        }
    }

    private void loadPassengers(Elevator elevator, Queue<Passenger> queue) {
        while (!queue.isEmpty() && elevator.getPassengers().size() < elevator.getCapacity()) {
            Passenger passenger = queue.poll();
            elevator.loadPassenger(passenger);
            passenger.setTickBoarded(currentTick);
        }
    }

    // Main method to run the simulation
    public static void main(String[] args) {
        // Path to your properties file
        String propertiesFilePath = "path/to/your/properties.file";
        // Create and start the elevator simulation
        ElevatorSimulation simulation = new ElevatorSimulation(propertiesFilePath);
        simulation.startSimulation();
    }
}
