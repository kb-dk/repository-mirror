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
import java.io.BufferedReader;
import java.io.FileReader;

public class ApiClient {

    private String user = "";
    private String passwd  = "";

    private static ConfigurableConstants consts = ConfigurableConstants.getInstance();
    private Logger logger = configureLog4j();

    public ApiClient() {}

    public void setLogin(String user, String password) {
	this.user    = user;
	this.passwd  = password;
    }

    private CredentialsProvider setCred() {
	CredentialsProvider cred = new BasicCredentialsProvider();
        cred.setCredentials(new AuthScope("localhost", 8080, "exist"),
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
	String contents = "";
	CloseableHttpClient httpClient = null;
	try {
	    HttpGet request = new HttpGet(URI);
	    httpClient = HttpClients.createDefault();
	    if( !this.user.equalsIgnoreCase("") && !this.passwd.equalsIgnoreCase("")) {}
	    CloseableHttpResponse response = httpClient.execute(request);
	    HttpEntity entity = response.getEntity();
	    contents = EntityUtils.toString(entity);
	} catch(java.io.IOException e) {
	    logger.info(logStackTrace(e));
	}
	try { if(httpClient != null) httpClient.close(); } catch(java.io.IOException e) {logger.info(logStackTrace(e));}
	return contents;
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
	    boolean apacheHttpApiWorks = false;
	    if(apacheHttpApiWorks) {
		String text = readFile(file);
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
	    HttpEntity entity = new StringEntity(text);
	    request.setEntity(entity);
	    if( !this.user.equalsIgnoreCase("") && !this.passwd.equalsIgnoreCase("")) {}
	    CloseableHttpResponse response = httpClient.execute(request);

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