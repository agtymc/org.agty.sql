# AgtySQL Session API

Этот документ фиксирует публичный session/JDBC contract класса `AgtySQL`.

Назначение документа:
- описать, как использовать `AgtySQL` как high-level API;
- описать, как использовать `AgtySQL` как low-level JDBC entry point;
- зафиксировать lifecycle ресурсов;
- дать базу для будущего полного пользовательского мануала.

## Роли `AgtySQL`

`AgtySQL` поддерживает два основных режима работы.

### 1. High-level режим

Используется для типовых сценариев:

- `fetch(...)`
- `listArray(...)`
- `insert(...)`
- `insertAndGet(...)`
- `update(...)`
- `updateAndGet(...)`
- `delete(...)`
- `countRows(...)`

Здесь библиотека сама:
- собирает SQL;
- работает с драйвером;
- преобразует результат в `SqlRow`;
- управляет типовым lifecycle временных JDBC-объектов.

Entity/model convenience layer (`fetch(..., entity)`, `insertEntity*`,
`saveEntity*`) сохраняется как поддерживаемая часть high-level API.
Для сценариев, где важна максимальная прозрачность cross-db поведения,
по-прежнему полезен явный CRUD через `Arguments`.

Важно:
- этот слой не является JPA-совместимостью;
- это собственный convenience API библиотеки для entity-oriented сценариев;
- он считается вторичным по отношению к базовому CRUD/JDBC фасаду, но остается
  поддерживаемой частью публичного API.
- это направление уже включает entity-return overloads поверх обычных
  write-операций, например `insertAndGet(..., Class<T>)` и
  `updateAndGet(..., Class<T>)`.
- эти write-return операции теперь не ограничены только native-returning
  драйверами: часть dialects использует documented follow-up fetch strategy.
- а также query-based overloads вроде `insertAndGet(String, Class<T>)` и
  `updateAndGet(String, Class<T>)`.
- но query-based write-return overloads не являются одинаково portable для всех
  follow-up драйверов: если после write нужен post-fetch, библиотеке нужна
  metadata (`table`, `where`, `primaryKey`), которой у голой строки запроса
  может не быть.
- и короткие формы `insert(..., Class<T>)` / `update(..., Class<T>)`,
  которые делегируют в entity-return write flow.
- на дату `2026-08-04` текущий query-based набор считается достаточным:
  дополнительные query-based overloads для `insertEntity*`, `updateEntity`,
  `saveEntity*` не планируются внутри `2.x`, потому что это object-driven API,
  а не query-driven API.

### 2. Low-level session/JDBC режим

Используется, когда разработчику нужен прямой контроль над JDBC:

- `getConnection()`
- `getStatement()`
- `prepareStatement(...)`
- `executeBatch(...)`
- `getGeneratedKeys(...)`
- `openCursor(...)`
- `beginTransaction()`
- `setAutoCommit(...)`
- `commit()`
- `rollback()`
- `setFetchSize(...)`

Этот режим нужен для:
- нестандартных SQL-запросов;
- ручной работы с `PreparedStatement`;
- batch execution;
- generated keys;
- streaming/cursor сценариев;
- явного transaction control.

При этом raw `ResultSet` helper-методы уровня `executeQuery(...)`,
`executeResultSet(...)`, `statementExecuteQuery(...)` и похожие больше не
считаются целевым API для нового кода. Для новых сценариев следует
предпочитать:

- `prepareStatement(...).executeQuery()`
- `getStatement()`
- `openCursor(...)`

На дату `2026-08-05` этот low-level контракт внутри фасада уже отделяется
через internal bridge `AgtySqlSessionSupport`, а рабочие lifecycle-классы
находятся в `org.agty.sql.session`. Пакет `org.agty.sql.connect` сохранен как
частичный compatibility-layer на период `2.x`: в нем оставлен только
`AgtySqlConnector`, потому что он все еще участвует в public API `AgtySQL`.

Аналогично на internal driver side:

- public capability/value типы остаются в `org.agty.sql.driver`;
- concrete dialect implementations уже могут жить в `org.agty.sql.dialect.*`;
- query/helper packages конкретных dialects тоже могут жить в
  `org.agty.sql.dialect.*`;
- внутренний выбор concrete dialect implementation теперь может идти через
  `DialectDriverRegistry`;
- deprecated helper/concrete classes вне `AgtySQL`, которые не участвуют в
  public сигнатурах фасада, удалены;
- compatibility в `2.x` на текущем этапе сохраняется в первую очередь через
  deprecated методы самого `AgtySQL` и через типы, которые все еще видны в его
  public API, например `org.agty.sql.connect.AgtySqlConnector`.

## Поддерживаемые low-level сценарии

### Connection

```java
AgtySQL sql = new AgtySQL("mysql");
Connection connection = sql.getConnection();
```

Когда использовать:
- нужна интеграция со сторонним JDBC-кодом;
- нужен доступ к специфическим JDBC-настройкам;
- нужно передать соединение во внешний код.

### Statement

```java
try (Statement statement = sql.getStatement()) {
    statement.execute("SELECT 1");
}
```

Когда использовать:
- одноразовый ручной SQL;
- batch через обычный `Statement`;
- ручная работа с JDBC без параметров.

### PreparedStatement

```java
try (PreparedStatement prepared = sql.prepareStatement(
        "INSERT INTO {users} (name, age) VALUES (?, ?)"
)) {
    prepared.setString(1, "Alex");
    prepared.setInt(2, 30);
    prepared.executeUpdate();
}
```

Важные режимы:
- `prepareStatement(query)`:
  запрос будет пропущен через rebuild-пайплайн библиотеки;
- `prepareStatement(query, boolean noRebuildQuery)`:
  можно отключить rebuild;
- `prepareStatement(query, autoGeneratedKeys)`:
  helper для generated keys;
- `prepareStatement(query, autoGeneratedKeys, noRebuildQuery)`:
  полный контроль.

### Generated keys

```java
try (PreparedStatement prepared = sql.prepareStatement(
        "INSERT INTO {users} (name) VALUES (?)",
        Statement.RETURN_GENERATED_KEYS
)) {
    prepared.setString(1, "Alex");
    prepared.executeUpdate();

    SqlRow keys = sql.getGeneratedKeys(prepared);
}
```

Когда использовать:
- нужен JDBC-style generated key flow;
- нужно работать ниже уровня `insert(... setReturnLastInsertId(true))`.

### Batch

```java
int[] result = sql.executeBatch(List.of(
        "INSERT INTO {users} (id, name) VALUES (1, 'A')",
        "INSERT INTO {users} (id, name) VALUES (2, 'B')"
));
```

Когда использовать:
- нужно выполнить несколько SQL-команд одним batch;
- не хочется вручную создавать `Statement` и наполнять `addBatch(...)`.

### Cursor / streaming

Есть два варианта.

#### Вариант 1. Прямой JDBC ResultSet

```java
try (PreparedStatement prepared = sql.prepareStatement(
        "SELECT * FROM {users} ORDER BY id"
);
     ResultSet resultSet = prepared.executeQuery()) {

    while (resultSet.next()) {
        long id = resultSet.getLong("id");
    }
}
```

Плюсы:
- максимальный контроль;
- чистый JDBC;
- удобно для внешних интеграций.

Минусы:
- lifecycle полностью на разработчике;
- нет `SqlRow`-обертки.

#### Вариант 2. Библиотечный `AgtySqlCursor`

```java
try (AgtySqlCursor cursor = sql.openCursor(
        Arguments.builder()
                .setTable("{users}")
                .setOrderBy("id ASC")
)) {
    SqlRow row;
    while ((row = cursor.next()) != null) {
        long id = row.getLong("id");
    }
}
```

Итерация через `hasNext()` тоже поддерживается:

```java
try (AgtySqlCursor cursor = sql.openCursor(
        "SELECT * FROM {users} ORDER BY id"
)) {
    while (cursor.hasNext()) {
        long id = cursor.next().getLong("id");
    }
}
```

Плюсы:
- forward-only API поверх JDBC;
- возвращает `SqlRow`;
- библиотека закрывает курсор при достижении конца выборки;
- удобно для типового streaming-сценария.

Минусы:
- это библиотечный abstraction, а не чистый JDBC;
- для очень специфичных сценариев прямой `ResultSet` может быть удобнее.

## Ownership и lifecycle

### High-level методы

Примеры:
- `fetch(...)`
- `listArray(...)`
- `insert(...)`
- `update(...)`

Кто владеет ресурсами:
- библиотека.

Что ожидается от разработчика:
- ничего дополнительно закрывать не нужно.

### `Statement` и `PreparedStatement`

Кто владеет ресурсами:
- разработчик.

Правило:
- если получил `Statement` или `PreparedStatement` напрямую, закрывай его сам через `try-with-resources`.

### Прямой `ResultSet`

Кто владеет ресурсами:
- разработчик.

Правило:
- если получил `ResultSet` напрямую, закрывай `ResultSet` и соответствующий `Statement` сам;
- предпочтительный стиль: `try-with-resources`.

### `AgtySqlCursor`

Кто владеет ресурсами:
- библиотека частично, разработчик частично.

Правило:
- курсор нужно закрывать через `try-with-resources`;
- при достижении конца выборки курсор закрывается автоматически;
- повторный `close()` допустим.

### `list(index)` legacy-cursor режим

Этот режим сохраняется для совместимости и постепенной миграции.

Правила:
- один `index` соответствует одному внутреннему cursor state;
- `closeListCursor(index)` закрывает конкретный cursor;
- `closeListCursors()` закрывает все list cursors;
- `AgtySQL.close()` закрывает все открытые list cursors автоматически.

Рекомендация:
- для нового кода предпочитать `AgtySqlCursor`.

## Транзакции

### Базовый шаблон

```java
AgtySQL sql = new AgtySQL("mysql");

try {
    sql.beginTransaction();

    sql.insert(...);
    sql.update(...);

    sql.commit();
} catch (Exception e) {
    sql.rollback();
    throw e;
} finally {
    sql.setAutoCommit(true);
    sql.close();
}
```

Важно:
- `beginTransaction()` сейчас эквивалентен `setAutoCommit(false)`;
- после ручной транзакции разработчик сам отвечает за возврат режима `autoCommit`, если это требуется сценарием.

## `fetchSize`

`fetchSize` относится к low-level/cursor сценариям.

```java
sql.setFetchSize(100);
```

Что гарантируется библиотекой:
- значение применяется к создаваемым `Statement`.

Что может отличаться:
- фактическое поведение зависит от JDBC-драйвера и конкретной СУБД;
- некоторые драйверы требуют специальных условий для реального server-side cursor / streaming behavior.

## Когда использовать что

Используй high-level API, если:
- запрос типовой;
- нужен `SqlRow`;
- не нужен прямой JDBC-контроль.

Используй `PreparedStatement`, если:
- есть параметры;
- нужны generated keys;
- нужен ручной JDBC flow.

Для `lastInsertId`-подобных сценариев важно различать уровень надежности
стратегии драйвера:

- session-safe function/sequence strategy;
- transaction-safe fallback;
- collision-prone fallback вроде `FETCH_LAST_ROW_UNSAFE`.

На дату `2026-08-04` в коде уже есть пример такого fallback для H2:

- high-level API не отключается полностью;
- вместо этого используется post-insert выборка последней строки по primary key;
- этот режим нужно считать unsafe при конкурентных insert-сценариях.

Используй `AgtySqlCursor`, если:
- нужен forward-only streaming;
- хочется получать `SqlRow`;
- нужен типовой library-managed cursor lifecycle.

Используй прямой `ResultSet`, если:
- нужен полный JDBC-контроль;
- нужен нестандартный способ обхода/чтения;
- библиотечный cursor abstraction уже мешает.

## Статус API

На текущем этапе:
- этот контракт уже поддерживается кодом и тестами;
- возможна дальнейшая нормализация имен;
- `list(index)` считается сохраненным compatibility-режимом, но не целевым API для нового кода.
