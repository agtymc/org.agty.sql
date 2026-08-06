# Changelog

## Unreleased

### Changed
- Maven publication was reconfigured from the legacy WebDAV repository to GitHub Packages at `https://maven.pkg.github.com/agtymc/org.agty.sql`.
- Maven server id for publishing and consuming artifacts was renamed from `agtymc` to `org.agty.sql`.

### Added
- GitHub Actions workflow for publishing Maven artifacts to GitHub Packages.

### Documentation
- README files now document the real GitHub Packages repository URL and the `org.agty.sql` Maven server id for consumer setup.

## 2.0.0 - 2026-08-06

### Added
- Support for MySQL, MariaDB, PostgreSQL, SQLite, and H2 drivers in the `org.agty.sql` package line.
- Public cursor API with `AgtySqlCursor` and `openCursor(...)` for forward-only streaming reads.
- Low-level JDBC/session entry points on `AgtySQL`, including `Connection`, `Statement`, `PreparedStatement`, transactions, batch execution, and generated keys flows.
- `AgtySqlDataSource` and `AgtySqlPooledDataSource` for `DataSource`-based and pooled integration scenarios.
- English and Russian user guides plus separate session/JDBC API documentation.

### Changed
- Library package migrated from `org.agty.agtysql` to `org.agty.sql`.
- `AgtySQL` was reduced toward a thinner facade with operation logic split into dedicated operation classes.
- Driver-specific mutation behavior is now controlled through explicit capability and strategy contracts.
- Connection pooling now follows named pool configuration instead of an implicit global style.
- File-based databases were moved to the `databases/` directory for local development and integration testing.

### Deprecated
- Legacy helper aliases such as `del(...)`, `rows(...)`, `findAll(...)`, and legacy list helpers remain available as a compatibility layer for `2.x`, but are deprecated for new code.
- Legacy helper methods including `lastInsertId(...)`, `statementExecute*`, `executeResultSet(...)`, `executeQuery(...)`, and `getByField(...)` remain only for migration compatibility.

### Testing
- Added and updated unit and integration coverage for dialect capabilities, cross-database flows, cursor lifecycle, JDBC access, insert strategies, pooled data sources, and connection pools.

### Documentation
- Added migration notes for renamed `AgtySQL` methods and updated guidance toward the target `2.x` API.
