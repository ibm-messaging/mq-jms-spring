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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ssl.NoSuchSslBundleException;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.boot.ssl.SslManagerBundle;

import com.ibm.mq.jakarta.jms.MQConnectionFactory;
import com.ibm.msg.client.jakarta.wmq.WMQConstants;

import jakarta.jms.JMSException;

/**
 * Factory to create a {@link MQConnectionFactory} instance from properties
 * defined in {@link MQConfigurationProperties}.
 */
public class MQConnectionFactoryFactory {

  private final MQConfigurationProperties properties;

  private final List<MQConnectionFactoryCustomizer> factoryCustomizers;
  private static Logger logger = LoggerFactory.getLogger(MQConnectionFactoryFactory.class);
  private SslBundles sslBundles;

  @SuppressWarnings("unchecked")
  public MQConnectionFactoryFactory(MQConfigurationProperties properties, SslBundles sslBundles,
      List<MQConnectionFactoryCustomizer> factoryCustomizers) {
    this.properties = properties;
    this.sslBundles = sslBundles;
    this.factoryCustomizers = (List<MQConnectionFactoryCustomizer>) (factoryCustomizers != null ? factoryCustomizers
        : Collections.emptyList());
    logger.trace("constructor");
    // logger.trace("!! Bundles = {} ", (sslBundles == null) ? "null" : (getSSLSocketFactory("ibmmq") != null) ? "Has IBMMQ"
    // : "No IBMMQ bundle");
  }

  @SuppressWarnings("unchecked")
  public <T extends MQConnectionFactory> T createConnectionFactory(Class<T> factoryClass) {
    String err = null;
    T cf = null;
    SSLSocketFactory sf = null;

    String jndiProviderUrl = this.properties.getJndi().getProviderUrl();
    String jndiCF = this.properties.getJndi().getProviderContextFactory();

    logger.trace("createConnectionFactory for class " + factoryClass.getSimpleName());

    /* Keystore System properties don't need the CF to be already created */
    String sslBundle = this.properties.getSslBundle();

    /*
     * From Spring Boot 3.1, we can put sets of SSL configuration items in a bundle
     * The bundle name takes priority over the ibm.mq.jks properties
     */
    if (sslBundles != null && isNotNullOrEmpty(sslBundle)) {
      sf = getSSLSocketFactory(sslBundle);
    }
    else {
      configureTLSStores(this.properties);
    }

    if (isNotNullOrEmpty(jndiProviderUrl) && isNotNullOrEmpty(jndiCF)) {
      logger.trace("createConnectionFactory using JNDI");
      try {
        String cfName = this.properties.getQueueManager();
        this.properties.getJndi().traceProperties(cfName);

        Context ctx = getJndiContext(this.properties.getJndi());

        if (jndiProviderUrl.toUpperCase().contains("LDAP") && !cfName.toUpperCase().startsWith("CN=")) {
          cf = (T) ctx.lookup("cn=" + cfName);
        }
        else {
          cf = (T) ctx.lookup(cfName);
        }

        if (sf != null) {
          cf.setSSLSocketFactory(sf);
        }

        // We will not dump the properties as they are not used for most of the configuration.
        // The JNDI config may well have overridden the actual values in any resource files.
        // But we will still allow the customize methods to be used.
        customize(cf);
      }
      catch (NamingException ex) {
        logger.trace("createConnectionFactory : exception " + ex.getMessage());
        throw new IllegalStateException("Unable to create MQConnectionFactory" + ((err != null) ? (": " + err) : ""), ex);
      }
    }
    else {
      try {
        cf = createConnectionFactoryInstance(factoryClass);
        if (sf != null) {
          cf.setSSLSocketFactory(sf);
        }
        configureConnectionFactory(cf, this.properties);
        customize(cf);
      }
      catch (JMSException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
          | NoSuchMethodException | SecurityException ex) {
        logger.trace("createConnectionFactory : exception " + ex.getMessage());
        throw new IllegalStateException("Unable to create MQConnectionFactory" + ((err != null) ? (": " + err) : ""), ex);
      }
    }

    return cf;
  }

  /*
   * This method allows someone to create their own CF and then have it configured
   * using the same MQConfigurationProperties class - which might have been assigned
   * from a different prefix in the properties file.
   */
  public static void configureConnectionFactory(MQConnectionFactory cf, MQConfigurationProperties props) throws JMSException {
    // Should usually provide a queue manager name but it can be empty, to connect to the
    // default queue manager.
    boolean bindingsMode = false;

    logger.trace("configureConnectionFactory");

    props.traceProperties();

    String qmName = props.getQueueManager();
    if (!isNullOrEmpty(qmName)) {
      cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, qmName);
    }

    // Use the channel name to decide whether to try to connect locally or as a
    // client. If the queue manager code has been installed locally, then this connection
    // will try to use native JNI bindings to match.
    String channel = props.getChannel();
    String connName = props.getConnName();
    String ccdtUrl = props.getCcdtUrl();

    if (!isNullOrEmpty(ccdtUrl)) {
      cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
      cf.setStringProperty(WMQConstants.WMQ_CCDTURL, ccdtUrl);
    }
    else {
      if (isNullOrEmpty(channel) || isNullOrEmpty(connName)) {
        cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_BINDINGS);
        bindingsMode = true;
      }
      else {
        cf.setStringProperty(WMQConstants.WMQ_CONNECTION_NAME_LIST, connName);
        cf.setStringProperty(WMQConstants.WMQ_CHANNEL, channel);
        cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
      }
    }

    String clientId = props.getClientId();
    if (!isNullOrEmpty(clientId)) {
      cf.setStringProperty(WMQConstants.CLIENT_ID, clientId);
    }

    if (!bindingsMode) {
      if (!isNullOrEmpty(props.getReconnect())) {
        cf.setIntProperty(WMQConstants.WMQ_CLIENT_RECONNECT_OPTIONS, props.getReconnectValue());
      }
      cf.setIntProperty(WMQConstants.WMQ_CLIENT_RECONNECT_TIMEOUT, props.getReconnectTimeout());

      /* Balancing options for Uniform clusters came available from 9.3.4 */
      if (!isNullOrEmpty(props.getBalancingApplicationType())) {
        cf.setIntProperty(WMQConstants.WMQ_BALANCING_APPLICATION_TYPE, props.getBalancingApplicationTypeValue());
      }

      if (!isNullOrEmpty(props.getBalancingOptions())) {
        cf.setIntProperty(WMQConstants.WMQ_BALANCING_OPTIONS, props.getBalancingOptionsValue());
      }

      if (!isNullOrEmpty(props.getBalancingTimeout())) {
        cf.setIntProperty(WMQConstants.WMQ_BALANCING_TIMEOUT, props.getBalancingTimeoutValue());
      }
    }

    String applicationName = props.getApplicationName();
    if (!isNullOrEmpty(applicationName)) {
      cf.setAppName(applicationName);
    }

    // Setup the authentication.If a token has been set, then we will use that with an empty
    // userid, regardless of the configured user property. Otherwise, if
    // there is a userid defined, prefer to use the CSP model for
    // password checking.But it is possible to override the choice via a
    // property, for some compatibility requirements. It is possible to have a blank userid
    // while also setting a password. The queue manager authentication mechanism would then
    // be responsible for allocating an identity - which might come from treating the password
    // as a token.
    String token = props.getToken();
    if (!isNullOrEmpty(token)) {
      cf.setStringProperty(WMQConstants.PASSWORD, token);
      cf.setStringProperty(WMQConstants.USERID, "");
      cf.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, true);
    }
    else {
      String u = props.getUser();
      if (!isNullOrEmpty(u)) {
        cf.setStringProperty(WMQConstants.USERID, u);
      }
      String p = props.getPassword();
      if (!isNullOrEmpty(p)) {
        cf.setStringProperty(WMQConstants.PASSWORD, p);
        cf.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, props.isUseAuthenticationMQCSP());
      }
    }

    if (!isNullOrEmpty(props.getSslCipherSuite()))
      cf.setStringProperty(WMQConstants.WMQ_SSL_CIPHER_SUITE, props.getSslCipherSuite());

    if (!isNullOrEmpty(props.getSslCipherSpec()))
      cf.setStringProperty(WMQConstants.WMQ_SSL_CIPHER_SPEC, props.getSslCipherSpec());

    if (!isNullOrEmpty(props.getSslPeerName())) {
      cf.setStringProperty(WMQConstants.WMQ_SSL_PEER_NAME, props.getSslPeerName());
    }
    cf.setBooleanProperty(WMQConstants.WMQ_SSL_FIPS_REQUIRED, props.isSslFIPSRequired());
    Integer vi = props.getSslKeyResetCount();
    if (vi != -1) {
      cf.setIntProperty(WMQConstants.WMQ_SSL_KEY_RESETCOUNT, vi);
    }

    if (!isNullOrEmpty(props.getTempQPrefix())) {
      cf.setStringProperty(WMQConstants.WMQ_TEMP_Q_PREFIX, props.getTempQPrefix());
    }
    if (!isNullOrEmpty(props.getTempTopicPrefix())) {
      cf.setStringProperty(WMQConstants.WMQ_TEMP_TOPIC_PREFIX, props.getTempTopicPrefix());
    }
    if (!isNullOrEmpty(props.getTempModel())) {
      cf.setStringProperty(WMQConstants.WMQ_TEMPORARY_MODEL, props.getTempModel());
    }

    /*
     * Additional properties that are not in the pre-defined recognised set can be put onto the
     * CF via a map in the external properties definitions. Use the format
     * "ibm.mq.additionalProperties.CONSTANT_NAME=value" where the CONSTANT_NAME
     * is either the actual string for the property name or the WMQConstants variable name
     * The real property value will often begin "XMSC". For example "XMSC_WMQ_SECURITY_EXIT".
     * They are NOT the same as the short strings used by the JMSAdmin program.
     *
     * There is no error checking on the property name other than ensuring that a name beginning WMQ_
     * is recognised, or the value. If the value looks like a number, we treat it as such.
     * Similarly if the value is TRUE/FALSE then that is processed as a boolean.
     * So you cannot try to set a string property that appears to be an integer.
     * Symbols representing the value of integer attributes cannot be used - the real
     * number must be used. This may reduce the need for a customizer method in application
     * code. Integers can be given either in decimal or with "0x" to indicate hex.
     */
    Map<String, String> additionalProperties = props.getAdditionalProperties();
    for (String k : additionalProperties.keySet()) {
      String v = additionalProperties.get(k);
      Boolean vb = null;
      vi = null;
      String key = k;

      // If the property looks like a variable name, try to look it up
      // in the WMQConstants class.
      if (key.startsWith("WMQ_")) {
        try {
          Field f = WMQConstants.class.getField(key);
          if (f != null) {
            Object o = f.get(new Object());
            if (o != null && o instanceof String) {
              key = (String) o;
              logger.trace("Successfully mapped {} to property name {}", k, key);
            }
          }
        }
        catch (Throwable e) {
          logger.warn("Cannot find value of property " + k, e);
        }
      }

      try {
        if (v.toUpperCase().startsWith("0X")) {
          vi = Integer.decode(v); // Could use decode for all values but this reduces the number of strings that
                                  // might mistakenly match
        }
        else {
          vi = Integer.valueOf(v);
        }
        cf.setIntProperty(key, vi);
        logger.trace("Using setIntProperty with key {} and value {} [{}]", key, vi, String.format("0x%08X", vi));
      }
      catch (NumberFormatException e) {
      }

      if (vi == null) {
        // Can't use Boolean.valueOf(v) directly because we need to know it
        // really does match TRUE/FALSE strings and that method doesn't fail if you
        // give it something else (it just returns 'false').
        if (v.toUpperCase().equals("TRUE") || v.toUpperCase().equals("FALSE")) {
          vb = Boolean.valueOf(v);
          cf.setBooleanProperty(key, vb);
          logger.trace("Using setBooleanProperty with key {} and value {}", key, vb);
        }
      }

      if (vi == null && vb == null) {
        cf.setStringProperty(key, v);
        logger.trace("Using setStringProperty with key {} and value {}", key, v);
      }
    }
  }

  /*
   * Access to Java keystores can be controlled by system properties. These are usually
   * given with -D options on the command line but we can set them here instead. The Spring properties
   * that drive these will be "ibm.mq.jks.keyStore=" ... For historic reasons, we set the com.ibm.ssl versions
   * as well as the regular javax.net.ssl properties. Deprecated in Spring Boot 3
   * where we try to use SSLBundles instead.
   */
  private static void configureTLSStores(MQConfigurationProperties props) {
    String prefixes[] = { "javax.net.ssl.", "com.ibm.ssl." };

    logger.trace("configuring TLS Store system properties");

    MQConfigurationPropertiesJks jksProperties = props.getJks();
    for (String prefix : prefixes) {
      if (!isNullOrEmpty(jksProperties.getKeyStore())) {
        System.setProperty(prefix + "keyStore", jksProperties.getKeyStore());
      }
      if (!isNullOrEmpty(jksProperties.getKeyStorePassword())) {
        System.setProperty(prefix + "keyStorePassword", jksProperties.getKeyStorePassword());
      }
      if (!isNullOrEmpty(jksProperties.getTrustStore())) {
        System.setProperty(prefix + "trustStore", jksProperties.getTrustStore());
      }
      if (!isNullOrEmpty(jksProperties.getTrustStorePassword())) {
        System.setProperty(prefix + "trustStorePassword", jksProperties.getTrustStorePassword());
      }
    }
  }

  private <T extends MQConnectionFactory> T createConnectionFactoryInstance(Class<T> factoryClass) throws InstantiationException,
      IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
    logger.trace("createConnectionFactoryInstance for class " + factoryClass.getSimpleName());
    return factoryClass.getConstructor().newInstance();
  }

  private void customize(MQConnectionFactory connectionFactory) {
    for (MQConnectionFactoryCustomizer factoryCustomizer : this.factoryCustomizers) {
      logger.trace("Calling MQConnectionFactoryCustomizer from class {} ", factoryCustomizer.getClass().getName());
      factoryCustomizer.customize(connectionFactory);
    }
  }

  /*
   * This method returns a JNDI context that can be used to lookup resources.
   * The only use of it in this package is to return a CF, but someone might be
   * interested in calling it directly to get a Context that can then be also
   * used for Destinations. Making it a static method helps in reuse.
   *
   */
  public static Context getJndiContext(MQConfigurationPropertiesJndi jproperties) throws NamingException {
    Context ctx;

    logger.trace("getJndiContext");
    String jndiProviderUrl = jproperties.getProviderUrl();
    String jndiCF = jproperties.getProviderContextFactory();
    Hashtable<String, String> environment = new Hashtable<String, String>();
    environment.put(Context.PROVIDER_URL, jndiProviderUrl);
    environment.put(Context.INITIAL_CONTEXT_FACTORY, jndiCF);

    // Try to parse the additional properties section for handling
    // the rarely-used settings
    Map<String, String> additionalProperties = jproperties.getAdditionalProperties();
    for (String k : additionalProperties.keySet()) {
      String v = additionalProperties.get(k);
      try {
        Field f = Context.class.getField(k);
        if (f != null) {
          Object o = f.get(new Object());
          if (o != null && o instanceof String) {
            k = (String) o;
          }
        }
      }
      catch (Throwable e) {
        logger.warn("Cannot find value of property " + k);
      }
      logger.trace(String.format("getJndiContext: Using additional property '%s' with value '%s'", k, v));
      environment.put(k, v);
    }

    ctx = new InitialDirContext(environment);
    return ctx;
  }

  static boolean isNullOrEmpty(String s) {
    if (s == null || s.isEmpty())
      return true;
    else
      return false;
  }

  static boolean isNotNullOrEmpty(String s) {
    return !isNullOrEmpty(s);
  }

  private SSLSocketFactory getSSLSocketFactory(String b) {
    SSLSocketFactory sf = null;

    if (b == null || b.isEmpty()) {
      /* Should never get here as the caller has already checked */
      logger.trace("getSSLSocketFactory - null/empty bundle name requested");
      return sf;
    }

    if (sslBundles != null) {
      try {
        SslBundle sb = sslBundles.getBundle(b);
        SSLContext sc;
        logger.trace("SSL Bundle for {} - found", b);

        sc = sb.createSslContext();
        // logger.trace("SSL Protocol is {}",sc.getProtocol());
        sf = sc.getSocketFactory();
      }
      catch (NoSuchSslBundleException e) {
        logger.error("SSL bundle for {} - not found", b);
      }
    }
    return sf;
  }
}
