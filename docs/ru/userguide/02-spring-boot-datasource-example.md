# 02. Пример DataSource для Spring Boot

Ниже пример конфигурации в стиле Spring Boot, где библиотека выступает
как основной `DataSource` bean вместо `HikariDataSource`.

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
                30000,   // max wait for getConnection()
                600000,  // how long an idle connection may stay in the pool
                1800000  // max total lifetime of a physical connection
        );
    }
}
```

Пояснения к примеру:

- Spring получает обычный `javax.sql.DataSource`, поэтому код выше выглядит
  так же, как и с Hikari;
- `@SpringSessionDataSource` говорит Spring Session использовать именно этот
  bean для таблицы `priv_session`;
- `AgtySqlPooledDataSource` сам управляет пулом, поэтому отдельный
  `HikariConfig` больше не нужен;
- `schema`, `driver`, `server`, `database`, `user`, `password` остаются в
  `AgtySqlConfig`, потому что библиотека сама создает physical JDBC
  connections;
- если bean живет в Spring context, его shutdown должен быть привязан к
  lifecycle контейнера, чтобы пул корректно закрылся при остановке
  приложения.
