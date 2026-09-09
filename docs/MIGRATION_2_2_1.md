# Drive: переход с 2.2.0 на 2.2.1

Maven: `org.agty:org-agty-sql:2.2.1`. Patch исправляет потерю типов в
entity INSERT/UPDATE, включая `saveEntityWithCheck()` и чтение результата.
`Boolean` и `BigDecimal` теперь включаются в payload сущности.
В prepared-режиме `Arguments.addData(String, Object)` также принимает
`LocalDateTime`, `LocalDate`, `LocalTime` и `java.util.Date` (включая JDBC-типы).
Entity API включает prepared-режим автоматически.
SQL NULL сбрасывает начальное значение ссылочного поля при чтении строки;
`@Column(skipIfNull = true)` по-прежнему исключает поле из записи.

## Поля сущностей

Для PostgreSQL `timestamp without time zone` используйте `LocalDateTime`,
например `public LocalDateTime updated_at;`. В вызывающем проекте измените
также getters/setters, конструкторы и присваивания: `LocalDateTime.now()` или
явный `LocalDateTime.parse(input, formatter)` на границе DTO/сервиса.
Формат входной строки выбирает приложение. `LocalDateTime` не содержит зоны.
Для денежных значений используйте `BigDecimal`, для nullable флагов — `Boolean`.

Строка остаётся строкой и связывается через `setString`, даже если похожа на
дату. PostgreSQL вправе отклонить её для timestamp; другие СУБД могут выполнить
собственное неявное преобразование. Библиотека не гарантирует переносимость
таких преобразований. Если String API необходимо сохранить, явно преобразуйте
значение в DTO перед сохранением либо используйте собственный SQL с явным CAST.

## SQLite

`LocalDateTime` и `Timestamp` записываются как TEXT
`yyyy-MM-dd HH:mm:ss.SSSSSSSSS`; `LocalDate`/`java.sql.Date` — `yyyy-MM-dd`,
`LocalTime`/`java.sql.Time` — `HH:mm:ss.SSSSSSSSS`.
`java.util.Date` преобразуется в локальное время JVM и тот же datetime TEXT.
JDBC-параметры WHERE используют тот же формат. Равенство и диапазоны при
одинаковом формате и BINARY collation сравниваются хронологически; дробные
секунды LocalDateTime сохраняются при чтении без промежуточного java.util.Date.

Существующие строки с `T`, другой длиной дробной части или числовым Unix time
нужно явно привести к единому формату перед сравнением с новыми параметрами.
Автоматической миграции данных нет. `CURRENT_TIMESTAMP` SQLite возвращает
строку без дробной части: её также нужно нормализовать для точного равенства.
SQLite date/time SQL-функции имеют собственную точность; для точных сравнений
используйте нормализованные значения непосредственно. См.
[типы SQLite](https://www.sqlite.org/datatype3.html) и
[date/time functions](https://www.sqlite.org/lang_datefunc.html).
SQLite NUMERIC affinity не гарантирует произвольную десятичную точность;
для финансовых данных со строгой точностью выбирайте целые минимальные единицы
или согласованное приложением текстовое представление.

## SQL Server и пул

Безопасное значение `AgtySqlConfig.trustServerCertificate` остаётся `false`.
Пул использует конфигурацию библиотеки: свойство из отдельного JDBC-подключения
Drive в неё автоматически не переносится. Если среда Drive уже требует
доверия самоподписанному сертификату, до создания пула явно задайте:

```java
config.setTrustServerCertificate(true);
```

Либо в INI для именованного подключения `mssql`:

```ini
db.mssql.trustServerCertificate = true
```

Для другого имени подключения замените `mssql` на имя профиля.
`true` отключает проверку сертификата; для production с доверенной цепочкой
оставляйте `false`. Это поведение
[Microsoft JDBC](https://learn.microsoft.com/en-us/sql/connect/jdbc/reference/gettrustservercertificate-method-sqlserverdatasource).
Настройка относится к SQL Server, не исправляет PostgreSQL timestamp binding.

## Проверка patch-кандидата (2026-09-09, Java 21)

На реальных JDBC-подключениях успешно выполнен `EntityTypedSaveIntegrationTest`:

| СУБД | INSERT, UPDATE, saveEntityWithCheck, fetch, NULL |
|---|---|
| PostgreSQL | пройдено |
| MySQL | пройдено |
| MariaDB | пройдено |
| SQLite | пройдено, включая TEXT-представление, равенство и диапазоны дат |
| SQL Server | пройдено |
| H2 | пройдено |

Проверены LocalDateTime с микросекундами, LocalDate, LocalTime, Boolean,
BigDecimal, Double и null, в том числе сброс начального значения поля.
Недоступных СУБД среди запрошенных нет. Полный набор: 266 тестов,
0 failures, 0 errors, 0 skipped. `clean verify` включает SpotBugs, PMD и
проверку порогов покрытия. Артефакты подготовлены локально; публикация
в Maven/GitHub Packages — отдельный шаг выпуска.
Проверка `package/check-api-compatibility.sh` также прошла: japicmp относительно
2.0.4 и запуск клиента, скомпилированного с 2.0.4, на JAR 2.2.1.
