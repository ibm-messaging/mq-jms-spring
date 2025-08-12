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

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes={MQConfigurationPropertiesJndi.class})
@RunWith(SpringRunner.class)
@TestPropertySource(properties = {
	    "logging.level.root=TRACE",
	    "logging.level.com.ibm.mq.spring.boot=TRACE"
	})
public class MQConfigurationPropertiesJndiTest {

	@Autowired
	private MQConfigurationPropertiesJndi mQConfigurationPropertiesJndi;
	
	@Test
	public void testMQConfigurationPropertiesJndiNotNull() {
		assertThat(mQConfigurationPropertiesJndi).isNotNull();
	}
	
	@Test
	public void testGetProviderUrl() {
		String providerUrl = "https://mykeycloak.server.com:32030/realms/master/protocol/openid-connect/token";
		assertThat(mQConfigurationPropertiesJndi.getProviderUrl()).isNull();
		mQConfigurationPropertiesJndi.setProviderUrl(providerUrl);
		assertThat(mQConfigurationPropertiesJndi.getProviderUrl()).isEqualTo(providerUrl);
	}
	
	@Test
	public void testProviderContextFactory() {
		String providerFactory = "KeycloakServer";
		assertThat(mQConfigurationPropertiesJndi.getProviderContextFactory()).isNull();
		mQConfigurationPropertiesJndi.setProviderContextFactory(providerFactory);
		assertThat(mQConfigurationPropertiesJndi.getProviderContextFactory()).isEqualTo(providerFactory);
	}
	
	@Test
	public void testAdditionalProperties() {
		assertThat(mQConfigurationPropertiesJndi.getAdditionalProperties().size()).isEqualTo(0);
		Map<String,String> additionalProps = new HashMap<>();
		additionalProps.put("key", "value");
		mQConfigurationPropertiesJndi.setAdditionalProperties(additionalProps);
		assertThat(mQConfigurationPropertiesJndi.getAdditionalProperties().size()).isEqualTo(1);
		assertTrue(mQConfigurationPropertiesJndi.getAdditionalProperties().containsKey("key"));
		assertTrue(mQConfigurationPropertiesJndi.getAdditionalProperties().containsValue("value"));
	}
	
	@Test
	public void testTrace() {
		 Logger mockLogger = Mockito.mock(Logger.class);
		 when(mockLogger.isTraceEnabled()).thenReturn(true);
	     Map<String,String> additionalProps = new HashMap<>();
		 additionalProps.put("server.port", "9090");
		 additionalProps.put("application.name", "app");
		 mQConfigurationPropertiesJndi.setAdditionalProperties(additionalProps);
		 assertThat(mQConfigurationPropertiesJndi.getAdditionalProperties().size()).isEqualTo(2);
		 mQConfigurationPropertiesJndi.traceProperties("ConnFactory");
		 reset();
	}

	private void reset() {
		mQConfigurationPropertiesJndi.setAdditionalProperties(new HashMap<>());
	}
	
}
