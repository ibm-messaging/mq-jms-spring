/*
 * Copyright © 2024 IBM Corp. All rights reserved.
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

import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class with a very short name that we can use for utility methods
 */
public class U {
  private static Logger logger = LoggerFactory.getLogger(U.class);

  static boolean isNullOrEmpty(String s) {
    if (s == null || s.isEmpty()) {
      return true;
    }
    else {
      return false;
    }
  }

  static boolean isNotNullOrEmpty(String s) {
    return !isNullOrEmpty(s);
  }

  /**
   * Run through the Manifest files to try to extract the MQ client version that we're
   * actually running with. That might be useful for some feature testing.
   *
   * @return A string like "9.4.3.0". If it cannot find the values, then it returns "0.0.0.0"
   */
  static String getMQClientVersion() {
    String version = "0.0.0.0";
    try {

      ClassLoader cl = U.class.getClassLoader();

      Enumeration<URL> res = cl.getResources("META-INF/MANIFEST.MF");
      while (res.hasMoreElements()) {
        try {
          URL url = res.nextElement();
          Manifest mf = new Manifest(url.openStream());
          Attributes attr = mf.getMainAttributes();

          // The Jakarta and JMSv2 jars have slightly different titles but
          // this test seems to pull out both.
          //     Implementation-Title=IBM MQ classes for JMS
          //     Implementation-Version=9.4.3.0 - p943-dfct-L250514.1
          Object title = attr.get(new Name("Implementation-Title"));
          Object vers  = attr.get(new Name("Implementation-Version"));

          if (title != null && vers != null && title instanceof String) {
            if (((String) title).startsWith("IBM MQ classes for JMS")) {
              version = ((String)vers).split(" ")[0];
              break;
            }
          }
        } catch (Exception e) {
          // Ignore the exception
        }
      }
    } catch (Exception e) {
      // Ignore the exception
    }
    logger.trace("MQ client version is {}",version);
    return version;
  }
}
