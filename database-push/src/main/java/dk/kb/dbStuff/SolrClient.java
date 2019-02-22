package dk.kb.dbStuff;

public class SolrClient {

    private String solr_server = "";
    private String file_server = "";

    public SolrClient() {}

    public String getIndexEntry() {
	return "";
    }

    public boolean sendIndexEntry() {
	return false;
    }
	
    public void setSolrServer(String server) {
	this.solr_server = server;
    }

    public void setFileServer(String server) {
	this.file_server = server;
    }

    



}