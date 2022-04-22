package com.ibm.mq.spring.boot;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class JmsPoolConnectionFactoryProperties {
  private static Logger logger = LoggerFactory.getLogger(JmsPoolConnectionFactoryProperties.class);

  // This is a dummy class to allow us to use the rest of the MQ Spring Boot code unchanged
  // when doing the automatic conversion to jakarta packages.
  // org.messaginghub/pooled-jms is not yet
  // enabled for Jakarta so we fake it here. But report an error if someone does
  // try to enable it.
  public void setEnabled(boolean b) {
    if (b) {
      logger.error("Use of JMS Pool connection properties is not currently available for JMS3 (Jakarta) components");
    }
  }

  public void setBlockIfFull(boolean blockIfFull) {
  }

  public void setBlockIfFullTimeout(Duration blockIfFullTimeout) {
  }

  public void setIdleTimeout(Duration idleTimeout) {
  }

  public void setMaxConnections(int maxConnections) {
  }

  public void setMaxSessionsPerConnection(int maxSessionsPerConnection) {
  }

  public void setTimeBetweenExpirationCheck(Duration timeBetweenExpirationCheck) {
  }

  public void setUseAnonymousProducers(boolean useAnonymousProducers) {
  }

  public boolean isEnabled() {
    return false;
  }

  public boolean isBlockIfFull() {
    return false;
  }

  public Duration getBlockIfFullTimeout() {
    return Duration.ofMillis(0);
  }

  public Duration getIdleTimeout() {
    return Duration.ofMillis(0);
  }

  public int getMaxConnections() {
    return 9999;
  }

  public int getMaxSessionsPerConnection() {
    return 9999;
  }

  public Duration getTimeBetweenExpirationCheck() {
    return Duration.ofMillis(0);
  }

  public boolean isUseAnonymousProducers() {
    return false;
  }
}
