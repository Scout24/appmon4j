package de.is24.util.monitoring.jmx;

import de.is24.util.monitoring.Counter;
import de.is24.util.monitoring.HistorizableList;
import de.is24.util.monitoring.Reportable;
import de.is24.util.monitoring.StateValueProvider;
import de.is24.util.monitoring.Timer;
import de.is24.util.monitoring.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;
import java.text.SimpleDateFormat;


/**
 * Dynamic MBean that represents one non-primitive Reportable (Timer, HistorizableList)
 * that contains multiple values.
 *
 * @author ptraeder
 */
public class InApplicationMonitorDynamicMBean implements DynamicMBean {
  static final Logger LOGGER = LoggerFactory.getLogger(InApplicationMonitorDynamicMBean.class);

  static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

  private final JmxReportable type;

  /**
   * @param reportable a {@link Reportable} to be monitored by this MBean. May not be null.
   */
  public InApplicationMonitorDynamicMBean(Reportable reportable) {
    assert reportable != null;
    if (reportable instanceof Timer) {
      type = new TimerJmxReportable((Timer) reportable);
    } else if (reportable instanceof Counter) {
      type = new CounterJmxReportable((Counter) reportable);
    } else if (reportable instanceof Version) {
      type = new VersionJmxReportable((Version) reportable);
    } else if (reportable instanceof StateValueProvider) {
      type = new StateValueProviderJmxReportable((StateValueProvider) reportable);
    } else if (reportable instanceof HistorizableList) {
      type = new HistorizableListJmxReportable((HistorizableList) reportable);
    } else {
      LOGGER.warn("Unknown reportable: {} of type {}", reportable.getName(), reportable.getClass().getName());
      type = NoJmxReportable.getInstance();
    }
  }

  /* (non-Javadoc)
   * @see javax.management.DynamicMBean#getMBeanInfo()
   */
  public MBeanInfo getMBeanInfo() {
    // assemble the MBean description
    final MBeanInfo beanInfo = new MBeanInfo("de.is24.util.monitoring.InApplicationMonitorDynamicMBeanThing",
      "InApplication Monitor dynamic MBean", type.getAttributes(), null, null, null);
    return beanInfo;
  }

  /* (non-Javadoc)
   * @see javax.management.DynamicMBean#getAttribute(java.lang.String)
   */
  public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
    return getAttributeInternal(attribute);
  }

  /* (non-Javadoc)
   * @see javax.management.DynamicMBean#getAttributes(java.lang.String[])
   */
  public AttributeList getAttributes(String[] attributes) {
    final AttributeList result = new AttributeList();
    for (String attributeName : attributes) {
      result.add(new Attribute(attributeName, getAttributeInternal(attributeName)));
    }
    return result;
  }

  private Object getAttributeInternal(String attribute) {
    final Object result = type.getAttribute(attribute);
    return result;
  }

  /* (non-Javadoc)
   * @see javax.management.DynamicMBean#setAttribute(javax.management.Attribute)
   */
  public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException,
                                                       MBeanException, ReflectionException {
  }


  /* (non-Javadoc)
   * @see javax.management.DynamicMBean#setAttributes(javax.management.AttributeList)
   */
  public AttributeList setAttributes(AttributeList attributes) {
    return null;
  }

  /* (non-Javadoc)
   * @see javax.management.DynamicMBean#invoke(java.lang.String, java.lang.Object[], java.lang.String[])
   */
  public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException,
                                                                                      ReflectionException {
    return null;
  }

}
