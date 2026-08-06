# 04. Arguments Builder

`Arguments` is the main parameter container for high-level operations.

Basic form:

```java
Arguments arguments = Arguments.builder()
        .setTable("{users}")
        .setFields("id, name")
        .setWhere("[active] = %d", 1)
        .setOrderBy("id ASC")
        .setLimit(10);
```

### Table and fields

```java
Arguments.builder()
        .setTable("{users}")
        .setFields("id, name, email");
```

- `setTable(...)` sets the table.
- `setFields(...)` sets the field list for select-like operations.
- if `fields` is not set, `*` is used.

It is recommended to use the same table-name form already adopted in the
project, for example `"{users}"`.

### WHERE

```java
Arguments.builder()
        .setTable("{users}")
        .setWhere("[id] = %d", 10);
```

There are two variants:

- `setWhere(String where)`
- `setWhere(String pattern, Object... args)`

You can also build the condition incrementally:

```java
Arguments.builder()
        .setTable("{users}")
        .setWhere("[active] = 1")
        .appendWhere(" AND [role] = 'admin'");
```

`appendWhere(...)` is useful only when you intentionally assemble the
expression piece by piece.

### ORDER BY, GROUP BY, HAVING

```java
Arguments.builder()
        .setTable("{orders}")
        .setFields("user_id, COUNT(*) as total")
        .setGroupBy("user_id")
        .setHaving("COUNT(*) > 3")
        .setOrderBy("total DESC");
```

Supported methods:

- `setOrderBy(...)`
- `setGroupBy(...)`
- `setHaving(...)`

### LIMIT and OFFSET

```java
Arguments.builder()
        .setTable("{users}")
        .setLimit(20)
        .setOffset(40);
```

Or as a string:

```java
Arguments.builder()
        .setTable("{users}")
        .setLimit("40,20");
```

This means: skip `40` rows and return `20`.

### Data for insert/update

```java
Arguments.builder()
        .setTable("{users}")
        .setData("name", "Alex")
        .setData("age", 30)
        .setData("active", true);
```

Supported scalar types:

- `String`
- `Integer`
- `Long`
- `Short`
- `Boolean`
- `Float`
- `Double`
- `Character`

Field order is preserved because the internal structure is `LinkedHashMap`.

### Raw query

```java
Arguments.builder()
        .setQuery("SELECT id, name FROM {users} WHERE [active] = 1 ORDER BY id ASC");
```

If `query` is set, it has priority over composite parameters such as `table`,
`fields`, and `where`.

Use this mode when:

- the high-level builder is not enough;
- SQL is easier to express by hand;
- you want to stay inside `AgtySQL` without building the query from parts.

### Action field and primary key

```java
Arguments.builder()
        .setTable("{users}")
        .setActionField("id");
```

`actionField` is used in several metadata/fetch scenarios, for example:

- `getFirstRow(...)`
- `getLastRow(...)`
- `min(...)`
- `max(...)`

`primaryKey` is available as part of the argument model, but in typical CRUD
code `where` is often enough.

### Columns

```java
Arguments.builder()
        .addColumn("name")
        .addColumn("email");
```

`columns` is a separate collection inside `Arguments`. It is not needed for the
basic CRUD flow but for narrower internal and metadata patterns. If you do not
have an explicit need, you can ignore it in application code.

### Behavioral flags

#### `setReturnLastInsertId(true)`

```java
long id = sql.insert(
        Arguments.builder()
                .setTable("{users}")
                .setData("name", "Alex")
                .setReturnLastInsertId(true)
);
```

Use this only where the driver officially supports returning the last inserted
ID. If the driver does not support the scenario, the library should fail fast.

#### `setNoRebuildQuery(true)`

```java
Arguments.builder()
        .setQuery("SELECT 1")
        .setNoRebuildQuery(true);
```

Disables the rebuild query pipeline. Use only when the query is already ready
to execute and must not pass through library transformation.

#### `setForceRebuildQuery(true)`

```java
Arguments.builder()
        .setQuery("SELECT * FROM {users}")
        .setForceRebuildQuery(true);
```

Forces rebuild where it is explicitly required.

#### `setNoStringEncode(true)`

Disables string encoding for values. This is a high-caution mode and should be
used only if you clearly understand how the driver will handle the value next.

#### `convertValueToString(true)`

```java
Arguments.builder()
        .setTable("{users}")
        .convertValueToString(true);
```

Forces the library to return values as strings in fetch/list-like scenarios.

#### `setEmulateMode(true)`

Emulation mode. Queries are not sent to the database. This is a specialized
flag that is usually unnecessary for normal application code.

### Practical `Arguments` templates

#### Fetch by ID

```java
Arguments.builder()
        .setTable("{users}")
        .setWhere("[id] = %d", 10);
```

#### List with sorting and limit

```java
Arguments.builder()
        .setTable("{users}")
        .setFields("id, name")
        .setWhere("[active] = %d", 1)
        .setOrderBy("id ASC")
        .setLimit(100);
```

#### Update by condition

```java
Arguments.builder()
        .setTable("{users}")
        .setData("name", "Alex")
        .setData("age", 30)
        .setWhere("[id] = %d", 10);
```

#### Aggregate/metadata scenario

```java
Arguments.builder()
        .setTable("{orders}")
        .setActionField("id");
```
