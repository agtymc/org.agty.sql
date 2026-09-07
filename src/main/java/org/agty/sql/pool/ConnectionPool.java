package org.agty.sql.pool;

import org.agty.sql.config.AgtySqlConfig;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Provides connection pool behavior.
 */
public final class ConnectionPool {

    private static final int DEFAULT_MAX_POOL_SIZE = 32;
    private static final Duration DEFAULT_MAX_LIFETIME = Duration.ofMinutes(30);
    private static final Duration DEFAULT_BORROW_TIMEOUT = Duration.ofMillis(300);

    private static final Map<String, AgtySQLPool> POOLS = new ConcurrentHashMap<>();
    private static final Map<String, PoolOptions> OPTIONS = new ConcurrentHashMap<>();

    private ConnectionPool() {
    }

    /**
     * Performs the register operation.
     * @param poolName parameter value
     * @param provider parameter value
     */
    public static void register(String poolName, Supplier<AgtySqlConfig> provider) {
        PoolDbConfigFactory.register(poolName, provider);
    }

    /**
     * Performs the register operation.
     * @param poolName parameter value
     * @param provider parameter value
     * @param options parameter value
     */
    public static void register(String poolName, Supplier<AgtySqlConfig> provider, PoolOptions options) {
        String key = normalize(poolName);
        register(key, provider);
        OPTIONS.put(key, options == null ? PoolOptions.defaults() : options);
    }

    /**
     * Returns the get.
     * @param name parameter value
     * @return operation result
     */
    public static AgtySQLPool get(String name) {
        String key = normalize(name);
        return POOLS.computeIfAbsent(key, ConnectionPool::createPool);
    }

    /**
     * Performs the close operation.
     * @param name parameter value
     */
    public static void close(String name) {
        String key = normalize(name);
        AgtySQLPool pool = POOLS.remove(key);
        if (pool != null) {
            pool.close();
        }
    }

    /**
     * Performs the close all operation.
     */
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

    /**
     * Configures capacity, connection lifetime, and acquisition timeout for a named pool.
     *
     * @param maxPoolSize maximum number of physical connections retained by the pool
     * @param maxLifetime maximum lifetime of a physical connection
     * @param borrowTimeout maximum time to wait for an available connection
     */
    public record PoolOptions(int maxPoolSize, Duration maxLifetime, Duration borrowTimeout) {

        /**
         * Creates a new instance.
         * @param maxPoolSize parameter value
         * @param maxLifetime parameter value
         * @param borrowTimeout parameter value
         */
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

        /**
         * Performs the defaults operation.
         * @return operation result
         */
        public static PoolOptions defaults() {
            return new PoolOptions(
                    DEFAULT_MAX_POOL_SIZE,
                    DEFAULT_MAX_LIFETIME,
                    DEFAULT_BORROW_TIMEOUT
            );
        }
    }
}
