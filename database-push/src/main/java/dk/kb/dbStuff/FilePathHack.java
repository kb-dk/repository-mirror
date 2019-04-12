package dk.kb.dbStuff;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
I had the intention that just about everything should be configurable
and nothing should be hard coded in JAVA.  There have been things
hardcoded, but they are very few, until this class. This is the one
containing the mapping between the file systemes in the git repository
and the eXist database. I assumed that I shouldn't need such a thing,
but that was wrong.
*/
public class FilePathHack {

    private String collection = null;
    private String path       = null;

    public FilePathHack() {}

    public void setCollection(String coll) {
	this.collection = coll;
    }

	
    public void setGitPath(String path) {
	this.path = path;
    }

    public String getServicePath() {
	String uri = "";
	if(collection.equals("adl")) {
	    uri = this.collection + "/" + this.path;
	} else if(collection.equals("sks")) {
	    String file = this.path.replaceAll("^.*data/v1.9/","");
	    uri = this.collection + "/" + file;
	} else if(collection.equals("gv")) {
	    String pat = "(18\\n\\n)_(\\n+[a-zA-Z]?)_?(\\n+)?_(com|intro|txr|txt|v0)$";
	    Pattern cpat = Pattern.compile(pat);
	    Matcher mat  = cpat.matcher(this.path);
	    if(mat.matches()) {
		uri = mat.group(1) + "/" + mat.group(2);
		if(mat.group(3).length() >0) {
		    uri = uri + "/" + mat.group(3);
		}
		uri = uri + "/" + mat.group(4);
	    }
	    
	} else {
	    uri = this.collection + "/" + this.path;
	}
	return uri;
    }

}
