/*
 * Copyright © 2018,2021 IBM Corp. All rights reserved.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jms.JmsPoolConnectionFactoryProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import com.ibm.msg.client.wmq.common.CommonConstants;

/**
 * There are many properties that can be set on an MQ Connection Factory, but these are the most commonly-used
 * for both direct and client connections. If you use TLS for client connectivity, most properties related to that
 * (keystore, certificates etc) must be set independently.
 * <p>
 * This class allows for setting the CipherSuite/CipherSpec property, and an indication of whether or not
 * to use the IBM JRE maps for Cipher names - that's not something that is standardised.
 * <p>
 * The default values have been set to match the settings of the
 * <a href="https://github.com/ibm-messaging/mq-docker">MQ Docker</a>
 * container.
 *
 * <ul>
 * <li>queueManager = QM1
 * <li>connName = localhost(1414)
 * <li>channel = DEV.ADMIN.SVRCONN
 * <li>user = admin
 * <li>password = passw0rd
 * </ul>
 */
@ConfigurationProperties(prefix = "ibm.mq")
public class MQConfigurationProperties {

  private static Logger logger = LoggerFactory.getLogger(MQConfigurationProperties.class);

  // Some system properties that may be set through this package. They are not regular CF properties, but still
  // affect how connections are made.
  private static final String PROPERTY_USE_IBM_CIPHER_MAPPINGS = "com.ibm.mq.cfg.useIBMCipherMappings";
  private static final String PROPERTY_OUTBOUND_SNI = "com.ibm.mq.cfg.SSL.outboundSNI";

  /**
   * MQ Queue Manager name
   */
  private String queueManager = "QM1";

  /**
   * Channel - for example "SYSTEM.DEF.SVRCONN"
   **/
  private String channel = "DEV.ADMIN.SVRCONN";

  /**
   * Connection Name - hostname or address and port. Can be comma-separated list.
   * Format like 'system.example.com(1414),system2.example.com(1414)'
   **/
  private String connName = "localhost(1414)";

  /**
   * MQ Client ID
   */
  private String clientId;

  /**
   * MQ Application Name
   */
  private String applicationName;

  /**
   * MQ user name
   */
  private String user = "admin";

  /**
   * MQ password
   */
  private String password = "passw0rd";

  /**
   * Override the authentication mode. This
   * should not normally be needed with current maintenance levels of MQ V8 or V9, but some earlier levels
   * sometimes got get it wrong and then this flag can be set to "false".
   *
   * @see <a href="https://www.ibm.com/support/knowledgecenter/en/SSFKSJ_latest/com.ibm.mq.sec.doc/q118680_.htm">the KnowledgeCenter</a>
   */
  private boolean userAuthenticationMQCSP = true;

  /**
   * For TLS connections, you can set either the sslCipherSuite or sslCipherSpec property.
   * For example, "SSL_ECDHE_RSA_WITH_AES_256_GCM_SHA384"
   *
   * @see <a href="https://www.ibm.com/support/knowledgecenter/en/SSFKSJ_latest/com.ibm.mq.dev.doc/q113210_.htm">the KnowledgeCenter</a>
   */
  private String sslCipherSuite;

  /**
   * For TLS connections, you can set either the sslCipherSuite or sslCipherSpec property.
   * For example, "ECDHE_RSA_AES_256_GCM_SHA384"
   *
   * @see <a href="https://www.ibm.com/support/knowledgecenter/en/SSFKSJ_latest/com.ibm.mq.dev.doc/q113210_.htm">the KnowledgeCenter</a>
   */
  private String sslCipherSpec;

  /**
   * Type a distinguished name skeleton that must match that provided by the queue manager.
   *
   * @see <a href="https://www.ibm.com/support/knowledgecenter/en/SSFKSJ_latest/com.ibm.mq.dev.doc/q112720_.htm">the KnowledgeCenter</a>
   */
  private String sslPeerName;

  /**
   * Set to true for the IBM JRE CipherSuite name maps; set to false to use the Oracle JRE CipherSuite mapping
   */
  private boolean useIBMCipherMappings = true;

  /**
   * Set to HOSTNAME for connection to OpenShift queue managers where SNI is important. CHANNEL
   * can be used for environments where you want to have different certificates associated with different
   * channels. The property does not get set unless explicitly configured as an external property so we would
   * otherwise use whatever the default behaviour is in the JMS client.
   */
  private String outboundSNI = ""; // HOSTNAME or CHANNEL are the valid alternatives

  /**
   * Whether to automatically reconnect to the qmgr when in client mode. Values can be YES/NO/QMGR/DISABLED.
   */
  private String defaultReconnect = "";

  /**
   * Enter the uniform resource locator (URL) that identifies the name and location of the file that contains
   * the client channel definition table and specifies how the file can be accessed.
   * You must set a value for either the Channel property or for the Client Channel Definition Table URL property but not both.
   * For example, "file:///home/admdata/ccdt1.tab"
   *
   * @see <a href="https://www.ibm.com/support/knowledgecenter/en/SSFKSJ_latest/com.ibm.mq.dev.doc/q032510_.html">the KnowledgeCenter</a>
   */
  private String ccdtUrl;

  /**
   * The prefix to be used to form the name of an MQ dynamic queue.
   */
  private String tempQPrefix = null;

  /**
   * The prefix to be used to form the name of an MQ dynamic topic.
   */
  private String tempTopicPrefix = null;

  /**
   * The name of a model queue for creating temporary destinations.
   */
  private String tempModel = null;

  /**
   * Indicates whether FIPS-certified algorithms must be used
   */
  private boolean sslFIPSRequired = false;

  /**
   * The reset count of the SSL key.
   */
  private int sslKeyResetCount = -1;


  /**
   * Additional CF properties that are not explicitly known can be provided
   * with the format "ibm.mq.additionalProperties.SOME_PROPERTY=SOME_VALUE". Strings,
   * integers and true/false values are recognised.
   *
   * The property is either the actual string for the MQ property, and will usually begin with "XMSC"
   * Or it can be the name of the variable in the WMQConstants class. So for example,
   * setting the name of a security exit would usually be done in code with
   * setStringProperty(WMQConstants.WMQ_SECURITY_EXIT). The value of that constant is
   * "XMSC_WMQ_SECURITY_EXIT" so the external property to set can be either
   *   "ibm.mq.additionalProperties.XMSC_WMQ_SECURITY_EXIT=com.example.SecExit"
   * or
   *   "ibm.mq.additionalProperties.WMQ_SECURITY_EXIT=com.example.SecExit"
   *
   */
  private Map<String, String> additionalProperties = new HashMap<String,String>();

  @NestedConfigurationProperty
  private JmsPoolConnectionFactoryProperties pool = new JmsPoolConnectionFactoryProperties();

  @NestedConfigurationProperty
  private MQConfigurationPropertiesJndi jndi = new MQConfigurationPropertiesJndi();

  public String getQueueManager() {
    return queueManager;
  }

  public void setQueueManager(String queueManager) {
    this.queueManager = queueManager;
  }

  public String getChannel() {
    return channel;
  }

  public void setChannel(String channel) {
    this.channel = channel;
  }

  public String getConnName() {
    return connName;
  }

  public void setConnName(String connName) {
    this.connName = connName;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getClientId() {
    return clientId;
  }

  public void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }

  public String getApplicationName() {
    return applicationName;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getSslCipherSuite() {
    return sslCipherSuite;
  }

  public void setSslCipherSuite(String sslCipherSuite) {
    this.sslCipherSuite = sslCipherSuite;
  }

  public String getSslCipherSpec() {
    return sslCipherSpec;
  }

  public void setSslCipherSpec(String sslCipherSpec) {
    this.sslCipherSpec = sslCipherSpec;
  }

  public boolean isUseIBMCipherMappings() {
    return useIBMCipherMappings;
  }

  public void setUseIBMCipherMappings(boolean useIBMCipherMappings) {
    System.setProperty(PROPERTY_USE_IBM_CIPHER_MAPPINGS, Boolean.toString(useIBMCipherMappings));
    this.useIBMCipherMappings = useIBMCipherMappings;
  }

  public String getOutboundSNI() {
    return outboundSNI;
  }

  public void setOutboundSNI(String outboundSNI) {
    System.setProperty(PROPERTY_OUTBOUND_SNI, outboundSNI);
    this.outboundSNI = outboundSNI;
  }

  public boolean isUserAuthenticationMQCSP() {
    return userAuthenticationMQCSP;
  }

  public void setUserAuthenticationMQCSP(boolean userAuthenticationMQCSP) {
    this.userAuthenticationMQCSP = userAuthenticationMQCSP;
  }

  public String getSslPeerName() {
    return sslPeerName;
  }

  public void setSslPeerName(String sslPeerName) {
    this.sslPeerName = sslPeerName;
  }

  public String getCcdtUrl() {
    return ccdtUrl;
  }

  public void setCcdtUrl(String ccdtUrl) {
    this.ccdtUrl = ccdtUrl;
  }

  public JmsPoolConnectionFactoryProperties getPool() {
    return pool;
  }

  public MQConfigurationPropertiesJndi getJndi() {
    return jndi;
  }

  public String getTempQPrefix() {
    return tempQPrefix;
  }

  public void setTempQPrefix(String tempQPrefix) {
    this.tempQPrefix = tempQPrefix;
  }

  public String getTempTopicPrefix() {
    return tempTopicPrefix;
  }

  public void setTempTopicPrefix(String tempTopicPrefix) {
    this.tempTopicPrefix = tempTopicPrefix;
  }

  public String getTempModel() {
    return tempModel;
  }

  public void setTempModel(String tempModel) {
    this.tempModel = tempModel;
  }

  public boolean isSslFIPSRequired() {
    return sslFIPSRequired;
  }

  public void setSslFIPSRequired(boolean sslFIPSRequired) {
    this.sslFIPSRequired = sslFIPSRequired;
  }

  public int getSslKeyResetCount() {
    return sslKeyResetCount;
  }

  public void setSslKeyResetCount(int sslKeyResetCount) {
    this.sslKeyResetCount = sslKeyResetCount;
  }

  public int getDefaultReconnectValue() {
    int rc = 0;
    switch (defaultReconnect.toUpperCase()) {
    case "QMGR":
      rc = CommonConstants.WMQ_CLIENT_RECONNECT_Q_MGR;
      break;
    case "DISABLED":
    case "NO":
      rc = CommonConstants.WMQ_CLIENT_RECONNECT_DISABLED;
      break;
    case "YES":
    case "ANY":
      rc = CommonConstants.WMQ_CLIENT_RECONNECT;
      break;
    default:
      rc = CommonConstants.WMQ_CLIENT_RECONNECT_AS_DEF;
      break;
    }
    return rc;
  }

  public String getDefaultReconnect() {
    return defaultReconnect;
  }
  public void setDefaultReconnect(String defaultReconnect) {
    this.defaultReconnect = defaultReconnect;
  }

  public Map<String, String> getAdditionalProperties() {
    return additionalProperties;
  }

  public void setAdditionalProperties(Map<String, String> properties) {
    this.additionalProperties = properties;
  }

  public void traceProperties() {
    if (!logger.isTraceEnabled())
      return;

    logger.trace("queueManager    : {}", getQueueManager());
    logger.trace("applicationName : {}", getApplicationName());
    logger.trace("ccdtUrl         : {}", getCcdtUrl());
    logger.trace("channel         : {}", getChannel());
    logger.trace("clientId        : {}", getClientId());
    logger.trace("connName        : {}", getConnName());
    logger.trace("defaultReconnect: \'{}\' [{}]", getDefaultReconnect(),String.format("0x%08X",getDefaultReconnectValue()));
    logger.trace("sslCipherSpec   : {}", getSslCipherSpec());
    logger.trace("sslCipherSuite  : {}", getSslCipherSuite());
    logger.trace("sslKeyresetcount: {}", getSslKeyResetCount());
    logger.trace("sslPeerName     : {}", getSslPeerName());
    logger.trace("tempModel       : {}", getTempModel());
    logger.trace("tempQPrefix     : {}", getTempQPrefix());
    logger.trace("tempTopicPrefix : {}", getTempTopicPrefix());
    logger.trace("user            : \'{}\'", getUser());
    /* Obviously we don't want to trace a password. But it is OK to indicate whether one has been configured */
    logger.trace("password set    : {}", (getPassword() != null && getPassword().length() > 0) ? "YES" : "NO");
    logger.trace("sslFIPSRequired        : {}", isSslFIPSRequired());
    logger.trace("useIBMCipherMappings   : {}", isUseIBMCipherMappings());
    logger.trace("userAuthenticationMQCSP: {}", isUserAuthenticationMQCSP());
    logger.trace("outboundSNI            : \'{}\'", getOutboundSNI());

    logger.trace("jndiCF          : {}", getJndi().getProviderContextFactory());
    logger.trace("jndiProviderUrl : {}", getJndi().getProviderUrl());

    if (additionalProperties.size() > 0) {
      for (String s: additionalProperties.keySet()) {
        logger.trace("Additional Property - {} : {}",s,additionalProperties.get(s));
      }
    }
    if (pool.isEnabled()) {
      logger.trace("Pool blockIfFullTimeout         : {}",pool.getBlockIfFullTimeout().toString());
      logger.trace("Pool idleTimeout                : {}",pool.getIdleTimeout().toString());
      logger.trace("Pool maxConnections             : {}",pool.getMaxConnections());
      logger.trace("Pool maxSessionsPerConn         : {}",pool.getMaxSessionsPerConnection());
      logger.trace("Pool timeBetweenExpirationCheck : {}",pool.getTimeBetweenExpirationCheck().toString());
    } else {
      logger.trace("Pooling is disabled");
    }
  }
}
