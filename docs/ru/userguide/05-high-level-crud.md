# 05. High-Level CRUD

Все CRUD-операции через `Arguments` поддерживают opt-in prepared-режим:

```java
SqlRow user = sql.fetch(
        Arguments.builder()
                .useStatementPrepare(true)
                .setTable("{users}")
                .setWhere("[email] = ?", email)
);
```

Тот же режим действует для `fetch`, list/cursor-чтения, `rowIsExists`,
`countRows`, `min/max`, insert, update, delete, generated ID и
`insertAndGet/updateAndGet`. Правила привязки и legacy-совместимость описаны в
разделе про `Arguments`.

### Fetch одной строки

```java
SqlRow user = sql.fetch(
        Arguments.builder()
                .setTable("{users}")
                .setWhere("[id] = %d", 10)
);
```

Есть также overload `fetch(String query)`, если нужен явный SQL.

### Fetch в объект

```java
User user = sql.fetch(
        Arguments.builder()
                .setTable("{users}")
                .setWhere("[id] = %d", 10),
        User.class
);
```

Этот паттерн уместен там, где проект уже использует object/entity mapping
поверх библиотеки.

### Проверка существования строки

```java
Boolean exists = sql.rowIsExists(
        Arguments.builder()
                .setTable("{users}")
                .setWhere("[email] = '%s'", "alex@example.com")
);
```

### List через legacy cursor API

```java
SqlRow row;
while ((row = sql.list(
        Arguments.builder()
                .setTable("{users}")
                .setOrderBy("id ASC"),
        2
)) != null) {
    System.out.println(row.getLong("id"));
}

sql.closeListCursor(2);
```

Это совместимый legacy-паттерн. Для нового кода лучше использовать
`openCursor(...)`.

На текущем этапе `list(...)` и `list(..., index)` считаются deprecated для
нового кода.

### List всех строк в память

```java
LinkedList<SqlRow> rows = sql.listArray(
        Arguments.builder()
                .setTable("{users}")
                .setWhere("[active] = %d", 1)
                .setOrderBy("id ASC")
);
```

Alias:

- `findAll(...)` это alias для `listArray(...)`.

На текущем этапе `findAll(...)` считается deprecated alias.

### Insert

```java
long insertedId = sql.insert(
        Arguments.builder()
                .setTable("{users}")
                .addData("id", 10)
                .addData("name", "Alex")
);
```

Если `setReturnLastInsertId(true)` не задан, обычный insert может вернуть `0`.
Это нормальный сценарий для ручного primary key или для insert без запроса
generated id.

### Insert с generated id

```java
long insertedId = sql.insert(
        Arguments.builder()
                .setTable("{users}")
                .addData("name", "Alex")
                .setReturnLastInsertId(true)
);
```

Этот сценарий доступен не для всех драйверов.

`lastInsertId(Arguments)` на текущем этапе считается deprecated helper.
Для нового кода предпочтительно использовать capability-driven сценарий через
`insert(... setReturnLastInsertId(true))`.

На дату `2026-08-04` стратегии такие:

- MySQL / MariaDB: session-safe connection function;
- PostgreSQL: session-safe sequence function через `currval(...)`;
- SQLite: session-safe connection function;
- H2: fallback `FETCH_LAST_ROW_UNSAFE`.

Для H2 это означает:

- библиотека после insert выбирает последнюю строку по primary key;
- такой режим может давать коллизии при конкурентных вставках;
- для многопоточных сценариев это нужно считать collision-prone fallback, а не
  безопасной cross-session гарантией.

### Insert и сразу вернуть строку

```java
SqlRow inserted = sql.insertAndGet(
        Arguments.builder()
                .setTable("{users}")
                .addData("name", "Alex"),
        "id, name"
);
```

Есть и overload без явного списка полей:

```java
SqlRow inserted = sql.insertAndGet(
        Arguments.builder()
                .setTable("{users}")
                .addData("name", "Alex")
);
```

На дату `2026-08-04` `insertAndGet(...)` работает так:

- PostgreSQL: native returning;
- MySQL / MariaDB / SQLite: follow-up fetch после insert;
- H2: follow-up fetch после insert через collision-prone fallback получения
  последнего ID.

Ограничение:

- raw `String query` overload для `insertAndGet(...)` не является универсальным
  cross-driver контрактом;
- на follow-up драйверах библиотеке нужна metadata вроде `table` и `primaryKey`,
  иначе строку после insert восстановить нельзя;
- поэтому `insertAndGet(String query, ...)` надежно работает только там, где
  драйвер умеет native returning, либо когда используется `Arguments` с
  достаточной metadata.

### Batch insert через массив `Arguments`

```java
ArrayList<Arguments> batch = new ArrayList<>();
batch.add(
        Arguments.builder()
                .setTable("{users}")
                .addData("id", 1)
                .addData("name", "A")
);
batch.add(
        Arguments.builder()
                .setTable("{users}")
                .addData("id", 2)
                .addData("name", "B")
);

sql.insert(batch);
```

### Update

```java
boolean updated = sql.update(
        Arguments.builder()
                .setTable("{users}")
                .addData("name", "Alex Updated")
                .setWhere("[id] = %d", 10)
);
```

### Update и вернуть строку

```java
SqlRow updated = sql.updateAndGet(
        Arguments.builder()
                .setTable("{users}")
                .addData("name", "Alex Updated")
                .setWhere("[id] = %d", 10),
        "id, name"
);
```

### Delete

```java
boolean deleted = sql.delete(
        Arguments.builder()
                .setTable("{users}")
                .setWhere("[id] = %d", 10)
);
```

Alias:

- `del(...)` это alias для `delete(...)`.

На текущем этапе `del(...)` считается deprecated alias.

### Count rows

```java
Long count = sql.countRows(
        Arguments.builder()
                .setTable("{users}")
);
```

Alias:

- `rows(...)` это alias для `countRows(...)`.

На текущем этапе `rows(...)` считается deprecated alias.
