package org.agty.sql.pool;

import org.agty.sql.config.AgtySqlConfig;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class ConnectionPool {

    private static final int DEFAULT_MAX_POOL_SIZE = 32;
    private static final Duration DEFAULT_MAX_LIFETIME = Duration.ofMinutes(30);
    private static final Duration DEFAULT_BORROW_TIMEOUT = Duration.ofMillis(300);

    private static final Map<String, AgtySQLPool> POOLS = new ConcurrentHashMap<>();
    private static final Map<String, PoolOptions> OPTIONS = new ConcurrentHashMap<>();

    private ConnectionPool() {
    }

    public static void register(String poolName, Supplier<AgtySqlConfig> provider) {
        PoolDbConfigFactory.register(poolName, provider);
    }

    public static void register(String poolName, Supplier<AgtySqlConfig> provider, PoolOptions options) {
        String key = normalize(poolName);
        register(key, provider);
        OPTIONS.put(key, options == null ? PoolOptions.defaults() : options);
    }

    public static AgtySQLPool get(String name) {
        String key = normalize(name);
        return POOLS.computeIfAbsent(key, ConnectionPool::createPool);
    }

    public static void close(String name) {
        String key = normalize(name);
        AgtySQLPool pool = POOLS.remove(key);
        if (pool != null) {
            pool.close();
        }
    }

    public static void closeAll() {
        for (String key : POOLS.keySet()) {
            close(key);
        }
    }

    private static AgtySQLPool createPool(String poolName) {
        PoolOptions options = OPTIONS.getOrDefault(poolName, PoolOptions.defaults());
        return new AgtySQLPool(
                PoolDbConfigFactory.getConfig(poolName),
                options.maxPoolSize(),
                options.maxLifetime(),
                options.borrowTimeout()
        );
    }

    private static String normalize(String name) {
        String trimmed = name == null ? "" : name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Connection pool name must not be empty");
        }
        return trimmed;
    }

    public record PoolOptions(int maxPoolSize, Duration maxLifetime, Duration borrowTimeout) {

        public PoolOptions {
            if (maxPoolSize < 1) {
                throw new IllegalArgumentException("maxPoolSize must be greater than zero");
            }
            if (maxLifetime == null || maxLifetime.isZero() || maxLifetime.isNegative()) {
                throw new IllegalArgumentException("maxLifetime must be positive");
            }
            if (borrowTimeout == null || borrowTimeout.isZero() || borrowTimeout.isNegative()) {
                throw new IllegalArgumentException("borrowTimeout must be positive");
            }
        }

        public static PoolOptions defaults() {
            return new PoolOptions(
                    DEFAULT_MAX_POOL_SIZE,
                    DEFAULT_MAX_LIFETIME,
                    DEFAULT_BORROW_TIMEOUT
            );
        }
    }
}
