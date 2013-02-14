package de.is24.util.monitoring;

import org.apache.log4j.Logger;


/**
 * @author OSchmitz
 *
 */
public class Version implements Reportable {
  private static final Logger LOGGER = Logger.getLogger(Version.class);
  private final String fName;
  private final String fValue;

  /**
   *
   */
  public Version(String aName, String aValue) {
    fName = aName;
    fValue = aValue;
  }

  public void accept(ReportVisitor aVisitor) {
    LOGGER.debug("+++ enter Version.accept +++");
    aVisitor.reportVersion(this);
  }

  /**
   * @return Returns the name.
   */
  public String getName() {
    return fName;
  }

  /**
   * @return Returns the value.
   */
  public String getValue() {
    return fValue;
  }

}
