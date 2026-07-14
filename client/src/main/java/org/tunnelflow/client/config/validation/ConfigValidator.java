package org.tunnelflow.client.config.validation;

import org.springframework.stereotype.Service;
import org.tunnelflow.client.config.model.ApplicationConfig;
import org.tunnelflow.client.config.model.TunnelFlowConfig;

import java.util.Map;

@Service
public class ConfigValidator {

    public ValidationResult validate(TunnelFlowConfig config) {

        ValidationResult result = new ValidationResult();

        validateVersion(config, result);
        validateProject(config, result);
        validateApps(config, result);

        return result;
    }

    private void validateVersion(
            TunnelFlowConfig config,
            ValidationResult result
    ) {

        if (config.getVersion() == null) {

            result.addError(
                    "version",
                    "Configuration version is required."
            );

        }

    }

    private void validateProject(
            TunnelFlowConfig config,
            ValidationResult result
    ) {

        if (config.getProject() == null) {

            result.addError(
                    "project",
                    "Project configuration is required."
            );

            return;
        }

        if (config.getProject().getName() == null
                || config.getProject().getName().isBlank()) {

            result.addError(
                    "project.name",
                    "Project name is required."
            );

        }

    }

    private void validateApps(
            TunnelFlowConfig config,
            ValidationResult result
    ) {

        if (config.getApps() == null
                || config.getApps().isEmpty()) {

            result.addError(
                    "apps",
                    "At least one application must be configured."
            );

            return;
        }

        for (Map.Entry<String, ApplicationConfig> entry :
                config.getApps().entrySet()) {

            String appName = entry.getKey();
            ApplicationConfig app = entry.getValue();

            if (app.getPort() <= 0) {

                result.addError(
                        "apps." + appName + ".port",
                        "Port must be greater than 0."
                );

            }

            if (app.getCommand() == null
                    || app.getCommand().isBlank()) {

                result.addError(
                        "apps." + appName + ".command",
                        "Command is required."
                );

            }

            if (app.getPath() == null
                    || app.getPath().isBlank()) {

                result.addError(
                        "apps." + appName + ".workingDirectory",
                        "Working directory is required."
                );

            }

        }

    }

}