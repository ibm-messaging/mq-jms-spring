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

package com.ibm.mq.testcontainers;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import com.github.dockerjava.api.command.InspectContainerResponse;

public class MQContainer extends GenericContainer<MQContainer> {

  // This default points at the current "MQ Advanced for Developers" image. That image
  // has license restrictions, constraining it to internal development and unit testing. See
  // https://www.ibm.com/support/customer/csol/terms/?id=L-HYGL-6STWD6&lc=en for full terms.
  private static final String DEFAULT_IMAGE_NAME = "icr.io/ibm-messaging/mq:latest";
  public static final DockerImageName DEFAULT_IMAGE = DockerImageName.parse(DEFAULT_IMAGE_NAME);

  private static final Logger logger = LoggerFactory.getLogger(MQContainer.class);

  // These come from the Developer image default configuration
  private static final String DEFAULT_QUEUE_MANAGER = "QM1";
  private static final String DEFAULT_CHANNEL = "DEV.APP.SVRCONN";

  // The known users in the Developer image. The container does not provide any
  // default passwords; they have to be set externally. We will make
  // them the same as the userid unless overridden. Set the passwords to empty
  //
  private static final String DEFAULT_APP_USER = "app";
  private static final String DEFAULT_ADMIN_USER = "admin";

  private static final String DEFAULT_APP_PASSWORD = DEFAULT_APP_USER;
  private static final String DEFAULT_ADMIN_PASSWORD = DEFAULT_ADMIN_USER;

  private static final int PORT_QMGR      = 1414;
  private static final int PORT_WEBSERVER = 9443;

  private static final Integer[] exposedPorts = {PORT_QMGR, PORT_WEBSERVER};

  private String queueManager = DEFAULT_QUEUE_MANAGER;
  private String channel = DEFAULT_CHANNEL;

  private String appUser = DEFAULT_APP_USER;
  private String adminUser = DEFAULT_ADMIN_USER;
  private String appPassword = DEFAULT_APP_PASSWORD;
  private String adminPassword = DEFAULT_ADMIN_PASSWORD;

  private boolean startWeb = false;
  private String  startupMQSC = null;
  private String  startupMsg = ".*AMQ5026.*"; // Msgid for "The listener <insert> has started";

  // The constructor. As recommended by testcontainers.org, a name is always to be
  // passed in. But we do provide a DEFAULT_IMAGE to simplify that.
  public MQContainer(DockerImageName imageName) {
    super(imageName);

    withExposedPorts(exposedPorts);

    withStartupTimeout(Duration.ofMinutes(2));

    logger.trace("Creating container from image: {}",imageName.toString());
  }

  @Override
  // This method is called before the image is actually started. So we can do things like
  // set environment variables, or mount additional files into the container.
  protected void configure() {
    logger.trace("configuring image");
    // The container passwords should not be passed via environment variables.
    // That mechanism is deprecated. Instead, they are set via a secret. We use
    // these lines to create a temporary file for the password, and mount/copy that into the MQ container.
    if (isNotNullOrEmpty(adminPassword)) {
      withCopyToContainer(Transferable.of(adminPassword),"/run/secrets/mqAdminPassword");
    }
    if (isNotNullOrEmpty(appPassword)) {
      withCopyToContainer(Transferable.of(appPassword),"/run/secrets/mqAppPassword");
    }

    if (isNotNullOrEmpty(this.startupMQSC)) {
      withCopyToContainer(MountableFile.forClasspathResource(startupMQSC,0444),"/etc/mqm/" + this.startupMQSC);
    }
    withEnv("MQ_QMGR_NAME", queueManager);

    if (this.startWeb) {
      // This message does not have an AMQ msgid. But it is also not translated.
      startupMsg = ".*Started web server.*";
    }
    withEnv("MQ_ENABLE_EMBEDDED_WEB_SERVER",this.startWeb?"true":"false");

    waitingFor(Wait.forLogMessage(startupMsg, 1));
  }


  @Override
  protected void containerIsStarted(InspectContainerResponse containerInfo) {
    if (this.startWeb) {
      logger.info(
          "Started IBM MQ container. The Web UI is available under: https://{}:{}",
          getHost(),
          getWebServerPort());
    } else {
      logger.info("Started IBM MQ container. The Web UI is disabled.");
    }
  }

  // The "MQ Advanced for Developer" image needs you to explicitly accept the license
  public MQContainer acceptLicense() {
    addEnv("LICENSE", "accept");
    return this;
  }

  public MQContainer withWebServer() {
    this.startWeb = true;
    return this;
  }

  public MQContainer withChannel(String s) {
    this.channel = s;
    return this;
  }

  public MQContainer withQueueManager(String s) {
    this.queueManager = s;
    return this;
  }

  public MQContainer withAppUser(String s) {
    this.appUser = s;
    return this;
  }

  public MQContainer withAdminUser(String s) {
    this.adminUser = s;
    return this;
  }

  public MQContainer withAppPassword(String s) {
    this.appPassword = s;
    return this;
  }

  public MQContainer withAdminPassword(String s) {
    this.adminPassword = s;
    return this;
  }

  public MQContainer withStartupMQSC(String s) {
    this.startupMQSC = s;
    return this;
  }

  public int getPort() {
    return getMappedPort(PORT_QMGR);
  }

  public int getWebServerPort() {
    return getMappedPort(PORT_WEBSERVER);
  }

  public String getConnName() {
    return String.format("%s(%d)", getHost(), getPort());
  }

  public String getQueueManager() {
    return this.queueManager;
  }

  public String getChannel() {
    return channel;
  }

  public String getAppUser() {
    return this.appUser;
  }

  public String getAdminUser() {
    return this.adminUser;
  }

  public String getAppPassword() {
    return this.appPassword;
  }

  public String getAdminPassword() {
    return this.adminPassword;
  }

  private static boolean isNullOrEmpty(String s) {
    if (s == null || s.isEmpty()) {
      return true;
    }
    return false;
  }
  private static boolean isNotNullOrEmpty(String s) {
    return !isNullOrEmpty(s);
  }

}
