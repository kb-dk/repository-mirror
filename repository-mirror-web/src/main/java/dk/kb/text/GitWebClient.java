package dk.kb.text;

import org.eclipse.jgit.api.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.lib.*;

import java.io.File;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import java.util.Properties;
import java.util.Iterator;
import java.util.List;

import java.util.regex.*;

public class GitWebClient {

    private static ConfigurableConstants consts = ConfigurableConstants.getInstance();
    private static Logger logger = configureLog4j();

    private Git git = null;

    private CredentialsProvider credentials = null;

    public GitWebClient() {
	String repo = "public-adl-text-sources";
	init(repo);
    }

    public GitWebClient(String repo) {
	init(repo);
    }

    private void init(String repo) {
	String home   = consts.getConstants().getProperty("data.home");
	String user   = consts.getConstants().getProperty("git.user");
	String passwd = consts.getConstants().getProperty("git.password");

	try {
	    git = Git.open( new F‌ile( home + "/" + repo + "/.git" ) );
	} catch(java.io.IOException repoProblem ) {
	    logger.error("IO prob: " + repoProblem);
	}
	credentials = new UsernamePasswordCredentialsProvider(user,passwd);
    }

    public String gitLog() {
	try {
	    LogCommand log = git.log();
	    //	    log.setCredentialsProvider(credentials);
	    java.lang.Iterable<RevCommit> log_arator = log.call();
	    return log_arator.iterator().next().toString();
	} catch (org.eclipse.jgit.api.errors.GitAPIException gitProblem) {
	    logger.error("git prob: " + gitProblem);
	    return "git failed";
	}
    }


    //    cloneCommand.setCredentialsProvider( new UsernamePasswordCredentialsProvider( "user", "password" ) );

    public String gitFetch() {
	try {
	    FetchCommand fetch = git.fetch();
	    fetch.setCredentialsProvider(credentials);
	    fetch.setRemoveDeletedRefs(true);
	    FetchResult res = fetch.call();
	    return res.toString();
	} catch (org.eclipse.jgit.api.errors.GitAPIException gitProblem) {
	    logger.error("git prob: " + gitProblem);
	    return "git failed";
	}
    }

    public String gitPull() {
	try {
	    PullCommand pull = git.pull();
	    pull.setCredentialsProvider(credentials);
	    PullResult res   = pull.call();
	    return res.toString();
	} catch (org.eclipse.jgit.api.errors.GitAPIException gitProblem) {
	    logger.error("git prob: " + gitProblem);
	    return "git failed";
	}
    }

    public String gitBranches() {
	try {
	    ListBranchCommand branches = git.branchList();
	    //	    branches.setCredentialsProvider(credentials);
	    branches.setListMode(ListBranchCommand.ListMode.ALL);
	    java.util.List<Ref> res      = branches.call();
	    Iterator<Ref> lister =  res.iterator();
	    String blist = "";
	    while(lister.hasNext()) {
		Ref branch = lister.next();
		String name = branch.getName() + "";

		Pattern pat   = Pattern.compile(".*origin.*");
		// match anything 
		// Pattern pat   = Pattern.compile(".*");
		Matcher match = pat.matcher(name);
		
		String rgx = "^(.*?)/([^/]*)$";
		String readable_name  = name.replaceAll(rgx,"$2");

		if(match.matches()) {
		    String item = "<option value=" + name + ">" + readable_name + "</option>\n";
		    blist = blist + item;
		}
	    }
	    return blist;
	} catch (org.eclipse.jgit.api.errors.GitAPIException gitProblem) {
	    logger.error("git prob: " + gitProblem);
	    return "git failed";
	}
    }

    private static Logger configureLog4j() {

	String level = "info";
	if (System.getProperty("loglevel") != null ) level = System.getProperty("loglevel");

	String file = consts.getConstants().getProperty("queue.logfile");
	if (System.getProperty("logfile") != null) file = System.getProperty("logfile");

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
	Logger logger = Logger.getLogger(GitWebClient.class);
	return logger;
    }

}