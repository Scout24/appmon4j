package de.is24.util.monitoring.database;

import de.is24.util.monitoring.InApplicationMonitor;
import de.is24.util.monitoring.StateValueProvider;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * <p>
 * The <code>MonitoringDataSource</code> wraps a given <code>DataSource</code>
 * and the subsequently returned <code>Connections</code>. The combination of
 * <code>MonitoringDataSource</code> and {@link MonitoringConnection} monitor
 * </p>
 * <ul>
 * <li>the number of requested database connections split into</li>
 * <ul>
 * <li>unpersonalised ({@link #getConnection()}) and</li>
 * <li>personalised connections {@link #getConnection(String, String)}</li>
 * </ul>
 * <li>the maximum number of connections used in parallel</li>
 * <li>the number of errors thrown when calling</li>
 * <ul>
 * <li>{@link java.sql.Connection#commit()}</li>
 * <li>{@link java.sql.Connection#rollback()}</li>
 * <li>{@link java.sql.Connection#rollback(java.sql.Savepoint)}</li>
 * </ul>
 * </ul>
 * <p>
 * Additionally, the returned {@link java.sql.Statement}s are wrapped and attempt to log
 * the SQL being executed in case an exception is thrown.
 * </p>
 *
 * If you want to use this Class, you need to require springframework core.
 *
 * @author Sebastian Kirsch
 * @see #getConnection()
 * @see #getConnection(String, String)
 */
public class MonitoringDataSource implements DataSource {
  private static final Logger LOGGER = LoggerFactory.getLogger(MonitoringDataSource.class);

  /** The wrapped <code>DataSource</code>. */
  private final DataSource original;

  /** The base name for all monitor values. */
  private final String monitorBaseName;

  /** This field is used to trace the maximum number of connections being use in parallel. */
  private final AtomicInteger maxConnections;

  /** This field is a helper to determine the correct for {@link #maxConnections}. */
  private final AtomicInteger currentConnections;

  /** These predicates indicate if a the executed SQL should be logged at INFO level. */
  final Set<Predicate<SQLException>> loggingFilters = new HashSet<Predicate<SQLException>>();

  // CONSTRUCTORS //////////////////////////////////////////////////////////////

  /**
   * Creates a new instance of <code>MonitoringDataSource</code>.
   *
   * @param   dataSource
   *          the <code>DataSource</code> to wrap
   * @param   monitorBaseName
   *          the monitor name to insert such that "de.is24.common.database.MonitoringDataSource.<i>monitorBaseName</i>.monitoringKey" is reported
   * @throws  IllegalArgumentException
   *          if <code>dataSource</code> is <code>null</code>
   */
  public MonitoringDataSource(DataSource dataSource, String monitorBaseName) {
    if (dataSource == null) {
      throw new IllegalArgumentException("DataSource is null");
    }
    this.original = dataSource;
    this.monitorBaseName = MonitoringDataSource.class.getName() +
      (StringUtils.isBlank(monitorBaseName) ? "" : ("." + monitorBaseName));
    this.currentConnections = new AtomicInteger(0);
    this.maxConnections = new AtomicInteger(0);

    InApplicationMonitor.getInstance().registerStateValue(new StateValueProvider() {
        @Override
        public String getName() {
          return MonitoringDataSource.this.monitorBaseName + ".maxOpenConnections";
        }

        @Override
        public long getValue() {
          return MonitoringDataSource.this.maxConnections.get();
        }
      });

    // initialise InApplicationMonitor
    InApplicationMonitor.getInstance()
    .initializeCounter(MonitoringDataSource.this.monitorBaseName +
      ".error.getConnection");
    InApplicationMonitor.getInstance().initializeCounter(MonitoringDataSource.this.monitorBaseName +
      ".error.commit");
    InApplicationMonitor.getInstance()
    .initializeCounter(MonitoringDataSource.this.monitorBaseName +
      ".getPersonalisedConnection");
    InApplicationMonitor.getInstance().initializeCounter(
      MonitoringDataSource.this.monitorBaseName + ".error.rollback");
    InApplicationMonitor.getInstance()
    .initializeCounter(
      MonitoringDataSource.this.monitorBaseName + ".error.rollbackSavepoint");
  }

  /**
   * Creates a new instance of <code>MonitoringDataSource</code>
   * without a specific monitoring base name.
   *
   * @param dataSource
   *          the <code>DataSource</code> to wrap
   * @throws IllegalArgumentException
   *           if <code>dataSource</code> is <code>null</code>
   */
  public MonitoringDataSource(DataSource dataSource) {
    this(dataSource, null);
  }

  // METHODS ///////////////////////////////////////////////////////////////////

  public void addExceptionLogFilter(Predicate<SQLException> predicate) {
    this.loggingFilters.add(predicate);
  }

  public void setExceptionLogFilters(String configuration) {
    this.loggingFilters.clear();
    if (StringUtils.isEmpty(configuration)) {
      return;
    }
    configureSqlExceptionPredicates(configuration);
  }

  private void configureSqlExceptionPredicates(String configuration) {
    for (String configEntry : configuration.split("[,]")) {
      String[] configEntryItems = configEntry.split("[:]");
      if (configEntryItems.length > 2) {
        throw new IllegalArgumentException("The config entry [" + configEntry +
          "] contains more than one ':' and thus is invalid!");
      }

      int errorCode = Integer.parseInt(configEntryItems[0]);
      String regex = (configEntryItems.length > 1) ? configEntryItems[1] : null;
      this.loggingFilters.add(new SqlExceptionPredicate(errorCode, regex));
    }
  }

  /**
   * Handles the monitoring for retrieving connections and adapts the <i>max connection</i> counter if appropriate.
   *
   * @param   startingInstant
   *          the instant a database connection was requested
   * @param   monitorSuffix
   *          the suffix for the monitor name to increase
   */
  private void doConnectionMonitoring(long startingInstant, String monitorSuffix) {
    InApplicationMonitor.getInstance()
    .addTimerMeasurement(this.monitorBaseName + monitorSuffix,
      System.currentTimeMillis() - startingInstant);

    int noCurrentConnections = this.currentConnections.incrementAndGet();
    if (noCurrentConnections > this.maxConnections.get()) {
      this.maxConnections.set(noCurrentConnections);
    }
  }

  /** Increases the error count for the getConnection method. */
  private void monitorFailedConnectionAttempt() {
    InApplicationMonitor.getInstance()
    .incrementCounter(MonitoringDataSource.this.monitorBaseName +
      ".error.getConnection");
  }

  /**
   * Executes the specified {@link java.util.concurrent.Callable} to fetch a connection.
   * Monitors occurring exceptions/errors.
   *
   * @param   callable
   *          the <code>Callable</code> that actually fetches a <code>Connection</code>
   * @param   monitorSuffix
   *          the suffix for the monitor name to increase (forwarded to {@link #doConnectionMonitoring(long, String)})
   * @return  a database <code>Connection</code>
   * @throws  java.sql.SQLException
   *          if fetching a <code>Connection</code> fails
   * @throws  RuntimeException
   */
  private Connection getConnection(Callable<Connection> callable, String monitorSuffix) throws SQLException {
    final long now = System.currentTimeMillis();
    try {
      MonitoringConnection c = new MonitoringConnection(callable.call());
      doConnectionMonitoring(now, monitorSuffix);
      return c;

      // CSOFF: IllegalCatch
      // CSOFF: IllegalThrows
    } catch (Error e) {
      monitorFailedConnectionAttempt();

      throw e;
    } catch (RuntimeException rE) {
      monitorFailedConnectionAttempt();

      // Well, this MAY happen, although it shouldn't
      throw rE;
      // CSON: IllegalCatch
      // CSON: IllegalThrows
    } catch (SQLException sqlE) {
      monitorFailedConnectionAttempt();

      // sad but true
      throw sqlE;
    } catch (Exception e) {
      monitorFailedConnectionAttempt();

      // This MUST NOT happen - meaning that someone frakked the code of this class
      LOGGER.error(
        "Unexpected Exception thrown by Callable; please check source code of de.is24.common.database.MonitoringDataSource: " +
        e);
      throw new RuntimeException(e);
    }
  }

  // java.lang.Object //////////////////////////////////////////////////////////

  /**
   * Returns a String representation of this object.
   *
   * @return  a <code>String</code> representing this instance
   */
  @Override
  public String toString() {
    return this.monitorBaseName.substring(MonitoringDataSource.class.getName().lastIndexOf('.') + 1) + " wrapping [" +
      this.original + "]";
  }

  // javax.sql.DataSource //////////////////////////////////////////////////////

  /**
   * <p>
   * Delegates the call to the wrapped data source, wraps the returned
   * connection and counts the connections returned.
   * </p>
   *
   * @return  a database <code>Connection</code>
   * @throws  java.sql.SQLException
   *          as a result of the delegation
   */
  public Connection getConnection() throws SQLException {
    return getConnection(
      new Callable<Connection>() {
        public Connection call() throws SQLException {
          return MonitoringDataSource.this.original.getConnection();
        }
      }, ".getConnection");
  }

  /**
   * <p>
   * Delegates the call to the wrapped data source, wraps the returned
   * connection and counts the connections returned.
   * </p>
   *
   * @param   username
   *          the name of the database user on whose behalf the connection isbeing made
   * @param   password
   *          the user's password
   * @return  a database <code>Connection</code>
   * @throws  java.sql.SQLException
   *          as a result of the delegation
   */
  public Connection getConnection(final String username, final String password) throws SQLException {
    return getConnection(
      new Callable<Connection>() {
        public Connection call() throws SQLException {
          return MonitoringDataSource.this.original.getConnection(username, password);
        }
      }, ".getPersonalisedConnection");
  }

  public PrintWriter getLogWriter() throws SQLException {
    return this.original.getLogWriter();
  }

  public int getLoginTimeout() throws SQLException {
    return this.original.getLoginTimeout();
  }

  public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
    return this.original.getParentLogger();
  }

  public void setLogWriter(PrintWriter out) throws SQLException {
    this.original.setLogWriter(out);
  }

  public void setLoginTimeout(int seconds) throws SQLException {
    this.original.setLoginTimeout(seconds);
  }

  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return original.isWrapperFor(iface);
  }

  public <T> T unwrap(Class<T> iface) throws SQLException {
    return original.unwrap(iface);
  }


  /**
   * <p>The <code>MonitoringConnection</code> is integral part of the {@link MonitoringDataSource}.</p>
   *
   * @author  <a href="mailto:SKirsch@is24.de">Sebastian Kirsch</a>
   * @see   MonitoringDataSource
   * @see   #close()
   * @see   #commit()
   * @see   #rollback()
   * @see   #rollback(java.sql.Savepoint)
   */
  private class MonitoringConnection implements Connection {
    private final Connection original;
    private final long creationTime = System.currentTimeMillis();

    public MonitoringConnection(Connection connection) {
      if (connection == null) {
        throw new NullPointerException("The Connection cannot be null!");
      }
      this.original = connection;
    }

    private Object wrapStatementWithSqlLoggingProxy(Statement statement, String sql) {
      Class<? extends Statement> clazz = statement.getClass();
      Object proxiedStatement = Proxy.newProxyInstance(clazz.getClassLoader(), clazz.getInterfaces(),
        new SqlLoggingInvocationHandler(MonitoringDataSource.this.loggingFilters, statement, sql));
      return proxiedStatement;
    }

    private Object wrapStatementWithSqlLoggingProxy(Statement statement) {
      return wrapStatementWithSqlLoggingProxy(statement, null);
    }

    // java.lang.Object ////////////////////////////////////////////////////////

    /**
     * Returns a String representation of this object.
     *
     * @return  a <code>String</code> representing this instance
     */
    @Override
    public String toString() {
      return "MonitoringConnection from " +
        MonitoringDataSource.this.monitorBaseName.substring(MonitoringDataSource.class.getName().lastIndexOf('.') + 1) +
        " wrapping [" + this.original + "]";
    }

    // java.sql.Connection ///////////////////////////////////////////////////

    /**
     * <p>Delegates the call to the wrapped data source, counting the number of calls made.</p>
     *
     * @throws  java.sql.SQLException
     *          as a result of the delegation
     */
    public void close() throws SQLException {
      try {
        this.original.close();
      } finally {
        MonitoringDataSource.this.currentConnections.decrementAndGet();
        InApplicationMonitor.getInstance().incrementCounter(MonitoringDataSource.this.monitorBaseName + ".close");
        InApplicationMonitor.getInstance()
        .addTimerMeasurement(MonitoringDataSource.this.monitorBaseName + ".usage",
          this.creationTime, System.currentTimeMillis());
      }
    }

    /**
     * <p>Delegates the call to the wrapped data source, counting the number of exceptions thrown.</p>
     *
     * @throws  java.sql.SQLException
     *          as a result of the delegation
     */
    public void commit() throws SQLException {
      try {
        this.original.commit();
      } catch (SQLException sqlE) {
        InApplicationMonitor.getInstance()
        .incrementCounter(MonitoringDataSource.this.monitorBaseName +
          ".error.commit");
        throw sqlE;
      }
    }

    /**
     * <p>Delegates the call to the wrapped data source, counting the number of exceptions thrown.</p>
     *
     * @throws  java.sql.SQLException
     *          as a result of the delegation
     */
    public void rollback() throws SQLException {
      try {
        this.original.rollback();
      } catch (SQLException sqlE) {
        InApplicationMonitor.getInstance()
        .incrementCounter(
          MonitoringDataSource.this.monitorBaseName + ".error.rollback");
        throw sqlE;
      }
    }

    /**
     * <p>Delegates the call to the wrapped data source, counting the number of exceptions thrown.</p>
     *
     * @throws  java.sql.SQLException
     *          as a result of the delegation
     */
    public void rollback(Savepoint savepoint) throws SQLException {
      try {
        this.original.rollback(savepoint);
      } catch (SQLException sqlE) {
        InApplicationMonitor.getInstance()
        .incrementCounter(
          MonitoringDataSource.this.monitorBaseName + ".error.rollbackSavepoint");
        throw sqlE;
      }
    }

    public void clearWarnings() throws SQLException {
      this.original.clearWarnings();
    }

    public Statement createStatement() throws SQLException {
      return (Statement) wrapStatementWithSqlLoggingProxy(this.original.createStatement());
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
                              throws SQLException {
      return (Statement) wrapStatementWithSqlLoggingProxy(this.original.createStatement(resultSetType,
          resultSetConcurrency, resultSetHoldability));
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
      return (Statement) wrapStatementWithSqlLoggingProxy(this.original.createStatement(resultSetType,
          resultSetConcurrency));
    }

    public boolean getAutoCommit() throws SQLException {
      return this.original.getAutoCommit();
    }

    public String getCatalog() throws SQLException {
      return this.original.getCatalog();
    }

    public int getHoldability() throws SQLException {
      return this.original.getHoldability();
    }

    public DatabaseMetaData getMetaData() throws SQLException {
      return this.original.getMetaData();
    }

    public int getTransactionIsolation() throws SQLException {
      return this.original.getTransactionIsolation();
    }

    public Map<String, Class<?>> getTypeMap() throws SQLException {
      return this.original.getTypeMap();
    }

    public SQLWarning getWarnings() throws SQLException {
      return this.original.getWarnings();
    }

    public boolean isClosed() throws SQLException {
      return this.original.isClosed();
    }

    public boolean isReadOnly() throws SQLException {
      return this.original.isReadOnly();
    }

    public String nativeSQL(String sql) throws SQLException {
      return this.original.nativeSQL(sql);
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
                                         int resultSetHoldability) throws SQLException {
      return (CallableStatement) wrapStatementWithSqlLoggingProxy(this.original.prepareCall(sql, resultSetType,
          resultSetConcurrency, resultSetHoldability), sql);
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
      return (CallableStatement) wrapStatementWithSqlLoggingProxy(this.original.prepareCall(sql, resultSetType,
          resultSetConcurrency), sql);
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
      return (CallableStatement) wrapStatementWithSqlLoggingProxy(this.original.prepareCall(sql), sql);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
                                              int resultSetHoldability) throws SQLException {
      return (PreparedStatement) wrapStatementWithSqlLoggingProxy(this.original.prepareStatement(sql, resultSetType,
          resultSetConcurrency, resultSetHoldability), sql);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
                                       throws SQLException {
      return (PreparedStatement) wrapStatementWithSqlLoggingProxy(this.original.prepareStatement(sql, resultSetType,
          resultSetConcurrency), sql);
    }

    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
      return (PreparedStatement) wrapStatementWithSqlLoggingProxy(this.original.prepareStatement(sql,
          autoGeneratedKeys), sql);
    }

    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
      return (PreparedStatement) wrapStatementWithSqlLoggingProxy(this.original.prepareStatement(sql, columnIndexes),
        sql);
    }

    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
      return (PreparedStatement) wrapStatementWithSqlLoggingProxy(this.original.prepareStatement(sql, columnNames),
        sql);
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
      return (PreparedStatement) wrapStatementWithSqlLoggingProxy(this.original.prepareStatement(sql), sql);
    }

    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
      this.original.releaseSavepoint(savepoint);
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
      this.original.setAutoCommit(autoCommit);
    }

    public void setCatalog(String catalog) throws SQLException {
      this.original.setCatalog(catalog);
    }

    public void setHoldability(int holdability) throws SQLException {
      this.original.setHoldability(holdability);
    }

    public void setReadOnly(boolean readOnly) throws SQLException {
      this.original.setReadOnly(readOnly);
    }

    public Savepoint setSavepoint() throws SQLException {
      return this.original.setSavepoint();
    }

    public Savepoint setSavepoint(String name) throws SQLException {
      return this.original.setSavepoint(name);
    }

    public void setTransactionIsolation(int level) throws SQLException {
      this.original.setTransactionIsolation(level);
    }

    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
      this.original.setTypeMap(map);
    }

    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
      return original.createArrayOf(typeName, elements);
    }

    public Blob createBlob() throws SQLException {
      return original.createBlob();
    }

    public Clob createClob() throws SQLException {
      return original.createClob();
    }

    public NClob createNClob() throws SQLException {
      return original.createNClob();
    }

    public SQLXML createSQLXML() throws SQLException {
      return original.createSQLXML();
    }

    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
      return original.createStruct(typeName, attributes);
    }

    public void setSchema(String schema) throws SQLException {
      this.original.setSchema(schema);
    }

    public String getSchema() throws SQLException {
      return this.original.getSchema();
    }

    public void abort(Executor executor) throws SQLException {
      this.original.abort(executor);
    }

    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
      this.original.setNetworkTimeout(executor, milliseconds);
    }

    public int getNetworkTimeout() throws SQLException {
      return this.original.getNetworkTimeout();
    }

    public Properties getClientInfo() throws SQLException {
      return original.getClientInfo();
    }

    public String getClientInfo(String name) throws SQLException {
      return original.getClientInfo(name);
    }

    public boolean isValid(int timeout) throws SQLException {
      return original.isValid(timeout);
    }

    public void setClientInfo(Properties properties) throws SQLClientInfoException {
      original.setClientInfo(properties);
    }

    public void setClientInfo(String name, String value) throws SQLClientInfoException {
      original.setClientInfo(name, value);
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
      return original.isWrapperFor(iface);
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
      return original.unwrap(iface);
    }
  }

  private static class SqlLoggingInvocationHandler implements InvocationHandler {
    private final Object wrappedObject;

    private final String preparedSql;

    final Set<Predicate<SQLException>> loggingFilters;

    public SqlLoggingInvocationHandler(Set<Predicate<SQLException>> loggingFilters, Object wrappedObject,
                                       String preparedSql) {
      this.loggingFilters = loggingFilters;
      this.wrappedObject = wrappedObject;
      this.preparedSql = preparedSql;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable {
      try {
        return method.invoke(this.wrappedObject, arguments);
      } catch (IllegalAccessException e) {
        throw new RuntimeException("Cannot execute wrapped call [" + method.getName() + "()@" + proxy + "]", e);
      } catch (InvocationTargetException iTE) {
        logExecutedSqlIfApplicable(method, arguments, iTE);
        throw iTE.getTargetException();
      }
    }

    private void logExecutedSqlIfApplicable(Method method, Object[] arguments, InvocationTargetException iTE) {
      if (!anExecuteMethodIsCalled(method)) {
        return;
      }

      String sql = determineExecutedSql(arguments);
      if (sql == null) {
        return;
      }
      logExecutedSqlAtTheAppropriateLevel(iTE, sql);
    }

    private boolean anExecuteMethodIsCalled(Method method) {
      return method.getName().startsWith("execute");
    }

    private String determineExecutedSql(Object[] arguments) {
      String sql = this.preparedSql;
      if ((arguments != null) && (arguments[0] instanceof String)) {
        sql = (String) arguments[0];
      }
      return sql;
    }

    private void logExecutedSqlAtTheAppropriateLevel(InvocationTargetException iTE, String sql) {
      if (shouldLogAsWarn(iTE)) {
        LOGGER.warn("Failed to execute [{}]: {}", sql, iTE.getTargetException());
      } else {
        LOGGER.info("Failed to execute [[{}]: {}", sql, iTE.getTargetException());
      }
    }

    private boolean shouldLogAsWarn(InvocationTargetException iTE) {
      boolean logAsWarn = true;
      int index = ExceptionUtils.indexOfType(iTE, SQLException.class);
      if (index >= 0) {
        SQLException sqlException = (SQLException) ExceptionUtils.getThrowables(iTE)[index];
        for (Predicate<SQLException> predicate : this.loggingFilters) {
          if (predicate.apply(sqlException)) {
            logAsWarn = false;
            break;
          }
        }
      }
      return logAsWarn;
    }

  }

  static class SqlExceptionPredicate implements Predicate<SQLException> {
    private final int errorCode;
    private final String messagePattern;

    public SqlExceptionPredicate(int errorCode, String messagePattern) {
      this.errorCode = errorCode;
      this.messagePattern = messagePattern;
    }

    @Override
    public boolean apply(SQLException input) {
      if (this.errorCode != input.getErrorCode()) {
        return false;
      }
      if (this.messagePattern == null) {
        return true;
      }
      return (input.getMessage() != null) && input.getMessage().matches(this.messagePattern);
    }

  }

  protected interface Predicate<T> {
    boolean apply(T input);

    @Override
    boolean equals(Object object);
  }

}
