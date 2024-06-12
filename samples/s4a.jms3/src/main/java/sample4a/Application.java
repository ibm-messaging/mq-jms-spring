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

/*
 * This application demonstrates two separate aspects of Spring processing
 * 1 - configuring multiple queue manager connections in the same application
 * 2 - using XA (two-phase) transactions to reliably move messages between two queue managers
 * 
 * For the XA coordination, we use Atomikos. Other coordinators are available.
 * 
 * At the start of the program, there should be two messages on the input queue (see the
 * startup script). This program sets up a listener. It then get messages copying them
 * to the same-named queue on another queue manager. But the first copy is committed, while the second
 * copy is rolled back. So we should end up with one message on each queue manager, and a non-zero BackoutCount
 * on one of them.
 */

package sample4a;

import java.text.SimpleDateFormat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import com.atomikos.jms.AtomikosConnectionFactoryBean;
import com.ibm.mq.jakarta.jms.MQQueue;

import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;

@SpringBootApplication
@EnableJms
@EnableTransactionManagement
@Transactional
public class Application {

  static final String qName = "DEV.QUEUE.1"; // A queue from the default MQ Developer container config
  static final String[] operations = { "COMMIT", "ROLLBACK" }; // Make sure COMMIT is first

  static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

  static ConfigurableApplicationContext context;
  static MessageProducer producer;
  static Connection conn2;
  static Session sess2;

  static boolean ok = true;

  public static void main(String[] args) throws JMSException {

    // Launch the application
    context = SpringApplication.run(Application.class, args);

    printStarted();

    // Get the connection to QM2 created and ready to be used inside the Listener. This is an XA-aware
    // connection, managed by Atomikos
    AtomikosConnectionFactoryBean atcf2 = context.getBean("qm2", AtomikosConnectionFactoryBean.class);
    conn2 = atcf2.createConnection();
    sess2 = conn2.createSession(Session.SESSION_TRANSACTED);
    producer = sess2.createProducer(new MQQueue(qName));

    // And we're done in the main part of the program. The real work gets done in the Listener. Just look for
    // an error, so we know when to clean up and exit.
    while (ok) {
      try {
        Thread.sleep(1000);
      }
      catch (InterruptedException e) {
      }
    }
    exit(0);
  }

  static void printStarted() {
    System.out.println();
    System.out.println("========================================");
    System.out.println("MQ JMS XA Transaction Sample started.");
    System.out.println("========================================");
  }

  // Cleanup various resources, including stopping the listener
  static void exit(int rc) {
    JmsListenerEndpointRegistry registry = context.getBean(JmsListenerEndpointRegistry.class);
    if (registry != null) {
      MessageListenerContainer mlc = registry.getListenerContainer(Listener.ID); // The "id" tag on the definition
      if (mlc != null && mlc.isRunning()) {
        System.out.println("Stopping listener");
        mlc.stop();
      }
    }

    try {
      conn2.close();
    }
    catch (JMSException e) {
    }

    // Wait a little while to give everything else a chance to tidy
    try {
      Thread.sleep(2000);
    }
    catch (InterruptedException e) {
    }

    // Finally, this is how we force an exit from a Spring application. It might take a little while, and generate
    // exception stacks, but at least it does finish.
    System.exit(SpringApplication.exit(context, () -> rc));
  }
}
