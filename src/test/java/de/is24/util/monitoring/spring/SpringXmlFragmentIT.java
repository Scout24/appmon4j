package de.is24.util.monitoring.spring;

import de.is24.util.monitoring.CorePlugin;
import de.is24.util.monitoring.InApplicationMonitor;
import de.is24.util.monitoring.TestingInApplicationMonitor;
import de.is24.util.monitoring.state2graphite.StateValuesToGraphite;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import static org.fest.assertions.Assertions.assertThat;


public class SpringXmlFragmentIT {
  @Before
  public void setup() {
    TestingInApplicationMonitor.resetInstanceForTesting();
  }

  @Test
  public void webFragmentShouldLoad() throws Exception {
    ClassPathXmlApplicationContext classPathXmlApplicationContext = new ClassPathXmlApplicationContext(
      "classpath:appmon4jWeb.spring.xml");
  }

  @Test
  public void standaloneFragmentShouldLoad() throws Exception {
    ClassPathXmlApplicationContext classPathXmlApplicationContext = new ClassPathXmlApplicationContext(
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


}
