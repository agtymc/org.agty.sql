package org.agty.sql.data;

import org.agty.sql.support.SqlIdentifierValidator;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * Keeps legacy-rendered and prepared values aligned in insertion order.
 */
final class ArgumentDataStore {
    private final Map<String, Object> legacyValues = new LinkedHashMap<>();
    private final Map<String, Object> preparedValues = new LinkedHashMap<>();

    void put(String field, Object legacyValue, Object preparedValue) {
        SqlIdentifierValidator.requireColumn(field, "data field");
        legacyValues.put(field, legacyValue);
        preparedValues.put(field, preparedValue);
    }

    void remove(String field) {
        legacyValues.remove(field);
        preparedValues.remove(field);
    }

    void clear() {
        legacyValues.clear();
        preparedValues.clear();
    }

    boolean isEmpty() {
        return legacyValues.isEmpty();
    }

    int size() {
        return legacyValues.size();
    }

    LinkedHashMap<String, Object> copy(boolean prepared) {
        return new LinkedHashMap<>(selected(prepared));
    }

    Object get(String key, boolean prepared) {
        return selected(prepared).get(key);
    }

    LinkedList<String> keys() {
        return new LinkedList<>(legacyValues.keySet());
    }

    LinkedList<Object> values(boolean prepared) {
        Collection<Object> values = selected(prepared).values();
        return new LinkedList<>(values);
    }

    Set<Map.Entry<String, Object>> legacyEntries() {
        return legacyValues.entrySet();
    }

    private Map<String, Object> selected(boolean prepared) {
        return prepared ? preparedValues : legacyValues;
    }
}
