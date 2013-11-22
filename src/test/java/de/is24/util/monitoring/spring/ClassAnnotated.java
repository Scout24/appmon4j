package de.is24.util.monitoring.spring;

@TimeMeasurement
public class ClassAnnotated {
  public void methodOne() {
  }

  public String methodTwo(String lala) {
    return lala + "blubb";
  }
}
