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

	    while (true) {
		String msg = "";
		try {

		    /*
		      We have different branches, locally and
		      remotely. Two are the ones we work with. One is
		      in our remote git text repository and one in the
		      remote repository. Their names are in the String variables

		      publishedBranch

		      and 

		      branch

		      respectively.

		      The former is in the configuration file the
		      latter comes in a message from ActiveMQ
		      
		    */

		    logger.info("Waiting for next message");
		    Message message = pull_consumer.receive();
		    if (message instanceof TextMessage) {
			TextMessage textMessage = (TextMessage) message;
			msg = textMessage.getText();
			logger.info("Received: " + msg);
			String reg = ";";
			String collection  = msg.split(reg)[0]; // this is ADL or SKS etc
			String repository  = msg.split(reg)[1]; // the git URI
			String branch      = msg.split(reg)[2]; // the branch containing the desired data
			String target      = msg.split(reg)[3]; // where we are going to deposit those data

			// Initialize our git gateway
			logger.info("Setting repository: " + repository);
			GitClient git = new GitClient(repository);

			// This branch is used for storing data prior
			// to loading it into eXist and Solr

			String publishedBranch = consts.getConstants().getProperty("published.branch");

			// Tell our git gateway the names of the two branches
			git.setBranch(branch);
			git.setPublishedBranch(publishedBranch);
			
			// OK, first we fetch. We'll basically get
			// everything that has happened since last
			// fetch.
			logger.info(git.gitFetch());

			// branch, checkout the desired branch and do
			// a pull
			
			logger.info(git.gitCheckOut());
			logger.info(git.gitPull());

			// publishedBranch. This is where we are doing
			// the work. The isn't any corresponding
			// branch remotely so there is no need to
			// pull.
			//
			// This branch should reflect the status of
			// the last import

			logger.info(git.gitCheckOutPublished());

			// Now we have the two branches. In spite of
			// the name gitLog() this is calculating a
			// diff, not a log. This is actually returning a 
			// map between an object and operation.

			java.util.HashMap<String,String> op = git.gitLog();

			// This resets the publishedBranch to the
			// state of the remote master. I.e., we have
			// exactly the same data as in branch master.
			logger.info(git.gitResetTo("origin/master"));

			// Now we merge the remote desired branch with
			// publishedBranch
			//
			// by doing this in this odd order, we'll be
			// able to take into account the fact that the
			// database might contain earlier
			// changes. These will be overwritten below
			// when we queue up the operations in the op
			// HashMap for execution in the next step in
			// the "conveyor belt"

			logger.info(git.gitMergeToPublished(branch));

			if(op.isEmpty()) {
			    logger.info("OK nothing to do");
			} else {
			    logger.info("found operations");
			    java.util.Iterator<String> keys = op.keySet().iterator();
			    while(keys.hasNext()) {
				String key = keys.next();
				String theMessage 
				    = collection + ";" 
				    + repository + ";" 
				    + branch     + ";"
				    + target     + ";"
				    + key        + ";" 
				    + op.get(key);
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

			    // We've got all files, then we just send a message that it is time
			    // to run a commit
			    String finalMessage 
				= collection + ";" 
				+ repository + ";" 
				+ branch     + ";"
				+ target     + ";"
				+ ""         + ";" 
				+ "COMMIT";
				logger.info("about to send final text msg = " + finalMessage);
			    try {
				TextMessage text_message = session.createTextMessage(finalMessage);
				producer.send(text_message);
				logger.debug("text message sent to jms queue");
			    } catch (JMSException jme) {
				jme.printStackTrace();
				logger.error("could not text send message to queue");
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
