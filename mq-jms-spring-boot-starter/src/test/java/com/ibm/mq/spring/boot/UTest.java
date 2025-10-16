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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes= {U.class})
public class UTest {

  @Test
  public void testIsNullOrEmpty() {
    Assertions.assertFalse(U.isNullOrEmpty("Hello"));
    Assertions.assertTrue(U.isNullOrEmpty(""));
    Assertions.assertTrue(U.isNullOrEmpty(null));
  }

  @Test
  public void testIsNotNullOrEmpty() {
    Assertions.assertTrue(U.isNotNullOrEmpty("Hello"));
    Assertions.assertFalse(U.isNotNullOrEmpty(""));
    Assertions.assertFalse(U.isNotNullOrEmpty(null));
  }
}
