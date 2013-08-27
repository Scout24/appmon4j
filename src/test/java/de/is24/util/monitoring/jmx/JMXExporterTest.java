package de.is24.util.monitoring.jmx;

import de.is24.util.monitoring.State;
import de.is24.util.monitoring.keyhandler.ValidatingKeyHandler;
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
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.Collection;
import static org.fest.assertions.Assertions.assertThat;


public class JMXExporterTest {
  private ObjectName objectName;
  private static final String TEST_BEAN_DOMAIN = "JXMExporterTest";
  private TestMBean testMBean;
  private static final String JAVA_LANG = "java.lang";

  @Before
  public void setup() throws MalformedObjectNameException, MBeanRegistrationException, InstanceAlreadyExistsException,
                             NotCompliantMBeanException, OpenDataException {
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
  public void doNotFailIfSomeOperationIsNotSupported() throws MalformedObjectNameException {
    JMXExporter jmxExporter = new JMXExporter(TEST_BEAN_DOMAIN + ":*");
    jmxExporter.getValues();
  }

  @Test
  public void logNumericAttributes() throws MalformedObjectNameException {
    JMXExporter jmxExporter = new JMXExporter(TEST_BEAN_DOMAIN + ":*");

    Collection<State> values = jmxExporter.getValues();
    assertThat(values.size()).isEqualTo(7);
  }

  @Test
  public void convertToLong() throws MalformedObjectNameException, MBeanRegistrationException,
                                     InstanceAlreadyExistsException, NotCompliantMBeanException {
    JMXExporter jmxExporter = new JMXExporter(TEST_BEAN_DOMAIN + ":*");
    Collection<State> values = jmxExporter.getValues();

    assertValue(values, "long", 1234567890L);

    assertValue(values, "int", 65536L);
    assertValue(values, "double", 1);
    assertValue(values, "float", 2);
    assertValue(values, "short", 17);
    assertValue(values, "boolean", 1);
    assertValue(values, "long_composite", 3232323232L);

  }

  @Test
  public void beAbleToHandleJavaLangWithoutExceptionInGetValues() throws MalformedObjectNameException {
    JMXExporter jmxExporter = new JMXExporter(JAVA_LANG + ":*");
    jmxExporter.getValues();
  }

  @Test
  public void generateStrictlyValidValueKeysForTestBean() throws MalformedObjectNameException {
    JMXExporter jmxExporter = new JMXExporter(TEST_BEAN_DOMAIN + ":*");
    Collection<State> values = jmxExporter.getValues();
    ValidatingKeyHandler validatingKeyHandler = new ValidatingKeyHandler();
    for (State value : values) {
      validatingKeyHandler.handle(value.name);
    }
  }

  @Test
  public void generateStrictlyValidValueKeysForJavaLang() throws MalformedObjectNameException {
    JMXExporter jmxExporter = new JMXExporter(JAVA_LANG + ":*");
    Collection<State> values = jmxExporter.getValues();
    ValidatingKeyHandler validatingKeyHandler = new ValidatingKeyHandler();
    for (State value : values) {
      validatingKeyHandler.handle(value.name);
    }
  }

  @Test
  public void readPatternFromFile() throws Exception {
    URL url = getClass().getResource("/jmxExporter/patternTestFile.txt");
    JMXExporter jmxExporter = new JMXExporter();

    jmxExporter.readFromFile(url.getFile());
    assertThat(jmxExporter.listPatterns().size()).isEqualTo(2);
  }

  @Test
  public void eachPatternOnlyAddedOnce() throws Exception {
    URL url = getClass().getResource("/jmxExporter/duplicatePatternTestFile.txt");
    JMXExporter jmxExporter = new JMXExporter();

    jmxExporter.readFromFile(url.getFile());
    assertThat(jmxExporter.listPatterns().size()).isEqualTo(1);
  }

  @Test
  public void nonExistingSilentlyIgnored() throws Exception {
    JMXExporter jmxExporter = new JMXExporter();

    jmxExporter.readFromDirectory("nonExistingDir");
    assertThat(jmxExporter.listPatterns().size()).isEqualTo(0);

  }

  @Test
  public void emptyDirIsOkay() throws Exception {
    URL url = getClass().getResource("/emptyDir");
    JMXExporter jmxExporter = new JMXExporter();

    jmxExporter.readFromDirectory(url.getFile());
    assertThat(jmxExporter.listPatterns().size()).isEqualTo(0);

  }


  @Test
  public void readPatternFilesFromDir() throws Exception {
    URL url = getClass().getResource("/jmxExporter");
    JMXExporter jmxExporter = new JMXExporter();

    jmxExporter.readFromDirectory(url.getFile());
    assertThat(jmxExporter.listPatterns().size()).isEqualTo(2);

  }

  @Test
  public void EmptyLinesInPatternFromFileWillNotLeadToMatchAllObjectName() throws Exception {
    URL url = getClass().getResource("/jmxExporter/emptyLinePatternTestFile.txt");
    JMXExporter jmxExporter = new JMXExporter();

    jmxExporter.readFromFile(url.getFile());
    assertThat(jmxExporter.listPatterns().size()).isEqualTo(2);
  }

  @Test
  public void skipInvalidPatternWhenReadingFromFile() throws Exception {
    URL url = getClass().getResource("/jmxExporter/invalidPatternTestFile.txt");
    JMXExporter jmxExporter = new JMXExporter();

    jmxExporter.readFromFile(url.getFile());
    assertThat(jmxExporter.listPatterns().size()).isEqualTo(1);
  }

  @Test
  public void doNotFailOnEmptyFile() throws Exception {
    URL url = getClass().getResource("/jmxExporter/emptyPatternTestFile.txt");
    JMXExporter jmxExporter = new JMXExporter();

    jmxExporter.readFromFile(url.getFile());
    assertThat(jmxExporter.listPatterns().size()).isEqualTo(0);
  }


  private void assertValue(Collection<State> values, String name, long targetValue) {
    int checked = 0;
    for (State state : values) {
      if (state.name.endsWith(name)) {
        assertThat(state.value).isEqualTo(targetValue);
        checked++;
      }
    }
    assertThat(checked).isEqualTo(1);
  }


  private class TestMBean implements DynamicMBean {
    private String[] itemNames = new String[] { "long_composite", "string_composite" };
    private String[] itemDescriptions = new String[] { "a long", "a string" };
    private OpenType[] itemTypes = new OpenType[] { SimpleType.LONG, SimpleType.STRING };
    private Object[] itemValues = new Object[] { new Long(3232323232L), "lalala" };
    private CompositeType compositeType;

    public TestMBean() throws OpenDataException {
      compositeType = new CompositeType("testCompositeType", "a text composite", itemNames, itemDescriptions,
        itemTypes);
    }

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
          new MBeanAttributeInfo("long_not_supported", "long", "long", true, false, false),
          new MBeanAttributeInfo("int", "int", "int", true, false, false),
          new MBeanAttributeInfo("double", "double", "double", true, false, false),
          new MBeanAttributeInfo("float", "float", "float", true, false, false),
          new MBeanAttributeInfo("short", "short", "short", true, false, false),
          new MBeanAttributeInfo("boolean", "boolean", "boolean", true, false, false),
          new MBeanAttributeInfo("string", "java.lang.String", "string", true, false, false),
          new MBeanAttributeInfo("composite", "javax.management.openmbean.CompositeType", "a composite", true, false,
            false),
        };
    }

    /* (non-Javadoc)
    * @see de.is24.util.monitoring.jmx.JmxReportable#getAttribute(java.lang.String)
    */
    public Object getAttribute(String attributeName) {
      if (attributeName.equals("long")) {
        return 1234567890L;
      } else if (attributeName.equals("long_not_supported")) {
        throw new UnsupportedOperationException("long_not_supported is not supported");
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
      } else if (attributeName.endsWith("composite")) {
        try {
          return new CompositeDataSupport(compositeType, itemNames, itemValues);
        } catch (OpenDataException e) {
          throw new RuntimeException(e);
        }
      }

      return null;
    }
  }
}
