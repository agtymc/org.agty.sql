package org.agty.sql.data;

/**
 * An explicitly trusted structural SQL fragment.
 *
 * <p>This type does not sanitize SQL. It marks a fragment that was authored or
 * allowlisted by the application and must never be created from request data.</p>
 */
public final class SqlExpression {
    private final String sql;

    private SqlExpression(String sql) {
        this.sql = sql;
    }

    /**
     * Marks an application-authored SQL fragment as trusted.
     *
     * @param sql static SQL or an application-allowlisted fragment
     * @return immutable trusted expression
     */
    public static SqlExpression trusted(String sql) {
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("Trusted SQL expression must not be null or blank");
        }
        return new SqlExpression(sql);
    }

    /**
     * Returns the trusted SQL fragment unchanged.
     *
     * @return SQL fragment
     */
    public String sql() {
        return sql;
    }

    @Override
    public String toString() {
        return sql;
    }
}
