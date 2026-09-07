package org.agty.sql.support;

import java.util.ArrayList;
import java.util.List;

/**
 * Internal error accumulator used by current production code.
 */
public final class Errors {
    /** Creates a new Errors instance. */
    public Errors() {
    }

    private final List<String> errors = new ArrayList<>();

    /**
     * Adds the error.
     * @param error parameter value
     */
    public void addError(String error) {
        this.errors.add(error);
    }

    /**
     * Adds the error.
     * @param type parameter value
     * @param message parameter value
     */
    public void addError(String type, String message) {
        this.errors.add(type + ": " + message);
    }

    /**
     * Returns the errors.
     * @param delimiter parameter value
     * @return operation result
     */
    public String getErrors(String delimiter) {
        return String.join(delimiter, errors);
    }

    /**
     * Returns the errors.
     * @return operation result
     */
    public String getErrors() {
        return String.join("; ", errors);
    }

    /**
     * Returns the errors array.
     * @return operation result
     */
    public List<String> getErrorsArray() {
        return new ArrayList<>(errors);
    }

    /**
     * Returns whether errors.
     * @return operation result
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Performs the no errors operation.
     * @return operation result
     */
    public boolean noErrors() {
        return errors.isEmpty();
    }

    /**
     * Returns whether empty.
     * @return operation result
     */
    public boolean isEmpty() {
        return noErrors();
    }

    /**
     * Performs the clear operation.
     */
    public void clear() {
        errors.clear();
    }
}
