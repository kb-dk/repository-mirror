package dk.kb.pullStuff;

import javax.jms.*;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;


public class JMSstuff {


    private static Logger logger = Logger.getLogger(JMSstuff.class);

    private Session session;
    private MessageProducer producer;
    private Connection connection;

    /**
     * Constructor setting up a connection to a server and specify a queue i.e.
     *  JMSProducer cannon = new JMSProducer("tcp://disdev-01:61616","kb.cop.test");
     * @param url tcp://disdev-01:61616
     * @param queue kb.cop.test
     * @throws JMSException
     */
    public JMSstuff(String url, String queue) throws JMSException {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
        connection = connectionFactory.createConnection();

        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Destination destination = session.createQueue(queue);

        producer = session.createProducer(destination);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        logger.info("Created JMS producer - fire at will");

    }

    /**
     * method for sending a message
     *
     * @param theMessage
     * @return true/false
     */
    public boolean sendMessage(String theMessage) {
        try {
            TextMessage message = session.createTextMessage(theMessage);
            producer.send(message);
            logger.debug("sent text message to jms queue " + theMessage);
            return true;
        } catch (JMSException jme) {
            jme.printStackTrace();
            logger.error("could not text send message to queue" + theMessage);
            return false;
        }

    }

    /**
     * method for sending a xmlNode to the queue
     *
     * @param xmlNode a xmlNode i.e. a mods record
     * @return true/false
     */
    public boolean sendMessage(Node xmlNode) {
        try {
            TextMessage message = session.createTextMessage();
            message.setObjectProperty("xmlnode",xmlNode);
            producer.send(message);
            logger.debug("sent xmlNode to jms queue " );

            return true;
        } catch (JMSException jme) {
            jme.printStackTrace();
            logger.error("could not send xmlNode to pdf queue" + xmlNode);
            return false;
        }

    }

    /**
     * method for shutting down the producer
     */
    public void shutDownPRoducer(){
        try {
            connection.close();
            logger.info("Closed JMS producer");
        } catch (JMSException e) {
            e.printStackTrace();
            logger.error("could not close jms producer");

        }
    }


    public static void main(String[] args) throws JMSException {

        JMSstuff cannon = new JMSstuff("tcp://10.6.1.140:61616","kb.cop.test");
        cannon.sendMessage("Awesome");
        cannon.sendMessage("Awesome");
        cannon.sendMessage("Awesome");
        cannon.shutDownPRoducer();
        System.exit(0);

    }

}