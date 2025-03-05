/*
 * Copyright 2024, 2025 IBM Corp. All rights reserved.
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

package sample4n;

// See comments in QM1Config that also describe this class and its purpose.

import java.util.List;

import org.messaginghub.pooled.jms.JmsPoolXAConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ibm.mq.jakarta.jms.MQXAConnectionFactory;
import com.ibm.mq.spring.boot.MQConfigurationProperties;
import com.ibm.mq.spring.boot.MQConnectionFactoryConfiguration.PooledMQConnectionFactoryConfiguration;
import com.ibm.mq.spring.boot.MQConnectionFactoryCustomizer;
import com.ibm.mq.spring.boot.MQConnectionFactoryFactory;

@Configuration
public class QM2Config {
  static final Logger log = LoggerFactory.getLogger(QM2Config.class);

  public MQConfigurationProperties props; 
  
  @Bean
  @ConfigurationProperties(prefix = "qm2")
  MQConfigurationProperties qm2ConfigProperties() {
    return new MQConfigurationProperties();
  }
  
  //@Bean(name = "qm2")
  // Don't want this to be instantiated automatically as we will prefer the Pooled version
  MQXAConnectionFactory qm2XAConnectionFactory(@Qualifier("qm2ConfigProperties") MQConfigurationProperties properties,
      ObjectProvider<SslBundles> sslBundles, ObjectProvider<List<MQConnectionFactoryCustomizer>> factoryCustomizers) {
    
    MQXAConnectionFactory x = new MQConnectionFactoryFactory(properties, sslBundles.getIfAvailable(), factoryCustomizers.getIfAvailable())
        .createConnectionFactory(MQXAConnectionFactory.class);
    U.trace(log, "Creating MQXAConnectionFactory");

    return x;
  }
  
  @Bean(name = "qm2p")
  JmsPoolXAConnectionFactory qm2PoolXAConnectionFactory(@Qualifier("qm2ConfigProperties") MQConfigurationProperties properties,
      ObjectProvider<SslBundles> sslBundles, ObjectProvider<List<MQConnectionFactoryCustomizer>> factoryCustomizers) {
    MQXAConnectionFactory connectionFactory = new MQConnectionFactoryFactory(properties, 
        sslBundles.getIfAvailable(), 
        factoryCustomizers.getIfAvailable()).createConnectionFactory(MQXAConnectionFactory.class);

    JmsPoolXAConnectionFactory x = PooledMQConnectionFactoryConfiguration.createInstance(JmsPoolXAConnectionFactory.class, connectionFactory, properties.getPool());
    U.trace(log, "Creating pooled MQXAConnectionFactory");

    return x;
     
  }
  
}
