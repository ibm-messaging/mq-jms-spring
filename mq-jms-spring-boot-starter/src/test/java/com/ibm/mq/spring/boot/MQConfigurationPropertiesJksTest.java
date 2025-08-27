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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
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
		assertThat(propertiesJks).isNotNull();
	}
	
	@Test
	public void testKeyStoreProperty() {
		assertThat(propertiesJks.getKeyStore()).isNull();
		String certificateLocation ="$HOME/keycloakpublic.pem";
		propertiesJks.setKeyStore(certificateLocation);
		assertThat(propertiesJks.getKeyStore()).isEqualTo(certificateLocation);
	}
	
	@Test
	public void testKeyStorePasswordProperty() {
		assertThat(propertiesJks.getKeyStorePassword()).isNull();
		String password = "password";
		propertiesJks.setKeyStorePassword(password);
		assertThat(propertiesJks.getKeyStorePassword()).isEqualTo(password);
	}
	
	@Test
	public void testTrustStoreProperty() {
		assertThat(propertiesJks.getTrustStore()).isNull();
		String certificateLocation ="$HOME/keycloakpublic.pem";
		propertiesJks.setTrustStore(certificateLocation);
		assertThat(propertiesJks.getTrustStore()).isEqualTo(certificateLocation);
	}
	
	@Test
	public void testTrustStorePasswordProperty() {
		assertThat(propertiesJks.getTrustStorePassword()).isNull();
		String password = "password";
		propertiesJks.setTrustStorePassword(password);
		assertThat(propertiesJks.getTrustStorePassword()).isEqualTo(password);
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
	  assertTrue(propertiesJks.getAdditionalProperties().containsKey("server.port"));
	  assertTrue(propertiesJks.getAdditionalProperties().containsValue("9090"));
	  assertTrue(propertiesJks.getAdditionalProperties().containsKey("application.name"));
	  assertTrue(propertiesJks.getAdditionalProperties().containsValue("app"));
	  propertiesJks.traceProperties();
	  assertThat(propertiesJks.getAdditionalProperties().size()).isEqualTo(2);
	  reset();
	}

	private void reset() {
		propertiesJks.setKeyStorePassword(null);
		propertiesJks.setTrustStorePassword(null);
		propertiesJks.setAdditionalProperties(new HashMap<>());
	}
}
