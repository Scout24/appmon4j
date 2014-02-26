package de.is24.util.monitoring.visitors;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import de.is24.util.monitoring.HistorizableList;
import de.is24.util.monitoring.MultiValueProvider;
import de.is24.util.monitoring.StateValueProvider;
import de.is24.util.monitoring.Version;


public class ValueOrderedReportVisitor extends AbstractSortedReportVisitor {
  protected Set<CounterEntry> counterSet;
  protected Set<TimerEntry> timerSet;

  public ValueOrderedReportVisitor() {
    counterSet = new TreeSet<CounterEntry>(new CounterComparator());
    timerSet = new TreeSet<TimerEntry>(new TimerComparator());
  }

  @Override
  public void reportHistorizableList(HistorizableList aHistorizableList) {
    // we do not report historizables
  }

  @Override
  public void reportVersion(Version aVersion) {
    // we do not report Versions
  }

  @Override
  public void reportStateValue(StateValueProvider stateValueProvider) {
    // we do not report stateValues
  }

  @Override
  public void reportMultiValue(MultiValueProvider multiValueProvider) {
    // we do not report multiValues
  }

  @Override
  protected void addEntry(Entry entry) {
    if (entry instanceof TimerEntry) {
      timerSet.add((TimerEntry) entry);
    } else if (entry instanceof CounterEntry) {
      counterSet.add((CounterEntry) entry);
    }
  }

  @Override
  public String toString() {
    StringBuilder buffy = new StringBuilder();
    buffy.append(getClass().getName());
    buffy.append("\n");

    for (CounterEntry entry : counterSet) {
      buffy.append(entry.getValue());
      buffy.append("\n");
    }
    for (TimerEntry entry : timerSet) {
      buffy.append(entry.getValue());
      buffy.append("\n");
    }

    return buffy.toString();
  }

  private static class CounterComparator implements Comparator<CounterEntry> {
    public int compare(CounterEntry o1, CounterEntry o2) {
      if (o1.getCount() == o2.getCount()) {
        return 0;
      }

      return (o1.getCount() > o2.getCount()) ? -1 : 1;
    }
  }

  private static class TimerComparator implements Comparator<TimerEntry> {
    public int compare(TimerEntry o1, TimerEntry o2) {
      if (o1.getAverage() == o2.getAverage()) {
        return 0;
      }

      return (o1.getAverage() > o2.getAverage()) ? -1 : 1;
    }
  }
}
