package de.is24.util.monitoring;

public class State {
  public String name;
  public long value;

  public State(String parentName, String valueName, long value) {
    this.name = (parentName + "." + valueName).replaceAll("[:* =]", "_").replaceAll(",", ".");
    this.value = value;
  }
}
