import java.util.*;

class Transaction {
    int id;
    int amount;
    String merchant;
    String account;
    long time;

    Transaction(int id, int amount, String merchant, String account, long time) {
        this.id = id;
        this.amount = amount;
        this.merchant = merchant;
        this.account = account;
        this.time = time;
    }
}

class FraudAnalyzer {

    List<int[]> findTwoSum(List<Transaction> tx, int target) {
        Map<Integer, Transaction> map = new HashMap<>();
        List<int[]> res = new ArrayList<>();

        for (Transaction t : tx) {
            int comp = target - t.amount;
            if (map.containsKey(comp)) {
                res.add(new int[]{map.get(comp).id, t.id});
            }
            map.put(t.amount, t);
        }
        return res;
    }

    List<int[]> twoSumTimeWindow(List<Transaction> tx, int target, long window) {
        Map<Integer, List<Transaction>> map = new HashMap<>();
        List<int[]> res = new ArrayList<>();

        for (Transaction t : tx) {
            int comp = target - t.amount;
            if (map.containsKey(comp)) {
                for (Transaction p : map.get(comp)) {
                    if (Math.abs(t.time - p.time) <= window) {
                        res.add(new int[]{p.id, t.id});
                    }
                }
            }
            map.computeIfAbsent(t.amount, k -> new ArrayList<>()).add(t);
        }
        return res;
    }

    List<List<Integer>> findKSum(List<Transaction> tx, int k, int target) {
        List<List<Integer>> res = new ArrayList<>();
        kSumHelper(tx, k, target, 0, new ArrayList<>(), res);
        return res;
    }

    void kSumHelper(List<Transaction> tx, int k, int target, int index, List<Integer> path, List<List<Integer>> res) {
        if (k == 0 && target == 0) {
            res.add(new ArrayList<>(path));
            return;
        }
        if (k == 0 || index >= tx.size()) return;

        for (int i = index; i < tx.size(); i++) {
            path.add(tx.get(i).id);
            kSumHelper(tx, k - 1, target - tx.get(i).amount, i + 1, path, res);
            path.remove(path.size() - 1);
        }
    }

    Map<String, Set<String>> detectDuplicates(List<Transaction> tx) {
        Map<String, Set<String>> map = new HashMap<>();

        for (Transaction t : tx) {
            String key = t.amount + "_" + t.merchant;
            map.computeIfAbsent(key, k -> new HashSet<>()).add(t.account);
        }

        Map<String, Set<String>> res = new HashMap<>();
        for (String k : map.keySet()) {
            if (map.get(k).size() > 1) res.put(k, map.get(k));
        }
        return res;
    }
}

public class TransactionAnalysis {

    public static void main(String[] args) {

        List<Transaction> transactions = new ArrayList<>();

        transactions.add(new Transaction(1, 500, "StoreA", "acc1", 1000));
        transactions.add(new Transaction(2, 300, "StoreB", "acc2", 1015));
        transactions.add(new Transaction(3, 200, "StoreC", "acc3", 1030));
        transactions.add(new Transaction(4, 500, "StoreA", "acc2", 1100));

        FraudAnalyzer analyzer = new FraudAnalyzer();

        List<int[]> twoSum = analyzer.findTwoSum(transactions, 500);
        for (int[] p : twoSum) {
            System.out.println("TwoSum Pair: (" + p[0] + "," + p[1] + ")");
        }

        List<int[]> windowPairs = analyzer.twoSumTimeWindow(transactions, 500, 3600);
        for (int[] p : windowPairs) {
            System.out.println("TimeWindow Pair: (" + p[0] + "," + p[1] + ")");
        }

        List<List<Integer>> ksum = analyzer.findKSum(transactions, 3, 1000);
        for (List<Integer> l : ksum) {
            System.out.println("KSum: " + l);
        }

        Map<String, Set<String>> dup = analyzer.detectDuplicates(transactions);
        for (String k : dup.keySet()) {
            System.out.println("Duplicate: " + k + " Accounts: " + dup.get(k));
        }
    }
}