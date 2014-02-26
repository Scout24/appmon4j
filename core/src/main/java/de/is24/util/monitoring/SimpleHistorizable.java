package de.is24.util.monitoring;

import java.util.Date;


/**
 * This is a default Implementation of {@link Historizable}
 * supplied for convenience.
 *
 * @author OSchmitz
 */
public class SimpleHistorizable implements Historizable {
  private final String fName;
  private final String fValue;
  private final Date fTimestamp;

  /**
   * Instantiate a SimpleHistorizable with the current time as timestamp
   * @param aName name of the Historizbale
   * @param aValue value of the Historizable
   */
  public SimpleHistorizable(String aName, String aValue) {
    this(aName, aValue, new Date());
  }

  /**
   * Instantiate a Simple historizable with a given timestamp
   *
   * @param aName name of the Historizbale
   * @param aValue value of the Historizable
   * @param aTimestamp timestamp associated with this Historizable
   */
  public SimpleHistorizable(String aName, String aValue, Date aTimestamp) {
    fName = aName;
    fValue = aValue;
    fTimestamp = aTimestamp;
  }

  public String getValue() {
    return fValue;
  }

  public String getName() {
    return fName;
  }

  public Date getTimestamp() {
    return fTimestamp;
  }

  @Override
  public String toString() {
    return "[" + getName() + "] " + getTimestamp() + " : " + getValue();
  }

}
