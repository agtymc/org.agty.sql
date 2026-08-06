# 14. Migration Notes

### Package

The current library package is:

```java
org.agty.sql
```

### Configuration

- the main config is located at the project root: `config.ini`;
- `config.ini-sample` is used as the template for the current config.

### Method renames

The rename table is maintained separately:

- `docs/en/MIGRATION_AGTYSQL_METHOD_RENAMES.md`

At the current stage, the already fixed method renames are not enough to
describe the whole state of `AgtySQL`: the up-to-date alias/legacy API map is
kept in that migration document.

### Legacy API

The library still contains legacy elements preserved for compatibility:

- `list(arguments, index)` cursor-like API;
- alias methods such as `del(...)`, `rows(...)`, `findAll(...)`;
- `lastInsertId(...)` as a standalone helper;
- `statementExecute*`, `executeResultSet(...)`, `executeQuery(...)`;
- `getByField(...)` short helper;

For new code, prefer:

- `delete(...)` instead of `del(...)`;
- `countRows(...)` instead of `rows(...)`;
- `listArray(...)` or `openCursor(...)` instead of old list patterns;
- `insert(... setReturnLastInsertId(true))` instead of direct `lastInsertId(...)`;
- `execute(...)` / `executeUpdate(...)` or the low-level JDBC API instead of
  old statement/result-set helper methods;
- `fetch(Arguments)` instead of `getByField(...)`.

As of `2026-08-03`, these legacy/alias methods are already considered
deprecated at the public-API level.

Policy:

- deprecated helper/alias API stays for the whole lifecycle of `2.x`;
- inside `2.x` it is treated as a compatibility layer;
- actual removal is postponed until the next major release;
- new code and new documentation should use the target API names.
