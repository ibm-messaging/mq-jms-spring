/*
 * Copyright © 2018, 2021 IBM Corp. All rights reserved.
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

import jakarta.jms.ConnectionFactory;

import org.apache.commons.pool2.PooledObject;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jms.JmsPoolConnectionFactoryProperties;
import org.springframework.boot.autoconfigure.jms.JmsProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;

import com.ibm.mq.jakarta.jms.MQConnectionFactory;

/**
 * Configuration for IBM MQ {@link ConnectionFactory}.
 */
@Configuration(proxyBeanMethods=false)
@ConditionalOnMissingBean(ConnectionFactory.class)
class MQConnectionFactoryConfiguration {
  private static Logger logger = LoggerFactory.getLogger(MQConnectionFactoryConfiguration.class);
  
  @Configuration(proxyBeanMethods=false)
  @ConditionalOnClass({ CachingConnectionFactory.class })
  @ConditionalOnProperty(prefix = "ibm.mq.pool", name = "enabled", havingValue = "false", matchIfMissing = true)
  static class RegularMQConnectionFactoryConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "spring.jms.cache", name = "enabled", havingValue = "false")
    public MQConnectionFactory jmsConnectionFactory(MQConfigurationProperties properties,
        ObjectProvider<SslBundles> sslBundles,
        ObjectProvider<List<MQConnectionFactoryCustomizer>> factoryCustomizers) {
      logger.trace("Creating single MQConnectionFactory");
      return createConnectionFactory(properties, sslBundles, factoryCustomizers);
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.jms.cache", name = "enabled", havingValue = "true", matchIfMissing = true)
    public CachingConnectionFactory cachingJmsConnectionFactory(MQConfigurationProperties properties,
        ObjectProvider<SslBundles> sslBundles,
        ObjectProvider<List<MQConnectionFactoryCustomizer>> factoryCustomizers, JmsProperties jmsProperties) {

      JmsProperties.Cache cacheProperties = jmsProperties.getCache();
      
      logger.trace("Creating caching MQConnectionFactory");
      MQConnectionFactory wrappedConnectionFactory = createConnectionFactory(properties, sslBundles, factoryCustomizers);

      CachingConnectionFactory connectionFactory = new CachingConnectionFactory(wrappedConnectionFactory);
      connectionFactory.setCacheConsumers(cacheProperties.isConsumers());
      connectionFactory.setCacheProducers(cacheProperties.isProducers());
      connectionFactory.setSessionCacheSize(cacheProperties.getSessionCacheSize());

      return connectionFactory;
    }

  }

  private static MQConnectionFactory createConnectionFactory(MQConfigurationProperties properties,
      ObjectProvider<SslBundles> sslBundles,
      ObjectProvider<List<MQConnectionFactoryCustomizer>> factoryCustomizers) {
    return new MQConnectionFactoryFactory(properties, sslBundles.getIfAvailable(), factoryCustomizers.getIfAvailable()).createConnectionFactory(MQConnectionFactory.class);
  }

  @Configuration(proxyBeanMethods=false)
  @ConditionalOnClass({ JmsPoolConnectionFactory.class, PooledObject.class })
  static class PooledMQConnectionFactoryConfiguration {

    @Bean(destroyMethod = "stop")
    @ConditionalOnProperty(prefix = "ibm.mq.pool", name = "enabled", havingValue = "true", matchIfMissing = false)
    public JmsPoolConnectionFactory pooledJmsConnectionFactory(MQConfigurationProperties properties,
        ObjectProvider<SslBundles> sslBundles,
        ObjectProvider<List<MQConnectionFactoryCustomizer>> factoryCustomizers) {

      logger.trace("Creating pooled MQConnectionFactory");
      MQConnectionFactory connectionFactory = createConnectionFactory(properties, sslBundles, factoryCustomizers);

      return create(connectionFactory, properties.getPool());
    }

    private JmsPoolConnectionFactory create(ConnectionFactory connectionFactory, JmsPoolConnectionFactoryProperties poolProperties) {

      JmsPoolConnectionFactory pooledConnectionFactory = new JmsPoolConnectionFactory();
      pooledConnectionFactory.setConnectionFactory(connectionFactory);

      pooledConnectionFactory.setBlockIfSessionPoolIsFull(poolProperties.isBlockIfFull());

      if (poolProperties.getBlockIfFullTimeout() != null) {
        pooledConnectionFactory.setBlockIfSessionPoolIsFullTimeout(poolProperties.getBlockIfFullTimeout().toMillis());
      }

      if (poolProperties.getIdleTimeout() != null) {
        pooledConnectionFactory.setConnectionIdleTimeout((int) poolProperties.getIdleTimeout().toMillis());
      }

      pooledConnectionFactory.setMaxConnections(poolProperties.getMaxConnections());
      pooledConnectionFactory.setMaxSessionsPerConnection(poolProperties.getMaxSessionsPerConnection());

      if (poolProperties.getTimeBetweenExpirationCheck() != null) {
        pooledConnectionFactory.setConnectionCheckInterval(poolProperties.getTimeBetweenExpirationCheck().toMillis());
      }

      pooledConnectionFactory.setUseAnonymousProducers(poolProperties.isUseAnonymousProducers());
      return pooledConnectionFactory;
    }

  }
}