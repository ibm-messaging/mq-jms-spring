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

package sample5.test;

/*
 * This is a simple demonstration of how a testing setup can exploit
 * an IBM MQ Container
 */

import static java.lang.Thread.sleep;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;

import com.github.dockerjava.api.model.Bind;
import com.ibm.mq.testcontainers.MQContainer;

@SpringBootTest
@EnableAutoConfiguration
@Import(TestcontainersTest.Config.class)
//@TestPropertySource(locations = "classpath:application.properties")
public class TestcontainersTest {

  private static final String QNAME = "DEV.QUEUE.1";

  // If you need credentials to pull the MQ image from the entitled
  // repository, then one way of achieving that is to set these fields.
  // And uncomment the block.
  //
  // You might also have external ways of setting these keys via configuration
  // or doing a "docker login" before running the tests. See the Testcontainers
  // documentation for alternative ways to reach a private registry.
  /*
  static {
    // If using properties, then this code needs to be executed before image resolution
    System.setProperty("registry.username", "<your-username>");
    System.setProperty("registry.password", "<your-password-or-entitlement-key>");
  }
   */

  @Autowired
  JmsTemplate jmsTemplate;

  static MQContainer container;

  /**
   * A simple test that puts a message and then polls the listener that is reading it back.
   * We assume that the queue was empty - which it will be if the container starts empty.
   * @throws InterruptedException
   */
  @Test
  void test1() throws InterruptedException  {

    // Print the container startup logs
    // System.out.println("LOGS:" + container.getLogs());

    String testMessage = "Hello from JMS";
    System.out.printf("Running a test put/get\n");

    Listener.reset();
    jmsTemplate.convertAndSend(QNAME, testMessage);
    while (Listener.lastMessage == null) {
      sleep(500);
    }
    Assertions.assertEquals(Listener.lastMessage,testMessage);

    // Uncommenting this line makes the test run for a very long time, giving a chance
    // to log into the container to check it looks how you expect.
    // Thread.sleep(10000000);
  }

  /**
   * Another test that's doing the same thing, but showing how we can
   * have multiple tests in the same source file.
   * @throws InterruptedException
   */
  @Test
  void test2() throws InterruptedException {

    String testMessage = "Hello again from JMS";
    System.out.printf("Running another test put/get\n");

    Listener.reset();
    jmsTemplate.convertAndSend(QNAME, testMessage);
    while (Listener.lastMessage == null) {
      sleep(500);
    }
    Assertions.assertEquals(Listener.lastMessage,testMessage);
  }

  @TestComponent
  static class Listener {
    static String lastMessage = null;

    @JmsListener(destination = QNAME)
    public void listen(Message<String> message) {
      lastMessage = message.getPayload();
      System.out.printf("Received message from %s = %s\n",QNAME, lastMessage);
    }

    // Make sure the last read message is null before a test is run. So a second
    // test doesn't pick it up.
    public static void reset() {
      lastMessage = null;
    }
  }

  @TestConfiguration
  static class Config {
    @SuppressWarnings("resource")
    @Bean
    @ServiceConnection
    public MQContainer mqContainer() {
      System.out.printf("About to start MQContainer\n");

      /* This option uses the default public image (which has a :latest tag). If you need to use one of the licensed
       * images (possibly using the non-production license if it's used for testing) then you probably
       * need to set credentials at the top of this test program. Or change the image to point at a private repository.
       *
       * This test also shows how to mount a pre-created Docker volume to make startup faster after the qmgr
       * has been initially created.
       * Note that the web server is not started by default. The 'withWebServer()' enables that if you need it.
       *
       * *NOTE* The default container referenced in the `MQContainer` class points at the MQ Advanced for Developers image. That image
       * has license restrictions, constraining it to internal development and unit testing. See
       * https://www.ibm.com/support/customer/csol/terms/?id=L-HYGL-6STWD6&lc=en for full terms.
       */
      container = new MQContainer(MQContainer.DEFAULT_IMAGE)
          .acceptLicense()
          .withStartupMQSC("99-startup.mqsc")
          .withCreateContainerCmdModifier(cmd -> cmd.getHostConfig().withBinds(Bind.parse("varmqm:/var/mqm")));

      return container;
    }

    @Bean
    public Listener listener() {
      return new Listener();
    }
  }
}
