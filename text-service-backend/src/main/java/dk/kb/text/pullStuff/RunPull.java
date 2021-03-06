package dk.kb.text.pullStuff;

import dk.kb.text.ConfigurableConstants;
import dk.kb.text.message.Invocation;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by dgj on 17-11-2016.
 * Stolen by slu on 11-01-2019
 *
 *   We have different branches, locally and
 *   remotely. Two are the ones we work with. One is
 *   in our remote git text repository and one in the
 *   remote repository. Their names are in the String variables
 *
 *   publishedBranch
 *
 *   and
 *
 *   branch
 *
 *   respectively.
 *
 *   The former is in the configuration file the
 *   latter comes in a message from ActiveMQ
 */
public class RunPull {
	/** The logger.*/
	private static Logger logger = Logger.getLogger(RunPull.class);

	/**
	 * Constructor.
	 */
	public RunPull() {}

	/**
	 * Performs the pull.
	 * @param invocation The invocation with the pull operation.
	 * @return The git operation for the endeavour of going from the git work branch to the target branch.
	 */
	public HashMap<String, String> performPull(Invocation invocation) throws IOException {
		logger.info("Setting repository: " + invocation.getRepository());
		GitClient git = new GitClient(invocation.getRepository());
		HashMap<String, String> op = null;
		try {
		    String workBranch = getPublishedBranch(invocation);

		    git.setPublishedBranch(workBranch);
		    git.setBranch(invocation.getBranch());

		    git.gitCheckOutPublished();

		    // OK, first we fetch. We'll basically get everything that has happened since last fetch.
		
		    logger.info(git.gitFetch());

		    // Now we checkout the desired branch and do a pull

		    logger.info(git.gitCheckOut());
		    logger.info(git.gitPull());

		    op = git.gitLog();

		    // Finally we switch the published branch to match the current branch.
		    git.gitSwitchPublished();

		} finally {
		    git.close();
		}
		return op;
	}

	protected String getPublishedBranch(Invocation invocation) {
		String useBranch;
		if(invocation.getTarget().equals("production")) {
			useBranch = "published.branch";
		} else {
			// i.e., target.equals("staging")
			useBranch = "previewed.branch";
		}

		return ConfigurableConstants.getInstance().getConstants().getProperty( useBranch );
	}

}
