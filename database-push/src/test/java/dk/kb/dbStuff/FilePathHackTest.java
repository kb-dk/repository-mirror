package dk.kb.dbStuff;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.annotations.Test;

import java.io.IOException;

public class FilePathHackTest extends ExtendedTestCase {

	@Test
	public void testGetServicePath() {
		FilePathHack hack = new FilePathHack();

		// What we get from ActiveMQ:
		// gv;GV;refs/remotes/origin/sigges_test_branch;staging;1815/1815GV/1815_264/1815_264_txt.xml;PUT
		// gv;GV;refs/remotes/origin/sigges_test_branch;staging;1829/1829GV/1829_477A/1829_477A_v0.xml;PUT
		String target = "gv";
		String coll   = "GV";
		String path   = "1815/1815GV/1815_264/1815_264_txt.xml";

		// 1814/1814GV/1814_236/1814_236_txt.xml
		// 1816/1816GV/1816_283/1816_283_com.xml 


		hack.setTarget(target);
		hack.setCollection(coll);
		hack.setDocument(path);

		System.err.println("VALID: " + hack.validDocPath());

		String URI = hack.getServicePath();
		System.err.println("OUTPUT:\n" + URI + "\n");

		path = "1829/1829GV/1829_477A/1829_477A_v0.xml";
		hack.setDocument(path);

		URI = hack.getServicePath();
		System.err.println("OUTPUT:\n" + URI + "\n");

		String file  = hack.getDocument();

		System.err.println("DOC:\n" + file + "\n");

	}
}