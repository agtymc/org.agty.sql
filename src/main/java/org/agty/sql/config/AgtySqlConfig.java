package org.agty.sql.config;

/**
 * A SQL config
 */
public class AgtySqlConfig {
    private String driver = "mysql";
    private String server = "localhost";
    private int port = 3306;
    private String user = "root";
    private String password;
    private String owner;
    private String database;
    private String schema;
    private String pfx;
    private String encoding = "UTF-8";
    private String timeZone = "UTC";
    private boolean autoCommit = true;
    private boolean logQuery = false;
    private String logQueryFile;
    private boolean logErrors = false;
    private String logErrorsFile;
    private boolean debug = false;
    private boolean throwException = false;
    private boolean noRequery = false;
    private int stmtRows;
    
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
                    .setLogQuery(agtySqlConfig.isLogQuery())
                    .setLogQueryFile(agtySqlConfig.getLogQueryFile())
                    .setLogErrors(agtySqlConfig.isLogErrors())
                    .setLogErrorsFile(agtySqlConfig.getLogErrorsFile())
                    .setDebug(agtySqlConfig.isDebug())
                    .setThrowException(agtySqlConfig.isThrowException())
                    .setNoRequery(agtySqlConfig.noRequery())
                    .setStmtRows(agtySqlConfig.getStmtRows());
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

    public boolean isLogQuery() {
        return logQuery;
    }

    public AgtySqlConfig setLogQuery(boolean logQuery) {
        this.logQuery = logQuery;
        return this;
    }

    public String getLogErrorsFile() {
        return logErrorsFile;
    }

    public String getLogErrorsFileOrDefault(String logErrorsFile) {
        return this.logErrorsFile.isEmpty() ? logErrorsFile : this.logErrorsFile;
    }

    public AgtySqlConfig setLogErrorsFile(String logErrorsFile) {
        this.logErrorsFile = logErrorsFile;
        return this;
    }

    public String getLogQueryFile() {
        return logQueryFile;
    }

    public String getLogQueryFileOrDefault(String logQueryFile) {
        return this.logQueryFile.isEmpty() ? logQueryFile : this.logQueryFile;
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
}
