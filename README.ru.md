# org.agty.sql

`org.agty.sql` — это легковесная Java-библиотека для работы с SQL, с простым
high-level API, низкоуровневым JDBC-доступом, курсорным чтением и удобным
entity/model-слоем.

Проект собирается через Maven и сейчас ориентирован на Java 18.

## Документация

- Английское руководство: `docs/en/USER_GUIDE.md`
- Русское руководство: `docs/ru/USER_GUIDE.md`
- Session и JDBC API:
  - `docs/en/AGTYSQL_SESSION_API.md`
  - `docs/ru/AGTYSQL_SESSION_API.md`

## Подключение Через Maven Из GitHub-Репозитория

Когда появится ссылка на GitHub Maven repository, подставьте ее вместо
заглушки ниже.

```xml
<repositories>
    <repository>
        <id>github-org-agty-sql</id>
        <url>GITHUB_MAVEN_REPOSITORY_URL</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>org.agty</groupId>
        <artifactId>org-agty-sql</artifactId>
        <version>2.0.0</version>
    </dependency>
</dependencies>
```

## Подключение Через JAR-Файл

Когда JAR будет упакован, его можно подключить одним из двух способов.

1. Установить в локальный Maven-репозиторий:

```bash
mvn install:install-file \
  -Dfile=path/to/org-agty-sql-2.0.0.jar \
  -DgroupId=org.agty \
  -DartifactId=org-agty-sql \
  -Dversion=2.0.0 \
  -Dpackaging=jar
```

После этого использовать обычную Maven-зависимость:

```xml
<dependency>
    <groupId>org.agty</groupId>
    <artifactId>org-agty-sql</artifactId>
    <version>2.0.0</version>
</dependency>
```

2. Хранить JAR рядом с приложением и подключать его вручную в classpath, если
сборочный процесс не использует установку артефакта в Maven.
