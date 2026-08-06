# 03. Модель использования

`AgtySQL` поддерживает два официальных режима.

### High-level API

Использовать, когда нужен типовой код библиотеки:

- `fetch(...)`
- `listArray(...)`
- `insert(...)`
- `insertAndGet(...)`
- `update(...)`
- `updateAndGet(...)`
- `delete(...)`
- `countRows(...)`

В этом режиме библиотека сама:

- собирает SQL;
- прогоняет rebuild-пайплайн;
- работает с драйвером;
- преобразует результат в `SqlRow`;
- управляет lifecycle временных JDBC-объектов.

### Low-level session/JDBC API

Использовать, когда нужен прямой контроль:

- `getConnection()`
- `getStatement()`
- `prepareStatement(...)`
- `executeBatch(...)`
- `getGeneratedKeys(...)`
- `openCursor(...)`
- `beginTransaction()`
- `setAutoCommit(...)`
- `commit()`
- `rollback()`
- `setFetchSize(...)`

Этот режим нужен для:

- нестандартного SQL;
- ручной работы с `PreparedStatement`;
- batch execution;
- generated keys;
- streaming/cursor сценариев;
- явного transaction control.
