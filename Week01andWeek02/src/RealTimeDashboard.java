import java.util.*;
import java.util.concurrent.*;

class PageEvent {
    String url;
    String userId;
    String source;

    PageEvent(String url, String userId, String source) {
        this.url = url;
        this.userId = userId;
        this.source = source;
    }
}

class AnalyticsSystem {

    ConcurrentHashMap<String, Integer> pageViews = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, Set<String>> uniqueVisitors = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, Integer> sourceCount = new ConcurrentHashMap<>();

    void processEvent(PageEvent e) {
        pageViews.merge(e.url, 1, Integer::sum);

        uniqueVisitors.putIfAbsent(e.url, ConcurrentHashMap.newKeySet());
        uniqueVisitors.get(e.url).add(e.userId);

        sourceCount.merge(e.source, 1, Integer::sum);
    }

    void getDashboard() {
        PriorityQueue<Map.Entry<String, Integer>> pq =
                new PriorityQueue<>((a, b) -> b.getValue() - a.getValue());

        pq.addAll(pageViews.entrySet());

        int k = 10;
        int rank = 1;

        System.out.println("Top Pages:");

        while (!pq.isEmpty() && k-- > 0) {
            Map.Entry<String, Integer> e = pq.poll();
            String url = e.getKey();
            int views = e.getValue();
            int unique = uniqueVisitors.getOrDefault(url, Collections.emptySet()).size();

            System.out.println(rank + ". " + url + " - " + views + " views (" + unique + " unique)");
            rank++;
        }

        System.out.println("\nTraffic Sources:");
        for (String s : sourceCount.keySet()) {
            System.out.println(s + " : " + sourceCount.get(s));
        }
    }
}

public class RealTimeDashboard {

    public static void main(String[] args) throws Exception {

        AnalyticsSystem system = new AnalyticsSystem();

        ScheduledExecutorService dashboard = Executors.newScheduledThreadPool(1);

        dashboard.scheduleAtFixedRate(() -> {
            System.out.println("\n------ DASHBOARD ------");
            system.getDashboard();
        }, 5, 5, TimeUnit.SECONDS);

        Random r = new Random();
        String[] pages = {
                "/article/breaking-news",
                "/sports/championship",
                "/tech/ai",
                "/politics/election",
                "/health/fitness"
        };

        String[] sources = {"google", "facebook", "direct", "twitter"};

        for (int i = 0; i < 100000; i++) {
            String page = pages[r.nextInt(pages.length)];
            String user = "user_" + r.nextInt(50000);
            String source = sources[r.nextInt(sources.length)];

            system.processEvent(new PageEvent(page, user, source));
        }
    }
}