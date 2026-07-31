package org.tunnelflow.client.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.tunnelflow.client.config.model.ApplicationConfig;
import org.tunnelflow.client.config.model.ApplicationDeployment;
import org.tunnelflow.client.runtime.ApplicationRuntime;
import org.tunnelflow.client.runtime.ApplicationRuntimeRegistry;
import org.tunnelflow.client.runtime.TunnelRuntime;
import org.tunnelflow.client.runtime.TunnelRuntimeRegistry;
import org.tunnelflow.client.service.*;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

@RestController
@RequestMapping("/api/apps")
@RequiredArgsConstructor
@Slf4j
public class ApplicationController {

    private final ApplicationManager applicationManager;
    private final ApplicationRuntimeRegistry applicationRuntimeRegistry;
    private final TunnelRuntimeRegistry tunnelRuntimeRegistry;
    private final ClientTunnelManager clientTunnelManager;
    private final ProcessLauncher processLauncher;
    private final PlaceholderResolver placeholderResolver;
    private final PortManagerService portManagerService;
    private final org.tunnelflow.client.config.loader.YamlConfigExporter yamlConfigExporter;

    @GetMapping
    public List<Map<String, Object>> getApplications() {
        List<Map<String, Object>> result = new ArrayList<>();
        applicationRuntimeRegistry.getAll().forEach(runtime -> {
            Map<String, Object> map = new HashMap<>();
            ApplicationConfig cfg = runtime.getConfig();
            map.put("name", cfg != null ? cfg.getName() : "Unknown");
            map.put("path", cfg != null ? cfg.getPath() : "");
            map.put("command", cfg != null ? cfg.getCommand() : "");
            map.put("port", cfg != null ? cfg.getPort() : 0);
            
            Map<String, String> cfgEnv = cfg != null && cfg.getEnv() != null ? cfg.getEnv() : Map.of();
            Map<String, String> resEnv = runtime.getResolvedEnvironment() != null ? runtime.getResolvedEnvironment() : Map.of();
            
            map.put("configuredEnv", cfgEnv);
            map.put("resolvedEnv", resEnv);

            Map<String, String> mergedEnv = new LinkedHashMap<>(cfgEnv);
            resEnv.forEach((k, v) -> {
                if (v != null && !v.isBlank()) {
                    mergedEnv.put(k, v);
                }
            });
            map.put("env", mergedEnv);

            map.put("pid", runtime.getProcess() != null ? runtime.getProcess().pid() : -1);
            map.put("isAlive", runtime.getProcess() != null && runtime.getProcess().isAlive());
            if (runtime.getTunnelRuntime() != null) {
                map.put("tunnelId", runtime.getTunnelRuntime().getTunnelId());
                map.put("publicUrl", runtime.getTunnelRuntime().getPublicUrl());
                map.put("localUrl", "http://localhost:" + (cfg != null ? cfg.getPort() : 0));
            }
            result.add(map);
        });
        return result;
    }

    @PostMapping("/detect")
    public Map<String, Object> detectFramework(@RequestBody Map<String, String> body) {
        String projectPathStr = body.getOrDefault("path", "./");
        Path projectPath = Path.of(projectPathStr);

        String framework = "Custom App";
        String icon = "⚡";
        String defaultCommand = "npm run dev";
        int defaultPort = 3000;

        File dir = projectPath.toFile();
        if (dir.exists() && dir.isDirectory()) {
            File packageJson = new File(dir, "package.json");
            File viteConfig = new File(dir, "vite.config.js");
            File viteConfigTs = new File(dir, "vite.config.ts");
            File nextConfig = new File(dir, "next.config.js");
            File nextConfigMjs = new File(dir, "next.config.mjs");
            File pomXml = new File(dir, "pom.xml");
            File buildGradle = new File(dir, "build.gradle");
            File reqTxt = new File(dir, "requirements.txt");
            File pyProject = new File(dir, "pyproject.toml");

            if (viteConfig.exists() || viteConfigTs.exists()) {
                framework = "Vite / React";
                icon = "⚛️";
                defaultCommand = "npm run dev";
                defaultPort = 5173;
            } else if (nextConfig.exists() || nextConfigMjs.exists()) {
                framework = "Next.js";
                icon = "▲";
                defaultCommand = "npm run dev";
                defaultPort = 3000;
            } else if (packageJson.exists()) {
                framework = "Node.js / Express";
                icon = "🟢";
                defaultCommand = "npm start";
                defaultPort = 3000;
            } else if (pomXml.exists() || buildGradle.exists()) {
                framework = "Spring Boot";
                icon = "☕";
                boolean isWin = System.getProperty("os.name").toLowerCase().contains("win");
                if (pomXml.exists()) {
                    defaultCommand = isWin ? "mvnw.cmd spring-boot:run" : "./mvnw spring-boot:run";
                } else {
                    defaultCommand = isWin ? "gradlew.bat bootRun" : "./gradlew bootRun";
                }
                defaultPort = 8080;
            } else if (reqTxt.exists() || pyProject.exists()) {
                framework = "Python App";
                icon = "🐍";
                defaultCommand = "python main.py";
                defaultPort = 8000;
            }
        }

        return Map.of(
                "framework", framework,
                "icon", icon,
                "command", defaultCommand,
                "port", defaultPort,
                "detectedPath", projectPathStr
        );
    }

    @PostMapping({"/deploy", "/deploy-stack"})
    public List<Map<String, Object>> deployStack(
            @RequestParam(required = false) String stackName,
            @RequestBody Object body
    ) throws Exception {
        List<ApplicationConfig> serviceConfigs = new ArrayList<>();
        String appStackName = stackName != null && !stackName.isBlank() ? stackName : "Notes App";

        if (body instanceof List) {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            serviceConfigs = mapper.convertValue(body, new com.fasterxml.jackson.core.type.TypeReference<List<ApplicationConfig>>(){});
        } else if (body instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) body;
            if (map.containsKey("stackName") && map.get("stackName") != null) {
                appStackName = map.get("stackName").toString();
            }
            Object servicesObj = map.get("services");
            if (servicesObj != null) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                serviceConfigs = mapper.convertValue(servicesObj, new com.fasterxml.jackson.core.type.TypeReference<List<ApplicationConfig>>(){});
            }
        }

        Map<String, ApplicationDeployment> deployments = new LinkedHashMap<>();

        // Step 1: Provision Tunnels for all services
        for (ApplicationConfig app : serviceConfigs) {
            if (app.getName() == null || app.getName().isBlank()) {
                throw new IllegalArgumentException("Service name is required");
            }
            TunnelRuntime tunnelRuntime = clientTunnelManager.expose(app.getPort());
            ApplicationDeployment deployment = new ApplicationDeployment(app);
            deployment.setTunnelRuntime(tunnelRuntime);
            deployments.put(app.getName(), deployment);
        }

        // Step 2: Resolve Placeholders across services
        placeholderResolver.resolve(deployments);

        // Save deployment stack to history for 1-click relaunching with custom stack name
        yamlConfigExporter.saveToHistory(appStackName, serviceConfigs);

        // Step 3: Launch Processes & Register Runtimes
        List<Map<String, Object>> results = new ArrayList<>();
        deployments.forEach((name, deployment) -> {
            // Port Safety Check: Auto-clear any lingering blocking process on target port
            PortManagerService.PortCheckResult check = portManagerService.checkPort(deployment.getConfig().getPort());
            if (check.isOccupied()) {
                log.warn("Port {} for service [{}] is occupied by PID {}. Terminating blocking process...",
                        deployment.getConfig().getPort(), name, check.getPid());
                portManagerService.killProcess(check.getPid());
            }

            ApplicationRuntime runtime = processLauncher.launch(deployment);
            runtime.setResolvedEnvironment(deployment.getResolvedEnvironment());
            applicationRuntimeRegistry.register(name, runtime);
            tunnelRuntimeRegistry.register(deployment.getTunnelRuntime());

            Map<String, Object> res = new HashMap<>();
            res.put("name", name);
            res.put("port", deployment.getConfig().getPort());
            res.put("publicUrl", deployment.getTunnelRuntime().getPublicUrl());
            res.put("localUrl", "http://localhost:" + deployment.getConfig().getPort());
            res.put("configuredEnv", deployment.getConfig().getEnv());
            res.put("resolvedEnv", deployment.getResolvedEnvironment());
            res.put("env", deployment.getResolvedEnvironment());
            res.put("pid", runtime.getProcess() != null ? runtime.getProcess().pid() : -1);
            results.add(res);
        });

        return results;
    }

    @PostMapping("/launch")
    public Map<String, Object> launchApp(@RequestBody ApplicationConfig config) throws Exception {
        if (config.getName() == null || config.getName().isBlank()) {
            throw new IllegalArgumentException("Application name is required");
        }
        if (config.getPort() < 1 || config.getPort() > 65535) {
            throw new IllegalArgumentException("Valid port is required");
        }

        TunnelRuntime tunnelRuntime = clientTunnelManager.expose(config.getPort());
        ApplicationDeployment deployment = new ApplicationDeployment(config);
        deployment.setTunnelRuntime(tunnelRuntime);

        Map<String, ApplicationDeployment> map = new LinkedHashMap<>();
        map.put(config.getName(), deployment);

        placeholderResolver.resolve(map);

        // Port Safety Check: Auto-clear lingering process on target port
        PortManagerService.PortCheckResult check = portManagerService.checkPort(config.getPort());
        if (check.isOccupied()) {
            log.warn("Port {} for app [{}] is occupied by PID {}. Terminating blocking process...",
                    config.getPort(), config.getName(), check.getPid());
            portManagerService.killProcess(check.getPid());
        }

        ApplicationRuntime runtime = processLauncher.launch(deployment);
        runtime.setResolvedEnvironment(deployment.getResolvedEnvironment());
        applicationRuntimeRegistry.register(config.getName(), runtime);
        tunnelRuntimeRegistry.register(tunnelRuntime);

        Map<String, Object> res = new HashMap<>();
        res.put("name", config.getName());
        res.put("port", config.getPort());
        res.put("publicUrl", tunnelRuntime.getPublicUrl());
        res.put("localUrl", "http://localhost:" + config.getPort());
        res.put("configuredEnv", config.getEnv());
        res.put("resolvedEnv", deployment.getResolvedEnvironment());
        res.put("env", deployment.getResolvedEnvironment());
        res.put("pid", runtime.getProcess() != null ? runtime.getProcess().pid() : -1);
        return res;
    }

    @PostMapping("/export")
    public Map<String, String> exportConfig(
            @RequestParam(required = false, defaultValue = "Notes App") String stackName,
            @RequestBody(required = false) List<ApplicationConfig> serviceConfigs
    ) throws Exception {
        List<ApplicationConfig> configs = serviceConfigs;

        if (configs == null || configs.isEmpty()) {
            configs = new ArrayList<>();
            for (ApplicationRuntime runtime : applicationRuntimeRegistry.getAll()) {
                if (runtime.getConfig() != null) {
                    configs.add(runtime.getConfig());
                }
            }
        }

        if (configs.isEmpty()) {
            throw new IllegalArgumentException("No application services running or provided to export.");
        }

        File file = yamlConfigExporter.exportToYamlFile(stackName, configs, "tunnelflow.yaml");
        return Map.of(
                "status", "success",
                "filePath", file.getAbsolutePath(),
                "yamlContent", yamlConfigExporter.exportToYamlString(stackName, configs)
        );
    }

    @PostMapping("/import")
    public Map<String, Object> importConfig(@RequestBody Map<String, String> body) throws Exception {
        String yamlContent = body.get("yamlContent");
        if (yamlContent == null || yamlContent.isBlank()) {
            throw new IllegalArgumentException("yamlContent is required for import");
        }

        List<ApplicationConfig> importedConfigs = yamlConfigExporter.importFromYamlString(yamlContent);
        List<Map<String, Object>> deployedApps = deployStack("Imported Stack", importedConfigs);

        return Map.of(
                "status", "success",
                "importedServicesCount", importedConfigs.size(),
                "apps", deployedApps
        );
    }

    @GetMapping("/history")
    public List<Map<String, Object>> getHistory() {
        return yamlConfigExporter.loadHistoryList();
    }

    @DeleteMapping("/history/{id}")
    public Map<String, Object> deleteHistory(@PathVariable String id) {
        boolean success = yamlConfigExporter.deleteHistoryEntry(id);
        return Map.of("success", success, "id", id);
    }

    @DeleteMapping("/{name}")
    public Map<String, String> stopApp(@PathVariable String name) {
        ApplicationRuntime runtime = applicationRuntimeRegistry.get(name);
        if (runtime != null) {
            if (runtime.getProcess() != null && runtime.getProcess().isAlive()) {
                runtime.getProcess().destroyForcibly();
            }
            if (runtime.getTunnelRuntime() != null) {
                try {
                    clientTunnelManager.deleteTunnel(runtime.getTunnelRuntime().getTunnelId());
                } catch (Exception e) {
                    log.warn("Failed to delete tunnel for app {}", name, e);
                }
            }
            applicationRuntimeRegistry.unregister(name);
            return Map.of("status", "stopped", "name", name);
        }
        return Map.of("status", "not_found", "name", name);
    }

    @PostMapping("/{name}/restart")
    public Map<String, Object> restartApp(@PathVariable String name) throws Exception {
        ApplicationRuntime runtime = applicationRuntimeRegistry.get(name);
        if (runtime == null || runtime.getConfig() == null) {
            throw new IllegalArgumentException("App not found: " + name);
        }

        if (runtime.getProcess() != null && runtime.getProcess().isAlive()) {
            runtime.getProcess().destroyForcibly();
        }

        // Port Safety Check: Auto-clear lingering process on target port
        PortManagerService.PortCheckResult check = portManagerService.checkPort(runtime.getConfig().getPort());
        if (check.isOccupied()) {
            log.warn("Port {} for app [{}] is occupied by PID {}. Terminating blocking process...",
                    runtime.getConfig().getPort(), name, check.getPid());
            portManagerService.killProcess(check.getPid());
        }

        ApplicationDeployment deployment = new ApplicationDeployment(runtime.getConfig());
        deployment.setTunnelRuntime(runtime.getTunnelRuntime());
        ApplicationRuntime newRuntime = processLauncher.launch(deployment);
        newRuntime.setResolvedEnvironment(runtime.getResolvedEnvironment());
        applicationRuntimeRegistry.register(name, newRuntime);

        Map<String, Object> res = new HashMap<>();
        res.put("name", name);
        res.put("pid", newRuntime.getProcess() != null ? newRuntime.getProcess().pid() : -1);
        res.put("status", "restarted");
        return res;
    }

    @GetMapping("/{name}/logs")
    public List<String> getAppLogs(@PathVariable String name) {
        ApplicationRuntime runtime = applicationRuntimeRegistry.get(name);
        if (runtime != null) {
            return runtime.getProcessLogs();
        }
        return List.of();
    }
}
