# 11. Владение ресурсами

### Что закрывает библиотека

- high-level методы вроде `fetch()`, `insert()`, `update()`, `delete()`;
- `listArray()` для своих временных JDBC-ресурсов;
- `AgtySqlCursor` на конце выборки;
- все legacy list cursors при `AgtySQL.close()`.

### Что закрывает разработчик

- `AgtySQL` как основной session-объект;
- `Statement`;
- `PreparedStatement`;
- JDBC `ResultSet`;
- `AgtySqlCursor`, если чтение прервано до конца выборки.

Предпочтительный стиль для low-level кода: `try-with-resources`.
