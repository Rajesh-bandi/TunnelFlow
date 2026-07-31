package org.tunnelflow.client.runtime;

import lombok.Data;
import org.tunnelflow.client.config.model.ApplicationConfig;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Data
public class ApplicationRuntime {

    private final ApplicationConfig config;

    private final TunnelRuntime tunnelRuntime;

    private final Process process;

    private Map<String, String> resolvedEnvironment = new ConcurrentHashMap<>();

    private final List<String> processLogs = new CopyOnWriteArrayList<>();

    public ApplicationRuntime(ApplicationConfig config, TunnelRuntime tunnelRuntime, Process process) {
        this.config = config;
        this.tunnelRuntime = tunnelRuntime;
        this.process = process;
    }

    public void addLog(String line) {
        if (processLogs.size() > 500) {
            processLogs.remove(0);
        }
        processLogs.add(line);
    }
}
