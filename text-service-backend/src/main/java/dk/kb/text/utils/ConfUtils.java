package dk.kb.text.utils;

import dk.kb.text.ConfigurableConstants;

/**
 * Utility class for handling the configurable elements.
 */
public final class ConfUtils {

    /**
     * Retrieve the host uri.
     * @return The URI of the host.
     */
    public static String getHost() {
        String host = System.getProperty("queue.uri");
        if (host == null) {
            return ConfigurableConstants.getInstance().getConstants().getProperty("queue.uri");
        }
        return host;
    }
}
