package dk.kb.text.connection;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import java.io.Closeable;

public class CloseableMessageConsumer implements MessageConsumer, Closeable {

    protected final MessageConsumer messageConsumer;

    public CloseableMessageConsumer(MessageConsumer messageConsumer) {
        this.messageConsumer = messageConsumer;
    }

    @Override
    public String getMessageSelector() throws JMSException {
        return messageConsumer.getMessageSelector();
    }

    @Override
    public MessageListener getMessageListener() throws JMSException {
        return messageConsumer.getMessageListener();
    }

    @Override
    public void setMessageListener(MessageListener messageListener) throws JMSException {
        messageConsumer.setMessageListener(messageListener);
    }

    @Override
    public Message receive() throws JMSException {
        return messageConsumer.receive();
    }

    @Override
    public Message receive(long l) throws JMSException {
        return messageConsumer.receive(l);
    }

    @Override
    public Message receiveNoWait() throws JMSException {
        return messageConsumer.receiveNoWait();
    }

    @Override
    public void close() {
        try {
            messageConsumer.close();
        } catch (JMSException e) {
            throw new IllegalStateException("Failed to close consumer.", e);
        }
    }
}
