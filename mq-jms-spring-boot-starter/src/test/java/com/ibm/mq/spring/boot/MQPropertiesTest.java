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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.ibm.msg.client.jakarta.wmq.WMQConstants;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={MQAutoConfiguration.class})
@TestPropertySource(properties = {
	    "logging.level.root=TRACE",
	    "logging.level.com.ibm.mq.spring.boot=TRACE"
	})
@EnableAutoConfiguration
@TestPropertySource(properties = {
        "ibm.mq.queueManager=QMSET",
        "ibm.mq.channel=CHANNELSET",
        "ibm.mq.connName=CONNSET",
        "ibm.mq.user=USER",
        "ibm.mq.password=PASS",
        "ibm.mq.clientId=mqm",
        "ibm.mq.applicationName=MQSpringTest",
        "ibm.mq.useIBMCipherMappings=true",
        "ibm.mq.userAuthenticationMQCSP=true",
        "ibm.mq.sslCipherSuite=CIPHER_SUITE",
        "ibm.mq.sslCipherSpec=CIPHER_SPEC",
        "ibm.mq.sslPeerName=CN=Mark,OU=IBM,C=GB",
        "ibm.mq.ccdtUrl=file:///home/admdata/ccdt1.tab",
        "ibm.mq.tokenServer.clientId=app",
        "ibm.mq.tokenServer.clientSecret=abcsecretxyz",
        "ibm.mq.tokenServer.endpoint=https://Keycloak.fyre:32030/realms/master/protocol/openid-connect",
        "ibm.mq.token=token",
        "ibm.mq.reconnect=true",
        "ibm.mq.reconnectTimeout=30",
        "ibm.mq.outboundSNI=outbound",
        "ibm.mq.channelSharing=DEV",
        "ibm.mq.sslBundle=ssl",
        "ibm.mq.sslFIPSRequired=true",
        "ibm.mq.sslCertificateValPolicy=true",
        "ibm.mq.sslKeyResetCount=2",
        "ibm.mq.balancingInstanceMode=3",
        "ibm.mq.tempTopicPrefix=3",
        "ibm.mq.tempModel=model"
})
public class MQPropertiesTest {

  @Autowired
  MQConfigurationProperties properties;

  @Autowired
  MQConnectionDetails connectionDetails;

    @Test
    public void test() {

        assertThat(properties.getQueueManager()).isEqualTo("QMSET");
        assertThat(properties.getChannel()).isEqualTo("CHANNELSET");
        assertThat(properties.getConnName()).isEqualTo("CONNSET");
        assertThat(properties.getUser()).isEqualTo("USER");
        assertThat(properties.getPassword()).isEqualTo("PASS");
        assertThat(properties.getClientId()).isEqualTo("mqm");
        assertThat(properties.getApplicationName()).isEqualTo("MQSpringTest");
        assertThat(properties.isUseIBMCipherMappings()).isEqualTo(true);
        assertThat(properties.isUseAuthenticationMQCSP()).isEqualTo(true);
        assertThat(System.getProperty("com.ibm.mq.cfg.useIBMCipherMappings")).isEqualTo("true");
        assertThat(properties.getSslCipherSuite()).isEqualTo("CIPHER_SUITE");
        assertThat(properties.getSslCipherSpec()).isEqualTo("CIPHER_SPEC");
        assertThat(properties.getSslPeerName()).isEqualTo("CN=Mark,OU=IBM,C=GB");
        assertThat(properties.getCcdtUrl()).isEqualTo("file:///home/admdata/ccdt1.tab");
        assertThat(properties.getTokenServer().getClientId()).isEqualTo("app");
        assertThat(properties.getTokenServer().getClientSecret().isBlank()).isFalse();
        assertThat(properties.getTokenServer().getEndpoint()).isEqualTo("https://Keycloak.fyre:32030/realms/master/protocol/openid-connect");
        assertThat(properties.getToken()).isEqualTo("token");
        assertThat(properties.getReconnect()).isEqualTo("true");
        assertThat(properties.getReconnectTimeout()).isEqualTo(30);
        assertThat(properties.getOutboundSNI()).isEqualTo("outbound");
        assertThat(properties.getChannelSharing()).isEqualTo("DEV");
        assertThat(properties.getSslBundle()).isEqualTo("ssl");
        assertThat(properties.getSslCertificateValPolicy()).isEqualTo("true");
        assertThat(properties.getSslKeyResetCount()).isEqualTo(2);
        assertThat(properties.getDefaultReconnect()).isNull();
        assertThat(properties.getBalancingInstanceMode()).isEqualTo("3");
        assertThat(properties.getTempTopicPrefix()).isEqualTo("3");
        assertThat(properties.getTempModel()).isEqualTo("model");
        assertThat(properties.getPool()).isNotNull();
    	assertThat(properties.getJks()).isNotNull();
    }

    @Test
    public void testGetAdditionalProperties() {
    	Map<String,String> additionalProps = new HashMap<>();
    	additionalProps.put("keycloak.port", "9890");
    	assertThat(properties.getAdditionalProperties().size()).isEqualTo(0);
    	properties.setAdditionalProperties(additionalProps);
    	assertThat(properties.getAdditionalProperties().size()).isEqualTo(1);
    }

    @Test
    public void testGetBalancingOptionsValue() {
    	assertThat(properties.getBalancingOptionsValue()).isEqualTo(0);
    	properties.setBalancingOptions(null);
    	assertThat(properties.getBalancingOptionsValue()).isEqualTo(0);
    	properties.setBalancingOptions("2,_3,NONE");
    	assertThat(properties.getBalancingOptionsValue()).isEqualTo(3);
    	properties.setBalancingOptions("2,_4,IGNORETRANS");
    	assertThat(properties.getBalancingOptionsValue()).isEqualTo(7);
    }

    @Test
    public void testGetRConnectValue() {
    	properties.setDefaultReconnect("QMGR");
    	assertThat(properties.getReconnectValue()).isEqualTo(WMQConstants.WMQ_CLIENT_RECONNECT_Q_MGR);
    	properties.setDefaultReconnect("DISABLED");
    	assertThat(properties.getReconnectValue()).isEqualTo(WMQConstants.WMQ_CLIENT_RECONNECT_DISABLED);
    	properties.setDefaultReconnect("YES");
    	assertThat(properties.getReconnectValue()).isEqualTo(WMQConstants.WMQ_CLIENT_RECONNECT);
    	properties.setDefaultReconnect("ANY");
    	assertThat(properties.getReconnectValue()).isEqualTo(WMQConstants.WMQ_CLIENT_RECONNECT);
    }

    @Test
    public void testGetBalancingTimeoutValue() {
    	assertThat(properties.getBalancingTimeoutValue()).isEqualTo(WMQConstants.WMQ_BALANCING_TIMEOUT_AS_DEFAULT);
    	properties.setBalancingTimeout(null);
    	assertThat(properties.getBalancingTimeoutValue()).isEqualTo(WMQConstants.WMQ_BALANCING_TIMEOUT_AS_DEFAULT);
    	properties.setBalancingTimeout("DEFAULT");
    	assertThat(properties.getBalancingTimeoutValue()).isEqualTo(WMQConstants.WMQ_BALANCING_TIMEOUT_AS_DEFAULT);
    	properties.setBalancingTimeout("IMMEDIATE");
    	assertThat(properties.getBalancingTimeoutValue()).isEqualTo(WMQConstants.WMQ_BALANCING_TIMEOUT_IMMEDIATE);
    	properties.setBalancingTimeout("NEVER");
    	assertThat(properties.getBalancingTimeoutValue()).isEqualTo(WMQConstants.WMQ_BALANCING_TIMEOUT_NEVER);
    	properties.setBalancingTimeout("10");
    	assertThat(properties.getBalancingTimeoutValue()).isEqualTo(10);
    	properties.setBalancingTimeout("PT20S");
    	assertThat(properties.getBalancingTimeoutValue()).isEqualTo(20);
    	properties.setBalancingTimeout("2S");
    	assertThat(properties.getBalancingTimeoutValue()).isEqualTo(2);
    }

    @Test
    public void testIsSslCertificateValidationNone() {
    	properties.setSslCertificateValPolicy("none");
    	assertTrue(properties.isSslCertificateValidationNone());
    	properties.setSslCertificateValPolicy("policy");
    	assertFalse(properties.isSslCertificateValidationNone());
    }

    @Test
    public void testGetBalancingApplicationTypeValue() {
    	properties.setBalancingApplicationType(null);
    	assertThat(properties.getBalancingApplicationTypeValue()).isEqualTo(0);
    	properties.setBalancingApplicationType("");
    	assertThat(properties.getBalancingApplicationTypeValue()).isEqualTo(0);
    	properties.setBalancingApplicationType("REQREP");
    	assertThat(properties.getBalancingApplicationTypeValue()).isEqualTo(WMQConstants.WMQ_BALANCING_APPLICATION_TYPE_REQUEST_REPLY);
    	properties.setBalancingApplicationType("REQUESTREPLY");
    	assertThat(properties.getBalancingApplicationTypeValue()).isEqualTo(WMQConstants.WMQ_BALANCING_APPLICATION_TYPE_REQUEST_REPLY);
    	properties.setBalancingApplicationType("SIMPLE");
    	assertThat(properties.getBalancingApplicationTypeValue()).isEqualTo(WMQConstants.WMQ_BALANCING_APPLICATION_TYPE_SIMPLE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetBalancingApplicationTypeValueThrowsException() {
    	properties.setBalancingApplicationType("abcbghgsdds");
    	properties.getBalancingApplicationTypeValue();
    }

    @Test
    public void testSetUseAuthenticationMQCSP() {
    	properties.setUseAuthenticationMQCSP(true);
    	assertTrue(properties.isUseAuthenticationMQCSP());
    }

    @Test
    public void testTempQPrefix() {
    	properties.setTempQPrefix("prefix");
    	assertThat(properties.getTempQPrefix()).isEqualTo("prefix");
    }


    @Test
	public void testTrace() {
	  Logger mockLogger = Mockito.mock(Logger.class);
	  when(mockLogger.isTraceEnabled()).thenReturn(true);
	  Map<String,String> additionalProps = new HashMap<>();
	  additionalProps.put("server.port", "9090");
	  additionalProps.put("application.name", "app");
	  properties.setAdditionalProperties(additionalProps);
	  assertThat(properties.getAdditionalProperties().size()).isEqualTo(2);
	  properties.traceProperties(connectionDetails);
	  reset();
	}

	private void reset() {
		properties.setAdditionalProperties(new HashMap<>());
	}
}
