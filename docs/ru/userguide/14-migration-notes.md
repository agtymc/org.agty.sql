# 14. Заметки по миграции

### Пакет

Актуальный пакет библиотеки:

```java
org.agty.sql
```

### Конфигурация

- основной конфиг расположен в корне проекта: `config.ini`;
- `config.ini-sample` используется как шаблон актуального конфига.

### Переименования методов

Таблица переименований ведется отдельно:

- `docs/MIGRATION_AGTYSQL_METHOD_RENAMES.md`

На момент обновления этого мануала зафиксированных переименований методов
`AgtySQL` уже недостаточно, чтобы описывать текущее состояние: актуальная
таблица нормализации alias/legacy API ведется в migration-документе.

### Legacy API

В библиотеке еще присутствуют legacy-элементы, которые сохраняются ради
совместимости:

- `list(arguments, index)` cursor-like API;
- alias-методы вроде `del(...)`, `rows(...)`, `findAll(...)`;
- `lastInsertId(...)` как отдельный helper;
- `statementExecute*`, `executeResultSet(...)`, `executeQuery(...)`;
- `getByField(...)` short helper;

Для нового кода предпочтительно:

- `delete(...)` вместо `del(...)`;
- `countRows(...)` вместо `rows(...)`;
- `listArray(...)` или `openCursor(...)` вместо старых list-паттернов.
- `insert(... setReturnLastInsertId(true))` вместо прямого `lastInsertId(...)`;
- `execute(...)` / `executeUpdate(...)` или JDBC low-level API вместо старых
  statement/result-set helper methods;
- `fetch(Arguments)` вместо `getByField(...)`.

На дату `2026-08-03` эти legacy/alias методы уже считаются deprecated на
уровне публичного API.

Policy:

- deprecated helper/alias API сохраняется на весь lifecycle ветки `2.x`;
- внутри `2.x` он рассматривается как compatibility-layer;
- удаление этих методов переносится только на следующий major-релиз;
- новый код и новая документация должны использовать целевые имена API.
