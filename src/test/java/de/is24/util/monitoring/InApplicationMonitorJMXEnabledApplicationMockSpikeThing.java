package de.is24.util.monitoring;

import de.is24.util.monitoring.jmx.JmxAppMon4JNamingStrategy;
import org.apache.log4j.Logger;


public class InApplicationMonitorJMXEnabledApplicationMockSpikeThing extends StateValueProvider {
  private static final Logger LOGGER = Logger.getLogger(InApplicationMonitorJMXEnabledApplicationMockSpikeThing.class);

  static int numberOfCounters = 0;

  public static void main(String[] args) {
    new InApplicationMonitorJMXEnabledApplicationMockSpikeThing().run();
  }


  public InApplicationMonitorJMXEnabledApplicationMockSpikeThing() {
    super();
    setupInApplicationMonitor();
  }

  private void setupInApplicationMonitor() {
    CorePlugin corePlugin = new CorePlugin(new JmxAppMon4JNamingStrategy() {
        @Override
        public String getJmxPrefix() {
          return "is24";
        }
      }, new DefaultKeyEscaper());

    InApplicationMonitor.getInstance().registerVersion("zaphod", "beeblebrox");
    InApplicationMonitor.getInstance().registerStateValue(this);
  }

  public void run() {
    int loop = 0;

    InApplicationMonitor.getInstance().registerVersion("ford", "prefect");

    long start = System.currentTimeMillis();
    while (true) {
      InApplicationMonitor.getInstance().incrementCounter("counter1");
      InApplicationMonitor.getInstance().incrementCounter("counter2");

      long duration = System.currentTimeMillis() - start;
      start = System.currentTimeMillis();
      InApplicationMonitor.getInstance().addTimerMeasurement("timer1", duration);
      LOGGER.info("duration since last run " + duration);

      InApplicationMonitor.getInstance()
      .addHistorizable(
        new SimpleHistorizable("numberOfAdditionalCounters",
          String.valueOf(numberOfCounters)));

      if ((++loop % 6) == 0) {
        numberOfCounters++;
        InApplicationMonitor.getInstance().incrementCounter("counter" + loop);
      }

      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }


  @Override
  public long getValue() {
    return 4711;
  }


  @Override
  public String getName() {
    return "name-of-the-state-value-that-does-not-want-to-be-called-by-his-name";
  }

}
