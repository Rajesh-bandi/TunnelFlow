package org.tunnelflow.client.config.validation;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ValidationResult {

    private final List<ValidationError> errors = new ArrayList<>();

    public boolean isValid() {
        return errors.isEmpty();
    }

    public void addError(String field, String message) {
        errors.add(new ValidationError(field, message));
    }
}
