package dk.kb.text.message;

import dk.kb.text.connection.CloseableMessageProducer;
import org.apache.log4j.Logger;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.io.Closeable;

/**
 * Response mediator.
 * For sending confirmations, responses, and updates back to the web-page.
 */
public class ResponseMediator implements Closeable {
    /** The logger.*/
    protected static final Logger logger = Logger.getLogger(ResponseMediator.class);

    /** The message producer to produce and send the response messages.*/
    protected final CloseableMessageProducer responseProducer;
    /** The session.*/
    protected final Session session;

    /**
     * Constructor.
     * @param session
     * @param invocation
     * @throws JMSException
     */
    public ResponseMediator(Session session, Invocation invocation) throws JMSException {
        this.session = session;
        String feedbackQueue = invocation.getCollection() + "_feedback";
        Destination feedbackDestination = session.createQueue(feedbackQueue);
        responseProducer = new CloseableMessageProducer(session.createProducer(feedbackDestination));
        responseProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        logger.debug("Instantiated response mediator to send to the queue: " + feedbackQueue);
    }

    /**
     * Sends the message.
     * @param message The content of the message.
     * @throws JMSException If it fails.
     */
    public void sendMessage(String message) throws JMSException {
        logger.debug("Sending message: " + message);
        TextMessage text_message = session.createTextMessage(message);
        responseProducer.send(text_message);
    }

    @Override
    public void close() {
        responseProducer.close();
    }
}
