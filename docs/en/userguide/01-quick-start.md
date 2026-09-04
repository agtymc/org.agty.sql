# 01. Quick Start

### Initialize from `config.ini`

```java
AgtySQL sql = new AgtySQL();
```

This constructor uses the root `config.ini` and the default section.

### Initialize by section name

```java
AgtySQL sql = new AgtySQL("mysql");
```

Use this when the config contains multiple connections.

### Initialize from an explicit config object

```java
AgtySqlConfig config = new AgtySqlConfig();
AgtySQL sql = new AgtySQL(config);
```

Use this when the configuration is assembled programmatically.

### Initialize a `DataSource`

If you need the library not only as an `AgtySQL` facade but also as a JDBC
`DataSource` for external code, two levels are available:

- `AgtySqlDataSource`:
  a basic non-pooled `DataSource` that creates raw JDBC connections from
  `AgtySqlConfig`;
- `AgtySqlPooledDataSource`:
  a HikariCP-backed pooled `DataSource` compatibility adapter that can be used
  as an application-level bean.

Basic example:

```java
AgtySqlConfig config = new AgtySqlConfig()
        .setDriver("pgsql")
        .setServer("127.0.0.1")
        .setPort(5432)
        .setDatabase("app_db")
        .setUser("app_user")
        .setPassword("app_password")
        .setSchema("private")
        .setPfx("");

DataSource dataSource = new AgtySqlDataSource(config);
```

If you need a pool at the `DataSource` level:

```java
AgtySqlConfig config = new AgtySqlConfig()
        .setDriver("pgsql")
        .setServer("127.0.0.1")
        .setPort(5432)
        .setDatabase("app_db")
        .setUser("app_user")
        .setPassword("app_password")
        .setSchema("private")
        .setPfx("");

DataSource dataSource = new AgtySqlPooledDataSource(
        config,
        10,      // maxPoolSize
        5,       // minIdle
        30000,   // connectionTimeoutMillis
        600000,  // idleTimeoutMillis
        1800000  // maxLifetimeMillis
);
```

Practical rule:

- if a pool already exists in an external library or container,
  `AgtySqlDataSource` is enough;
- if you need your own pooled `DataSource` bean, use
  `AgtySqlPooledDataSource`.

### Initialize `AgtySQLPool`

If you need a pool of ready-to-use `AgtySQL` sessions rather than a
`DataSource`, use `AgtySQLPool` or the `ConnectionPool` registry.

Direct creation:

```java
AgtySqlConfig config = new AgtySqlConfig()
        .setDriver("pgsql")
        .setServer("127.0.0.1")
        .setPort(5432)
        .setDatabase("app_db")
        .setUser("app_user")
        .setPassword("app_password")
        .setSchema("private")
        .setPfx("");

AgtySQLPool pool = new AgtySQLPool(
        config,
        10,
        Duration.ofMinutes(30),
        Duration.ofSeconds(30)
);
```

Registry-based creation:

```java
ConnectionPool.register(
        "main",
        () -> new AgtySqlConfig()
                .setDriver("pgsql")
                .setServer("127.0.0.1")
                .setPort(5432)
                .setDatabase("app_db")
                .setUser("app_user")
                .setPassword("app_password")
                .setSchema("private")
                .setPfx(""),
        new ConnectionPool.PoolOptions(
                10,
                Duration.ofMinutes(30),
                Duration.ofSeconds(30)
        )
);

AgtySQLPool pool = ConnectionPool.get("main");
```

Borrow a session from the pool:

```java
try (AgtySQLPool.PooledAgtySQL borrowed = pool.borrow()) {
    AgtySQL sql = borrowed.sql();

    SqlRow row = sql.fetch(
            Arguments.builder()
                    .setTable("{users}")
                    .useStatementPrepare(true)
                    .setWhere("[id] = ?", 1)
    );
}
```

Notes:

- `AgtySQLPool` returns a ready `AgtySQL`, not a `Connection`;
- each borrow creates a one-use lease and facade; `borrowed.close()` closes
  that lease and returns its JDBC connection to HikariCP;
- a closed `PooledAgtySQL` must not be reused;
- this is a session-level facade pool, not a Spring JDBC `DataSource`;
- for a Spring Boot `DataSource` bean, use `AgtySqlPooledDataSource`, not
  `AgtySQLPool`.

### First fetch

```java
SqlRow row = sql.fetch(
        Arguments.builder()
                .setTable("{users}")
                .useStatementPrepare(true)
                .setWhere("[id] = ?", 1)
);
```

### First insert

```java
sql.insert(
        Arguments.builder()
                .setTable("{users}")
                .useStatementPrepare(true)
                .addDataString("name", "Alex")
                .addDataInt("age", 30)
);
```

### Basic lifecycle

```java
AgtySQL sql = new AgtySQL("mysql");

try {
        SqlRow row = sql.fetch(
            Arguments.builder()
                    .setTable("{users}")
                    .useStatementPrepare(true)
                    .setWhere("[id] = ?", 1)
    );
} finally {
    sql.close();
}
```

High-level methods close their temporary JDBC resources automatically, but the
`AgtySQL` instance itself should be closed at the end of its scope.

`AgtySqlPooledDataSource` works differently:

- the application obtains `Connection` via `dataSource.getConnection()`;
- `Connection.close()` returns the physical connection to the pool;
- `AgtySqlPooledDataSource` itself should be closed at application shutdown or
  bean-container shutdown.

`AgtySQLPool` works at another level:

- the application borrows `PooledAgtySQL` through `pool.borrow()`;
- it then gets `AgtySQL` via `borrowed.sql()`;
- `PooledAgtySQL.close()` invalidates that facade and returns only its JDBC
  connection to HikariCP;
- this is suitable for application code around the library, but it does not
  replace a standard Spring JDBC `DataSource` bean.

### SQL Server TLS

SQL Server connections always use encryption and validate the server
certificate by default. Keep `trustServerCertificate` unset or `false` in
production. Only local development with a self-signed certificate should use:

```java
config.setTrustServerCertificate(true);
```
