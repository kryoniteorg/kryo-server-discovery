package org.kryonite.kryoserverdiscovery.serverdiscovery;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kryonite.kryoserverdiscovery.KryoServerDiscoveryPlugin;

@Slf4j
@RequiredArgsConstructor
public class ServerDiscoveryTask extends TimerTask {

  protected static final String LABEL_NAME = "server-discovery";

  private final ProxyServer proxyServer;
  private final DefaultKubernetesClient kubernetesClient;
  private final Map<String, String> configuration;
  private final Set<String> discoveredServers = new HashSet<>();

  @Override
  public void run() {
    try {
      NamespaceList list = kubernetesClient.namespaces()
        .withLabel(LABEL_NAME, "true")
        .list();

      Set<ServerInfo> serverInfo = new HashSet<>();
      for (Namespace namespace : list.getItems()) {
        serverInfo.addAll(getServerInfosOfNamespace(namespace));
      }

      proxyServer
        .getAllServers()
        .stream()
        .filter(server-> discoveredServers.remove(server.getServerInfo().getName()))
        .forEach(server -> proxyServer.unregisterServer(server.getServerInfo()));

      serverInfo.forEach(info -> {
        discoveredServers.add(info.getName());
        proxyServer.registerServer(info);
      });
    } catch (Exception exception) {
      log.error("Failed to get servers", exception);
    }
  }

  private Set<ServerInfo> getServerInfosOfNamespace(Namespace namespace) {
    try {
      List<Pod> podList = kubernetesClient.pods()
        .inNamespace(namespace.getMetadata().getName())
        .withLabel(LABEL_NAME, "true")
        .list().getItems();

      if (podList.isEmpty()) {
        log.warn(String.format("Labeled namespace '%s' has no labeled pods!", namespace.getMetadata().getName()));
      }

      return getServerInfos(podList);
    } catch (Exception exception) {
      log.error("Failed to get servers", exception);
      return Collections.emptySet();
    }
  }

  private Set<ServerInfo> getServerInfos(List<Pod> pods) {
    return pods.stream()
      .map(pod -> {
        List<ContainerPort> ports = pod.getSpec()
          .getContainers()
          .stream()
          .map(ct -> ct.getPorts().stream().filter(port -> port.getName().equals("minecraft")).findFirst())
          .filter(Optional::isPresent)
          .map(Optional::get)
          .toList();

        String serverName = String.format(this.configuration.get("server-name-format"), pod.getMetadata().getName());
        // Probably a misconfiguration if there is more than one container with a minecraft port
        if (ports.size() == 1) {
          ContainerPort port = ports.get(0);
          return new ServerInfo(serverName, new InetSocketAddress(pod.getStatus().getPodIP(), port.getHostPort()));
        }

        log.warn(String.format("None or multiple containers in pod '%s' have a port named 'minecraft', using default port 25565", pod.getMetadata().getName()));

        return new ServerInfo(serverName, new InetSocketAddress(pod.getStatus().getPodIP(), 25565));
      })
      .collect(Collectors.toSet());
  }
}
