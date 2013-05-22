package de.is24.util.monitoring.spring;

import de.is24.util.monitoring.Counter;
import de.is24.util.monitoring.InApplicationMonitor;
import de.is24.util.monitoring.TestHelper;
import de.is24.util.monitoring.Timer;
import de.is24.util.monitoring.tools.DoNothingReportVisitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import static de.is24.util.monitoring.spring.MonitoringHandlerInterceptor.POST_HANDLE_TIME;
import static de.is24.util.monitoring.spring.MonitoringHandlerInterceptor.START_TIME;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


public class MonitoringHandlerInterceptorTest {
  private static final String PREFIX = "MonitoringHandlerInterceptor.";
  private static final String HANDLING = ".handling";
  private static final String RENDERING = ".rendering";
  private static final String COMPLETE = ".complete";
  private static final String TIME_ERROR = ".timeError";
  private static final String DUPLICATE_HANDLER = ".duplicateHandler";

  private static final int SLEEP_TIME = 100;

  private Map<String, Long> counterCalled;
  private Map<String, Timer> timerMap;
  private InApplicationMonitor monitor;
  private MonitoringHandlerInterceptor interceptor;

  @Before
  public void setup() {
    monitor = TestHelper.setInstanceForTesting();
    interceptor = new MonitoringHandlerInterceptor();
  }

  @After
  public void teaDown() {
    TestHelper.resetInstanceForTesting();
  }

  @Test
  public void shouldMeasureDurations() throws Exception {
    HttpServletRequest request = new MockHttpServletRequest();
    Object handlerInstance = new Object();

    interceptor.preHandle(request, null, handlerInstance);
    Thread.sleep(SLEEP_TIME);
    interceptor.postHandle(request, null, handlerInstance, null);
    Thread.sleep(SLEEP_TIME);
    interceptor.afterCompletion(request, null, handlerInstance, null);

    fillCounterAndTimerMap();

    // measuring times on windows machines is a little tricky since the internal clock
    // does not roll with every ms tick but only every 15th or 16th tick
    assertTimer(timerMap, handlerInstance, HANDLING, SLEEP_TIME);
    assertTimer(timerMap, handlerInstance, RENDERING, SLEEP_TIME);
    assertTimer(timerMap, handlerInstance, COMPLETE, 2 * SLEEP_TIME);
  }

  @Test
  public void shouldStripOfCGLIBEnhancerIdFromKey() {
    Object handlerClass = new FeedbackController$$EnhancerByCGLIB$$700793d4();

    String prefix = interceptor.getPrefix(handlerClass);

    assertEquals(
      "MonitoringHandlerInterceptor.de.is24.util.monitoring.spring.MonitoringHandlerInterceptorTest$FeedbackControllerEnhancerByCGLIB_IdStripped",
      prefix);
  }

  @Test
  public void shouldNotMeasureInCaseOfErrorInActionPhase() throws Exception {
    HttpServletRequest request = new MockHttpServletRequest();
    Object handlerInstance = new Long(1L);

    interceptor.preHandle(request, null, handlerInstance);
    Thread.sleep(SLEEP_TIME);
    interceptor.afterCompletion(request, null, handlerInstance, null);

    fillCounterAndTimerMap();

    assertNoMeasurement(timerMap, handlerInstance, HANDLING);
    assertNoMeasurement(timerMap, handlerInstance, RENDERING);
    assertNoMeasurement(timerMap, handlerInstance, COMPLETE);
  }

  @Test
  public void shouldNotMeasureInCasePreHandleWasNotCalledButIncrementErrorCounter() throws Exception {
    HttpServletRequest request = new MockHttpServletRequest();
    Object handlerInstance = new Integer(1);

    Thread.sleep(SLEEP_TIME);
    interceptor.afterCompletion(request, null, handlerInstance, null);

    fillCounterAndTimerMap();

    assertThat(counterCalled.get(PREFIX + handlerInstance.getClass().getName() + TIME_ERROR), is(1L));
    assertNoMeasurement(timerMap, handlerInstance, HANDLING);
    assertNoMeasurement(timerMap, handlerInstance, RENDERING);
    assertNoMeasurement(timerMap, handlerInstance, COMPLETE);
  }

  @Test
  public void shouldDetectDuplicateInvocation() throws Exception {
    HttpServletRequest request = new MockHttpServletRequest();
    Object handlerInstance = new Integer(1);

    interceptor.preHandle(request, null, handlerInstance);
    interceptor.preHandle(request, null, handlerInstance);

    fillCounterAndTimerMap();

    assertThat(counterCalled.get(PREFIX + handlerInstance.getClass().getName() + DUPLICATE_HANDLER), is(1L));
  }


  @Test
  public void shouldRemoveStartAndPostHandleTime() throws Exception {
    HttpServletRequest request = new MockHttpServletRequest();
    request.setAttribute(START_TIME, 123L);
    request.setAttribute(POST_HANDLE_TIME, 123L);

    interceptor.afterCompletion(request, null, new String(), null);

    assertThat(request.getAttribute(START_TIME), is(nullValue()));
    assertThat(request.getAttribute(POST_HANDLE_TIME), is(nullValue()));
  }

  @Test
  public void shouldRemovePostHandleTimeWhenStartTimeIsNotSet() throws Exception {
    HttpServletRequest request = new MockHttpServletRequest();
    request.setAttribute(POST_HANDLE_TIME, 123L);

    interceptor.afterCompletion(request, null, new Float(0.1), null);

    assertThat(request.getAttribute(POST_HANDLE_TIME), is(nullValue()));
  }


  private void assertNoMeasurement(Map<String, Timer> timerMap, Object handlerInstance, String name) {
    String timerFullName = PREFIX + handlerInstance.getClass().getName() + name;
    assertTrue(!timerMap.containsKey(timerFullName));
  }

  private void fillCounterAndTimerMap() {
    counterCalled = new HashMap<String, Long>();
    timerMap = new HashMap<String, Timer>();
    monitor.getCorePlugin().reportInto(new DoNothingReportVisitor() {
        @Override
        public void reportCounter(Counter counter) {
          counterCalled.put(counter.getName(), counter.getCount());
        }

        @Override
        public void reportTimer(Timer timer) {
          timerMap.put(timer.getName(), timer);
        }

      });
  }

  private void assertTimer(Map<String, Timer> timerMap, Object handlerInstance, String timerName, int time) {
    String timerFullName = PREFIX + handlerInstance.getClass().getName() + timerName;
    Timer timer = timerMap.get(timerFullName);
    assertNotNull(timer);
    assertEquals(1, timer.getCount());
    assertThat(new Double(time), closeTo(time, 16 /* tick diff on windows machines*/));
  }

  public static class FeedbackController$$EnhancerByCGLIB$$700793d4 {
  }
}
