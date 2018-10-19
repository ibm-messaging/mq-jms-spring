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

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

/**
 * The default Spring configuration for a JMS Listener sets the receive timeout (a polling
 * loop timer) too low for a cost-effective solution with IBM MQ. 
 * See <a href="https://developer.ibm.com/messaging/2018/02/09/mq-spring-tip">this article</a>
 * for more information.
 * 
 * This class will override that default value if it is not explicitly set in the application
 * properties. So the application still has control.
 * 
 * It is invoked early in the cycle, once the application environment has been loaded. We then
 * get the opportunity to inspect and add to the environment that will be used later on by the 
 * JMS Listener initialisers.
 */

public class MQConfigurationOverride implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
  @Autowired
  private final Long defaultReceiveTimeout = 60 * 1000L; // 60 seconds
  private final String timeoutProperty = "spring.jms.template.receiveTimeout";

  @Bean
  @Lazy(false)
  public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
    ConfigurableEnvironment env = (event != null)?event.getEnvironment():null;
    if (env != null) {
      Object p = env.getProperty(timeoutProperty);

      if (p == null) {
        // The user has not given any specific value for this attribute
        //System.out.println("MQConfigurationOverride: Setting receiveTimeout property to " + defaultReceiveTimeout);
        Properties props = new Properties();
        props.put(timeoutProperty, defaultReceiveTimeout);
        env.getPropertySources().addFirst(new PropertiesPropertySource(this.getClass().getName(), props));
      }
      else {
        // Leave the environment alone
        //System.out.println("MQConfigurationOverride: receiveTimeout is already set to " + p.toString());
      }
    }
  }

}
