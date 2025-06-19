/*
 * Copyright © 2025 IBM Corp. All rights reserved.
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

/**
 * This Properties class provides an option to connect to MQ using JWT authentication with JWKS.
 * When used, it is mandatory to populate all the properties, else it may fail or
 * fallback to default connection
 */
public class MQConfigurationPropertiesTokenServer {

  private static Logger logger = LoggerFactory.getLogger(MQConfigurationPropertiesTokenServer.class);

  /**
   * URL endpoint of the token provider
   */
  private String endpoint;

  /**
   * Identity of the client requesting the token
   */
  private String clientId;

  /**
   * Client secret for authenticating with the token provider
   */
  private String clientSecret;

  /**
   * Returns the JWT authentication server token URL.
   *
   * @return URL endpoint of the token provider
   */
  public String getEndpoint() {
    return endpoint;
  }

  /**
   * Set the JWT authentication server token URL.
   *
   * @param endpoint - URL endpoint of the token provider
   */
  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  /**
   * Returns the JWT authentication server client ID.
   *
   * @return Identity of the client requesting the token.
   */
  public String getClientId() {
    return clientId;
  }

  /**
   * Set the JWT authentication server client ID.
   *
   * @param clientId - Identity of the client requesting the token
   */
  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  /**
   * Returns the JWT Auth Server Client Secret.
   *
   * @return Client secret that authenticates with the token provider
   */
  public String getClientSecret() {
    return clientSecret;
  }

  /**
   * Set the JWT Auth Server Client Secret.
   *
   * @param clientSecret - Client secret for authenticating with the token provider
   */
  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }

  /**
   * Traces the configuration attributes of the current object.
   * Use the parent logger so it appears neater in the output.
   */
  public void traceProperties(Logger parentLogger) {
    if (!parentLogger.isTraceEnabled()) {
      return;
    }

    parentLogger.trace("Token Server");
    parentLogger.trace("  clientId         : {}", getClientId());
    parentLogger.trace("  endpoint         : {}", getEndpoint());
    parentLogger.trace("  clientSecret set : {}", (getClientSecret() != null && getClientSecret().length() > 0) ? "YES":"NO");
  }

}
