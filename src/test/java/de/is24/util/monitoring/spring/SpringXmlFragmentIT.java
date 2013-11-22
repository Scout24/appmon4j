package de.is24.util.monitoring.spring;

import de.is24.util.monitoring.CorePlugin;
import de.is24.util.monitoring.InApplicationMonitor;
import de.is24.util.monitoring.MonitorPlugin;
import de.is24.util.monitoring.TestingInApplicationMonitor;
import de.is24.util.monitoring.state2graphite.StateValuesToGraphite;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import java.util.List;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


public class SpringXmlFragmentIT {
  @Before
  public void setup() {
    TestingInApplicationMonitor.resetInstanceForTesting();
  }

  @Test
  public void webFragmentShouldLoad() throws Exception {
    new ClassPathXmlApplicationContext(
      "classpath:appmon4jWeb.spring.xml");
  }

  @Test
  public void standaloneFragmentShouldLoad() throws Exception {
    new ClassPathXmlApplicationContext(
      "classpath:appmon4jStandalone.spring.xml");
  }

  @Test
  public void graphiteShouldLoad() throws Exception {
    ClassPathXmlApplicationContext classPathXmlApplicationContext = new ClassPathXmlApplicationContext(
      "classpath:propertyPlaceholder.spring.xml", "classpath:appmon4jWeb.spring.xml",
      "classpath:appmon4jState2Graphite.spring.xml");
    CorePlugin corePlugin = InApplicationMonitor.getInstance().getCorePlugin();
    assertThat(corePlugin.getMultiValueProvider("JMXExporter")).isNotNull();

    assertThat(((StateValuesToGraphite) classPathXmlApplicationContext.getBean("appmon4j.stateValuesToGraphite"))
      .multiValueProviderCount()).isEqualTo(2);
  }

  @Test
  public void saveStatsdPluginShouldLoad() throws Exception {
    ClassPathXmlApplicationContext classPathXmlApplicationContext = new ClassPathXmlApplicationContext(
      "classpath:propertyPlaceholder.spring.xml",
      "classpath:appmon4jWeb.spring.xml",
      "classpath:appmon4jSaveStatsd.spring.xml");
    List<String> registeredPluginKeys = InApplicationMonitor.getInstance().getRegisteredPluginKeys();
    assertThat(registeredPluginKeys).isNotNull();
    assertThat(registeredPluginKeys.get(1)).isEqualTo("StatsdPlugin_localhost_8125_1.0");


  }

  @Test
  public void methodTimeMeasurementAspectShouldWork() throws Exception {
    ClassPathXmlApplicationContext classPathXmlApplicationContext = new ClassPathXmlApplicationContext(
      "classpath:propertyPlaceholder.spring.xml",
      "classpath:appmon4jStandalone.spring.xml",
      "classpath:appmon4jAopTimeMeasurement.spring.xml",
      "classpath:timeMeasurementAnnotatedBeans.spring.xml");

    MethodAnnotated methodAnnotatedBean = (MethodAnnotated) classPathXmlApplicationContext.getBean(
      "appmon4j.test.methodAnnotated");
    MonitorPlugin monitorPlugin = mock(MonitorPlugin.class);
    InApplicationMonitor.getInstance().registerPlugin(monitorPlugin);

    methodAnnotatedBean.methodOne();
    methodAnnotatedBean.methodTwo("lala");

    verify(monitorPlugin, times(1)).addTimerMeasurement(eq("de.is24.util.monitoring.spring.MethodAnnotated.methodOne"),
      anyLong());

    verify(monitorPlugin, times(1)).addTimerMeasurement(eq("de.is24.util.monitoring.spring.MethodAnnotated.methodTwo"),
      anyLong());

  }


  @Test
  public void classLevelTimeMeasurementAspectShouldWork() throws Exception {
    ClassPathXmlApplicationContext classPathXmlApplicationContext = new ClassPathXmlApplicationContext(
      "classpath:propertyPlaceholder.spring.xml",
      "classpath:appmon4jStandalone.spring.xml",
      "classpath:appmon4jAopTimeMeasurement.spring.xml",
      "classpath:timeMeasurementAnnotatedBeans.spring.xml");

    ClassAnnotated classAnnotatedBean = (ClassAnnotated) classPathXmlApplicationContext.getBean(
      "appmon4j.test.classAnnotated");
    MonitorPlugin monitorPlugin = mock(MonitorPlugin.class);
    InApplicationMonitor.getInstance().registerPlugin(monitorPlugin);

    classAnnotatedBean.methodOne();
    classAnnotatedBean.methodTwo("lala");

    verify(monitorPlugin, times(1)).addTimerMeasurement(eq("de.is24.util.monitoring.spring.ClassAnnotated.methodOne"),
      anyLong());

    verify(monitorPlugin, times(1)).addTimerMeasurement(eq("de.is24.util.monitoring.spring.ClassAnnotated.methodTwo"),
      anyLong());

  }

}
