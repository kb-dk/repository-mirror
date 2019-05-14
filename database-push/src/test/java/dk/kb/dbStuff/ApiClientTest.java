package dk.kb.dbStuff;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.annotations.Test;

import java.io.IOException;

public class ApiClientTest extends ExtendedTestCase {

	@Test
	public void testInstantiation() {
//		File f = new File("../config.xml");
//		Assert.assertTrue(f.isFile());
//		InputStream in = this.getClass().getResourceAsStream("/xsl/add-id.xsl");
//		Assert.assertNotNull(in);
		ApiClient apiClient = new ApiClient();
	}

	@Test
	public void testTransformText() throws IOException {
		ApiClient apiClient = new ApiClient();
		String transformedText = apiClient.transformText("src/test/resources/text.xml");
		System.err.println("OUTPUT:\n" + transformedText);
	}
}