# 10. Transactions

### Базовый шаблон

```java
try {
    sql.beginTransaction();

    sql.insert(
            Arguments.builder()
                    .setTable("{users}")
                    .setData("name", "Alex")
    );

    sql.update(
            Arguments.builder()
                    .setTable("{accounts}")
                    .setData("balance", 100)
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

Поддерживаемые методы transaction control:

- `beginTransaction()`
- `isAutoCommit()`
- `setAutoCommit(...)`
- `commit()`
- `rollback()`

### Практические правила

- если вручную отключили auto-commit, верните его в ожидаемое состояние;
- если строите read-after-write сценарий, держите его в одной транзакции там,
  где это требуется логикой драйвера;
- low-level `PreparedStatement` и `ResultSet` внутри транзакции все равно
  нужно закрывать вручную.
