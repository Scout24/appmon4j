package de.is24.util.monitoring.keyhandler;

import de.is24.util.monitoring.tools.LocalHostNameResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class KeyDefinitionExpander {
  private static final Logger LOGGER = LoggerFactory.getLogger(KeyDefinitionExpander.class);

  public static String preparePrefix(String appName, LocalHostNameResolver localHostNameResolver) {
    KeyProcessor[] keyProcessors = new KeyProcessor[] {
      new SystemPropertyNamePartKeyProcessor(), new SystemPropertyValueKeyProcessor(),
      new HostNameKeyProcessor(localHostNameResolver)
    };
    String result = appName;

    for (KeyProcessor processor : keyProcessors) {
      result = processor.process(result);
    }

    // old default behavior
    if (result.equals(appName)) {
      result = appName + "." + localHostNameResolver.getLocalHostName() + ".states";
    }
    return result;
  }

  private interface KeyProcessor {
    String process(String input);
  }

  private static class SystemPropertyNamePartKeyProcessor implements KeyProcessor {
    private Pattern systemPropertyNamePattern = Pattern.compile("\\$\\{systemPropertyName:([^}]*)\\}");

    @Override
    public String process(String input) {
      StringBuffer buffer = new StringBuffer();
      Matcher matcher = systemPropertyNamePattern.matcher(input);
      while (matcher.find()) {
        String namePattern = matcher.group(1);
        Pattern pattern = Pattern.compile(namePattern);
        boolean matched = false;
        for (Enumeration e = System.getProperties().propertyNames(); !matched && e.hasMoreElements();) {
          String name = (String) e.nextElement();
          Matcher entryMatcher = pattern.matcher(name);
          if (entryMatcher.matches()) {
            matched = true;
            matcher.appendReplacement(buffer, entryMatcher.group(1));
          }
        }
        if (!matched) {
          throw new RuntimeException("could not find a System property matching name " + namePattern);
        }
      }
      matcher.appendTail(buffer);
      return buffer.toString();

    }
  }

  private static class SystemPropertyValueKeyProcessor implements KeyProcessor {
    private Pattern systemPropertyNamePattern = Pattern.compile("\\$\\{systemProperty:([^}]*)\\}");

    @Override
    public String process(String input) {
      StringBuffer buffer = new StringBuffer();
      Matcher matcher = systemPropertyNamePattern.matcher(input);
      while (matcher.find()) {
        String propertyName = matcher.group(1);
        String value = System.getProperty(propertyName);
        if (value == null) {
          String message = "could not find System property " + propertyName;
          LOGGER.warn(message);
          throw new RuntimeException(message);
        }
        matcher.appendReplacement(buffer, value);
      }
      matcher.appendTail(buffer);
      return buffer.toString();

    }
  }

  private static class HostNameKeyProcessor implements KeyProcessor {
    private LocalHostNameResolver localHostNameResolver;

    public HostNameKeyProcessor(LocalHostNameResolver localHostNameResolver) {
      this.localHostNameResolver = localHostNameResolver;
    }

    @Override
    public String process(String input) {
      if (input.contains("${hostname}")) {
        return input.replaceAll("\\$\\{hostname\\}", localHostNameResolver.getLocalHostName());
      }
      return input;
    }
  }

}
