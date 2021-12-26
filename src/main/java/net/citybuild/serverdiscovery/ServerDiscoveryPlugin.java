package net.citybuild.serverdiscovery;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import java.util.Timer;
import net.citybuild.serverdiscovery.listener.PlayerJoinListener;
import net.citybuild.serverdiscovery.serverdiscovery.ServerDiscoveryTask;

@Plugin(id = "serverdiscovery", name = "Server Discovery", version = "0.1.0-SNAPSHOT")
public class ServerDiscoveryPlugin {

  private final Timer timer = new Timer(true);
  private final ProxyServer server;

  @Inject
  public ServerDiscoveryPlugin(ProxyServer server) {
    this.server = server;
  }

  @Subscribe
  public void onInitialize(ProxyInitializeEvent event) {
    DefaultKubernetesClient kubernetesClient = new DefaultKubernetesClient();
    timer.scheduleAtFixedRate(new ServerDiscoveryTask(server, kubernetesClient), 1000, 1000);
    server.getEventManager().register(this, new PlayerJoinListener(server));
  }
}