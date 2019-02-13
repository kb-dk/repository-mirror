package dk.kb.pullStuff;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.*;
import org.apache.http.HttpEntity;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;
import java.util.Properties;
import java.io.InputStream;

/**
 * Created by dgj on 17-11-2016.
 * Stolen by slu on 11-01-2019
 */
public class RunPull {

    private static ConfigurableConstants consts = ConfigurableConstants.getInstance();
    private static Logger logger = configureLog4j();

    public static void main(String args[]) {

        String host = System.getProperty("queue.uri");
        if (host == null) host = consts.getConstants().getProperty("queue.uri");
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(host);
        Connection connection = null;
        Session session = null;
        MessageConsumer pull_consumer = null;
	MessageProducer producer = null;

	try {
            connection = connectionFactory.createConnection();
            connection.start();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            String queue = System.getProperty("queue");
            if (queue == null ) queue = consts.getConstants().getProperty("queue.name");

            Destination pull_destination = session.createQueue(queue);

            String push_queue = System.getProperty("queue.load.name");
            if (push_queue == null ) push_queue = consts.getConstants().getProperty("queue.load.name");

            Destination push_destination = session.createQueue(push_queue);

	    pull_consumer = session.createConsumer(pull_destination);
	    
	    producer = session.createProducer(push_destination);
	    producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

	logger.info("kilroy 5");

	    while (true) {
		String msg = "";
		try {
		    logger.info("Waiting for next message");
		    Message message = pull_consumer.receive();
		    if (message instanceof TextMessage) {
			TextMessage textMessage = (TextMessage) message;
			msg = textMessage.getText();
			logger.info("Received: " + msg);
			String reg = ";";
			String collection  = msg.split(reg)[0];
			String repository  = msg.split(reg)[1];
			String branch      = msg.split(reg)[2];
			String target      = msg.split(reg)[3];

			logger.info("repository: " + repository);
			GitClient git = new GitClient(repository);

			git.setBranch(branch);

			logger.info(git.gitFetch());
			logger.info(git.gitCheckOut());
			logger.info(git.gitPull());
			java.util.HashMap<String,String> op = git.gitLog();
			if(op.isEmpty()) {
			    logger.info("OK nothing to do");
			} else {
			    logger.info("found operations");
			    java.util.Iterator<String> keys = op.keySet().iterator();
			    while(keys.hasNext()) {
				String key = keys.next();
				String theMessage = collection + ";" + repository + ";" + key + ";" + op.get(key);
				logger.info("about to send text msg = " + theMessage);
				try {
				    TextMessage text_message = session.createTextMessage(theMessage);
				    producer.send(text_message);
				    logger.debug("text message sent to jms queue");
				} catch (JMSException jme) {
				    jme.printStackTrace();
				    logger.error("could not text send message to queue");
				}
			    }
			}
		    } else {
			msg = message.toString();
		    }

		} catch (Exception e) {
		    logger.error("Error connecting  "+e);
		    logger.error("Waiting 60 sek and try again");

		    e.printStackTrace();
		    Thread.sleep(60000);
		}
	    }
	} catch (Exception e) {
	    logger.fatal("Stopping execution ",e);
	} finally {
	    try {
		pull_consumer.close();
		session.close();
		connection.close();
	    } catch (Exception e) {
		logger.fatal("error while shutting down ",e);
	    }
	} 
    }

    private static Logger configureLog4j() {

	String level = consts.getConstants().getProperty("queue.loglevel");
	if (System.getProperty("queue.loglevel") != null ) level = System.getProperty("queue.loglevel");

	String file = consts.getConstants().getProperty("queue.logfile");
	if (System.getProperty("queue.logfile") != null) file = System.getProperty("queue.logfile");

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
	logger.info("logging at level " + level + " in file " + file + "\n");
	return logger;
    }

    private static boolean wasItASuccess(InputStream response, Logger logger) {
	logger.info("I'm asked wheter I was successful. To be hones, I don't know really.");
	return true;
    }
}
