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

import java.util.List;

import javax.jms.ConnectionFactory;

import org.apache.commons.pool2.PooledObject;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ibm.mq.jms.MQConnectionFactory;

/**
 * Configuration for IBM MQ {@link ConnectionFactory}.
 */
@Configuration
@ConditionalOnMissingBean(ConnectionFactory.class)
class MQConnectionFactoryConfiguration {

	@Configuration
	@ConditionalOnProperty(prefix = "ibm.mq.pool", name = "enabled", havingValue = "false", matchIfMissing = true)
	static class RegularMQConnectionFactoryConfiguration {

		@Bean
		public MQConnectionFactory jmsConnectionFactory(MQConfigurationProperties properties,
																										ObjectProvider<List<MQConnectionFactoryCustomizer>> factoryCustomizers) {

			return new MQConnectionFactoryFactory(properties, factoryCustomizers.getIfAvailable())
							.createConnectionFactory(MQConnectionFactory.class);
		}
	}

	@Configuration
	@ConditionalOnClass({JmsPoolConnectionFactory.class, PooledObject.class})
	static class PooledMQConnectionFactoryConfiguration {

		@Bean(destroyMethod = "stop")
		@ConditionalOnProperty(prefix = "ibm.mq.pool", name = "enabled", havingValue = "true", matchIfMissing = false)
		public JmsPoolConnectionFactory pooledJmsConnectionFactory(MQConfigurationProperties properties,
																															 ObjectProvider<List<MQConnectionFactoryCustomizer>> factoryCustomizers) {

			MQConnectionFactory connectionFactory = new MQConnectionFactoryFactory(properties, factoryCustomizers.getIfAvailable())
							.createConnectionFactory(MQConnectionFactory.class);

			return create(connectionFactory, properties.getPool());
		}

		private JmsPoolConnectionFactory create(ConnectionFactory connectionFactory, MQConfigurationProperties.PoolProperties poolProperties) {

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
