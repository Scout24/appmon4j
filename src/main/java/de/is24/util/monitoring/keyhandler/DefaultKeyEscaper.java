package de.is24.util.monitoring.keyhandler;


public class DefaultKeyEscaper extends RegexKeyEscaper {
  public DefaultKeyEscaper() {
    super("[:=]");
  }

}
