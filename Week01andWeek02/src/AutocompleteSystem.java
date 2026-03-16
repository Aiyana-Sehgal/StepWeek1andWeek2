import java.util.*;

class TrieNode {
    Map<Character, TrieNode> children = new HashMap<>();
    boolean end;
}

class AutocompleteSystem {

    TrieNode root = new TrieNode();
    HashMap<String, Integer> freq = new HashMap<>();

    void insert(String query) {
        TrieNode node = root;
        for (char c : query.toCharArray()) {
            node.children.putIfAbsent(c, new TrieNode());
            node = node.children.get(c);
        }
        node.end = true;
    }

    void addQuery(String query) {
        insert(query);
        freq.put(query, freq.getOrDefault(query, 0) + 1);
    }

    TrieNode findPrefix(String prefix) {
        TrieNode node = root;
        for (char c : prefix.toCharArray()) {
            if (!node.children.containsKey(c)) return null;
            node = node.children.get(c);
        }
        return node;
    }

    void dfs(TrieNode node, String prefix, List<String> results) {
        if (node.end) results.add(prefix);

        for (char c : node.children.keySet()) {
            dfs(node.children.get(c), prefix + c, results);
        }
    }

    List<String> search(String prefix) {
        TrieNode node = findPrefix(prefix);
        if (node == null) return new ArrayList<>();

        List<String> candidates = new ArrayList<>();
        dfs(node, prefix, candidates);

        PriorityQueue<String> pq = new PriorityQueue<>(
                (a, b) -> freq.get(a) - freq.get(b)
        );

        for (String q : candidates) {
            pq.offer(q);
            if (pq.size() > 10) pq.poll();
        }

        List<String> result = new ArrayList<>();
        while (!pq.isEmpty()) result.add(pq.poll());

        Collections.reverse(result);
        return result;
    }

    void updateFrequency(String query) {
        addQuery(query);
    }

    public static void main(String[] args) {

        AutocompleteSystem system = new AutocompleteSystem();

        system.addQuery("java tutorial");
        system.addQuery("javascript");
        system.addQuery("java download");
        system.addQuery("java 21 features");
        system.addQuery("java 21 features");

        List<String> suggestions = system.search("jav");

        int rank = 1;
        for (String s : suggestions) {
            System.out.println(rank + ". " + s + " (" + system.freq.get(s) + " searches)");
            rank++;
        }

        system.updateFrequency("java 21 features");
        System.out.println("Updated Frequency: " + system.freq.get("java 21 features"));
    }
}