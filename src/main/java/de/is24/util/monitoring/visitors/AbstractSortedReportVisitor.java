package de.is24.util.monitoring.visitors;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import de.is24.util.monitoring.Counter;
import de.is24.util.monitoring.Historizable;
import de.is24.util.monitoring.HistorizableList;
import de.is24.util.monitoring.ReportVisitor;
import de.is24.util.monitoring.StateValueProvider;
import de.is24.util.monitoring.Timer;
import de.is24.util.monitoring.Version;


/**
 * @author oschmitz
 */
public abstract class AbstractSortedReportVisitor implements ReportVisitor {
  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

  public AbstractSortedReportVisitor() {
  }

  protected abstract void addEntry(Entry entry);

  public void reportCounter(Counter counter) {
    addEntry(new CounterEntry(counter));
  }

  /* (non-Javadoc)
   * @see de.is24.util.monitoring.ReportVisitor#reportTimer(de.is24.util.monitoring.Timer)
   */
  public void reportTimer(Timer timer) {
    addEntry(new TimerEntry(timer));
  }

  /* (non-Javadoc)
   * @see de.is24.util.monitoring.ReportVisitor#reportStateValue(de.is24.util.monitoring.StateValueProvider)
   */
  public void reportStateValue(StateValueProvider stateValueProvider) {
    addEntry(new StateValueEntry(stateValueProvider));
  }

  /* (non-Javadoc)
   * @see de.is24.util.monitoring.ReportVisitor#reportVersion(de.is24.util.monitoring.Version)
   */
  public void reportVersion(Version aVersion) {
    addEntry(new VersionEntry(aVersion));
  }

  /**
   *
   */
  public void reportHistorizableList(HistorizableList aHistorizableList) {
    addEntry(new HistorizableEntry(aHistorizableList));
  }

  public abstract String toString();


  public abstract static class Entry {
    String name;
    String path;
    String type;

    private Entry(String fqn, String type) {
      int index = fqn.lastIndexOf(".");
      if (index >= 0) {
        this.name = fqn.substring(index + 1);
        this.path = fqn.substring(0, index);
      } else {
        this.name = fqn;
        this.path = "";
      }
      this.type = type;
    }

    public abstract String getValue();

    public String getKey() {
      return name + "." + type;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
      return name;
    }

    /**
     * @return Returns the type.
     */
    public String getType() {
      return type;
    }

    /**
     * @return Returns the path.
     */
    public String getPath() {
      return path;
    }
  }

  public final class TimerEntry extends Entry {
    private long count;
    private long sum;
    private double average;
    private double stdDev;

    private TimerEntry(Timer timer) {
      super(timer.getName(), "timer");
      count = timer.getCount();
      sum = timer.getTimerSum();
      average = timer.getTimerAvg();
      stdDev = timer.getTimerStdDev();
    }

    public String getValue() {
      return getPath() + " Timer " + getName() + " : " + getCount() + " took " + getSum() + " avg. " +
        average + " stdDev. " + getStdDev();
    }

    public long getCount() {
      return count;
    }

    public long getSum() {
      return sum;
    }

    public double getAverage() {
      return average;
    }

    public double getStdDev() {
      return stdDev;
    }
  }

  public final class CounterEntry extends Entry {
    private long count;

    private CounterEntry(Counter counter) {
      super(counter.getName(), "counter");
      count = counter.getCount();
    }

    public String getValue() {
      return getPath() + " Counter " + getName() + " : " + getCount();
    }

    public long getCount() {
      return count;
    }

  }

  public final class StateValueEntry extends Entry {
    private long stateValue;

    private StateValueEntry(StateValueProvider stateValueProvider) {
      super(stateValueProvider.getName(), "StateValue");
      stateValue = stateValueProvider.getValue();
    }

    public String getValue() {
      return getPath() + " StateValue " + getName() + " : " + getStateValue();
    }

    public long getStateValue() {
      return stateValue;
    }

  }

  public final class VersionEntry extends Entry {
    private Version fVersion;

    private VersionEntry(Version aVersion) {
      super(aVersion.getName(), "Version");
      fVersion = aVersion;
    }

    public String getValue() {
      return getPath() + " Version " + getName() + " : " + fVersion.getValue();
    }

  }

  public final class HistorizableEntry extends Entry {
    private ArrayList<HistorizableSubEntry> historyValues;

    private HistorizableEntry(HistorizableList historizableList) {
      super(historizableList.getName(), "Historizable");
      historyValues = new ArrayList<HistorizableSubEntry>();
      for (Historizable historizable : historizableList) {
        historyValues.add(new HistorizableSubEntry(historizable.getTimestamp(), historizable.getValue()));
      }
    }

    public String getValue() {
      StringBuilder buffy = new StringBuilder();
      buffy.append(getPath()).append(" Historizable ").append(getName()).append(" :\n");
      for (Iterator<HistorizableSubEntry> iter = historyValues.iterator(); iter.hasNext();) {
        HistorizableSubEntry element = iter.next();
        buffy.append(DATE_FORMAT.format(element.getTimestamp())).append(" : ").append(element.getValue()).append("\n");
      }
      return buffy.toString();
    }

  }

  public final class HistorizableSubEntry {
    private final Date fTimestamp;
    private final String fValue;

    /**
     *
     */
    public HistorizableSubEntry(Date aTimestamp, String aValue) {
      fTimestamp = aTimestamp;
      fValue = aValue;
    }

    /**
     * @return Returns the timestamp.
     */
    public Date getTimestamp() {
      return fTimestamp;
    }

    /**
     * @return Returns the value.
     */
    public String getValue() {
      return fValue;
    }
  }
}
