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

## Безопасная production-конфигурация

- Один изменяемый экземпляр `AgtySQL`, `Arguments` или `AgtySqlCursor` должен
  использоваться только в рамках одного запроса/транзакции и одного потока.
  Пулы можно разделять между потоками, заимствованные handles нельзя.
- Оставляйте `throwException=true`, чтобы ошибка соединения или выполнения не
  выглядела как пустой результат SQL-запроса.
- Настраивайте отдельно `loginTimeoutSeconds` и `networkTimeoutMillis`.
- Оставляйте `logQueryValues=false`. Тогда в query log редактируются строковые,
  числовые и dollar-quoted литералы; присваивания credentials редактируются
  всегда.
- Используйте `${ENVIRONMENT_VARIABLE}` как полное значение секрета в ini либо
  создавайте `AgtySqlConfig` из результата secret manager. Локальный
  `config.ini` ограничьте правами владельца: `chmod 600 config.ini`.
