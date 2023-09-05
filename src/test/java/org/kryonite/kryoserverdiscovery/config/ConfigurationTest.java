package org.kryonite.kryoserverdiscovery.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kryonite.kryoserverdiscovery.KryoServerDiscoveryPlugin;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class ConfigurationTest {

  @InjectMocks
  private KryoServerDiscoveryPlugin testee;

  @Test
  void testConfig() {
    Map<String, String> environmentVariables = new HashMap<>();
    environmentVariables.put("KRYO_SV_ENABLE_JOIN_LISTENER", "true");

    // Act
    testee.loadConfiguration(environmentVariables);

    // Assert
    assertEquals(testee.getConfigEntry("enable-join-listener"), "true");

  }
}
