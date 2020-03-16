package dk.kb.text.connection;

import javax.jms.Connection;
import javax.jms.ConnectionConsumer;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.Topic;
import java.io.Closeable;

public class CloseableConnection implements Connection, Closeable {

    protected final Connection connection;

    public CloseableConnection(Connection connection) throws JMSException {
        this.connection = connection;
        start();
    }

    @Override
    public Session createSession(boolean b, int i) throws JMSException {
        return connection.createSession(b, i);
    }

    @Override
    public String getClientID() throws JMSException {
        return connection.getClientID();
    }

    @Override
    public void setClientID(String s) throws JMSException {
        connection.setClientID(s);
    }

    @Override
    public ConnectionMetaData getMetaData() throws JMSException {
        return connection.getMetaData();
    }

    @Override
    public ExceptionListener getExceptionListener() throws JMSException {
        return connection.getExceptionListener();
    }

    @Override
    public void setExceptionListener(ExceptionListener exceptionListener) throws JMSException {
        connection.setExceptionListener(exceptionListener);
    }

    @Override
    public void start() throws JMSException {
        connection.start();
    }

    @Override
    public void stop() throws JMSException {
        connection.stop();
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (JMSException e) {
            throw new IllegalStateException("Failed to close connection", e);
        }
    }

    @Override
    public ConnectionConsumer createConnectionConsumer(Destination destination, String s, ServerSessionPool serverSessionPool, int i) throws JMSException {
        return connection.createConnectionConsumer(destination, s, serverSessionPool, i);
    }

    @Override
    public ConnectionConsumer createDurableConnectionConsumer(Topic topic, String s, String s1, ServerSessionPool serverSessionPool, int i) throws JMSException {
        return connection.createDurableConnectionConsumer(topic, s, s1, serverSessionPool, i);
    }
}
