package org.tunnelflow.client.runtime;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class RequestLogRegistry {

    private static final int MAX_LOGS = 500;

    private final ConcurrentLinkedDeque<RequestLog> logs =
            new ConcurrentLinkedDeque<>();

    public void add(RequestLog log) {

        logs.addFirst(log);

        while (logs.size() > MAX_LOGS) {
            logs.pollLast();
        }
    }

    public List<RequestLog> getAll() {
        return new ArrayList<>(logs);
    }

    public List<RequestLog> getByTunnelId(String tunnelId) {

        return logs.stream()
                .filter(log ->
                        tunnelId.equals(log.getTunnelId())
                )
                .toList();
    }

    public void clear() {
        logs.clear();
    }
}