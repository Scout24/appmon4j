/**
 *
 */
package de.is24.util.monitoring.jmx;

import java.util.ArrayList;
import java.util.List;
import javax.management.MBeanAttributeInfo;
import org.apache.log4j.Logger;
import de.is24.util.monitoring.HistorizableList;


/**
 * A wrapper for monitoring {@link HistorizableList} reportables.
 *
 * @author <a href="mailto:sschubert@immobilienscout24.de">Stefan Schubert</a>, IT-E, IS24
 */
class HistorizableListJmxReportable implements JmxReportable {
  private static final Logger LOGGER = Logger.getLogger(HistorizableListJmxReportable.class);

  private final HistorizableList reportable;

  /**
   * @param reportable the {@link HistorizableList}. May not be null.
   */
  public HistorizableListJmxReportable(HistorizableList reportable) {
    assert reportable != null;
    this.reportable = reportable;
  }

  /* (non-Javadoc)
   * @see de.is24.util.monitoring.jmx.JmxReportable#getAttributes()
   */
  public MBeanAttributeInfo[] getAttributes() {
    final List<MBeanAttributeInfo> attributes = new ArrayList<MBeanAttributeInfo>();
    for (int i = 1; i <= reportable.getMaxEntriesToKeep(); i++) {
      attributes.add(new MBeanAttributeInfo(Integer.toString(i), "String", "Value number " + i + " in history", true,
          false, false));
    }
    return attributes.toArray(new MBeanAttributeInfo[attributes.size()]);
  }

  /* (non-Javadoc)
   * @see de.is24.util.monitoring.jmx.JmxReportable#getAttribute(java.lang.String)
   */
  public Object getAttribute(String attributeName) {
    try {
      final int index = Integer.parseInt(attributeName) - 1;
      if (index < reportable.getMaxEntriesToKeep()) {
        return reportable.get(index).getValue();
      }
    } catch (NumberFormatException e) {
      LOGGER.error("Should never happen! Requested attribute should be an integer but was: " + attributeName);
    }
    return null;
  }
}
