package de.is24.util.monitoring.keyhandler;

import de.is24.util.monitoring.tools.LocalHostNameResolver;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class KeyDefinitionExpanderTest {
  LocalHostNameResolver localHostNameResolver;
  private static final String SIMPLE_APP_NAME = "testAppName";

  @Rule
  public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties("proc_datanode",
    "appname");

  @Before
  public void setup() {
    localHostNameResolver = mock(LocalHostNameResolver.class);
    when(localHostNameResolver.getLocalHostName()).thenReturn("testHost");
  }

  @Test
  public void useAppNameHostNameAndStateAsPrefix() throws Exception {
    String expandedKey = expand(SIMPLE_APP_NAME);
    assertThat(expandedKey, is("testAppName.testHost.states"));
  }

  @Test
  public void allowHostnamePatternInPrefix() throws Exception {
    String expandedKey = expand("typ.${hostname}.app");

    assertThat(expandedKey, is("typ.testHost.app"));
  }

  @Test
  public void allowSystemPropertyNamePartialMatchInPrefix() throws Exception {
    System.setProperty("proc_datanode", "");

    String expandedKey = expand("typ.${systemPropertyName:proc_(.*)}.app");

    assertThat(expandedKey, is("typ.datanode.app"));
  }

  @Test(expected = RuntimeException.class)
  public void missingSystemPropertyNameMatchWillFail() throws Exception {
    String expandedKey = expand("typ.${systemPropertyName:proc_(.*)}.app");
    assertThat(expandedKey, is("typ.datanode.app"));
  }


  @Test
  public void allowSystemPropertyValueInPrefix() throws Exception {
    System.setProperty("appname", "schnulli");

    String expandedKey = expand("typ.${systemProperty:appname}.app");

    assertThat(expandedKey, is("typ.schnulli.app"));
  }

  @Test(expected = RuntimeException.class)
  public void missingSystemPropertyValueWillFail() throws Exception {
    String expandedKey = expand("typ.${systemProperty:appname}.app");

  }

  @Test
  public void allowCombinationOfPatternsInPrefix() throws Exception {
    System.setProperty("proc_datanode", "");
    System.setProperty("appname", "schnulli");

    String expandedKey = expand("typ.${hostname}.${systemPropertyName:proc_(.*)}.${systemProperty:appname}.app");

    assertThat(expandedKey, is("typ.testHost.datanode.schnulli.app"));
  }


  private String expand(String keyDefinition) {
    return KeyDefinitionExpander.preparePrefix(keyDefinition,
      localHostNameResolver);
  }


}
