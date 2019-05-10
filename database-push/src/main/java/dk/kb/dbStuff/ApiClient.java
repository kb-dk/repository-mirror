package dk.kb.dbStuff;

import org.apache.http.client.*;
import org.apache.http.client.methods.*;

import org.apache.http.client.config.*;

import org.apache.http.HttpEntity;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.*;
import org.apache.http.client.entity.EntityBuilder;


import org.apache.http.util.EntityUtils;
import org.apache.http.impl.client.*;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.NameValuePair;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.client.ResponseHandler;

import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import java.util.Properties;

import java.io.InputStream;
import java.io.StringWriter;
import java.io.BufferedReader;
import java.io.FileReader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerFactory;
import net.sf.saxon.TransformerFactoryImpl;
import javax.xml.transform.*;

public class ApiClient {

    private String user = "";
    private String passwd  = "";
    private String host  = "";
    private int    port;
    private String realm  = "";

    CloseableHttpResponse ht_response = null;

    private static ConfigurableConstants consts = ConfigurableConstants.getInstance();
    private Logger logger = configureLog4j();

    private TransformerFactory trans_fact = new TransformerFactoryImpl();
    private Transformer transformer = null;

    public ApiClient() {
	this.init();
    }

    private void init() {
	try {
	    java.io.File src = new java.io.File(consts.getConstants().getProperty("xsl.add_id"));
	    javax.xml.transform.stream.StreamSource source = new javax.xml.transform.stream.StreamSource();
	    this.transformer = trans_fact.newTransformer(source);
	} catch(TransformerConfigurationException xerror) {
	    logger.info(logStackTrace(xerror));
	}
    }

    public void setLogin(String user, String password, String host, int port, String realm) {
	this.user    = user;
	this.passwd  = password;
	this.host    = host;
	this.port    = port;
	this.realm   = realm;
    }

    private CredentialsProvider setCred() {
	CredentialsProvider cred = new BasicCredentialsProvider();
        cred.setCredentials(new AuthScope(this.host, this.port, this.realm),
			    new UsernamePasswordCredentials(this.user, this.passwd));
	return cred;
    }

    private List <NameValuePair>  fillForm() {
	List <NameValuePair> nvps = new ArrayList <NameValuePair>();
	nvps.add(new BasicNameValuePair("username", this.user));
	nvps.add(new BasicNameValuePair("password", this.passwd));
	return nvps;
    }

    public String restGet(String URI) {
	String contents = null;
	CloseableHttpClient httpClient = null;
	try {
	    HttpGet request = new HttpGet(URI);
	    httpClient = HttpClients.createDefault();
	    if( !this.user.equalsIgnoreCase("") && !this.passwd.equalsIgnoreCase("")) {}
	    ht_response = httpClient.execute(request);
	    HttpEntity entity = ht_response.getEntity();
	    int statusCode = ht_response.getStatusLine().getStatusCode();
	    logger.info("GOT " +  statusCode + " for GET " + URI);
	    if(statusCode == 200) {
		contents = EntityUtils.toString(entity);
		return contents;
	    }
	} catch(java.io.IOException e) {
	    logger.info(logStackTrace(e));
	}
	try { if(httpClient != null) httpClient.close(); } catch(java.io.IOException e) {logger.info(logStackTrace(e));}
	return contents;
    }

    public String getHttpHeader(String headName) {
	String header= "";
	if(ht_response != null && ht_response.containsHeader(headName)) {
	    header = ht_response.getFirstHeader(headName).getValue();
	}
	return header;
    }

    public String restHead(String URI) {
	String contents = "";
	CloseableHttpClient httpClient = null;
	try {
	    HttpHead request = new HttpHead(URI);
	    httpClient = HttpClients.createDefault();
	    CloseableHttpResponse response = httpClient.execute(request);
	    HttpEntity entity = response.getEntity();
	    contents = contents + response.toString();
	} catch(java.io.IOException e) {
	    logger.info(logStackTrace(e));
	}
	try { if(httpClient != null) httpClient.close(); } catch(java.io.IOException e) {logger.info(logStackTrace(e));}

	return contents;
    }

    public String restDelete(String URI) {
	String contents = "";
	CloseableHttpClient httpClient = null;
	try {
	    HttpDelete request = new HttpDelete(URI);
	    httpClient = HttpClients.createDefault();
	    if( this.user.equalsIgnoreCase("") && this.passwd.equalsIgnoreCase("")) {
		httpClient = HttpClients.createDefault();
	    } else {
		httpClient = HttpClients.custom().setDefaultCredentialsProvider( this.setCred() ).build();
	    }
	    CloseableHttpResponse response = httpClient.execute(request);
	    HttpEntity entity = response.getEntity();
	    contents = EntityUtils.toString(entity);
	    contents = contents + response.toString();
	} catch(java.io.IOException e) {
	    logger.info(logStackTrace(e));
	}
	try { if(httpClient != null) httpClient.close(); } catch(java.io.IOException e) {logger.info(logStackTrace(e));}

	return contents;
    }

    // should I set agent to, say "crud-client/0.1 "?

    // This giving me even grayer hair. See
    // https://stackoverflow.com/questions/9161591/apache-httpclient-4-x-behaving-strange-when-uploading-larger-files
    // https://stackoverflow.com/questions/32303385/http-post-using-apache-http-client-with-expect-continue-option-to-an-authorized

    public String restPut(String file, String URI) {
	String contents = "";
	try {
	    boolean apacheHttpApiWorks = true;
	    if(apacheHttpApiWorks) {
		String text = null;
		if(this.transformer != null) {
		    // here we need to xsl transform the text to add
		    // xml:id on all elements that haven't got it already
		    // and make sure that the root element has xml:id="root"
		    org.w3c.dom.Document doc = this.readDom(file);
		    java.io.StringWriter wrtr = new java.io.StringWriter();
		    DOMSource source = new DOMSource(doc);
		    StreamResult result = new StreamResult(wrtr);
		    try {
			this.transformer.transform(source, result);
		    } catch(TransformerException trprblm) {
			logger.info(logStackTrace(trprblm));
		    }
		    text = wrtr.toString();
		} else {
		    text = readFile(file);
		}
		contents = doingItApacheWay(text, URI);
	    } else {
		DirtyPutHack hack = new DirtyPutHack();
		contents = hack.restUpload("PUT", file, URI,this.user,this.passwd);
	    }
	} catch(java.io.IOException e) {
	    logger.info(logStackTrace(e));
	}
	return contents;
    }

    public String readFile(String fileName) throws java.io.IOException {
	BufferedReader br = new BufferedReader(new FileReader(fileName));
	try {
	    StringBuilder sb = new StringBuilder();
	    String line = br.readLine();

	    while (line != null) {
		sb.append(line);
		sb.append("\n");
		line = br.readLine();
	    }
	    return sb.toString();
	} finally {
	    br.close();
	}
    }

    public org.w3c.dom.Document readDom(String fileName) throws java.io.IOException {
	java.io.File srcFile = new java.io.File(fileName);
	org.w3c.dom.Document doc = null;
	try {
	    javax.xml.parsers.DocumentBuilder builder =  javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
	    doc = builder.parse(srcFile);
	} catch (javax.xml.parsers.ParserConfigurationException parser) {
	    logger.info(logStackTrace(parser));
	} catch (org.xml.sax.SAXException sax) {
	    logger.info(logStackTrace(sax));
	}

	return doc;

    }

    public String doingItApacheWay(String text, String URI)  throws java.io.IOException {

	String contents = "";

	CloseableHttpClient httpClient = null;
	try {
	    HttpPut request = new HttpPut(URI);
	    if( this.user.equalsIgnoreCase("") && this.passwd.equalsIgnoreCase("")) {
		logger.info("Have no credentials for " + URI);
		httpClient = HttpClients.createDefault();
	    } else {
		logger.info("using credentials " +  this.user + " with password " + this.passwd);
		httpClient = HttpClients.custom().setDefaultCredentialsProvider( this.setCred() ).build();
	    }
	    if(httpClient == null) {
		logger.info("failed to create httpClient");
	    } else {

		RequestConfig.Builder req = RequestConfig.custom();
		req.setConnectTimeout(50000);
		req.setConnectionRequestTimeout(50000);
		req.setRedirectsEnabled(true);
		req.setSocketTimeout(50000);
		req.setExpectContinueEnabled(true);

		request.setConfig(req.build());

		logger.info("setting entity " + URI);

		HttpEntity entity = new StringEntity(text,ContentType.create("text/xml","UTF-8"));
		request.setEntity( entity);

		logger.info("about to execute " + URI);

		// Create a custom response handler
		ResponseHandler<String> responseHandler = response->{
		    int status = response.getStatusLine().getStatusCode();
		    if (status >= 200 && status < 300 || status==401) {
			HttpEntity responseEntity = response.getEntity();
			logger.info("Got " +  status + " for " + URI);
			return responseEntity != null ? EntityUtils.toString(responseEntity) : null;
		    } else {
			throw new ClientProtocolException("Unexpected response status: " + status);
		    }
		}; 
		String responseBody = httpClient.execute(request, responseHandler);
		// CloseableHttpResponse response = httpClient.execute(request);
		// int statusCode = response.getStatusLine().getStatusCode();
		// logger.info("GOT " +  statusCode + " for " + URI);
		contents = contents + responseBody; //response.toString();
	    }
	} catch(java.io.IOException io) {
	    logger.info("IO exception for " + URI);
	    logger.info("Exception " + io.toString());
	    logger.info(logStackTrace(io));
	}
	try { if(httpClient != null) httpClient.close(); } catch(java.io.IOException e) {logger.info(logStackTrace(e));}
	return contents;

    }

    public String restPost(String text, String URI) {
	String contents = "";
	CloseableHttpClient httpClient = null;
	try {
	    httpClient = HttpClients.createDefault();
	    HttpPost request = new HttpPost(URI);
	    HttpEntity entity = new StringEntity(text,ContentType.create("text/xml","UTF-8"));
	    request.setEntity(entity);
	    // We post to solr, which can be done on port 8983
	    // if( !this.user.equalsIgnoreCase("") && !this.passwd.equalsIgnoreCase("")) {}
	    CloseableHttpResponse response = httpClient.execute(request);
	    logger.info(response.getStatusLine());
	    contents = contents + response.getStatusLine() + "\n" + response.toString();
	} catch(java.io.IOException e) {
	    logger.info(logStackTrace(e));
	}
	try { if(httpClient != null) httpClient.close(); } catch(java.io.IOException e) {logger.info(logStackTrace(e));}
	return contents;
    }

    // https://stackoverflow.com/questions/8595748/java-runtime-exec

    // EDIT:: I don't have csh on my system so I used bash instead. The following worked for me
    // Process p = Runtime.getRuntime().exec(new String[]{"bash","-c","ls /home/XXX"});


    private String logStackTrace(java.lang.Exception e) {

	java.io.StringWriter sw = new java.io.StringWriter();
	java.io.PrintWriter  pw = new java.io.PrintWriter(sw);

	e.printStackTrace(pw);
	return  sw.toString();

    }


    /* Should really not do it this way */

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