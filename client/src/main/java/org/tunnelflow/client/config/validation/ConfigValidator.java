package org.tunnelflow.client.config.validation;

import org.springframework.stereotype.Service;
import org.tunnelflow.client.config.model.TunnelFlowConfig;

@Service
public class ConfigValidator {

    public ValidationResult validate(TunnelFlowConfig config) {

        ValidationResult result = new ValidationResult();

        validateProject(config, result);

        return result;
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
}