/*
 * Copyright © 2018, 2023 IBM Corp. All rights reserved.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.JmsProperties;
import org.springframework.boot.autoconfigure.jms.JndiConnectionFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.jta.JtaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.ibm.mq.jakarta.jms.MQConnectionFactory;

import jakarta.jms.ConnectionFactory;

// See https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0.0-M5-Release-Notes
// where autoconfiguration was moved from META-INF/spring.factories to a separate file. The
// original file can remain in place though for both Boot 2 and Boot 3.

@Configuration(proxyBeanMethods=false)
@AutoConfigureBefore(JmsAutoConfiguration.class)
@AutoConfigureAfter({ JndiConnectionFactoryAutoConfiguration.class, JtaAutoConfiguration.class,MQConfigurationSslBundles.class})
@ConditionalOnClass({ ConnectionFactory.class, MQConnectionFactory.class })
@ConditionalOnProperty(prefix = "ibm.mq", name = "autoConfigure", matchIfMissing=true)
@ConditionalOnMissingBean(ConnectionFactory.class)
@EnableConfigurationProperties({MQConfigurationProperties.class, JmsProperties.class})
@Import({ MQConfigurationSslBundles.class, MQXAConnectionFactoryConfiguration.class,MQConnectionFactoryConfiguration.class })
public class MQAutoConfiguration {
  private static Logger logger = LoggerFactory.getLogger(MQAutoConfiguration.class);
  public MQAutoConfiguration() {
    logger.trace("constructor");
  }
}

