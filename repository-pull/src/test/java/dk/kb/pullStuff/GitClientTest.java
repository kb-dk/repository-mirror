package dk.kb.pullStuff;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.UUID;

public class GitClientTest extends ExtendedTestCase {

    @Test
    public void testInstantiation() {
        ConfigurableConstants.getInstance().getConstants().setProperty("data.home", "/home/jolf/git/");
        GitClient client = new GitClient("Yggdrasil");

        Assert.assertNotNull(client.git);
//        client.

        System.err.println(client.gitBranches());
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testBadInstantiation() {
        GitClient client = new GitClient("ThisRepoDoesNotExist" + UUID.randomUUID().toString());
    }
}