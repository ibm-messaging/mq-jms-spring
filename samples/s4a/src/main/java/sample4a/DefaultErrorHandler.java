/*
 * Copyright 2024 IBM Corp. All rights reserved.
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

package sample4a;

import org.springframework.util.ErrorHandler;

// This gets hooked into the Listener when there's an exception generated
public class DefaultErrorHandler implements ErrorHandler {
  @Override
  public void handleError(Throwable t) {
    int i=0;
    System.out.printf("In Error Handler:\n");
    while (t != null) {
      System.out.printf("  [%d] %s\n",++i,t.getMessage());
      t = t.getCause();
    }
  }
}
