package org.tunnelflow.client.controller;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/status")
public class StatusController {

    @GetMapping
    public Map<String, Object> getStatus() {
        return Map.of(
                "name", "TunnelFlow",
                "status", "running",
                "registered", true,
                "version", "0.1.0"
        );
    }
}