# 13. Recommended Usage Patterns

### For a typical application

- use high-level CRUD;
- use `Arguments.builder()` to assemble queries;
- use `listArray()` only when the result set reasonably fits in memory;
- close `AgtySQL` at the end of its scope.

### For large result sets

- use `openCursor(...)` or direct JDBC `ResultSet`;
- tune `fetchSize` for the scenario;
- do not load large results through `listArray()` when streaming is needed.

### For custom SQL

- use `prepareStatement(...)` or `execute(...)`;
- disable rebuild only for already prepared SQL;
- read generated keys through `getGeneratedKeys(...)` when a JDBC-style path is
  needed.

### For cross-database code

- avoid implicit assumptions about `RETURNING`;
- treat `insertAndGet()` and `updateAndGet()` as capability-driven API;
- define transaction boundaries explicitly.
