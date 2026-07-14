package org.tunnelflow.client.runtime;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Getter
public class ApplicationRuntimeContext {

    private final Map<String, String> serviceUrls =
            new ConcurrentHashMap<>();

}