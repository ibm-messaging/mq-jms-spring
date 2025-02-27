/*
 * Copyright © 2021 IBM Corp. All rights reserved.
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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class gives some basic support for JNDI access when looking up
 * ConnectionFactory (but NOT Destination) objects. The MQ JMSAdmin program
 * does have some additional configuration attributes which we might consider
 * adding in future if necessary. For example, user/password access to database and nested contexts.
 */

public class MQConfigurationPropertiesJndi {

  private static Logger logger = LoggerFactory.getLogger(MQConfigurationPropertiesJndi.class);

  /**
   * The name of the class that implements the JNDI lookup. For example,
   *   com.sun.jndi.fscontext.RefFSContextFactory
   * The implementation of that class must be available in your built application.
   */
  private String providerContextFactory = null;


  /**
   * The URL for the JNDI context. For example,
   *  file:///home/username/mqjms/jndi
   */
  private String providerUrl = null;

  private Map<String, String> additionalProperties = new HashMap<String,String>();


  public String getProviderContextFactory() {
    return providerContextFactory;
  }

  public void setProviderContextFactory(String providerContextFactory) {
    this.providerContextFactory = providerContextFactory;
  }

  public String getProviderUrl() {
    return providerUrl;
  }

  public void setProviderUrl(String providerUrl) {
    this.providerUrl = providerUrl;
  }

  public Map<String, String> getAdditionalProperties() {
    return additionalProperties;
  }

  public void setAdditionalProperties(Map<String, String> properties) {
    this.additionalProperties = properties;
  }

  public void traceProperties(String cfName) {
    if (!logger.isTraceEnabled())
      return;

    logger.trace("CF Name         : {}", cfName);
    logger.trace("jndiCF          : {}", getProviderContextFactory());
    logger.trace("jndiProviderUrl : {}", getProviderUrl());

    if (additionalProperties.size() > 0) {
      for (String s: additionalProperties.keySet()) {
        logger.trace("Additional Property - {} : {}",s,additionalProperties.get(s));
      }
    }
  }

}
