# 11. Resource Ownership

### What the library closes

- high-level methods such as `fetch()`, `insert()`, `update()`, `delete()`;
- `listArray()` for its temporary JDBC resources;
- `AgtySqlCursor` at the end of the result set;
- all legacy list cursors on `AgtySQL.close()`.

### What the developer closes

- `AgtySQL` as the main session object;
- `Statement`;
- `PreparedStatement`;
- JDBC `ResultSet`;
- `AgtySqlCursor` if reading stops before the end of the result set.

The preferred style for low-level code is `try-with-resources`.
