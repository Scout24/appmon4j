package de.is24.util.monitoring.spring;

public class MethodAnnotated {
  @TimeMeasurement
  public void methodOne() {
  }

  @TimeMeasurement
  public String methodTwo(String lala) {
    return lala + "blubb";
  }

}
