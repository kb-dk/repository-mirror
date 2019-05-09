package dk.kb.dbStuff;

import com.damnhandy.uri.template.UriTemplate;
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
    private String baseUri    = null;
    private String target     = null;
    private static ConfigurableConstants consts = ConfigurableConstants.getInstance();

    public FilePathHack() {}

    public void setBaseUri(String bUri) {
	this.baseUri = bUri;
    }

    public void setTarget(String trgt) {
	this.target = trgt;
    }


    public void setCollection(String coll) {
	this.collection = coll;
    }

	
    public void setGitPath(String path) {
	this.path = path;
    }

    public boolean validDocPath() {
	if(collection.equals("gv")) {
	    if(this.path.matches("^.*18[0-9][0-9]GV.*$")) {
		return true;
	    } else {
		return false;
	    }
	} else {
	    return true;
	}
    }

    public String getServicePath() {

	if(collection.equals("adl")) {
	    return this.encodeUri(this.path);
	} else if(collection.equals("sks")) {
	    String file = this.path.replaceAll("^.*data/v1.9/","");
	    return this.encodeUri(file);
	} else if(collection.equals("gv")) {
	    if(this.validDocPath()) {
		// A data directory should match (GNU find regexp)
		// '^.*18[0-9][0-9]GV.*\$'
		// This (perl) regexp is the one we use for extracting data
		// (18\d\d)_(\d+[a-zA-Z]?)_?(\d+)?_(com|intro|txr|txt|v0).xml$

		String file = "";

		String pat = "(18\\d\\d)_(\\d+[a-zA-Z]?)_?(\\d+)?_(com|intro|txr|txt|v0).xml$";
		Pattern cpat = Pattern.compile(pat);
		Matcher match  = cpat.matcher(this.path);
		if(match.matches()) {
		    file = match.group(1) + "_" + match.group(2);
		    if(match.group(3).length() >0) {
			file = file + "_" + match.group(3);
		    }
		    file = file + "/" + match.group(4);
		}
		return this.encodeUri(file);
	    }
	} else {
	    return this.encodeUri(this.path);
	}
	return "";
    }

    public String encodeUri(String document) {
	String URI = UriTemplate.fromTemplate(consts.getConstants().getProperty("file.template"))
	    .set("exist_hostport", consts.getConstants().getProperty(this.target) )
	    .set("collection", this.collection)
	    .set("file", document)
	    .expand();
	return URI;
    }

}
