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

// See comments in QM1Config that also describe this class and its purpose.

import java.util.List;
import java.util.Properties;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.atomikos.jms.AtomikosConnectionFactoryBean;
import com.ibm.mq.jakarta.jms.MQXAConnectionFactory;
import com.ibm.mq.spring.boot.MQConfigurationProperties;
import com.ibm.mq.spring.boot.MQConnectionFactoryCustomizer;
import com.ibm.mq.spring.boot.MQConnectionFactoryFactory;

@Configuration
public class QM2Config {
  @Bean
  @ConfigurationProperties("qm2")
  MQConfigurationProperties qm2ConfigProperties() {
    return new MQConfigurationProperties();
  }

  @Bean(name = "qm2")
  AtomikosConnectionFactoryBean qm2ConnectionFactory(@Qualifier("qm2ConfigProperties") MQConfigurationProperties properties,
      ObjectProvider<SslBundles> sslBundles, ObjectProvider<List<MQConnectionFactoryCustomizer>> factoryCustomizers) {

    // Start with the MQ XA Connection Factory
    MQXAConnectionFactory cf = new MQConnectionFactoryFactory(properties, sslBundles.getIfAvailable(), factoryCustomizers.getIfAvailable())
        .createConnectionFactory(MQXAConnectionFactory.class);
    
    // And then link that into the Atomikos equivalent
    AtomikosConnectionFactoryBean ab = new AtomikosConnectionFactoryBean();
    ab.setXaConnectionFactory(cf);
    ab.setUniqueResourceName("QM2");
    ab.setLocalTransactionMode(false); // Ensure it's using global transaction
    
    return ab;
  }
}
