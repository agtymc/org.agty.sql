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

## Production Safety

- Keep one mutable `AgtySQL`, `Arguments`, or `AgtySqlCursor` instance within a
  single request/transaction and thread. Pools and pooled data sources may be
  shared; borrowed handles may not.
- Keep `throwException=true` so connection and execution failures cannot look
  like empty query results.
- Use separate `loginTimeoutSeconds` and `networkTimeoutMillis` values.
- Keep `logQueryValues=false`. Query logs then redact string, numeric, and
  dollar-quoted literals; common credential assignments are always redacted.
- Query-log writes are asynchronous and use a bounded process-wide queue. A
  saturated queue drops the new diagnostic entry and records an `AgtySQL`
  error instead of blocking JDBC execution; allow a short drain interval
  before inspecting the file in operational tooling.
- Use `${ENVIRONMENT_VARIABLE}` as the complete value of an ini credential, or
  construct `AgtySqlConfig` from a secret-manager result. Restrict a local
  `config.ini` to owner read/write permissions (`chmod 600 config.ini`).
