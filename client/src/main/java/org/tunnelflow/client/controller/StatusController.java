package org.tunnelflow.client.controller;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/status")
public class StatusController {

    @GetMapping
    public Map<String, String> getStatus() {

        return Map.of(
                "name", "TunnelFlow",
                "status", "running",
                "version", "0.1.0"
        );
    }
}