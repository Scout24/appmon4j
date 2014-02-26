package de.is24.util.monitoring.wrapper;

import de.is24.util.monitoring.InApplicationMonitor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


/**
 * This class provides the ability to wrap arbitrary objects and monitor the
 * execution times of methods invoked on the object. The wrapped object must be
 * described by an interface so that the wrapper can generate a proxy object
 * during runtime.
 *
 * <p>
 * The wrapper can be configured with a {@link TimingReporter} that is used to
 * report the timinig measurement of a single method invocation. A default
 * implementation based on the {@link de.is24.util.monitoring.InApplicationMonitor} is provided.
 * </p>
 *
 * <p>
 * Use one of the static {@link #wrapObject(Class, Object)} or
 * {@link #wrapObject(Class, Object, TimingReporter)} factory methods to wrap a
 * given object. Here is a small code snipped demonstrating the use:
 *
 * <pre>
 * final Connection connection = dataSource.getConnection();
 * final wrappedConnection = GenericMonitoringWrapper.wrapObject(Connection.class, connection);
 * </pre>
 *
 * Now, if you make method calls on the <code>wrappedConnection</code> object,
 * each method invocation will be reported to the {@link de.is24.util.monitoring.InApplicationMonitor},
 * as the {@link InApplicationMonitorTimingReporter} is the default reporter to
 * use in case none is given.
 * </p>
 *
 * @see de.is24.util.monitoring.InApplicationMonitor
 * @see java.lang.reflect.Proxy
 *
 * @author Alexander Metzner
 *
 * @param <E>
 *          the interface of the object being wrapped.
 */
public class GenericMonitoringWrapper<E> implements InvocationHandler {
  /** Stores the interface the wrapped object want's to expose. */
  private final Class<E> targetClass;

  /** The target object being wrapped. */
  private final Object target;

  /** The reported used to report method invocation timings. */
  private final TimingReporter reporter;

  /**
   * Wraps the given object and returns the reporting proxy. Uses the
   * {@link InApplicationMonitorTimingReporter} to report the timings.
   *
   * @param <E>
   *          the type of the public interface of the wrapped object
   * @param clazz
   *          the class object to the interface
   * @param target
   *          the object to wrap
   * @return the monitoring wrapper
   */
  public static <E> E wrapObject(final Class<E> clazz, final Object target) {
    return wrapObject(clazz, target, new InApplicationMonitorTimingReporter());
  }

  /**
   * Wraps the given object and returns the reporting proxy. Uses the given
   * timing reporter to report timings.
   *
   * @param <E>
   *          the type of the public interface of the wrapped object
   * @param clazz
   *          the class object to the interface
   * @param target
   *          the object to wrap
   * @param timingReporter
   *          the reporter to report timing information to
   * @return the monitoring wrapper
   */
  @SuppressWarnings("unchecked")
  public static <E> E wrapObject(final Class<E> clazz, final Object target, final TimingReporter timingReporter) {
    return (E) Proxy.newProxyInstance(GenericMonitoringWrapper.class.getClassLoader(), new Class[] { clazz },
      new GenericMonitoringWrapper<E>(clazz, target, timingReporter));
  }

  /**
   * Constructs a new wrapper object. Use internally by the static
   * {@link #wrapObject(Class, Object)} factory methods.
   *
   * @param targetClass
   *          the class object of the public interface to wrap
   * @param target
   *          the target object to wrap
   * @param timingReporter
   *          the timing reporter
   */
  protected GenericMonitoringWrapper(final Class<E> targetClass, final Object target,
                                     final TimingReporter timingReporter) {
    this.targetClass = targetClass;
    this.target = target;
    this.reporter = timingReporter;
  }

  /**
   * Handles method invocations on the generated proxy. Measures the time needed
   * to execute the given method on the wrapped object.
   *
   * @see java.lang.reflect.InvocationHandler#invoke(Object, java.lang.reflect.Method, Object[])
   */
  public Object invoke(Object proxy, Method method, Object[] args) /* CSOFF: IllegalThrows */
                throws Throwable /* CSON: IllegalThrows */ {
    final long startTime = System.currentTimeMillis();
    Object result = null;
    try {
      result = method.invoke(target, args);
      if ((result != null) && !method.getReturnType().equals(Void.TYPE) && method.getReturnType().isInterface()) {
        result = wrapObject(method.getReturnType(), result, reporter);
      }
    } catch (InvocationTargetException t) {
      if (t.getCause() != null) {
        throw t.getCause();
      }
    } finally {
      final long endTime = System.currentTimeMillis();
      reporter.reportTimedOperation(targetClass, method, startTime, endTime);
    }
    return result;
  }

  /**
   * Interface for objects that receive and handle timinig information for a
   * given method invocation.
   *
   * @author Alexander Metzner
   *
   */
  public static interface TimingReporter {
    void reportTimedOperation(Class<?> targetClass, Method targetMethod, long startTime, long endTime);
  }

  /**
   * Default implementation of the {@link TimingReporter} interface that uses
   * the {@link de.is24.util.monitoring.InApplicationMonitor} as it's backend.
   *
   * @author Alexander Metzner
   *
   */
  public static class InApplicationMonitorTimingReporter implements TimingReporter {
    public void reportTimedOperation(Class<?> targetClass, Method targetMethod, long startTime, long endTime) {
      InApplicationMonitor.getInstance()
      .addTimerMeasurement(targetClass.getName() + "." + targetMethod.getName(),
        startTime, endTime);
    }
  }
}
