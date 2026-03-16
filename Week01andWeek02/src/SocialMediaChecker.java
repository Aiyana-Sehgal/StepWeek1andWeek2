import java.util.HashMap;
import java.util.Hashtable;
import java.util.Scanner;

public class SocialMediaChecker {

    static Hashtable<Integer, String> ht = new Hashtable<Integer, String>();
    static HashMap<String, Integer> popularity = new HashMap<String, Integer>();

    static void Store(String value) {
        int key = value.length();
        key = key % 50;

        // Linear probing for collision
        while (ht.containsKey(key)) {
            key = (key + 1) % 50;
        }

        ht.put(key, value);

        // Fix spelling here
        popularity.put(value, popularity.getOrDefault(value, 0) + 1);
    }

    static boolean contain(String value) {
        int key = value.length();
        key = key % 50;

        // Linear probing search
        int start = key;

        while (ht.containsKey(key)) {
            if (ht.get(key).equals(value)) {
                return true;
            }
            key = (key + 1) % 50;

            if (key == start) {
                break;
            }
        }

        return false;
    }

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        String username = sc.nextLine();

        if (!contain(username)) {
            Store(username);
            System.out.println("Username stored.");
        } else {
            System.out.println("Username already exists.");
        }

        sc.close();
    }
}