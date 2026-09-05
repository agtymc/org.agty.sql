# 12. Driver Capability Notes

Write-return behavior depends on `DialectCapabilities`.

The public `ReadAfterWriteSafety` enum distinguishes atomic, connection-scoped,
transaction-guarded, collision-prone, and unsupported behavior. The current
matrix is:

| Driver | Schema | Storage | Last ID | `insertAndGet()` | `updateAndGet()` |
|---|---:|---|---|---|---|
| PostgreSQL | yes | server | connection-scoped sequence function | native / atomic | native / atomic |
| SQL Server | yes | server | connection-scoped function | native / atomic | native / atomic |
| MySQL | no | server | connection-scoped function | follow-up / transaction-guarded | follow-up by WHERE / collision-prone |
| MariaDB | no | server | connection-scoped function | follow-up / transaction-guarded | follow-up by WHERE / collision-prone |
| SQLite | no | file | connection-scoped function | follow-up / transaction-guarded | follow-up by WHERE / collision-prone |
| H2 | yes | file | last-row fallback / collision-prone | follow-up / collision-prone | follow-up by WHERE / collision-prone |

Practical meaning:

- `insertAndGet()` and `updateAndGet()` depend on the dialect strategy;
- for `updateAndGet()`, the library now distinguishes not only
  support/no-support but also the exact follow-up path;
- `setReturnLastInsertId(true)` also depends on the ID-resolution strategy;
- applications can inspect `lastInsertIdSafety()`, `insertAndGetSafety()`, and
  `updateAndGetSafety()` before selecting a portable flow;
- some strategies are native/session-safe;
- some strategies are emulated;
- for H2, the insert-return scenario must currently be treated as
  collision-prone under concurrent inserts;
- raw `String query` overloads for `insertAndGet()` / `updateAndGet()` should
  not be treated as equally portable on follow-up drivers: without metadata,
  the library cannot perform a correct post-write fetch.

If read-after-write behavior is critical to your business logic, do not assume
that all databases behave the same way.

Package-map note:

- the public capability API for `2.x` stays in `org.agty.sql.driver`;
- concrete internal dialect implementations already live in
  `org.agty.sql.dialect.*`;
- internal query/helper packages for concrete dialects are also moving to
  `org.agty.sql.dialect.*`;
- the internal dialect-selection entry point may now go through
  `org.agty.sql.dialect.DialectDriverRegistry`;
- `org.agty.sql.base` is not treated as a fully legacy package:
  `Field`, `FieldsType`, `RowData`, and `RowDataEmpty` remain supported
  model/base API inside `2.x`;
- deprecated helper/concrete classes outside `AgtySQL` that do not participate
  in its public-facing API have been removed from the production source set;
- deprecated compatibility in `2.x` is currently preserved primarily at the
  level of methods on `AgtySQL` itself.
