/*
 * Copyright © 2018,2024 IBM Corp. All rights reserved.
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

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jms.JmsPoolConnectionFactoryProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.DeprecatedConfigurationProperty;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import com.ibm.msg.client.jakarta.wmq.WMQConstants;

/**
 * There are many properties that can be set on an MQ Connection Factory/ This
 * class allows configuration for most of them
 * for both direct and client connections. Any that are not explicitly named in
 * here can be managed through the "additionalProperties" map.
 * <p>
 * This class allows for setting the CipherSuite/CipherSpec property, and an
 * indication of whether or not
 * to use the IBM JRE maps for Cipher names - that's not something that is
 * standardised.
 * <p>
 * The default values have been set to match the settings of the
 * <a href="https://github.com/ibm-messaging/mq-container">developer-configured
 * container</a>. Note that the default userid/password settings have now been removed;
 * they must be explicitly enabled for the queue manager.
 * 
 * <ul>
 * <li>queueManager = QM1
 * <li>connName = localhost(1414)
 * <li>channel = DEV.ADMIN.SVRCONN
 * <li>user = 
 * <li>password = 
 * </ul>
 */
@ConfigurationProperties(prefix = "ibm.mq")
public class MQConfigurationProperties {

  private static Logger logger = LoggerFactory.getLogger(MQConfigurationProperties.class);

  // Some system properties that may be set through this package. They are not
  // regular CF properties, but still affect how connections are made.
  private static final String PROPERTY_USE_IBM_CIPHER_MAPPINGS = "com.ibm.mq.cfg.useIBMCipherMappings";
  private static final String PROPERTY_OUTBOUND_SNI = "com.ibm.mq.cfg.SSL.outboundSNI";
  private static final String PROPERTY_CHANNEL_SHARING = "com.ibm.mq.jms.channel.sharing";

  public MQConfigurationProperties() {
    logger.trace("constructor");
    return;
  }

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
  private String user = "";

  /**
   * MQ password
   */
  private String password = "";
  
  /**
   * An OIDC/JWT token. The token can either be set in the
   * password field, in which case the user needs to be overridden to be
   * blank, or it can be set here explicitly.
   */
  private String token = "";

  /**
   * Override the authentication mode. This should not normally be needed with current maintenance 
   * levels of MQ V8 or V9, but some earlier levels sometimes got get it wrong and then this flag 
   * can be set to "false". There is also some confusion about
   * whether the field is called "userAuth.." or "useAuth..." So we allow both
   * spellings and use the explicit setting to set a different attribute.
   */
  @SuppressWarnings("unused")
  private boolean userAuthenticationMQCSP; // These variables appear unused but are needed to ensure the Spring setters
                                           // recognise them
  @SuppressWarnings("unused")
  private boolean useAuthenticationMQCSP;
  private boolean authCSP = true;

  /**
   * For TLS connections, you can set either the sslCipherSuite or sslCipherSpec property.
   * For example, "SSL_ECDHE_RSA_WITH_AES_256_GCM_SHA384"
   */
  private String sslCipherSuite;

  /**
   * For TLS connections, you can set either the sslCipherSuite or sslCipherSpec property.
   * For example, "ECDHE_RSA_AES_256_GCM_SHA384"
   */
  private String sslCipherSpec;

  /**
   * Type a distinguished name skeleton that must match that provided by the queue manager.
   */
  private String sslPeerName;

  /**
   * Set to true for the IBM JRE CipherSuite name maps; set to false to use the
   * Oracle JRE CipherSuite mapping
   */
  private boolean useIBMCipherMappings = true;

  /**
   * Set to HOSTNAME for connection to OpenShift queue managers where SNI is important. CHANNEL
   * can be used for environments where you want to have different certificates
   * associated with different channels. The property does not get set unless explicitly 
   * configured as an external property so we would otherwise use whatever the default behaviour 
   * is in the JMS client.
   */
  private String outboundSNI = ""; // HOSTNAME or CHANNEL are the valid alternatives

  /**
   * Set to GLOBAL or CONNECTION for strategies to share TCP/IP connections.
   */
  private String channelSharing = "";

  /**
   * Whether to automatically reconnect to the qmgr when in client mode. Values
   * can be YES/NO/QMGR/DISABLED.
   */
  private String reconnect = "";
  /**
   * For automatic reconnection, this is the timeout in seconds before giving up. Defaults
   * to 1800 - 30 minutes.
   */
  private int reconnectTimeout = WMQConstants.WMQ_CLIENT_RECONNECT_TIMEOUT_DEFAULT;

  // The following variable is not really used, and is declared purely
  // to allow a Getter to mark it as deprecated
  private String defaultReconnect = null; // This was the original name but I don't like it.

  /**
   * Enter the uniform resource locator (URL) that identifies the name and
   * location of the file that contains
   * the client channel definition table and specifies how the file can be accessed.
   * You must set a value for either the Channel property or for the Client
   * Channel Definition Table URL property but not both.
   * For example, "file:///home/admdata/ccdt1.tab"
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
   * The key to the SSL Bundle attributes available from Spring Boot 3.1
   * Would perhaps be better called sslBundleName
   */
  private String sslBundle;

  /**
   * Uniform cluster application balancing options
   * Type can be "SIMPLE" or "REQREP" (or its alias of "REQUESTREPLY").
   * The "RA_MANAGED" value for JEE environments does not apply here so is not
   * recognised
   */
  private String balancingApplicationType = "";

  /**
   * Uniform cluster application balancing options
   * Timeout can be set to "DEFAULT", "IMMEDIATE", "NEVER" or a integer number of
   * seconds
   */
  private String balancingTimeout = "";

  /**
   * Uniform cluster application balancing options, comma-separated.
   * Currently recognised options: only IGNORE_TRANS for now, but can
   * also supply an integer value directly.
   */
  private String balancingOptions = "";

  /**
   * Additional CF properties that are not explicitly known can be provided
   * with the format "ibm.mq.additionalProperties.SOME_PROPERTY=SOME_VALUE".
   * Strings, integers and true/false values are recognised.
   *
   * The property is either the actual string for the MQ property, and will
   * usually begin with "XMSC"
   * Or it can be the name of the variable in the WMQConstants class. So for example,
   * setting the name of a security exit would usually be done in code with
   * setStringProperty(WMQConstants.WMQ_SECURITY_EXIT). The value of that constant is
   * "XMSC_WMQ_SECURITY_EXIT" so the external property to set can be either
   * "ibm.mq.additionalProperties.XMSC_WMQ_SECURITY_EXIT=com.example.SecExit" or
   * "ibm.mq.additionalProperties.WMQ_SECURITY_EXIT=com.example.SecExit"
   *
   */
  private Map<String, String> additionalProperties = new HashMap<String, String>();

  @NestedConfigurationProperty
  private JmsPoolConnectionFactoryProperties pool = new JmsPoolConnectionFactoryProperties();

  @NestedConfigurationProperty
  private MQConfigurationPropertiesJndi jndi = new MQConfigurationPropertiesJndi();

  @NestedConfigurationProperty
  private MQConfigurationPropertiesJks jks = new MQConfigurationPropertiesJks();

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
  
  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
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

  public String getChannelSharing() {
    return channelSharing;
  }

  public void setChannelSharing(String channelSharing) {
    System.setProperty(PROPERTY_CHANNEL_SHARING, channelSharing);
    this.channelSharing = channelSharing;
  }

  // Both forms of this seem to have been used at different times. So we allow
  // either to be set. The local field named after the config option is actually
  // irrelevant; we always set a different common field.
  public boolean isUseAuthenticationMQCSP() {
    return authCSP;
  }

  public void setUserAuthenticationMQCSP(boolean userAuthenticationMQCSP) {
    authCSP = userAuthenticationMQCSP;
  }

  public void setUseAuthenticationMQCSP(boolean useAuthenticationMQCSP) {
    authCSP = useAuthenticationMQCSP;
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

  public MQConfigurationPropertiesJks getJks() {
    return jks;
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

  public String getSslBundle() {
    return sslBundle;
  }

  public void setSslBundle(String sslBundle) {
    this.sslBundle = sslBundle;
  }

  public int getReconnectValue() {
    int rc = 0;
    switch (reconnect.toUpperCase()) {
    case "QMGR":
      rc = WMQConstants.WMQ_CLIENT_RECONNECT_Q_MGR;
      break;
    case "DISABLED":
    case "NO":
      rc = WMQConstants.WMQ_CLIENT_RECONNECT_DISABLED;
      break;
    case "YES":
    case "ANY":
      rc = WMQConstants.WMQ_CLIENT_RECONNECT;
      break;
    default:
      rc = WMQConstants.WMQ_CLIENT_RECONNECT_AS_DEF;
      break;
    }
    return rc;
  }

  public void setDefaultReconnect(String defaultReconnect) {
    // Set the preferred property, not the original
    this.reconnect = defaultReconnect; 
  }
  
  @DeprecatedConfigurationProperty(replacement="ibm.mq.reconnect")
  public String getDefaultReconnect() {
    // This method is never called, but we need a getter to mark the deprecation
    return defaultReconnect;
  }

  public String getReconnect() {
    return reconnect;
  }

  public void setReconnect(String reconnect) {
    this.reconnect = reconnect;
  }
  
  public void setReconnectTimeout(int reconnectTimeout) {
    this.reconnectTimeout = reconnectTimeout;
  }
  
  public int getReconnectTimeout() {
    return reconnectTimeout;
  }

  public void setBalancingTimeout(String balancingTimeout) {
    this.balancingTimeout = balancingTimeout;
  }

  public String getBalancingTimeout() {
    return balancingTimeout;
  }

  public void setBalancingApplicationType(String balancingApplicationType) {
    this.balancingApplicationType = balancingApplicationType;
  }

  public String getBalancingApplicationType() {
    return balancingApplicationType;
  }

  public void setBalancingOptions(String balancingOptions) {
    this.balancingOptions = balancingOptions;
  }

  public String getBalancingOptions() {
    return balancingOptions;
  }

  public int getBalancingApplicationTypeValue() {
    int rc = 0;
    if (balancingApplicationType == null || balancingApplicationType.equals(""))
      return rc;

    String ba = balancingApplicationType.trim().toUpperCase().replaceAll("_", "");
    switch (ba) {
    case "REQREP":
    case "REQUESTREPLY":
      rc = WMQConstants.WMQ_BALANCING_APPLICATION_TYPE_REQUEST_REPLY;
      break;
    case "SIMPLE":
      rc = WMQConstants.WMQ_BALANCING_APPLICATION_TYPE_SIMPLE;
      break;
    default:
      throw new IllegalArgumentException(
          String.format("ApplicationType value \'%s\' not recognised", balancingApplicationType));
    }
    return rc;
  }

  public int getBalancingTimeoutValue() {
    int rc = WMQConstants.WMQ_BALANCING_TIMEOUT_AS_DEFAULT;

    if (balancingTimeout == null || balancingTimeout.equals(""))
      return rc;

    String ba = balancingTimeout.toUpperCase();
    switch (ba) {
    case "DEFAULT":
      rc = WMQConstants.WMQ_BALANCING_TIMEOUT_AS_DEFAULT;
      break;
    case "IMMEDIATE":
      rc = WMQConstants.WMQ_BALANCING_TIMEOUT_IMMEDIATE;
      break;
    case "NEVER":
      rc = WMQConstants.WMQ_BALANCING_TIMEOUT_NEVER;
      break;
    default:
      // Try to parse using a Java-standard Duration string (eg "PT10S"). If that
      // fails, try to parse it as an integer but allow the value to have a trailing
      // "s". An exception will be thrown if
      // it still can't be parsed. Spring Boot does have a more general parser for
      // Durations but this should be good enough.
      try {
        rc = (int) (Duration.parse(ba).toMillis() / 1000);
      }
      catch (DateTimeParseException e) {
        if (ba.endsWith("S")) {
          ba = ba.substring(0, ba.length() - 1);
        }
        rc = Integer.parseInt(ba);
      }
      break;
    }
    return rc;
  }

  public int getBalancingOptionsValue() {
    int rc = WMQConstants.WMQ_BALANCING_OPTIONS_NONE;
    if (balancingOptions == null || balancingOptions.isEmpty()) {
      return rc;
    }

    // This has been written to make it easy if further options are made available.
    // Split the option string at commas, then transform each field into a canonical
    // version so that simple errors can be avoided. Allow numbers and strings. For
    // strings, remove any "_" characters and make the whole thing upper-case.
    String boArray[] = balancingOptions.split(",");
    for (String bo : boArray) {
      String boCanon = bo.trim().toUpperCase().replaceAll("_", "");
      try {
        int val = Integer.decode(boCanon);
        rc |= val;
      }
      catch (NumberFormatException e) {
        switch (boCanon) {
        case "NONE":
          rc |= WMQConstants.WMQ_BALANCING_OPTIONS_NONE;
          break;
        case "IGNORETRANS":
          rc |= WMQConstants.WMQ_BALANCING_OPTIONS_IGNORE_TRANSACTIONS;
          break;
        default:
          throw new IllegalArgumentException(String.format("Balancing Options value \'%s\' not recognised", bo));
        }
      }
    }
    return rc;
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
    logger.trace("reconnectOption : \'{}\' [{}]", getReconnect(), String.format("0x%08X", getReconnectValue()));
    logger.trace("reconnectTimeout: {}", getReconnectTimeout());
    logger.trace("sslCipherSpec   : {}", getSslCipherSpec());
    logger.trace("sslCipherSuite  : {}", getSslCipherSuite());
    logger.trace("sslKeyresetcount: {}", getSslKeyResetCount());
    logger.trace("sslPeerName     : {}", getSslPeerName());
    logger.trace("sslBundle       : {}", getSslBundle());

    logger.trace("tempModel       : {}", getTempModel());
    logger.trace("tempQPrefix     : {}", getTempQPrefix());
    logger.trace("tempTopicPrefix : {}", getTempTopicPrefix());
    logger.trace("user            : \'{}\'", getUser());
    /*
     * Obviously we don't want to trace a password. But it is OK to indicate whether
     * one has been configured
     */
    logger.trace("password set    : {}", (getPassword() != null && getPassword().length() > 0) ? "YES" : "NO");
    logger.trace("token set       : {}", (getToken() != null && getToken().length() > 0) ? "YES" : "NO");

    logger.trace("sslFIPSRequired        : {}", isSslFIPSRequired());
    logger.trace("useIBMCipherMappings   : {}", isUseIBMCipherMappings());
    logger.trace("userAuthenticationMQCSP: {}", isUseAuthenticationMQCSP());
    logger.trace("outboundSNI            : \'{}\'", getOutboundSNI());
    logger.trace("channelSharing         : \'{}\'", getChannelSharing());

    logger.trace("balancingAppType       : \'{}\' [{}]", getBalancingApplicationType(), getBalancingApplicationTypeValue());
    logger.trace("balancingTimeout       : \'{}\' [{}]", getBalancingTimeout(), getBalancingTimeoutValue());
    logger.trace("balancingOptions       : \'{}\' [{}]", getBalancingOptions(), getBalancingOptionsValue());

    logger.trace("jndiCF          : {}", getJndi().getProviderContextFactory());
    logger.trace("jndiProviderUrl : {}", getJndi().getProviderUrl());

    String pw = getJks().getKeyStorePassword();
    logger.trace("JKS keystore           : {}", getJks().getKeyStore());
    logger.trace("JKS keystore pw set    : {}", (pw != null && pw.length() > 0) ? "YES" : "NO");
    pw = getJks().getTrustStorePassword();
    logger.trace("JKS truststore         : {}", getJks().getTrustStore());
    logger.trace("JKS truststore pw set  : {}", (pw != null && pw.length() > 0) ? "YES" : "NO");

    if (additionalProperties.size() > 0) {
      for (String s : additionalProperties.keySet()) {
        logger.trace("Additional Property - {} : {}", s, additionalProperties.get(s));
      }
    }
    else {
      logger.trace("No additional properties defined");
    }

    if (pool.isEnabled()) {
      logger.trace("Pool blockIfFullTimeout         : {}", pool.getBlockIfFullTimeout().toString());
      logger.trace("Pool idleTimeout                : {}", pool.getIdleTimeout().toString());
      logger.trace("Pool maxConnections             : {}", pool.getMaxConnections());
      logger.trace("Pool maxSessionsPerConn         : {}", pool.getMaxSessionsPerConnection());
      logger.trace("Pool timeBetweenExpirationCheck : {}", pool.getTimeBetweenExpirationCheck().toString());
    }
    else {
      logger.trace("Pooling is disabled");
    }
  }
}
