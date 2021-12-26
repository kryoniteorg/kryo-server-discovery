package net.citybuild.serverdiscovery.serverdiscovery;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.velocitypowered.api.proxy.ProxyServer;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.kubernetes.client.dsl.Resource;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ServerDiscoveryTaskTest {

  @InjectMocks
  private ServerDiscoveryTask testee;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ProxyServer proxyServerMock;

  @Mock
  private DefaultKubernetesClient kubernetesClientMock;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private NonNamespaceOperation<Namespace, NamespaceList, Resource<Namespace>> namespaceOperationMock;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private MixedOperation<Pod, PodList, PodResource<Pod>> mixedOperationMock;


  @Test
  void shouldDiscoverServers() {
    // Arrange
    String expectedName = "testServer";
    String expectedHostName = "192.1.1.1";

    NamespaceList namespaceList = mock(NamespaceList.class, Answers.RETURNS_DEEP_STUBS);

    when(kubernetesClientMock.namespaces()).thenReturn(namespaceOperationMock);
    when(namespaceOperationMock.withLabel(ServerDiscoveryTask.LABEL_NAME, "true").list()).thenReturn(namespaceList);

    Namespace namespace = mock(Namespace.class, Answers.RETURNS_DEEP_STUBS);
    when(namespaceList.getItems()).thenReturn(List.of(namespace));

    PodList podList = mock(PodList.class);

    when(kubernetesClientMock.pods()).thenReturn(mixedOperationMock);
    when(mixedOperationMock.inNamespace(namespace.getMetadata().getName())
        .withLabel(ServerDiscoveryTask.LABEL_NAME, "true").list())
        .thenReturn(podList);

    Pod pod = mock(Pod.class, Answers.RETURNS_DEEP_STUBS);
    when(podList.getItems()).thenReturn(List.of(pod));
    when(pod.getMetadata().getName()).thenReturn(expectedName);
    when(pod.getStatus().getPodIP()).thenReturn(expectedHostName);

    // Act
    testee.run();

    // Assert
    verify(proxyServerMock).registerServer(
        argThat(argument -> expectedName.equals(argument.getName())
            && expectedHostName.equals(argument.getAddress().getHostName())));
  }
}