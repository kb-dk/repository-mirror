package dk.kb.pullStuff;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.*;
import org.apache.http.HttpEntity;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Properties;
import java.io.InputStream;

/**
 * Created by dgj on 17-11-2016.
 * Stolen by slu on 11-01-2019
 */
public class RunPull {

    private static ConfigurableConstants consts = ConfigurableConstants.getInstance();

    public static void main(String args[]) {

        Logger logger = configureLog4j();


        String host = System.getProperty("mqhost");
        if (host == null) host = consts.getConstants().getProperty("cop2.solrizr.queue.host");
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(host);
        Connection connection = null;
        Session session = null;
        MessageConsumer consumer = null;

        try {
            connection = connectionFactory.createConnection();
            connection.start();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            String queue = System.getProperty("queue");
            if (queue == null ) queue = consts.getConstants().getProperty("cop2.solrizr.queue.update");
            Destination destination = session.createQueue(queue);

            consumer = session.createConsumer(destination);

            while (true) {
                String id = null;
                try {
                    logger.info("Waiting for next message");
                    Message message = consumer.receive();
                    if (message instanceof TextMessage) {
                        TextMessage textMessage = (TextMessage) message;
                        id = textMessage.getText();
                    } else {
                        id = message.toString();
                    }
                    logger.info("Received: " + id);
                    String solrize_url = consts.getConstants().getProperty("cop2_backend.internal.baseurl") + "/solrizr" + id;
                    String solr_url = System.getProperty("solr_baseurl");
                    if  (null != solr_url) solrize_url += "?solr_baseurl="+solr_url;
                    logger.info("Solrizr url " + solrize_url);
                    HttpClient client = new HttpClient();
                    GetMethod get = new GetMethod(solrize_url);
                    client.executeMethod(get);
                    int statusCode = get.getStatusCode();
                    if (statusCode == 200) {
                        if (wasItASuccess(get.getResponseBodyAsStream(), logger))
                            logger.info("Solrize " + id + " SUCCESS");
                        else {
                            logger.info("Solrize " + id + " FAILED");
                            sendToFailedQueue(id,"",logger);
                        }
                    }
                    else {
                        logger.info("Solrize " + id + " FAILED");
                        sendToFailedQueue(id,"statuscode is "+statusCode,logger);
                    }
                } catch (Exception e) {
                    logger.error("Error connecting to Solrizr "+e);
                    logger.error("Waiting 60 sek and try again");
                    sendToFailedQueue(id,"Error connecting to Solrizr "+e.getMessage(),logger);
                    e.printStackTrace();
                    Thread.sleep(60000);
                }
            }
        } catch (Exception e) {
            logger.fatal("Stopping execution ",e);
        } finally {
            try {
                consumer.close();
                session.close();
                connection.close();
            } catch (Exception e) {
                logger.fatal("error while shutting donw ",e);
            }
        }
    }

    private static Logger configureLog4j() {
        String level = "info";
        if (System.getProperty("loglevel") != null ) level = System.getProperty("loglevel");
        String file = "runsolrizr.log";
        if (System.getProperty("logfile") != null) file = System.getProperty("logfile");
        Properties props = new Properties();
        props.put("log4j.rootLogger", level+", FILE");
        props.put("log4j.appender.FILE", "org.apache.log4j.DailyRollingFileAppender");
        props.put("log4j.appender.FILE.File",file);
        props.put("log4j.appender.FILE.ImmediateFlush","true");
        props.put("log4j.appender.FILE.Threshold",level);
        props.put("log4j.appender.FILE.Append","true");
        props.put("log4j.appender.FILE.layout", "org.apache.log4j.PatternLayout");
        props.put("log4j.appender.FILE.layout.conversionPattern","[%d{yyyy-MM-dd HH.mm:ss}] %-5p %C{1} %M: %m %n");
        PropertyConfigurator.configure(props);
        Logger logger = Logger.getLogger(RunPull.class);
        return logger;
    }

    private static void sendToFailedQueue(String id, String msg, Logger logger) {
        JMSstuff producer = null;
        try {
            producer = new JMSstuff(
                    consts.getConstants().getProperty("cop2.solrizr.queue.host"),
                    consts.getConstants().getProperty("cop2.solrizr.queue.update")+".failed");
            producer.sendMessage(id + "|" + msg);
        } catch (JMSException e) {
            logger.error("Error sending fail message ",e);
        } finally {
            if (producer != null) {
                producer.shutDownPRoducer();
            }
        }
    }

    private static boolean wasItASuccess(InputStream response, Logger logger) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(response);
            XPath xPath =  XPathFactory.newInstance().newXPath();
            NodeList nodes = (NodeList) xPath.compile("/response/lst[@name=\"responseHeader\"]/int[@name=\"status\"]")
                    .evaluate(doc, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                Node n = nodes.item(i);
                int status = Integer.parseInt(n.getTextContent());
                if (status == 0) 
		    return true;
		else
		    logger.error("solr error response status "+status);
            }
        } catch (ParserConfigurationException e) {
            logger.error("Error parsing solr response ",e);
        } catch (SAXException e) {
            logger.error("Error parsing solr response ",e);
        } catch (IOException e) {
            logger.error("Error parsing solr response ",e);
        } catch (XPathExpressionException e) {
            logger.error("Error parsing solr response ",e);
        }
        return false;
    }
}
