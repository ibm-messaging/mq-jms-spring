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

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes={MQConfigurationPropertiesJndi.class})
@TestPropertySource(properties = {
    "logging.level.root=INFO",
    "logging.level.com.ibm.mq.spring.boot=INFO"
})
public class MQConfigurationPropertiesJndiTest {

  @Autowired
  private MQConfigurationPropertiesJndi mQConfigurationPropertiesJndi;

  @Test
  public void testMQConfigurationPropertiesJndiNotNull() {
    Assertions.assertNotNull(mQConfigurationPropertiesJndi);
  }

  @Test
  public void testGetProviderUrl() {
    String providerUrl = "https://mykeycloak.server.com:32030/realms/master/protocol/openid-connect/token";
    Assertions.assertNull(mQConfigurationPropertiesJndi.getProviderUrl());
    mQConfigurationPropertiesJndi.setProviderUrl(providerUrl);
    Assertions.assertEquals(mQConfigurationPropertiesJndi.getProviderUrl(),providerUrl);
  }

  @Test
  public void testProviderContextFactory() {
    String providerFactory = "KeycloakServer";
    Assertions.assertNull(mQConfigurationPropertiesJndi.getProviderContextFactory());
    mQConfigurationPropertiesJndi.setProviderContextFactory(providerFactory);
    Assertions.assertEquals(mQConfigurationPropertiesJndi.getProviderContextFactory(),providerFactory);
  }

  @Test
  public void testAdditionalProperties() {
    Assertions.assertEquals(mQConfigurationPropertiesJndi.getAdditionalProperties().size(),0);
    Map<String,String> additionalProps = new HashMap<>();
    additionalProps.put("key", "value");
    mQConfigurationPropertiesJndi.setAdditionalProperties(additionalProps);
    Assertions.assertEquals(mQConfigurationPropertiesJndi.getAdditionalProperties().size(),1);
    Assertions.assertTrue(mQConfigurationPropertiesJndi.getAdditionalProperties().containsKey("key"));
    Assertions.assertTrue(mQConfigurationPropertiesJndi.getAdditionalProperties().containsValue("value"));
  }

  @Test
  public void testTrace() {
    Logger mockLogger = Mockito.mock(Logger.class);
    when(mockLogger.isTraceEnabled()).thenReturn(true);
    Map<String,String> additionalProps = new HashMap<>();
    additionalProps.put("server.port", "9090");
    additionalProps.put("application.name", "app");
    mQConfigurationPropertiesJndi.setAdditionalProperties(additionalProps);
    Assertions.assertEquals(mQConfigurationPropertiesJndi.getAdditionalProperties().size(),2);
    mQConfigurationPropertiesJndi.traceProperties("ConnFactory");
    reset();
  }

  private void reset() {
    mQConfigurationPropertiesJndi.setAdditionalProperties(new HashMap<>());
  }

}
