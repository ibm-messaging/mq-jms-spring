package sample4a;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.mq.constants.MQConstants;

import jakarta.jms.JMSException;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;

// This class implements a JMS Listener, invoked when a message arrives on the queue.
// We then try to copy the message to a queue on the other queue manager, whose connection/session
// have already been setup.
//
// Depending on the content of the message, we will then either commit or rollback the move. If
// we see the same message a second time (to be expected after a rollback) we exit.

@Component
public class Listener {
  static final String ID = "S4A.Listener";
  
  @JmsListener(destination = Application.qName, containerFactory = "qm1JmsListenerContainerFactory", id = ID)
  @Transactional(rollbackFor = RuntimeException.class)
  public void receiveMessage(Session session, TextMessage msg) throws JMSException, RuntimeException {

    if (msg != null) {

      System.out.println("Received message: " + msg.getText());

      // If we've done a rollback for a message, then its delivery count will increase and we will see it again.
      // For this example, we are therefore going to exit.
      int dc = (msg.getIntProperty(MQConstants.MQ_JMSX_DELIVERY_COUNT));
      if (dc > 1) {
        System.out.println("Exiting because delivery count indicates repeated delivery. Count: " + dc);
        // Tell the application to cleanup and exit. This might be called several times until the main thread
        // notices and completes the shutdown steps.
        Application.ok = false;
      }

      // Copy the message to the other queue manager
      Application.producer.send(msg);

      // Determine what to do next
      if (msg.getText().startsWith(Application.operations[0])) {
        System.out.println("Executing: Commit");
      }
      else {
        System.out.println("Executing: Rollback");
        throw new RuntimeException("Doing XA rollback");
      }
    }
  }
}
