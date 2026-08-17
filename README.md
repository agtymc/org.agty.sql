# org.agty.sql

`org.agty.sql` is a lightweight Java SQL library focused on a simple high-level
API, low-level JDBC access, cursor-style reading, and convenience support for
entity/model mapping.

The project is built with Maven and currently targets Java 18.
Source repository: `https://github.com/agtymc/org.agty.sql`

## Supported Drivers

Current SQL driver support in `2.0.3`:

- MySQL
- MariaDB
- PostgreSQL
- MSSQL (SQL Server)
- SQLite
- H2

Driver notes:

- MySQL, MariaDB, SQLite, and H2 use documented follow-up fetch strategies for
  some write-return flows.
- PostgreSQL and MSSQL provide native row-return support for
  `insertAndGet()` / `updateAndGet()`.
- SQLite and H2 are file-based development-friendly options.
- MSSQL local development can be started with `install/install-mssql.sh`.

## Documentation

- English guide: `docs/en/USER_GUIDE.md`
- Russian guide: `docs/ru/USER_GUIDE.md`
- Session and JDBC API:
  - `docs/en/AGTYSQL_SESSION_API.md`
  - `docs/ru/AGTYSQL_SESSION_API.md`

## Maven Dependency From GitHub Repository

This project is configured for GitHub Packages:

- Repository: `https://github.com/agtymc/org.agty.sql`
- Maven registry: `https://maven.pkg.github.com/agtymc/org.agty.sql`
- Maven server id: `org.agty.sql`

Add the GitHub Packages repository:

```xml
<repositories>
    <repository>
        <id>org.agty.sql</id>
        <url>https://maven.pkg.github.com/agtymc/org.agty.sql</url>
    </repository>
</repositories>
```

Then add the dependency:

```xml
<dependencies>
    <dependency>
        <groupId>org.agty</groupId>
        <artifactId>org-agty-sql</artifactId>
        <version>2.0.3</version>
    </dependency>
</dependencies>
```

Authentication for GitHub Packages is usually required. In `~/.m2/settings.xml`:

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

## Using A Local JAR

When the JAR is packaged, add it to your project in one of these ways:

1. Install it into the local Maven repository:

```bash
mvn install:install-file \
  -Dfile=path/to/org-agty-sql-2.0.3.jar \
  -DgroupId=org.agty \
  -DartifactId=org-agty-sql \
  -Dversion=2.0.3 \
  -Dpackaging=jar
```

Then use the regular Maven dependency:

```xml
<dependency>
    <groupId>org.agty</groupId>
    <artifactId>org-agty-sql</artifactId>
    <version>2.0.3</version>
</dependency>
```

2. Keep the JAR in your application and add it manually to the classpath if
your build flow does not use Maven artifact installation.

## License

Apache License 2.0. See `LICENSE`.
