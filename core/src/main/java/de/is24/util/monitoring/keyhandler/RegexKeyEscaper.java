package de.is24.util.monitoring.keyhandler;

import java.util.regex.Pattern;


public class RegexKeyEscaper implements KeyHandler {
  private final Pattern keyEscapePattern;

  public RegexKeyEscaper(String pattern) {
    keyEscapePattern = Pattern.compile(pattern);
  }


  /**
   * helper function that escapes a reportable's name so that it is JMX-compatible
   *
   * @param name the original name of the reportable
   * @return the espaced name
   *         Or we should have a defined contract on allowed chars in keys and enfocr it on the entry side.
   *         Due to performance and responsibility reasons.
   */
  public String handle(String name) {
    return keyEscapePattern.matcher(name).replaceAll("_");
  }
}
