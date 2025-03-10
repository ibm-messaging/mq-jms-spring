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


/*
 * Any simple common routines can go in here
 */
package sample4n;

import org.slf4j.Logger;

public class U {
  
  // Log to both stdout and, if enabled, the logging stream 
  static void trace(Logger logger, String s) {
    logger.trace(s);
    System.out.println(s);
  }
}
