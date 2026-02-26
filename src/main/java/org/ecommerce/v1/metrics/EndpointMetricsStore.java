package org.ecommerce.v1.metrics;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EndpointMetricsStore {

    private final Map<String, Stats> metrics = new ConcurrentHashMap<>();

    public void record(String key, long elapsedMs) {
        metrics.compute(key, (k, existing) -> {
            if (existing == null) {
                return new Stats(1, elapsedMs, elapsedMs);
            }
            return new Stats(
                    existing.count + 1,
                    existing.totalMs + elapsedMs,
                    Math.max(existing.maxMs, elapsedMs)
            );
        });
    }

    public Map<String, Stats> snapshot() {
        return Map.copyOf(metrics);
    }

    public record Stats(long count, long totalMs, long maxMs) {
        public double avgMs() {
            return count == 0 ? 0 : (double) totalMs / count;
        }
    }
}
