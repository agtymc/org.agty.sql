package org.agty.sql.dialect.pgsql.fields;

import org.agty.sql.base.Field;
import org.agty.sql.base.FieldsType;

public class PgSqlFieldsType extends FieldsType {

    public PgSqlFieldsType() {
        fill();
    }

    private void fill() {
        /*DEFAULT*/
        add(new Field("default", "CHARACTER VARYING", "255"));

        /*INTEGERS*/
        add(new Field("int", "INTEGER"));
        add(new Field("tinyint", "SMALLINT"));
        add(new Field("smallint", "INTEGER"));
        add(new Field("mediumint", "INTEGER"));
        add(new Field("bigint", "BIGINT"));
        add(new Field("float", "REAL"));
        add(new Field("double", "DOUBLE PRECISION"));
        add(new Field("decimal", "DECIMAL", "65,2"));
        add(new Field("numeric", "NUMERIC"));
        add(new Field("bit", "BIT"));

        add(new Field("intUnsigned", "BIGINT"));
        add(new Field("tinyintUnsigned", "SMALLINT"));
        add(new Field("smallintUnsigned", "INTEGER"));
        add(new Field("mediumintUnsigned", "INTEGER"));
        add(new Field("bigintUnsigned", "NUMERIC(20)"));

        /*BOOLEAN*/
        add(new Field("boolean", "BOOLEAN"));

        /*TEXT*/
        add(new Field("longtext", "TEXT"));
        add(new Field("mediumtext", "TEXT"));
        add(new Field("tinytext", "TEXT"));
        add(new Field("text", "TEXT"));
        add(new Field("blob", "BYTEA"));
        add(new Field("varchar", "CHARACTER VARYING", "255"));
        add(new Field("char", "CHAR"));

        /*DATETIME*/
        add(new Field("date", "DATE"));
        add(new Field("time", "TIME without time zone"));
        add(new Field("timeTZ", "TIME with time zone"));
        add(new Field("timeNoTZ", "TIME without time zone"));
        add(new Field("datetime", "TIMESTAMP without time zone"));
        add(new Field("datetimeNoTZ", "TIMESTAMP without time zone"));
        add(new Field("datetimeTZ", "TIMESTAMP with time zone"));
        add(new Field("datetimeCurrent", "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"));
        add(new Field("datetimeCurrentNoTZ", "TIMESTAMP without time zone DEFAULT CURRENT_TIMESTAMP"));
        add(new Field("datetimeCurrentTZ", "TIMESTAMP with time zone DEFAULT CURRENT_TIMESTAMP"));

        /*AUTOINCREMENT*/
        add(new Field("autoincrement", "INTEGER"));
    }
}
