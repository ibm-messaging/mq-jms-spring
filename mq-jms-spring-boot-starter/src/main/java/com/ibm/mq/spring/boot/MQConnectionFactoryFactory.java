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

import javax.jms.JMSException;

import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;

/**
 * Factory to create a {@link MQConnectionFactory} instance from properties defined in {@link MQConfigurationProperties}.
 */
class MQConnectionFactoryFactory {

  private final MQConfigurationProperties properties;

  private final List<MQConnectionFactoryCustomizer> factoryCustomizers;

  MQConnectionFactoryFactory(MQConfigurationProperties properties, List<MQConnectionFactoryCustomizer> factoryCustomizers) {
    this.properties = properties;
    this.factoryCustomizers = (factoryCustomizers != null ? factoryCustomizers : Collections.emptyList());
  }

  // There are many properties that can be set on an MQ Connection Factory, but these are the most commonly-used
  // for both direct and client connections. If you use TLS for client connectivity, properties related to that
  // (keystore, certificates, ciphers etc) must be set independently.  That could be done in a customizer() method.
  public <T extends MQConnectionFactory> T createConnectionFactory(Class<T> factoryClass) {
    try {
      T cf = createConnectionFactoryInstance(factoryClass);

      // Must provide a queue manager name.
      String qmName = this.properties.getQueueManager();
      if (isNullOrEmpty(qmName)) {
        throw new JMSException("Must provide queue manager name");
      }
      
      cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, this.properties.getQueueManager());

      // Use the channel name to decide whether to try to connect locally or as a client
      String channel = this.properties.getChannel();
      String connName = this.properties.getConnName();
      if (isNullOrEmpty(channel) || isNullOrEmpty(connName)) {
        cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_BINDINGS);
      }
      else {
        cf.setStringProperty(WMQConstants.WMQ_CONNECTION_NAME_LIST, connName);
        cf.setStringProperty(WMQConstants.WMQ_CHANNEL, channel);
        cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
      }

      // Setup the authentication. If there is a userid defined, always use the CSP model for
      // password checking. That is more general than the cf.connect(user,pass) method which has
      // some restrictions in the MQ client.
      String u = this.properties.getUser();

      if (!isNullOrEmpty(u)) {
        cf.setStringProperty(WMQConstants.USERID, u);
        cf.setStringProperty(WMQConstants.PASSWORD, this.properties.getPassword());
        cf.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, true);
      }

      customize(cf);
      return cf;
    }
    catch (JMSException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
        | SecurityException ex) {
      throw new IllegalStateException("Unable to create MQConnectionFactory", ex);
    }
  }

  private <T extends MQConnectionFactory> T createConnectionFactoryInstance(Class<T> factoryClass)
      throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
    return factoryClass.getConstructor(String.class).newInstance();
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
