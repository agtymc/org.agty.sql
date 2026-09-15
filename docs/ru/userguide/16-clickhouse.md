# 16. ClickHouse

ClickHouse поддерживается как JDBC SQL-диалект для аналитических сценариев:
чтение, курсоры, low-level JDBC, обычные `INSERT` и базовые mutation-команды.
Это не транзакционная OLTP-СУБД, поэтому часть write-return API намеренно
отключена capability-моделью.

## Локальный Docker

Для локальной тестовой базы используйте installer:

```bash
./install/install-clickhouse.sh
```

По умолчанию он создает:

- container: `agty-sql-clickhouse`
- database: `agty_sql`
- user: `agty_sql`
- password: `agty_sql`
- HTTP/JDBC port: `28123`
- native TCP port: `29000`

Пароль можно переопределить:

```bash
AGTY_SQL_CLICKHOUSE_PASSWORD="secret" ./install/install-clickhouse.sh
```

## Maven

JDBC-драйвер ClickHouse является optional runtime dependency библиотеки. Если
приложение подключает драйверы явно, добавьте:

```xml
<dependency>
    <groupId>com.clickhouse</groupId>
    <artifactId>clickhouse-jdbc</artifactId>
    <version>0.10.0</version>
    <scope>runtime</scope>
</dependency>
```

## Конфигурация

Пример секции:

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

Создание фасада:

```java
AgtySQL sql = new AgtySQL("clickhouse");
```

## Создание Таблицы

ClickHouse требует явного engine:

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

Префикс `{events}` будет обработан обычным query rebuild механизмом AgtySQL.

## Запись И Чтение

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

Для больших выборок используйте cursor API:

```java
try (AgtySqlCursor cursor = sql.openCursor(
        Arguments.builder().setTable("{events}").setOrderBy("id ASC"))) {
    SqlRow row;
    while ((row = cursor.next()) != null) {
        // process row
    }
}
```

## Ограничения

- `setReturnLastInsertId(true)` не поддерживается.
- `insertAndGet()` не поддерживается.
- `updateAndGet()` не поддерживается.
- generated keys через high-level API не объявлены как supported capability.
- `UPDATE` и `DELETE` строятся через `ALTER TABLE ... UPDATE/DELETE WHERE`;
  учитывайте ClickHouse mutation semantics и eventual completion.
- Полагайтесь на low-level JDBC для ClickHouse-специфичных запросов, настроек
  и DDL, которые выходят за переносимый AgtySQL CRUD.
