/*
 * Copyright © 2023 IBM Corp. All rights reserved.
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

/*
 * The SslBundles packages were introduced in Spring Boot 3.1 which
 * means that this package cannot be used in the older JMS2-based package.
 * The makeJms3.sh script copies and renames this file during the build process
 */

package com.ibm.mq.spring.boot;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.ssl.NoSuchSslBundleException;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureBefore(MQConfigurationProperties.class)
public class MQConfigurationSslBundles {
  private static Logger logger = LoggerFactory.getLogger(MQConfigurationSslBundles.class);

  /**
   * The configured set of SSL Bundles. A map that can be
   * references by the sslBundle key.
   */
  private static SslBundles sslBundles = null;

  /* This is called during the initialisation phase */
  public MQConfigurationSslBundles(SslBundles _sslBundles) {
    logger.trace("constructor - Bundles are {}", (_sslBundles == null) ? "null" : "not null");
    sslBundles = _sslBundles;
  }

  static boolean isSupported() {
    logger.trace("SSLBundles are supported");
    return true;
  }

  /* If the bundle name does not exist, then getBundle throws an exception. Since
   * there is always some default bundle in Boot 3, we can't rely on there being
   * a null bundle. So we log an error for your configuration, but otherwise try to continue.
   */
  public static SSLSocketFactory getSSLSocketFactory(String b) {
    SSLSocketFactory sf = null;

    if (b == null || b.isEmpty()) {
      /* Should never get here as the caller has already checked */
      logger.trace("getSSLSocketFactory - null/empty bundle name requested");
      return sf;
    }

    if (sslBundles != null) {
      try {
        SslBundle sb = sslBundles.getBundle(b);
        logger.trace("SSL Bundle for {} - found", b);
        SSLContext sc = sb.createSslContext();     
        // logger.trace("SSL Protocol is {}",sc.getProtocol());
        sf = sc.getSocketFactory();
      }
      catch (NoSuchSslBundleException e) {
        logger.error("SSL bundle for {} - not found", b);
      }
    }
    return sf;
  }

}