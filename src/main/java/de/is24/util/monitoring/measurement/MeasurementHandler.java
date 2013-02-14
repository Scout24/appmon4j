package de.is24.util.monitoring.measurement;

/**
 * Created with IntelliJ IDEA.
 * User: oschmitz
 * Date: 20.09.12
 * Time: 12:34
 * To change this template use File | Settings | File Templates.
 */
public interface MeasurementHandler {
  void handle(String monitorName, long measurement);
}
