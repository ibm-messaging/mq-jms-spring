/*
 * Copyright © 2025 IBM Corp. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package com.ibm.mq.spring.boot;

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes={MQConfigurationPropertiesTokenServer.class})
@TestPropertySource(properties = {
    "logging.level.root=INFO",
    "logging.level.com.ibm.mq.spring.boot=INFO"
})
public class MQConfigurationPropertiesTokenServerTest {


  @Autowired
  private MQConfigurationPropertiesTokenServer mqConfigurationPropertiesTokenServer;

  @Test
  public void testMqConfigurationPropertiesTokenServer() {
    Assertions.assertNotNull(mqConfigurationPropertiesTokenServer);
  }

  @Test
  public void testGetEndPoint() {
    String tokenEndpoint = "https://mykeycloak.server.com:32030/realms/master/protocol/openid-connect/token";
    Assertions.assertNull(mqConfigurationPropertiesTokenServer.getEndpoint());
    mqConfigurationPropertiesTokenServer.setEndpoint(tokenEndpoint);
    Assertions.assertEquals(mqConfigurationPropertiesTokenServer.getEndpoint(),tokenEndpoint);
  }

  @Test
  public void testGetClientId() {
    String clientId = "jms-client";
    Assertions.assertNull(mqConfigurationPropertiesTokenServer.getClientId());
    mqConfigurationPropertiesTokenServer.setClientId(clientId);
    Assertions.assertEquals(mqConfigurationPropertiesTokenServer.getClientId(),clientId);
  }

  @Test
  public void testGetClientSecret() {
    String secret = "fioOBXlM7yg8q6trJi1xpjR5smSA8WRp";
    Assertions.assertNull(mqConfigurationPropertiesTokenServer.getClientSecret());
    mqConfigurationPropertiesTokenServer.setClientSecret(secret);
    Assertions.assertEquals(mqConfigurationPropertiesTokenServer.getClientSecret(),secret);
  }


  @Test
  public void testTrace() {
    Logger mockLogger = Mockito.mock(Logger.class);
    when(mockLogger.isTraceEnabled()).thenReturn(true);
    mqConfigurationPropertiesTokenServer.traceProperties(mockLogger);
  }
}
