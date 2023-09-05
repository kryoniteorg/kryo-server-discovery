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

  @Override
  public void run() {
    try {
      NamespaceList list = kubernetesClient.namespaces()
          .withLabel(LABEL_NAME, "true")
          .list();

      Set<ServerInfo> serverInfo = new HashSet<>();
      for (Namespace namespace : list.getItems()) {
        serverInfo.addAll(getMinecraftServersFromNamespace(namespace));
      }

      proxyServer.getAllServers().forEach(server -> proxyServer.unregisterServer(server.getServerInfo()));
      serverInfo.forEach(proxyServer::registerServer);
    } catch (Exception exception) {
      log.error("Failed to get servers", exception);
    }
  }

  private Set<ServerInfo> getMinecraftServersFromNamespace(Namespace namespace) {
    try {
      PodList podList = kubernetesClient.pods()
          .inNamespace(namespace.getMetadata().getName())
          .withLabel(LABEL_NAME, "true")
          .list();

      return getServerInfo(podList.getItems());
    } catch (Exception exception) {
      log.error("Failed to get servers", exception);
      return Collections.emptySet();
    }
  }

  private Set<ServerInfo> getServerInfo(List<Pod> pods) {
    return pods.stream()
      .map(pod -> {
        List<ContainerPort> ports = pod.getSpec()
          .getContainers()
          .stream()
          .map(ct -> ct.getPorts().stream().filter(port -> port.getName().equals("minecraft")).findFirst())
          .filter(Optional::isPresent)
          .map(Optional::get)
          .toList();

        String serverName = pod.getMetadata().getName();
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
