package org.kryonite.kryoserverdiscovery.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlayerJoinListenerTest {

  @InjectMocks
  private PlayerJoinListener testee;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ProxyServer proxyServerMock;

  @Test
  void shouldSetInitialServer() {
    // Arrange
    Player player = mock(Player.class, Answers.RETURNS_DEEP_STUBS);
    PlayerChooseInitialServerEvent event = new PlayerChooseInitialServerEvent(player, null);

    RegisteredServer registeredServer = mock(RegisteredServer.class);
    when(proxyServerMock.getAllServers()).thenReturn(Set.of(registeredServer));

    // Act
    testee.onPlayerChooseInitialServer(event);

    // Assert
    assertTrue(event.getInitialServer().isPresent(), "Initial server should be set");
    assertEquals(registeredServer, event.getInitialServer().get(), "Initial server did not match");
  }
}