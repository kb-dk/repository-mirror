package dk.kb.dbStuff;



public class FilePathHack {

    private String collection = null;
    private String path       = null;

    class FilePathHack() {}

    public void setCollection(String coll) {
	this.collection = coll;
    }

	
    public void setGitPath(String path) {
	this.path = path;
    }

    public String getServicePath() {
	String uri = "";
	return uri;
    }

}