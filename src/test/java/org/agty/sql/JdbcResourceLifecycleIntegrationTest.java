package org.agty.sql;

import org.agty.sql.config.AgtySqlConfig;
import org.agty.sql.connect.AgtySqlConnector;
import org.agty.sql.data.Arguments;
import org.agty.sql.data.SqlExpression;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdbcResourceLifecycleIntegrationTest {

    @Test
    @SuppressWarnings("deprecation")
    void statementsAndResultSetsHaveBoundedLifecycle() throws Exception {
        TrackingConnection tracking = new TrackingConnection(DriverManager.getConnection(
                "jdbc:h2:mem:resources_" + UUID.randomUUID().toString().replace("-", "")
                        + ";MODE=MySQL;DB_CLOSE_DELAY=-1"
        ));
        AgtySqlConfig config = new AgtySqlConfig()
                .setDriver("h2")
                .setDatabase("unused")
                .setPfx("")
                .setThrowException(true);

        AgtySQL sql = new AgtySQL(new AgtySqlConnector(config, tracking.proxy()));
        sql.execute("CREATE TABLE resource_data (id BIGINT PRIMARY KEY, string_value VARCHAR(255))", true);

        for (int index = 1; index <= 200; index++) {
            assertEquals(1L, sql.executeUpdate(
                    "INSERT INTO resource_data (id, string_value) VALUES (" + index + ", 'value')"
            ));
            assertFalse(sql.fetch(
                    Arguments.builder()
                            .useStatementPrepare(true)
                            .setTable("resource_data")
                            .setWhere("[id] = ?", index)
            ).isEmpty());
        }
        assertEquals(0, tracking.openStatements());
        assertEquals(0, tracking.openResultSets());

        try (AgtySqlCursor cursor = sql.openCursor(
                Arguments.builder()
                        .useStatementPrepare(true)
                        .setQuery("SELECT * FROM resource_data WHERE id > ?", 0)
                        .setNoRebuildQuery(true)
        )) {
            assertNotNull(cursor);
            assertTrue(cursor.hasNext());
        }
        assertEquals(0, tracking.openStatements());
        assertEquals(0, tracking.openResultSets());

        ResultSet closedByHandle = sql.executeQuery(
                "SELECT * FROM resource_data WHERE id = 1",
                true
        );
        assertEquals(1, tracking.openStatements());
        assertEquals(1, tracking.openResultSets());
        closedByHandle.close();
        assertEquals(0, tracking.openStatements());
        assertEquals(0, tracking.openResultSets());

        ResultSet abandoned = sql.executeResultSet(
                "SELECT * FROM resource_data WHERE id = 2",
                true
        );
        assertNotNull(abandoned);
        assertEquals(1, tracking.openStatements());
        assertEquals(1, tracking.openResultSets());

        sql.close();
        assertTrue(abandoned.isClosed());
        assertEquals(0, tracking.openStatements());
        assertEquals(0, tracking.openResultSets());
        assertTrue(tracking.isClosed());
    }

    @Test
    void agtySqlIsAutoCloseableAndClosesTrackedCursor() throws Exception {
        TrackingConnection tracking = new TrackingConnection(DriverManager.getConnection(
                "jdbc:h2:mem:auto_close;MODE=MySQL;DB_CLOSE_DELAY=-1"
        ));
        AgtySqlCursor cursor;

        try (AgtySQL sql = new AgtySQL(new AgtySqlConnector(
                new AgtySqlConfig().setDriver("h2").setDatabase("unused").setPfx(""),
                tracking.proxy()
        ))) {
            cursor = sql.openCursor(
                    Arguments.builder()
                            .setQuery(SqlExpression.trusted("SELECT 1"))
                            .setNoRebuildQuery(true)
            );
            assertNotNull(cursor);
            assertFalse(cursor.isClosed());
        }

        assertTrue(cursor.isClosed());
        assertEquals(0, tracking.openStatements());
        assertEquals(0, tracking.openResultSets());
    }

    private static final class TrackingConnection implements InvocationHandler {
        private final Connection delegate;
        private final Connection proxy;
        private final AtomicInteger openStatements = new AtomicInteger();
        private final AtomicInteger openResultSets = new AtomicInteger();
        private final AtomicBoolean closed = new AtomicBoolean(false);
        private final Set<TrackingStatement> statements = ConcurrentHashMap.newKeySet();

        private TrackingConnection(Connection delegate) {
            this.delegate = delegate;
            this.proxy = (Connection) Proxy.newProxyInstance(
                    Connection.class.getClassLoader(),
                    new Class<?>[]{Connection.class},
                    this
            );
        }

        private Connection proxy() {
            return proxy;
        }

        private int openStatements() {
            return openStatements.get();
        }

        private int openResultSets() {
            return openResultSets.get();
        }

        private boolean isClosed() {
            return closed.get();
        }

        @Override
        public Object invoke(Object proxyObject, Method method, Object[] args) throws Throwable {
            String name = method.getName();
            if ("close".equals(name)) {
                if (closed.compareAndSet(false, true)) {
                    delegate.close();
                    for (TrackingStatement statement : Set.copyOf(statements)) {
                        statement.markClosed();
                    }
                }
                return null;
            }
            if ("isClosed".equals(name)) {
                return closed.get() || delegate.isClosed();
            }

            try {
                Object result = method.invoke(delegate, args);
                if (result instanceof PreparedStatement preparedStatement) {
                    return trackStatement(preparedStatement, PreparedStatement.class);
                }
                if (result instanceof Statement statement) {
                    return trackStatement(statement, Statement.class);
                }
                return result;
            } catch (InvocationTargetException exception) {
                throw exception.getCause();
            }
        }

        private Statement trackStatement(Statement statement, Class<?> statementInterface) {
            TrackingStatement handler = new TrackingStatement(statement);
            Statement statementProxy = (Statement) Proxy.newProxyInstance(
                    Statement.class.getClassLoader(),
                    new Class<?>[]{statementInterface},
                    handler
            );
            handler.setProxy(statementProxy);
            statements.add(handler);
            openStatements.incrementAndGet();
            return statementProxy;
        }

        private final class TrackingStatement implements InvocationHandler {
            private final Statement delegateStatement;
            private final AtomicBoolean closedStatement = new AtomicBoolean(false);
            private final Set<TrackingResultSet> resultSets = ConcurrentHashMap.newKeySet();
            private Statement statementProxy;

            private TrackingStatement(Statement delegateStatement) {
                this.delegateStatement = delegateStatement;
            }

            private void setProxy(Statement statementProxy) {
                this.statementProxy = statementProxy;
            }

            @Override
            public Object invoke(Object proxyObject, Method method, Object[] args) throws Throwable {
                String name = method.getName();
                if ("close".equals(name)) {
                    if (closedStatement.compareAndSet(false, true)) {
                        delegateStatement.close();
                        markResultSetsClosed();
                        statements.remove(this);
                        openStatements.decrementAndGet();
                    }
                    return null;
                }
                if ("isClosed".equals(name)) {
                    return closedStatement.get() || delegateStatement.isClosed();
                }
                try {
                    Object result = method.invoke(delegateStatement, args);
                    if (result instanceof ResultSet resultSet) {
                        return trackResultSet(resultSet);
                    }
                    return result;
                } catch (InvocationTargetException exception) {
                    throw exception.getCause();
                }
            }

            private ResultSet trackResultSet(ResultSet resultSet) {
                TrackingResultSet handler = new TrackingResultSet(resultSet, this);
                ResultSet resultSetProxy = (ResultSet) Proxy.newProxyInstance(
                        ResultSet.class.getClassLoader(),
                        new Class<?>[]{ResultSet.class},
                        handler
                );
                resultSets.add(handler);
                openResultSets.incrementAndGet();
                return resultSetProxy;
            }

            private void markResultSetsClosed() {
                for (TrackingResultSet resultSet : Set.copyOf(resultSets)) {
                    resultSet.markClosed();
                }
            }

            private void markClosed() {
                if (closedStatement.compareAndSet(false, true)) {
                    markResultSetsClosed();
                    statements.remove(this);
                    openStatements.decrementAndGet();
                }
            }
        }

        private final class TrackingResultSet implements InvocationHandler {
            private final ResultSet delegateResultSet;
            private final TrackingStatement statement;
            private final AtomicBoolean closedResultSet = new AtomicBoolean(false);

            private TrackingResultSet(ResultSet delegateResultSet, TrackingStatement statement) {
                this.delegateResultSet = delegateResultSet;
                this.statement = statement;
            }

            @Override
            public Object invoke(Object proxyObject, Method method, Object[] args) throws Throwable {
                String name = method.getName();
                if ("close".equals(name)) {
                    if (closedResultSet.compareAndSet(false, true)) {
                        delegateResultSet.close();
                        statement.resultSets.remove(this);
                        openResultSets.decrementAndGet();
                    }
                    return null;
                }
                if ("isClosed".equals(name)) {
                    return closedResultSet.get() || delegateResultSet.isClosed();
                }
                if ("getStatement".equals(name)) {
                    return statement.statementProxy;
                }
                try {
                    return method.invoke(delegateResultSet, args);
                } catch (InvocationTargetException exception) {
                    throw exception.getCause();
                }
            }

            private void markClosed() {
                if (closedResultSet.compareAndSet(false, true)) {
                    statement.resultSets.remove(this);
                    openResultSets.decrementAndGet();
                }
            }
        }
    }
}
