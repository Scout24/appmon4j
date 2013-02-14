package de.is24.util.monitoring.status;

import de.is24.util.monitoring.InApplicationMonitor;
import de.is24.util.monitoring.StateValueProvider;
import org.apache.log4j.Logger;


public class SampledData {
  private static final Logger LOGGER = Logger.getLogger(SampledData.class);
  final int maxIndex = 15;
  long[] successData = new long[maxIndex];
  long[] failureData = new long[maxIndex];

  int currentPointer = 0;
  private final String name;

  public SampledData(String name) {
    this.name = name;
    initData();
    InApplicationMonitor.getInstance().registerStateValue(new StateValueProvider() {
        @Override
        public long getValue() {
          return (long) (getOnMinuteFailureRate() * 100);
        }

        @Override
        public String getName() {
          return SampledData.this.name + ".oneMinuteFailureRate";
        }
      });
  }

  public void addSample(long successCount, long failureCount) {
    incrementCurrentPointer();
    failureData[currentPointer] = failureCount;
    successData[currentPointer] = successCount;
  }

  private void incrementCurrentPointer() {
    currentPointer += 1;
    if (currentPointer >= maxIndex) {
      currentPointer = 0;
    }
  }

  private void initData() {
    for (int i = 0; i < maxIndex; i++) {
      failureData[i] = 0;
      successData[i] = 0;
    }
  }

  public float getOnMinuteFailureRate() {
    return calcRateOverInterval(1);
  }


  public float getFiveMinuteFailureRate() {
    return calcRateOverInterval(5);

  }


  public Float getFifteenMinuteFailureRate() {
    return calcRateOverInterval(15);
  }

  private float calcRateOverInterval(int interval) {
    int currentIndex = currentPointer;
    int successCount = 0;
    int failureCount = 0;
    int count = 0;
    while (count < interval) {
      successCount += successData[currentIndex];
      failureCount += failureData[currentIndex];
      currentIndex -= 1;
      if (currentIndex < 0) {
        currentIndex = maxIndex - 1;
      }
      count++;
    }
    return calcFailureRate(successCount, failureCount);
  }

  private float calcFailureRate(long successCount, long failureCount) {
    LOGGER.debug("success count " + successCount + ", failureCount : " + failureCount);

    long total = successCount + failureCount;
    return (total == 0) ? 0.0f : ((float) failureCount / total);
  }

}
