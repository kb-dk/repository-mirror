package dk.kb.text.utils;

public final class ValidationUtils {

    /**
     * Validates that a string is neither null nor empty.
     * @param argument The argument to validate.
     * @param argumentName The name of the argument.
     */
    public static void validateString(String argument, String argumentName) {
        if(argument == null || argument.isEmpty()) {
            throw new IllegalArgumentException("The String '" + argumentName + "' must not be null"
                    + " or empty!");
        }
    }

    /**
     * Validates a object is not null.
     * @param argument The argument to validate.
     * @param argumentName The name of the argument.
     */
    public static void validateObject(Object argument, String argumentName) {
        if(argument == null) {
            throw new IllegalArgumentException("The Object '" + argumentName + "' must not be null"
                    + " or empty!");
        }

    }
}
