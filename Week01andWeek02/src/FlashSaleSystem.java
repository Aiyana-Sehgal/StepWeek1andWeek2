import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

class Product {
    String id;
    AtomicInteger stock;
    ConcurrentLinkedQueue<String> waitingList = new ConcurrentLinkedQueue<>();

    Product(String id, int stock) {
        this.id = id;
        this.stock = new AtomicInteger(stock);
    }
}

class InventorySystem {

    ConcurrentHashMap<String, Product> inventory = new ConcurrentHashMap<>();

    void addProduct(String id, int stock) {
        inventory.put(id, new Product(id, stock));
    }

    boolean checkAvailability(String id) {
        Product p = inventory.get(id);
        if (p == null) return false;
        return p.stock.get() > 0;
    }

    String purchase(String id, String user) {
        Product p = inventory.get(id);
        if (p == null) return "Product not found";

        while (true) {
            int current = p.stock.get();
            if (current <= 0) {
                p.waitingList.add(user);
                return "Added to waiting list";
            }
            if (p.stock.compareAndSet(current, current - 1)) {
                return "Purchase successful";
            }
        }
    }

    int getStock(String id) {
        Product p = inventory.get(id);
        if (p == null) return -1;
        return p.stock.get();
    }

    List<String> getWaitingList(String id) {
        Product p = inventory.get(id);
        if (p == null) return new ArrayList<>();
        return new ArrayList<>(p.waitingList);
    }
}

public class FlashSaleSystem {

    public static void main(String[] args) throws InterruptedException {

        InventorySystem system = new InventorySystem();
        system.addProduct("ITEM1", 100);

        ExecutorService executor = Executors.newFixedThreadPool(100);

        for (int i = 1; i <= 50000; i++) {
            String user = "User" + i;
            executor.execute(() -> {
                System.out.println(system.purchase("ITEM1", user));
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        System.out.println("Remaining Stock: " + system.getStock("ITEM1"));
        System.out.println("Waiting List Size: " + system.getWaitingList("ITEM1").size());
    }
}