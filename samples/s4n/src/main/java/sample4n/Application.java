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
 * This application demonstrates two separate aspects of Spring processing
 * 1 - configuring multiple queue manager connections in the same application
 * 2 - using XA (two-phase) transactions to reliably move messages between two queue managers
 *
 * For the XA coordination, we use Narayana. Other coordinators are available.
 *
 * The only way I could get the coordination to work was essentially to ignore all the
 * helper/proxy functions that Narayana provides and instead to explicitly create the
 * XA resources and manually enlist them in the transactions.
 *
 * This program starts by putting two messages to a queue. It then reads them back copying them
 * to the same-named queue on another queue manager. But the first copy is committed, while the second
 * copy is rolled back.  So we should end up with one message on each queue manager, and a BackoutCount of 1
 * on one message.
 */

package sample4n;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.transaction.xa.XAResource;

import org.messaginghub.pooled.jms.JmsPoolXAConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.mq.jakarta.jms.MQQueue;

import jakarta.jms.Destination;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSProducer;
import jakarta.jms.TextMessage;
import jakarta.jms.XAJMSContext;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;

@SpringBootApplication
@EnableJms
@EnableTransactionManagement
@Transactional
public class Application {

  static final String qName = "DEV.QUEUE.1"; // A queue from the default MQ Developer container config
  static final String[] operations = { "COMMIT", "ROLLBACK" }; // Make sure COMMIT is first
  static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
  static final Logger log = LoggerFactory.getLogger(Application.class);

  static ConfigurableApplicationContext context;

  static JmsPoolXAConnectionFactory xacf1;
  static JmsPoolXAConnectionFactory xacf2;

  static XAJMSContext ctx1;
  static XAJMSContext ctx2;

  static XAResource xares1;
  static XAResource xares2;

  static JMSConsumer consumer = null;
  static JMSProducer producer = null;

  static Destination q;

  public static void main(String[] args) {
    TextMessage msg;
    context = SpringApplication.run(Application.class, args);

    TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

    printStarted();

    try {
      // Get the XACFs for each of the queue manager configurations that we have defined.
      // These named beans pick up their own connection configuration from the resource file.
      // They don't really need to be pooled connections for this sample, but it might be
      // helpful to show how that can be done.
      xacf1 = context.getBean("qm1p", JmsPoolXAConnectionFactory.class);
      xacf2 = context.getBean("qm2p", JmsPoolXAConnectionFactory.class);

      // In order to get MQ JMS trace output, the CFs must be built before any other MQ work is done
      q = new MQQueue(qName);

      // Create the (XA) JMSContext objects and stash references to the XAResources
      ctx1 = xacf1.createXAContext();
      ctx2 = xacf2.createXAContext();

      xares1 = ctx1.getXAResource();
      xares2 = ctx2.getXAResource();

      // Put some initial messages on the queue for us to process. We use the XA
      // steps even though there's only one participant in this block
      try {
        tm.begin();
        tm.getTransaction().enlistResource(xares1);

        JMSProducer initialProducer = ctx1.createProducer();

        for (int i = 0; i < 2; i++) {
          String body = operations[i % 2] + ": test message sent at " + dateFormat.format(new Date());
          try {
            Thread.sleep(1000); // so we get a slightly different timestamp
          }
          catch (InterruptedException e) {
          }
          msg = ctx1.createTextMessage(body);
          initialProducer.send(q, msg);
        }

        tm.commit();

      }
      catch (Exception e) {
        trace("Initial production failed: " + e.getMessage());
        e.printStackTrace(System.err);
        exit(1);
      }

      // Now start the real work
      consumer = ctx1.createConsumer(q);
      producer = ctx2.createProducer();

      for (int i = 0; i < 2; i++) {
        try {
          trace("\nStarting transaction");
          tm.begin();

          // Need to explicitly name the resources that will be part of this transaction
          tm.getTransaction().enlistResource(xares1);
          tm.getTransaction().enlistResource(xares2);

          // Get a message
          msg = (TextMessage) consumer.receive(1000);
          if (msg != null) {
            // Copy it to the other queue manager. In a real application, this
            // is where we might modify or otherwise work with the message before continuing
            trace("Received message: " + msg.getText());
            producer.send(q, ctx2.createTextMessage(msg.getText()));
          }

          // Decide how to resolve the transaction
          String t = msg.getText();
          trace("Resolving transaction: " + t);

          if (msg != null && t.startsWith(operations[0])) {
            tm.commit();
          }
          else {
            // Note: If you are capturing debug/trace logs from the Transaction Manager, at any level up to and
            // including WARN, you might see exceptions caused by
            // apparent errors during the rollback. This is actually OK because the rollback is done with
            // two calls:
            // 1.  xa_end, with a flag TMFAIL indicating a rollback should happen
            //     MQ probably returns XA_RBROLLBACK which Narayana logs as an exception
            // 2.  xa_rollback to "complete" the operation but the RM has already forgotten the transaction
            //     MQ returns XAER_NOTA which Narayana also logs as an exception
            // It took a while to work through this, but I'm happy about the explanation. Perhaps Narayana could
            // take account of the state rather than always logging an exception, but at least there is
            // an explanation.
            tm.rollback();
          }
        }
        catch (IllegalStateException | SecurityException | RollbackException | HeuristicMixedException | HeuristicRollbackException
            | SystemException | NotSupportedException e) {
          trace("Exception in UT control: " + e.getMessage());
          e.printStackTrace();
          exit(1);
        }

      }

    }
    catch (Exception e) {
      e.printStackTrace();
      exit(1);
    }
    trace("Done.");
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

    // Wait a little while to give everything else a chance to tidy
    try {
      Thread.sleep(2000);
    }
    catch (InterruptedException e) {
    }

    try {
      if (ctx1 != null) {
        ctx1.close();
      }

      if (ctx2 != null) {
        ctx2.close();
      }
    }
    catch (Exception e) {

    }

    // Finally, this is how we force an exit from a Spring application. It might take a little while, and generate
    // exception stacks, but at least it does finish.
    System.exit(SpringApplication.exit(context, () -> rc));
  }

  private static void trace(String s) {
    U.trace(log,s);
  }

}
