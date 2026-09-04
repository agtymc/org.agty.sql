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

Обычные String-аргументы проверяются как SQL-идентификаторы. Допускаются
простые имена, начинающиеся с буквы или `_` и продолжающиеся буквами, цифрами
или `_`, qualified-имена через `.`, а также документированные формы `{table}`
и `[column]`. Кавычки, комментарии, операторы и `;` отклоняются с
`IllegalArgumentException`.

Рекомендуется использовать ту же форму имен таблиц, что уже принята в проекте,
например `"{users}"`.

### WHERE

```java
Arguments.builder()
        .setTable("{users}")
        .setWhere("[id] = %d", 10);
```

Есть два варианта:

- `setWhere(String pattern, Object... args)`
- `setWhere(SqlExpression.trusted(...))` для статического raw-выражения

Можно накапливать условие вручную:

```java
Arguments.builder()
        .setTable("{users}")
        .setWhere(SqlExpression.trusted("[active] = 1"))
        .appendWhere(SqlExpression.trusted(" AND [role] = 'admin'"));
```

Непустые raw String-overloads `setWhere(String)` и `appendWhere(String)`
deprecated и отклоняются. Они не должны получать request data.

### ORDER BY, GROUP BY, HAVING

```java
Arguments.builder()
        .setTable("{orders}")
        .setFields(SqlExpression.trusted("user_id, COUNT(*) AS total"))
        .setGroupBy("user_id")
        .setHaving(SqlExpression.trusted("COUNT(*) > 3"))
        .setOrderBy("total DESC");
```

Поддерживаемые методы:

- `setOrderBy(...)`
- `setGroupBy(...)`
- `setHaving(...)`

String-варианты `setFields`, `setGroupBy` и `setOrderBy` принимают только
проверяемые идентификаторы и простые списки/направления сортировки. Функции,
aliases и другие полноценные SQL-выражения требуют явного
`SqlExpression.trusted(...)`.

`SqlExpression.trusted(...)` ничего не экранирует и не проверяет. Это явная
граница доверия только для статического SQL приложения или результата
собственного allowlist. Никогда не создавайте его из HTTP/request-параметров.

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
        .addDataString("name", "Alex")
        .addDataInt("age", 30)
        .addDataBoolean("active", true);
```

Именованные методы проверяют фактический runtime-тип. Это важно, если значение
возвращается как `Object`:

```java
Object value = post.getData();

Arguments.builder()
        .addDataString("name", value); // Только String или null.
```

Если `value` содержит `Integer`, DTO, коллекцию или другой неподходящий тип,
`addDataString(...)` сразу выбросит `IllegalArgumentException` до построения и
выполнения SQL.

Доступны методы:

- `addDataString(...)` для `String`;
- `addDataInt(...)` / `addDataInteger(...)` для `Integer`;
- `addDataLong(...)`, `addDataShort(...)`, `addDataByte(...)`;
- `addDataBoolean(...)` / `addDataBool(...)`;
- `addDataFloat(...)`, `addDataDouble(...)`;
- `addDataChar(...)` / `addDataCharacter(...)`;
- `addDataDecimal(...)` / `addDataNumber(...)` для любого `Number`;
- `addDataBigDecimal(...)` и `addDataBigInteger(...)` для строгой проверки
  соответствующих типов.
- `addDataNull(...)` для явного SQL `NULL` без неоднозначности overload-вызова.

`addDataDecimal(...)` принимает `Byte`, `Short`, `Integer`, `Long`, `Float`,
`Double`, `BigInteger`, `BigDecimal` и другие корректные реализации `Number`.
Нестандартная реализация `Number` нормализуется в `BigDecimal`. `NaN` и
бесконечные значения отклоняются.

Compatibility-overloads `addData(...)` сохранены. Новый
`addData(String, Object)` проверяет общий whitelist:

- `String`
- `Number`
- `Boolean`
- `Character`

Любой другой runtime-тип приводит к `IllegalArgumentException`; неизвестный
объект больше не преобразуется неявно через `toString()`.

Имя поля в любом методе `addData...` обязательно: `null` приводит к
`IllegalArgumentException` до сохранения данных и построения SQL.

Порядок полей сохраняется, потому что внутри используется `LinkedHashMap`.

### Raw query

```java
Arguments.builder()
        .setQuery(SqlExpression.trusted(
                "SELECT id, name FROM {users} WHERE [active] = 1 ORDER BY id ASC"
        ));
```

Если задан `query`, он имеет приоритет над составными параметрами вроде
`table`, `fields`, `where`.

Непараметризованный raw query требует `SqlExpression.trusted(...)`.
`setQuery(String)` deprecated и отклоняет непустой SQL. Для значений используйте
prepared-вариант `setQuery("... WHERE id = ?", id)`.

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

#### `useStatementPrepare(true)`

Prepared-режим включается явно. По умолчанию он отключён: библиотека сохраняет
legacy-сборку SQL и HTML-кодирование строковых значений `addData(...)`.

```java
boolean updated = sql.update(
        Arguments.builder()
                .useStatementPrepare(true)
                .setTable("{users}")
                .addDataString("name", "O'Reilly & <admin>")
                .setWhere("[id] = ?", 10)
);
```

Итоговый update содержит placeholders, например
`UPDATE ... SET name=? WHERE id=?`. JDBC получает значения отдельно и в таком
порядке: `"O'Reilly & <admin>"`, затем `10`. Связанные строки сохраняются в
исходном виде и не проходят через legacy HTML-кодирование.

Правила prepared-режима:

- в `setWhere(...)` нужно ставить `?` без SQL-кавычек, а значения передавать
  следующими аргументами;
- для raw SQL используется `setQuery("SELECT ... WHERE id = ?", id)`;
- значения `addData(...)` для insert и update связываются автоматически;
- `setNoStringEncode(...)` не влияет на связанные значения;
- legacy-префикс raw-значения `[~` становится обычным текстом параметра;
- имена таблиц и полей, сортировка, группировка и иная структура SQL не могут
  передаваться через `?` и не должны формироваться из недоверенных данных;
- для prepared multi-row insert режим должен быть включён у каждой строки.

HTML-кодирование сохранено в режиме по умолчанию для совместимости, но оно не
заменяет JDBC-привязку параметров. Legacy `%s` допускается только внутри
одинарных SQL-кавычек; `%d`, `%f` и `%b` проверяют ожидаемый runtime-тип.
Неизвестные placeholder и произвольные объекты отклоняются.

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
        .setQuery(SqlExpression.trusted("SELECT 1"))
        .setNoRebuildQuery(true);
```

Отключает rebuild query pipeline. Использовать только если запрос уже готов
к выполнению и не должен проходить через преобразование библиотеки.

#### `setForceRebuildQuery(true)`

```java
Arguments.builder()
        .setQuery(SqlExpression.trusted("SELECT * FROM {users}"))
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
