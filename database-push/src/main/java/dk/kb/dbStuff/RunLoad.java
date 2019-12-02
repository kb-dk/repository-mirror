package dk.kb.dbStuff;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import com.damnhandy.uri.template.UriTemplate;
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

/**
 * Created by dgj on 17-11-2016.
 * Stolen by slu on 11-01-2019
 */
public class RunLoad {

	private static ConfigurableConstants consts = ConfigurableConstants.getInstance();
	static Logger logger = configureLog4j();

	public static void main(String args[]) {

		String host = consts.getConstants().getProperty("queue.uri");
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(host);
		Connection connection = null;
		Session session = null;
		MessageConsumer consumer = null;

		MessageProducer feedback_producer = null;

		ApiClient htclient = new ApiClient();

		try {
			connection = connectionFactory.createConnection();
			connection.start();

			session      = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			String queue = consts.getConstants().getProperty("queue.load.name");
			Destination destination = session.createQueue(queue);

			consumer = session.createConsumer(destination);

			while (true) {
				String msg = null;
				try {
					logger.info("Waiting for next message");
					Message message = consumer.receive();
					if (message instanceof TextMessage) {
						TextMessage textMessage = (TextMessage) message;
						msg = textMessage.getText();
					} else {
						msg = message.toString();
					}

					handleMessage(htclient, session, msg);
				} catch (Exception e) {
					logger.error("Error connecting " + e);
					logger.error("Waiting 6 seconds and try again");

					e.printStackTrace();
					Thread.sleep(6000);
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

	protected static synchronized void handleMessage(ApiClient htclient, Session session, String msg)
			throws JMSException {
		logger.info("Received: " + msg);

		String reg = ";";
		String[] arr = msg.split(reg);

		if(arr.length < 6) {
			throw new IllegalArgumentException("Cannot comprehend message. Needs at least 6 parts (separated by '"
					+ reg + "'): " + msg);
		}
		String collection = arr[0];
		String repository = arr[1];
		String branch     = arr[2];
		String target     = arr[3];
		String document   = arr[4];
		String op         = arr[5];

		String credField = target + ".credentials";

		logger.info("credField: " + credField);

		String user   = consts.getConstants().getProperty(credField).split(reg)[0];
		String passwd = consts.getConstants().getProperty(credField).split(reg)[1];

		logger.info("creds user: " + user + " creds passwd: " + passwd);

		FilePathHack fileFixer = new FilePathHack();
		fileFixer.setTarget(target);
		fileFixer.setCollection(collection);
		fileFixer.setDocument(document);

		if(fileFixer.validDocPath()) {

			String URI = fileFixer.getServicePath();

			String file = consts.getConstants().getProperty("data.home") + repository + "/" + document;
			String database_host = consts.getConstants().getProperty(target).split(":")[0];
			String port_number   = consts.getConstants().getProperty(target).split(":")[1];
			int    port = Integer.parseInt(port_number);
			String realm = "exist";
			htclient.setLogin(user,passwd,database_host,port,realm);

			logger.info("URI  " + URI);
			logger.info("File " + file);

			String index_name   =  consts.getConstants().getProperty(target + "." + "index_name");
			String index_server =  consts.getConstants().getProperty(target + "." + "index_hostport");
			String solr_index_uri = UriTemplate.fromTemplate(consts.getConstants().getProperty("indexing.template"))
					.set("index_name", index_name)
					.set("solr_hostport", index_server)
					.expand();

			if(op.matches(".*PUT.*")) {
				logger.info("operation = " + op);

				String putRes = htclient.restPut(file, URI);
				logger.info("HTTP PUT result: " + putRes);

				String solrizrURI = UriTemplate.fromTemplate(consts.getConstants().getProperty("solrizr.template"))
						.set("exist_hostport", consts.getConstants().getProperty(target) )
						.set("op", "solrize")
						.set("doc", document)
						.set("c", collection)
						.expand();

				sendMessage(session, collection, "Putting document " + document + " to " + URI + "\n");

				logger.info("solrizr: " + solrizrURI);
				String solrizedRes = htclient.restGet(solrizrURI);

				if(solrizedRes == null) {
					logger.info("solrizr: got null");
					sendMessage(session, collection, "Failed to index the document '" + document + "\n");
				} else {
					logger.info("solrizr: status 200 OK");
					String volume_id = htclient.getHttpHeader("X-Volume-ID");

					// This is definately overkill for ADL, but
					// necessary for, let's say Grundtvig
					if(volume_id.length() > 0) {
						String solrDel = solrDeleteVolumeCmd(volume_id);
						logger.info("delete command: " + solrDel);
						sendMessage(session, collection, "Deleting volume at " + URI + "\n");

						String solr_del_res = htclient.restPost(solrDel,solr_index_uri);
						logger.info("HTTP POST delete operation result: " + solr_del_res);
						sendMessage(session, collection, "Successfully deleted volume from index\n");
					}

					String index_res = htclient.restPost(solrizedRes,solr_index_uri);
					logger.info("index_result " + index_res + " from " + solr_index_uri);
					sendMessage(session, collection, "sending doc '" + document + "' to index\n");
				}
			} else if(op.matches(".*DELETE.*")) {
				logger.info("delete operation = " + op);
				String res = htclient.restDelete(URI);
				String solrDel = solrDeleteDocCmd(collection,document);

				logger.info("delete command: " + solrDel);
				sendMessage(session, collection,
						"Performing delete on document '" + document + "' at URI '" + URI + "'\n");

				String solr_del_res = htclient.restPost(solrDel,solr_index_uri);
				res = res + "\n" + solr_del_res;
				sendMessage(session, collection,
						"Successfully deleted document '" + document + "' at URI '" + URI + "'\n");
			} else if(op.matches(".*GET.*")) {
				logger.info("GET operation = " + op);
			} else if(op.matches(".*COMMIT.*")) {
				String solr_commit_uri = UriTemplate.fromTemplate(consts.getConstants().getProperty("commit.template"))
						.set("solr_hostport", index_server)
						.set("commit", "true")
						.expand();
                sendMessage(session, collection,"Finalizing operations by committing the changes in " + URI
                        + " to index.\n");

				String commit_res = htclient.restGet(solr_commit_uri);

				logger.info("Commit command " + solr_commit_uri + " result:\n" + commit_res);
				sendMessage(session, collection,"Finished!\n");
			} else {
				String res = htclient.restHead(URI);
				logger.info("Other operation result: " + res);
			}
		} else {
			sendMessage(session, collection, "The document '" + document
					+ "' doesn't seem to belong in database. Cannot perform operation.\n");
		}
	}

	protected static void sendMessage(Session session, String collection, String message) throws JMSException {
		String feedback_queue = collection + "_feedback";
		Destination feedback_destination = session.createQueue(feedback_queue);
		MessageProducer feedbackProducer = session.createProducer(feedback_destination);
		feedbackProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		TextMessage text_message = session.createTextMessage(message);
		feedbackProducer.send(text_message);
		feedbackProducer.close();
	}

	// this is for deleting single TEI documents, which may correspond
	// to many solr records

	private static String solrDeleteVolumeCmd(String volume_id) {
		String delete_query = "volume_id_ssi:" + volume_id;

		// don't use query *:*, that is dangerous

		String solr_del = "<delete><query>" + delete_query + "</query></delete>";

		return solr_del;
	}

	private static String solrDeleteDocCmd (String collection, String document) {

		String doc_part = document
				.replaceAll("\\.xml$","-root")
				.replaceAll("/","-");
		String delete_query = "volume_id_ssi:" + collection + "-" + doc_part;

		// don't use query *:*, that is dangerous

		String solr_del = "<delete><query>" + delete_query + "</query></delete>";

		return solr_del;
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
		Logger logger = Logger.getLogger(RunLoad.class);
		logger.info("logging at level " + level + " in file " + file + "\n");
		return logger;
	}
}
