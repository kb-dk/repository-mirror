package dk.kb.text;

import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: jac
 * Date: 8/23/11
 * Time: 1:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConfigurableConstants{

     private static Logger logger = Logger.getLogger(ConfigurableConstants.class);

    private Properties props = null;

    private static ConfigurableConstants ourInstance = new ConfigurableConstants();

    public static ConfigurableConstants getInstance() {
        return ourInstance;
    }

    private ConfigurableConstants() {
        String propFile = "/config.xml";
        this.setConstants(propFile);
    }

    public void setConstants(String propFile) {
        this.props = new Properties();
        try {
            InputStream in = this.getClass().getResourceAsStream(propFile);
            props.loadFromXML(in);
        } catch (FileNotFoundException fileNotFound) {
            logger.error(String.format("The file '%s' was not found", propFile), fileNotFound);
        } catch (IOException ioException) {
            logger.error(String.format("An exception occurred while reading from the file '%s' ", propFile), ioException);
        }
    }

     public Properties getConstants() {
        return this.props;
    }
}
