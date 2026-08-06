# 07. Слой Entity и Model

В `AgtySQL` поддерживается model/entity convenience API:

- `fetch(..., T object)`
- `fetch(..., Class<?>)`
- `insertEntity(...)`
- `insertEntityWithCheck(...)`
- `updateEntity(...)`
- `saveEntity(...)`
- `saveEntityWithCheck(...)`
- `saveEntityOrSkip(...)`

Этот слой остается поддерживаемой частью публичного API, потому что важны
сценарии сохранения сущности с возвратом сущности и смежные model-oriented
операции.

По смыслу это не JPA-слой и не совместимость с JPA-протоколом. Это собственный
entity-oriented convenience layer библиотеки поверх `AgtySQL`.

При этом нужно учитывать:

- внутри этот слой опирается на capability-driven `insertAndGet(...)` и
  `updateAndGet(...)`;
- cross-db ограничения write-return сценариев распространяются и на entity API;
- для сложных и критичных по предсказуемости сценариев явный CRUD через
  `Arguments.builder()` остается самым прозрачным вариантом.

### Когда использовать entity API

- когда приложение строится вокруг entity-классов;
- когда важно сохранить сущность и сразу получить обновленную сущность назад;
- когда нужен короткий high-level flow без ручной сборки `Arguments`.

### Типовые entity-сценарии

Если имя поля entity совпадает с именем колонки, `@Column(name = "...")`
не обязателен. Если `name` не указан, используется имя поля.

```java
MyUser user = sql.fetch(
        Arguments.builder()
                .setTable("{users}")
                .setWhere("[id] = %d", 1),
        MyUser.class
);
```

```java
@Entity
class MyUser {
    Long id;
    String name;
}
```

```java
MyUser saved = sql.saveEntity(user);
```

```java
Object id = sql.saveEntity(user, "id");
```

```java
MyUser inserted = sql.insertAndGet(
        Arguments.builder()
                .setTable("{users}")
                .setData("name", "Alex"),
        MyUser.class
);
```

```java
MyUser updated = sql.updateAndGet(
        Arguments.builder()
                .setTable("{users}")
                .setData("name", "Alex Updated")
                .setWhere("[id] = %d", 10),
        MyUser.class
);
```

```java
MyUser insertedByQuery = sql.insertAndGet(
        "INSERT INTO {users} (name) VALUES ('Alex')",
        MyUser.class
);
```

```java
MyUser insertedShort = sql.insert(
        Arguments.builder()
                .setTable("{users}")
                .setData("name", "Alex"),
        MyUser.class
);
```

```java
MyUser updatedShort = sql.update(
        Arguments.builder()
                .setTable("{users}")
                .setData("name", "Alex Updated")
                .setWhere("[id] = %d", 10),
        MyUser.class
);
```

### Когда использовать explicit CRUD API

- когда нужен полный контроль над SQL и transaction boundaries;
- когда важно явно видеть capability-aware write/read flow;
- когда логика должна быть одинаково прозрачной для всех драйверов.

### Направление развития entity API

Уже поддерживается короткий entity-return flow поверх обычного write API:

```java
MyUser user = sql.insertAndGet(arguments, MyUser.class);
```

Также поддерживаются короткие формы поверх этого же flow:

```java
MyUser inserted = sql.insert(arguments, MyUser.class);
MyUser updated = sql.update(arguments, MyUser.class);
```

На дату `2026-08-04` этого набора достаточно для `2.x`:

- query-based entity-return overloads ограничиваются query-driven write API
  (`insertAndGet(...)`, `updateAndGet(...)` и short forms поверх них);
- для `insertEntity*`, `updateEntity`, `saveEntity*` дополнительные
  query-based overloads не планируются, потому что это object-driven API.
