package dk.kb.dbStuff;

import org.apache.http.client.*;
import org.apache.http.client.methods.*;

import org.apache.http.HttpEntity;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.*;
import org.apache.http.util.EntityUtils;
import org.apache.http.impl.client.*;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.NameValuePair;

import org.apache.http.impl.auth.AuthSchemeBase;
import org.apache.http.auth.Credentials;

import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import java.util.Properties;

public class ApiClient {

    private Credentials credentials = null;
    private String user = "";
    private String passwd  = "";

    private static ConfigurableConstants consts = ConfigurableConstants.getInstance();
    private Logger logger = configureLog4j();

    public ApiClient() {}

    private Credentials getCred() {
	return this.credentials;
    }

    public void setLogin(String user, String password) {
	this.user    = user;
	this.passwd  = password;
    }

    private List <NameValuePair>  applyCredentials() {
	List <NameValuePair> nvps = new ArrayList <NameValuePair>();
	nvps.add(new BasicNameValuePair("username", this.user));
	nvps.add(new BasicNameValuePair("password", this.passwd));
	return nvps;
    }

    public String restGet(String URI) {
	String contents = "";
	try {
	    HttpGet request = new HttpGet(URI);
	    CloseableHttpClient httpClient = HttpClients.createDefault();
	    if( !this.user.equalsIgnoreCase("") && !this.passwd.equalsIgnoreCase("")) {}
	    CloseableHttpResponse response = httpClient.execute(request);
	    HttpEntity entity = response.getEntity();
	    contents = EntityUtils.toString(entity);
	} catch(java.io.IOException e) {

	}
	return contents;
    }

    public String restHead(String URI) {
	String contents = "";
	try {
	    HttpHead request = new HttpHead(URI);
	    CloseableHttpClient httpClient = HttpClients.createDefault();
	    CloseableHttpResponse response = httpClient.execute(request);
	    HttpEntity entity = response.getEntity();
	    contents = contents + response.toString();
	    //	    contents = contents + EntityUtils.toString(entity);
	} catch(java.io.IOException e) {

	}
	return contents;
    }

    public String restDelete(String URI) {
	String contents = "";
	try {
	    HttpDelete request = new HttpDelete(URI);
	    CloseableHttpClient httpClient = HttpClients.createDefault();
	    CloseableHttpResponse response = httpClient.execute(request);
	    HttpEntity entity = response.getEntity();
	    contents = EntityUtils.toString(entity);
	} catch(java.io.IOException e) {

	}
	return contents;
    }

    public String restPut(String text, String URI) {
	String contents = "";
	try {
	    HttpPut request = new HttpPut(URI);
	    AbstractHttpEntity entity = new StringEntity(text);
	    entity.setContentType("text/xml");
            entity.setContentEncoding("UTF-8");
	    request.setEntity(entity);
	    CloseableHttpClient httpClient = HttpClients.createDefault();
	    CloseableHttpResponse response = httpClient.execute(request);

	} catch(java.io.IOException e) {

	}
	return contents;
    }

    public String restPost(String text, String URI) {
	String contents = "";
	try {
	    HttpPost request = new HttpPost(URI);
	    HttpEntity entity = new StringEntity(text);
	    request.setEntity(entity);
	    CloseableHttpClient httpClient = HttpClients.createDefault();
	    CloseableHttpResponse response = httpClient.execute(request);

	} catch(java.io.IOException e) {

	}
	return contents;
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