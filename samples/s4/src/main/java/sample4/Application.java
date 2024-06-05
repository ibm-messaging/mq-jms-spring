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
 * This program starts by putting two messages to a queue. It then reads them back copying them
 * to the same-named queue on another queue manager. But the first copy is committed, while the second
 * copy is rolledback. So we should end up with one message on each queue manager, and a BackoutCount of 1
 * on one message.
 */

package sample4;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.jms.AtomikosConnectionFactoryBean;
import com.ibm.mq.jakarta.jms.MQQueue;

import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import jakarta.jms.XAConnectionFactory;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;

@SpringBootApplication
@EnableJms
@EnableTransactionManagement
@Transactional
public class Application {

  static final String qName = "DEV.QUEUE.1"; // A queue from the default MQ Developer container config
  static final String[] operations = { "COMMIT", "ROLLBACK" }; // Make sure COMMIT is first
  static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

  public static void main(String[] args) throws JMSException {

    TextMessage msg;
    UserTransactionImp utx;

    // Launch the application
    ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);

    printStarted();

    // Get the XACFs for each of the queue manager configurations that we have defined.
    // These named beans pick up their own connection configuration from the resource file.
    XAConnectionFactory xacf1 = context.getBean("qm1", XAConnectionFactory.class);
    XAConnectionFactory xacf2 = context.getBean("qm2", XAConnectionFactory.class);

    // And make the CFs available to Atomikos
    AtomikosConnectionFactoryBean atcf1 = new AtomikosConnectionFactoryBean();
    atcf1.setUniqueResourceName("QM1");
    atcf1.setXaConnectionFactory(xacf1);

    AtomikosConnectionFactoryBean atcf2 = new AtomikosConnectionFactoryBean();
    atcf2.setUniqueResourceName("QM2");
    atcf2.setXaConnectionFactory(xacf2);

    // Create a context where transactions are managed
    utx = new UserTransactionImp();

    // From here on, it's essentially standard JMS operations. We're not going to use
    // the Spring simplified classes such as JmsTemplate.
    Connection conn1 = atcf1.createConnection();
    Connection conn2 = atcf2.createConnection();

    Session sess1 = conn1.createSession(Session.SESSION_TRANSACTED);
    Session sess2 = conn2.createSession(Session.SESSION_TRANSACTED);

    MessageConsumer consumer = sess1.createConsumer(new MQQueue(qName));
    conn1.start(); // Need to "start" in order to receive messages

    MessageProducer producer = sess2.createProducer(new MQQueue(qName));

    // Put some initial messages on the queue for us to process. We use the XA
    // steps even though there's only one participant in this block
    MessageProducer initialProducer = sess1.createProducer(new MQQueue(qName));
    try {
      utx.begin();
      for (int i = 0; i < 2; i++) {
        String body = operations[i % 2] + ": test message sent at " + dateFormat.format(new Date());
        try {
          Thread.sleep(1000); // so we get a slightly different timestamp
        }
        catch (InterruptedException e) {
        }
        msg = sess1.createTextMessage(body);
        initialProducer.send(msg);
      }

      utx.commit();
    }
    catch (Exception e) {
      System.out.println("Initial production failed: " + e.getMessage());
      System.exit(1);
    }

    // Now start the real work
    for (int i = 0; i < 2; i++) {

      try {
        utx.begin();
      }
      catch (IllegalStateException | SecurityException | SystemException | NotSupportedException e) {
        System.out.println("Exception in UT begin" + e.getMessage());
        System.exit(1);
      }

      // Get a message
      msg = (TextMessage) consumer.receive(1000);
      if (msg != null) {
        // Copy it to the other queue manager. In a real application, this
        // is where we might modify or otherwise work with the message before continuing
        System.out.println("Received message: " + msg.getText());
        producer.send(msg);
      }

      // Decide how to resolve the transaction
      try {
        if (msg != null && msg.getText().startsWith(operations[0])) {
          utx.commit();
        }
        else {
          utx.rollback();
        }
      }
      catch (IllegalStateException | SecurityException | RollbackException | HeuristicMixedException | HeuristicRollbackException
          | SystemException e) {
        System.out.println("Exception in UT commit/rollback: " + e.getMessage());
        System.exit(1);
      }
    }

    System.out.println("Done.");
  }

  static void printStarted() {
    System.out.println();
    System.out.println("========================================");
    System.out.println("MQ JMS XA Transaction Sample started.");
    System.out.println("========================================");
  }
}
