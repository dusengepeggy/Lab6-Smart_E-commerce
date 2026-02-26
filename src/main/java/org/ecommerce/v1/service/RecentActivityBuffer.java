package org.ecommerce.v1.service;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class RecentActivityBuffer {

    private static final int MAX_SIZE = 500;

    private final List<ActivityEntry> buffer = new CopyOnWriteArrayList<>();

    public void record(String action, String detail) {
        ActivityEntry entry = new ActivityEntry(Instant.now(), action, detail);
        buffer.add(entry);
        if (buffer.size() > MAX_SIZE) {
            buffer.remove(0);
        }
    }

    public List<ActivityEntry> getRecent(int limit) {
        int size = buffer.size();
        if (size == 0) return List.of();
        int from = Math.max(0, size - limit);
        return Collections.unmodifiableList(buffer.subList(from, size));
    }

    public record ActivityEntry(Instant at, String action, String detail) {}
}
