package de.is24.util.monitoring.spring;

import de.is24.util.monitoring.InApplicationMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.regex.Pattern;


/** {@link org.springframework.web.servlet.HandlerInterceptor} to monitor duration of request processing **/
public class MonitoringHandlerInterceptor implements HandlerInterceptor {
  private static final Logger LOG = LoggerFactory.getLogger(MonitoringHandlerInterceptor.class);
  private static final String PREFIX = "MonitoringHandlerInterceptor.";
  static final String START_TIME = PREFIX + "startTime";
  static final String POST_HANDLE_TIME = PREFIX + "postHandleTime";
  private static final String HANDLING = ".handling";
  private static final String RENDERING = ".rendering";
  private static final String COMPLETE = ".complete";
  private static final String ERROR = ".error";
  private static final String TIME_ERROR = ".timeError";
  private static final String DUPLICATE_HANDLER = ".duplicateHandler";
  private InApplicationMonitor monitor = InApplicationMonitor.getInstance();
  private static final Pattern CGLIB_PATTERN = Pattern.compile("[$]*EnhancerByCGLIB[0-9a-z$]*");

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    if (request.getAttribute(START_TIME) != null) {
      String prefix = getPrefix(handler);
      LOG.warn("Looks like MonitoringHandlerInterceptor is registered twice for request " + request.getRequestURI() +
        " Handler Info: " + prefix);
      monitor.incrementCounter(prefix + DUPLICATE_HANDLER);
    }
    request.setAttribute(START_TIME, System.currentTimeMillis());
    return true;
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                         ModelAndView modelAndView) throws Exception {
    long currentTime = System.currentTimeMillis();
    long startTime = (Long) request.getAttribute(START_TIME);

    monitor.addTimerMeasurement(getPrefix(handler) + HANDLING,
      startTime, currentTime);
    request.setAttribute(POST_HANDLE_TIME, currentTime);
  }


  /**
   * check, whether {@link #POST_HANDLE_TIME} is set, and {@link de.is24.util.monitoring.InApplicationMonitor#addTimerMeasurement(String, long, long) add timer measuremets} for post phase and complete request.
   */
  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
                       throws Exception {
    long currentTime = System.currentTimeMillis();
    String measurementPrefix = getPrefix(handler);

    Object startTimeAttribute = getAndRemoveAttribute(request, START_TIME);
    Object postHandleObject = getAndRemoveAttribute(request, POST_HANDLE_TIME);

    if (startTimeAttribute == null) {
      LOG.info("Could not find start_time. Something went wrong with handler: " + measurementPrefix);
      monitor.incrementCounter(measurementPrefix + TIME_ERROR);
      return;
    }

    long startTime = (Long) startTimeAttribute;
    if (ex != null) {
      monitor.addTimerMeasurement(measurementPrefix + ERROR, startTime, currentTime);
    } else {
      if (postHandleObject != null) {
        long postHandleTime = (Long) postHandleObject;

        monitor.addTimerMeasurement(measurementPrefix + RENDERING, postHandleTime, currentTime);
        monitor.addTimerMeasurement(measurementPrefix + COMPLETE, startTime, currentTime);
      }
    }
  }

  private Object getAndRemoveAttribute(HttpServletRequest request, String attributeName) {
    Object attribute = request.getAttribute(attributeName);
    if (attribute != null) {
      request.removeAttribute(attributeName);
    }

    return attribute;
  }

  protected String getPrefix(Object handler) {
    String key;
    if (handler instanceof HandlerMethod) {
      HandlerMethod handlerMethod = (HandlerMethod) handler;
      key = PREFIX + handlerMethod.getBeanType().getName().replaceAll("\\.", "_") + "." +
        handlerMethod.getMethod().getName();
    } else {
      key = PREFIX + handler.getClass().getName();
    }
    key = CGLIB_PATTERN.matcher(key).replaceAll("EnhancerByCGLIB_IdStripped");
    return key;
  }

}
