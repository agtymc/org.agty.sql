# 01. Быстрый старт

### Инициализация по `config.ini`

```java
AgtySQL sql = new AgtySQL();
```

Этот конструктор использует `config.ini` из корня проекта и секцию по
умолчанию.

### Инициализация по имени секции

```java
AgtySQL sql = new AgtySQL("mysql");
```

Этот вариант удобен, если в конфиге есть несколько подключений.

### Инициализация по явному объекту конфигурации

```java
AgtySqlConfig config = new AgtySqlConfig();
AgtySQL sql = new AgtySQL(config);
```

Такой вариант нужен, когда конфигурация собирается программно.

### Инициализация `DataSource`

Если библиотека нужна не только как `AgtySQL` facade, но и как JDBC
`DataSource` для внешнего кода, доступны два уровня:

- `AgtySqlDataSource`:
  базовый non-pooled `DataSource`, который просто создает raw JDBC
  connections по `AgtySqlConfig`;
- `AgtySqlPooledDataSource`:
  pooled `DataSource`, который можно использовать как application-level bean
  вместо `HikariDataSource`.

Базовый пример:

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

Если нужен pool на уровне `DataSource`:

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

Практическое правило:

- если pool уже есть во внешней библиотеке или контейнере, достаточно
  `AgtySqlDataSource`;
- если нужен собственный bean уровня `DataSource` с пулом, использовать
  `AgtySqlPooledDataSource`.

### Инициализация `AgtySQLPool`

Если нужен не `DataSource`, а пул готовых `AgtySQL`-сессий, использовать
нужно `AgtySQLPool` или registry `ConnectionPool`.

Базовый пример прямого создания:

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

Пример через именованный registry:

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

Забор сессии из пула:

```java
try (AgtySQLPool.PooledAgtySQL borrowed = pool.borrow()) {
    AgtySQL sql = borrowed.sql();

    SqlRow row = sql.fetch(
            Arguments.builder()
                    .setTable("{users}")
                    .setWhere("[id] = %d", 1)
    );
}
```

Пояснения:

- `AgtySQLPool` выдает не `Connection`, а готовый `AgtySQL`;
- `borrowed.close()` возвращает сессию обратно в пул;
- это pool session-level facade, а не `DataSource` для Spring JDBC;
- для `Spring Boot` bean `DataSource` нужен `AgtySqlPooledDataSource`, а не
  `AgtySQLPool`.

### Первая выборка

```java
SqlRow row = sql.fetch(
        Arguments.builder()
                .setTable("{users}")
                .setWhere("[id] = %d", 1)
);
```

### Первая вставка

```java
sql.insert(
        Arguments.builder()
                .setTable("{users}")
                .addData("name", "Alex")
                .addData("age", 30)
);
```

### Базовый lifecycle

```java
AgtySQL sql = new AgtySQL("mysql");

try {
    SqlRow row = sql.fetch(
            Arguments.builder()
                    .setTable("{users}")
                    .setWhere("[id] = %d", 1)
    );
} finally {
    sql.close();
}
```

High-level методы сами закрывают временные JDBC-ресурсы, но сам `AgtySQL`
нужно закрывать в конце работы.

`AgtySqlPooledDataSource` работает иначе:

- приложение берет `Connection` через `dataSource.getConnection()`;
- `Connection.close()` возвращает physical connection обратно в пул;
- закрывать сам `AgtySqlPooledDataSource` нужно в конце lifecycle приложения
  или bean-container.

`AgtySQLPool` работает на другом уровне:

- приложение берет `PooledAgtySQL` через `pool.borrow()`;
- затем получает `AgtySQL` через `borrowed.sql()`;
- `PooledAgtySQL.close()` возвращает именно facade/session обратно в пул;
- этот вариант подходит для прикладного кода библиотеки, но не заменяет
  стандартный `DataSource` bean в Spring JDBC-интеграциях.
