# 16. ClickHouse

ClickHouse is supported as a JDBC SQL dialect for analytical workloads:
reading, cursors, low-level JDBC, regular `INSERT`, and basic mutation
statements. It is not a transactional OLTP database, so part of the
write-return API is intentionally disabled by the capability model.

## Local Docker

Use the installer for a local test database:

```bash
./install/install-clickhouse.sh
```

Defaults:

- container: `agty-sql-clickhouse`
- database: `agty_sql`
- user: `agty_sql`
- password: `agty_sql`
- HTTP/JDBC port: `28123`
- native TCP port: `29000`

Override the password when needed:

```bash
AGTY_SQL_CLICKHOUSE_PASSWORD="secret" ./install/install-clickhouse.sh
```

## Maven

The ClickHouse JDBC driver is an optional runtime dependency of the library. If
your application declares JDBC drivers explicitly, add:

```xml
<dependency>
    <groupId>com.clickhouse</groupId>
    <artifactId>clickhouse-jdbc</artifactId>
    <version>0.10.0</version>
    <scope>runtime</scope>
</dependency>
```

## Configuration

Example section:

```ini
db.clickhouse.driver = clickhouse
db.clickhouse.server = localhost
db.clickhouse.port = 28123
db.clickhouse.user = agty_sql
db.clickhouse.password = agty_sql
db.clickhouse.database = agty_sql
db.clickhouse.pfx = agty_
db.clickhouse.encoding = UTF-8
db.clickhouse.serverTimeZone = UTC
db.clickhouse.logquery = false
db.clickhouse.debug = false
```

Create the facade:

```java
AgtySQL sql = new AgtySQL("clickhouse");
```

## Creating Tables

ClickHouse requires an explicit engine:

```java
sql.execute("""
    CREATE TABLE IF NOT EXISTS {events} (
        id UInt64,
        name String,
        visits UInt32,
        active UInt8
    )
    ENGINE = MergeTree
    ORDER BY id
    """);
```

The `{events}` prefix placeholder is handled by the regular AgtySQL query
rebuild mechanism.

## Writing And Reading

```java
sql.insert(
    Arguments.builder()
        .setTable("{events}")
        .addData("id", 1)
        .addData("name", "page-view")
        .addData("visits", 10)
        .addData("active", true, "clickhouse")
);

SqlRow row = sql.fetch(
    Arguments.builder()
        .setTable("{events}")
        .setWhere("[id] = %d", 1)
);
```

For large reads, use cursor API:

```java
try (AgtySqlCursor cursor = sql.openCursor(
        Arguments.builder().setTable("{events}").setOrderBy("id ASC"))) {
    SqlRow row;
    while ((row = cursor.next()) != null) {
        // process row
    }
}
```

## Limits

- `setReturnLastInsertId(true)` is not supported.
- `insertAndGet()` is not supported.
- `updateAndGet()` is not supported.
- generated keys are not advertised as a supported high-level capability.
- `UPDATE` and `DELETE` are rendered as `ALTER TABLE ... UPDATE/DELETE WHERE`;
  account for ClickHouse mutation semantics and eventual completion.
- Use low-level JDBC for ClickHouse-specific queries, settings, and DDL that
  go beyond portable AgtySQL CRUD.
