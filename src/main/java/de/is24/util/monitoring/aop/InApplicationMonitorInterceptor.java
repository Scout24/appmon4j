package de.is24.util.monitoring.aop;

import de.is24.util.monitoring.PerfTimer;
import de.is24.util.monitoring.PerfTimer.PerfMonitor;
import de.is24.util.monitoring.measurement.MeasurementHandler;
import de.is24.util.monitoring.measurement.TimerMeasurementHandler;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * This class implements a {@link org.aopalliance.intercept.MethodInterceptor} and measures the time the method invocation needs.
 * That value is then reported to the InApplicationMonitor. The name of the value is the full
 * qualified class name followed by the methods name.
 *
 * If a prefix is set the prefix is put before the monitor name, e.g. prefix:monitorname
 *
 */
public class InApplicationMonitorInterceptor implements MethodInterceptor {
  private static final Logger LOG = Logger.getLogger(InApplicationMonitorInterceptor.class);

  private String prefix;
  MeasurementHandler[] handlers = new MeasurementHandler[] { new TimerMeasurementHandler() };

  public void setHandlers(MeasurementHandler[] handlers) {
    this.handlers = handlers;
  }

  /**
  * {@inheritDoc}
  */
  public Object invoke(MethodInvocation mi) /* Honoring the interface earns you a punch in the face ...
                                            CSOFF: IllegalThrows */ throws Throwable /* CSON: IllegalThrows */ {
    String monitorName = getMonitorName(mi);
    final PerfMonitor monitor = PerfTimer.createMonitor();
    try {
      if (LOG.isDebugEnabled()) {
        LOG.debug("enter [" + monitorName + "]");
      }
      return mi.proceed();
    } finally {
      if (LOG.isDebugEnabled()) {
        LOG.debug("leave [" + monitorName + "]");
      }

      monitor.handleMeasurement(monitorName, handlers);
    }
  }

  protected String getMonitorName(MethodInvocation mi) {
    final StringBuilder builder = new StringBuilder();

    if (StringUtils.isNotEmpty(prefix)) {
      builder.append(prefix).append(":");
    }

    builder.append(mi.getThis().getClass().getName());
    builder.append(".");
    builder.append(mi.getMethod().getName());

    return builder.toString();
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

}
