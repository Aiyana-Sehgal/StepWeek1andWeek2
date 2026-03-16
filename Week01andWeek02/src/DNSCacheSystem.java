import java.util.*;

class Entry {
    String domain;
    String ip;
    long expiry;

    Entry(String d, String i, long ttl) {
        domain = d;
        ip = i;
        expiry = System.currentTimeMillis() + ttl * 1000;
    }

    boolean expired() {
        return System.currentTimeMillis() > expiry;
    }
}

class DNSCache {

    private int capacity;
    private Map<String, Entry> map;
    private LinkedList<String>[] table;
    private int hits = 0;
    private int misses = 0;

    DNSCache(int capacity) {
        this.capacity = capacity;
        map = new HashMap<>();
        table = new LinkedList[capacity];
        for (int i = 0; i < capacity; i++) table[i] = new LinkedList<>();
    }

    private int hash(String domain) {
        return Math.abs(domain.hashCode()) % capacity;
    }

    private void remove(String domain) {
        Entry e = map.remove(domain);
        if (e != null) {
            int index = hash(domain);
            table[index].remove(domain);
        }
    }

    private void evictLRU() {
        String oldest = null;
        long time = Long.MAX_VALUE;

        for (Entry e : map.values()) {
            if (e.expiry < time) {
                time = e.expiry;
                oldest = e.domain;
            }
        }

        if (oldest != null) remove(oldest);
    }

    public String resolve(String domain) {
        int index = hash(domain);
        cleanExpired();

        if (map.containsKey(domain)) {
            Entry e = map.get(domain);
            if (!e.expired()) {
                hits++;
                return e.ip;
            }
            remove(domain);
        }

        misses++;
        String ip = upstreamDNS(domain);
        put(domain, ip, 10);
        return ip;
    }

    public void put(String domain, String ip, long ttl) {
        if (map.size() >= capacity) evictLRU();

        Entry e = new Entry(domain, ip, ttl);
        map.put(domain, e);

        int index = hash(domain);
        table[index].add(domain);
    }

    private void cleanExpired() {
        List<String> removeList = new ArrayList<>();

        for (Entry e : map.values()) {
            if (e.expired()) removeList.add(e.domain);
        }

        for (String d : removeList) remove(d);
    }

    private String upstreamDNS(String domain) {
        return "192.168.1." + (Math.abs(domain.hashCode()) % 255);
    }

    public void stats() {
        int total = hits + misses;
        double ratio = total == 0 ? 0 : (double) hits / total;

        System.out.println("Hits: " + hits);
        System.out.println("Misses: " + misses);
        System.out.println("Hit Ratio: " + ratio);
    }
}

public class DNSCacheSystem {
    public static void main(String[] args) throws Exception {

        DNSCache cache = new DNSCache(5);

        System.out.println(cache.resolve("google.com"));
        System.out.println(cache.resolve("openai.com"));
        System.out.println(cache.resolve("google.com"));
        System.out.println(cache.resolve("github.com"));
        System.out.println(cache.resolve("stackoverlow.com"));
        System.out.println(cache.resolve("google.com"));

        cache.stats();
    }
}