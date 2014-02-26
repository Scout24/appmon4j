package de.is24.util.monitoring;

/**
 * <p>Implements StateValueProvider for easily adding a key - value pair to
 * our inapplicationmonitor. Every time this class is implemented it is in
 * an anonymous class ... I did not want to do this all the time, i have
 * to use ist ;-)</p>
 * @author adeetzen
 */
public class SimpleStateValueProvider extends StateValueProvider {
  private final String name;
  private long value;

  public SimpleStateValueProvider(String name, long value) {
      this.value = value;
      this.name = name;
    }

    public long getValue() {
      return value;
    }

    public String getName() {
      return name;
    }

    public void setValue(long value) {
      this.value = value;
    }
}
