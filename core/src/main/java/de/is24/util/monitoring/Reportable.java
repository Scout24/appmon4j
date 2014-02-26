package de.is24.util.monitoring;

/**
 * The building blocks of the InApplicationMonitor class are Reportables.
 * All Reportabls have a name, which is a simple String.
 * Names can be choosen freely, but it is recommended to use canonical names
 * and a good choice is to use the class name of the monitored class
 * plus a description of the reportable value.
 *
 * @author OSchmitz
 */
public interface Reportable {
  void accept(ReportVisitor visitor);

  String getName();
}
