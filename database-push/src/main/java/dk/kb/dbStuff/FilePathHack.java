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

	    // A data directory should match (GNU find regexp)
	    // '^.*18[0-9][0-9]GV.*\$'
	    // This (perl) regexp is the one we use for extracting data
	    // (18\d\d)_(\d+[a-zA-Z]?)_?(\d+)?_(com|intro|txr|txt|v0).xml$

	    String pat = "(18\\d\\d)_(\\d+[a-zA-Z]?)_?(\\d+)?_(com|intro|txr|txt|v0).xml$";
	    Pattern cpat = Pattern.compile(pat);
	    Matcher match  = cpat.matcher(this.path);
	    if(match.matches()) {
		uri = match.group(1) + "/" + match.group(2);
		if(match.group(3).length() >0) {
		    uri = uri + "/" + match.group(3);
		}
		uri = uri + "/" + match.group(4);
	    }
	    
	} else {
	    uri = this.collection + "/" + this.path;
	}
	return uri;
    }

}
