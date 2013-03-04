package de.is24.util.monitoring;


public class DefaultKeyEscaper extends RegexKeyEscaper {
  public DefaultKeyEscaper() {
    super("[:=]");
  }

}
