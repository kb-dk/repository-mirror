package dk.kb.text.pullStuff;

import org.apache.log4j.Logger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

/**
 * The invocation message has the format:
 * "collection;repository;branch;target"
 */
public class Invocation {
    /** The logger.*/
    private static Logger logger = Logger.getLogger(Invocation.class);

    /** The collection.*/
    protected final String collection;
    /** The repository.*/
    protected final String repository;
    /** The branch.*/
    protected final String branch;
    /** The target.*/
    protected final String target;

    /**
     * Static method for extracting the invocation from the JMS message.
     * @param message The message with the content for the invocation.
     * @return The invocation.
     * @throws JMSException If it fails to extract the message.
     */
    public static Invocation extractFromMessage(Message message) throws JMSException {
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            String msg = textMessage.getText();
            logger.info("Received: " + msg);
            String reg = ";";
            String[] split = msg.split(reg);
            if(split.length < 4) {
                throw new IllegalArgumentException("Invalid message. Does not contain 4 parts. '" + msg + "'");
            }
            String collection  = msg.split(reg)[0]; // this is ADL or SKS etc
            String repository  = msg.split(reg)[1]; // the git URI
            String branch      = msg.split(reg)[2]; // the branch containing the desired data
            String target      = msg.split(reg)[3]; // where we are going to deposit those data

            return new Invocation(collection, repository, branch, target);
        } else {
            logger.error("Cannot handle non-text-message: " + message.toString());
            throw new IllegalStateException("Cannot handle non-text-message: " + message.toString());
        }
    }

    /**
     * Constructor.
     * @param collection The collection.
     * @param repository The repository.
     * @param branch The branch.
     * @param target The target.
     */
    public Invocation(String collection, String repository, String branch, String target) {
        this.collection = collection;
        this.repository = repository;
        this.branch = branch;
        this.target = target;
    }

    public String getCollection() {
        return collection;
    }

    public String getRepository() {
        return repository;
    }

    public String getBranch() {
        return branch;
    }

    public String getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return "Invocation: [" + collection + ";" +repository + ";" + branch + ";" + target + "]";
    }
}
