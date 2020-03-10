package dk.kb.text.pullStuff;

import org.jaccept.structure.ExtendedTestCase;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.Enumeration;
import java.util.UUID;

public class InvocationTest extends ExtendedTestCase {

    @Test(expectedExceptions = IllegalStateException.class)
    public void testFailureWrongMessageFormat() throws Exception {
        addDescription("Test when sending a wrong format for the message (e.g. not TextMessage)");
        Message msg = Mockito.mock(Message.class);

        Invocation.extractFromMessage(msg);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFailureBadMessageFormat() throws Exception {
        addDescription("Test when sending a wrong format for the message (e.g. not TextMessage)");

        TextMessage txtMessage = getTextMessage("NOT ENOUGH PARTS");

        Invocation.extractFromMessage(txtMessage);
    }

    @Test
    public void testStuff() throws Exception {
        addDescription("Test that a correctly formatted message is parsed successfully.");

        String collection = UUID.randomUUID().toString();
        String repository  = "public-adl-text-sources";
        String branch      = "origin/KB-test-branch"; //"master";
        String target      = UUID.randomUUID().toString();

        TextMessage txtMessage = getTextMessage(collection + ";" + repository + ";" + branch + ";" + target);

        Invocation invocation = Invocation.extractFromMessage(txtMessage);

        Assert.assertEquals(invocation.getCollection(), collection);
        Assert.assertEquals(invocation.getRepository(), repository);
        Assert.assertEquals(invocation.getBranch(), branch);
        Assert.assertEquals(invocation.getTarget(), target);
    }

    protected TextMessage getTextMessage(String message) {
        return new TextMessage() {
            String text = message;
            @Override
            public void setText(String s) throws JMSException {
                text = s;
            }

            @Override
            public String getText() throws JMSException {
                return text;
            }

            @Override
            public String getJMSMessageID() throws JMSException {
                return null;
            }

            @Override
            public void setJMSMessageID(String s) throws JMSException {

            }

            @Override
            public long getJMSTimestamp() throws JMSException {
                return 0;
            }

            @Override
            public void setJMSTimestamp(long l) throws JMSException {

            }

            @Override
            public byte[] getJMSCorrelationIDAsBytes() throws JMSException {
                return new byte[0];
            }

            @Override
            public void setJMSCorrelationIDAsBytes(byte[] bytes) throws JMSException {

            }

            @Override
            public void setJMSCorrelationID(String s) throws JMSException {

            }

            @Override
            public String getJMSCorrelationID() throws JMSException {
                return null;
            }

            @Override
            public Destination getJMSReplyTo() throws JMSException {
                return null;
            }

            @Override
            public void setJMSReplyTo(Destination destination) throws JMSException {

            }

            @Override
            public Destination getJMSDestination() throws JMSException {
                return null;
            }

            @Override
            public void setJMSDestination(Destination destination) throws JMSException {

            }

            @Override
            public int getJMSDeliveryMode() throws JMSException {
                return 0;
            }

            @Override
            public void setJMSDeliveryMode(int i) throws JMSException {

            }

            @Override
            public boolean getJMSRedelivered() throws JMSException {
                return false;
            }

            @Override
            public void setJMSRedelivered(boolean b) throws JMSException {

            }

            @Override
            public String getJMSType() throws JMSException {
                return null;
            }

            @Override
            public void setJMSType(String s) throws JMSException {

            }

            @Override
            public long getJMSExpiration() throws JMSException {
                return 0;
            }

            @Override
            public void setJMSExpiration(long l) throws JMSException {

            }

            @Override
            public int getJMSPriority() throws JMSException {
                return 0;
            }

            @Override
            public void setJMSPriority(int i) throws JMSException {

            }

            @Override
            public void clearProperties() throws JMSException {

            }

            @Override
            public boolean propertyExists(String s) throws JMSException {
                return false;
            }

            @Override
            public boolean getBooleanProperty(String s) throws JMSException {
                return false;
            }

            @Override
            public byte getByteProperty(String s) throws JMSException {
                return 0;
            }

            @Override
            public short getShortProperty(String s) throws JMSException {
                return 0;
            }

            @Override
            public int getIntProperty(String s) throws JMSException {
                return 0;
            }

            @Override
            public long getLongProperty(String s) throws JMSException {
                return 0;
            }

            @Override
            public float getFloatProperty(String s) throws JMSException {
                return 0;
            }

            @Override
            public double getDoubleProperty(String s) throws JMSException {
                return 0;
            }

            @Override
            public String getStringProperty(String s) throws JMSException {
                return null;
            }

            @Override
            public Object getObjectProperty(String s) throws JMSException {
                return null;
            }

            @Override
            public Enumeration getPropertyNames() throws JMSException {
                return null;
            }

            @Override
            public void setBooleanProperty(String s, boolean b) throws JMSException {

            }

            @Override
            public void setByteProperty(String s, byte b) throws JMSException {

            }

            @Override
            public void setShortProperty(String s, short i) throws JMSException {

            }

            @Override
            public void setIntProperty(String s, int i) throws JMSException {

            }

            @Override
            public void setLongProperty(String s, long l) throws JMSException {

            }

            @Override
            public void setFloatProperty(String s, float v) throws JMSException {

            }

            @Override
            public void setDoubleProperty(String s, double v) throws JMSException {

            }

            @Override
            public void setStringProperty(String s, String s1) throws JMSException {

            }

            @Override
            public void setObjectProperty(String s, Object o) throws JMSException {

            }

            @Override
            public void acknowledge() throws JMSException {

            }

            @Override
            public void clearBody() throws JMSException {

            }
        };
    }
}
