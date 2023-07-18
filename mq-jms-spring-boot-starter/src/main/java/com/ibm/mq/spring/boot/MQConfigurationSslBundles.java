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

import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ssl.NoSuchSslBundleException;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.stereotype.Component;

@Component
public class MQConfigurationSslBundles {
  private static Logger logger = LoggerFactory.getLogger(MQConfigurationSslBundles.class);

  static SslBundles bundles = null;

  /* This is called during the initialisation phase */
  public MQConfigurationSslBundles(SslBundles sslBundles) {
    logger.trace("constructor - Bundles are {}", (sslBundles == null) ? "null" : "not null");
    bundles = sslBundles;
  }

  /* If the bundle name does not exist, then getBundle throws an exception. Since
     there is always some default bundle, we can't rely on there being no bundle.
     So we log an error, but otherwise try to continue.
   */
  public static SSLSocketFactory getSSLSocketFactory(String b) {
    SSLSocketFactory sf = null;
    logger.trace("getSSLSocketFactory for {}", b);

    if (b == null || b.isEmpty()) {
      return sf;
    }

    if (bundles != null) {
      try {
        SslBundle sb = bundles.getBundle(b);
        sf = sb.createSslContext().getSocketFactory();
      }
      catch (NoSuchSslBundleException e) {
        logger.error("No SSL bundle found for {}", b);
      }
    }
    return sf;
  }
}