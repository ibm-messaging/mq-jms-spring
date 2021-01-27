/*
 * Copyright © 2018, 2020 IBM Corp. All rights reserved.
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

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jms.JmsProperties;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.jms.annotation.JmsListener;

/**
 * The default Spring configuration for a JMS Listener sets the receive timeout (a polling
 * loop timer) too low for a cost-effective solution with IBM MQ.
 * See <a href="https://developer.ibm.com/messaging/2018/02/09/mq-spring-tip">this article</a>
 * for more information.
 *
 * This class will override that default value if it is not explicitly set in the application
 * properties. The application still has control though. If the app sets the value
 * via a call to the listener.setReceiveTimeout method, then that still will be honoured as it comes
 * after the creation and initial attribute setting of the listener object.
 *
 * This method is invoked early in the cycle, once the application environment has been loaded. We then
 * get the opportunity to inspect and add to the environment that will be used later on by the
 * JMS Listener initialisers.
 * 
 * This allows the MQ JMS listener to have a different effective default than other JMS providers.
 * 
 * Support for this property within Spring Boot itself was added in version 2.2.0
 * 
 */
@ConditionalOnClass({ JmsProperties.Listener.class, JmsListener.class })
@ConditionalOnMissingBean(JmsListener.class)
public class MQConfigurationListenerDefault implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
  
  @Autowired
  private final Long defaultReceiveTimeout = 30 * 1000L; // 30 seconds
  
  // There are a number of formats for the property name supported by Spring.
  // This set includes the recommended variants.
  private static String lcPrefix = "spring.jms.listener.";
  private static String ucPrefix = "SPRING_JMS_LISTENER_";
  //@formatter:off
  private final String timeoutProperties[] = { 
      lcPrefix + "receiveTimeout",  
      lcPrefix + "receivetimeout", 
      lcPrefix + "receive-timeout",
      lcPrefix + "receive_timeout", 
      lcPrefix + "RECEIVE_TIMEOUT", 
      ucPrefix + "RECEIVE_TIMEOUT" 
      };
  //@formatter:on
  

  @Bean
  @Lazy(false)
  public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
    try {
      String foundProperty = null;
      Object p = null;
      ConfigurableEnvironment env = (event != null) ? event.getEnvironment() : null;
      if (env != null) {
        // See if any of the variations of the property name exist in the environment
        for (String timeoutProperty : timeoutProperties) {
          p = env.getProperty(timeoutProperty);
          if (p != null) {
            foundProperty = timeoutProperty;
            break;
          }
        }

        // If the user has not given any specific value for this attribute, force the new default.
        if (foundProperty == null) {
          Properties props = new Properties();
          props.put(timeoutProperties[0], defaultReceiveTimeout);
          env.getPropertySources().addFirst(new PropertiesPropertySource(this.getClass().getName(), props));
        } 
      }
    }
    catch (Throwable e) {
      // If there are any errors (there shouldn't be, but just for safety here), then ignore them.
    }
  }
}
