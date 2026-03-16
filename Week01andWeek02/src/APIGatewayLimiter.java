import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

class TokenBucket {
    int maxTokens;
    AtomicInteger tokens;
    long lastRefillTime;
    int refillRate;

    TokenBucket(int maxTokens, int refillRate) {
        this.maxTokens = maxTokens;
        this.refillRate = refillRate;
        this.tokens = new AtomicInteger(maxTokens);
        this.lastRefillTime = System.currentTimeMillis();
    }

    synchronized int refill() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRefillTime;
        int tokensToAdd = (int) (elapsed / 3600000.0 * refillRate);
        if (tokensToAdd > 0) {
            int newTokens = Math.min(maxTokens, tokens.get() + tokensToAdd);
            tokens.set(newTokens);
            lastRefillTime = now;
        }
        return tokens.get();
    }

    synchronized boolean allowRequest() {
        refill();
        if (tokens.get() > 0) {
            tokens.decrementAndGet();
            return true;
        }
        return false;
    }

    int remaining() {
        return tokens.get();
    }
}

class RateLimiter {

    ConcurrentHashMap<String, TokenBucket> clients = new ConcurrentHashMap<>();
    int limit = 1000;

    String checkRateLimit(String clientId) {
        clients.putIfAbsent(clientId, new TokenBucket(limit, limit));
        TokenBucket bucket = clients.get(clientId);

        if (bucket.allowRequest()) {
            return "Allowed (" + bucket.remaining() + " requests remaining)";
        } else {
            long retry = 3600 - (System.currentTimeMillis() - bucket.lastRefillTime) / 1000;
            return "Denied (0 requests remaining, retry after " + retry + "s)";
        }
    }

    String getRateLimitStatus(String clientId) {
        TokenBucket bucket = clients.get(clientId);
        if (bucket == null) return "Client not found";

        int used = bucket.maxTokens - bucket.remaining();
        long reset = bucket.lastRefillTime + 3600000;

        return "{used: " + used + ", limit: " + bucket.maxTokens + ", reset: " + reset + "}";
    }
}

public class APIGatewayLimiter {

    public static void main(String[] args) {

        RateLimiter limiter = new RateLimiter();

        for (int i = 0; i < 5; i++) {
            System.out.println(limiter.checkRateLimit("abc123"));
        }

        System.out.println(limiter.getRateLimitStatus("abc123"));
    }
}