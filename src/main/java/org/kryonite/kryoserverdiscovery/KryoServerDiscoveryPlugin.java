package org.kryonite.kryoserverdiscovery;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import java.util.Timer;
import org.kryonite.kryoserverdiscovery.listener.PlayerJoinListener;
import org.kryonite.kryoserverdiscovery.serverdiscovery.ServerDiscoveryTask;

@Plugin(id = "kryoserverdiscovery", name = "Kryo Server Discovery", version = "1.0.0")
public class KryoServerDiscoveryPlugin {

  private final Timer timer = new Timer(true);
  private final ProxyServer server;

  @Inject
  public KryoServerDiscoveryPlugin(ProxyServer server) {
    this.server = server;
  }

  @Subscribe
  public void onInitialize(ProxyInitializeEvent event) {
    DefaultKubernetesClient kubernetesClient = new DefaultKubernetesClient();
    timer.scheduleAtFixedRate(new ServerDiscoveryTask(server, kubernetesClient), 1000, 1000);
    server.getEventManager().register(this, new PlayerJoinListener(server));
  }
}