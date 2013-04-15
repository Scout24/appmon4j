package de.is24.util.monitoring.jmx;

import de.is24.util.monitoring.State;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.Map;
import static org.fest.assertions.Assertions.assertThat;


public class JMXExporterTest {
  private final Logger LOGGER = Logger.getLogger(JMXExporter.class);
  private ObjectName objectName;
  private static final String TEST_BEAN_DOMAIN = "JXMExporterTest";
  private TestMBean testMBean;

  @Before
  public void setup() throws MalformedObjectNameException, MBeanRegistrationException, InstanceAlreadyExistsException,
                             NotCompliantMBeanException {
    objectName = new ObjectName(TEST_BEAN_DOMAIN, "name", "testBean");

    testMBean = new TestMBean();
    ManagementFactory.getPlatformMBeanServer().registerMBean(testMBean, objectName);
  }

  @After
  public void tearDown() throws InstanceNotFoundException, MBeanRegistrationException {
    MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
    if (beanServer.isRegistered(objectName)) {
      beanServer.unregisterMBean(objectName);
    }

  }

  @Test
  public void shouldFindMbeansByDomain() {
    JMXExporter jmxExporter = new JMXExporter(TEST_BEAN_DOMAIN);
    Map<ObjectName, MBeanInfo> beanInfos = jmxExporter.getMBeanInfos();
    assertThat(beanInfos.size()).isEqualTo(1);
  }

  @Test
  public void logNumericAttributes() {
    JMXExporter jmxExporter = new JMXExporter(TEST_BEAN_DOMAIN);

    Collection<State> values = jmxExporter.getValues();
    assertThat(values.size()).isEqualTo(6);
  }

  @Test
  public void convertToLong() throws MalformedObjectNameException, MBeanRegistrationException,
                                     InstanceAlreadyExistsException, NotCompliantMBeanException {
    JMXExporter jmxExporter = new JMXExporter(TEST_BEAN_DOMAIN);
    Collection<State> values = jmxExporter.getValues();

    assertValue(values, "long", 1234567890L);

    assertValue(values, "int", 65536L);
    assertValue(values, "double", 1);
    assertValue(values, "float", 2);
    assertValue(values, "short", 17);
    assertValue(values, "boolean", 1);

  }

  @Test
  public void lala() {
    JMXExporter jmxExporter = new JMXExporter("java.lang");
    Collection<State> values = jmxExporter.getValues();
  }

  private void assertValue(Collection<State> values, String name, long targetValue) {
    int checked = 0;
    for (State state : values) {
      if (state.name.contains(name)) {
        assertThat(state.value).isEqualTo(targetValue);
        checked++;
      }
    }
    assertThat(checked).isEqualTo(1);
  }


  private class TestMBean implements DynamicMBean {
    @Override
    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException,
                                                         MBeanException, ReflectionException {
    }

    @Override
    public AttributeList getAttributes(String[] attributes) {
      return null;
    }

    @Override
    public AttributeList setAttributes(AttributeList attributes) {
      return null;
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException,
                                                                                        ReflectionException {
      return null;
    }

    @Override
    public MBeanInfo getMBeanInfo() {
      return new MBeanInfo("TestMBean",
        "", getAttributeInfos(), null, null, null);
    }

    public MBeanAttributeInfo[] getAttributeInfos() {
      return new MBeanAttributeInfo[] {
          new MBeanAttributeInfo("long", "long", "long", true, false, false),
          new MBeanAttributeInfo("int", "int", "int", true, false, false),
          new MBeanAttributeInfo("double", "double", "double", true, false, false),
          new MBeanAttributeInfo("float", "float", "float", true, false, false),
          new MBeanAttributeInfo("short", "short", "short", true, false, false),
          new MBeanAttributeInfo("boolean", "boolean", "boolean", true, false, false),
          new MBeanAttributeInfo("string", "java.lang.String", "string", true, false, false)
        };
    }

    /* (non-Javadoc)
    * @see de.is24.util.monitoring.jmx.JmxReportable#getAttribute(java.lang.String)
    */
    public Object getAttribute(String attributeName) {
      if (attributeName.equals("long")) {
        return 1234567890L;
      } else if (attributeName.equals("int")) {
        return 65536;
      } else if (attributeName.equals("double")) {
        return 1.73;
      } else if (attributeName.equals("float")) {
        return 2.48f;
      } else if (attributeName.equals("short")) {
        return (short) 17;
      } else if (attributeName.equals("boolean")) {
        return true;
      } else if (attributeName.equals("string")) {
        return "string";
      }
      return null;
    }
  }
}
