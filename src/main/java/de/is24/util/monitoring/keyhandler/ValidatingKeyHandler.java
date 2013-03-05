package de.is24.util.monitoring.keyhandler;

import java.util.regex.Pattern;


public class ValidatingKeyHandler implements KeyHandler {
  private static Pattern validationPattern = Pattern.compile("[a-zA-Z0-9_\\-.]*");

  @Override
  public String handle(String name) {
    if (!validationPattern.matcher(name).matches()) {
      throw new IllegalArgumentException("invalid key value");
    }
    return name;
  }
}
