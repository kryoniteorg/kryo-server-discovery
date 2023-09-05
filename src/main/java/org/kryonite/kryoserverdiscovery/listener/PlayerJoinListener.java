package org.kryonite.kryoserverdiscovery.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kryonite.kryoserverdiscovery.KryoServerDiscoveryPlugin;

import java.util.Optional;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class PlayerJoinListener {

  private final KryoServerDiscoveryPlugin plugin;
  private final ProxyServer proxyServer;

  @Subscribe
  public void onPlayerChooseInitialServer(PlayerChooseInitialServerEvent event) {
    Set<String> discoveredServers = this.plugin.getDiscoveredServers();
    Optional<RegisteredServer> server = proxyServer.getAllServers()
      .stream()
      .filter(downstream -> discoveredServers.contains(downstream.getServerInfo().getName()))
      .findAny();

    if (server.isEmpty()) {
      log.warn("Join listener could not find a server for player!");
      return;
    }

    event.setInitialServer(server.get());

  }
}