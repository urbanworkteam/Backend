package kr.farmily.api.subscription.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Redis 대안: 인메모리 idempotency 기록 (TTL 24h). 운영 시 Redis 권장. */
@Component
public class CreditIdempotencyStore {

    private static final long TTL_MS = 24 * 60 * 60 * 1000L;
    private final Map<String, Long> seen = new ConcurrentHashMap<>();

    public synchronized boolean tryAcquire(long userId, String idemKey) {
        String key = userId + ":" + idemKey;
        long now = System.currentTimeMillis();
        cleanup(now);
        Long prev = seen.putIfAbsent(key, now);
        return prev == null;
    }

    public synchronized void release(long userId, String idemKey) {
        seen.remove(userId + ":" + idemKey);
    }

    private void cleanup(long now) {
        seen.entrySet().removeIf(e -> now - e.getValue() > TTL_MS);
    }
}
