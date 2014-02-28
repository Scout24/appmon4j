package de.is24.util.monitoring.agent;

import org.junit.Test;
import java.net.URL;


public class Appmon4JAgentTest {
  @Test
  public void failingConfigWillNotKillJVM() throws Exception {
    URL resource = getClass().getClassLoader().getResource("failingConfig.properties");
    Appmon4JAgent.premain(resource.getFile(), null);

  }
}
