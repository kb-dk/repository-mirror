package dk.kb.dbStuff;

import org.apache.http.client.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.apache.http.impl.client.*;

public class ApiClient {

    public ApiClient() {}

    public String restGet(String URI) {
	String contents = "";
	try {
	    HttpGet request = new HttpGet(URI);
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
	    CloseableHttpClient httpClient = HttpClients.createDefault();
	    CloseableHttpResponse response = httpClient.execute(request);
	    HttpEntity entity = response.getEntity();
	    contents = EntityUtils.toString(entity);
	} catch(java.io.IOException e) {

	}
	return contents;
    }


}