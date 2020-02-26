package dk.kb.pullStuff;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GitClientTest extends ExtendedTestCase {

    String devNull = "/dev/null";

    public File getTestRepo() {
        File gitRepoDir = new File("../../TextServiceTests").getAbsoluteFile();
        if(!gitRepoDir.exists()) {
            throw new SkipException("CANNOT FIND REPO AT: " + gitRepoDir.getAbsolutePath());
        }
        return gitRepoDir;
    }

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
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testBadInstantiation() {
        new GitClient("ThisRepoDoesNotExist" + UUID.randomUUID().toString());
    }

    @Test
    public void testBranchesMasterToStep1() throws Exception {
        addDescription("Testing the GIT diffs and operations between master branch and the branch step1.");
        File gitRepoDir = getTestRepo();
        ConfigurableConstants.getInstance().getConstants().setProperty("data.home", gitRepoDir.getParent());
        GitClient client = new GitClient(gitRepoDir.getName());

        addStep("Setting op to be from master to step1", "");
        String from = "master";
        String to = "step1";
        client.setPublishedBranch(from);
        client.setBranch(to);
        ObjectId fromCheckin =   client.git.getRepository().resolve(from);
        ObjectId toCheckin   =   client.git.getRepository().resolve(to);

        addStep("Looking at the GitDiffs", "Should be 1, which should be an 'ADD' for the file.");
        List<DiffEntry> diffs = client.getDiffs(fromCheckin, toCheckin);
        Assert.assertEquals(diffs.size(), 1);
        Assert.assertEquals(diffs.get(0).getChangeType(), DiffEntry.ChangeType.ADD);
        Assert.assertEquals(diffs.get(0).getNewPath(), "test-file.txt");
        Assert.assertEquals(diffs.get(0).getOldPath(), devNull);

        addStep("Looking at the operations", "Should be 1 which should be a 'PUT'");
        Map<String, String> operations = client.gitLog();
        Assert.assertEquals(operations.size(), 1);
        Assert.assertTrue(operations.containsKey("test-file.txt"));
        Assert.assertEquals(operations.get("test-file.txt"), "PUT");
    }

    @Test
    public void testBranchesMasterToStep2() throws Exception {
        addDescription("Testing the GIT diffs and operations between master branch and the branch step2.");
        File gitRepoDir = getTestRepo();
        ConfigurableConstants.getInstance().getConstants().setProperty("data.home", gitRepoDir.getParent());
        GitClient client = new GitClient(gitRepoDir.getName());

        addStep("Setting op to be from master to step2", "");
        String from = "master";
        String to = "step2";
        client.setPublishedBranch(from);
        client.setBranch(to);
        ObjectId fromCheckin =   client.git.getRepository().resolve(from);
        ObjectId toCheckin   =   client.git.getRepository().resolve(to);

        addStep("Looking at the GitDiffs", "Should be 2, which both should be an 'ADD' for the file.");
        List<DiffEntry> diffs = client.getDiffs(fromCheckin, toCheckin);
        Assert.assertEquals(diffs.size(), 2);
        Assert.assertEquals(diffs.get(0).getChangeType(), DiffEntry.ChangeType.ADD);
        Assert.assertEquals(diffs.get(0).getNewPath(), "test-file.txt");
        Assert.assertEquals(diffs.get(0).getOldPath(), devNull);
        Assert.assertEquals(diffs.get(1).getChangeType(), DiffEntry.ChangeType.ADD);
        Assert.assertEquals(diffs.get(1).getNewPath(), "test-file2.txt");
        Assert.assertEquals(diffs.get(1).getOldPath(), devNull);

        addStep("Looking at the operations", "Should be 2 which both should be a 'PUT'");
        Map<String, String> operations = client.gitLog();
        Assert.assertEquals(operations.size(), 2);
        Assert.assertTrue(operations.containsKey("test-file.txt"));
        Assert.assertEquals(operations.get("test-file.txt"), "PUT");
        Assert.assertTrue(operations.containsKey("test-file2.txt"));
        Assert.assertEquals(operations.get("test-file2.txt"), "PUT");
    }

    @Test
    public void testBranchesStep1ToStep2() throws Exception {
        addDescription("Testing the GIT diffs and operations between the branch step1 and the branch step2.");
        File gitRepoDir = getTestRepo();
        ConfigurableConstants.getInstance().getConstants().setProperty("data.home", gitRepoDir.getParent());
        GitClient client = new GitClient(gitRepoDir.getName());

        addStep("Setting op to be from step1 to step2", "");
        String from = "step1";
        String to = "step2";
        client.setPublishedBranch(from);
        client.setBranch(to);
        ObjectId fromCheckin =   client.git.getRepository().resolve(from);
        ObjectId toCheckin   =   client.git.getRepository().resolve(to);

        addStep("Looking at the GitDiffs", "Should be 2, one with MODIFY and one with PUT");
        List<DiffEntry> diffs = client.getDiffs(fromCheckin, toCheckin);
        Assert.assertEquals(diffs.size(), 2);
        Assert.assertEquals(diffs.get(0).getChangeType(), DiffEntry.ChangeType.MODIFY);
        Assert.assertEquals(diffs.get(0).getNewPath(), "test-file.txt");
        Assert.assertEquals(diffs.get(0).getOldPath(), "test-file.txt");
        Assert.assertEquals(diffs.get(1).getChangeType(), DiffEntry.ChangeType.ADD);
        Assert.assertEquals(diffs.get(1).getNewPath(), "test-file2.txt");
        Assert.assertEquals(diffs.get(1).getOldPath(), devNull);

        addStep("Looking at the operations", "Should be 2 which both should be a 'PUT'");
        Map<String, String> operations = client.gitLog();
        Assert.assertEquals(operations.size(), 2);
        Assert.assertTrue(operations.containsKey("test-file.txt"));
        Assert.assertEquals(operations.get("test-file.txt"), "PUT");
        Assert.assertTrue(operations.containsKey("test-file2.txt"));
        Assert.assertEquals(operations.get("test-file2.txt"), "PUT");
    }

    @Test
    public void testBranchesStep1ToMaster() throws Exception {
        addDescription("Testing the GIT diffs and operations when reverting from the branch step1 to the master branch.");
        File gitRepoDir = getTestRepo();
        ConfigurableConstants.getInstance().getConstants().setProperty("data.home", gitRepoDir.getParent());
        GitClient client = new GitClient(gitRepoDir.getName());

        addStep("Setting op to be from step1 to master", "");
        String from = "step1";
        String to = "master";
        client.setPublishedBranch(from);
        client.setBranch(to);
        ObjectId fromCheckin =   client.git.getRepository().resolve(from);
        ObjectId toCheckin   =   client.git.getRepository().resolve(to);

        addStep("Looking at the GitDiffs", "Should be 1 with DELETE");
        List<DiffEntry> diffs = client.getDiffs(fromCheckin, toCheckin);
        Assert.assertEquals(diffs.size(), 1);
        Assert.assertEquals(diffs.get(0).getChangeType(), DiffEntry.ChangeType.DELETE);
        Assert.assertEquals(diffs.get(0).getNewPath(), devNull);
        Assert.assertEquals(diffs.get(0).getOldPath(), "test-file.txt");

        addStep("Looking at the operations", "Should be 1 with DELETE");
        Map<String, String> operations = client.gitLog();
        Assert.assertEquals(operations.size(), 1);
        Assert.assertTrue(operations.containsKey("test-file.txt"));
        Assert.assertEquals(operations.get("test-file.txt"), "DELETE");
    }

    @Test
    public void testBranchesStep2ToMaster() throws Exception {
        addDescription("Testing the GIT diffs and operations when reverting from the branch step2 to the master branch.");
        File gitRepoDir = getTestRepo();
        ConfigurableConstants.getInstance().getConstants().setProperty("data.home", gitRepoDir.getParent());
        GitClient client = new GitClient(gitRepoDir.getName());

        addStep("Setting op to be from step2 to master", "");
        String from = "step2";
        String to = "master";
        client.setPublishedBranch(from);
        client.setBranch(to);
        ObjectId fromCheckin =   client.git.getRepository().resolve(from);
        ObjectId toCheckin   =   client.git.getRepository().resolve(to);

        addStep("Looking at the GitDiffs", "Should be 2 with DELETE");
        List<DiffEntry> diffs = client.getDiffs(fromCheckin, toCheckin);
        Assert.assertEquals(diffs.size(), 2);
        Assert.assertEquals(diffs.get(0).getChangeType(), DiffEntry.ChangeType.DELETE);
        Assert.assertEquals(diffs.get(0).getNewPath(), devNull);
        Assert.assertEquals(diffs.get(0).getOldPath(), "test-file.txt");
        Assert.assertEquals(diffs.get(1).getChangeType(), DiffEntry.ChangeType.DELETE);
        Assert.assertEquals(diffs.get(1).getNewPath(), devNull);
        Assert.assertEquals(diffs.get(1).getOldPath(), "test-file2.txt");

        addStep("Looking at the operations", "Should be 2 with DELETE");
        Map<String, String> operations = client.gitLog();
        Assert.assertEquals(operations.size(), 2);
        Assert.assertTrue(operations.containsKey("test-file.txt"));
        Assert.assertEquals(operations.get("test-file.txt"), "DELETE");
        Assert.assertTrue(operations.containsKey("test-file2.txt"));
        Assert.assertEquals(operations.get("test-file2.txt"), "DELETE");
    }

    @Test
    public void testBranchesStep2ToStep1() throws Exception {
        addDescription("Testing the GIT diffs and operations when reverting from the branch step2 to the branch step1.");
        File gitRepoDir = getTestRepo();
        ConfigurableConstants.getInstance().getConstants().setProperty("data.home", gitRepoDir.getParent());
        GitClient client = new GitClient(gitRepoDir.getName());

        addStep("Setting op to be from step2 to step1", "");
        String from = "step2";
        String to = "step1";
        client.setPublishedBranch(from);
        client.setBranch(to);
        ObjectId fromCheckin =   client.git.getRepository().resolve(from);
        ObjectId toCheckin   =   client.git.getRepository().resolve(to);

        addStep("Looking at the GitDiffs", "Should be 2, one with MODIFY and one with DELETE");
        List<DiffEntry> diffs = client.getDiffs(fromCheckin, toCheckin);
        Assert.assertEquals(diffs.size(), 2);
        Assert.assertEquals(diffs.get(0).getChangeType(), DiffEntry.ChangeType.MODIFY);
        Assert.assertEquals(diffs.get(0).getNewPath(), "test-file.txt");
        Assert.assertEquals(diffs.get(0).getOldPath(), "test-file.txt");
        Assert.assertEquals(diffs.get(1).getChangeType(), DiffEntry.ChangeType.DELETE);
        Assert.assertEquals(diffs.get(1).getNewPath(), devNull);
        Assert.assertEquals(diffs.get(1).getOldPath(), "test-file2.txt");

        addStep("Looking at the operations", "Should be 2, one with PUT and one with DELETE");
        Map<String, String> operations = client.gitLog();
        Assert.assertEquals(operations.size(), 2);
        Assert.assertTrue(operations.containsKey("test-file.txt"));
        Assert.assertEquals(operations.get("test-file.txt"), "PUT");
        Assert.assertTrue(operations.containsKey("test-file2.txt"));
        Assert.assertEquals(operations.get("test-file2.txt"), "DELETE");
    }


    @Test
    public void testBranchesMasterToStepA() throws Exception {
        addDescription("Testing the GIT diffs and operations between master branch and the branch stepA.");
        File gitRepoDir = getTestRepo();
        ConfigurableConstants.getInstance().getConstants().setProperty("data.home", gitRepoDir.getParent());
        GitClient client = new GitClient(gitRepoDir.getName());

        addStep("Setting op to be from master to stepA", "");
        String from = "master";
        String to = "stepA";
        client.setPublishedBranch(from);
        client.setBranch(to);
        ObjectId fromCheckin =   client.git.getRepository().resolve(from);
        ObjectId toCheckin   =   client.git.getRepository().resolve(to);

        addStep("Looking at the GitDiffs", "Should be 1, which should be an 'ADD' for the file.");
        List<DiffEntry> diffs = client.getDiffs(fromCheckin, toCheckin);
        Assert.assertEquals(diffs.size(), 1);
        Assert.assertEquals(diffs.get(0).getChangeType(), DiffEntry.ChangeType.ADD);
        Assert.assertEquals(diffs.get(0).getNewPath(), "a.txt");
        Assert.assertEquals(diffs.get(0).getOldPath(), devNull);

        addStep("Looking at the operations", "Should be 1 which should be a 'PUT'");
        Map<String, String> operations = client.gitLog();
        Assert.assertEquals(operations.size(), 1);
        Assert.assertTrue(operations.containsKey("a.txt"));
        Assert.assertEquals(operations.get("a.txt"), "PUT");
    }

    @Test
    public void testBranchesStepAToMaster() throws Exception {
        addDescription("Testing the GIT diffs and operations between the branch stepA and the master branch.");
        File gitRepoDir = getTestRepo();
        ConfigurableConstants.getInstance().getConstants().setProperty("data.home", gitRepoDir.getParent());
        GitClient client = new GitClient(gitRepoDir.getName());

        addStep("Setting op to be from stepA to master", "");
        String from = "stepA";
        String to = "master";
        client.setPublishedBranch(from);
        client.setBranch(to);
        ObjectId fromCheckin =   client.git.getRepository().resolve(from);
        ObjectId toCheckin   =   client.git.getRepository().resolve(to);

        addStep("Looking at the GitDiffs", "Should be 1, which should be an 'DELETE' for the file.");
        List<DiffEntry> diffs = client.getDiffs(fromCheckin, toCheckin);
        Assert.assertEquals(diffs.size(), 1);
        Assert.assertEquals(diffs.get(0).getChangeType(), DiffEntry.ChangeType.DELETE);
        Assert.assertEquals(diffs.get(0).getNewPath(), devNull);
        Assert.assertEquals(diffs.get(0).getOldPath(), "a.txt");

        addStep("Looking at the operations", "Should be 1 which should be a 'Delete'");
        Map<String, String> operations = client.gitLog();
        Assert.assertEquals(operations.size(), 1);
        Assert.assertTrue(operations.containsKey("a.txt"));
        Assert.assertEquals(operations.get("a.txt"), "DELETE");
    }

    @Test
    public void testBranchesStepAToStep1() throws Exception {
        addDescription("Testing the GIT diffs and operations between the branch stepA and the branch step1.");
        File gitRepoDir = getTestRepo();
        ConfigurableConstants.getInstance().getConstants().setProperty("data.home", gitRepoDir.getParent());
        GitClient client = new GitClient(gitRepoDir.getName());

        addStep("Setting op to be from stepA to step1", "");
        String from = "stepA";
        String to = "step1";
        client.setPublishedBranch(from);
        client.setBranch(to);
        ObjectId fromCheckin =   client.git.getRepository().resolve(from);
        ObjectId toCheckin   =   client.git.getRepository().resolve(to);

        addStep("Looking at the GitDiffs", "Should be 2, one with 'ADD' and one with 'DELETE.");
        List<DiffEntry> diffs = client.getDiffs(fromCheckin, toCheckin);
        Assert.assertEquals(diffs.size(), 2);
        Assert.assertEquals(diffs.get(0).getChangeType(), DiffEntry.ChangeType.DELETE);
        Assert.assertEquals(diffs.get(0).getNewPath(), devNull);
        Assert.assertEquals(diffs.get(0).getOldPath(), "a.txt");
        Assert.assertEquals(diffs.get(1).getChangeType(), DiffEntry.ChangeType.ADD);
        Assert.assertEquals(diffs.get(1).getNewPath(), "test-file.txt");
        Assert.assertEquals(diffs.get(1).getOldPath(), devNull);

        addStep("Looking at the operations", "Should be 2, one with 'DELETE' and one with 'PUT'");
        Map<String, String> operations = client.gitLog();
        Assert.assertEquals(operations.size(), 2);
        Assert.assertTrue(operations.containsKey("a.txt"));
        Assert.assertEquals(operations.get("a.txt"), "DELETE");
        Assert.assertTrue(operations.containsKey("test-file.txt"));
        Assert.assertEquals(operations.get("test-file.txt"), "PUT");
    }

    @Test
    public void testBranchesStep1ToStepA() throws Exception {
        addDescription("Testing the GIT diffs and operations between the branch step1 and the branch stepA.");
        File gitRepoDir = getTestRepo();
        ConfigurableConstants.getInstance().getConstants().setProperty("data.home", gitRepoDir.getParent());
        GitClient client = new GitClient(gitRepoDir.getName());

        addStep("Setting op to be from step1 to stepA", "");
        String from = "step1";
        String to = "stepA";
        client.setPublishedBranch(from);
        client.setBranch(to);
        ObjectId fromCheckin =   client.git.getRepository().resolve(from);
        ObjectId toCheckin   =   client.git.getRepository().resolve(to);

        addStep("Looking at the GitDiffs", "Should be 2, one with 'ADD' and one with 'DELETE.");
        List<DiffEntry> diffs = client.getDiffs(fromCheckin, toCheckin);
        Assert.assertEquals(diffs.size(), 2);
        Assert.assertEquals(diffs.get(0).getChangeType(), DiffEntry.ChangeType.ADD);
        Assert.assertEquals(diffs.get(0).getNewPath(), "a.txt");
        Assert.assertEquals(diffs.get(0).getOldPath(), devNull);
        Assert.assertEquals(diffs.get(1).getChangeType(), DiffEntry.ChangeType.DELETE);
        Assert.assertEquals(diffs.get(1).getNewPath(), devNull);
        Assert.assertEquals(diffs.get(1).getOldPath(), "test-file.txt");

        addStep("Looking at the operations", "Should be 2, one with 'DELETE' and one with 'PUT'");
        Map<String, String> operations = client.gitLog();
        Assert.assertEquals(operations.size(), 2);
        Assert.assertTrue(operations.containsKey("a.txt"));
        Assert.assertEquals(operations.get("a.txt"), "PUT");
        Assert.assertTrue(operations.containsKey("test-file.txt"));
        Assert.assertEquals(operations.get("test-file.txt"), "DELETE");
    }

    @Test
    public void testBranchesStepAToStep2() throws Exception {
        addDescription("Testing the GIT diffs and operations between the branch stepA and the branch step2.");
        File gitRepoDir = getTestRepo();
        ConfigurableConstants.getInstance().getConstants().setProperty("data.home", gitRepoDir.getParent());
        GitClient client = new GitClient(gitRepoDir.getName());

        addStep("Setting op to be from stepA to step2", "");
        String from = "stepA";
        String to = "step2";
        client.setPublishedBranch(from);
        client.setBranch(to);
        ObjectId fromCheckin =   client.git.getRepository().resolve(from);
        ObjectId toCheckin   =   client.git.getRepository().resolve(to);

        addStep("Looking at the GitDiffs", "Should be 3, two with 'ADD' and one with 'DELETE.");
        List<DiffEntry> diffs = client.getDiffs(fromCheckin, toCheckin);
        Assert.assertEquals(diffs.size(), 3);
        Assert.assertEquals(diffs.get(0).getChangeType(), DiffEntry.ChangeType.DELETE);
        Assert.assertEquals(diffs.get(0).getNewPath(), devNull);
        Assert.assertEquals(diffs.get(0).getOldPath(), "a.txt");
        Assert.assertEquals(diffs.get(1).getChangeType(), DiffEntry.ChangeType.ADD);
        Assert.assertEquals(diffs.get(1).getNewPath(), "test-file.txt");
        Assert.assertEquals(diffs.get(1).getOldPath(), devNull);
        Assert.assertEquals(diffs.get(2).getChangeType(), DiffEntry.ChangeType.ADD);
        Assert.assertEquals(diffs.get(2).getNewPath(), "test-file2.txt");
        Assert.assertEquals(diffs.get(2).getOldPath(), devNull);

        addStep("Looking at the operations", "Should be 3, one with 'DELETE' and two with 'PUT'");
        Map<String, String> operations = client.gitLog();
        Assert.assertEquals(operations.size(), 3);
        Assert.assertTrue(operations.containsKey("a.txt"));
        Assert.assertEquals(operations.get("a.txt"), "DELETE");
        Assert.assertTrue(operations.containsKey("test-file.txt"));
        Assert.assertEquals(operations.get("test-file.txt"), "PUT");
        Assert.assertTrue(operations.containsKey("test-file2.txt"));
        Assert.assertEquals(operations.get("test-file2.txt"), "PUT");
    }

    @Test
    public void testBranchesStep2ToStepA() throws Exception {
        addDescription("Testing the GIT diffs and operations between the branch stepA and the branch step2.");
        File gitRepoDir = getTestRepo();
        ConfigurableConstants.getInstance().getConstants().setProperty("data.home", gitRepoDir.getParent());
        GitClient client = new GitClient(gitRepoDir.getName());

        addStep("Setting op to be from step2 to stepA", "");
        String from = "step2";
        String to = "stepA";
        client.setPublishedBranch(from);
        client.setBranch(to);
        ObjectId fromCheckin =   client.git.getRepository().resolve(from);
        ObjectId toCheckin   =   client.git.getRepository().resolve(to);

        addStep("Looking at the GitDiffs", "Should be 3, one with 'ADD' and two with 'DELETE.");
        List<DiffEntry> diffs = client.getDiffs(fromCheckin, toCheckin);
        Assert.assertEquals(diffs.size(), 3);
        Assert.assertEquals(diffs.get(0).getChangeType(), DiffEntry.ChangeType.ADD);
        Assert.assertEquals(diffs.get(0).getNewPath(), "a.txt");
        Assert.assertEquals(diffs.get(0).getOldPath(), devNull);
        Assert.assertEquals(diffs.get(1).getChangeType(), DiffEntry.ChangeType.DELETE);
        Assert.assertEquals(diffs.get(1).getNewPath(), devNull);
        Assert.assertEquals(diffs.get(1).getOldPath(), "test-file.txt");
        Assert.assertEquals(diffs.get(2).getChangeType(), DiffEntry.ChangeType.DELETE);
        Assert.assertEquals(diffs.get(2).getNewPath(), devNull);
        Assert.assertEquals(diffs.get(2).getOldPath(), "test-file2.txt");

        addStep("Looking at the operations", "Should be 3, two with 'DELETE' and one with 'PUT'");
        Map<String, String> operations = client.gitLog();
        Assert.assertEquals(operations.size(), 3);
        Assert.assertTrue(operations.containsKey("a.txt"));
        Assert.assertEquals(operations.get("a.txt"), "PUT");
        Assert.assertTrue(operations.containsKey("test-file.txt"));
        Assert.assertEquals(operations.get("test-file.txt"), "DELETE");
        Assert.assertTrue(operations.containsKey("test-file2.txt"));
        Assert.assertEquals(operations.get("test-file2.txt"), "DELETE");
    }

}