package dk.kb.dbStuff;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.lang.ProcessBuilder;

public class DirtyPutHack {

    public DirtyPutHack() {}

    public String restUpload(String method, String file, String URI, String user, String passwd) throws IOException {

	String result = "";

	try {

	    //Build command 
	    String[] commands = {
		"/bin/sh",
		"-c",
		"(/usr/bin/lwp-request -H Content-type:text/xml -m " + method + " -s -C" +user + ":" + passwd + " " + URI + " < "+file +")"};

	    System.out.println(commands);
	    //Run macro on target
	    ProcessBuilder pb = new ProcessBuilder(commands);
	    //	    pb.directory(new File("/home/text-service/"));
	    pb.redirectErrorStream(true);
	    Process process = pb.start();

	    //Read output
	    StringBuilder out = new StringBuilder();
	    BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
	    String line = null, previous = null;
	    while ((line = br.readLine()) != null)
		if (!line.equals(previous)) {
		    previous = line;
		    out.append(line).append('\n');
		    result = result + line;
		}

	    //Check result
	    if (process.waitFor() == 0) {
		result = result + " Success!";
	    }

	    //Abnormal termination: Log command parameters and output and throw ExecutionException

	    result = result + commands;
	    result = result + out.toString();

	} catch (InterruptedException interuption) {
	    // interuption
	}
 
	return result;

    }
}
