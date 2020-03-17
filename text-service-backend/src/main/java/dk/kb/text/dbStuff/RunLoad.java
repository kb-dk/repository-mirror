package dk.kb.text.dbStuff;

import com.damnhandy.uri.template.UriTemplate;
import dk.kb.text.ConfigurableConstants;
import dk.kb.text.message.ResponseMediator;
import dk.kb.text.message.Invocation;
import org.apache.log4j.Logger;

import javax.jms.JMSException;

/**
 * Created by dgj on 17-11-2016.
 * Stolen by slu on 11-01-2019
 */
public class RunLoad {

	protected static final ConfigurableConstants CONFIG = ConfigurableConstants.getInstance();
	protected static final Logger logger = Logger.getLogger(RunLoad.class);

	public static final String FINAL_MESSAGE = "Finished!\n";
	public static final String EMPTY_OPERATION_MESSAGE = "No revisions found: Nothing to do!";


	public static final String HEAD_VOLUME_ID_REQUEST = "X-Volume-ID";

	protected final ApiClient apiClient;
	protected final Invocation invocation;
	protected final ResponseMediator responseMediator;

	protected final String credField;
	protected final String user;
	protected final String password;

	protected final String index_name;
	protected final String index_server;
	protected final String solr_index_uri;

	protected final FilePathHack fileFixer = new FilePathHack();

	/**
	 * Constructor.
	 * A new RunLoad should be instantiated for each invocation.
	 * @param apiClient        The api client for loading and indexing the data.
	 * @param invocation       The invocation for this load.
	 * @param responseMediator The mediator for sending responses.
	 */
	public RunLoad(ApiClient apiClient, Invocation invocation, ResponseMediator responseMediator) {
		this.apiClient = apiClient;
		this.invocation = invocation;
		this.responseMediator = responseMediator;

		credField = invocation.getTarget() + ".credentials"; // something like staging.credentials
		user   = CONFIG.getConstants().getProperty(credField).split(";")[0];
		password = CONFIG.getConstants().getProperty(credField).split(";")[1];
		logger.info("credField: " + credField + ", creds user: " + user + " creds passwd: " + password);

		index_name = CONFIG.getConstants().getProperty(invocation.getTarget() + "." + "index_name");
		index_server = CONFIG.getConstants().getProperty(invocation.getTarget() + "." + "index_hostport");
		solr_index_uri = UriTemplate.fromTemplate(CONFIG.getConstants().getProperty("indexing.template"))
				.set("index_name", index_name)
				.set("solr_hostport", index_server)
				.expand();

		fileFixer.setDatabase(CONFIG.getConstants().getProperty(invocation.getTarget()));
		fileFixer.setCollection(invocation.getCollection());

		// initialize apiClient
		String database_host = CONFIG.getConstants().getProperty(invocation.getTarget()).split(":")[0];
		String port_number   = CONFIG.getConstants().getProperty(invocation.getTarget()).split(":")[1];
		int port = Integer.parseInt(port_number);
		String realm = "exist";

		apiClient.setLogin(user,password,database_host,port,realm);
	}

	/**
	 * Commit the changes of the operations.
	 * This should be the final action on an invocation.
	 * @throws Exception
	 */
	public void commit() throws Exception {
		String URI = fileFixer.getServicePath();
		String solr_commit_uri = UriTemplate.fromTemplate(CONFIG.getConstants().getProperty("commit.template"))
				.set("solr_hostport", index_server)
				.set("index_name",index_name)
				.set("commit", "true")
				.expand();
		responseMediator.sendMessage("Finalizing operations by committing the changes in " + URI + " to index.\n");

		String commit_res = apiClient.restGet(solr_commit_uri);

		logger.info("Commit command " + solr_commit_uri + " result:\n" + commit_res);
		responseMediator.sendMessage(FINAL_MESSAGE);
	}

	public void handleOperation(String document, String op) throws Exception {
		// Start by dismissing empty operations and invalid document paths.
		if(op.matches(".*EMPTY.*")) {
			responseMediator.sendMessage(EMPTY_OPERATION_MESSAGE);
			return;
		}

		fileFixer.setDocument(document);
		if(! fileFixer.validDocPath()) {
			responseMediator.sendMessage( "The document '" + document
					+ "' doesn't seem to belong in database. Cannot perform operation.\n");
			return;
		}


		String URI = fileFixer.getServicePath();
		// http:// xstorage-stage-01.kb.dk:8080/exist/rest/db/text-retriever/gv/1815_264/txt.xml

		String existFile = fileFixer.getDocument();
		// 1815_264/txt.xml

		String file = CONFIG.getConstants().getProperty("data.home") + invocation.getRepository() + "/" + document;
		// looks like /home/text-service/GV/1841/1841GV/1841_700/1841_700_txr.xml

		logger.info("Operation: " + op + ", with URI '" + URI + "' and file '" + file + "' and exist file '"
				+ existFile + "'");

		if(op.matches(".*PUT.*")) {
			performPutOperation(file, URI, existFile, document);
		} else if(op.matches(".*DELETE.*")) {
			performDeleteOperation(file, URI, existFile, document);
		} else {
			responseMediator.sendMessage( "Operation " + op + " is not supported");
		}
	}

    String solrizrURICalculator(String existFile,String collection) { 
	return UriTemplate.fromTemplate(CONFIG.getConstants().getProperty("solrizr.template"))
	    .set("exist_hostport", CONFIG.getConstants().getProperty(invocation.getTarget()) )
	    .set("op", "solrize")
	    .set("doc", existFile)
	    .set("c", collection)
	    .expand();
    }
    
	protected void performPutOperation(String file, String URI, String existFile, String document)
			throws JMSException {
		String putRes = apiClient.restPut(file, URI);
		if(putRes != null) {
			logger.info("HTTP PUT done ");
		} else {
			logger.error("HTTP PUT problem");
		}

		String solrizrURI =  solrizrURICalculator( existFile,invocation.getCollection());
	
		logger.info("solrizing at " + solrizrURI);

		String capabilitizrURI =
				UriTemplate.fromTemplate(CONFIG.getConstants().getProperty("capabilitizr.template"))
						.set("user",user)
						.set("password",password)
						.set("exist_hostport", CONFIG.getConstants().getProperty(invocation.getTarget()) )
						.set("op", "solrize")
						.set("doc", existFile)
						.set("c", invocation.getCollection())
						.expand();
		logger.info("capabilitizring at " + capabilitizrURI);

		responseMediator.sendMessage( "Putting document " + document + " to " + URI + "\n");

		String capabilitizrRes = null;
		if(invocation.getCollection().toLowerCase().matches(".*(gv)|(sks).*")) {
			capabilitizrRes = apiClient.restGet(capabilitizrURI);
			logger.debug("capability: " +  capabilitizrRes);
		}
		String solrizedRes     = apiClient.restGet(solrizrURI);

		if(solrizedRes == null) {
			logger.info("solrizr: got null");
			responseMediator.sendMessage( "Failed to solrize the document (failed to create index) '" + document + "\n");
		} else {
			logger.info("solrizr: status 200 OK");
			String volumeId = apiClient.getHttpHeader(HEAD_VOLUME_ID_REQUEST);

			// This is definately overkill for ADL, but
			// necessary for, let's say Grundtvig
			if(volumeId.length() > 0) {
				String solrDel = solrDeleteVolumeCmd(volumeId);
				logger.info("delete command: " + solrDel);

				String solr_del_res = apiClient.restPost(solrDel,solr_index_uri);
				logger.info("HTTP POST delete operation result: " + solr_del_res);

				String index_res = apiClient.restPost(solrizedRes,solr_index_uri);
				logger.info("index_result " + index_res + " from " + solr_index_uri + " for volume id " + volumeId );
				responseMediator.sendMessage( "Reindexed '" + document + "' with volume id " + volumeId + "\n");
			}
		}
	}

	protected void performDeleteOperation(String file, String URI, String existFile, String document ) throws JMSException {
	    
		String solrizrURI =  solrizrURICalculator( existFile,invocation.getCollection());
	    	String solrizedRes     = apiClient.restGet(solrizrURI);

		if(solrizedRes == null) {
			logger.info("solrizr: got null");
			responseMediator.sendMessage( "Failed to solrize the document (failed to create index) '" + document + "\n");
		} else {
			logger.info("solrizr: status 200 OK");
			String volumeId = apiClient.getHttpHeader(HEAD_VOLUME_ID_REQUEST);
	    
			String res = apiClient.restDelete(URI);
			String solrDel = solrDeleteVolumeCmd(volumeId);

			logger.info("delete command: " + solrDel);
			responseMediator.sendMessage("Deleting document '" + document + "' at in database URI '" + URI + "'\n");

			String solr_del_res = apiClient.restPost(solrDel,solr_index_uri);
			res = res + "\n" + solr_del_res;
			responseMediator.sendMessage("Deleted volume with ID '" + volumeId + "' from index\n");
		}
	}

	/**
	 * This is for deleting single TEI documents, which may correspond
	 * to many solr records
	 * @param volumeId The ID of the volume to delete.
	 * @return The SOLR string for deleting a given volume.
	 */
	protected String solrDeleteVolumeCmd(String volumeId) {
		String deleteQuery = "volume_id_ssi:" + volumeId;

		// don't use query *:*, that is dangerous

		String solrDel = "<delete><query>" + deleteQuery + "</query></delete>";

		return solrDel;
	}

}
