/*
 * Copyright © 2018 IBM Corp. All rights reserved.
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

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;

import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;

/**
 * Factory to create a {@link MQConnectionFactory} instance from properties defined in {@link MQConfigurationProperties}.
 */
public class MQConnectionFactoryFactory {
 
  private final MQConfigurationProperties properties;

  private final List<MQConnectionFactoryCustomizer> factoryCustomizers;

  @SuppressWarnings("unchecked")
  public MQConnectionFactoryFactory(MQConfigurationProperties properties, List<MQConnectionFactoryCustomizer> factoryCustomizers) {
    this.properties = properties;
    this.factoryCustomizers = (List<MQConnectionFactoryCustomizer>) (factoryCustomizers != null ? factoryCustomizers : Collections.emptyList());
  }

  // There are many properties that can be set on an MQ Connection Factory, but these are the most commonly-used
  // for both direct and client connections.
  // 
  // If you use TLS for client connectivity, most properties related to that
  // (keystore, certificates etc) must be set independently. That could be done in a customizer() method.
  // Keystores are often set in global properties defined by -D options on the command line. 
  public <T extends MQConnectionFactory> T createConnectionFactory(Class<T> factoryClass) {
    String err = null;

    try {
      T cf = createConnectionFactoryInstance(factoryClass);

      // Should usually provide a queue manager name but it can be empty, to connect to the 
      // default queue manager.
      String qmName = this.properties.getQueueManager();
      cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, qmName);

      // Use the channel name to decide whether to try to connect locally or as a client. If the queue manager
      // code has been installed locally, then this connection will try to use native JNI bindings to match.
      String channel = this.properties.getChannel();
      String connName = this.properties.getConnName();
      String ccdtUrl  = this.properties.getCcdtUrl();
      
      if (!isNullOrEmpty(ccdtUrl)) {
        cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
        cf.setStringProperty(WMQConstants.WMQ_CCDTURL, ccdtUrl);
      }
      else {
        if (isNullOrEmpty(channel) || isNullOrEmpty(connName)) {
          cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_BINDINGS);
        }
        else {
          cf.setStringProperty(WMQConstants.WMQ_CONNECTION_NAME_LIST, connName);
          cf.setStringProperty(WMQConstants.WMQ_CHANNEL, channel);
          cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
        }
      }
      String clientId = this.properties.getClientId();
      if(!isNullOrEmpty(clientId)){
        cf.setStringProperty(WMQConstants.CLIENT_ID, clientId);
      }
      
      String applicationName = this.properties.getApplicationName();
      if(!isNullOrEmpty(applicationName)){
        cf.setAppName(applicationName);
      }

      // Setup the authentication. If there is a userid defined, prefer to use the CSP model for
      // password checking. That is more general than the cf.connect(user,pass) method which has
      // some restrictions in the MQ client. But it is possible to override the choice via a 
      // property, for some compatibility requirements. 
      String u = this.properties.getUser();
      if (!isNullOrEmpty(u)) {
        cf.setStringProperty(WMQConstants.USERID, u);
        String p =  this.properties.getPassword();
        if (!isNullOrEmpty(p))
          cf.setStringProperty(WMQConstants.PASSWORD, p);
        cf.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, this.properties.isUserAuthenticationMQCSP());
      }

      if (!isNullOrEmpty(this.properties.getSslCipherSuite()))
        cf.setStringProperty(WMQConstants.WMQ_SSL_CIPHER_SUITE, this.properties.getSslCipherSuite());

      if (!isNullOrEmpty(this.properties.getSslCipherSpec()))
        cf.setStringProperty(WMQConstants.WMQ_SSL_CIPHER_SPEC, this.properties.getSslCipherSpec());
      
      if (!isNullOrEmpty(this.properties.getSslPeerName())) {
        cf.setStringProperty(WMQConstants.WMQ_SSL_PEER_NAME, this.properties.getSslPeerName());
      }
      cf.setBooleanProperty(WMQConstants.WMQ_SSL_FIPS_REQUIRED,this.properties.isSslFIPSRequired());
      Integer vi = this.properties.getSslKeyResetCount();
      if (vi != -1) {
        cf.setIntProperty(WMQConstants.WMQ_SSL_KEY_RESETCOUNT, vi);
      }

      if (!isNullOrEmpty(this.properties.getTempQPrefix())) {
        cf.setStringProperty(WMQConstants.WMQ_TEMP_Q_PREFIX, this.properties.getTempQPrefix());
      }
      if (!isNullOrEmpty(this.properties.getTempTopicPrefix())) {
        cf.setStringProperty(WMQConstants.WMQ_TEMP_TOPIC_PREFIX, this.properties.getTempTopicPrefix());
      }
      if (!isNullOrEmpty(this.properties.getTempModel())) {
        cf.setStringProperty(WMQConstants.WMQ_TEMPORARY_MODEL, this.properties.getTempModel());
      }
      
      /*
       * Additional properties that are not in the standard recognised set can be put onto the
       * CF via a map in the external properties definitions. Use the format
       * "ibm.mq.additionalProperties.CONSTANT_NAME=value" where the CONSTANT_NAME
       * is the actual string for the property name. It will often begin
       * "XMSC". For example "XMSC_WMQ_SECURITY_EXIT". There is no error checking on the
       * property name or value. If the value looks like a number, we treat it as such.
       * Similarly if the value is TRUE/FALSE then that is processed as a boolean.
       * So you cannot try to set a string property that appears to be an integer. 
       * Symbols representing the value of integer attributes cannot be used - the real
       * number must be used. This may reduce the need for a customizer method in application
       * code.
       */
      Map<String, String> additionalProperties = this.properties.getAdditionalProperties();
      for (String k : additionalProperties.keySet()) {
        String v = additionalProperties.get(k);
        Boolean vb = null;
        vi = null;

        try {
          vi = Integer.valueOf(v);
          cf.setIntProperty(k, vi);
          //System.out.printf("Additional Props: key=%s Int value=%d\n", k, vi);
        }
        catch (NumberFormatException e) {
        }

        if (vi == null) {
          // Can't use Boolean.valueOf(v) directly because we need to know it
          // really does match TRUE/FALSE strings and that method doesn't fail if you
          // give it something else (it just returns 'false').
          if (v.toUpperCase().equals("TRUE") || v.toUpperCase().equals("FALSE")) {
            vb = Boolean.valueOf(v);
            cf.setBooleanProperty(k, vb);
            //System.out.printf("Additional Props: key=%s Bool value=%b\n", k, vb);
          }
        }

        if (vi == null && vb == null) {
          cf.setStringProperty(k, v);
          //System.out.printf("Additional Props: key=%s String value=%s\n", k, v);
        }
      }

      customize(cf);
      return cf;
    }
    catch (JMSException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
        | SecurityException ex) {
      throw new IllegalStateException("Unable to create MQConnectionFactory" + ((err != null) ? (": " + err) : ""), ex);
    }
  }

  private <T extends MQConnectionFactory> T createConnectionFactoryInstance(Class<T> factoryClass)
      throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
    return factoryClass.getConstructor().newInstance();
  }

  private void customize(MQConnectionFactory connectionFactory) {
    for (MQConnectionFactoryCustomizer factoryCustomizer : this.factoryCustomizers) {
      factoryCustomizer.customize(connectionFactory);
    }
  }

  boolean isNullOrEmpty(String s) {
    if (s == null || s.isEmpty())
      return true;
    else
      return false;
  }  
}
