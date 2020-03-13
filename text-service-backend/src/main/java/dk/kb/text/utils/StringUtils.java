package dk.kb.text.utils;

public final class StringUtils {

    public static void validateString(String argument, String argumentName) {
        if(argument == null || argument.isEmpty()) {
            throw new IllegalArgumentException("The String '" + argumentName + " must not be null"
                    + " or empty!");
        }
    }
}
