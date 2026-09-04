# 02. Spring Boot DataSource Example

Below is a Spring Boot-style configuration where the library acts as the main
`DataSource` bean. `AgtySqlPooledDataSource` is itself backed by HikariCP, so a
second pool must not wrap it.

```java
import org.agty.sql.config.AgtySqlConfig;
import org.agty.sql.datasource.AgtySqlPooledDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.session.jdbc.SpringSessionDataSource;

import javax.sql.DataSource;

@Configuration
@EnableJdbcHttpSession(
        tableName = "priv_session",
        maxInactiveIntervalInSeconds = 18001
)
public class SessionDataConfiguration {

    @Bean
    @SpringSessionDataSource
    public DataSource dataSource() {
        AgtySqlConfig config = new AgtySqlConfig()
                .setDriver("pgsql")
                .setServer("127.0.0.1")
                .setPort(5432)
                .setDatabase("app_db")
                .setUser("app_user")
                .setPassword("app_password")
                .setSchema("private")
                .setPfx("")
                .setThrowException(true);

        return new AgtySqlPooledDataSource(
                config,
                10,      // maximum number of pooled connections
                5,       // minimum idle connections kept warm
                30000,   // maximum wait for getConnection()
                600000,  // how long an idle connection may remain in the pool
                1800000  // maximum total lifetime of a physical connection
        );
    }
}
```

Notes:

- Spring receives a normal `javax.sql.DataSource`, so the code shape is the
  same as with Hikari;
- `@SpringSessionDataSource` tells Spring Session to use this exact bean for
  the `priv_session` table;
- `AgtySqlPooledDataSource` manages its HikariCP pool, so a separate
  `HikariConfig` is not needed;
- `schema`, `driver`, `server`, `database`, `user`, and `password` remain in
  `AgtySqlConfig` because the library creates the physical JDBC connections
  itself;
- if the bean lives inside a Spring context, its shutdown should be tied to the
  container lifecycle so that the pool is closed cleanly when the application
  stops.
