# 09. Cursor And Streaming

For large result sets, two official modes are supported.

### Direct JDBC `ResultSet`

```java
try (PreparedStatement prepared = sql.prepareStatement(
        "SELECT * FROM {users} ORDER BY id"
);
     ResultSet resultSet = prepared.executeQuery()) {

    while (resultSet.next()) {
        long id = resultSet.getLong("id");
        System.out.println(id);
    }
}
```

Pros:

- maximum control;
- familiar JDBC;
- convenient for external integrations.

Cons:

- lifecycle is fully manual;
- there is no `SqlRow` wrapper.

### Library `AgtySqlCursor`

```java
try (AgtySqlCursor cursor = sql.openCursor(
        Arguments.builder()
                .setTable("{users}")
                .setOrderBy("id ASC")
)) {
    SqlRow row;
    while ((row = cursor.next()) != null) {
        System.out.println(row.getLong("id"));
    }
}
```

There is also a raw-query variant:

```java
try (AgtySqlCursor cursor = sql.openCursor(
        "SELECT * FROM {users} ORDER BY id"
)) {
    SqlRow row;
    while ((row = cursor.next()) != null) {
        System.out.println(row.getLong("id"));
    }
}
```

An iterative `hasNext()` style is also supported:

```java
try (AgtySqlCursor cursor = sql.openCursor(
        "SELECT * FROM {users} ORDER BY id"
)) {
    while (cursor.hasNext()) {
        System.out.println(cursor.next().getLong("id"));
    }
}
```

Properties of `AgtySqlCursor`:

- forward-only reading;
- returns `SqlRow`;
- supports both `while ((row = cursor.next()) != null)` and `while (cursor.hasNext()) { cursor.next(); }` patterns;
- closes automatically at the end of the result set;
- safe for `try-with-resources`.

### Legacy list cursor

```java
SqlRow row = sql.list(arguments, 0);
```

This mode is kept for compatibility.

Additional methods:

- `hasOpenListCursor(index)`
- `closeListCursor(index)`
- `closeListCursors()`

For new code, prefer `AgtySqlCursor`.
