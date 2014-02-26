package de.is24.util.monitoring.keyhandler;

/**
 * this KeyHandler returns the key unchanged
 */
public class TransparentKeyHandler implements KeyHandler {
  @Override
  public String handle(String name) {
    return name;
  }
}
