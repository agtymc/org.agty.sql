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

Plain String arguments are validated as SQL identifiers. Simple names starting
with a letter or `_` and continuing with letters, digits, or `_`, qualified
names separated by `.`, and the documented `{table}` / `[column]` forms are
accepted. Quotes, comments, operators, and `;` produce
`IllegalArgumentException`.

It is recommended to use the same table-name form already adopted in the
project, for example `"{users}"`.

### WHERE

```java
Arguments.builder()
        .setTable("{users}")
        .setWhere("[id] = %d", 10);
```

There are two variants:

- `setWhere(String pattern, Object... args)`
- `setWhere(SqlExpression.trusted(...))` for a static raw expression

You can also build the condition incrementally:

```java
Arguments.builder()
        .setTable("{users}")
        .setWhere(SqlExpression.trusted("[active] = 1"))
        .appendWhere(SqlExpression.trusted(" AND [role] = 'admin'"));
```

The non-empty raw String overloads `setWhere(String)` and
`appendWhere(String)` are deprecated and rejected. They must not receive
request data.

### ORDER BY, GROUP BY, HAVING

```java
Arguments.builder()
        .setTable("{orders}")
        .setFields(SqlExpression.trusted("user_id, COUNT(*) AS total"))
        .setGroupBy("user_id")
        .setHaving(SqlExpression.trusted("COUNT(*) > 3"))
        .setOrderBy("total DESC");
```

Supported methods:

- `setOrderBy(...)`
- `setGroupBy(...)`
- `setHaving(...)`

The String variants of `setFields`, `setGroupBy`, and `setOrderBy` accept only
validated identifiers and simple lists/sort directions. Functions, aliases,
and other full SQL expressions require an explicit
`SqlExpression.trusted(...)` value.

`SqlExpression.trusted(...)` does not escape or validate SQL. It is an explicit
trust boundary for application-authored static SQL or an application-owned
allowlist result. Never create it from HTTP/request parameters.

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
        .addDataString("name", "Alex")
        .addDataInt("age", 30)
        .addDataBoolean("active", true);
```

Named methods validate the actual runtime type. This matters when a value is
returned as `Object`:

```java
Object value = post.getData();

Arguments.builder()
        .addDataString("name", value); // String or null only.
```

If `value` contains an `Integer`, DTO, collection, or another mismatched type,
`addDataString(...)` immediately throws `IllegalArgumentException` before SQL
is built or executed.

Available methods:

- `addDataString(...)` for `String`;
- `addDataInt(...)` / `addDataInteger(...)` for `Integer`;
- `addDataLong(...)`, `addDataShort(...)`, and `addDataByte(...)`;
- `addDataBoolean(...)` / `addDataBool(...)`;
- `addDataFloat(...)` and `addDataDouble(...)`;
- `addDataChar(...)` / `addDataCharacter(...)`;
- `addDataDecimal(...)` / `addDataNumber(...)` for any `Number`;
- `addDataBigDecimal(...)` and `addDataBigInteger(...)` for strict matching of
  those types.
- `addDataNull(...)` for an explicit SQL `NULL` without overload ambiguity.

`addDataDecimal(...)` accepts `Byte`, `Short`, `Integer`, `Long`, `Float`,
`Double`, `BigInteger`, `BigDecimal`, and other valid `Number`
implementations. A non-standard `Number` is normalized to `BigDecimal`. `NaN`
and infinite values are rejected.

Compatibility `addData(...)` overloads remain available. The new
`addData(String, Object)` overload validates this common whitelist:

- `String`
- `Number`
- `Boolean`
- `Character`

Every other runtime type produces `IllegalArgumentException`; unknown objects
are no longer silently converted through `toString()`.

The field name is required by every `addData...` method: `null` produces an
`IllegalArgumentException` before data is stored or SQL is built.

Field order is preserved because the internal structure is `LinkedHashMap`.

### Raw query

```java
Arguments.builder()
        .setQuery(SqlExpression.trusted(
                "SELECT id, name FROM {users} WHERE [active] = 1 ORDER BY id ASC"
        ));
```

If `query` is set, it has priority over composite parameters such as `table`,
`fields`, and `where`.

A raw query without parameters requires `SqlExpression.trusted(...)`.
`setQuery(String)` is deprecated and rejects non-empty SQL. Use prepared
`setQuery("... WHERE id = ?", id)` for values.

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

#### `useStatementPrepare(true)`

Prepared mode is opt-in. By default it is disabled and the library preserves
the legacy SQL rendering and HTML encoding behavior for `addData(...)` string
values.

```java
boolean updated = sql.update(
        Arguments.builder()
                .useStatementPrepare(true)
                .setTable("{users}")
                .addDataString("name", "O'Reilly & <admin>")
                .setWhere("[id] = ?", 10)
);
```

The generated update contains placeholders, for example
`UPDATE ... SET name=? WHERE id=?`. JDBC receives the values separately in
this order: `"O'Reilly & <admin>"`, then `10`. Bound strings are stored in their
original form and do not pass through the legacy HTML encoder.

Rules for prepared mode:

- use unquoted `?` placeholders in `setWhere(...)` and pass their values as the
  following arguments;
- for raw SQL, use `setQuery("SELECT ... WHERE id = ?", id)`;
- `addData(...)` values are bound automatically for insert and update;
- `setNoStringEncode(...)` does not affect bound values;
- the legacy `[~` raw-value prefix is treated as ordinary bound text;
- table names, field names, sorting, grouping, and other SQL structure cannot
  be bound through `?` and must never come directly from untrusted input;
- every row in a prepared multi-row insert must enable prepared mode.

HTML encoding is retained for compatibility in the default mode, but it is not
a replacement for JDBC parameter binding. Legacy `%s` is accepted only inside
single SQL quotes; `%d`, `%f`, and `%b` validate their expected runtime type.
Unknown placeholders and arbitrary objects are rejected.

#### `setReturnLastInsertId(true)`

```java
long id = sql.insert(
        Arguments.builder()
                .setTable("{users}")
                .addData("name", "Alex")
                .setReturnLastInsertId(true)
);
```

Use this only where the driver officially supports returning the last inserted
ID. If the driver does not support the scenario, the library should fail fast.

#### `setNoRebuildQuery(true)`

```java
Arguments.builder()
        .setQuery(SqlExpression.trusted("SELECT 1"))
        .setNoRebuildQuery(true);
```

Disables the rebuild query pipeline. Use only when the query is already ready
to execute and must not pass through library transformation.

#### `setForceRebuildQuery(true)`

```java
Arguments.builder()
        .setQuery(SqlExpression.trusted("SELECT * FROM {users}"))
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
        .addData("name", "Alex")
        .addData("age", 30)
        .setWhere("[id] = %d", 10);
```

#### Aggregate/metadata scenario

```java
Arguments.builder()
        .setTable("{orders}")
        .setActionField("id");
```
