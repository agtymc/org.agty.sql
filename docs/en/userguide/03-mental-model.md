# 03. Mental Model

`AgtySQL` officially supports two working modes.

### High-level API

Use this when you want the standard library flow:

- `fetch(...)`
- `listArray(...)`
- `insert(...)`
- `insertAndGet(...)`
- `update(...)`
- `updateAndGet(...)`
- `delete(...)`
- `countRows(...)`

In this mode the library:

- builds SQL;
- runs the rebuild pipeline;
- talks to the driver;
- maps results to `SqlRow`;
- manages the lifecycle of temporary JDBC objects.

### Low-level session/JDBC API

Use this when you need direct control:

- `getConnection()`
- `getStatement()`
- `prepareStatement(...)`
- `executeBatch(...)`
- `getGeneratedKeys(...)`
- `openCursor(...)`
- `beginTransaction()`
- `setAutoCommit(...)`
- `commit()`
- `rollback()`
- `setFetchSize(...)`

This mode is useful for:

- custom SQL;
- manual `PreparedStatement` work;
- batch execution;
- generated keys;
- streaming/cursor scenarios;
- explicit transaction control.
