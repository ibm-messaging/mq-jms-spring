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

package com.ibm.mq.spring.testcontainers.service.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionDetailsFactory;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource;

import com.ibm.mq.spring.boot.MQConnectionDetails;
import com.ibm.mq.testcontainers.MQContainer;

public class MQContainerConnectionDetailsFactory
extends ContainerConnectionDetailsFactory<MQContainer, MQConnectionDetails> {

  private static Logger logger = LoggerFactory.getLogger(MQContainerConnectionDetailsFactory.class);

  @Override
  protected MQConnectionDetails getContainerConnectionDetails(
      ContainerConnectionSource<MQContainer> source) {
    logger.trace("Source: {}", (source == null)?"null":source.getOrigin().toString());
    return new MQContainerConnectionDetails(source);
  }

  private static final class MQContainerConnectionDetails
  extends ContainerConnectionDetails<MQContainer> implements MQConnectionDetails {
    private static Logger logger = LoggerFactory.getLogger(MQContainerConnectionDetails.class);

    private MQContainerConnectionDetails(ContainerConnectionSource<MQContainer> source) {
      super(source);
      logger.trace("constructor");
    }

    @Override
    public String getConnName() {
      return getContainer().getConnName();
    }

    @Override
    public String getQueueManager() {
      return getContainer().getQueueManager();
    }

    @Override
    public String getChannel() {
      return getContainer().getChannel();
    }

    @Override
    public String getUser() {
      return getContainer().getAppUser();
    }

    @Override
    public String getPassword() {
      return getContainer().getAppPassword();
    }
  }
}
