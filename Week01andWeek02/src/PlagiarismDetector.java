import java.util.*;

class PlagiarismDetector {

    int n = 5;
    HashMap<String, Set<String>> index = new HashMap<>();
    HashMap<String, Set<String>> docNgrams = new HashMap<>();

    List<String> generateNgrams(String text) {
        String[] words = text.toLowerCase().split("\\s+");
        List<String> grams = new ArrayList<>();
        for (int i = 0; i <= words.length - n; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < n; j++) {
                sb.append(words[i + j]).append(" ");
            }
            grams.add(sb.toString().trim());
        }
        return grams;
    }

    void addDocument(String docId, String text) {
        List<String> grams = generateNgrams(text);
        Set<String> set = new HashSet<>(grams);
        docNgrams.put(docId, set);

        for (String g : set) {
            index.putIfAbsent(g, new HashSet<>());
            index.get(g).add(docId);
        }
    }

    void analyzeDocument(String docId) {
        Set<String> grams = docNgrams.get(docId);
        Map<String, Integer> matchCount = new HashMap<>();

        for (String g : grams) {
            if (index.containsKey(g)) {
                for (String other : index.get(g)) {
                    if (!other.equals(docId)) {
                        matchCount.put(other, matchCount.getOrDefault(other, 0) + 1);
                    }
                }
            }
        }

        System.out.println("analyzeDocument(\"" + docId + "\")");
        System.out.println("→ Extracted " + grams.size() + " n-grams");

        for (String other : matchCount.keySet()) {
            int matches = matchCount.get(other);
            int total = grams.size();
            double similarity = (matches * 100.0) / total;

            System.out.println("→ Found " + matches + " matching n-grams with \"" + other + "\"");
            System.out.println("→ Similarity: " + String.format("%.1f", similarity) + "% " +
                    (similarity > 50 ? "(PLAGIARISM DETECTED)" : "(suspicious)"));
        }
    }

    public static void main(String[] args) {

        PlagiarismDetector detector = new PlagiarismDetector();

        String doc1 = "machine learning models learn patterns from data and improve performance";
        String doc2 = "machine learning models learn patterns from data and improve accuracy";
        String doc3 = "artificial intelligence systems analyze large amounts of data";

        detector.addDocument("essay_089.txt", doc1);
        detector.addDocument("essay_092.txt", doc2);
        detector.addDocument("essay_123.txt", doc3);

        detector.analyzeDocument("essay_123.txt");
    }
}