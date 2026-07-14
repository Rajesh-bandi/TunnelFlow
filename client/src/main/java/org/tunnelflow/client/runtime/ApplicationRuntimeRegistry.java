package org.tunnelflow.client.runtime;

import org.springframework.stereotype.Service;
import org.tunnelflow.client.runtime.ApplicationRuntime;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ApplicationRuntimeRegistry {

    private final Map<String, ApplicationRuntime> runtimes =
            new ConcurrentHashMap<>();

    public void register(
            String applicationName,
            ApplicationRuntime runtime
    ) {
        runtimes.put(applicationName, runtime);
    }

    public ApplicationRuntime get(String applicationName) {
        return runtimes.get(applicationName);
    }

    public Collection<ApplicationRuntime> getAll() {
        return runtimes.values();
    }

    public void unregister(String applicationName) {
        runtimes.remove(applicationName);
    }

    public boolean isRunning(String applicationName) {
        return runtimes.containsKey(applicationName);
    }
}