package org.agty.sql.support;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/** Creates a ResultSet handle that owns and closes its originating Statement. */
public final class ManagedResultSet {

    private ManagedResultSet() {
    }

    public static ResultSet wrap(
            ResultSet resultSet,
            Statement statement,
            Consumer<ResultSet> closeCallback
    ) {
        if (resultSet == null) {
            throw new IllegalArgumentException("ResultSet must not be null");
        }
        if (statement == null) {
            throw new IllegalArgumentException("Statement must not be null");
        }

        Handler handler = new Handler(resultSet, statement, closeCallback);
        ResultSet proxy = (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class<?>[]{ResultSet.class},
                handler
        );
        handler.setProxy(proxy);
        return proxy;
    }

    private static final class Handler implements InvocationHandler {
        private final ResultSet resultSet;
        private final Statement statement;
        private final Consumer<ResultSet> closeCallback;
        private final AtomicBoolean closed = new AtomicBoolean(false);
        private ResultSet proxy;

        private Handler(
                ResultSet resultSet,
                Statement statement,
                Consumer<ResultSet> closeCallback
        ) {
            this.resultSet = resultSet;
            this.statement = statement;
            this.closeCallback = closeCallback;
        }

        private void setProxy(ResultSet proxy) {
            this.proxy = proxy;
        }

        @Override
        public Object invoke(Object proxyObject, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            if ("close".equals(methodName)) {
                close();
                return null;
            }
            if ("isClosed".equals(methodName)) {
                return closed.get() || resultSet.isClosed();
            }
            if ("getStatement".equals(methodName)) {
                return statement;
            }
            if ("toString".equals(methodName)) {
                return "ManagedResultSet[" + resultSet + "]";
            }
            if ("hashCode".equals(methodName)) {
                return System.identityHashCode(proxyObject);
            }
            if ("equals".equals(methodName)) {
                return proxyObject == (args == null ? null : args[0]);
            }
            if (closed.get()) {
                throw new SQLException("ResultSet is closed");
            }

            try {
                return method.invoke(resultSet, args);
            } catch (InvocationTargetException exception) {
                throw exception.getCause();
            }
        }

        private void close() throws SQLException {
            if (!closed.compareAndSet(false, true)) {
                return;
            }

            SQLException failure = null;
            try {
                resultSet.close();
            } catch (SQLException exception) {
                failure = exception;
            }
            try {
                statement.close();
            } catch (SQLException exception) {
                if (failure == null) {
                    failure = exception;
                } else {
                    failure.addSuppressed(exception);
                }
            } finally {
                if (closeCallback != null) {
                    closeCallback.accept(proxy);
                }
            }

            if (failure != null) {
                throw failure;
            }
        }
    }
}
