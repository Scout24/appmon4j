package de.is24.util.monitoring.jmx;

import org.springframework.web.context.ServletContextAware;
import javax.servlet.ServletContext;


/**
 * This classes provides the contextPath of the servletContext as jmx prefix for the {@link InApplicationMonitorJMXConnector}.
 * This should be used for webApps, cause multiple webApps create multiple instances of the {@link InApplicationMonitorJMXConnector} and override eachother.
 *
 * @see InApplicationMonitorJMXConnector
 */
public class WebContextJmxAppMon4JNamingStrategy implements JmxAppMon4JNamingStrategy, ServletContextAware {
  private String prefix = "is24";

  /**
   * @see JmxAppMon4JNamingStrategy#getJmxPrefix()
   * @return the String provided by the {@link ServletContext#getContext(String)} method
   */
  public String getJmxPrefix() {
    return prefix;
  }

  /**
   * @see ServletContextAware#setServletContext(ServletContext)
   * @param servletContext the servlet context
   */
  public void setServletContext(ServletContext servletContext) {
    if (servletContext == null) {
      throw new IllegalArgumentException("servletContext is null");
    }

    String contextPath = servletContext.getContextPath();
    if ((contextPath != null) && (contextPath.length() > 1)) {
      // use context path as jmx prefix
      prefix = contextPath.replaceFirst("/", "");
    } else {
      // use default jmx prefix
    }
  }

}
