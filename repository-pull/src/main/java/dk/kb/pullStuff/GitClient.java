package dk.kb.pullStuff;

import org.eclipse.jgit.api.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
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
    private CredentialsProvider credentials = null;
    private String repository  = "";
    private String branch      = "";
    private String target      = "";

    Git git = null;

    public GitClient(String repo) {
	this.setRepository(repo);
	init();
    }

    public void setRepository(String repo) {
	this.repository = repo;
    }

    public void setBranch(String branch) {
	this.branch = branch;
    }

    public void setTarget(String target) {
	this.target = target;
    }


    private void init() {

	String home   = consts.getConstants().getProperty("data.home");
	String user   = consts.getConstants().getProperty("git.user");
	String passwd = consts.getConstants().getProperty("git.password");

	try {
	    git = Git.open( new F‌ile( home + "/" + this.repository + "/.git" ) );
	} catch(java.io.IOException repoProblem ) {
	    logger.error("IO prob: " + repoProblem);
	}
	credentials = new UsernamePasswordCredentialsProvider(user,passwd);
    }

    //
    // These are basically local, doesn't seem to understand anything about 
    // authentication.

    // gitDiff or gitLog should do what 
    //
    // git log --name-only --oneline --decorate=no master...
    //
    // or 
    //
    // git diff --name-only my_branch..master
    //
    // does

    public String walkTree (RevCommit commit) {
	Repository repo = git.getRepository();
	TreeWalk treeWalk = new TreeWalk(repo);

	String paths = "";

	try {
	    logger.info("trying walk tree ");
	    treeWalk.addTree( commit.getId() );
	    logger.info("add tree ");
	    while( treeWalk.next() ) {
		logger.info("climbing tree ");
		String path = treeWalk.getPathString();
		logger.info("entering path ");
		logger.info("path = " + path);
		paths = paths + "\n" + path;
	    }
	} catch(org.eclipse.jgit.errors.MissingObjectException missing) {
	    logger.error("Missing object: " + missing);	   
	    return "object sadly missing";
	} catch(org.eclipse.jgit.errors.IncorrectObjectTypeException e) {
	    logger.error("git prob: " + e);
	    return "git incorrect type";
	} catch(org.eclipse.jgit.errors.CorruptObjectException typeProb) {
	    logger.error("git prob: " + typeProb);
	    return "git incorrect type";
	} catch(java.io.IOException e) {
	    logger.error("git prob: " + e);
	    return "git io exception";
	}
	return paths;
    }

    public String gitLog() {
	try {
	    LogCommand log = git.log();
	    Repository repo = git.getRepository();
	    ObjectId from = repo.resolve("master");
	    ObjectId to = repo.resolve(this.branch);
	    log.addRange(from, to);
	    java.lang.Iterable<RevCommit> logList = log.call();
	    java.util.Iterator<RevCommit> liter = logList.iterator();
	    logger.info("Log list:");
	    while(liter.hasNext()) {

		RevCommit commit = liter.next();
		logger.info("The really long ID: " + commit.getId().getName());
		logger.info("Full msg: " + commit.getFullMessage());
		logger.info("as string: " + commit.toString());

		logger.info("Tree as string " + commit.getTree());
		logger.info("walk tree: " + walkTree(commit));

	    }
	    return "Returned from gitLog";
	} catch (org.eclipse.jgit.api.errors.GitAPIException gitProblem) {
	    logger.error("git prob: " + gitProblem);
	    return "git log failed";
	} catch (org.eclipse.jgit.errors.AmbiguousObjectException objectProblem) {
	    logger.error("git ambiguity prob: " + objectProblem);
	    return "git ambiguity";
	} catch(org.eclipse.jgit.errors.IncorrectObjectTypeException e) {
	    logger.error("git prob: " + e);
	    return "git incorrect type";
	} catch(org.eclipse.jgit.errors.MissingObjectException e) {
	    logger.error("git prob: " + e);
	    return "git missing object";
	} catch(java.io.IOException e) {
	    logger.error("git prob: " + e);
	    return "git io exception";
	}
    }

    /*
    public String gitDiff() {
	try {
	    DiffCommand diff = git.diff();
	    Repository repo = git.getRepository();
	    ObjectId from = repo.resolve("master");
	    ObjectId to = repo.resolve(this.branch);
	    diff.addRange(from, to);

	    diff.setShowNameAndStatusOnly();

	    java.lang.Iterable<RevCommit> logList = log.call();
	    java.util.Iterator liter = logList.iterator();
	    String list = "Log list:\n";
	    while(liter.hasNext()) {
		list = list + liter.next() + "\n";
	    }
	    return list;
	} catch (org.eclipse.jgit.api.errors.GitAPIException gitProblem) {
	    logger.error("git prob: " + gitProblem);
	    return "git log failed";
	} catch (org.eclipse.jgit.errors.AmbiguousObjectException objectProblem) {
	    logger.error("git ambiguity prob: " + objectProblem);
	    return "git ambiguity";
	} catch(org.eclipse.jgit.errors.IncorrectObjectTypeException e) {
	    logger.error("git prob: " + e);
	    return "git incorrect type";
	} catch(org.eclipse.jgit.errors.MissingObjectException e) {
	    logger.error("git prob: " + e);
	    return "git missing object";
	} catch(java.io.IOException e) {
	    logger.error("git prob: " + e);
	    return "git missing io exception";
	}
	} 
    */


    public String gitBranches() {
	try {
	    ListBranchCommand branches = git.branchList();
	    branches.setListMode(ListBranchCommand.ListMode.ALL);
	    java.util.List<Ref> res      = branches.call();
	    Iterator<Ref> lister =  res.iterator();
	    String blist = "";
	    while(lister.hasNext()) {
		blist = blist + lister.next() + "\n"; 
	    }
	    return res.toString();
	} catch (org.eclipse.jgit.api.errors.GitAPIException gitProblem) {
	    logger.error("git prob: " + gitProblem);
	    return "git branch failed";
	}
    }

    public String gitCheckOut() {
	try {
	    logger.info("about to check out: " + this.branch);
	    CheckoutCommand co = git.checkout();
	    String local_branch = this.branch.replaceAll("(.*?/)","");
	    logger.info("local_branch: " + local_branch);
	    co.setName(local_branch);
	    logger.info("name set to local_branch");
	    co.setStartPoint(this.branch);
	    logger.info("start point set to " + this.branch);
	    co.setUpstreamMode(org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM);
	    logger.info("Upstream mode");
	    co.setCreateBranch(true);
	    logger.info("create branch");
	    try {
		Ref rsult = co.call();
		logger.info("Done checking out");
		return rsult + "";
	    } catch (org.eclipse.jgit.api.errors.RefAlreadyExistsException branchProbl) {
		logger.error("git branch prob: " + branchProbl);
		co.setCreateBranch(false);
		logger.info("don't create branch");
		Ref rsult = co.call();
		logger.info("Done checking out");
		return rsult + "";
	    }
	} catch (org.eclipse.jgit.api.errors.GitAPIException gitProblem) {
	    logger.error("git prob: " + gitProblem);
	    return "git checkout failed";
	}
    }

    // 
    // Down here we have the ones requiring credentials
    //

    public String gitFetch() {
	try {
	    logger.info("about to fetch: " + this.repository);
	    FetchCommand fetch = git.fetch();

	    fetch.setRemoveDeletedRefs(true);

	    fetch.setCredentialsProvider(credentials);
	    FetchResult res = fetch.call();
	    String all_res = res.getMessages() + "\n" + res.getURI() + "\n";
	    return all_res;
	} catch (org.eclipse.jgit.api.errors.GitAPIException gitProblem) {
	    logger.error("git prob: " + gitProblem);
	    return "git fetch failed";
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
	    return "git pull failed";
	}
    }

    // Other stuff

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