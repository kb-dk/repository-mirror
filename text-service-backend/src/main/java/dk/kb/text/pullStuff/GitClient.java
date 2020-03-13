package dk.kb.text.pullStuff;

import dk.kb.text.ConfigurableConstants;
import dk.kb.text.utils.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class GitClient {

	private static ConfigurableConstants consts = ConfigurableConstants.getInstance();
	private static Logger logger = configureLog4j();
	private CredentialsProvider credentials = null;

	private String repository       = "";
	private String branch           = "";
	private String published_branch = "";

	String branchId = "";

	Git git = null;

	public GitClient(String repo) {
		this.setRepository(repo);
		init();
	}

	public void setRepository(String repo) {
		StringUtils.validateString(repo, "repo");
		this.repository = repo;
	}

	public void setBranch(String branch) {
		StringUtils.validateString(branch, "branch");
		this.branch = branch;
	}

	public void setPublishedBranch(String branch) {
		StringUtils.validateString(branch, "branch");
		this.published_branch = branch;
		if(!gitBranches().contains(branch)) {
			createBranch(branch);
		}
	}

	private void init() {

		String home   = consts.getConstants().getProperty("data.home");
		String user   = consts.getConstants().getProperty("git.user");
		String passwd = consts.getConstants().getProperty("git.password");

		File gitFolder = new File( home + "/" + this.repository + "/.git" );

		try {
			git = Git.open(gitFolder);
		} catch(IOException repoProblem ) {
			logger.error("IO prob: ", repoProblem);
		}
		if(git == null) {
			throw new IllegalStateException("Could not instantiate the Git repo at: '" + gitFolder.getAbsolutePath()
					+ "'.");
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



	public HashMap<String,String> gitLog() {
		try {
			Repository repo = git.getRepository();

			logger.info("Git difference between branches: " + this.branch + " and " + this.published_branch);

			ObjectId from =   repo.resolve(branch);
			ObjectId to   =   repo.resolve(published_branch);

			return listOperations(to,from);
		} catch (GitAPIException | IOException e) {
			logger.error("Exception caught while looking at the git log.", e);
			throw new IllegalStateException("Failed to retrieve git log.", e);
		}
	}

	/* Borrowed from dstadler's jgit-cookbook: https://bit.ly/2S7ihzj */

	/**
	 * Retrieves the diffs between two commits potential on different branches.
	 * @param oldCommit From commit.
	 * @param newCommit To commit.
	 * @return The list of git-diffs.
	 * @throws GitAPIException Git error
	 * @throws IOException IO error
	 */
	protected List<DiffEntry> getDiffs(ObjectId oldCommit, ObjectId newCommit) throws GitAPIException, IOException {
		if(oldCommit == null) {
			throw new IllegalArgumentException("oldCommit is null");
		}
		if(newCommit == null) {
			throw new IllegalArgumentException("newCommit is null");
		}

		return git.diff()
				.setOldTree(prepareTreeParser(git.getRepository(), oldCommit))
				.setNewTree(prepareTreeParser(git.getRepository(), newCommit))
				.call();
	}

	/**
	 * Lists the different operations to traverse between the 'from commit' to the 'to commit'.
	 *
	 * @param oldCommit The from commit.
	 * @param newCommit The to commit.
	 * @return The list of operations on which files.
	 * @throws GitAPIException Git error
	 * @throws IOException IO error
	 */
	protected HashMap<String,String> listOperations(ObjectId oldCommit, ObjectId newCommit)
			throws GitAPIException, IOException {
	    
		List<DiffEntry> diffs = getDiffs(oldCommit, newCommit);

		HashMap<String,String> operations = new HashMap<>() ;

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

	private AbstractTreeIterator prepareTreeParser(Repository repo,  ObjectId objId) throws IOException  {
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
		} catch (GitAPIException e) {
			logger.error("Error while retrieving branches: ", e);
			throw new IllegalStateException("", e);
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
			co.setForce(true);

			co.setStartPoint(my_branch);
			logger.info("start point set to " + my_branch);


			co.setCreateBranch(true);
			co.setUpstreamMode(org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM);

			logger.info("create branch");
			try {
				Ref rsult = co.call();
				this.branchId = rsult.getObjectId().toString();
				logger.debug("Done checking out");
				return rsult + "";
			} catch (org.eclipse.jgit.api.errors.RefAlreadyExistsException branchProbl) {
				logger.info("not really a git branch problem " + branchProbl);
				co.setCreateBranch(false);
				co.setName(local_branch);
				co.setForce(true);
				co.setStartPoint(my_branch);
				co.setUpstreamMode(org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM);
				Ref rsult = co.call();
				this.branchId = rsult.getObjectId().toString();
				logger.debug("Done checking out");
				return rsult + "";
			}
		} catch (org.eclipse.jgit.api.errors.GitAPIException gitProblem) {
			logger.error("git prob: ", gitProblem);
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
			logger.error("git prob: ", gitProblem);
			return "git fetch failed";
		}
	}

	public String gitPull() {
		// We choose the remote
		return gitPullFromBranch(this.branch);
	}

	public String gitPullFromBranch(String branch) {

	    String local_name = branch; //.replaceAll("(.*?/)","");

		try {
			PullResult res = git.pull()
			    .setCredentialsProvider(credentials)
			    //			    .setRemote("origin")
			    .setRemoteBranchName(local_name)
			    .call();
			
			return res.toString();
		} catch (org.eclipse.jgit.api.errors.GitAPIException gitProblem) {
			logger.error("git prob: " + gitProblem + " trying to pull from " + local_name, gitProblem);
			return "git pull failed";
		}
	}

	public String gitResetTo(String branch) {
		try {
			Ref res = git.reset().setRef(branch).setMode(ResetCommand.ResetType.HARD).call();
			return res.toString();
		}  catch (org.eclipse.jgit.api.errors.GitAPIException gitProblem) {
			logger.error("git prob: ", gitProblem);
			return "git reset failed";
		}
	}

	public void gitSwitchPublished() {
		try {
			gitCheckOutBranch(branch);
			deleteBranch(published_branch);
			createBranch(published_branch);
		} catch (GitAPIException e) {
			logger.info("git prob: ", e);
			throw new IllegalStateException("Failed to switch branches.", e);
		}
	}

	/**
	 * Creates a branch at the current point with the given name.
	 * @param branchName The name of the branch.
	 */
	protected void createBranch(String branchName) {
		try {
			git.branchCreate().setName(branchName).call();
		} catch (GitAPIException e) {
			throw new IllegalStateException("Failed to instantiate branch with name '" + branchName + "'", e);
		}
	}

	/**
	 * Deletes the given branch. Will use force!
	 * @param branchName The name of the branch to delete.
	 * @throws GitAPIException If it fails to delete the branch.
	 */
	protected void deleteBranch(String branchName) throws GitAPIException {
		git.branchDelete().setBranchNames(branchName).setForce(true).call();
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
