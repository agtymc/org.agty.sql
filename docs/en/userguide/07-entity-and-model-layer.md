# 07. Entity And Model Layer

`AgtySQL` supports a model/entity convenience API:

- `fetch(..., T object)`
- `fetch(..., Class<?>)`
- `insertEntity(...)`
- `insertEntityWithCheck(...)`
- `updateEntity(...)`
- `saveEntity(...)`
- `saveEntityWithCheck(...)`
- `saveEntityOrSkip(...)`

This layer remains a supported part of the public API because
entity-save/entity-return scenarios are important.

Semantically this is not a JPA layer and not JPA compatibility. It is the
library's own entity-oriented convenience layer on top of `AgtySQL`.

Keep in mind:

- internally this layer depends on capability-driven `insertAndGet(...)` and
  `updateAndGet(...)`;
- cross-driver limitations of write-return flows also apply to the entity API;
- for complex and critical cross-db scenarios, explicit CRUD through
  `Arguments.builder()` remains the most transparent option.

### When to use the entity API

- when the application is built around entity classes;
- when it is important to save an entity and immediately get the updated entity
  back;
- when you want a short high-level flow without building `Arguments` manually.

### Typical entity scenarios

If an entity field name matches the column name, `@Column(name = "...")` is not
required. If `name` is not set, the field name is used.

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
                .addData("name", "Alex"),
        MyUser.class
);
```

```java
MyUser updated = sql.updateAndGet(
        Arguments.builder()
                .setTable("{users}")
                .addData("name", "Alex Updated")
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
                .addData("name", "Alex"),
        MyUser.class
);
```

```java
MyUser updatedShort = sql.update(
        Arguments.builder()
                .setTable("{users}")
                .addData("name", "Alex Updated")
                .setWhere("[id] = %d", 10),
        MyUser.class
);
```

### When to use explicit CRUD API

- when you need full control over SQL and transaction boundaries;
- when it is important to see the capability-aware write/read flow explicitly;
- when the logic must stay equally transparent for all drivers.

### Direction of the entity API

A short entity-return flow on top of the regular write API is already supported:

```java
MyUser user = sql.insertAndGet(arguments, MyUser.class);
```

Short forms on top of the same flow are also supported:

```java
MyUser inserted = sql.insert(arguments, MyUser.class);
MyUser updated = sql.update(arguments, MyUser.class);
```

As of `2026-08-04`, this set is enough for `2.x`:

- query-based entity-return overloads are limited to query-driven write API
  (`insertAndGet(...)`, `updateAndGet(...)`, and short forms on top of them);
- extra query-based overloads for `insertEntity*`, `updateEntity`,
  `saveEntity*` are not planned because that is object-driven API, not
  query-driven API.
