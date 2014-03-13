package de.is24.util.monitoring.database;

import de.is24.util.monitoring.Counter;
import de.is24.util.monitoring.InApplicationMonitor;
import de.is24.util.monitoring.InApplicationMonitorRule;
import de.is24.util.monitoring.Reportable;
import de.is24.util.monitoring.ReportableObserver;
import de.is24.util.monitoring.StateValueProvider;
import de.is24.util.monitoring.Timer;
import de.is24.util.monitoring.database.MonitoringDataSource.SqlExceptionPredicate;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.easymock.IArgumentMatcher;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import javax.sql.DataSource;
import java.io.PrintWriter;
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
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Tests the behaviour of {@link MonitoringDataSource}.
 *
 * @author  <a href="mailto:SKirsch@is24.de">Sebastian Kirsch</a>
 */
public final class MonitoringDataSourceTest extends EasyMockSupport {
  @Rule
  public final InApplicationMonitorRule inApplicationMonitorRule = new InApplicationMonitorRule();

  private static LoggingEvent logEventMatches(Level level, String messagePattern) {
    EasyMock.reportMatcher(new LogEventMatches(level, messagePattern));
    return null;
  }

  /** The observer is used to hook into AppMon4J. */
  private AppMon4JObserver observer;

  /**
   * Asserts that the monitor has the specified value.
   *
   * @param   monitorName
   *          the name of the monitor value to check
   * @param   expectedValue
   *          the expected value
   */
  private void assertMonitor(String monitorName, long expectedValue) {
    Reportable reportable = this.observer.reportables.get(monitorName);
    long actualCount = 0;
    if (expectedValue > 0) {
      Assert.assertNotNull("There is no counter for [" + monitorName + "]!", reportable);
      if (reportable instanceof Counter) {
        actualCount = ((Counter) reportable).getCount();
      } else if (reportable instanceof Timer) {
        actualCount = ((Timer) reportable).getCount();
      } else if (reportable instanceof StateValueProvider) {
        actualCount = ((StateValueProvider) reportable).getValue();
      } else {
        actualCount = -1;
      }
    }
    Assert.assertEquals("Wrong count for monitor name [" + monitorName + "]!", expectedValue, actualCount);
  }

  private String expectLogEventWithLevelContaining(Level level, String message) {
    Appender appenderMock = givenAnAppenderAtTheMonitoringDataSourceLogger();

    appenderMock.doAppend(logEventMatches(level, "^.*" + message + ".*$"));
    expectLastCall();

    return message;
  }

  private String expectWarnLogEventCotaining(String message) {
    return expectLogEventWithLevelContaining(Level.WARN, message);
  }

  private Appender givenAnAppenderAtTheMonitoringDataSourceLogger() {
    Appender appenderMock = createMock(Appender.class);
    Logger logger = Logger.getLogger(MonitoringDataSource.class);
    logger.addAppender(appenderMock);
    return appenderMock;
  }

  /** Set up the test case. */
  @Before
  public void setUp() {
    this.observer = new AppMon4JObserver();
    InApplicationMonitor.getInstance().getCorePlugin().addReportableObserver(this.observer);
  }

  /** Clean up. */
  @After
  public void tearDown() {
    resetAll();
  }

  @Test
  public void shouldLogExecutedSqlWhenExceptionWasThrownForStatement() throws SQLException {
    String sql = expectWarnLogEventCotaining("SELECT 1 FROM DUAL");

    DataSource datasourceMock = createMock(DataSource.class);
    Connection connectionMock = createMock(Connection.class);
    Statement statementMock = createMock(Statement.class);
    expect(datasourceMock.getConnection()).andReturn(connectionMock);
    expect(connectionMock.createStatement()).andReturn(statementMock);
    expect(statementMock.executeQuery(sql)).andThrow(new SQLException(
        "Somebody set up us the bomb!"));

    MonitoringDataSource objectUnderTest = new MonitoringDataSource(datasourceMock);

    replayAll();

    Connection connection = objectUnderTest.getConnection();
    Statement statement = connection.createStatement();
    try {
      statement.executeQuery(sql);
      fail("Expectations have been screwed!");
    } catch (SQLException expected) {
    }
    verifyAll();
  }

  @Test
  public void shouldLogExecutedSqlWhenExceptionWasThrownForPreparedStatement() throws SQLException {
    String sql = expectWarnLogEventCotaining("SELECT 1 FROM DUAL");
    DataSource datasourceMock = createMock(DataSource.class);
    Connection connectionMock = createMock(Connection.class);
    PreparedStatement statementMock = createMock(PreparedStatement.class);
    expect(datasourceMock.getConnection()).andReturn(connectionMock);
    expect(connectionMock.prepareStatement(sql)).andReturn(statementMock);
    expect(statementMock.executeQuery()).andThrow(new SQLException(
        "Somebody set up us the bomb!"));

    MonitoringDataSource objectUnderTest = new MonitoringDataSource(datasourceMock);

    replayAll();

    Connection connection = objectUnderTest.getConnection();
    PreparedStatement statement = connection.prepareStatement(sql);
    try {
      statement.executeQuery();
      fail("Expectations have been screwed!");
    } catch (SQLException expected) {
    }
    verifyAll();
  }

  @Test
  public void shouldNotLogExecutedSqlWhenExceptionWasThrownDuringClose() throws SQLException {
    givenAnAppenderAtTheMonitoringDataSourceLogger();

    String sql = "SELECT 1 FROM DUAL";
    DataSource datasourceMock = createMock(DataSource.class);
    Connection connectionMock = createMock(Connection.class);
    PreparedStatement statementMock = createMock(PreparedStatement.class);
    expect(datasourceMock.getConnection()).andReturn(connectionMock);
    expect(connectionMock.prepareStatement(sql)).andReturn(statementMock);
    statementMock.close();
    expectLastCall().andThrow(new SQLException("Somebody set up us the bomb!"));

    MonitoringDataSource objectUnderTest = new MonitoringDataSource(datasourceMock);

    replayAll();

    Connection connection = objectUnderTest.getConnection();
    Statement statement = connection.prepareStatement(sql);
    try {
      statement.close();
      fail("Expectations have been screwed!");
    } catch (SQLException expected) {
    }

    verifyAll();
  }

  @Test
  public void shouldLogFilteredSqlExceptionAtInfoLevel() throws SQLException {
    String sql = expectLogEventWithLevelContaining(Level.INFO, "SELECT 1 FROM DUAL");
    DataSource datasourceMock = createMock(DataSource.class);
    Connection connectionMock = createMock(Connection.class);
    Statement statementMock = createMock(Statement.class);
    expect(datasourceMock.getConnection()).andReturn(connectionMock);
    expect(connectionMock.createStatement()).andReturn(statementMock);
    expect(statementMock.executeQuery(sql)).andThrow(new SQLException(
        "Somebody set up us the bomb!", "SomeState", 42));

    MonitoringDataSource objectUnderTest = new MonitoringDataSource(datasourceMock);
    objectUnderTest.addExceptionLogFilter(new MonitoringDataSource.Predicate<SQLException>() {
        @Override
        public boolean apply(SQLException input) {
          return input.getErrorCode() == 42;
        }
      });

    replayAll();

    Connection connection = objectUnderTest.getConnection();
    Statement statement = connection.createStatement();
    try {
      statement.executeQuery(sql);
      fail("Expectations have been screwed!");
    } catch (SQLException expected) {
    }
    verifyAll();
  }

  @Test
  public void shouldRecognizeExceptionLogFiltersAppropriately() throws SQLException {
    String sql = expectLogEventWithLevelContaining(Level.INFO, "SELECT 1 FROM DUAL");
    DataSource datasourceMock = createMock(DataSource.class);
    Connection connectionMock = createMock(Connection.class);
    Statement statementMock = createMock(Statement.class);
    expect(datasourceMock.getConnection()).andReturn(connectionMock);
    expect(connectionMock.createStatement()).andReturn(statementMock);
    expect(statementMock.executeQuery(sql)).andThrow(new SQLException(
        "Somebody set up us the bomb!", "SomeState", 42));

    MonitoringDataSource objectUnderTest = new MonitoringDataSource(datasourceMock);
    objectUnderTest.setExceptionLogFilters("1,42:^.*bomb.*$");

    replayAll();

    Connection connection = objectUnderTest.getConnection();
    Statement statement = connection.createStatement();
    try {
      statement.executeQuery(sql);
      fail("Expectations have been screwed!");
    } catch (SQLException expected) {
    }
    verifyAll();
  }

  @Test
  public void shouldFilterErrorCodeOfSqlExceptionsCorrectly() {
    SqlExceptionPredicate objectUnderTest = new MonitoringDataSource.SqlExceptionPredicate(1, null);

    assertTrue(objectUnderTest.apply(new SQLException("foo", "bar", 1)));
    assertTrue(objectUnderTest.apply(new SQLException("foo", null, 1)));
    assertTrue(objectUnderTest.apply(new SQLException(null, null, 1)));
    assertFalse(objectUnderTest.apply(new SQLException()));
    assertFalse(objectUnderTest.apply(new SQLException("foo")));
    assertFalse(objectUnderTest.apply(new SQLException("foo", "bar", 42)));
  }

  @Test
  public void shouldFilterErrorCodeAndMessageRegexOfSqlExceptionsCorrectly() {
    SqlExceptionPredicate objectUnderTest = new MonitoringDataSource.SqlExceptionPredicate(42, "^.*foo.*$");

    assertTrue(objectUnderTest.apply(new SQLException("blafoobla", "bar", 42)));
    assertTrue(objectUnderTest.apply(new SQLException("foo", "bar", 42)));
    assertTrue(objectUnderTest.apply(new SQLException("foo", null, 42)));
    assertFalse(objectUnderTest.apply(new SQLException(null, null, 42)));
    assertFalse(objectUnderTest.apply(new SQLException()));
    assertFalse(objectUnderTest.apply(new SQLException("foo")));
    assertFalse(objectUnderTest.apply(new SQLException("foo", "bar", 1)));
  }

  /** Verifies that the {@link MonitoringDataSource} makes the appropriate AppMon4J calls.
   *
   * @throws java.sql.SQLException
   */
  @Test
  public void checkErrorCounters() throws SQLException {
    AtomicBoolean errorSwitch = new AtomicBoolean(false);
    String baseName = "checkErrorCounter";
    MonitoringDataSource dataSource = new MonitoringDataSource(new MockDataSource(errorSwitch), baseName);
    assertMonitor(MonitoringDataSource.class.getName() + ".maxOpenConnections", 0);

    Connection c = dataSource.getConnection();
    errorSwitch.set(true);
    try {
      dataSource.getConnection();
      Assert.fail("Calling getConnection() should fail!");
    } catch (SQLException sqlE) {
      // we expect that
    }
    try {
      c.commit();
      Assert.fail("Calling commit() should fail!");
    } catch (SQLException sqlE) {
      // we expect that
    }
    try {
      c.rollback();
      Assert.fail("Calling rollback() should fail!");
    } catch (SQLException sqlE) {
      // we expect that
    }
    try {
      c.rollback(null);
      Assert.fail("Calling rollback(Savepoint) should fail!");
    } catch (SQLException sqlE) {
      // we expect that
    }
    c.close();

    assertMonitor(MonitoringDataSource.class.getName() + "." + baseName + ".error.getConnection", 1);
    assertMonitor(MonitoringDataSource.class.getName() + "." + baseName + ".getConnection", 1);
    assertMonitor(MonitoringDataSource.class.getName() + "." + baseName + ".getPersonalisedConnection", 0);
    assertMonitor(MonitoringDataSource.class.getName() + "." + baseName + ".error.commit", 1);
    assertMonitor(MonitoringDataSource.class.getName() + "." + baseName + ".error.rollback", 1);
    assertMonitor(MonitoringDataSource.class.getName() + "." + baseName + ".error.rollbackSavepoint", 1);
    assertMonitor(MonitoringDataSource.class.getName() + "." + baseName + ".close", 1);
    assertMonitor(MonitoringDataSource.class.getName() + "." + baseName + ".maxOpenConnections", 1);

    errorSwitch.set(false);
    c = dataSource.getConnection("me", "secret");
    errorSwitch.set(true);
    try {
      dataSource.getConnection("me", "secret");
      Assert.fail("Calling getConnection(String, String) should fail!");
    } catch (SQLException sqlE) {
      // we expect that
    }
    try {
      c.commit();
      Assert.fail("Calling commit() should fail!");
    } catch (SQLException sqlE) {
      // we expect that
    }
    try {
      c.rollback();
      Assert.fail("Calling rollback() should fail!");
    } catch (SQLException sqlE) {
      // we expect that
    }
    try {
      c.rollback(null);
      Assert.fail("Calling rollback(Savepoint) should fail!");
    } catch (SQLException sqlE) {
      // we expect that
    }
    c.close();

    assertMonitor(MonitoringDataSource.class.getName() + "." + baseName + ".error.getConnection", 2);
    assertMonitor(MonitoringDataSource.class.getName() + "." + baseName + ".getConnection", 1);
    assertMonitor(MonitoringDataSource.class.getName() + "." + baseName + ".getPersonalisedConnection", 1);
    assertMonitor(MonitoringDataSource.class.getName() + "." + baseName + ".error.commit", 2);
    assertMonitor(MonitoringDataSource.class.getName() + "." + baseName + ".error.rollback", 2);
    assertMonitor(MonitoringDataSource.class.getName() + "." + baseName + ".error.rollbackSavepoint", 2);
    assertMonitor(MonitoringDataSource.class.getName() + "." + baseName + ".close", 2);
    assertMonitor(MonitoringDataSource.class.getName() + "." + baseName + ".maxOpenConnections", 1);
  }

  /**
   * Used to hook into AppMon4J for JUnit tests.
   *
   * @author  <a href="mailto:SKirsch@is24.de">Sebastian Kirsch</a>
   * @see     MonitoringDataSourceTest
   */
  private static class AppMon4JObserver implements ReportableObserver {
    public final ConcurrentHashMap<String, Reportable> reportables = new ConcurrentHashMap<String, Reportable>();

    public void addNewReportable(Reportable reportable) {
      reportables.put(reportable.getName(), reportable);
    }
  }

  /**
   * Mocks a {@link javax.sql.DataSource} for JUnit tests.
   *
   * @author  <a href="mailto:SKirsch@is24.de">Sebastian Kirsch</a>
   * @see     MonitoringDataSourceTest
   */
  private static class MockDataSource implements DataSource {
    private final AtomicBoolean throwError;

    public MockDataSource(AtomicBoolean throwError) {
      this.throwError = throwError;
    }

    public Connection getConnection() throws SQLException {
      if (throwError.get()) {
        throw new SQLException("Somebody set up us the bomb!", "All your base");
      }
      return new MockConnection(this.throwError);
    }

    public Connection getConnection(String username, String password) throws SQLException {
      if (throwError.get()) {
        throw new SQLException("Somebody set up us the bomb!", "All your base");
      }
      return new MockConnection(this.throwError);
    }

    public PrintWriter getLogWriter() throws SQLException {
      return null;
    }

    public int getLoginTimeout() throws SQLException {
      return 0;
    }

    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
      return null;
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
    }

    public void setLoginTimeout(int seconds) throws SQLException {
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
      return false;
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
      return null;
    }
  }

  /**
   * Mocks a {@link java.sql.Connection} for JUnit tests.
   *
   * @author  <a href="mailto:SKirsch@is24.de">Sebastian Kirsch</a>
   * @see     MonitoringDataSourceTest
   */
  private static class MockConnection implements Connection {
    private final AtomicBoolean throwError;

    public MockConnection(AtomicBoolean throwError) {
      this.throwError = throwError;
    }

    public void clearWarnings() throws SQLException {
    }

    public void close() throws SQLException {
    }

    public void commit() throws SQLException {
      if (throwError.get()) {
        throw new SQLException("Somebody set up us the bomb!", "All your base");
      }
    }

    public Statement createStatement() throws SQLException {
      return null;
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
      return null;
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
                              throws SQLException {
      return null;
    }

    public boolean getAutoCommit() throws SQLException {
      return false;
    }

    public String getCatalog() throws SQLException {
      return null;
    }

    public int getHoldability() throws SQLException {
      return 0;
    }

    public DatabaseMetaData getMetaData() throws SQLException {
      return null;
    }

    public int getTransactionIsolation() throws SQLException {
      return 0;
    }

    public Map<String, Class<?>> getTypeMap() throws SQLException {
      return null;
    }

    public SQLWarning getWarnings() throws SQLException {
      return null;
    }

    public boolean isClosed() throws SQLException {
      return false;
    }

    public boolean isReadOnly() throws SQLException {
      return false;
    }

    public String nativeSQL(String sql) throws SQLException {
      return null;
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
      return null;
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
      return null;
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
                                         int resultSetHoldability) throws SQLException {
      return null;
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
      return null;
    }

    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
      return null;
    }

    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
      return null;
    }

    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
      return null;
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
                                       throws SQLException {
      return null;
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
                                              int resultSetHoldability) throws SQLException {
      return null;
    }

    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
    }

    public void rollback() throws SQLException {
      if (throwError.get()) {
        throw new SQLException("Somebody set up us the bomb!", "All your base");
      }
    }

    public void rollback(Savepoint savepoint) throws SQLException {
      if (throwError.get()) {
        throw new SQLException("Somebody set up us the bomb!", "All your base");
      }
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
    }

    public void setCatalog(String catalog) throws SQLException {
    }

    public void setHoldability(int holdability) throws SQLException {
    }

    public void setReadOnly(boolean readOnly) throws SQLException {
    }

    public Savepoint setSavepoint() throws SQLException {
      return null;
    }

    public Savepoint setSavepoint(String name) throws SQLException {
      return null;
    }

    public void setTransactionIsolation(int level) throws SQLException {
    }

    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
    }

    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
      return null;
    }

    public Blob createBlob() throws SQLException {
      return null;
    }

    public Clob createClob() throws SQLException {
      return null;
    }

    public NClob createNClob() throws SQLException {
      return null;
    }

    public SQLXML createSQLXML() throws SQLException {
      return null;
    }

    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
      return null;
    }

    public void setSchema(String schema) throws SQLException {
    }

    public String getSchema() throws SQLException {
      return null;
    }

    public void abort(Executor executor) throws SQLException {
    }

    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
    }

    public int getNetworkTimeout() throws SQLException {
      return 0;
    }

    public Properties getClientInfo() throws SQLException {
      return null;
    }

    public String getClientInfo(String name) throws SQLException {
      return null;
    }

    public boolean isValid(int timeout) throws SQLException {
      return false;
    }

    public void setClientInfo(Properties properties) throws SQLClientInfoException {
    }

    public void setClientInfo(String name, String value) throws SQLClientInfoException {
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
      return false;
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
      return null;
    }
  }

  private static class LogEventMatches implements IArgumentMatcher {
    private final Level expectedLevel;
    private final String messagePattern;

    public LogEventMatches(Level expectedLevel, String messagePattern) {
      this.expectedLevel = expectedLevel;
      if ((messagePattern == null) || (messagePattern.length() == 0)) {
        throw new IllegalArgumentException("messagePattern must not be empty");
      }
      this.messagePattern = messagePattern;
    }

    @Override
    public void appendTo(StringBuffer buffer) {
      buffer.append("logEventMatches(");
      if (this.expectedLevel != null) {
        buffer.append(this.expectedLevel).append("|");
      }
      buffer.append("\"").append(this.messagePattern).append("\")");
    }

    @Override
    public boolean matches(Object argument) {
      if (!LoggingEvent.class.isInstance(argument)) {
        return false;
      }

      LoggingEvent logEvent = (LoggingEvent) argument;
      if ((this.expectedLevel != null) && !this.expectedLevel.equals(logEvent.getLevel())) {
        return false;
      }

      return (logEvent.getMessage() != null) && logEvent.getMessage().toString().matches(messagePattern);
    }

  }
}
