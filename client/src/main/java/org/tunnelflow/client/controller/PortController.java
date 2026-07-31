package org.tunnelflow.client.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.tunnelflow.client.service.PortManagerService;

import java.util.Map;

@RestController
@RequestMapping("/api/ports")
@RequiredArgsConstructor
public class PortController {

    private final PortManagerService portManagerService;

    @GetMapping("/check")
    public PortManagerService.PortCheckResult checkPort(@RequestParam int port) {
        return portManagerService.checkPort(port);
    }

    @PostMapping("/kill")
    public Map<String, Object> killProcess(@RequestBody Map<String, Long> request) {
        Long pid = request.get("pid");
        if (pid == null || pid <= 0) {
            throw new IllegalArgumentException("Valid PID is required");
        }
        boolean success = portManagerService.killProcess(pid);
        return Map.of("success", success, "pid", pid);
    }
}
