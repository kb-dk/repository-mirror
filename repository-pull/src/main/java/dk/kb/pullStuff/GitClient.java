package dk.kb.pullStuff;

import org.eclipse.jgit.api.*;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.revwalk.*;
import org.eclipse.jgit.treewalk.*;
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

    private String repository       = "";
    private String branch           = "";
    private String published_branch = "";
    private String target           = "";

    String branchId = "";

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

    public void setPublishedBranch(String branch) {
	this.published_branch = branch;
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



    public  java.util.HashMap<String,String> gitLog() {
	java.util.HashMap<String,String> op = new java.util.HashMap<String,String>();
	try {
	    LogCommand log =  git.log();
	    Repository repo = git.getRepository();

	    ObjectId from =   repo.resolve(this.branch);
	    ObjectId to   =   repo.resolve(this.published_branch);

	    op = listDiff(repo,from,to);

	} catch (org.eclipse.jgit.api.errors.GitAPIException gitProblem) {
	    logger.error("git prob: " + gitProblem);
	} catch (org.eclipse.jgit.errors.AmbiguousObjectException objectProblem) {
	    logger.error("git ambiguity prob: " + objectProblem);
	} catch(org.eclipse.jgit.errors.IncorrectObjectTypeException e) {
	    logger.error("git prob: " + e);
	} catch(org.eclipse.jgit.errors.MissingObjectException e) {
	    logger.error("git prob: " + e);
	} catch(java.io.IOException e) {
	    logger.error("git prob: " + e);
	}
	return op;
    }

    /* Borrowed from dstadler's jgit-cookbook: https://bit.ly/2S7ihzj */


    private java.util.HashMap<String,String> listDiff(Repository repo,
			  ObjectId oldCommit, 
			  ObjectId newCommit) throws org.eclipse.jgit.api.errors.GitAPIException, java.io.IOException {

         java.util.List<DiffEntry> diffs = git.diff()
	    .setOldTree(prepareTreeParser(repo, oldCommit))
	    .setNewTree(prepareTreeParser(repo, newCommit))
	    .call();

	 java.util.HashMap<String,String> operations = new java.util.HashMap<String,String>() ;

	 logger.info("Found: " + diffs.size() + " differences");

	 /*
	   The possible GIT diff types are:

	   ADD    Add a new file to the project
	   COPY   Copy an existing file to a new location, keeping the original
	   DELETE Delete an existing file from the project
	   MODIFY Modify an existing file in the project (content and/or mode)
	   RENAME Rename an existing file to a new location

	 */

        for (DiffEntry diff : diffs) {

	    String type = diff.getChangeType() + "";

	    logger.info("**********");

	    if(type.equals("ADD") || type.equals("MODIFY") || type.equals("COPY")) {
		String file = diff.getNewPath() + ""; 
		String method = "PUT";
		operations.put(file,method);
		logger.info("type=" + type + " We'll " + method + " " + file);
	    } else if(type.equals("RENAME")) {
		String file = diff.getOldPath() + ""; 
		String method = "DELETE";

		logger.info("type=" + type + " We'll " + method + " " + file);
		operations.put(file,method);

		file = diff.getNewPath() + ""; 
		method = "PUT";

		logger.info("type=" + type + " We'll " + method + " " + file);
		operations.put(file,method);

	    } else if(type.equals("DELETE")) {
		String file = diff.getOldPath() + ""; 
		String method = "DELETE";

		logger.info("type=" + type + " We'll " + method + " " + file);
		operations.put(file,method);

	    }

	    logger.info("Type="+diff.getChangeType() + "\nNew path=" +  diff.getNewPath() + "\nOld Path=" +  diff.getOldPath());

            logger.info("Diff: " + diff.getChangeType() + ": " +
                    (diff.getOldPath().equals(diff.getNewPath()) ? diff.getNewPath() : diff.getOldPath() + " -> " + diff.getNewPath()));
        }
	return operations;
    }

   private AbstractTreeIterator prepareTreeParser(Repository repo,  ObjectId objId) throws java.io.IOException  {
        // from the commit we can build the tree which allows us to construct the TreeParser
        // noinspection Duplicates

	RevWalk walk = new RevWalk(repo);
	RevCommit commit = walk.parseCommit(objId);
	RevTree tree = walk.parseTree(commit.getTree().getId());

	CanonicalTreeParser treeParser = new CanonicalTreeParser();

	ObjectReader reader = repo.newObjectReader();
	treeParser.reset(reader, tree.getId());

	walk.dispose();

	return treeParser;

    }

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
	return gitCheckOutBranch(this.branch);
    }

    public String gitCheckOutPublished() {
	return gitCheckOutBranch(this.published_branch);
    }

    public String gitCheckOutBranch(String my_branch) {
	logger.info("about to check out: " + my_branch);

	try {
	    CheckoutCommand co = git.checkout();

	    String local_branch = my_branch.replaceAll("(.*?/)","");
	    logger.info("local_branch: " + local_branch);
	    co.setName(local_branch);
	    logger.info("name set to local_branch");
	    co.setStartPoint(this.branch);
	    logger.info("start point set to " + my_branch);

	    co.setUpstreamMode(org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM);

	    logger.info("Upstream mode");
	    co.setCreateBranch(true);
	    logger.info("create branch");
	    try {
		Ref rsult = co.call();
		this.branchId = rsult.getObjectId().toString();
		logger.debug("Done checking out");
		return rsult + "";
	    } catch (org.eclipse.jgit.api.errors.RefAlreadyExistsException branchProbl) {
		logger.debug("not really a git branch problem " + branchProbl);
		co.setCreateBranch(false);
		logger.info("branch already created");
		Ref rsult = co.call();
		this.branchId = rsult.getObjectId().toString();
		logger.debug("Done checking out");
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
	    logger.debug("about to fetch: " + this.repository);
	    FetchCommand fetch = git.fetch();

	    fetch.setRemoveDeletedRefs(true);

	    fetch.setCredentialsProvider(credentials);
	    FetchResult res = fetch.call();
	    String all_res = res.getMessages() + " " + res.getURI() + "\n";
	    logger.info("fetching " + all_res);
	    return "git fetch succeeded";
	} catch (org.eclipse.jgit.api.errors.GitAPIException gitProblem) {
	    logger.error("git prob: " + gitProblem);
	    return "git fetch failed";
	}
    }

    public String gitPull() {
	// We choose the remote
	return gitPullFromBranch(this.branch);
    }

    public String gitPullFromBranch(String branch) {

	String local_name = branch.replaceAll("(.*?/)","");

	try {
	    PullResult res = git.pull()
		.setCredentialsProvider(credentials)
		.setRemoteBranchName(local_name)
		.call();
	    return res.toString();
	} catch (org.eclipse.jgit.api.errors.GitAPIException gitProblem) {
	    logger.error("git prob: " + gitProblem);
	    return "git pull failed";
	}
    }

    public String gitMergeToPublished(String branch) {

	String local_name = branch.replaceAll("(.*?/)","");

	try {
	    MergeCommand mgCmd = git.merge();
	    Repository repo = git.getRepository();
	    mgCmd.include(repo.resolve(this.branch)); 
	    MergeResult res = mgCmd.call(); 
	    return res.toString();
	} catch (org.eclipse.jgit.errors.IncorrectObjectTypeException objectTypeProb) {
	    logger.error("git prob: " + objectTypeProb);
	    return "git pull failed";
	} catch (org.eclipse.jgit.errors.AmbiguousObjectException ambiguityProb) {
	    logger.error("git prob: " + ambiguityProb);
	    return "git pull failed";
	} catch (org.eclipse.jgit.api.errors.GitAPIException gitProblem) {
	    logger.error("git prob: " + gitProblem);
	    return "git pull failed";
	} catch(java.io.IOException repoProblem ) {
	    logger.error("IO prob: " + repoProblem);
	    return "git pull failed";
	}

    }



    // Other stuff

    private static Logger configureLog4j() {

	String level = consts.getConstants().getProperty("queue.loglevel");
	if (System.getProperty("queue.loglevel") != null ) level = System.getProperty("queue.loglevel");

	String file = consts.getConstants().getProperty("queue.logfile");
	if (System.getProperty("queue.logfile") != null) file = System.getProperty("queue.logfile");

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
	logger.info("logging at level " + level + " in file " + file + "\n");
	return logger;
    }

 

}