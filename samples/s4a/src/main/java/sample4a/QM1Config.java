/*
 * Copyright 2024 IBM Corp. All rights reserved.
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

package sample4a;

import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;

import com.ibm.mq.jakarta.jms.MQXAConnectionFactory;
import com.ibm.mq.spring.boot.MQConfigurationProperties;
import com.ibm.mq.spring.boot.MQConnectionFactoryCustomizer;
import com.ibm.mq.spring.boot.MQConnectionFactoryFactory;

import com.atomikos.jms.AtomikosConnectionFactoryBean;

import jakarta.jms.ConnectionFactory;

/*
 * This class allows us to build an MQ ConnectionFactory using the same properties and underlying construction
 * mechanisms as the default MQ Spring Boot module, but using a different configuration prefix. That allows us
 * to have multiple connections defined and managed within the same process and resource definitions.
 * 
 * Because we want to use two-phase XA transactions, we need to instantiate an XAConnectionFactory. In fact, because
 * we are using Atomkios as the coordinator, we can explicitly create an appropriate bean here. It then gets associated
 * with the JmsListener. 
 * 
 * A variation of this class with QM1 replaced by QM2 gives the beans for the second queue manager.
 *  
 * There are approaches to handling the configuration that could be made more generic (eg putting the definitions in a list)
 * but for this sample, explicitly having two classes is OK. It shows more about how it's working.
 */

@Configuration
public class QM1Config {

  @Bean
  @ConfigurationProperties("qm1")
  MQConfigurationProperties qm1ConfigProperties() {
    return new MQConfigurationProperties();
  }

  @Bean(name = "qm1")
  AtomikosConnectionFactoryBean qm1ConnectionFactory(@Qualifier("qm1ConfigProperties") MQConfigurationProperties properties,
      ObjectProvider<SslBundles> sslBundles, ObjectProvider<List<MQConnectionFactoryCustomizer>> factoryCustomizers) {

    // Start with the MQ XA Connection Factory
    MQXAConnectionFactory cf = new MQConnectionFactoryFactory(properties, sslBundles.getIfAvailable(),
        factoryCustomizers.getIfAvailable()).createConnectionFactory(MQXAConnectionFactory.class);

    // And then link that into the Atomikos equivalent
    AtomikosConnectionFactoryBean ab = new AtomikosConnectionFactoryBean();
    ab.setXaConnectionFactory(cf);
    ab.setUniqueResourceName("QM1");
    ab.setLocalTransactionMode(false); // Ensure it's using global transaction

    return ab;
  }

  @Bean
  JmsListenerContainerFactory<?> qm1JmsListenerContainerFactory(@Qualifier("qm1") ConnectionFactory connectionFactory,
      DefaultJmsListenerContainerFactoryConfigurer configurer) {
    DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
    configurer.configure(factory, connectionFactory);

    factory.setErrorHandler(new DefaultErrorHandler()); // Setup an error handler for the listener

    return factory;
  }
}
