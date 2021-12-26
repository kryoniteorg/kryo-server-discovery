package net.citybuild.serverdiscovery.serverdiscovery;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
        try {
          PodList podList = kubernetesClient.pods()
              .inNamespace(namespace.getMetadata().getName())
              .withLabel(LABEL_NAME, "true")
              .list();

          serverInfo.addAll(getServerInfo(podList.getItems()));
        } catch (Exception exception) {
          log.error("Failed to get servers", exception);
        }
      }

      proxyServer.getAllServers().forEach(server -> proxyServer.unregisterServer(server.getServerInfo()));
      serverInfo.forEach(proxyServer::registerServer);
    } catch (Exception exception) {
      log.error("Failed to get servers", exception);
    }
  }

  private Set<ServerInfo> getServerInfo(List<Pod> pods) {
    return pods.stream()
        .map(pod -> new ServerInfo(
                pod.getMetadata().getName(),
                new InetSocketAddress(pod.getStatus().getPodIP(), 25565)
            )
        )
        .collect(Collectors.toSet());
  }
}
