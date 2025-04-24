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

// This class implements the minimal information needed for the testcontainers connection.
class PropertiesMQConnectionDetails implements MQConnectionDetails {

  private static final Logger logger = LoggerFactory.getLogger(PropertiesMQConnectionDetails.class);
  private final MQConfigurationProperties properties;

  public PropertiesMQConnectionDetails(MQConfigurationProperties properties) {
    logger.trace("constructor");
    this.properties = properties;
  }

  @Override
  public String getConnName() {
    return properties.getConnName();
  }

  @Override
  public String getQueueManager() {
    return properties.getQueueManager();
  }

  @Override
  public String getChannel() {
    return properties.getChannel();
  }

  @Override
  public String getUser() {
    return properties.getUser();
  }

  @Override
  public String getPassword() {
    return properties.getPassword();
  }
}
