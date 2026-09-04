# Changelog

## Unreleased

## 2.1.0 - 2026-09-05

### Added
- JaCoCo now enforces measured coverage floors for SQL builders, query rebuilding, arguments, row conversion, pools, and data sources.
- Pull-request/push CI now verifies Java 21 and 25 on Linux and Windows, runs the six-database integration matrix separately, validates shell syntax, and pins current third-party actions by commit SHA.
- Maven release gates for Java/Maven versions, SpotBugs, PMD, JaCoCo reporting, source/Javadoc JARs, reproducible archive timestamps, and a checksum-verified Maven wrapper distribution.
- Separate connection-login and established-network timeouts, `${ENV_VAR}` ini values, query-log value redaction, bounded UTF-8 log rotation, and a private vulnerability-reporting policy.
- Opt-in high-level JDBC parameter binding through `Arguments.useStatementPrepare(true)`, including `?` parameters for `WHERE` and raw `setQuery(...)` SQL across CRUD, cursor, aggregate, generated-key, and write-return flows.
- Runtime-validating `Arguments.addDataString/addDataInt/addDataLong/addDataDecimal` and related typed methods, plus a whitelist-enforcing `addData(String, Object)` overload for dynamically typed input.
- Central structural SQL validation for tables, data fields, columns, primary/action fields, select fields, `GROUP BY`, and `ORDER BY`, plus explicit `SqlExpression.trusted(...)` escape hatches for application-authored expressions.
- Controlled legacy `%s/%d/%f/%b` WHERE formatting that validates runtime types, requires quoted string placeholders, HTML-encodes text, and rejects arbitrary `Object.toString()` conversion.
- HikariCP-backed compatibility implementations of `AgtySQLPool` and `AgtySqlPooledDataSource`, including guarded connection handles that prohibit physical-connection `unwrap()`.
- Secure SQL Server certificate configuration through `AgtySqlConfig.setTrustServerCertificate(...)`; certificate-chain and host-name validation are enabled by default.
- CycloneDX SBOM generation, checksum-pinned OSV-Scanner security CI, and Dependabot configuration for Maven and GitHub Actions dependencies.
- Explicitly trusted raw-query overloads for fetch, cursor, insert-return, and update-return operations.
- Release artifacts now receive GitHub/Sigstore build-provenance attestations before publication.

### Compatibility
- Legacy query rendering and HTML encoding remain the default. Prepared mode stores bound values unchanged, treats the legacy `[~` raw-value prefix as ordinary data, and keeps SQL structure outside parameter binding. Legacy encoding now preserves trailing backslashes and represents control/non-ASCII code points as reversible numeric HTML entities.
- Public binary and source compatibility is checked against the Git commit that produced `2.0.4`; the only explicit exclusion is the intentionally removed internal `QueryValueDataBuilder` class.

### Changed
- The release helper now validates a clean tree, synchronized versions, changelog entry, complete local gates, JMH smoke results, and a signed tag before waiting for hosted CI and dispatching publication.
- GitHub Packages and GitHub Releases now receive the same verified JAR, source, Javadoc, POM, and SBOM bytes from one CI build instead of rebuilding after release creation.
- The minimum Java runtime is now Java 21; supported builds are also verified on Java 25.
- JDBC drivers are optional runtime dependencies, preventing unused database drivers from entering consumer classpaths transitively.
- Missing database credentials and infrastructure failures now fail closed by default with typed exceptions retaining their original cause; legacy error accumulation remains opt-in through `setThrowException(false)`.
- Development database scripts require externally supplied passwords, bind ports to loopback, avoid persistent restart policies, and pin supported SQL images to patch versions.
- Mutable facade, argument, cursor, and borrowed pool handles now have explicit non-thread-safe ownership documentation.
- SQL query rebuilding now uses a quote/comment-aware tokenizer instead of regex placeholders, preserving empty literals, doubled or backslash-escaped quotes, comments, and PostgreSQL dollar-quoted strings.
- `RowData` numeric getters now accept every JDBC `Number` implementation consistently, and date/time getters use case-insensitive column lookup across Java and JDBC temporal types.
- Result rows now use JDBC column labels, so `AS` aliases are preserved consistently by `fetch`, `listArray`, and cursors across drivers.
- Empty row getters consistently return `null` for missing nullable values, and both empty row implementations now provide a non-null `toString()` result.
- High-level fetch and native write-return operations now close the JDBC statement associated with their result set.
- All `Number` values, including `Byte`, `BigInteger`, and `BigDecimal`, are rendered as numeric SQL values and bound with compatible JDBC types; unsupported dynamic data types now fail with `IllegalArgumentException` instead of implicit string conversion.
- The internal `QueryValueDataBuilder` was replaced by `SqlValueRenderer`, with `render()` as the terminal rendering method.
- `AgtySqlPooledDataSource.setLogWriter(...)` retains its checked-exception-free `2.0.4` signature and wraps an unexpected delegate failure in `IllegalStateException`.
- All `Arguments.addData...` methods now reject a `null` data field name with `IllegalArgumentException` instead of storing an invalid map key.
- Internal model/save arguments now use prepared execution by default and bind entity IDs instead of formatting them into SQL.
- Deprecated `AgtySQL.getByField(...)` now validates identifiers and binds its value through a prepared statement.
- `AgtySQL` now implements `AutoCloseable` and closes tracked cursors and deprecated raw ResultSet handles with their originating statements.
- JDBC strings are bound through `PreparedStatement.setString(...)`, and configured `autoCommit` is applied when a physical connection is created.
- JDBC dependencies were updated to H2 `2.4.240`, MariaDB `3.5.10`, SQLite JDBC `3.53.2.1`, MSSQL JDBC `13.4.0.jre11`, MySQL Connector/J `26.7.0`, and PostgreSQL JDBC `42.7.13`.

### Deprecated
- Direct access to `Arguments.table` is deprecated; use `setTable(...)` and `getTable()`. Directly assigned values remain validated on read.
- Raw `Arguments.setHaving(String)` expressions are rejected; use the explicit `setHaving(SqlExpression.trusted(...))` overload for static or allowlisted SQL.
- Non-empty raw `Arguments.setWhere(String)`, `appendWhere(String)`, and `setQuery(String)` fragments are rejected; use parameters or the corresponding `SqlExpression.trusted(...)` overload.
- Query-based `AgtySQL.fetch(String)`, `openCursor(String)`, `insertAndGet(String, ...)`, and `updateAndGet(String, ...)` overloads are deprecated in favor of their `SqlExpression` equivalents.

### Documentation
- The current refactoring and hardening work remains in the foundational `2.x` release line; compatibility reports guard against accidental API breakage without automatically requiring a `3.x` release.

### Testing
- A separate JMH project now benchmarks query rendering, structural query rebuilding, row conversion, and sampled eight-thread pooled-data-source latency without adding benchmark dependencies to the library artifact.

### Fixed
- JDBC connections are closed if applying configured session settings fails, preventing a partially initialized connection leak.
- Static analysis findings for dead dialect state, redundant boxing, and inefficient map iteration were removed; reviewed JDBC/mutable-facade findings are documented in the versioned SpotBugs filter.
- `AgtySQL.VERSION` now matches the Maven artifact version `2.1.0`.
- Nullable query/error log paths no longer throw `NullPointerException`, and `DataSource.setLoginTimeout()` no longer misuses JDBC network timeout after connection establishment.
- `QueryUpdateBuilder` and `QueryDeleteBuilder` now render the configured offset instead of repeating the limit value after `OFFSET`.
- January values and `LocalDate` inputs no longer produce invalid date/time conversions in `RowData`.
- `Arguments.appendWhere(String, Object...)` now formats the appended clause itself instead of accidentally referencing the raw-query field.
- Legacy WHERE string values now receive the same reversible encoding baseline as write values, including apostrophes, quotes, ampersands, backslashes, line breaks, and Unicode.
- Terminal raw statement helpers now close their `Statement`; deprecated ResultSet-returning helpers return managed handles and are also cleaned up by `AgtySQL.close()`.
- SQL configuration loading now closes its `FileInputStream` and preserves the original I/O exception as the cause.
- SQL Server JDBC URLs no longer set `trustServerCertificate=true` unconditionally.
- `AgtySQLPool.borrow()` now closes a connection acquired during facade-construction failure and cannot publish an unregistered lease while the pool is closing.
- Malformed generic/type markup and obsolete `@throws` tags no longer break Maven Javadoc generation; contracts for the newly added SQL-safety APIs are documented explicitly.

## 2.0.4 - 2026-08-19

### Added
- `Arguments.getBooleanValueForDriver(boolean, String)` for driver-aware boolean payload conversion, plus `Arguments.addData(String, boolean, String)` for storing those values directly in query data.

## 2.0.3 - 2026-08-17

### Added
- `AgtySqlCursor.hasNext()` as an additional forward-only iteration pattern for `openCursor(...)`, alongside the existing `next() != null` loop style.

### Documentation
- Cursor documentation now shows both supported `AgtySqlCursor` iteration styles so downstream ports can keep the public API behavior aligned.

## 2.0.2 - 2026-08-12

### Added
- Initial MSSQL (`mssql`) dialect support with SQL Server JDBC integration.
- Native `insertAndGet()` / `updateAndGet()` support for MSSQL via `OUTPUT inserted`.
- Development install script `install/install-mssql.sh` for a local SQL Server Docker container.

### Changed
- README files now list the full supported SQL driver matrix: MySQL, MariaDB, PostgreSQL, MSSQL, SQLite, and H2.
- Sample and local config files now include an `mssql` connection profile for SQL Server development and integration runs.

### Testing
- MSSQL was added to the live cross-database integration matrix and validated against the current capability and CRUD integration tests.

## 2.0.1 - 2026-08-06

### Changed
- Maven publication was reconfigured from the legacy WebDAV repository to GitHub Packages at `https://maven.pkg.github.com/agtymc/org.agty.sql`.
- Maven server id for publishing and consuming artifacts was renamed from `agtymc` to `org.agty.sql`.

### Deprecated
- `Arguments.setData(...)` was superseded by `Arguments.addData(...)` and remains only as a deprecated compatibility alias.
- `Arguments.getFromData(...)`, `Arguments.removeFromData(...)`, `Arguments.getDataArray(...)`, `Arguments.forceRequery()`, `SqlRow.setDataIsString(...)`, `SqlRow.dataIsString()`, and `SqlRow.noEmpty()` were superseded by clearer names and remain only as deprecated compatibility aliases.

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
