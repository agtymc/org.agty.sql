# 09. Курсоры и потоковое чтение

Для больших выборок поддерживаются два официальных режима.

### Прямой JDBC `ResultSet`

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

Плюсы:

- максимальный контроль;
- привычный JDBC;
- удобно для внешних интеграций.

Минусы:

- lifecycle полностью на разработчике;
- нет `SqlRow`-обертки.

### Библиотечный `AgtySqlCursor`

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

Есть и raw-query вариант:

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

Свойства `AgtySqlCursor`:

- forward-only чтение;
- возвращает `SqlRow`;
- закрывается автоматически на конце выборки;
- безопасен для `try-with-resources`.

### Legacy list cursor

```java
SqlRow row = sql.list(arguments, 0);
```

Этот режим сохраняется для совместимости.

Дополнительные методы:

- `hasOpenListCursor(index)`
- `closeListCursor(index)`
- `closeListCursors()`

Для нового кода предпочтителен `AgtySqlCursor`.
