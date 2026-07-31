package org.tunnelflow.client.config.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tunnelflow.client.config.model.ApplicationConfig;
import org.tunnelflow.client.config.model.ProjectConfig;
import org.tunnelflow.client.config.model.TunnelFlowConfig;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class YamlConfigExporter {

    private final ObjectMapper yamlObjectMapper;

    public String exportToYamlString(String stackName, List<ApplicationConfig> serviceConfigs) throws IOException {
        TunnelFlowConfig config = buildConfig(stackName, serviceConfigs);
        return yamlObjectMapper.writeValueAsString(config);
    }

    public File exportToYamlFile(String stackName, List<ApplicationConfig> serviceConfigs, String targetPath) throws IOException {
        TunnelFlowConfig config = buildConfig(stackName, serviceConfigs);
        File file = new File(targetPath != null && !targetPath.isBlank() ? targetPath : "tunnelflow.yaml");
        yamlObjectMapper.writeValue(file, config);
        saveToHistory(stackName, serviceConfigs);
        log.info("Exported TunnelFlow YAML config to [{}]", file.getAbsolutePath());
        return file;
    }

    @SuppressWarnings("unchecked")
    public List<ApplicationConfig> importFromYamlString(String yamlContent) throws IOException {
        if (yamlContent == null || yamlContent.isBlank()) {
            throw new IllegalArgumentException("YAML content cannot be empty.");
        }

        List<ApplicationConfig> list = new ArrayList<>();
        String stackName = "Imported Stack";

        // Strategy 1: Try parsing full TunnelFlowConfig
        try {
            TunnelFlowConfig config = yamlObjectMapper.readValue(yamlContent, TunnelFlowConfig.class);
            if (config != null && config.getProject() != null && config.getProject().getName() != null) {
                stackName = config.getProject().getName();
            }
            if (config != null && config.getApps() != null && !config.getApps().isEmpty()) {
                config.getApps().forEach((name, app) -> {
                    if (app.getName() == null || app.getName().isBlank()) {
                        app.setName(name);
                    }
                    list.add(app);
                });
                saveToHistory(stackName, list);
                return list;
            }
        } catch (Exception e) {
            log.debug("Failed to parse as TunnelFlowConfig, trying map fallback...", e);
        }

        // Strategy 2: Flexible Map fallback (handles apps: { ... } or root { backend: { ... } })
        Map<String, Object> rootMap = yamlObjectMapper.readValue(yamlContent, Map.class);
        if (rootMap == null || rootMap.isEmpty()) {
            throw new IllegalArgumentException("Invalid YAML format: empty document.");
        }

        Object appsObj = rootMap.get("apps");
        Map<String, Object> appsMap = (appsObj instanceof Map) ? (Map<String, Object>) appsObj : rootMap;

        appsMap.forEach((key, val) -> {
            if (val instanceof Map) {
                Map<String, Object> appData = (Map<String, Object>) val;
                ApplicationConfig app = new ApplicationConfig();
                app.setName(appData.getOrDefault("name", key).toString());
                app.setPath(appData.getOrDefault("path", "./").toString());
                app.setCommand(appData.getOrDefault("command", "npm run dev").toString());

                if (appData.containsKey("port")) {
                    try {
                        app.setPort(Integer.parseInt(appData.get("port").toString()));
                    } catch (Exception ignored) {
                        app.setPort(3000);
                    }
                } else {
                    app.setPort(3000);
                }

                if (appData.containsKey("env") && appData.get("env") instanceof Map) {
                    Map<String, String> envMap = new LinkedHashMap<>();
                    ((Map<?, ?>) appData.get("env")).forEach((k, v) -> envMap.put(k.toString(), v != null ? v.toString() : ""));
                    app.setEnv(envMap);
                }

                list.add(app);
            }
        });

        if (list.isEmpty()) {
            throw new IllegalArgumentException("Could not find any application service definitions in YAML.");
        }

        saveToHistory(stackName, list);
        return list;
    }

    public void saveToHistory(List<ApplicationConfig> serviceConfigs) {
        saveToHistory("Notes App", serviceConfigs);
    }

    public void saveToHistory(String stackName, List<ApplicationConfig> serviceConfigs) {
        if (serviceConfigs == null || serviceConfigs.isEmpty()) return;
        try {
            File historyFile = getHistoryFile();
            List<Map<String, Object>> history = loadHistoryList();

            String finalStackName = (stackName != null && !stackName.isBlank()) ? stackName : serviceConfigs.get(0).getName() + " Stack";
            String stackSignature = getStackSignature(serviceConfigs);

            // Deduplicate: Remove any existing entry matching stack signature or appName
            history.removeIf(entry -> {
                String existingSig = (String) entry.get("signature");
                if (stackSignature.equals(existingSig)) return true;

                String existingAppName = (String) entry.get("appName");
                if (finalStackName.equalsIgnoreCase(existingAppName)) return true;

                return false;
            });

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("id", UUID.randomUUID().toString().substring(0, 8));
            entry.put("signature", stackSignature);
            entry.put("timestamp", System.currentTimeMillis());
            entry.put("appName", finalStackName);
            entry.put("servicesCount", serviceConfigs.size());
            entry.put("apps", serviceConfigs);

            history.add(0, entry);
            if (history.size() > 20) {
                history = history.subList(0, 20);
            }

            yamlObjectMapper.writeValue(historyFile, history);
        } catch (Exception e) {
            log.warn("Failed to save application stack to history", e);
        }
    }

    private String getStackSignature(List<ApplicationConfig> serviceConfigs) {
        StringBuilder sb = new StringBuilder();
        serviceConfigs.stream()
                .sorted(Comparator.comparing(ApplicationConfig::getName))
                .forEach(cfg -> sb.append(cfg.getName()).append(":").append(cfg.getPort()).append("|"));
        return sb.toString();
    }

    public boolean deleteHistoryEntry(String id) {
        try {
            File historyFile = getHistoryFile();
            List<Map<String, Object>> history = loadHistoryList();
            boolean removed = history.removeIf(entry -> id.equals(entry.get("id")));
            if (removed) {
                yamlObjectMapper.writeValue(historyFile, history);
            }
            return removed;
        } catch (Exception e) {
            log.warn("Failed to delete history entry {}", id, e);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> loadHistoryList() {
        try {
            File historyFile = getHistoryFile();
            if (historyFile.exists()) {
                return yamlObjectMapper.readValue(historyFile, List.class);
            }
        } catch (Exception e) {
            log.warn("Failed to read history file", e);
        }
        return new ArrayList<>();
    }

    private File getHistoryFile() {
        String userHome = System.getProperty("user.home");
        File dir = new File(userHome, ".tunnelflow");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, "history_stacks.yaml");
    }

    private TunnelFlowConfig buildConfig(String stackName, List<ApplicationConfig> serviceConfigs) {
        Map<String, ApplicationConfig> appsMap = new LinkedHashMap<>();
        for (ApplicationConfig app : serviceConfigs) {
            appsMap.put(app.getName(), app);
        }
        return new TunnelFlowConfig(1, new ProjectConfig(stackName != null && !stackName.isBlank() ? stackName : "my-stack"), appsMap);
    }
}
