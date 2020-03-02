/*
 * Copyright © 2017, 2020 IBM Corp. All rights reserved.
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

// A simple example of using the Spring Boot JMSTemplate 
// 
// This program connects to a queue manager, creates a JMS Message Listener
// and then puts a message. The Listener receives the message and continues to 
// wait for more messages. The program will not end until you hit
// a break key like Ctrl-C.

package sample1;

import java.util.Date;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;

@SpringBootApplication
@EnableJms
public class Application {

  static final String qName = "DEV.QUEUE.1"; // A queue from the default MQ Developer container config

  public static void main(String[] args) {
   
    // Launch the application
    ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);

    // Create the JMS Template object to control connections and sessions.
    JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);

    // Send a single message with a timestamp
    String msg = "Hello from IBM MQ at " + new Date();

    // The default SimpleMessageConverter class will be called and turn a String
    // into a JMS TextMessage
    jmsTemplate.convertAndSend(qName, msg);

    status();

  }

  static void status() {
    System.out.println();
    System.out.println("========================================");
    System.out.println("MQ JMS Sample started. Message sent to queue: " + Application.qName);
    System.out.println("========================================");
  }
}
