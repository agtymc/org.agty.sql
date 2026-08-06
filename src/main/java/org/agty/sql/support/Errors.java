package org.agty.sql.support;

import java.util.ArrayList;
import java.util.List;

/**
 * Internal error accumulator used by current production code.
 */
public final class Errors {
    private final List<String> errors = new ArrayList<>();

    public void addError(String error) {
        this.errors.add(error);
    }

    public void addError(String type, String message) {
        this.errors.add(type + ": " + message);
    }

    public String getErrors(String delimiter) {
        return String.join(delimiter, errors);
    }

    public String getErrors() {
        return String.join("; ", errors);
    }

    public List<String> getErrorsArray() {
        return new ArrayList<>(errors);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean noErrors() {
        return errors.isEmpty();
    }

    public boolean isEmpty() {
        return noErrors();
    }

    public void clear() {
        errors.clear();
    }
}
