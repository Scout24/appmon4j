package de.is24.util.monitoring;

/**
 * Plugin base class with suitable equals and hashCode implementations and default handling for
 * HighRate and SingleEvent.
 *
 * Plugins are used via InApplicationMonitor.getInstance().registerPlugin(new MyPlugin());
 */
public abstract class AbstractMonitorPlugin implements MonitorPlugin {
  @Override
  public void register() {
    InApplicationMonitor.getInstance().registerPlugin(this);
  }

  @Override
  public boolean equals(Object other) {
    return (other instanceof AbstractMonitorPlugin) && ((MonitorPlugin) other).getUniqueName().equals(getUniqueName());
  }

  @Override
  public int hashCode() {
    return getUniqueName().hashCode();
  }

}
