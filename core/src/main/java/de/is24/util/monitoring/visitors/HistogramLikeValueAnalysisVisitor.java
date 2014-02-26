package de.is24.util.monitoring.visitors;

import java.util.TreeMap;
import de.is24.util.monitoring.Counter;
import de.is24.util.monitoring.HistorizableList;
import de.is24.util.monitoring.MultiValueProvider;
import de.is24.util.monitoring.ReportVisitor;
import de.is24.util.monitoring.StateValueProvider;
import de.is24.util.monitoring.Timer;
import de.is24.util.monitoring.Version;
import de.is24.util.monitoring.helper.HistogramLikeValue;


public class HistogramLikeValueAnalysisVisitor implements ReportVisitor {
  private String base;
  private float[] percentages = { 0.8f, 0.90f, 0.95f, 0.99f, 1f };
  private long totalCount = 0;
  private long factor = 0;
  private TreeMap<Long, Long> timeToCount = new TreeMap<Long, Long>();
  private long currentMax;

  public HistogramLikeValueAnalysisVisitor(String base) {
    this.base = base;
  }

  @Override
  public void reportCounter(Counter counter) {
    String name = counter.getName();
    String baseName = base + HistogramLikeValue.NAME_BIGGER_THAN;
    if (name.startsWith(baseName)) {
      String timeString = name.substring(baseName.length());
      long time = Long.parseLong(timeString);
      long count = counter.getCount();
      timeToCount.put(time, count);
      totalCount += count;
    }
  }

  @Override
  public void reportHistorizableList(HistorizableList historizableList) {
  }

  @Override
  public void reportStateValue(StateValueProvider stateValueProvider) {
    String baseNameFactor = base + HistogramLikeValue.NAME_FACTOR;
    String baseNameCurrentMax = base + HistogramLikeValue.NAME_CURRENT_MAX;
    if (stateValueProvider.getName().equals(baseNameFactor)) {
      factor = stateValueProvider.getValue();
    } else if (stateValueProvider.getName().equals(baseNameCurrentMax)) {
      currentMax = stateValueProvider.getValue();
    }
  }

  @Override
  public void reportTimer(Timer timer) {
  }

  @Override
  public void reportVersion(Version version) {
  }

  @Override
  public void reportMultiValue(MultiValueProvider multiValueProvider) {
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    int currentPercentageIdx = 0;
    long currentCount = 0;
    sb.append(base).append("\n");
    for (Long time : timeToCount.keySet()) {
      Long count = timeToCount.get(time);
      currentCount += count;

      float border = percentages[currentPercentageIdx] * totalCount;
      if (currentCount >= border) {
        double currentPercentage = ((double) currentCount / totalCount) * 100;
        sb.append(currentCount)
        .append(" values, which are ")
        .append(currentPercentage)
        .append(
          "%, are smaller than ")
        .append(((currentPercentage >= 100) ? "or equal to " : ""))
        .append(
          (currentPercentage >= 100) ? currentMax : (time + factor))
        .append(" flurbs")
        .append("\n");
        currentPercentageIdx++;
      }
    }
    return sb.toString();
  }
}
