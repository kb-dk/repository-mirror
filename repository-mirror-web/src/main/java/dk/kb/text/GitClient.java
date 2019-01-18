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

public class GitClient {

    private static ConfigurableConstants consts = ConfigurableConstants.getInstance();
    private static Logger logger = configureLog4j();

    Git git = null;

    public GitClient(String repo) {
	String home = consts.getConstants().getProperty("data.home");

	try {
	    git = Git.open( new F‌ile( home + "/" + repo + "/.git" ) );
	} catch(java.io.IOException repoProblem ) {
	    logger.error("IO prob: " + repoProblem);
	}

    }

    public String gitLog() {
	try {
	    java.lang.Iterable<RevCommit> log = git.log().call();
	    return log.iterator().next().toString();
	} catch (org.eclipse.jgit.api.errors.GitAPIException gitProblem) {
	    logger.error("git prob: " + gitProblem);
	    return "git failed";
	}
    }

    public String gitFetch() {
	try {
	    FetchCommand fetch = git.fetch();
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
	    branches.setListMode(ListBranchCommand.ListMode.REMOTE);
	    java.util.List<Ref> res      = branches.call();
	    Iterator<Ref> lister =  res.iterator();
	    String blist = "";
	    while(lister.hasNext()) {
		blist = blist + lister.next() + "\n"; 
	    }
	    return res.toString();
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
	Logger logger = Logger.getLogger(GitClient.class);
	return logger;
    }

}