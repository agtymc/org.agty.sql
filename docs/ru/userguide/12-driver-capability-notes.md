# 12. Замечания по возможностям драйверов

Поведение write-return сценариев зависит от `DialectCapabilities`.

На текущем этапе в проекте зафиксировано следующее:

- PostgreSQL: native returning;
- MySQL: follow-up fetch для `insertAndGet()`;
- MariaDB: follow-up fetch для `insertAndGet()`;
- SQLite: follow-up fetch для `insertAndGet()`;
- H2: follow-up fetch для `insertAndGet()` через collision-prone last-id fallback;
- PostgreSQL: `UpdateAndGetStrategy.NATIVE_RETURNING`;
- MySQL: `UpdateAndGetStrategy.FOLLOW_UP_FETCH_BY_WHERE`;
- MariaDB: `UpdateAndGetStrategy.FOLLOW_UP_FETCH_BY_WHERE`;
- SQLite: `UpdateAndGetStrategy.FOLLOW_UP_FETCH_BY_WHERE`;
- H2: `UpdateAndGetStrategy.FOLLOW_UP_FETCH_BY_WHERE`.

Практический смысл:

- `insertAndGet()` и `updateAndGet()` зависят от стратегии диалекта;
- для `updateAndGet()` библиотека теперь различает не только
  support/no-support, а и форму follow-up path;
- `setReturnLastInsertId(true)` тоже зависит от стратегии получения ID;
- часть стратегий являются native/session-safe;
- часть стратегий являются emulated;
- для H2 insert-return сценарий сейчас нужно считать collision-prone при
  конкурентных вставках.
- raw `String query` overloads для `insertAndGet()` / `updateAndGet()` не нужно
  считать одинаково переносимыми на follow-up драйверах: без metadata библиотека
  не может корректно выполнить post-write fetch.

Если поведение read-after-write критично для бизнес-логики, не полагайтесь на
предположение, что все СУБД ведут себя одинаково.

Отдельно по package map:

- public capability API для `2.x` остается в `org.agty.sql.driver`;
- concrete internal dialect implementations уже живут в
  `org.agty.sql.dialect.*`;
- internal query/helper packages для concrete dialects тоже постепенно
  переезжают в `org.agty.sql.dialect.*`;
- внутренняя точка выбора dialect implementation теперь может проходить через
  `org.agty.sql.dialect.DialectDriverRegistry`;
- `org.agty.sql.base` не считается целиком legacy-пакетом:
  - `Field`
  - `FieldsType`
  - `RowData`
  - `RowDataEmpty`
  остаются supported model/base API внутри `2.x`;
- это значит, что `SqlRow`-сценарии и field-type mapping classes можно
  продолжать использовать как поддерживаемую часть библиотеки, а не как
  временный compatibility-only слой;
- deprecated helper/concrete classes вне `AgtySQL`, которые не образуют
  public-facing API фасада, удалены из production source set;
- deprecated compatibility в `2.x` на текущем этапе сохранена прежде всего на
  уровне самих методов `AgtySQL`.
