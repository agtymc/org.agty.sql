package org.agty.sql.config;

/**
 * A SQL config
 */
public class AgtySqlConfig {
    private String driver = "mysql";
    private String server = "localhost";
    private int port = 3306;
    private String user;
    private String password;
    private String owner;
    private String database;
    private String schema;
    private String pfx;
    private String encoding = "UTF-8";
    private String timeZone = "UTC";
    private boolean autoCommit = true;
    private boolean trustServerCertificate = false;
    private boolean logQuery = false;
    private boolean logQueryValues = false;
    private String logQueryFile;
    private boolean logErrors = false;
    private String logErrorsFile;
    private boolean debug = false;
    private boolean throwException = true;
    private boolean noRequery = false;
    private int stmtRows;
    private int loginTimeoutSeconds;
    private int networkTimeoutMillis;
    
    public AgtySqlConfig() {}

    /**
     * Deep clone of the config
     * @param agtySqlConfig Config
     * @return Clone AgtySqlConfig
     */
    public static AgtySqlConfig getClone(AgtySqlConfig agtySqlConfig) {
        return new AgtySqlConfig()
                    .setDriver(agtySqlConfig.getDriver())
                    .setServer(agtySqlConfig.getServer())
                    .setPort(agtySqlConfig.getPort())
                    .setUser(agtySqlConfig.getUser())
                    .setPassword(agtySqlConfig.getPassword())
                    .setOwner(agtySqlConfig.getOwner())
                    .setDatabase(agtySqlConfig.getDatabase())
                    .setSchema(agtySqlConfig.getSchema())
                    .setPfx(agtySqlConfig.getPfx())
                    .setEncoding(agtySqlConfig.getEncoding())
                    .setTimeZone(agtySqlConfig.getTimeZone())
                    .setAutoCommit(agtySqlConfig.isAutoCommit())
                    .setTrustServerCertificate(agtySqlConfig.isTrustServerCertificate())
                    .setLogQuery(agtySqlConfig.isLogQuery())
                    .setLogQueryValues(agtySqlConfig.isLogQueryValues())
                    .setLogQueryFile(agtySqlConfig.getLogQueryFile())
                    .setLogErrors(agtySqlConfig.isLogErrors())
                    .setLogErrorsFile(agtySqlConfig.getLogErrorsFile())
                    .setDebug(agtySqlConfig.isDebug())
                    .setThrowException(agtySqlConfig.isThrowException())
                    .setNoRequery(agtySqlConfig.noRequery())
                    .setStmtRows(agtySqlConfig.getStmtRows())
                    .setLoginTimeoutSeconds(agtySqlConfig.getLoginTimeoutSeconds())
                    .setNetworkTimeoutMillis(agtySqlConfig.getNetworkTimeoutMillis());
    }

    /**
     * Getters and setters
     */
    public String getDriver() {
        return driver;
    }

    public AgtySqlConfig setDriver(String driver) {
        this.driver = driver;
        return this;
    }

    public String getServer() {
        return server;
    }

    public AgtySqlConfig setServer(String server) {
        this.server = server;
        return this;
    }

    public int getPort() {
        return port;
    }

    public AgtySqlConfig setPort(int port) {
        this.port = port;
        return this;
    }

    public String getUser() {
        return user;
    }

    public AgtySqlConfig setUser(String user) {
        this.user = user;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public AgtySqlConfig setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getOwner() {
        return owner;
    }

    public AgtySqlConfig setOwner(String owner) {
        this.owner = owner;
        return this;
    }

    public String getDatabase() {
        return database;
    }

    public boolean isDatabase() {
        return database != null;
    }

    public AgtySqlConfig setDatabase(String database) {
        this.database = database;
        return this;
    }

    public String getSchema() {
        return schema;
    }

    public boolean isSchema() {
        return schema != null;
    }

    public AgtySqlConfig setSchema(String schema) {
        this.schema = schema;
        return this;
    }

    public String getPfx() {
        return pfx;
    }

    public AgtySqlConfig setPfx(String pfx) {
        this.pfx = pfx;
        return this;
    }

    public String getEncoding() {
        return encoding;
    }

    public AgtySqlConfig setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public AgtySqlConfig setTimeZone(String timeZone) {
        this.timeZone = timeZone;
        return this;
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }

    public AgtySqlConfig setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
        return this;
    }

    /**
     * Whether SQL Server may trust a certificate without validating its chain
     * and host name. Disabled by default; enable only for isolated development.
     */
    public boolean isTrustServerCertificate() {
        return trustServerCertificate;
    }

    /**
     * Enables an insecure SQL Server development override.
     *
     * @param trustServerCertificate {@code true} to skip certificate validation
     * @return current config
     */
    public AgtySqlConfig setTrustServerCertificate(boolean trustServerCertificate) {
        this.trustServerCertificate = trustServerCertificate;
        return this;
    }

    public boolean isLogQuery() {
        return logQuery;
    }

    public AgtySqlConfig setLogQuery(boolean logQuery) {
        this.logQuery = logQuery;
        return this;
    }

    /**
     * Whether query logs may contain literal values. Disabled by default.
     */
    public boolean isLogQueryValues() {
        return logQueryValues;
    }

    /**
     * Allows literal values in query logs. Do not enable for untrusted or personal data.
     *
     * @param logQueryValues whether values may be logged
     * @return current config
     */
    public AgtySqlConfig setLogQueryValues(boolean logQueryValues) {
        this.logQueryValues = logQueryValues;
        return this;
    }

    public String getLogErrorsFile() {
        return logErrorsFile;
    }

    public String getLogErrorsFileOrDefault(String logErrorsFile) {
        return this.logErrorsFile == null || this.logErrorsFile.isBlank() ? logErrorsFile : this.logErrorsFile;
    }

    public AgtySqlConfig setLogErrorsFile(String logErrorsFile) {
        this.logErrorsFile = logErrorsFile;
        return this;
    }

    public String getLogQueryFile() {
        return logQueryFile;
    }

    public String getLogQueryFileOrDefault(String logQueryFile) {
        return this.logQueryFile == null || this.logQueryFile.isBlank() ? logQueryFile : this.logQueryFile;
    }

    public AgtySqlConfig setLogQueryFile(String logQueryFile) {
        this.logQueryFile = logQueryFile;
        return this;
    }

    public boolean isLogErrors() {
        return logErrors;
    }

    public AgtySqlConfig setLogErrors(boolean logErrors) {
        this.logErrors = logErrors;
        return this;
    }

    public boolean isDebug() {
        return debug;
    }

    public AgtySqlConfig setDebug(boolean debug) {
        this.debug = debug;
        return this;
    }

    public boolean isThrowException() {
        return throwException;
    }

    public AgtySqlConfig setThrowException(boolean throwException) {
        this.throwException = throwException;
        return this;
    }

    public int getStmtRows() {
        return stmtRows;
    }

    public AgtySqlConfig setStmtRows(int stmtRows) {
        this.stmtRows = stmtRows;
        return this;
    }

    public boolean noRequery() {
        return noRequery;
    }

    public AgtySqlConfig setNoRequery(boolean noRequery) {
        this.noRequery = noRequery;
        return this;
    }

    /** Maximum time allowed to establish a connection, in seconds. Zero uses the driver default. */
    public int getLoginTimeoutSeconds() {
        return loginTimeoutSeconds;
    }

    public AgtySqlConfig setLoginTimeoutSeconds(int loginTimeoutSeconds) {
        if (loginTimeoutSeconds < 0) {
            throw new IllegalArgumentException("Login timeout must not be negative");
        }
        this.loginTimeoutSeconds = loginTimeoutSeconds;
        return this;
    }

    /** Maximum time for an established connection's network operations, in milliseconds. */
    public int getNetworkTimeoutMillis() {
        return networkTimeoutMillis;
    }

    public AgtySqlConfig setNetworkTimeoutMillis(int networkTimeoutMillis) {
        if (networkTimeoutMillis < 0) {
            throw new IllegalArgumentException("Network timeout must not be negative");
        }
        this.networkTimeoutMillis = networkTimeoutMillis;
        return this;
    }
}
