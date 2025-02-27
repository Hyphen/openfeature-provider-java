package dev.hyphen.openfeature.util;

import dev.hyphen.openfeature.HyphenProviderOptions;
import java.util.regex.Pattern;

/**
 * Utility class for validating provider options.
 */
public class ValidationUtils {
    private static final Pattern ALTERNATE_ID_PATTERN = Pattern.compile("^(?!.*\\b(environments)\\b)[a-z0-9\\-_]{1,25}$");
    private static final Pattern PROJECT_ENV_ID_PATTERN = Pattern.compile("^pevr_[a-zA-Z0-9]+$");

    private ValidationUtils() {
        // Prevent instantiation
    }

    /**
     * Validates the environment format.
     * 
     * @param environment The environment identifier to validate
     * @throws IllegalArgumentException if the environment format is invalid
     */
    public static void validateEnvironmentFormat(String environment) {
        if (environment == null || environment.isEmpty()) {
            throw new IllegalArgumentException("Environment is required");
        }

        boolean isEnvironmentId = PROJECT_ENV_ID_PATTERN.matcher(environment).matches();
        boolean isValidAlternateId = ALTERNATE_ID_PATTERN.matcher(environment).matches();

        if (!isEnvironmentId && !isValidAlternateId) {
            throw new IllegalArgumentException(
                "Invalid environment format. Must be either a project environment ID (starting with \"pevr_\") " +
                "or a valid alternateId (1-25 characters, lowercase letters, numbers, hyphens, and underscores, " +
                "and must not contain the word \"environments\")."
            );
        }
    }

    /**
     * Validates the required provider options.
     * 
     * @param options The HyphenProviderOptions to validate
     * @throws IllegalArgumentException if any validation fails
     */
    public static void validateOptions(HyphenProviderOptions options) {
        if (options == null) {
            throw new IllegalArgumentException("Options cannot be null");
        }
        
        String application = options.getApplication();
        if (application == null || application.isEmpty()) {
            throw new IllegalArgumentException("Application is required");
        }
        
        String environment = options.getEnvironment();
        if (environment == null || environment.isEmpty()) {
            throw new IllegalArgumentException("Environment is required");
        }
        
        validateEnvironmentFormat(environment);
    }
}
