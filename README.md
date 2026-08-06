# org.agty.sql

`org.agty.sql` is a lightweight Java SQL library focused on a simple high-level
API, low-level JDBC access, cursor-style reading, and convenience support for
entity/model mapping.

The project is built with Maven and currently targets Java 18.

## Documentation

- English guide: `docs/en/USER_GUIDE.md`
- Russian guide: `docs/ru/USER_GUIDE.md`
- Session and JDBC API:
  - `docs/en/AGTYSQL_SESSION_API.md`
  - `docs/ru/AGTYSQL_SESSION_API.md`

## Maven Dependency From GitHub Repository

Replace the repository URL below with the GitHub Maven repository URL when it
is available.

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

## Using A Local JAR

When the JAR is packaged, add it to your project in one of these ways:

1. Install it into the local Maven repository:

```bash
mvn install:install-file \
  -Dfile=path/to/org-agty-sql-2.0.0.jar \
  -DgroupId=org.agty \
  -DartifactId=org-agty-sql \
  -Dversion=2.0.0 \
  -Dpackaging=jar
```

Then use the regular Maven dependency:

```xml
<dependency>
    <groupId>org.agty</groupId>
    <artifactId>org-agty-sql</artifactId>
    <version>2.0.0</version>
</dependency>
```

2. Keep the JAR in your application and add it manually to the classpath if
your build flow does not use Maven artifact installation.
