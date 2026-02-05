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
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.ibm.msg.client.jakarta.wmq.WMQConstants;

@SpringBootTest(classes={MQAutoConfiguration.class})
@TestPropertySource(properties = {
    "logging.level.root=INFO",
    "logging.level.com.ibm.mq.spring.boot=INFO"
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
    "ibm.mq.tempModel=model",
    "ibm.mq.ccdtHttpsCertValPolicy=ANY",
    "ibm.mq.ccdtSslBundle=ccdt"
})
public class MQPropertiesTest {

  @Autowired
  MQConfigurationProperties properties;

  @Autowired
  MQConnectionDetails connectionDetails;

  @Test
  public void test() {

    Assertions.assertEquals(properties.getQueueManager(),"QMSET");
    Assertions.assertEquals(properties.getChannel(),"CHANNELSET");
    Assertions.assertEquals(properties.getConnName(),"CONNSET");
    Assertions.assertEquals(properties.getUser(),"USER");
    Assertions.assertEquals(properties.getPassword(),"PASS");
    Assertions.assertEquals(properties.getClientId(),"mqm");
    Assertions.assertEquals(properties.getApplicationName(),"MQSpringTest");
    Assertions.assertEquals(properties.isUseIBMCipherMappings(),true);
    Assertions.assertEquals(properties.isUseAuthenticationMQCSP(),true);
    Assertions.assertEquals(System.getProperty("com.ibm.mq.cfg.useIBMCipherMappings"),"true");
    Assertions.assertEquals(properties.getSslCipherSuite(),"CIPHER_SUITE");
    Assertions.assertEquals(properties.getSslCipherSpec(),"CIPHER_SPEC");
    Assertions.assertEquals(properties.getSslPeerName(),"CN=Mark,OU=IBM,C=GB");
    Assertions.assertEquals(properties.getCcdtUrl(),"file:///home/admdata/ccdt1.tab");
    Assertions.assertEquals(properties.getTokenServer().getClientId(),"app");
    Assertions.assertFalse(properties.getTokenServer().getClientSecret().isBlank());
    Assertions.assertEquals(properties.getTokenServer().getEndpoint(),"https://Keycloak.fyre:32030/realms/master/protocol/openid-connect");
    Assertions.assertEquals(properties.getToken(),"token");
    Assertions.assertEquals(properties.getReconnect(),"true");
    Assertions.assertEquals(properties.getReconnectTimeout(),30);
    Assertions.assertEquals(properties.getOutboundSNI(),"outbound");
    Assertions.assertEquals(properties.getChannelSharing(),"DEV");
    Assertions.assertEquals(properties.getSslBundle(),"ssl");
    Assertions.assertEquals(properties.getSslCertificateValPolicy(),"true");
    Assertions.assertEquals(properties.getSslKeyResetCount(),2);
    Assertions.assertNull(properties.getDefaultReconnect());
    Assertions.assertEquals(properties.getBalancingInstanceMode(),"3");
    Assertions.assertEquals(properties.getTempTopicPrefix(),"3");
    Assertions.assertEquals(properties.getTempModel(),"model");
    Assertions.assertNotNull(properties.getPool());
    Assertions.assertNotNull(properties.getJks());
    Assertions.assertEquals(properties.getCcdtSslBundle(),"ccdt");
    Assertions.assertEquals(properties.getCcdtHttpsCertValPolicy(),"ANY");
  }

  @Test
  public void testGetAdditionalProperties() {
    Map<String,String> additionalProps = new HashMap<>();
    additionalProps.put("keycloak.port", "9890");
    Assertions.assertEquals(properties.getAdditionalProperties().size(),0);
    properties.setAdditionalProperties(additionalProps);
    Assertions.assertEquals(properties.getAdditionalProperties().size(),1);
  }

  @Test
  public void testGetBalancingOptionsValue() {
    Assertions.assertEquals(properties.getBalancingOptionsValue(),0);
    properties.setBalancingOptions(null);
    Assertions.assertEquals(properties.getBalancingOptionsValue(),0);
    properties.setBalancingOptions("2,_3,NONE");
    Assertions.assertEquals(properties.getBalancingOptionsValue(),3);
    properties.setBalancingOptions("2,_4,IGNORETRANS");
    Assertions.assertEquals(properties.getBalancingOptionsValue(),7);
  }

  @Test
  public void testGetRConnectValue() {
    properties.setDefaultReconnect("QMGR");
    Assertions.assertEquals(properties.getReconnectValue(),WMQConstants.WMQ_CLIENT_RECONNECT_Q_MGR);
    properties.setDefaultReconnect("DISABLED");
    Assertions.assertEquals(properties.getReconnectValue(),WMQConstants.WMQ_CLIENT_RECONNECT_DISABLED);
    properties.setDefaultReconnect("YES");
    Assertions.assertEquals(properties.getReconnectValue(),WMQConstants.WMQ_CLIENT_RECONNECT);
    properties.setDefaultReconnect("ANY");
    Assertions.assertEquals(properties.getReconnectValue(),WMQConstants.WMQ_CLIENT_RECONNECT);
  }

  @Test
  public void testGetBalancingTimeoutValue() {
    Assertions.assertEquals(properties.getBalancingTimeoutValue(),WMQConstants.WMQ_BALANCING_TIMEOUT_AS_DEFAULT);
    properties.setBalancingTimeout(null);
    Assertions.assertEquals(properties.getBalancingTimeoutValue(),WMQConstants.WMQ_BALANCING_TIMEOUT_AS_DEFAULT);
    properties.setBalancingTimeout("DEFAULT");
    Assertions.assertEquals(properties.getBalancingTimeoutValue(),WMQConstants.WMQ_BALANCING_TIMEOUT_AS_DEFAULT);
    properties.setBalancingTimeout("IMMEDIATE");
    Assertions.assertEquals(properties.getBalancingTimeoutValue(),WMQConstants.WMQ_BALANCING_TIMEOUT_IMMEDIATE);
    properties.setBalancingTimeout("NEVER");
    Assertions.assertEquals(properties.getBalancingTimeoutValue(),WMQConstants.WMQ_BALANCING_TIMEOUT_NEVER);
    properties.setBalancingTimeout("10");
    Assertions.assertEquals(properties.getBalancingTimeoutValue(),10);
    properties.setBalancingTimeout("PT20S");
    Assertions.assertEquals(properties.getBalancingTimeoutValue(),20);
    properties.setBalancingTimeout("2S");
    Assertions.assertEquals(properties.getBalancingTimeoutValue(),2);
  }

  @Test
  public void testIsSslCertificateValidationNone() {
    properties.setSslCertificateValPolicy("none");
    Assertions.assertTrue(properties.isSslCertificateValidationNone());
    properties.setSslCertificateValPolicy("policy");
    Assertions.assertFalse(properties.isSslCertificateValidationNone());
  }

  @Test
  public void testGetBalancingApplicationTypeValue() {
    properties.setBalancingApplicationType(null);
    Assertions.assertEquals(properties.getBalancingApplicationTypeValue(),0);
    properties.setBalancingApplicationType("");
    Assertions.assertEquals(properties.getBalancingApplicationTypeValue(),0);
    properties.setBalancingApplicationType("REQREP");
    Assertions.assertEquals(properties.getBalancingApplicationTypeValue(),WMQConstants.WMQ_BALANCING_APPLICATION_TYPE_REQUEST_REPLY);
    properties.setBalancingApplicationType("REQUESTREPLY");
    Assertions.assertEquals(properties.getBalancingApplicationTypeValue(),WMQConstants.WMQ_BALANCING_APPLICATION_TYPE_REQUEST_REPLY);
    properties.setBalancingApplicationType("SIMPLE");
    Assertions.assertEquals(properties.getBalancingApplicationTypeValue(),WMQConstants.WMQ_BALANCING_APPLICATION_TYPE_SIMPLE);
  }

  @Test
  public void testGetBalancingApplicationTypeValueThrowsException() {
    Assertions.assertThrows(IllegalArgumentException.class,() -> {
      properties.setBalancingApplicationType("abcbghgsdds");
      properties.getBalancingApplicationTypeValue();
    });
  }

  @Test
  public void testSetUseAuthenticationMQCSP() {
    properties.setUseAuthenticationMQCSP(true);
    Assertions.assertTrue(properties.isUseAuthenticationMQCSP());
  }

  @Test
  public void testTempQPrefix() {
    properties.setTempQPrefix("prefix");
    Assertions.assertEquals(properties.getTempQPrefix(),"prefix");
  }


  @Test
  public void testTrace() {
    Logger mockLogger = Mockito.mock(Logger.class);
    when(mockLogger.isTraceEnabled()).thenReturn(true);
    Map<String,String> additionalProps = new HashMap<>();
    additionalProps.put("server.port", "9090");
    additionalProps.put("application.name", "app");
    properties.setAdditionalProperties(additionalProps);
    Assertions.assertEquals(properties.getAdditionalProperties().size(),2);
    properties.traceProperties(connectionDetails);
    reset();
  }

  private void reset() {
    properties.setAdditionalProperties(new HashMap<>());
  }
}
