package org.agty.sql.interfaces;

import org.agty.sql.data.Arguments;
import org.agty.sql.driver.DialectCapabilities;

import java.sql.ResultSet;
import java.util.ArrayList;

/**
 * An interface for AgtySQL drivers
 */
public interface Sql {
    /**
     * Используемый драйвер базы данных
     *
     * @return драйвер базы данных.
     */
    String getDriverName();

    /**
     * База данных по умолчанию.
     *
     * @return имя базы данных.
     */
    String getDefaultDatabase();

    /**
     * Кавычки используемые для таблиц.
     * SELECT field FROM "myTable";
     *
     * @return кавычки.
     */
    String getQuoteTable();

    /**
     * Кавычки используемые для полей.
     * SELECT "field" FROM myTable WHERE "field" = value;
     *
     * @return кавычки.
     */
    String getQuoteColumn();

    /**
     * Кавычки используемые для значений.
     * SELECT field FROM myTable WHERE field = 'value';
     *
     * @return кавычки.
     */
    String getQuoteValue();

    /**
     * Наличие поддержки метода JDBC.largeUpdate().
     *
     * @return true если поддерживается.
     */
    boolean isSupportLargeUpdate();

    /**
     * Documented behavior guarantees for the current dialect.
     *
     * @return dialect capabilities.
     */
    default DialectCapabilities getCapabilities() {
        return DialectCapabilities.none();
    }

    /**
     * Запрос SELECT на основе параметров Arguments.
     * SELECT fields FROM table WHERE ... ORDER BY field LIMIT 12 OFFSET 10;
     *
     * @param arguments объект Arguments.
     * @return запрос.
     */
    String selectQuery(Arguments arguments);

    /**
     * Запрос INSERT на основе параметров Arguments.
     * INSERT INTO myTable (field, field) SET (value, value);
     *
     * @param arguments объект Arguments.
     * @return запрос.
     */
    String insertQuery(Arguments arguments);

    /**
     * Запрос INSERT на основе массива параметров Arguments.
     * INSERT INTO myTable (field, field) SET (value, value), (value, value);
     *
     * @param arguments коллекция ArrayList объектов Arguments.
     * @return запрос.
     */
    String insertQuery(ArrayList<Arguments> arguments);

    /**
     * Запрос UPDATE на основе параметров Arguments.
     * UPDATE table SET ... WHERE ... ORDER BY field ASC LIMIT 12 OFFSET 10;
     *
     * @param arguments объект Arguments.
     * @return запрос.
     */
    String updateQuery(Arguments arguments);

    /**
     * Запрос DELETE на основе параметров Arguments.
     * DELETE FROM table WHERE ... ORDER BY field ASC LIMIT 12 OFFSET 10;
     *
     * @param arguments объект Arguments.
     * @return запрос.
     */
    String deleteQuery(Arguments arguments);

    /**
     * Запрос первой найденной строки на основе параметров Arguments.
     *
     * При формировании запроса не учитывается параметр limit, так как
     * он подставляется в самый конец запроса внутри метода.
     *
     * Формируется только первая часть запроса до LIMIT.
     *
     * SELECT field FROM myTable WHERE field = value ORDER BY field ASC LIMIT 1;
     *
     * @param arguments объект Arguments.
     * @return запрос.
     */
    String fetchQuery(Arguments arguments);

    /**
     * Запрос подсчета количества строк на основе запроса из параметров Arguments.
     * SELECT COUNT(*) as rows FROM myTable WHERE field = value;
     *
     * @param arguments объект Arguments.
     * @return запрос.
     */
    String countRowsQuery(Arguments arguments);

    /**
     * Запрос на очищение таблицы.
     * TRUNCATE myTable;
     *
     * @param table таблица.
     * @return запрос.
     */
    String truncateQuery(String table);

    /**
     * Обнулить все последовательности.
     *
     * @param table имя таблицы.
     */
    boolean restartIdentity(String table);

    /**
     * Запрос на удаление таблицы.
     * DROP myTable;
     *
     * @param table таблица.
     * @return запрос.
     */
    String dropTableQuery(String table);

    /**
     * Запрос удаления столбца из таблицы.
     * ALTER TABLE table DROP column;
     *
     * @param arguments объект Arguments.
     * @return запрос.
     */
    String dropColumnQuery(Arguments arguments);

    /**
     * Запрос получение первой строки на основе запроса из параметров Arguments.
     * SELECT field FROM myTable WHERE ... ORDER BY actionField ASC LIMIT 1;
     *
     * @param arguments объект Arguments.
     * @return запрос.
     */
    String getFirstRowQuery(Arguments arguments);

    /**
     * Запрос получение последней строки на основе запроса из параметров Arguments.
     * SELECT field FROM myTable WHERE ... ORDER BY actionField DESC LIMIT 1;
     *
     * @param arguments объект Arguments.
     * @return запрос.
     */
    String getLastRowQuery(Arguments arguments);

    /**
     * Проверяет наличие таблицы.
     * if (SQL.isTable("myTable")) { ... }
     *
     * @param table таблица.
     * @return запрос.
     */
    boolean tableIsExists(String table);

    /**
     * Проверяет наличие строк на основе запроса из аргументов Arguments.
     * SELECT EXISTS ( QUERY ) as isExists;
     *
     * @param arguments объект Arguments.
     * @return true, если строки были найдены.
     */
    Boolean rowIsExists(Arguments arguments);

    /**
     * Максимальное значение по указанному полю.
     * SELECT MAX(field) as max FROM table WHERE ...
     *
     * @param arguments объект Arguments.
     * @return максимальное значение.
     */
    Long max(Arguments arguments);

    /**
     * Минимальное значение по указанному полю.
     * SELECT MIN(field) as max FROM table WHERE ...
     *
     * @param arguments объект Arguments.
     * @return минимальное значение.
     */
    Long min(Arguments arguments);

    /**
     * Последний назначенный ID в AUTO_INCREMENT (PK Sequence).
     * PrimaryKey определяется автоматически.
     *
     * @param table таблица.
     * @return последний вставленный ID.
     */
    Long getLastInsertId(String table);

    /**
     * Последний назначенный ID в AUTO_INCREMENT (PK Sequence).
     *
     * @param table таблица.
     * @param primaryKey основной ключ.
     * @return последний вставленный ID.
     */
    Long getLastInsertId(String table, String primaryKey);

    /**
     * Основной ключ таблицы.
     *
     * @param table таблица.
     * @return последний вставленный ID.
     */
    String getPrimaryKey(String table);

    /**
     * Основной ключ таблицы указанной в аргументах Arguments.
     *
     * @param arguments объект Arguments.
     * @return последний вставленный ID.
     */
    String getPrimaryKey(Arguments arguments);

    /**
     * Insert a row and get a result
     * @param arguments Arguments
     * @return ResultSet
     */
    ResultSet insertAndGet(Arguments arguments, String fields);

    /**
     * Update a row and get a result
     * @param arguments Arguments
     * @return ResultSet
     */
    ResultSet updateAndGet(Arguments arguments, String fields);

    /*
     * ============= ЭКСПЕРИМЕНТАЛЬНЫЕ =================
     */

    //PgSqlFieldsType typeOfFields();

    //LinkedHashMap<String, HashMap> tableFields(String table);

    //LinkedHashMap<String, HashMap> tableIndexes(String table);

    //HashMap<String, Object> tableInfo(String table);

    //HashMap<String, Object> tableSequences(String table);

    //HashMap<String, String> databaseInfo(String database);

    //LinkedHashMap<String, Object> tableCreateStatement(HashMap<String, Object> tableInfo, LinkedHashMap<String, HashMap> fields, LinkedHashMap<String, HashMap> indexes, HashMap<String, Object> sequences);
}
