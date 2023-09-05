package org.kryonite.kryoserverdiscovery;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import lombok.extern.slf4j.Slf4j;
import org.kryonite.kryoserverdiscovery.listener.PlayerJoinListener;
import org.kryonite.kryoserverdiscovery.serverdiscovery.ServerDiscoveryTask;

@Slf4j
@Plugin(id = "kryoserverdiscovery", name = "Kryo Server Discovery", version = "1.0.0")
public class KryoServerDiscoveryPlugin {

  private final Timer timer = new Timer(true);
  private final ProxyServer server;
  private final Map<String, String> configuration = new HashMap<>();

  private final Map<String, String> configurationDefaults = new HashMap<>();

  @Inject
  public KryoServerDiscoveryPlugin(ProxyServer server) {
    this.server = server;
  }

  @Subscribe
  public void onInitialize(ProxyInitializeEvent event) {
    this.configurationDefaults.put("enable-join-listener", "true");
    this.configurationDefaults.put("discovery-task-interval-ms", "1000");
    this.configurationDefaults.put("server-name-format", "k8s-%s");
    this.loadEnvironmentConfiguration();

    log.info("Following configuration was parsed:");
    this.configuration.forEach((k, v) -> log.info(String.format("%s: %s", k, v)));

    DefaultKubernetesClient kubernetesClient = new DefaultKubernetesClient();
    timer.scheduleAtFixedRate(new ServerDiscoveryTask(server, kubernetesClient, this), 1000, Long.parseLong(this.configuration.get("discovery-task-period-ms")));

    if (Boolean.parseBoolean(this.configuration.get("enable-join-listener"))) {
      server.getEventManager().register(this, new PlayerJoinListener(server));
    }
  }

  public String getConfigEntry(String key) {
    return this.configuration.get(key);
  }

  private void loadEnvironmentConfiguration() {
    Map<String, String> envDirectives = new HashMap<>();
    System.getenv().entrySet()
      .stream()
      .filter(entry -> entry.getKey().startsWith("KRYO_SV_"))
      .forEach(entry -> {
        // This transforms the key from the standard environment var format to our configuration directive format
        // Example: KRYO_SV_ENABLE_JOIN_LISTENER -> enable-join-listener
        String key = entry.getKey()
          .replaceFirst("KRYO_SV_", "")
          .toLowerCase().replace("_", "-");
        envDirectives.put(key, entry.getValue());
      });

    this.configurationDefaults
      .keySet()
      .forEach(key -> this.configuration.put(key, envDirectives.getOrDefault(key, this.configurationDefaults.get(key))));
  }
}