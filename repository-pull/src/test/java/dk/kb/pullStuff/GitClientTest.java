package dk.kb.pullStuff;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.UUID;

public class GitClientTest extends ExtendedTestCase {

    @Test//(enabled = false)
    public void testInstantiation() {
	addDescription("This is testing if the code is living in a git project");
        File dir = new File("../..");

        ConfigurableConstants.getInstance().getConstants().setProperty("data.home", dir.getAbsoluteFile().getAbsolutePath());

        GitClient client = new GitClient("repository-mirror");

        Assert.assertNotNull(client.git);

        String branches= client.gitBranches();
        Assert.assertNotNull(branches);
        Assert.assertTrue(branches.contains("master"), "must contain a master branch");
        // System.err.println(branches);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testBadInstantiation() {
        GitClient client = new GitClient("ThisRepoDoesNotExist" + UUID.randomUUID().toString());
    }
}