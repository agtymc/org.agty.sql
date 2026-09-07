package org.agty.sql.data;

/**
 * Limit section for the query
 */
public class Limit {
    private Integer offset;
    private Integer limit;

    /**
     * Creates a new instance.
     */
    public Limit() {}

    /**
     * Creates a new instance.
     * @param limit parameter value
     * @param offset parameter value
     */
    public Limit(int limit, int offset) {
        this.limit = limit;
        this.offset = offset;
    }

    /**
     * Creates a new instance.
     * @param limit parameter value
     */
    public Limit(int limit) {
        this.limit = limit;
    }

    /**
     * Returns the offset.
     * @return operation result
     */
    public Integer getOffset() {
        return offset;
    }

    /**
     * Returns the offset string.
     * @return operation result
     */
    public String getOffsetString() {
        return hasOffset() ? offset.toString() : "";
    }

    /**
     * Sets the offset.
     * @param offset parameter value
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * Returns the limit.
     * @return operation result
     */
    public Integer getLimit() {
        return limit;
    }

    /**
     * Returns the limit string.
     * @return operation result
     */
    public String getLimitString() {
        return hasLimit() ? limit.toString() : "";
    }

    /**
     * Sets the limit.
     * @param limit parameter value
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }

    /**
     * Returns whether limit.
     * @return operation result
     */
    public boolean hasLimit() {
        return limit != null;
    }

    /**
     * Returns whether offset.
     * @return operation result
     */
    public boolean hasOffset() {
        return offset != null;
    }

    /**
     * Returns whether limit and offset.
     * @return operation result
     */
    public boolean hasLimitAndOffset() {
        return hasLimit() && hasOffset();
    }
}
