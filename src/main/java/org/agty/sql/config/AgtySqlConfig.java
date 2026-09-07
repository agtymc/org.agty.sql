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
    
    /**
     * Creates a new instance.
     */
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
     * @return operation result
     */
    public String getDriver() {
        return driver;
    }

    /**
     * Sets the driver.
     * @param driver parameter value
     * @return operation result
     */
    public AgtySqlConfig setDriver(String driver) {
        this.driver = driver;
        return this;
    }

    /**
     * Returns the server.
     * @return operation result
     */
    public String getServer() {
        return server;
    }

    /**
     * Sets the server.
     * @param server parameter value
     * @return operation result
     */
    public AgtySqlConfig setServer(String server) {
        this.server = server;
        return this;
    }

    /**
     * Returns the port.
     * @return operation result
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the port.
     * @param port parameter value
     * @return operation result
     */
    public AgtySqlConfig setPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * Returns the user.
     * @return operation result
     */
    public String getUser() {
        return user;
    }

    /**
     * Sets the user.
     * @param user parameter value
     * @return operation result
     */
    public AgtySqlConfig setUser(String user) {
        this.user = user;
        return this;
    }

    /**
     * Returns the password.
     * @return operation result
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     * @param password parameter value
     * @return operation result
     */
    public AgtySqlConfig setPassword(String password) {
        this.password = password;
        return this;
    }

    /**
     * Returns the owner.
     * @return operation result
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Sets the owner.
     * @param owner parameter value
     * @return operation result
     */
    public AgtySqlConfig setOwner(String owner) {
        this.owner = owner;
        return this;
    }

    /**
     * Returns the database.
     * @return operation result
     */
    public String getDatabase() {
        return database;
    }

    /**
     * Returns whether database.
     * @return operation result
     */
    public boolean isDatabase() {
        return database != null;
    }

    /**
     * Sets the database.
     * @param database parameter value
     * @return operation result
     */
    public AgtySqlConfig setDatabase(String database) {
        this.database = database;
        return this;
    }

    /**
     * Returns the schema.
     * @return operation result
     */
    public String getSchema() {
        return schema;
    }

    /**
     * Returns whether schema.
     * @return operation result
     */
    public boolean isSchema() {
        return schema != null;
    }

    /**
     * Sets the schema.
     * @param schema parameter value
     * @return operation result
     */
    public AgtySqlConfig setSchema(String schema) {
        this.schema = schema;
        return this;
    }

    /**
     * Returns the pfx.
     * @return operation result
     */
    public String getPfx() {
        return pfx;
    }

    /**
     * Sets the pfx.
     * @param pfx parameter value
     * @return operation result
     */
    public AgtySqlConfig setPfx(String pfx) {
        this.pfx = pfx;
        return this;
    }

    /**
     * Returns the encoding.
     * @return operation result
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Sets the encoding.
     * @param encoding parameter value
     * @return operation result
     */
    public AgtySqlConfig setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    /**
     * Returns the time zone.
     * @return operation result
     */
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * Sets the time zone.
     * @param timeZone parameter value
     * @return operation result
     */
    public AgtySqlConfig setTimeZone(String timeZone) {
        this.timeZone = timeZone;
        return this;
    }

    /**
     * Returns whether auto commit.
     * @return operation result
     */
    public boolean isAutoCommit() {
        return autoCommit;
    }

    /**
     * Sets the auto commit.
     * @param autoCommit parameter value
     * @return operation result
     */
    public AgtySqlConfig setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
        return this;
    }

    /**
     * Whether SQL Server may trust a certificate without validating its chain
     * and host name. Disabled by default; enable only for isolated development.
     * @return operation result
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

    /**
     * Returns whether log query.
     * @return operation result
     */
    public boolean isLogQuery() {
        return logQuery;
    }

    /**
     * Sets the log query.
     * @param logQuery parameter value
     * @return operation result
     */
    public AgtySqlConfig setLogQuery(boolean logQuery) {
        this.logQuery = logQuery;
        return this;
    }

    /**
     * Whether query logs may contain literal values. Disabled by default.
     * @return operation result
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

    /**
     * Returns the log errors file.
     * @return operation result
     */
    public String getLogErrorsFile() {
        return logErrorsFile;
    }

    /**
     * Returns the log errors file or default.
     * @param logErrorsFile parameter value
     * @return operation result
     */
    public String getLogErrorsFileOrDefault(String logErrorsFile) {
        return this.logErrorsFile == null || this.logErrorsFile.isBlank() ? logErrorsFile : this.logErrorsFile;
    }

    /**
     * Sets the log errors file.
     * @param logErrorsFile parameter value
     * @return operation result
     */
    public AgtySqlConfig setLogErrorsFile(String logErrorsFile) {
        this.logErrorsFile = logErrorsFile;
        return this;
    }

    /**
     * Returns the log query file.
     * @return operation result
     */
    public String getLogQueryFile() {
        return logQueryFile;
    }

    /**
     * Returns the log query file or default.
     * @param logQueryFile parameter value
     * @return operation result
     */
    public String getLogQueryFileOrDefault(String logQueryFile) {
        return this.logQueryFile == null || this.logQueryFile.isBlank() ? logQueryFile : this.logQueryFile;
    }

    /**
     * Sets the log query file.
     * @param logQueryFile parameter value
     * @return operation result
     */
    public AgtySqlConfig setLogQueryFile(String logQueryFile) {
        this.logQueryFile = logQueryFile;
        return this;
    }

    /**
     * Returns whether log errors.
     * @return operation result
     */
    public boolean isLogErrors() {
        return logErrors;
    }

    /**
     * Sets the log errors.
     * @param logErrors parameter value
     * @return operation result
     */
    public AgtySqlConfig setLogErrors(boolean logErrors) {
        this.logErrors = logErrors;
        return this;
    }

    /**
     * Returns whether debug.
     * @return operation result
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * Sets the debug.
     * @param debug parameter value
     * @return operation result
     */
    public AgtySqlConfig setDebug(boolean debug) {
        this.debug = debug;
        return this;
    }

    /**
     * Returns whether throw exception.
     * @return operation result
     */
    public boolean isThrowException() {
        return throwException;
    }

    /**
     * Sets the throw exception.
     * @param throwException parameter value
     * @return operation result
     */
    public AgtySqlConfig setThrowException(boolean throwException) {
        this.throwException = throwException;
        return this;
    }

    /**
     * Returns the stmt rows.
     * @return operation result
     */
    public int getStmtRows() {
        return stmtRows;
    }

    /**
     * Sets the stmt rows.
     * @param stmtRows parameter value
     * @return operation result
     */
    public AgtySqlConfig setStmtRows(int stmtRows) {
        this.stmtRows = stmtRows;
        return this;
    }

    /**
     * Performs the no requery operation.
     * @return operation result
     */
    public boolean noRequery() {
        return noRequery;
    }

    /**
     * Sets the no requery.
     * @param noRequery parameter value
     * @return operation result
     */
    public AgtySqlConfig setNoRequery(boolean noRequery) {
        this.noRequery = noRequery;
        return this;
    }

    /**
     * Returns the maximum time allowed to establish a connection.
     *
     * @return timeout in seconds, or zero to use the driver default
     */
    public int getLoginTimeoutSeconds() {
        return loginTimeoutSeconds;
    }

    /**
     * Sets the login timeout seconds.
     * @param loginTimeoutSeconds parameter value
     * @return operation result
     */
    public AgtySqlConfig setLoginTimeoutSeconds(int loginTimeoutSeconds) {
        if (loginTimeoutSeconds < 0) {
            throw new IllegalArgumentException("Login timeout must not be negative");
        }
        this.loginTimeoutSeconds = loginTimeoutSeconds;
        return this;
    }

    /**
     * Returns the maximum time for established-connection network operations.
     *
     * @return network timeout in milliseconds
     */
    public int getNetworkTimeoutMillis() {
        return networkTimeoutMillis;
    }

    /**
     * Sets the network timeout millis.
     * @param networkTimeoutMillis parameter value
     * @return operation result
     */
    public AgtySqlConfig setNetworkTimeoutMillis(int networkTimeoutMillis) {
        if (networkTimeoutMillis < 0) {
            throw new IllegalArgumentException("Network timeout must not be negative");
        }
        this.networkTimeoutMillis = networkTimeoutMillis;
        return this;
    }
}
