package org.kryonite.kryoserverdiscovery;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;

import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import java.util.Timer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kryonite.kryoserverdiscovery.listener.PlayerJoinListener;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class KryoServerDiscoveryPluginTest {

  @InjectMocks
  private KryoServerDiscoveryPlugin testee;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ProxyServer proxyServerMock;

  @Test
  void shouldSetupPlayerJoinListenerOnProxyInitializeEvent() {
    // Arrange
    ProxyInitializeEvent proxyInitializeEvent = new ProxyInitializeEvent();

    // Act
    testee.onInitialize(proxyInitializeEvent);

    // Assert
    verify(proxyServerMock.getEventManager()).register(any(), any(PlayerJoinListener.class));
  }
}
