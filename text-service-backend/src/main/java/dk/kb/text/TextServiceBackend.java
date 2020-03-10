package dk.kb.text;

import dk.kb.text.connection.CloseableConnection;
import dk.kb.text.connection.CloseableMessageConsumer;
import dk.kb.text.connection.CloseableSession;
import dk.kb.text.dbStuff.ApiClient;
import dk.kb.text.dbStuff.RunLoad;
import dk.kb.text.message.ResponseMediator;
import dk.kb.text.pullStuff.Invocation;
import dk.kb.text.pullStuff.RunPull;
import dk.kb.text.utils.ConfUtils;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.Map;
import java.util.Properties;

public class TextServiceBackend {

    private static Logger logger = configureLog4j();

    protected static RunPull runPull;

    public static void main(String args[]) {
        String host = ConfUtils.getHost();
        runPull = new RunPull();

        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(host);
        String consumeQueue = ConfigurableConstants.getInstance().getConstants().getProperty("queue.name");

        // Use try-with-resource to auto-close connections
        try (CloseableConnection connection = new CloseableConnection(connectionFactory.createConnection());
             CloseableSession session = new CloseableSession(connection.createSession(
                     false, Session.AUTO_ACKNOWLEDGE));){
            Destination pullDestination = session.createQueue(consumeQueue);

            try (CloseableMessageConsumer pullConsumer = new CloseableMessageConsumer(
                    session.createConsumer(pullDestination))) {

                while (true) {
                    try {
                        logger.info("Waiting for next message");
                        Message message = pullConsumer.receive();
                        Invocation invocation = Invocation.extractFromMessage(message);

                        handleMessage(session, invocation);
                    } catch (Exception e) {
                        logger.error("Error connecting. Waiting 60 sek and try again.", e);

                        Thread.sleep(60000);
                    }
                }
            }
        } catch (Exception e) {
            logger.fatal("Stopping execution ", e);
        }
    }

    protected static synchronized void handleMessage(Session session, Invocation invocation) throws JMSException {
        try (ResponseMediator mediator = new ResponseMediator(session, invocation)) {
            mediator.sendMessage("Initializing switch for '" + invocation.getTarget() + "' towards branch: '"
                    + invocation.getBranch() + "' for repository: '" + invocation.getRepository() + "'");
            try {
                Map<String, String> operations = runPull.performPull(invocation);

                mediator.sendMessage("Git repository '" + invocation.getRepository() + "' switched to '"
                        + invocation.getBranch() + "' for '" + invocation.getTarget() + "'. Found '"
                        + operations.size() + "' file changes to handle.");

                RunLoad runLoad = new RunLoad(new ApiClient(), invocation, mediator);

                // load the data
                for (Map.Entry<String, String> operation : operations.entrySet()) {
                    try {
                        runLoad.handleOperation(operation.getKey(), operation.getValue());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                runLoad.commit();
            } catch (Exception e) {
                logger.error("Failed to handle message '" + invocation + "'", e);
                mediator.sendMessage("Failed to handle message '" + invocation + "'. Got error message: "
                        + e.getMessage());
            }
        }
    }

    private static Logger configureLog4j() {

        String level = ConfigurableConstants.getInstance().getConstants().getProperty("queue.loglevel");
        if (System.getProperty("queue.loglevel") != null ) {
            level = System.getProperty("queue.loglevel");
        }

        String file = ConfigurableConstants.getInstance().getConstants().getProperty("queue.logfile");
        if (System.getProperty("queue.logfile") != null) {
            file = System.getProperty("queue.logfile");
        }

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
        Logger logger = Logger.getLogger(TextServiceBackend.class);
        logger.info("logging at level " + level + " in file " + file + "\n");
        return logger;
    }
}
