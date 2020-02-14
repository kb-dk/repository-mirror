package dk.kb.dbStuff;

import com.damnhandy.uri.template.UriTemplate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/*
I had the intention that just about everything should be configurable
and nothing should be hard coded in JAVA.  There have been things
hardcoded, but they are very few, until this class. This is the one
containing the mapping between the file systemes in the git repository
and the eXist database. I assumed that I shouldn't need such a thing,
but that was wrong.
*/
public class FilePathHack {

    private static Logger logger = Logger.getLogger(FilePathHack.class);
    
    private String exist_db   = "target-db-should-be-initialized";
    private String collection = null;
    private String path       = null;

    private String fixed_file = null;

    private static ConfigurableConstants consts = ConfigurableConstants.getInstance();

    public FilePathHack() {}

    public void setDatabase(String trgt) {
	this.exist_db = trgt;
    }


    public void setCollection(String coll) {
	this.collection = coll.toLowerCase();
    }

	
    public void setDocument(String path) {
	this.path = path;
    }

    public String getDocument() {
	return this.fixed_file;
    }

    public boolean validDocPath() {
	if(collection.equals("gv")) {
	    if(this.path.length() > 0 && this.path.matches("^.*18[0-9][0-9]GV.*$")) {
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
	    this.fixed_file = this.path;
	    return this.encodeUri(this.path);
	} else if(collection.equals("sks")) {

	    // The Kierkegaard text collection has its data under
	    // data/v1.9/ where v1.9 is a version number In the
	    // snippet server/database we recast these to paths like
	    // sks/ee1/txt.xml, i.e., collection name, work acronym
	    // and file.

	    this.fixed_file = this.path.replaceAll("^.*data/v1.9/","");
	    return this.encodeUri(this.fixed_file);
	} else if(collection.equals("gv")) {
	    if(this.validDocPath()) {

		// In Grundtvig the repository contain more than we
		// want, but we'll always find the data files in 
		// data directories matching regexp '^.*18[0-9][0-9]GV.*\$'

		// files are have names like GV/1830/1830GV/1830_485/1830_485_txt.xml
		// we recast them to gv/1830_485/txt.xml

		this.fixed_file = "";
		String  pat   = ".+/(18\\d\\d_[^/]+)_(com|intro|txr|txt|v\\d+).xml$";
		Pattern cpat  = Pattern.compile(pat);
		Matcher match = cpat.matcher(this.path);
		if(match.matches()) {
		    this.fixed_file = this.fixed_file + match.group(1) + "/" + match.group(2) + ".xml";
		    return this.encodeUri(this.fixed_file);
		}
	    }
	} else {
	    this.fixed_file = this.path;
	    return this.encodeUri(this.path);
	}
	return "";
    }

    public String encodeUri(String document) {
	String URI = UriTemplate.fromTemplate(consts.getConstants().getProperty("file.template"))
	    .set("exist_hostport", this.exist_db )
	    .set("collection", this.collection)
	    .set("file", document)
	    .expand();

	return URI;
    }

}
