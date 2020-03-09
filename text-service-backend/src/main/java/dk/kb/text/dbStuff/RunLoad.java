package dk.kb.text.dbStuff;

import com.damnhandy.uri.template.UriTemplate;
import dk.kb.text.ConfigurableConstants;
import dk.kb.text.connection.CloseableMessageProducer;
import dk.kb.text.pullStuff.Invocation;
import org.apache.log4j.Logger;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * Created by dgj on 17-11-2016.
 * Stolen by slu on 11-01-2019
 */
public class RunLoad {

	protected static ConfigurableConstants consts = ConfigurableConstants.getInstance();
	protected static Logger logger = Logger.getLogger(RunLoad.class);

	protected ApiClient apiClient;
	protected Session session;
	protected Invocation invocation;

	public RunLoad(Session session, Invocation invocation) {
		this.apiClient = new ApiClient();
		this.session = session;
		this.invocation = invocation;
	}

	public void handleOperation(String document, String op) throws Exception {
		String credField = invocation.getTarget() + ".credentials"; // something like staging.credentials

		logger.info("credField: " + credField);

		String user   = consts.getConstants().getProperty(credField).split(";")[0];
		String passwd = consts.getConstants().getProperty(credField).split(";")[1];

		logger.info("creds user: " + user + " creds passwd: " + passwd);

		FilePathHack fileFixer = new FilePathHack();
		fileFixer.setDatabase(consts.getConstants().getProperty(invocation.getTarget()));
		// staging gives something like xstorage-stage-01.kb.dk:8080
		fileFixer.setCollection(invocation.getCollection());
		// collection should be something like adl or gv
		fileFixer.setDocument(document);
		// 1841/1841GV/1841_700/1841_700_txt.xml

		String index_name   =  consts.getConstants().getProperty(invocation.getTarget() + "." + "index_name");
		String index_server =  consts.getConstants().getProperty(invocation.getTarget() + "." + "index_hostport");
		String solr_index_uri = UriTemplate.fromTemplate(consts.getConstants().getProperty("indexing.template"))
				.set("index_name", index_name)
				.set("solr_hostport", index_server)
				.expand();

		String URI = fileFixer.getServicePath();
		// http:// xstorage-stage-01.kb.dk:8080/exist/rest/db/text-retriever/gv/1815_264/txt.xml

		if(op.matches(".*COMMIT.*")) {
			String solr_commit_uri = UriTemplate.fromTemplate(consts.getConstants().getProperty("commit.template"))
					.set("solr_hostport", index_server)
					.set("index_name",index_name)
					.set("commit", "true")
					.expand();
			sendMessage(session, invocation.getCollection(),"Finalizing operations by committing the changes in " + URI + " to index.\n");

			String commit_res = apiClient.restGet(solr_commit_uri);

			logger.info("Commit command " + solr_commit_uri + " result:\n" + commit_res);
			sendMessage(session, invocation.getCollection(),"Finished!\n");
		} else if(op.matches(".*EMPTY.*")) {
			sendMessage(session, invocation.getCollection(),"No revisions found: Nothing to do!");
		} else {

			if(fileFixer.validDocPath()) {

				String existFile = fileFixer.getDocument();
				// 1815_264/txt.xml

				String file = consts.getConstants().getProperty("data.home") + invocation.getRepository() + "/" + document;
				// looks like /home/text-service/GV/1841/1841GV/1841_700/1841_700_txr.xml

				String database_host = consts.getConstants().getProperty(invocation.getTarget()).split(":")[0];
				String port_number   = consts.getConstants().getProperty(invocation.getTarget()).split(":")[1];
				int port = Integer.parseInt(port_number);
				String realm = "exist";

				apiClient.setLogin(user,passwd,database_host,port,realm);

				logger.info("URI  " + URI);
				logger.info("File " + file);
				logger.info("existFile " + existFile);

				if(op.matches(".*PUT.*")) {
					logger.info("operation = " + op);

					String putRes = apiClient.restPut(file, URI);
					if(putRes != null) {
						logger.info("HTTP PUT done ");
					} else {
						logger.error("HTTP PUT problem");
					}

					String solrizrURI =
							UriTemplate.fromTemplate(consts.getConstants().getProperty("solrizr.template"))
									.set("exist_hostport", consts.getConstants().getProperty(invocation.getTarget()) )
									.set("op", "solrize")
									.set("doc", existFile)
									.set("c", invocation.getCollection())
									.expand();
					logger.info("solrizing at " + solrizrURI);

					String capabilitizrURI =
							UriTemplate.fromTemplate(consts.getConstants().getProperty("capabilitizr.template"))
									.set("user",user)
									.set("password",passwd)
									.set("exist_hostport", consts.getConstants().getProperty(invocation.getTarget()) )
									.set("op", "solrize")
									.set("doc", existFile)
									.set("c", invocation.getCollection())
									.expand();
					logger.info("capabilitizring at " + capabilitizrURI);

					sendMessage(session, invocation.getCollection(), "Putting document " + document + " to " + URI + "\n");

					String capabilitizrRes = null;
					if(invocation.getCollection().toLowerCase().matches(".*(gv)|(sks).*")) {
						capabilitizrRes = apiClient.restGet(capabilitizrURI);
						logger.debug("capability: " +  capabilitizrRes);
					}
					String solrizedRes     = apiClient.restGet(solrizrURI);

					if(solrizedRes == null) {
						logger.info("solrizr: got null");
						sendMessage(session, invocation.getCollection(), "Failed to solrize the document (failed to create index) '" + document + "\n");
					} else {
						logger.info("solrizr: status 200 OK");
						String volume_id = apiClient.getHttpHeader("X-Volume-ID");

						// This is definately overkill for ADL, but
						// necessary for, let's say Grundtvig
						if(volume_id.length() > 0) {
							String solrDel = solrDeleteVolumeCmd(volume_id);
							logger.info("delete command: " + solrDel);

							String solr_del_res = apiClient.restPost(solrDel,solr_index_uri);
							logger.info("HTTP POST delete operation result: " + solr_del_res);

							String index_res = apiClient.restPost(solrizedRes,solr_index_uri);
							logger.info("index_result " + index_res + " from " + solr_index_uri + " for volume id " + volume_id );
							sendMessage(session, invocation.getCollection(), "Reindexed '" + document + "' with volume id " + volume_id + "\n");
						}
					}
				} else if(op.matches(".*DELETE.*")) {
					logger.info("delete operation = " + op);
					String res = apiClient.restDelete(URI);
					String solrDel = solrDeleteDocCmd(invocation.getCollection(),document);

					logger.info("delete command: " + solrDel);
					sendMessage(session, invocation.getCollection(),
							"Deleting document '" + document + "' at in database URI '" + URI + "'\n");

					String solr_del_res = apiClient.restPost(solrDel,solr_index_uri);
					res = res + "\n" + solr_del_res;
					sendMessage(session, invocation.getCollection(),
							"Deleted document '" + document + "' from index\n");
				} else {
					sendMessage(session, invocation.getCollection(), "Operation " + op + " is not supported");
				}
			} else {
				sendMessage(session, invocation.getCollection(), "The document '" + document
						+ "' doesn't seem to belong in database. Cannot perform operation.\n");
			}
		}
	}

	protected void sendMessage(Session session, String collection, String message) throws JMSException {
		String feedback_queue = collection + "_feedback";
		Destination feedback_destination = session.createQueue(feedback_queue);
		try (CloseableMessageProducer feedbackProducer = new CloseableMessageProducer(session.createProducer(feedback_destination));) {
			feedbackProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			TextMessage text_message = session.createTextMessage(message);
			feedbackProducer.send(text_message);
		}
	}

	// this is for deleting single TEI documents, which may correspond
	// to many solr records

	private String solrDeleteVolumeCmd(String volume_id) {
		String delete_query = "volume_id_ssi:" + volume_id;

		// don't use query *:*, that is dangerous

		String solr_del = "<delete><query>" + delete_query + "</query></delete>";

		return solr_del;
	}

	private String solrDeleteDocCmd (String collection, String document) {

		String doc_part = document
				.replaceAll("\\.xml$","-root")
				.replaceAll("/","-");
		String delete_query = "volume_id_ssi:" + collection + "-" + doc_part;

		// don't use query *:*, that is dangerous

		String solr_del = "<delete><query>" + delete_query + "</query></delete>";

		return solr_del;
	}
}
