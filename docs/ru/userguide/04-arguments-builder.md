# 04. Построитель Arguments

`Arguments` это основной контейнер параметров для high-level операций.

Базовая форма:

```java
Arguments arguments = Arguments.builder()
        .setTable("{users}")
        .setFields("id, name")
        .setWhere("[active] = %d", 1)
        .setOrderBy("id ASC")
        .setLimit(10);
```

### Таблица и поля

```java
Arguments.builder()
        .setTable("{users}")
        .setFields("id, name, email");
```

- `setTable(...)` задает таблицу.
- `setFields(...)` задает список полей для select-like операций.
- если `fields` не задан, используется `*`.

Рекомендуется использовать ту же форму имен таблиц, что уже принята в проекте,
например `"{users}"`.

### WHERE

```java
Arguments.builder()
        .setTable("{users}")
        .setWhere("[id] = %d", 10);
```

Есть два варианта:

- `setWhere(String where)`
- `setWhere(String pattern, Object... args)`

Можно накапливать условие вручную:

```java
Arguments.builder()
        .setTable("{users}")
        .setWhere("[active] = 1")
        .appendWhere(" AND [role] = 'admin'");
```

`appendWhere(...)` полезен только если вы осознанно собираете выражение
по частям.

### ORDER BY, GROUP BY, HAVING

```java
Arguments.builder()
        .setTable("{orders}")
        .setFields("user_id, COUNT(*) as total")
        .setGroupBy("user_id")
        .setHaving("COUNT(*) > 3")
        .setOrderBy("total DESC");
```

Поддерживаемые методы:

- `setOrderBy(...)`
- `setGroupBy(...)`
- `setHaving(...)`

### LIMIT и OFFSET

```java
Arguments.builder()
        .setTable("{users}")
        .setLimit(20)
        .setOffset(40);
```

Или строковой формой:

```java
Arguments.builder()
        .setTable("{users}")
        .setLimit("40,20");
```

Это означает: пропустить `40` строк и вернуть `20`.

### Data для insert/update

```java
Arguments.builder()
        .setTable("{users}")
        .addData("name", "Alex")
        .addData("age", 30)
        .addData("active", true);
```

Поддерживаются основные scalar-типы:

- `String`
- `Integer`
- `Long`
- `Short`
- `Boolean`
- `Float`
- `Double`
- `Character`

Порядок полей сохраняется, потому что внутри используется `LinkedHashMap`.

### Raw query

```java
Arguments.builder()
        .setQuery("SELECT id, name FROM {users} WHERE [active] = 1 ORDER BY id ASC");
```

Если задан `query`, он имеет приоритет над составными параметрами вроде
`table`, `fields`, `where`.

Использовать этот режим стоит, когда:

- high-level builder недостаточен;
- SQL проще выразить вручную;
- нужно сохранить работу через `AgtySQL`, но без сборки запроса из частей.

### Action field и primary key

```java
Arguments.builder()
        .setTable("{users}")
        .setActionField("id");
```

`actionField` используется в ряде metadata/fetch сценариев, например:

- `getFirstRow(...)`
- `getLastRow(...)`
- `min(...)`
- `max(...)`

`primaryKey` доступен как часть модели аргументов, но в типовом CRUD-коде
чаще всего достаточно `where`.

### Columns

```java
Arguments.builder()
        .addColumn("name")
        .addColumn("email");
```

`columns` это отдельная коллекция внутри `Arguments`. Она нужна не для
базового CRUD-сценария, а для более узких внутренних и metadata-паттернов.
Если нет явной необходимости, в пользовательском коде можно на нее не
опираться.

### Поведенческие флаги

#### `setReturnLastInsertId(true)`

```java
long id = sql.insert(
        Arguments.builder()
                .setTable("{users}")
                .addData("name", "Alex")
                .setReturnLastInsertId(true)
);
```

Использовать только там, где драйвер официально поддерживает возврат
последнего inserted id. Если драйвер не поддерживает сценарий, библиотека
должна завершиться ошибкой сразу.

#### `setNoRebuildQuery(true)`

```java
Arguments.builder()
        .setQuery("SELECT 1")
        .setNoRebuildQuery(true);
```

Отключает rebuild query pipeline. Использовать только если запрос уже готов
к выполнению и не должен проходить через преобразование библиотеки.

#### `setForceRebuildQuery(true)`

```java
Arguments.builder()
        .setQuery("SELECT * FROM {users}")
        .setForceRebuildQuery(true);
```

Форсирует rebuild там, где это требуется явно.

#### `setNoStringEncode(true)`

Отключает string encoding для значений. Это режим с повышенной осторожностью,
его стоит использовать только если вы точно понимаете, как дальше драйвер
обрабатывает значение.

#### `convertValueToString(true)`

```java
Arguments.builder()
        .setTable("{users}")
        .convertValueToString(true);
```

Заставляет библиотеку возвращать значения как строки в fetch/list-like
сценариях.

#### `setEmulateMode(true)`

Режим эмуляции. Запросы не отправляются в базу. Это специализированный флаг,
который обычно не нужен в обычном приложении.

### Практические шаблоны `Arguments`

#### Fetch по id

```java
Arguments.builder()
        .setTable("{users}")
        .setWhere("[id] = %d", 10);
```

#### List с сортировкой и ограничением

```java
Arguments.builder()
        .setTable("{users}")
        .setFields("id, name")
        .setWhere("[active] = %d", 1)
        .setOrderBy("id ASC")
        .setLimit(100);
```

#### Update по условию

```java
Arguments.builder()
        .setTable("{users}")
        .addData("name", "Alex")
        .addData("age", 30)
        .setWhere("[id] = %d", 10);
```

#### Aggregate/metadata сценарий

```java
Arguments.builder()
        .setTable("{orders}")
        .setActionField("id");
```
