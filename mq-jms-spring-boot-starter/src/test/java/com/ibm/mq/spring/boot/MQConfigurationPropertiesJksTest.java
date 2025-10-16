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

@SpringBootTest(classes={MQConfigurationPropertiesJks.class})
@TestPropertySource(properties = {
    "logging.level.root=INFO",
    "logging.level.com.ibm.mq.spring.boot=INFO"
})
public class MQConfigurationPropertiesJksTest {

  @Autowired
  private MQConfigurationPropertiesJks propertiesJks;

  @Test
  public void testPropertiesNotNull() {
    Assertions.assertNotNull(propertiesJks);
  }

  @Test
  public void testKeyStoreProperty() {
    Assertions.assertNull(propertiesJks.getKeyStore());
    String certificateLocation ="$HOME/keycloakpublic.pem";
    propertiesJks.setKeyStore(certificateLocation);
    Assertions.assertEquals(propertiesJks.getKeyStore(),certificateLocation);
  }

  @Test
  public void testKeyStorePasswordProperty() {
    Assertions.assertNull(propertiesJks.getKeyStorePassword());
    String password = "password";
    propertiesJks.setKeyStorePassword(password);
    Assertions.assertEquals(propertiesJks.getKeyStorePassword(),password);
  }

  @Test
  public void testTrustStoreProperty() {
    Assertions.assertNull(propertiesJks.getTrustStore());
    String certificateLocation ="$HOME/keycloakpublic.pem";
    propertiesJks.setTrustStore(certificateLocation);
    Assertions.assertEquals(propertiesJks.getTrustStore(),certificateLocation);
  }

  @Test
  public void testTrustStorePasswordProperty() {
    Assertions.assertNull(propertiesJks.getTrustStorePassword());
    String password = "password";
    propertiesJks.setTrustStorePassword(password);
    Assertions.assertEquals(propertiesJks.getTrustStorePassword(),password);
  }

  @Test
  public void testTrace() {
    Logger mockLogger = Mockito.mock(Logger.class);
    propertiesJks.setKeyStorePassword("password");
    propertiesJks.setTrustStorePassword("password");
    when(mockLogger.isTraceEnabled()).thenReturn(true);
    Map<String,String> additionalProps = new HashMap<>();
    additionalProps.put("server.port", "9090");
    additionalProps.put("application.name", "app");
    propertiesJks.setAdditionalProperties(additionalProps);
    Assertions.assertTrue(propertiesJks.getAdditionalProperties().containsKey("server.port"));
    Assertions.assertTrue(propertiesJks.getAdditionalProperties().containsValue("9090"));
    Assertions.assertTrue(propertiesJks.getAdditionalProperties().containsKey("application.name"));
    Assertions.assertTrue(propertiesJks.getAdditionalProperties().containsValue("app"));
    propertiesJks.traceProperties();
    Assertions.assertEquals(propertiesJks.getAdditionalProperties().size(),2);
    reset();
  }

  private void reset() {
    propertiesJks.setKeyStorePassword(null);
    propertiesJks.setTrustStorePassword(null);
    propertiesJks.setAdditionalProperties(new HashMap<>());
  }
}
