package org.agty.sql;

import org.agty.sql.dialect.DialectDriverRegistry;
import org.agty.sql.base.Field;
import org.agty.sql.base.RowData;
import org.agty.sql.base.RowDataEmpty;
import org.agty.sql.config.AgtySqlConfig;
import org.agty.sql.connect.AgtySqlConnector;
import org.agty.sql.data.SqlQueryRebuild;
import org.agty.sql.data.ListResultSet;
import org.agty.sql.driver.DialectCapabilities;
import org.agty.sql.exceptions.AgtySqlException;
import org.agty.sql.data.Arguments;
import org.agty.sql.interfaces.SqlRow;
import org.agty.sql.model.ModelControl;
import org.agty.sql.model.SaveModelMode;
import org.agty.sql.model.builders.ObjectBuilder;
import org.agty.sql.operations.DeleteOperation;
import org.agty.sql.operations.FetchOperation;
import org.agty.sql.operations.InsertOperation;
import org.agty.sql.operations.ListOperation;
import org.agty.sql.operations.MetadataOperation;
import org.agty.sql.operations.UpdateOperation;
import org.agty.sql.support.DebugMessages;
import org.agty.sql.support.Errors;
import org.agty.sql.support.Logger;
import org.agty.sql.support.RowFactory;
import org.agty.sql.support.SqlTextUtils;
import org.agty.sql.interfaces.Sql;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Main session-like facade of the library.
 *
 * <p>{@code AgtySQL} remains the primary public entry point for {@code 2.x} and
 * combines three documented usage modes:
 * <ul>
 *     <li>high-level CRUD methods such as {@link #fetch(Arguments)},
 *     {@link #insert(Arguments)}, {@link #update(Arguments)},
 *     {@link #delete(Arguments)}, {@link #countRows(Arguments)},
 *     {@link #listArray(Arguments)} and {@link #openCursor(Arguments)};</li>
 *     <li>low-level JDBC/session access via {@link #getConnection()},
 *     {@link #getStatement()}, {@link #prepareStatement(String)},
 *     transactions, batch execution and generated keys helpers;</li>
 *     <li>supported entity-oriented convenience overloads such as
 *     {@link #fetch(Arguments, Object)}, {@link #insertAndGet(Arguments, Object)}
 *     and {@link #updateAndGet(Arguments, Object)}.</li>
 * </ul>
 *
 * <p>Deprecated alias/helper methods are preserved as a compatibility layer for
 * the whole {@code 2.x} lifecycle. New code should target the non-deprecated
 * methods documented above.
 */
public class AgtySQL {
    /** Version */
    final public static String VERSION = "1.26.01";

    /** Connection and statement*/
    private final AgtySqlConnector connector;

    /** Collection of errors*/
    private final Errors errors = new Errors();

    /** Текущий объект (драйвер) SQL*/
    private Sql driverSqlObject;

    /** Internal bridge for operation classes moved out of the root package. */
    private final AgtySqlOperationSupport operationSupport = new AgtySqlOperationSupport(this);
    /** Internal bridge for session/connect lifecycle. */
    private final AgtySqlSessionSupport sessionSupport;

    /** Write operations */
    private final InsertOperation insertOperation = new InsertOperation(operationSupport);
    private final UpdateOperation updateOperation = new UpdateOperation(operationSupport);
    private final FetchOperation fetchOperation = new FetchOperation(operationSupport);
    private final ListOperation listOperation = new ListOperation(operationSupport);
    private final DeleteOperation deleteOperation = new DeleteOperation(operationSupport);
    private final MetadataOperation metadataOperation = new MetadataOperation(operationSupport);

    /** Данные для list() */
    private final List<ListResultSet> listResultSetPool = new ArrayList<>();

    /**
     * Constructor.
     * The default constructor by the default section
     */
    public AgtySQL() {
        connector = new AgtySqlConnector();
        sessionSupport = new AgtySqlSessionSupport(connector);
    }

    /**
     * Constructor.
     * The constructor by the server section
     */
    public AgtySQL(String server) {
        connector = new AgtySqlConnector(server);
        sessionSupport = new AgtySqlSessionSupport(connector);
    }

    /**
     * Constructor.
     * The constructor by the server section into the property file
     */
    public AgtySQL(String server, String path) {
        connector = new AgtySqlConnector(server, path);
        sessionSupport = new AgtySqlSessionSupport(connector);
    }

    /**
     * Constructor.
     * The constructor with AgtySqlConfigBuilder.
     */
    public AgtySQL(AgtySqlConfig agtySqlConfig) {
        connector = new AgtySqlConnector(agtySqlConfig);
        sessionSupport = new AgtySqlSessionSupport(connector);
    }

    /**
     * Constructor.
     * The constructor with AgtySqlConnector.
     */
    public AgtySQL(AgtySqlConnector agtySqlConnector) {
        connector = agtySqlConnector;
        sessionSupport = new AgtySqlSessionSupport(connector);
    }

    /**
     * Connector.
     * @return AgtySqlConnector
     */
    public AgtySqlConnector getConnector() {
        return connector;
    }

    public String lastQuery = "";

    /**
     * Connect to the server DB.
     */
    public AgtySQL connect() {
        try {
            sessionSupport.getConnection();
        } catch (SQLException e) {
            throwError("AgtySQL.connect()", e.getMessage());
        }
        return this;
    }

    /**
     * Назначить текущее количество строк возвращаемое за раз
     * @param stmtRows количество строк.
     */
    public AgtySQL setStmtRows(int stmtRows) {
        sessionSupport.setStmtRows(stmtRows);
        return this;
    }

    /**
     * Кол-во выборки за раз
     */
    public void setFetchSize(int fetchSize) {
        sessionSupport.setFetchSize(fetchSize);
    }

    public int getFetchSize() {
        return sessionSupport.getFetchSize();
    }

    /**
     * Returns the active JDBC connection for manual low-level work.
     *
     * <p>This is a supported public API for {@code 2.x}. The caller is expected
     * to treat the returned connection as session-owned by this {@code AgtySQL}
     * instance and to coordinate transaction/lifecycle operations accordingly.
     *
     * @return active JDBC connection
     */
    public Connection getConnection() {
        try {
            return sessionSupport.getConnection();
        } catch (SQLException e) {
            throwError("AgtySQL.getConnection()", e.getMessage());
        }
        return null;
    }

    /**
     * Creates a JDBC {@link Statement} bound to the current session.
     *
     * <p>Use this when explicit JDBC control is required. For high-level
     * library scenarios prefer {@link #execute(String)},
     * {@link #executeUpdate(String)}, {@link #listArray(Arguments)} or
     * {@link #openCursor(Arguments)}.
     *
     * @return JDBC statement
     */
    public Statement getStatement() {
        try {
            return sessionSupport.getStatement();
        } catch (SQLException e) {
            throwError("AgtySQL.getStatement()", e.getMessage());
        }
        return null;
    }

    /**
     * Creates a prepared statement from a query rebuilt by the library.
     *
     * @param query SQL query in library form
     * @return prepared statement
     */
    public PreparedStatement prepareStatement(String query) {
        try {
            return sessionSupport.prepareStatement(prepareQuery(query));
        } catch (SQLException e) {
            throwError("AgtySQL.prepareStatement()", e.getMessage());
        }
        return null;
    }

    /**
     * Creates a prepared statement with optional query rebuild bypass.
     *
     * @param query SQL query
     * @param noRebuildQuery when {@code true}, the query is used as-is
     * @return prepared statement
     */
    public PreparedStatement prepareStatement(String query, boolean noRebuildQuery) {
        try {
            return sessionSupport.prepareStatement(noRebuildQuery ? query : prepareQuery(query));
        } catch (SQLException e) {
            throwError("AgtySQL.prepareStatement()", e.getMessage());
        }
        return null;
    }

    /**
     * Creates a prepared statement configured for JDBC generated keys.
     *
     * @param query SQL query in library form
     * @param autoGeneratedKeys generated keys mode from {@link Statement}
     * @return prepared statement
     */
    public PreparedStatement prepareStatement(String query, int autoGeneratedKeys) {
        return prepareStatement(query, autoGeneratedKeys, false);
    }

    /**
     * Creates a prepared statement configured for JDBC generated keys with
     * optional query rebuild bypass.
     *
     * @param query SQL query
     * @param autoGeneratedKeys generated keys mode from {@link Statement}
     * @param noRebuildQuery when {@code true}, the query is used as-is
     * @return prepared statement
     */
    public PreparedStatement prepareStatement(String query, int autoGeneratedKeys, boolean noRebuildQuery) {
        try {
            return sessionSupport.prepareStatement(
                    noRebuildQuery ? query : prepareQuery(query),
                    autoGeneratedKeys
            );
        } catch (SQLException e) {
            throwError("AgtySQL.prepareStatement()", e.getMessage());
        }
        return null;
    }

    /**
     * Starts manual transaction control for the current session.
     *
     * <p>Equivalent to {@link #setAutoCommit(boolean) setAutoCommit(false)}.
     */
    public void beginTransaction() {
        setAutoCommit(false);
    }

    public boolean isAutoCommit() {
        try {
            return sessionSupport.isAutoCommit();
        } catch (SQLException e) {
            throwError("AgtySQL.isAutoCommit()", e.getMessage());
        }
        return false;
    }

    /**
     * Executes a batch of queries after regular library query rebuild.
     *
     * @param queries SQL queries
     * @return JDBC batch update counts
     */
    public int[] executeBatch(List<String> queries) {
        return executeBatch(queries, false);
    }

    /**
     * Executes a batch of queries with optional query rebuild bypass.
     *
     * @param queries SQL queries
     * @param noRebuildQuery when {@code true}, each query is used as-is
     * @return JDBC batch update counts
     */
    public int[] executeBatch(List<String> queries, boolean noRebuildQuery) {
        debugMessageEnterInMethod("executeBatch()");

        if (queries == null || queries.isEmpty()) {
            return new int[0];
        }

        clearErrors();

        try (Statement statement = sessionSupport.createBatchStatement()) {
            for (String query : queries) {
                String preparedQuery = noRebuildQuery ? query : prepareQuery(query);
                if (preparedQuery == null) {
                    throwError("AgtySQL.executeBatch()", "Query is empty or NULL");
                    return new int[0];
                }
                logQuery(preparedQuery);
                statement.addBatch(preparedQuery);
            }

            return statement.executeBatch();
        } catch (SQLException e) {
            throwError("AgtySQL.executeBatch()", e.getMessage());
        }

        return new int[0];
    }

    /**
     * Reads JDBC generated keys from an already executed prepared statement.
     *
     * @param preparedStatement executed prepared statement
     * @return first generated-keys row mapped into {@link SqlRow}
     */
    public SqlRow getGeneratedKeys(PreparedStatement preparedStatement) {
        return getGeneratedKeys(preparedStatement, false);
    }

    /**
     * Reads JDBC generated keys from an already executed prepared statement.
     *
     * @param preparedStatement executed prepared statement
     * @param convertValueToString whether generated values should be converted to strings
     * @return first generated-keys row mapped into {@link SqlRow}
     */
    public SqlRow getGeneratedKeys(PreparedStatement preparedStatement, boolean convertValueToString) {
        try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
            return getFetchRow(
                    resultSet,
                    Arguments.builder().convertValueToString(convertValueToString)
            );
        } catch (SQLException e) {
            throwError("AgtySQL.getGeneratedKeys()", e.getMessage());
        }

        return RowFactory.emptyRow();
    }

    /**
     * Выполнить запрос
     */
    public void commit() {
        try {
            sessionSupport.commit();
        } catch (SQLException e) {
            throwError("AgtySqlConnector.commit()", e.getMessage());
        }
    }

    /**
     * Autocommit
     */
    public void setAutoCommit(boolean autocommit) {
        try {
            sessionSupport.setAutoCommit(autocommit);
        } catch (SQLException e) {
            throwError("AgtySqlConnector.setAutoCommit()", e.getMessage());
        }
    }

    /**
     * Откатить запрос
     */
    public void rollback() {
        try {
            sessionSupport.rollback();
        } catch (SQLException e) {
            throwError("AgtySqlConnector.rollback()", e.getMessage());
        }
    }

    /**
     * Закрывает все соединения и очищает временные данные
     */
    public void close() {
        try {
            closeListCursors();
            sessionSupport.close();
            clearErrors();
        } catch (SQLException e) {
            throwError("AgtySQL.close()", e.getMessage());
        }
    }

    /**
     * Добавляем в массив ошибок и сбрасываем в лог
     */
    public void setAndLogError(String type, String error) {
        errors.addError(type, error);
        logError(type, error);
    }

    /**
     * Add an error to log
     */
    private void logError(String type, String error) {
        if (getConfig().isLogErrors()) {
            try {
                new Logger(getConfig().getLogErrorsFileOrDefault("logs/log.log")).append(type  + " -> " + error);
            } catch (IOException e) {
                errors.addError("AgtySQL.setAndLogError()", e.getMessage());
            }
        }
    }

    /**
     * Сбрасывает запрос в лог
     * @param query строка запроса
     */
    private void logQuery(String query) {
        lastQuery = query;

        if (getConfig().isLogQuery()) {
            try {
                new Logger(getConfig().getLogQueryFileOrDefault("logs/query.log")).append(query);
            } catch (IOException e) {
                throwError("AgtySQL.logQuery()", e.getMessage());
            }
        }
    }

    public String getLastQuery() {
        return lastQuery;
    }

    /**
     * True if errors are existing
     * @return bool true if exists
     */
    public boolean hasErrors() {
        return errors.hasErrors();
    }

    /**
     * Вернуть все ошибки в виде строки
     */
    public String getErrors() {
        return errors.getErrors();
    }

    /**
     * Вернуть все ошибки в виде строки через разделитель
     */
    public String getErrors(String delimiter) {
        return errors.getErrors(delimiter);
    }

    /**
     * Вернуть все ошибки в виде List
     */
    public List<String> getErrorsArray() {
        return errors.getErrorsArray();
    }

    /**
     * Очистить ошибки
     */
    public void clearErrors() {
        errors.clear();
    }

    /**
     * Конфиг.
     *
     * @return объект типа AgtySqlConfig.
     */
    public AgtySqlConfig getConfig() {
        return getConnector().getConfig();
    }

    /**
     * Документированные возможности активного диалекта.
     */
    public DialectCapabilities getDialectCapabilities() {
        return getDriverSqlObject().getCapabilities();
    }

    /**
     * Текущий объект (драйвер) SQL
     */
    private Sql getDriverSqlObject() {
        if (driverSqlObject == null) {
            driverSqlObject = DialectDriverRegistry.getDialect(getConfig().getDriver(), this);
        }
        return driverSqlObject;
    }

    Sql getDriverSqlObjectInternal() {
        return getDriverSqlObject();
    }

    /**
     * Сбрасывает ошибку в лог и в исключение.
     * @param type тип ошибки.
     * @param message сообщение ошибки.
     */
    private void throwError(String type, String message) {
        setAndLogError(type, message);
        throwException(type, message);
    }

    void throwErrorInternal(String type, String message) {
        throwError(type, message);
    }

    /**
     * Вызывает исключение
     * @param type тип ошибки.
     * @param message сообщение ошибки.
     */
    private void throwException(String type, String message) {
        if (getConfig().isThrowException()) {
            throw new AgtySqlException(type, message);
        }
    }

    /**
     * Выводит сообщение в консоль.
     *
     * @param type тип сообщения (обычно Class.method()).
     * @param message сообщение.
     */
    private void debugMessage(String type, String message) {
       if (getConfig().isDebug()) DebugMessages.print(type, message);
    }

    void debugMessageInternal(String type, String message) {
        debugMessage(type, message);
    }

    /**
     * Выводит сообщение о нахождении в методе.
     *
     * @param method наименование метода.
     */
    private void debugMessageEnterInMethod(String method) {
        debugMessage("AgtySQL." + method, "Enter in method");
    }

    void debugMessageEnterInMethodInternal(String method) {
        debugMessageEnterInMethod(method);
    }

    /**
     * Проверяет наличие запроса в Arguments.
     * 
     * @param arguments объект Arguments.
     * @return true если запрос не пустой.
     */
    private boolean hasQuery(Arguments arguments) {
        return hasQuery(arguments.getQuery());
    }

    boolean hasQueryInternal(Arguments arguments) {
        return hasQuery(arguments);
    }

    /**
     * Проверяет наличие запроса.
     *
     * @param query строка запроса.
     * @return true если запрос не пустой.
     */
    private boolean hasQuery(String query) {
        return query != null && query.length() >= 3;
    }

    boolean hasQueryInternal(String query) {
        return hasQuery(query);
    }

    /**
     * Метод ищет все названия таблиц, которые записаны {table_name} и меняет
     * на название с префиксом: `pfx_table_name`. Так же заменяются наименования
     * полей в скобках [fieldName] на названия в кавычках "fieldName".
     * Тип кавычек зависит от драйвера базы данных.
     *
     * @return String query
     */
    public String rebuildQuery(String query) {
        if (getConfig().noRequery()) return query;
        return getSqlRebuildQuery().setQuery(query).rebuildAndGet();
    }

    /**
     * Метод ищет все названия таблиц, которые записаны {table_name} и меняет
     * на название с префиксом: `pfx_table_name`.
     *
     * @return String table
     */
    public String rebuildTable(String table) {
        return getSqlRebuildQuery().tablePrefixNoQuote(table);
    }

    /**
     * Объект SqlRequery с предопределенными свойствами.
     *
     * @return SqlRequery объект.
     */
    protected SqlQueryRebuild getSqlRebuildQuery() {
        return new SqlQueryRebuild()
                .setDebug(getConfig().isDebug())
                .setPrefix(getConfig().getPfx())
                .setQuoteColumn(getDriverSqlObject().getQuoteColumn())
                .setQuoteTable(getDriverSqlObject().getQuoteTable())
                .setQuoteValue(getDriverSqlObject().getQuoteValue());
    }

    /**
     * Выполнить запрос exec().
     *
     * @param query запрос.
     * @return true, если ошибок нет.
     * @deprecated Compatibility layer for {@code 2.x}. Use {@link #execute(String)}
     * for facade-style execution or {@link #getStatement()} for raw JDBC access.
     */
    @Deprecated
    public boolean statementExecute(String query) {
        debugMessageEnterInMethod("statementExecute()");

        try {
            logQuery(query);
            return getConnector().getStatement().execute(query);
        } catch (Exception e) {
            throwError("AgtySQL.statementExecute()", e.getMessage() + "\n[" + query + "]\n");
        }

        return false;
    }

    /**
     * Выполнить запрос exec().
     *
     * @param query запрос.
     * @return true, если ошибок нет.
     * @deprecated Compatibility layer for {@code 2.x}. Use
     * {@link #prepareStatement(String)} with {@code executeQuery()} or
     * {@link #openCursor(String)}.
     */
    @Deprecated
    public ResultSet statementExecuteResultSet(String query) {
        debugMessageEnterInMethod("statementExecuteResultSet()");

        try {
            logQuery(query);
            Statement statement = getConnector().getStatement();
            statement.execute(query);
            return statement.getResultSet();
        } catch (SQLException e) {
            throwError("AgtySQL.statementExecuteResultSet()",  e.getMessage() + "\n[" + query + "]\n");
        }

        return null;
    }

    /**
     * Выполнить запрос execQuery().
     *
     * @param query запрос.
     * @return true, если ошибок нет.
     * @deprecated Compatibility layer for {@code 2.x}. Use
     * {@link #prepareStatement(String)} with {@code executeQuery()} or
     * {@link #openCursor(String)}.
     */
    @Deprecated
    public ResultSet statementExecuteQuery(String query) {
        debugMessageEnterInMethod("statementExecuteQuery()");

        try {
            logQuery(query);
            //System.out.println(query);
            return getConnector().getStatement().executeQuery(query);
            //Statement statement = getConnector().getStatement();
            //statement.execute(query);
            //return statement.getResultSet();
        } catch (Exception e) {
            throwError("AgtySQL.statementExecuteQuery()", e.getMessage() + "\n[" + query + "]\n");
        }

        return null;
    }

    /**
     * Выполнить запрос executeUpdate().
     *
     * @param query запрос.
     * @return кол-во затронутых строк.
     * @deprecated Compatibility layer for {@code 2.x}. Use
     * {@link #executeUpdate(String)} or {@link #prepareStatement(String)} with
     * {@code executeUpdate()}.
     */
    @Deprecated
    public Integer statementExecuteUpdate(String query) {
        debugMessageEnterInMethod("statementExecuteUpdate()");

        try {
            logQuery(query);
            return getConnector().getStatement().executeUpdate(query);
        } catch (Exception e) {
            throwError("AgtySQL.statementExecuteUpdate()", e.getMessage() + "\n[" + query + "]\n");
        }

        return null;
    }

    /**
     * DONE. Выполнить запрос executeLargeUpdate().
     *
     * @param query запрос.
     * @return кол-во затронутых строк.
     * @deprecated Compatibility layer for {@code 2.x}. Use
     * {@link #executeUpdate(String)} or {@link #prepareStatement(String)} with
     * {@code executeLargeUpdate()}.
     */
    @Deprecated
    public Long statementExecuteLargeUpdate(String query) {
        debugMessageEnterInMethod("statementExecuteLargeUpdate()");

        try {
            logQuery(query);
            return getConnector().getStatement().executeLargeUpdate(query);
        } catch (SQLException e) {
            throwError("AgtySQL.statementExecuteLargeUpdate()", e.getMessage() + "\n[" + query + "]\n");
        }

        return 0L;
    }

    /**
     * Check and prepare the query.
     *
     * @param query The query
     * @return String query
     */
    private String prepareQuery(String query) {
        debugMessageEnterInMethod("prepareExecute()");

        //Очищаем ошибки перед выполнением
        clearErrors();

        if (!hasQuery(query)) {
            setAndLogError("AgtySQL.prepareExecute()", "Query is empty or NULL");
            return null;
        }

        query = rebuildQuery(query);

        query = SqlTextUtils.removeUnsupportedChars(query);

        debugMessage("AgtySQL.prepareExecute()", query);

        return query;
    }

    /**
     * Формирует и отправляет запрос.
     *
     * @param query запрос
     * @return result
     */
    public boolean execute(String query) {
        debugMessageEnterInMethod("execute(String query)");
        return execute(query, false);
    }

    public boolean execute(String query, boolean noRebuildQuery) {
        debugMessageEnterInMethod("execute(String query, boolean noRebuildQuery)");
        return statementExecute(noRebuildQuery ? query : prepareQuery(query));
    }

    /**
     * Legacy helper returning a raw ResultSet.
     * Prefer getStatement()/prepareStatement() for JDBC control or openCursor() for streaming.
     *
     * @deprecated Compatibility layer for {@code 2.x}. Use
     * {@link #prepareStatement(String, boolean)} with {@code executeQuery()} or
     * {@link #openCursor(String)}.
     */
    @Deprecated
    public ResultSet executeResultSet(String query, boolean noRebuildQuery) {
        debugMessageEnterInMethod("executeResultSet(String query, boolean noRebuildQuery)");
        return statementExecuteResultSet(noRebuildQuery ? query : prepareQuery(query));
    }

    /**
     * Формирует и отправляет запрос.
     *
     * @param query запрос
     * @return result
     * @deprecated Compatibility layer for {@code 2.x}. Use
     * {@link #prepareStatement(String, boolean)} with {@code executeQuery()} or
     * {@link #openCursor(String)}.
     */
    @Deprecated
    public ResultSet executeQuery(String query, boolean noRebuildQuery) {
        debugMessageEnterInMethod("executeQuery()");
        return statementExecuteQuery(noRebuildQuery ? query : prepareQuery(query));
    }

    /**
     * Отправляет запрос на изменение данных
     *
     * @param query String
     */
    public Long executeUpdate(String query) {
        debugMessageEnterInMethod("executeUpdate()");

        Long result = 0L;

        query = prepareQuery(query);

        if (query != null) {
            if (getDriverSqlObject().isSupportLargeUpdate()) {
                result = statementExecuteLargeUpdate(query);
            } else {
                Integer res = statementExecuteUpdate(query);
                if (res != null) {
                    result = (long) res;
                }
            }
        }

        debugMessage("AgtySQL.executeUpdate()", "Affected rows: " + result);

        return result;
    }

    /**
     * Возвращает строку с данными.
     *
     * @param arguments объект Arguments.
     * @return SqlRow
     */
    final public SqlRow fetch(Arguments arguments) {
        debugMessageEnterInMethod("fetch()");
        return fetchOperation.fetch(arguments);
    }

    /**
     * Fetch data from DB and convert into an entity.
     * Supported convenience layer over the regular fetch(...) flow.
     *
     * @param arguments Arguments
     * @param object Entity
     * @return Entity
     * @param <T> Entity
     */
    final public <T> T fetch(Arguments arguments, T object) {
        return fetchOperation.fetchEntity(arguments, object);
    }

    /**
     * Fetch data from DB and convert into an entity.
     * Supported convenience layer over the regular fetch(...) flow.
     *
     * @param arguments Arguments
     * @param clazz Class<?>
     * @return Entity
     * @param <T> Entity
     */
    final public <T> T fetch(Arguments arguments, Class<?> clazz) {
        return fetchOperation.fetchEntity(arguments, clazz);
    }

    /**
     * Возвращает строку с данными.
     * В качестве параметра принимает строку запроса
     *
     * @param query строка запроса.
     * @return SqlRow
     */
    final public SqlRow fetch(String query) {
        return fetch(new Arguments().setQuery(query));
    }

    /**
     * Fetch data from DB and convert into an entity.
     * Supported convenience layer over the regular fetch(String) flow.
     *
     * @param query A query string.
     * @return Entity
     */
    final public <T> T fetch(String query, T object) {
        return fetch(new Arguments().setQuery(query), object);
    }

    /**
     * Fetch data from DB and convert into an entity.
     * Supported convenience layer over the regular fetch(String) flow.
     *
     * @param query A query string.
     * @return Entity
     */
    final public <T> T fetch(String query,  Class<?> clazz) {
        return fetch(new Arguments().setQuery(query), clazz);
    }

    /**
     * Формирует и возвращает строку с данными.
     *
     * @param resultSet ResultSet
     * @param arguments Arguments
     * @return SqlRow
     */
    private SqlRow getFetchRow(ResultSet resultSet, Arguments arguments) throws SQLException {
        debugMessageEnterInMethod("getFetchRow()");

        SqlRow returnData = RowFactory.newSqlRow();
        returnData.setDataIsString(arguments.convertValueToString());

        if (errors.hasErrors() || resultSet == null) return RowFactory.emptyRow(); //Пустая строка

        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int columns = resultSetMetaData.getColumnCount();

        if (resultSet.next()) {
            for (int i = 1; i <= columns; ++i) {
                returnData.setData(
                        resultSetMetaData.getColumnName(i),
                        arguments.convertValueToString() ? resultSet.getString(i) : resultSet.getObject(i)
                );
            }
        } //if (resultSet.next()) {

        return returnData;
    }

    SqlRow getFetchRowInternal(ResultSet resultSet, Arguments arguments) throws SQLException {
        return getFetchRow(resultSet, arguments);
    }

    ResultSet getListResultSetInternal(int index) {
        ListResultSet listResultSet = getListResultSet(index);
        return listResultSet == null ? null : listResultSet.getResultSet();
    }

    void setListResultSetInternal(ResultSet resultSet, int index) {
        setListResultSet(new ListResultSet(resultSet), index);
    }

    boolean hasListResultSetInternal(int index) {
        return hasListResultSet(index);
    }

    void clearListResultSetInternal(int index) {
        closeListCursor(index);
    }

    /**
     * Вставить строку в таблицу.
     * TODO: подумать, как сразу вернуть ID без дополнительного запроса
     *
     * @param arguments объект Arguments.
     * @return ID вставленной строки.
     */
    final public long insert(Arguments arguments) {
        return insertOperation.insert(arguments);
    }

    /**
     * Insert data and map the returned row into an entity.
     * Short form over insertAndGet(...).
     *
     * @param arguments Arguments
     * @param object entity instance used as a target type marker
     * @param <T> entity type
     * @return mapped entity or null if no row was returned
     */
    final public <T> T insert(Arguments arguments, T object) {
        return insertAndGet(arguments, object);
    }

    /**
     * Insert data and map the returned row into an entity.
     * Short form over insertAndGet(...).
     *
     * @param arguments Arguments
     * @param clazz entity class
     * @param <T> entity type
     * @return mapped entity or null if no row was returned
     */
    final public <T> T insert(Arguments arguments, Class<?> clazz) {
        return insertAndGet(arguments, clazz);
    }

    /**
     * Insert data by a raw query and map the returned row into an entity.
     * Short form over insertAndGet(...).
     *
     * @param query SQL query
     * @param object entity instance used as a target type marker
     * @param <T> entity type
     * @return mapped entity or null if no row was returned
     */
    final public <T> T insert(String query, T object) {
        return insertAndGet(query, object);
    }

    /**
     * Insert data by a raw query and map the returned row into an entity.
     * Short form over insertAndGet(...).
     *
     * @param query SQL query
     * @param clazz entity class
     * @param <T> entity type
     * @return mapped entity or null if no row was returned
     */
    final public <T> T insert(String query, Class<?> clazz) {
        return insertAndGet(query, clazz);
    }

    /**
     * Вставить строку в таблицу.
     *
     * @param arguments объект Arguments.
     * @return ID вставленной строки.
     */
    final public SqlRow insertAndGet(Arguments arguments, String fields) {
        debugMessageEnterInMethod("insertAndGet()");
        return insertOperation.insertAndGet(arguments, fields);
    }

    final public SqlRow insertAndGet(Arguments arguments) {
        return insertAndGet(arguments, "*");
    }

    /**
     * Insert data by a raw query and return one row.
     *
     * @param query SQL query
     * @return SqlRow
     */
    final public SqlRow insertAndGet(String query) {
        return insertAndGet(new Arguments().setQuery(query));
    }

    /**
     * Insert data by a raw query and return one row with requested fields.
     *
     * @param query SQL query
     * @param fields returning fields
     * @return SqlRow
     */
    final public SqlRow insertAndGet(String query, String fields) {
        return insertAndGet(new Arguments().setQuery(query), fields);
    }

    /**
     * Insert data and map the returned row into an entity.
     *
     * @param arguments Arguments
     * @param object entity instance used as a target type marker
     * @param <T> entity type
     * @return mapped entity or null if no row was returned
     */
    final public <T> T insertAndGet(Arguments arguments, T object) {
        return mapSqlRowToEntity(insertAndGet(arguments), object);
    }

    /**
     * Insert data and map the returned row into an entity.
     *
     * @param arguments Arguments
     * @param clazz entity class
     * @param <T> entity type
     * @return mapped entity or null if no row was returned
     */
    final public <T> T insertAndGet(Arguments arguments, Class<?> clazz) {
        return mapSqlRowToEntity(insertAndGet(arguments), clazz);
    }

    /**
     * Insert data by a raw query and map the returned row into an entity.
     *
     * @param query SQL query
     * @param object entity instance used as a target type marker
     * @param <T> entity type
     * @return mapped entity or null if no row was returned
     */
    final public <T> T insertAndGet(String query, T object) {
        return mapSqlRowToEntity(insertAndGet(query), object);
    }

    /**
     * Insert data by a raw query and map the returned row into an entity.
     *
     * @param query SQL query
     * @param clazz entity class
     * @param <T> entity type
     * @return mapped entity or null if no row was returned
     */
    final public <T> T insertAndGet(String query, Class<?> clazz) {
        return mapSqlRowToEntity(insertAndGet(query), clazz);
    }

    /**
     * Вставить строку в таблицу.
     *
     * @param arguments объект Arguments.
     * @return ID вставленной строки.
     */
    final public SqlRow updateAndGet(Arguments arguments, String fields) {
        debugMessageEnterInMethod("updateAndGet()");
        return updateOperation.updateAndGet(arguments, fields);
    }

    final public SqlRow updateAndGet(Arguments arguments) {
        return updateAndGet(arguments, "*");
    }

    /**
     * Update data by a raw query and return one row.
     *
     * @param query SQL query
     * @return SqlRow
     */
    final public SqlRow updateAndGet(String query) {
        return updateAndGet(new Arguments().setQuery(query));
    }

    /**
     * Update data by a raw query and return one row with requested fields.
     *
     * @param query SQL query
     * @param fields returning fields
     * @return SqlRow
     */
    final public SqlRow updateAndGet(String query, String fields) {
        return updateAndGet(new Arguments().setQuery(query), fields);
    }

    /**
     * Update data and map the returned row into an entity.
     *
     * @param arguments Arguments
     * @param object entity instance used as a target type marker
     * @param <T> entity type
     * @return mapped entity or null if no row was returned
     */
    final public <T> T updateAndGet(Arguments arguments, T object) {
        return mapSqlRowToEntity(updateAndGet(arguments), object);
    }

    /**
     * Update data and map the returned row into an entity.
     *
     * @param arguments Arguments
     * @param clazz entity class
     * @param <T> entity type
     * @return mapped entity or null if no row was returned
     */
    final public <T> T updateAndGet(Arguments arguments, Class<?> clazz) {
        return mapSqlRowToEntity(updateAndGet(arguments), clazz);
    }

    /**
     * Update data by a raw query and map the returned row into an entity.
     *
     * @param query SQL query
     * @param object entity instance used as a target type marker
     * @param <T> entity type
     * @return mapped entity or null if no row was returned
     */
    final public <T> T updateAndGet(String query, T object) {
        return mapSqlRowToEntity(updateAndGet(query), object);
    }

    /**
     * Update data by a raw query and map the returned row into an entity.
     *
     * @param query SQL query
     * @param clazz entity class
     * @param <T> entity type
     * @return mapped entity or null if no row was returned
     */
    final public <T> T updateAndGet(String query, Class<?> clazz) {
        return mapSqlRowToEntity(updateAndGet(query), clazz);
    }

    /**
     * Возвращает ID последней вставленной строки
     *
     * @return mixed
     * @deprecated Compatibility layer for {@code 2.x}. Use
     * {@link #insert(Arguments)} with {@code setReturnLastInsertId(true)} or
     * {@link #getGeneratedKeys(PreparedStatement)}.
     */
    @Deprecated
    final public Long lastInsertId(Arguments arguments) {
        return metadataOperation.lastInsertId(arguments);
    }

    /**
     * Вставка множества строк в таблицу.
     */
    final public void insert(ArrayList<Arguments> arguments) {
        insertOperation.insert(arguments);
    }

    /**
     * Метод обеспечивающий вставку строки или группы строк в таблицу
     *
     * @return boolean
     */
    final public boolean update(Arguments arguments) {
        return updateOperation.update(arguments);
    }

    /**
     * Update data and map the returned row into an entity.
     * Short form over updateAndGet(...).
     *
     * @param arguments Arguments
     * @param object entity instance used as a target type marker
     * @param <T> entity type
     * @return mapped entity or null if no row was returned
     */
    final public <T> T update(Arguments arguments, T object) {
        return updateAndGet(arguments, object);
    }

    /**
     * Update data and map the returned row into an entity.
     * Short form over updateAndGet(...).
     *
     * @param arguments Arguments
     * @param clazz entity class
     * @param <T> entity type
     * @return mapped entity or null if no row was returned
     */
    final public <T> T update(Arguments arguments, Class<?> clazz) {
        return updateAndGet(arguments, clazz);
    }

    /**
     * Update data by a raw query and map the returned row into an entity.
     * Short form over updateAndGet(...).
     *
     * @param query SQL query
     * @param object entity instance used as a target type marker
     * @param <T> entity type
     * @return mapped entity or null if no row was returned
     */
    final public <T> T update(String query, T object) {
        return updateAndGet(query, object);
    }

    /**
     * Update data by a raw query and map the returned row into an entity.
     * Short form over updateAndGet(...).
     *
     * @param query SQL query
     * @param clazz entity class
     * @param <T> entity type
     * @return mapped entity or null if no row was returned
     */
    final public <T> T update(String query, Class<?> clazz) {
        return updateAndGet(query, clazz);
    }

    /**
     * Метод удаляющий строки из таблицы.
     *
     * @param arguments объект Arguments
     */
    final public boolean delete(Arguments arguments) {
        debugMessageEnterInMethod("delete()");
        return deleteOperation.delete(arguments);
    }

    /**
     * Alias for AgtySQL.delete() method.
     *
     * @param arguments объект Arguments.
     * @return boolean
     * @deprecated Compatibility layer for {@code 2.x}. Use
     * {@link #delete(Arguments)}.
     */
    @Deprecated
    final public boolean del(Arguments arguments) {
        return delete(arguments);
    }

    /**
     * Метод подсчитывающий кол-во строк в запросе.
     *
     * @param arguments объект Arguments.
     * @return Long
     */
    final public Long countRows(Arguments arguments) {
        return fetchOperation.countRows(arguments);
    }

    /**
     * Alias for AgtySQL.countRows() method.
     *
     * @param arguments объект Arguments.
     * @return boolean
     * @deprecated Compatibility layer for {@code 2.x}. Use
     * {@link #countRows(Arguments)}.
     */
    @Deprecated
    final public Long rows(Arguments arguments) {
        return countRows(arguments);
    }

    /**
     * Проверяет наличие таблицы.
     *
     * @param arguments объект Arguments.
     * @return true если таблица существует.
     */
    final public boolean tableIsExists(Arguments arguments) {
        return fetchOperation.tableIsExists(arguments);
    }

    /**
     * Запрос проверки наличие строк в таблице
     *
     * @param arguments объект Arguments.
     * @return true если строки найдены, null если ошибка в запросе.
     */
    final public Boolean rowIsExists(Arguments arguments) {
        return fetchOperation.rowIsExists(arguments);
    }

    /**
     * Очищение таблицу и обнуление счетчика последовательностей.
     *
     * @param arguments объект Arguments.
     * @return true, если нет ошибок.
     */
    final public boolean truncate(Arguments arguments) {
        return metadataOperation.truncate(arguments);
    }

    /**
     * Метод удаляющий столбец из таблицы
     *
     * @param arguments объект Arguments.
     * @return bool
     */
    final public boolean dropColumn(Arguments arguments) {
        return metadataOperation.dropColumn(arguments);
    }

    /**
     * Метод удаляющий таблицу
     *
     * @param arguments объект Arguments.
     * @return bool
     */
    final public boolean dropTable(Arguments arguments) {
        return metadataOperation.dropTable(arguments);
    }

    /**
     * Максимальное значение по указанному полю.
     *
     * @param arguments объект Arguments.
     * @return Long
     */
    final public Long max(Arguments arguments) {
        return metadataOperation.max(arguments);
    }

    /**
     * Maximum value by action field. If the value is null, then return a defaultValue.
     * @param arguments Arguments
     * @param defaultValue default value
     * @return Long
     */
    final public Long maxOrDefault(Arguments arguments, long defaultValue) {
        return metadataOperation.maxOrDefault(arguments, defaultValue);
    }

    /**
     * Минимальное значение по указанному полю.
     *
     * @param arguments объект Arguments.
     * @return int|null.
     */
    final public Long min(Arguments arguments) {
        return metadataOperation.min(arguments);
    }

    /**
     * Minimum value by action field. If the value is null, then return a defaultValue.
     * @param arguments Arguments
     * @param defaultValue default value
     * @return Long
     */
    final public Long minOrDefault(Arguments arguments, long defaultValue) {
        return metadataOperation.minOrDefault(arguments, defaultValue);
    }

    /**
     * Получить последнюю строку.
     *
     * @param arguments объект Arguments.
     * @return объект SqlRow с последней строкой.
     */
    final public SqlRow getLastRow(Arguments arguments) {
        return fetchOperation.getLastRow(arguments);
    }

    /**
     * Получить первую строку.
     *
     * @param arguments объект Arguments.
     * @return объект SqlRow с последней строкой.
     */
    final public SqlRow getFirstRow(Arguments arguments) {
        return fetchOperation.getFirstRow(arguments);
    }

    /**
     * Legacy cursor-like list API with a default index (0).
     * Prefer listArray(...) for eager reads or openCursor(...) for streaming.
     *
     * @return SqlRow данные строки.
     * @deprecated Compatibility layer for {@code 2.x}. Use
     * {@link #openCursor(Arguments)} for streaming or
     * {@link #listArray(Arguments)} for eager reads.
     */
    @Deprecated
    public SqlRow list(Arguments arguments) {
        return list(arguments, 0);
    }

    /**
     * Legacy cursor-like list API backed by an internal cursor slot.
     * Prefer listArray(...) for eager reads or openCursor(...) for streaming.
     *
     * @param index Index of a data list
     * @return SqlRow данные строки.
     * @deprecated Compatibility layer for {@code 2.x}. Use
     * {@link #openCursor(Arguments)} for streaming or
     * {@link #listArray(Arguments)} for eager reads.
     */
    @Deprecated
    public SqlRow list(Arguments arguments, int index) {
        debugMessageEnterInMethod("list(%d)".formatted(index));
        return listOperation.list(arguments, index);
    }

    /**
     * Eagerly reads the full result into memory as a linked collection.
     *
     * @return LinkedList<SqlRow>
     */
    public LinkedList<SqlRow> listArray(Arguments arguments) {
        debugMessageEnterInMethod("listArray()");
        return listOperation.listArray(arguments);
    }

    /**
     * Получить все строки в виде связанной коллекции.
     * An alias the listArray() method.
     *
     * @return LinkedList<SqlRow>
     * @deprecated Compatibility layer for {@code 2.x}. Use
     * {@link #listArray(Arguments)}.
     */
    @Deprecated
    public LinkedList<SqlRow> findAll(Arguments arguments) {
        return listArray(arguments);
    }

    /**
     * Opens a forward-only library-managed cursor for streaming reads.
     *
     * <p>Prefer this over legacy {@link #list(Arguments)} when the caller wants
     * cursor-like iteration without manual {@link ResultSet} ownership.
     *
     * @param arguments query arguments
     * @return managed cursor
     */
    public AgtySqlCursor openCursor(Arguments arguments) {
        debugMessageEnterInMethod("openCursor()");
        return listOperation.openCursor(arguments);
    }

    /**
     * Opens a forward-only library-managed cursor for a raw query.
     *
     * @param query SQL query
     * @return managed cursor
     */
    public AgtySqlCursor openCursor(String query) {
        return openCursor(new Arguments().setQuery(query));
    }

    /**
     * Checks whether a legacy internal cursor slot is still open.
     *
     * @param index legacy cursor slot index
     * @return {@code true} when the slot contains an open cursor wrapper
     */
    public boolean hasOpenListCursor(int index) {
        return hasListResultSet(index);
    }

    /**
     * Closes one legacy internal cursor slot created by {@link #list(Arguments, int)}.
     *
     * @param index legacy cursor slot index
     */
    public void closeListCursor(int index) {
        if (index < 0 || index >= listResultSetPool.size()) {
            return;
        }

        ListResultSet listResultSet = listResultSetPool.get(index);
        if (listResultSet == null) {
            return;
        }

        try {
            listResultSet.close();
        } catch (SQLException e) {
            throwError("AgtySQL.closeListCursor(%d)".formatted(index), e.getMessage());
        } finally {
            listResultSetPool.set(index, null);
        }
    }

    /**
     * Closes all legacy internal cursor slots tracked by this session.
     */
    public void closeListCursors() {
        for (int i = 0; i < listResultSetPool.size(); i++) {
            closeListCursor(i);
        }
    }

    /**
     * Get ResultSet for a list() method
     * @return ResultSet
     */
    private ListResultSet getListResultSet(int index) {
        return index >= listResultSetPool.size() || index < 0 ? null : listResultSetPool.get(index);
    }

    /**
     * Set ResultSet for a list() method
     * @param resultSet listResultSet
     * @param index Index
     */
    private void setListResultSet(ListResultSet resultSet, int index) {
        while (listResultSetPool.size() <= index) {
            listResultSetPool.add(null);
        }
        listResultSetPool.set(index, resultSet);
    }

    /**
     * Наличие данных ResultSet и завершения выборки.
     *
     * @param index Index
     * @return true, если ResulSet создан и не завершен.
     */
    private boolean hasListResultSet(int index) {
        return !listResultSetPool.isEmpty()
                && index >= 0
                && index < listResultSetPool.size()
                && listResultSetPool.get(index) != null;
    }

    /**
     * Short method.
     * Fetch by table, field and value
     *
     * @deprecated Compatibility layer for {@code 2.x}. Use
     * {@link #fetch(Arguments)} with {@link Arguments#builder()}.
     */
    @Deprecated
    final public SqlRow getByField(String table, String field, String value) {
        return fetch(new Arguments().setTable(table).setWhere(field + "=" + value));
    }


    private <T> T save(T object, SaveModelMode saveModelMode, String returnFields) {
        try {
            return ModelControl.newModelControl().save(object, this, saveModelMode, returnFields);
        } catch (Exception e) {
            throwError("AgSQL.save()//[ModelSave: " + saveModelMode.toString() + "]", e.getMessage());
        }
        return null;
    }

    private <T> Object saveAndGetField(T object, SaveModelMode saveModelMode, String returnField) {
        try {
            return ModelControl.newModelControl().saveAndGetField(object, this, saveModelMode, returnField);
        } catch (Exception e) {
            throwError("AgSQL.saveAndGetField()//[ModelSave: " + saveModelMode.toString() + "]", e.getMessage());
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> T mapSqlRowToEntity(SqlRow sqlRow, T object) {
        if (sqlRow == null || sqlRow.isEmpty()) {
            return null;
        }

        return (T) ObjectBuilder.builder()
                .object(object)
                .sqlRow(sqlRow)
                .build();
    }

    @SuppressWarnings("unchecked")
    private <T> T mapSqlRowToEntity(SqlRow sqlRow, Class<?> clazz) {
        if (sqlRow == null || sqlRow.isEmpty()) {
            return null;
        }

        return (T) ObjectBuilder.builder()
                .clazz(clazz)
                .sqlRow(sqlRow)
                .build();
    }

    /**
     * Insert entity and return the mapped entity result.
     * Supported convenience layer over insertAndGet(...).
     */
    final public <T> T insertEntity(T object) {
        return save(object, SaveModelMode.INSERT_ONLY, "*");
    }

    /**
     * Insert entity with a pre-check and return the mapped entity result.
     */
    final public <T> T insertEntityWithCheck(T object) {
        return save(object, SaveModelMode.INSERT_ONLY_WITH_CHECK, "*");
    }

    /**
     * Update entity and return the mapped entity result.
     */
    final public <T> T updateEntity(T object) {
        return save(object, SaveModelMode.UPDATE_ONLY, "*");
    }

    /**
     * Update entity with check.
     */
    /*final public <T> T updateEntityWithCheck(T object) {
        return save(object, SaveModelMode.UPDATE_ONLY_WITH_CHECK, "*");
    }*/

    /**
     * Insert or update an entity and return the mapped entity result.
     */
    final public <T> T saveEntity(T object) {
        return save(object, SaveModelMode.WITHOUT_CHECK, "*");
    }

    /**
     * Insert or update an entity with existence check and return the mapped entity result.
     */
    final public <T> T saveEntityWithCheck(T object) {
        return save(object, SaveModelMode.WITH_CHECK, "*");
    }

    /**
     * Insert entity unless it already exists, then skip and return the mapped entity result.
     */
    final public <T> T saveEntityOrSkip(T object) {
        return save(object, SaveModelMode.SAVE_OR_SKIP, "*");
    }

    /**
     * Insert or update an entity and return one mapped field.
     */
    final public <T> Object saveEntity(T object, String returnField) {
        return saveAndGetField(object, SaveModelMode.WITHOUT_CHECK, returnField);
    }

    /**
     * Insert or update an entity with existence check and return one mapped field.
     */
    final public <T> Object saveEntityWithCheck(T object, String returnField) {
        return saveAndGetField(object, SaveModelMode.WITH_CHECK, returnField);
    }
}
