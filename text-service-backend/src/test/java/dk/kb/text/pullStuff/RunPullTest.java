package dk.kb.text.pullStuff;

import dk.kb.text.ConfigurableConstants;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class RunPullTest extends ExtendedTestCase {

    @Test
    public void testRunPull() throws IOException {
        addDescription("Testing the GIT diffs and operations between master branch and the branch step1.");
        File gitRepoDir = GitClientTest.getTestRepo();
        ConfigurableConstants.getInstance().getConstants().setProperty("data.home", gitRepoDir.getParent());

        String collection = UUID.randomUUID().toString();
        String target = "production";
        String repository = gitRepoDir.getName();
        String branch = "step2";

        Invocation invocation = new Invocation(collection, repository, branch, target);
        RunPull runPull = new RunPull();

        addStep("Ensure that the published branch starts at 'stepA'", "");
        GitClient client = new GitClient(gitRepoDir.getName());
        client.gitCheckOutBranch("origin/stepA");
        client.setBranch("stepA");
        client.setPublishedBranch(runPull.getPublishedBranch(invocation));
        client.gitSwitchPublished();

        Assert.assertEquals(client.git.getRepository().getBranch(), "stepA");

        addStep("Perform pull between ", "");
        Map<String, String> op = runPull.performPull(invocation);

        Assert.assertEquals(op.size(), 3);
        Assert.assertTrue(op.containsKey("test-file2.txt"));
        Assert.assertEquals(op.get("test-file2.txt"), "PUT");
        Assert.assertTrue(op.containsKey("test-file.txt"));
        Assert.assertEquals(op.get("test-file.txt"), "PUT");
        Assert.assertTrue(op.containsKey("a.txt"));
        Assert.assertEquals(op.get("a.txt"), "DELETE");

        Assert.assertEquals(client.git.getRepository().getBranch(), "step2");
    }
}
