# org.agty.sql

`org.agty.sql` — это легковесная Java-библиотека для работы с SQL, с простым
high-level API, низкоуровневым JDBC-доступом, курсорным чтением и удобным
entity/model-слоем.

Проект собирается через Maven и сейчас ориентирован на Java 18.
Исходный репозиторий: `https://github.com/agtymc/org.agty.sql`

## Поддерживаемые Драйверы

Актуальная поддержка SQL-драйверов в `2.0.4`:

- MySQL
- MariaDB
- PostgreSQL
- MSSQL (SQL Server)
- SQLite
- H2

Замечания по драйверам:

- MySQL, MariaDB, SQLite и H2 используют documented follow-up fetch стратегии
  для части write-return сценариев.
- PostgreSQL и MSSQL поддерживают native row-return для
  `insertAndGet()` / `updateAndGet()`.
- SQLite и H2 подходят как file-based варианты для локальной разработки.
- Для локального MSSQL теперь есть `install/install-mssql.sh`.

## Документация

- Английское руководство: `docs/en/USER_GUIDE.md`
- Русское руководство: `docs/ru/USER_GUIDE.md`
- Session и JDBC API:
  - `docs/en/AGTYSQL_SESSION_API.md`
  - `docs/ru/AGTYSQL_SESSION_API.md`

## Подключение Через Maven Из GitHub-Репозитория

Проект настроен на публикацию в GitHub Packages:

- Репозиторий: `https://github.com/agtymc/org.agty.sql`
- Maven registry: `https://maven.pkg.github.com/agtymc/org.agty.sql`
- Maven server id: `org.agty.sql`

Добавьте Maven-репозиторий:

```xml
<repositories>
    <repository>
        <id>org.agty.sql</id>
        <url>https://maven.pkg.github.com/agtymc/org.agty.sql</url>
    </repository>
</repositories>
```

Затем добавьте зависимость:

```xml
<dependencies>
    <dependency>
        <groupId>org.agty</groupId>
        <artifactId>org-agty-sql</artifactId>
        <version>2.0.4</version>
    </dependency>
</dependencies>
```

Для GitHub Packages обычно нужна аутентификация. В `~/.m2/settings.xml`:

```xml
<settings>
    <servers>
        <server>
            <id>org.agty.sql</id>
            <username>YOUR_GITHUB_LOGIN</username>
            <password>YOUR_GITHUB_TOKEN</password>
        </server>
    </servers>
</settings>
```

## Подключение Через JAR-Файл

Когда JAR будет упакован, его можно подключить одним из двух способов.

1. Установить в локальный Maven-репозиторий:

```bash
mvn install:install-file \
  -Dfile=path/to/org-agty-sql-2.0.4.jar \
  -DgroupId=org.agty \
  -DartifactId=org-agty-sql \
  -Dversion=2.0.4 \
  -Dpackaging=jar
```

После этого использовать обычную Maven-зависимость:

```xml
<dependency>
    <groupId>org.agty</groupId>
    <artifactId>org-agty-sql</artifactId>
    <version>2.0.4</version>
</dependency>
```

2. Хранить JAR рядом с приложением и подключать его вручную в classpath, если
сборочный процесс не использует установку артефакта в Maven.

## Лицензия

Apache License 2.0. См. `LICENSE`.
