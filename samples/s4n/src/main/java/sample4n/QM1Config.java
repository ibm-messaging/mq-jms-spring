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
import com.ibm.mq.spring.boot.MQConnectionFactoryCustomizer;
import com.ibm.mq.spring.boot.MQConnectionFactoryFactory;
import com.ibm.mq.spring.boot.MQConnectionFactoryConfiguration.PooledMQConnectionFactoryConfiguration;

/*
 * This class allows us to build an MQ ConnectionFactory using the same properties and underlying construction
 * mechanisms as the default MQ Spring Boot module, but using a different configuration prefix. That allows us
 * to have multiple connections defined and managed within the same process and resource definitions.
 * 
 * Because we want to use two-phase XA transactions, we need to instantiate an XAConnectionFactory. And in this
 * example, we'll actually make it a Pooled XACF.
 * 
 * A variation of this class with QM1 replaced by QM2 gives the beans for the second queue manager.
 *  
 * There are approaches to handling the configuration that could be made more generic (eg putting the definitions in a list)
 * but for this sample, explicitly having two classes is OK. It shows more about how it's working.
 */

@Configuration
public class QM1Config {
  
  static final Logger log = LoggerFactory.getLogger(QM1Config.class);

  @Bean
  @ConfigurationProperties("qm1")
  MQConfigurationProperties qm1ConfigProperties() {
    return new MQConfigurationProperties();
  }

  //@Bean(name = "qm1")
  // Don't want this to be instantiated automatically as we will prefer the Pooled version
  MQXAConnectionFactory qm1ConnectionFactory(@Qualifier("qm1ConfigProperties") MQConfigurationProperties properties,
      ObjectProvider<SslBundles> sslBundles, ObjectProvider<List<MQConnectionFactoryCustomizer>> factoryCustomizers) {
    
    MQXAConnectionFactory x = new MQConnectionFactoryFactory(properties, sslBundles.getIfAvailable(), factoryCustomizers.getIfAvailable())
        .createConnectionFactory(MQXAConnectionFactory.class);
    U.trace(log, "Creating MQXAConnectionFactory");
    return x;
  }
  
  @Bean(name = "qm1p")
  JmsPoolXAConnectionFactory qm1PoolXAConnectionFactory(@Qualifier("qm1ConfigProperties") MQConfigurationProperties properties,
      ObjectProvider<SslBundles> sslBundles, ObjectProvider<List<MQConnectionFactoryCustomizer>> factoryCustomizers) {
    MQXAConnectionFactory connectionFactory = new MQConnectionFactoryFactory(properties, 
        sslBundles.getIfAvailable(), 
        factoryCustomizers.getIfAvailable()).createConnectionFactory(MQXAConnectionFactory.class);

    JmsPoolXAConnectionFactory x = PooledMQConnectionFactoryConfiguration.createInstance(JmsPoolXAConnectionFactory.class, connectionFactory, properties.getPool());
    U.trace(log, "Creating pooled MQXAConnectionFactory");

    return x;
     
  }
}
