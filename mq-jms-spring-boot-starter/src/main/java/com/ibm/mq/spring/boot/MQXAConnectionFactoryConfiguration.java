/*
 * Copyright © 2018, 2025 IBM Corp. All rights reserved.
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

import java.util.List;

import org.apache.commons.pool2.PooledObject;
import org.messaginghub.pooled.jms.JmsPoolXAConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.ssl.SslProperties;
import org.springframework.boot.jms.XAConnectionFactoryWrapper;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.ibm.mq.jakarta.jms.MQConnectionFactory;
import com.ibm.mq.jakarta.jms.MQXAConnectionFactory;
import com.ibm.mq.spring.boot.MQConnectionFactoryConfiguration.PooledMQConnectionFactoryConfiguration;

import jakarta.jms.ConnectionFactory;

/**
 * Configuration for IBM MQ XA {@link ConnectionFactory}.
 * The MQXAConnectionFactory is a subclass of MQConnectionFactory.
 */
@Configuration(proxyBeanMethods=false)
@ConditionalOnBean(XAConnectionFactoryWrapper.class)
@ConditionalOnMissingBean(ConnectionFactory.class)
class MQXAConnectionFactoryConfiguration {
  private static Logger logger = LoggerFactory.getLogger(MQXAConnectionFactoryConfiguration.class);

  @Primary
  @Bean(name = { "jmsConnectionFactory", "xaJmsConnectionFactory" })
  public ConnectionFactory jmsConnectionFactory(MQConnectionDetails connectionDetails,
      MQConfigurationProperties properties,
      ObjectProvider<SslBundles> sslBundles,
      ObjectProvider<SslProperties> sslProperties,
      ObjectProvider<List<MQConnectionFactoryCustomizer>> factoryCustomizers,
      XAConnectionFactoryWrapper wrapper) throws Exception {
    logger.trace("Creating MQXAConnectionFactory");
    MQXAConnectionFactory connectionFactory = new MQConnectionFactoryFactory(connectionDetails, properties, sslBundles.getIfAvailable(), sslProperties.getIfAvailable(), factoryCustomizers.getIfAvailable()).createConnectionFactory(MQXAConnectionFactory.class);
    return wrapper.wrapConnectionFactory(connectionFactory);
  }

  @Bean
  public ConnectionFactory nonXaJmsConnectionFactory(MQConnectionDetails connectionDetails,
      MQConfigurationProperties properties,
      ObjectProvider<SslBundles> sslBundles,
      ObjectProvider<SslProperties> sslProperties,
      ObjectProvider<List<MQConnectionFactoryCustomizer>> factoryCustomizers) {
    logger.trace("Creating non-XA MQConnectionFactory");
    return new MQConnectionFactoryFactory(connectionDetails, properties, sslBundles.getIfAvailable(), sslProperties.getIfAvailable(),
        factoryCustomizers.getIfAvailable()).createConnectionFactory(MQConnectionFactory.class);
  }

  @Configuration(proxyBeanMethods=false)
  @ConditionalOnClass({ JmsPoolXAConnectionFactory.class, PooledObject.class })
  static public class PooledMQXAConnectionFactoryConfiguration {

    @Bean(destroyMethod = "stop")
    @ConditionalOnProperty(prefix = "ibm.mq.pool", name = "enabled", havingValue = "true", matchIfMissing = false)
    JmsPoolXAConnectionFactory pooledJmsXAConnectionFactory(MQConnectionDetails connectionDetails,
        MQConfigurationProperties properties,
        ObjectProvider<SslBundles> sslBundles,
        ObjectProvider<SslProperties> sslProperties,
        ObjectProvider<List<MQConnectionFactoryCustomizer>> factoryCustomizers) {

      logger.trace("Creating pooled MQXAConnectionFactory");
      MQXAConnectionFactory connectionFactory = new MQConnectionFactoryFactory(connectionDetails, properties, sslBundles.getIfAvailable(), sslProperties.getIfAvailable(),
          factoryCustomizers.getIfAvailable()).createConnectionFactory(MQXAConnectionFactory.class);

      return PooledMQConnectionFactoryConfiguration.createInstance(JmsPoolXAConnectionFactory.class, connectionFactory, properties.getPool());
    }
  }
}
