/*
 * Copyright © 2022 IBM Corp. All rights reserved.
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
import org.springframework.boot.context.properties.DeprecatedConfigurationProperty;

/**
 * This class gives a mechanism to control access to keystore/truststore JKS files
 * without needing to use -D options on the command line. Other keystore types might
 * be supported within the JSSE environment, but this is the most common. The same system
 * properties ought to work though.
 */

public class MQConfigurationPropertiesJks {

  private static Logger logger = LoggerFactory.getLogger(MQConfigurationPropertiesJks.class);
  
  private String keyStore = null;
  private String trustStore = null;
  private String keyStorePassword = null;
  private String trustStorePassword = null;

  private Map<String, String> additionalProperties = new HashMap<String,String>();

  @DeprecatedConfigurationProperty(replacement="spring.ssl.bundle")
  public String getKeyStore() {
    return keyStore;
  }

  public void setKeyStore(String keyStore) {
    this.keyStore = keyStore;
  }

  @DeprecatedConfigurationProperty(replacement="spring.ssl.bundle")
  public String getTrustStore() {
    return trustStore;
  }

  public void setTrustStore(String trustStore) {
    this.trustStore = trustStore;
  }
  
  @DeprecatedConfigurationProperty(replacement="spring.ssl.bundle")
  public String getKeyStorePassword() {
    return keyStorePassword;
  }

  public void setKeyStorePassword(String keyStorePassword) {
    this.keyStorePassword = keyStorePassword;
  }

  @DeprecatedConfigurationProperty(replacement="spring.ssl.bundle")
  public String getTrustStorePassword() {
    return trustStorePassword;
  }

  public void setTrustStorePassword(String trustStorePassword) {
    this.trustStorePassword = trustStorePassword;
  }
  
  public Map<String, String> getAdditionalProperties() {
    return additionalProperties;
  }

  public void setAdditionalProperties(Map<String, String> properties) {
    this.additionalProperties = properties;
  }

  public void traceProperties() {
    if (!logger.isTraceEnabled())
      return;

    logger.trace("keyStore         : {}", getKeyStore());
    logger.trace("trustStore       : {}", getTrustStore());
    logger.trace("keyStorePw set   : {}", (getKeyStorePassword()   != null || getKeyStorePassword().length()   > 0) ? "YES":"NO");
    logger.trace("trustStorePw set : {}", (getTrustStorePassword() != null || getTrustStorePassword().length() > 0) ? "YES":"NO");

    if (additionalProperties.size() > 0) {
      for (String s: additionalProperties.keySet()) {
        logger.trace("Additional Property - {} : {}",s,additionalProperties.get(s));
      }
    }
  }
}
