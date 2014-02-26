package de.is24.util.monitoring.tools;

import org.junit.Test;
import java.net.InetAddress;
import java.net.UnknownHostException;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class LocalHostNameResolverTest {
  @Test
  public void shouldResolveHostnameFromFQDN() throws UnknownHostException {
    InetAddress address = mock(InetAddress.class);
    when(address.getHostName()).thenReturn("testHost.bla.bli.blu");

    LocalHostProvider localHostProvider = mock(LocalHostProvider.class);
    when(localHostProvider.getLocalHost()).thenReturn(address);

    LocalHostNameResolver target = new LocalHostNameResolver(localHostProvider);

    assertThat(target.getLocalHostName()).isEqualTo("testHost");
  }

  @Test
  public void shouldReturnUnknownHostOnError() throws UnknownHostException {
    LocalHostProvider localHostProvider = mock(LocalHostProvider.class);
    when(localHostProvider.getLocalHost()).thenThrow(new UnknownHostException("testHost"));

    LocalHostNameResolver target = new LocalHostNameResolver(localHostProvider);

    assertThat(target.getLocalHostName()).isEqualTo("unknownHost");
  }
}
