import java.util.*;

class ParkingSpot {
    String plate;
    long entryTime;
    boolean occupied;
    boolean deleted;
}

class ParkingLot {

    ParkingSpot[] table;
    int capacity;
    int size = 0;
    int probes = 0;
    int totalParks = 0;

    ParkingLot(int capacity) {
        this.capacity = capacity;
        table = new ParkingSpot[capacity];
        for (int i = 0; i < capacity; i++) table[i] = new ParkingSpot();
    }

    int hash(String plate) {
        return Math.abs(plate.hashCode()) % capacity;
    }

    void parkVehicle(String plate) {
        int index = hash(plate);
        int p = 0;

        while (table[index].occupied) {
            index = (index + 1) % capacity;
            p++;
        }

        table[index].plate = plate;
        table[index].entryTime = System.currentTimeMillis();
        table[index].occupied = true;
        table[index].deleted = false;

        size++;
        probes += p;
        totalParks++;

        System.out.println("parkVehicle(\"" + plate + "\") → Assigned spot #" + index + " (" + p + " probes)");
    }

    void exitVehicle(String plate) {
        int index = hash(plate);

        while (table[index].occupied || table[index].deleted) {
            if (table[index].occupied && plate.equals(table[index].plate)) {

                long durationMs = System.currentTimeMillis() - table[index].entryTime;
                long minutes = durationMs / 60000;
                double fee = minutes * 0.1;

                table[index].occupied = false;
                table[index].deleted = true;
                size--;

                System.out.println("exitVehicle(\"" + plate + "\") → Spot #" + index +
                        " freed, Duration: " + minutes + "m, Fee: $" + String.format("%.2f", fee));
                return;
            }

            index = (index + 1) % capacity;
        }

        System.out.println("Vehicle not found");
    }

    int findNearestSpot() {
        for (int i = 0; i < capacity; i++) {
            if (!table[i].occupied) return i;
        }
        return -1;
    }

    void getStatistics() {
        double occupancy = (size * 100.0) / capacity;
        double avgProbes = totalParks == 0 ? 0 : (double) probes / totalParks;

        System.out.println("getStatistics() → Occupancy: " +
                String.format("%.2f", occupancy) + "%, Avg Probes: " +
                String.format("%.2f", avgProbes));
    }
}

public class SmartParkingSystem {

    public static void main(String[] args) throws Exception {

        ParkingLot lot = new ParkingLot(500);

        lot.parkVehicle("ABC-1234");
        lot.parkVehicle("ABC-1235");
        lot.parkVehicle("XYZ-9999");

        Thread.sleep(2000);

        lot.exitVehicle("ABC-1234");

        lot.getStatistics();
    }
}