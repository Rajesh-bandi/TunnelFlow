package org.tunnelflow.client.service;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;

@Service
@Slf4j
public class PortManagerService {

    @Data
    @Builder
    public static class PortCheckResult {
        private boolean occupied;
        private int port;
        private long pid;
        private String processName;
    }

    public PortCheckResult checkPort(int port) {
        boolean isBound = false;
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", port), 300);
            isBound = true;
        } catch (Exception e) {
            isBound = false;
        }

        if (!isBound) {
            return PortCheckResult.builder()
                    .occupied(false)
                    .port(port)
                    .pid(-1)
                    .processName("")
                    .build();
        }

        long pid = findPidOnPort(port);
        String processName = findProcessNameByPid(pid);

        return PortCheckResult.builder()
                .occupied(true)
                .port(port)
                .pid(pid)
                .processName(processName.isEmpty() ? "Occupied Process" : processName)
                .build();
    }

    public boolean killProcess(long pid) {
        if (pid <= 0) return false;
        try {
            boolean isWin = System.getProperty("os.name").toLowerCase().contains("win");
            ProcessBuilder pb = isWin
                    ? new ProcessBuilder("taskkill", "/F", "/PID", String.valueOf(pid))
                    : new ProcessBuilder("kill", "-9", String.valueOf(pid));

            Process p = pb.start();
            p.waitFor();
            Thread.sleep(500); // Allow socket to release
            log.info("Terminated blocking process PID {}", pid);
            return true;
        } catch (Exception e) {
            log.error("Failed to kill PID {}", pid, e);
            return false;
        }
    }

    private long findPidOnPort(int port) {
        try {
            boolean isWin = System.getProperty("os.name").toLowerCase().contains("win");
            if (isWin) {
                Process p = new ProcessBuilder("cmd", "/c", "netstat -ano | findstr :" + port).start();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.contains("LISTENING")) {
                            String[] parts = line.trim().split("\\s+");
                            if (parts.length > 0) {
                                String pidStr = parts[parts.length - 1];
                                return Long.parseLong(pidStr);
                            }
                        }
                    }
                }
            } else {
                Process p = new ProcessBuilder("sh", "-c", "lsof -i :" + port + " -t").start();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String line = reader.readLine();
                    if (line != null && !line.isBlank()) {
                        return Long.parseLong(line.trim());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to find PID for port {}", port, e);
        }
        return -1;
    }

    private String findProcessNameByPid(long pid) {
        if (pid <= 0) return "";
        try {
            boolean isWin = System.getProperty("os.name").toLowerCase().contains("win");
            if (isWin) {
                Process p = new ProcessBuilder("cmd", "/c", "tasklist /FI \"PID eq " + pid + "\" /NH").start();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String line = reader.readLine();
                    if (line != null && !line.isBlank()) {
                        String[] parts = line.trim().split("\\s+");
                        if (parts.length > 0) {
                            return parts[0];
                        }
                    }
                }
            } else {
                Process p = new ProcessBuilder("ps", "-p", String.valueOf(pid), "-o", "comm=").start();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String line = reader.readLine();
                    if (line != null) return line.trim();
                }
            }
        } catch (Exception e) {
            log.warn("Failed to find process name for PID {}", pid, e);
        }
        return "";
    }
}
