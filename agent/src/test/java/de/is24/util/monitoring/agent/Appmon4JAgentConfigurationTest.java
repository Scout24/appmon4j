package de.is24.util.monitoring.agent;

import org.junit.Test;
import java.net.URL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;


public class Appmon4JAgentConfigurationTest {
  @Test
  public void configCanConfigureJMXExport() throws Exception {
    URL resource = getClass().getClassLoader().getResource("configuration.properties");
    Appmon4JAgentConfiguration configuration = Appmon4JAgentConfiguration.load(resource.getFile());
    assertThat(configuration.isJmxExportConfigured(), is(true));
  }

  @Test
  public void configCanConfigureJMXExportDir() throws Exception {
    URL resource = getClass().getClassLoader().getResource("configuration.properties");
    Appmon4JAgentConfiguration configuration = Appmon4JAgentConfiguration.load(resource.getFile());
    assertThat(configuration.isJmxExportFile(), is(false));
  }

  @Test
  public void configCanSetupStateValuesToGraphitePlugin() throws Exception {
    URL resource = getClass().getClassLoader().getResource("configuration.properties");
    Appmon4JAgentConfiguration configuration = Appmon4JAgentConfiguration.load(resource.getFile());
    assertThat(configuration.isGraphiteConfigured(), is(true));
    assertThat(configuration.getGraphiteHost(), is("localhost"));
    assertThat(configuration.getGraphitePort(), is(2003));
    assertThat(configuration.getGraphiteAppNamePrefix(), is("Test"));
  }

  @Test
  public void emptyConfigEnablesNothing() throws Exception {
    URL resource = getClass().getClassLoader().getResource("emptyConfig.properties");
    Appmon4JAgentConfiguration configuration = Appmon4JAgentConfiguration.load(resource.getFile());
    assertThat(configuration.isGraphiteConfigured(), is(false));
    assertThat(configuration.isJmxExportConfigured(), is(false));
    assertThat(configuration.isInstrumentationConfigured(), is(false));
  }

  @Test(expected = NumberFormatException.class)
  public void buggyConfigWillThrowException() throws Exception {
    URL resource = getClass().getClassLoader().getResource("failingConfig.properties");
    Appmon4JAgentConfiguration.load(resource.getFile());
  }


}
