package net.citybuild.serverdiscovery.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PlayerJoinListener {

  private final ProxyServer proxyServer;

  @Subscribe
  public void onPlayerChooseInitialServer(PlayerChooseInitialServerEvent event) {
    event.setInitialServer(proxyServer.getAllServers().stream().findAny().orElseThrow());
  }
}