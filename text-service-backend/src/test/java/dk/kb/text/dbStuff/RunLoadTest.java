package dk.kb.text.dbStuff;

import dk.kb.text.ConfigurableConstants;
import dk.kb.text.message.Invocation;
import dk.kb.text.message.ResponseMediator;
import dk.kb.text.pullStuff.GitClient;
import org.jaccept.structure.ExtendedTestCase;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

public class RunLoadTest extends ExtendedTestCase {

    String gitRepoDir = "TextServiceTests";
    String gitBranch = "stepA";

    String collection = gitRepoDir;
    String repository = gitRepoDir;
    String branch = gitBranch;
    String target = "production";

    @BeforeClass
    public void setupMethod() {
        GitClient client = new GitClient(gitRepoDir);
        client.gitCheckOutBranch("origin/" + gitBranch);
    }

    @Test
    public void testEmptyOperation() throws Exception {
        addDescription("Test handling an empty operation");

        ApiClient apiClient = mock(ApiClient.class);
        Invocation invocation = new Invocation(collection, repository, branch, target);
        ResponseMediator responseMediator = mock(ResponseMediator.class);

        RunLoad runLoad = new RunLoad(apiClient, invocation, responseMediator);

        runLoad.handleOperation("document", "EMPTY");

        verify(apiClient).setLogin(anyString(), anyString(), anyString(), anyInt(), anyString());
        verifyNoMoreInteractions(apiClient);

        verify(responseMediator).sendMessage(eq(RunLoad.EMPTY_OPERATION_MESSAGE));
        verifyNoMoreInteractions(responseMediator);
    }

    // TODO: make a test for the invalid path -> GV bad filename format
    @Test(enabled = false)
    public void testInvalidPathOperation() throws Exception {
        addDescription("Testing the invalid path");

    }

    @Test
    public void testPutOperationSuccess() throws Exception {
        addDescription("Test the successful put operation");

        ApiClient apiClient = mock(ApiClient.class);
        Invocation invocation = new Invocation(collection, repository, branch, target);
        ResponseMediator responseMediator = mock(ResponseMediator.class);

        when(apiClient.restGet(anyString())).thenReturn("HTTP: 200");
        when(apiClient.getHttpHeader(anyString())).thenReturn("The Freudian ID for this volume!");

        addStep("Run the load", "Should perform a put operation. "
                + "First deleting, then pushing the new stuff.");
        RunLoad runLoad = new RunLoad(apiClient, invocation, responseMediator);
        runLoad.handleOperation("a.txt", "PUT");

        verify(apiClient).setLogin(anyString(), anyString(), anyString(), anyInt(), anyString());
        verify(apiClient).restPut(anyString(), anyString());
        verify(apiClient).restGet(anyString());
        verify(apiClient).getHttpHeader(eq(RunLoad.HEAD_VOLUME_ID_REQUEST));
        verify(apiClient, times(2)).restPost(anyString(), anyString());
        verifyNoMoreInteractions(apiClient);

        // responseMediator.sendMessage( "Reindexed '" + document + "' with volume id " + volumeId + "\n");
        verify(responseMediator, times(2)).sendMessage(anyString());
        verifyNoMoreInteractions(responseMediator);
    }

    @Test
    public void testDeleteOperationSuccess() throws Exception {
        addDescription("Test the successful delete operation");

        ApiClient apiClient = mock(ApiClient.class);
        Invocation invocation = new Invocation(collection, repository, branch, target);
        ResponseMediator responseMediator = mock(ResponseMediator.class);

        when(apiClient.restGet(anyString())).thenReturn("HTTP: 200");
        when(apiClient.getHttpHeader(anyString())).thenReturn("The Freudian ID for this volume!");

        addStep("Run the load", "Should perform a put operation. "
                + "First deleting, then pushing the new stuff.");
        RunLoad runLoad = new RunLoad(apiClient, invocation, responseMediator);
        runLoad.handleOperation("test-file.txt", "DELETE");

        verify(apiClient).setLogin(anyString(), anyString(), anyString(), anyInt(), anyString());
        verify(apiClient).restPost(anyString(), anyString());
        verify(apiClient).restDelete(anyString());
        verifyNoMoreInteractions(apiClient);

        // responseMediator.sendMessage( "Reindexed '" + document + "' with volume id " + volumeId + "\n");
        verify(responseMediator, times(2)).sendMessage(anyString());
        verifyNoMoreInteractions(responseMediator);
    }


}
