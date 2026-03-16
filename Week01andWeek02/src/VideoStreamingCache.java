import java.util.*;

class VideoData {
    String id;
    String data;

    VideoData(String id, String data) {
        this.id = id;
        this.data = data;
    }
}

class LRUCache<K, V> extends LinkedHashMap<K, V> {
    int capacity;

    LRUCache(int capacity) {
        super(capacity, 0.75f, true);
        this.capacity = capacity;
    }

    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }
}

class MultiLevelCache {

    LRUCache<String, VideoData> L1 = new LRUCache<>(10000);
    LRUCache<String, VideoData> L2 = new LRUCache<>(100000);
    HashMap<String, VideoData> L3 = new HashMap<>();
    HashMap<String, Integer> accessCount = new HashMap<>();

    int l1Hits = 0, l2Hits = 0, l3Hits = 0;

    MultiLevelCache() {
        for (int i = 1; i <= 200000; i++) {
            String id = "video_" + i;
            L3.put(id, new VideoData(id, "data_" + i));
        }
    }

    VideoData getVideo(String id) {

        if (L1.containsKey(id)) {
            l1Hits++;
            accessCount.put(id, accessCount.getOrDefault(id, 0) + 1);
            System.out.println("L1 Cache HIT");
            return L1.get(id);
        }

        if (L2.containsKey(id)) {
            l2Hits++;
            VideoData v = L2.get(id);
            accessCount.put(id, accessCount.getOrDefault(id, 0) + 1);
            if (accessCount.get(id) > 2) {
                L1.put(id, v);
            }
            System.out.println("L2 Cache HIT → Promoted if popular");
            return v;
        }

        if (L3.containsKey(id)) {
            l3Hits++;
            VideoData v = L3.get(id);
            accessCount.put(id, 1);
            L2.put(id, v);
            System.out.println("L3 Database HIT → Added to L2");
            return v;
        }

        return null;
    }

    void invalidate(String id) {
        L1.remove(id);
        L2.remove(id);
        L3.remove(id);
        accessCount.remove(id);
    }

    void getStatistics() {

        int total = l1Hits + l2Hits + l3Hits;

        double l1Rate = total == 0 ? 0 : (l1Hits * 100.0) / total;
        double l2Rate = total == 0 ? 0 : (l2Hits * 100.0) / total;
        double l3Rate = total == 0 ? 0 : (l3Hits * 100.0) / total;

        System.out.println("L1: Hit Rate " + String.format("%.2f", l1Rate) + "%");
        System.out.println("L2: Hit Rate " + String.format("%.2f", l2Rate) + "%");
        System.out.println("L3: Hit Rate " + String.format("%.2f", l3Rate) + "%");
    }
}

public class VideoStreamingCache {

    public static void main(String[] args) {

        MultiLevelCache cache = new MultiLevelCache();

        cache.getVideo("video_123");
        cache.getVideo("video_123");
        cache.getVideo("video_999");
        cache.getVideo("video_999");
        cache.getVideo("video_999");

        cache.getStatistics();
    }
}