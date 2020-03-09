package dk.kb.pullStuff;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.Produce;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by dgj on 17-11-2016.
 * Stolen by slu on 11-01-2019
 */
public class RunPull {

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

	private static ConfigurableConstants consts = ConfigurableConstants.getInstance();
	private static Logger logger = configureLog4j();

	public static void main(String args[]) {

		String host = getHost();
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(host);
		Connection connection = null;
		Session session = null;
		MessageConsumer pullConsumer = null;
		MessageProducer producer = null;

		try {
			connection = connectionFactory.createConnection();
			connection.start();

			session                     = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			String queue                = consts.getConstants().getProperty("queue.name");
			Destination pullDestination = session.createQueue(queue);
			String push_queue           = consts.getConstants().getProperty("queue.load.name");
			Destination pushDestination = session.createQueue(push_queue);
			pullConsumer                = session.createConsumer(pullDestination);

			producer = session.createProducer(pushDestination);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

			while (true) {
				try {
					logger.info("Waiting for next message");
					Message message = pullConsumer.receive();

					handleMessage(producer, session, message);
				} catch (Exception e) {
					logger.error("Error connecting. Waiting 60 sek and try again.", e);

					Thread.sleep(60000);
				}
			}
		} catch (Exception e) {
			logger.fatal("Stopping execution ",e);
		} finally {
			try {
				pullConsumer.close();
				session.close();
				connection.close();
				producer.close();
			} catch (Exception e) {
				logger.fatal("error while shutting down ",e);
			}
		}
	}

	protected static synchronized void handleMessage(MessageProducer producer,
							 Session session,
							 Message message) throws JMSException {
	    
		String msg = "";
		if (message instanceof TextMessage) {
			TextMessage textMessage = (TextMessage) message;
			msg = textMessage.getText();
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

			performPull(producer, session, collection, repository, branch, target);
		} else {
			logger.error("Cannot handle non-text-message: " + message.toString());
			throw new IllegalStateException("Cannot handle non-text-message: " + message.toString());
		}
	}

	protected static void performPull(MessageProducer producer, Session session, String collection, String repository,
									  String branch, String target) {
		// Initialize our git gateway
	    
		logger.info("Setting repository: " + repository);
		GitClient git = new GitClient(repository);

		// workBranch is used for storing data prior to loading it into eXist and then Solr

		String use_branch;

		if(target.equals("production")) {
		    use_branch = "published.branch";
		} else {
		    // i.e., target.equals("staging")
		    use_branch = "previewed.branch";
		}


		
		String workBranch = consts.getConstants().getProperty( use_branch );

		// branch is the branch we want to mirror. Here we tell our git gateway the names of the two branches

		git.setBranch(branch);
		git.setPublishedBranch(workBranch);

		// OK, first we fetch. We'll basically get everything that has happened since last fetch.
		
		logger.info(git.gitFetch());

		// Now we checkout the desired branch and do a pull
		
		logger.info(git.gitCheckOut());
		logger.info(git.gitPull());

		// Now we turn to the workBranch. This is where we are doing the work.
		// There is no corresponding branch, remotely and hence no need to pull.
		//
		// This branch should reflect the status of the last import

		logger.info(git.gitCheckOutPublished());

		// Now we have two branches, out of which branch is in sync with the repository.
		// In spite of its name gitLog() is calculating a diff, not a log.
		// It is actually returning a map between an object (path and file name) and operation.
		// I.e., basically filenames connected to rest GET, PUT, DELETE etc.

		java.util.HashMap<String, String> op = git.gitLog();

		// Now we reset the workBranch to the state of the remote master.
		// I.e., we have exactly the same data as in branch master.
		
		logger.info(git.gitResetTo("origin/master")); 

		// There is something silly in this, at least for GV,
		// who don't update its development branch more than
		// four times a year and hardly ever it merge to master

		// Having done that we merge the remote desired branch into workBranch.
		//
		// By doing this in this odd order, we'll be able to take into account the fact that the
		// database might contain earlier changes. These will be overwritten below
		// when we queue up the operations in the op HashMap for execution in the next step in
		// the "conveyor belt"

		logger.info(git.gitMergeToPublished(branch));

		if (op.isEmpty()) {
			logger.info("OK nothing to do");
			sendMessage(producer, session, collection, repository, branch, target, "", "EMPTY");
		} else {
			logger.info("found operations");
			java.util.Iterator<String> keys = op.keySet().iterator();
			while (keys.hasNext()) {
				String key = keys.next();
				sendMessage(producer, session, collection, repository, branch, target, key, op.get(key));
			}

			// We've got all files, then we just send a message that it is time
			// to run a commit
			logger.info("Sending final message.");
			sendMessage(producer, session, collection, repository, branch, target, "", "COMMIT");
		}
	}

	protected static void sendMessage(MessageProducer producer, Session session, String collection, String repository,
							   String branch, String target, String key, String operation) {
		String message = collection + ";"
				+ repository + ";"
				+ branch + ";"
				+ target + ";"
				+ key + ";"
				+ operation;

		logger.info("sending message = " + message);
		try {
			TextMessage text_message = session.createTextMessage(message);
			producer.send(text_message);
			logger.debug("text message sent to jms queue");
		} catch (JMSException jme) {
			jme.printStackTrace();
			logger.error("could not text send message to queue");
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

	private static String getHost() {
		String host = System.getProperty("queue.uri");
		if (host == null) {
			return consts.getConstants().getProperty("queue.uri");
		}
		return host;
	}

	private static boolean wasItASuccess(InputStream response, Logger logger) {
		logger.info("I'm asked wheter I was successful. To be hones, I don't know really.");
		return true;
	}
}
