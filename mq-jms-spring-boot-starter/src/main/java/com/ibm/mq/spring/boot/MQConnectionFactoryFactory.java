/*
 * Copyright © 2018,2020 IBM Corp. All rights reserved.
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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;

/**
 * Factory to create a {@link MQConnectionFactory} instance from properties defined in {@link MQConfigurationProperties}.
 */
public class MQConnectionFactoryFactory {
 
  private final MQConfigurationProperties properties;        

  private final List<MQConnectionFactoryCustomizer> factoryCustomizers;
  private static Logger logger = LoggerFactory.getLogger(MQConnectionFactoryFactory.class);

  @SuppressWarnings("unchecked")
  public MQConnectionFactoryFactory(MQConfigurationProperties properties, List<MQConnectionFactoryCustomizer> factoryCustomizers) {
    this.properties = properties;
    this.factoryCustomizers = (List<MQConnectionFactoryCustomizer>) (factoryCustomizers != null ? factoryCustomizers : Collections.emptyList());  
  }

 
  public <T extends MQConnectionFactory> T createConnectionFactory(Class<T> factoryClass) {
    String err = null;

    try {
      T cf = createConnectionFactoryInstance(factoryClass);
      
      configureConnectionFactory(cf,this.properties);
      customize(cf);
      
      return cf;
    }
    catch (JMSException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
        | SecurityException ex) {
      throw new IllegalStateException("Unable to create MQConnectionFactory" + ((err != null) ? (": " + err) : ""), ex);
    }
  }

  
 /*
  * This method allows someone to create their own CF and then have it configured
  * using the same MQConfigurationProperties class - which might have been assigned
  * from a different prefix in the properties file.
  * 
  */
  public static void configureConnectionFactory(MQConnectionFactory cf, MQConfigurationProperties props) throws JMSException {
    // Should usually provide a queue manager name but it can be empty, to connect to the 
    // default queue manager.
    String qmName = props.getQueueManager();
    cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, qmName);

    // Use the channel name to decide whether to try to connect locally or as a client. If the queue manager
    // code has been installed locally, then this connection will try to use native JNI bindings to match.
    String channel = props.getChannel();
    String connName = props.getConnName();
    String ccdtUrl  = props.getCcdtUrl();
    
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
    String clientId = props.getClientId();
    if(!isNullOrEmpty(clientId)){
      cf.setStringProperty(WMQConstants.CLIENT_ID, clientId);
    }
    
    String applicationName = props.getApplicationName();
    if(!isNullOrEmpty(applicationName)){
      cf.setAppName(applicationName);
    }

    // Setup the authentication. If there is a userid defined, prefer to use the CSP model for
    // password checking. That is more general than the cf.connect(user,pass) method which has
    // some restrictions in the MQ client. But it is possible to override the choice via a 
    // property, for some compatibility requirements. 
    String u = props.getUser();
    if (!isNullOrEmpty(u)) {
      cf.setStringProperty(WMQConstants.USERID, u);
      String p =  props.getPassword();
      if (!isNullOrEmpty(p))
        cf.setStringProperty(WMQConstants.PASSWORD, p);
      cf.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, props.isUserAuthenticationMQCSP());
    }

    if (!isNullOrEmpty(props.getSslCipherSuite()))
      cf.setStringProperty(WMQConstants.WMQ_SSL_CIPHER_SUITE, props.getSslCipherSuite());

    if (!isNullOrEmpty(props.getSslCipherSpec()))
      cf.setStringProperty(WMQConstants.WMQ_SSL_CIPHER_SPEC, props.getSslCipherSpec());
    
    if (!isNullOrEmpty(props.getSslPeerName())) {
      cf.setStringProperty(WMQConstants.WMQ_SSL_PEER_NAME, props.getSslPeerName());
    }
    cf.setBooleanProperty(WMQConstants.WMQ_SSL_FIPS_REQUIRED,props.isSslFIPSRequired());
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
     * Additional properties that are not in the standard recognised set can be put onto the
     * CF via a map in the external properties definitions. Use the format
     * "ibm.mq.additionalProperties.CONSTANT_NAME=value" where the CONSTANT_NAME
     * is either the actual string for the property name or the WMQConstants variable name
     * The real property value will often begin "XMSC". For example "XMSC_WMQ_SECURITY_EXIT". 
     * 
     * There is no error checking on the
     * property name or value. If the value looks like a number, we treat it as such.
     * Similarly if the value is TRUE/FALSE then that is processed as a boolean.
     * So you cannot try to set a string property that appears to be an integer. 
     * Symbols representing the value of integer attributes cannot be used - the real
     * number must be used. This may reduce the need for a customizer method in application
     * code.
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
              key = (String)o;
            }
          }
        
        } catch (Throwable e) {
          logger.error("Error trying to find value of property " + k,e);
        }
      } 

      try {
        vi = Integer.valueOf(v);
        cf.setIntProperty(key, vi);
        //System.out.printf("Additional Props: key=%s Int value=%d\n", key, vi);
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
          //System.out.printf("Additional Props: key=%s Bool value=%b\n", key, vb);
        }
      }

      if (vi == null && vb == null) {
        cf.setStringProperty(key, v);
        //System.out.printf("Additional Props: key=%s String value=%s\n", key, v);
      }
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

  static boolean isNullOrEmpty(String s) {
    if (s == null || s.isEmpty())
      return true;
    else
      return false;
  }  
}
