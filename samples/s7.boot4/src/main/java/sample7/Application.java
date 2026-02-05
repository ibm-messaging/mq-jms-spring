/*
 * Copyright © 2026 IBM Corp. All rights reserved.
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

// A simple example of using the JmsClient interface introduced in Spring Framework 7.
//
// This program connects to a queue manager, puts a message and then reads it back. It assumes
// the queue was empty to start with.


package sample7;

import java.util.Date;
import java.util.Optional;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsClient;
import org.springframework.messaging.Message;

@SpringBootApplication
@EnableJms
public class Application {

  static final String qName = "DEV.QUEUE.1"; // A queue from the default MQ Developer container config

  public static void main(String[] args) {

    // Launch the application
    ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
    status("MQ JMS Sample using JmsClient class started.");

    // Create the JmsClient object to control connections and sessions.
    JmsClient client = context.getBean(JmsClient.class);

    // Send a single message with a timestamp
    String outMsg = "Hello from IBM MQ at " + new Date();

    // This fluent approach combines several operations in one line
    client
    .destination(qName)
    .send(outMsg);
    status("Message sent to queue: " + Application.qName);

    // And now try to read the message back
    Optional<Message<?>> inMsg = client
        .destination(qName)
        .withReceiveTimeout(1000)
        .receive();

    String s = String.format("Message received: %s\n", (inMsg != null)?inMsg.toString():"null");
    status(s);

    System.out.println("Done.");

  }

  static void status(String s) {
    System.out.println();
    System.out.println("========================================");
    System.out.println(s);
    System.out.println("========================================");
  }
}
