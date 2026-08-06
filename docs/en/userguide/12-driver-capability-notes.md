# 12. Driver Capability Notes

Write-return behavior depends on `DialectCapabilities`.

At the current stage, the project fixes the following:

- PostgreSQL: native returning;
- MySQL: follow-up fetch for `insertAndGet()`;
- MariaDB: follow-up fetch for `insertAndGet()`;
- SQLite: follow-up fetch for `insertAndGet()`;
- H2: follow-up fetch for `insertAndGet()` through a collision-prone last-ID
  fallback;
- PostgreSQL: `UpdateAndGetStrategy.NATIVE_RETURNING`;
- MySQL: `UpdateAndGetStrategy.FOLLOW_UP_FETCH_BY_WHERE`;
- MariaDB: `UpdateAndGetStrategy.FOLLOW_UP_FETCH_BY_WHERE`;
- SQLite: `UpdateAndGetStrategy.FOLLOW_UP_FETCH_BY_WHERE`;
- H2: `UpdateAndGetStrategy.FOLLOW_UP_FETCH_BY_WHERE`.

Practical meaning:

- `insertAndGet()` and `updateAndGet()` depend on the dialect strategy;
- for `updateAndGet()`, the library now distinguishes not only
  support/no-support but also the exact follow-up path;
- `setReturnLastInsertId(true)` also depends on the ID-resolution strategy;
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
