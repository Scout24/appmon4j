package de.is24.util.monitoring.spring;

import de.is24.util.monitoring.TestingInApplicationMonitor;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;


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
  }


}
