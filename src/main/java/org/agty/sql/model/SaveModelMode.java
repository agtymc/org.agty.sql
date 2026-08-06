package org.agty.sql.model;

public enum SaveModelMode {
    WITH_CHECK, WITHOUT_CHECK,
    INSERT_ONLY, INSERT_ONLY_WITH_CHECK,
    UPDATE_ONLY, UPDATE_ONLY_WITH_CHECK,
    SAVE_OR_SKIP;
}
