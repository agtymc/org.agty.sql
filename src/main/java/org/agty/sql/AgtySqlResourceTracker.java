package org.agty.sql;

import org.agty.sql.data.Arguments;
import org.agty.sql.support.ManagedResultSet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Tracks JDBC resources whose lifecycle is owned by one facade instance.
 */
final class AgtySqlResourceTracker {
    private final Set<ResultSet> resultSets = ConcurrentHashMap.newKeySet();
    private final Set<AgtySqlCursor> cursors = ConcurrentHashMap.newKeySet();

    ResultSet manage(ResultSet resultSet, Statement statement) {
        ResultSet managed = ManagedResultSet.wrap(resultSet, statement, resultSets::remove);
        resultSets.add(managed);
        return managed;
    }

    AgtySqlCursor createCursor(ResultSet resultSet, Arguments arguments) {
        if (resultSet == null) {
            return null;
        }
        AgtySqlCursor cursor = new AgtySqlCursor(resultSet, arguments, cursors::remove);
        cursors.add(cursor);
        return cursor;
    }

    void closeAll(Consumer<Exception> failureHandler) {
        for (AgtySqlCursor cursor : List.copyOf(cursors)) {
            try {
                cursor.close();
            } catch (RuntimeException exception) {
                failureHandler.accept(exception);
            }
        }
        for (ResultSet resultSet : Set.copyOf(resultSets)) {
            try {
                resultSet.close();
            } catch (SQLException exception) {
                failureHandler.accept(exception);
            }
        }
    }
}
