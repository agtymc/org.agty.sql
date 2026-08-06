package org.agty.sql.dialect.emptysql;

import org.agty.sql.AgtySQL;
import org.agty.sql.driver.DialectCapabilities;
import org.agty.sql.interfaces.Sql;
import org.agty.sql.data.Arguments;

import java.sql.ResultSet;
import java.util.ArrayList;

public class EmptySQL implements Sql {
    public EmptySQL(AgtySQL agtySQL) {}

    @Override
    public String getDriverName() {
        return null;
    }

    @Override
    public String getDefaultDatabase() {
        return null;
    }

    @Override
    public String getQuoteTable() {
        return null;
    }

    @Override
    public String getQuoteColumn() {
        return null;
    }

    @Override
    public String getQuoteValue() {
        return null;
    }

    @Override
    public boolean isSupportLargeUpdate() {
        return false;
    }

    @Override
    public DialectCapabilities getCapabilities() {
        return DialectCapabilities.none();
    }

    @Override
    public String insertQuery(Arguments arguments) {
        return null;
    }

    @Override
    public String selectQuery(Arguments arguments) {
        return null;
    }

    @Override
    public String insertQuery(ArrayList<Arguments> arguments) {
        return null;
    }

    @Override
    public String updateQuery(Arguments arguments) {
        return null;
    }

    @Override
    public String deleteQuery(Arguments arguments) {
        return null;
    }

    @Override
    public String countRowsQuery(Arguments arguments) {
        return null;
    }

    @Override
    public String fetchQuery(Arguments Arguments) {
        return null;
    }

    @Override
    public boolean tableIsExists(String table) {
        return false;
    }

    @Override
    public Boolean rowIsExists(Arguments arguments) {
        return null;
    }

    @Override
    public Long max(Arguments arguments) {
        return null;
    }

    @Override
    public Long min(Arguments arguments) {
        return null;
    }

    @Override
    public String truncateQuery(String table) {
        return null;
    }

    @Override
    public boolean restartIdentity(String table) {
        return false;
    }

    @Override
    public String dropTableQuery(String table) {
        return null;
    }

    @Override
    public String dropColumnQuery(Arguments arguments) {
        return null;
    }

    @Override
    public Long getLastInsertId(String table) {
        return 0L;
    }

    @Override
    public Long getLastInsertId(String table, String primaryKey) {
        return 0L;
    }

    @Override
    public String getPrimaryKey(String table) {
        return null;
    }

    @Override
    public String getPrimaryKey(Arguments arguments) {
        return null;
    }

    /**
     * Insert a row and get a result
     *
     * @param arguments Arguments
     * @return ResultSet
     */
    @Override
    public ResultSet insertAndGet(Arguments arguments, String fields) {
        return null;
    }

    /**
     * Update a row and get a result
     *
     * @param arguments Arguments
     * @param fields
     * @return ResultSet
     */
    @Override
    public ResultSet updateAndGet(Arguments arguments, String fields) {
        return null;
    }

    @Override
    public String getFirstRowQuery(Arguments arguments) {
        return null;
    }

    @Override
    public String getLastRowQuery(Arguments arguments) {
        return null;
    }
}
