# 13. Рекомендуемые шаблоны использования

### Для типового приложения

- использовать high-level CRUD;
- использовать `Arguments.builder()` для сборки запроса;
- использовать `listArray()` только если набор данных разумно помещается
  в память;
- закрывать `AgtySQL` в конце scope.

### Для больших выборок

- использовать `openCursor(...)` или прямой JDBC `ResultSet`;
- настраивать `fetchSize` под сценарий;
- не загружать большой результат через `listArray()`, если нужен streaming.

### Для нестандартного SQL

- использовать `prepareStatement(...)` или `execute(...)`;
- отключать rebuild только для уже готового SQL;
- generated keys читать через `getGeneratedKeys(...)`, если нужен JDBC-путь.

### Для кросс-СУБД кода

- избегать неявных предположений о `RETURNING`;
- считать `insertAndGet()` и `updateAndGet()` capability-driven API;
- фиксировать transaction boundaries явно.
