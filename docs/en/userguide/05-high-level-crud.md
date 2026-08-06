# 05. High-Level CRUD

### Fetch one row

```java
SqlRow user = sql.fetch(
        Arguments.builder()
                .setTable("{users}")
                .setWhere("[id] = %d", 10)
);
```

There is also a `fetch(String query)` overload if you need explicit SQL.

### Fetch into an object

```java
User user = sql.fetch(
        Arguments.builder()
                .setTable("{users}")
                .setWhere("[id] = %d", 10),
        User.class
);
```

This pattern is useful when the project already uses object/entity mapping on
top of the library.

### Check row existence

```java
Boolean exists = sql.rowIsExists(
        Arguments.builder()
                .setTable("{users}")
                .setWhere("[email] = '%s'", "alex@example.com")
);
```

### List through the legacy cursor API

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

This is a compatibility legacy pattern. For new code, prefer `openCursor(...)`.

At the current stage, `list(...)` and `list(..., index)` are considered
deprecated for new code.

### Load all rows into memory

```java
LinkedList<SqlRow> rows = sql.listArray(
        Arguments.builder()
                .setTable("{users}")
                .setWhere("[active] = %d", 1)
                .setOrderBy("id ASC")
);
```

Alias:

- `findAll(...)` is an alias for `listArray(...)`.

At the current stage, `findAll(...)` is considered a deprecated alias.

### Insert

```java
long insertedId = sql.insert(
        Arguments.builder()
                .setTable("{users}")
                .setData("id", 10)
                .setData("name", "Alex")
);
```

If `setReturnLastInsertId(true)` is not set, a normal insert may return `0`.
This is expected for manual primary keys or inserts that do not request a
generated ID.

### Insert with a generated ID

```java
long insertedId = sql.insert(
        Arguments.builder()
                .setTable("{users}")
                .setData("name", "Alex")
                .setReturnLastInsertId(true)
);
```

This scenario is not available for every driver.

`lastInsertId(Arguments)` is currently considered a deprecated helper. For new
code, prefer the capability-driven flow through
`insert(... setReturnLastInsertId(true))`.

As of `2026-08-04`, the strategies are:

- MySQL / MariaDB: session-safe connection function;
- PostgreSQL: session-safe sequence function through `currval(...)`;
- SQLite: session-safe connection function;
- H2: `FETCH_LAST_ROW_UNSAFE` fallback.

For H2 this means:

- after insert the library selects the last row by primary key;
- this mode may collide under concurrent inserts;
- for multi-threaded scenarios it must be treated as a collision-prone
  fallback, not as a safe cross-session guarantee.

### Insert and immediately return the row

```java
SqlRow inserted = sql.insertAndGet(
        Arguments.builder()
                .setTable("{users}")
                .setData("name", "Alex"),
        "id, name"
);
```

There is also an overload without an explicit field list:

```java
SqlRow inserted = sql.insertAndGet(
        Arguments.builder()
                .setTable("{users}")
                .setData("name", "Alex")
);
```

As of `2026-08-04`, `insertAndGet(...)` works like this:

- PostgreSQL: native returning;
- MySQL / MariaDB / SQLite: follow-up fetch after insert;
- H2: follow-up fetch after insert through a collision-prone last-ID fallback.

Limitation:

- the raw `String query` overload for `insertAndGet(...)` is not a universal
  cross-driver contract;
- on follow-up drivers the library needs metadata such as `table` and
  `primaryKey`, otherwise it cannot reconstruct the row after insert;
- therefore `insertAndGet(String query, ...)` is reliable only where the driver
  supports native returning, or when `Arguments` with enough metadata is used.

### Batch insert through an array of `Arguments`

```java
ArrayList<Arguments> batch = new ArrayList<>();
batch.add(
        Arguments.builder()
                .setTable("{users}")
                .setData("id", 1)
                .setData("name", "A")
);
batch.add(
        Arguments.builder()
                .setTable("{users}")
                .setData("id", 2)
                .setData("name", "B")
);

sql.insert(batch);
```

### Update

```java
boolean updated = sql.update(
        Arguments.builder()
                .setTable("{users}")
                .setData("name", "Alex Updated")
                .setWhere("[id] = %d", 10)
);
```

### Update and return the row

```java
SqlRow updated = sql.updateAndGet(
        Arguments.builder()
                .setTable("{users}")
                .setData("name", "Alex Updated")
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

- `del(...)` is an alias for `delete(...)`.

At the current stage, `del(...)` is considered a deprecated alias.

### Count rows

```java
Long count = sql.countRows(
        Arguments.builder()
                .setTable("{users}")
);
```

Alias:

- `rows(...)` is an alias for `countRows(...)`.

At the current stage, `rows(...)` is considered a deprecated alias.
