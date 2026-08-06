# 10. Transactions

### Basic template

```java
try {
    sql.beginTransaction();

    sql.insert(
            Arguments.builder()
                    .setTable("{users}")
                    .addData("name", "Alex")
    );

    sql.update(
            Arguments.builder()
                    .setTable("{accounts}")
                    .addData("balance", 100)
                    .setWhere("[id] = %d", 1)
    );

    sql.commit();
} catch (Exception e) {
    sql.rollback();
    throw e;
} finally {
    sql.setAutoCommit(true);
}
```

Supported transaction-control methods:

- `beginTransaction()`
- `isAutoCommit()`
- `setAutoCommit(...)`
- `commit()`
- `rollback()`

### Practical rules

- if you disable auto-commit manually, restore it to the expected state;
- if you build read-after-write scenarios, keep them inside one transaction
  where the driver semantics require it;
- low-level `PreparedStatement` and `ResultSet` objects inside a transaction
  still have to be closed manually.
