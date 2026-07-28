package org.tunnelflow.client.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.tunnelflow.client.runtime.RequestLog;
import org.tunnelflow.client.runtime.RequestLogRegistry;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class RequestLogController {

    private final RequestLogRegistry requestLogRegistry;

    @GetMapping
    public List<RequestLog> getRequests(
            @RequestParam(required = false) String tunnelId
    ) {

        if (tunnelId != null && !tunnelId.isBlank()) {
            return requestLogRegistry.getByTunnelId(tunnelId);
        }

        return requestLogRegistry.getAll();
    }
}