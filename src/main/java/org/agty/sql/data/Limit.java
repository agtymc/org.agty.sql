package org.agty.sql.data;

/**
 * Limit section for the query
 */
public class Limit {
    private Integer offset;
    private Integer limit;

    public Limit() {}

    public Limit(int limit, int offset) {
        setLimit(limit);
        setOffset(offset);
    }

    public Limit(int limit) {
        setLimit(limit);
    }

    public Integer getOffset() {
        return offset;
    }

    public String getOffsetString() {
        return hasOffset() ? offset.toString() : "";
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public Integer getLimit() {
        return limit;
    }

    public String getLimitString() {
        return hasLimit() ? limit.toString() : "";
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public boolean hasLimit() {
        return limit != null;
    }

    public boolean hasOffset() {
        return offset != null;
    }

    public boolean hasLimitAndOffset() {
        return hasLimit() && hasOffset();
    }
}
