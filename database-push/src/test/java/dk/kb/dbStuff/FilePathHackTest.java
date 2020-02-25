package dk.kb.dbStuff;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.annotations.Test;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;

public class FilePathHackTest extends ExtendedTestCase {
    private static Logger logger = Logger.getLogger(FilePathHackTest.class);

    @Test
    public void  testGetServicePath() {
	logger.info("Starting test of GetServicePath");
	
	FilePathHack hack = new  dk.kb.dbStuff.FilePathHack();

	// What we get from ActiveMQ:
	// gv;GV;refs/remotes/origin/sigges_test_branch;staging;1815/1815GV/1815_264/1815_264_txt.xml;PUT
	// gv;GV;refs/remotes/origin/sigges_test_branch;staging;1829/1829GV/1829_477A/1829_477A_v0.xml;PUT
	String target = "http://xstorage-host-something.kb.dk:port_number";
	String coll   = "gv";
	String path   = "1815/1815GV/1815_264/1815_264_txt.xml";

	// 1814/1814GV/1814_236/1814_236_txt.xml
	// 1816/1816GV/1816_283/1816_283_com.xml 


	hack.setDatabase(target);
	hack.setCollection(coll);
	hack.setDocument(path);

	logger.info("target, coll and path are " + target + ", " + coll + " and " + path + " respectively");
	logger.info("VALID: " + hack.validDocPath());

	String URI = hack.getServicePath();
	logger.info("OUTPUT:\n" + URI + "\n");

	path = "1829/1829GV/1829_477A/1829_477A_v0.xml";
	hack.setDocument(path);

	URI = hack.getServicePath();
	logger.info("OUTPUT:\n" + URI + "\n");

	String file  = hack.getDocument();

	logger.info("DOC:\n" + file + "\n");

    }
}
