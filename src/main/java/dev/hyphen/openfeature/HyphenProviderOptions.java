package dev.hyphen.openfeature;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Configuration options for the Hyphen Provider.
 */
public class HyphenProviderOptions {
    /** The application name or ID for the current evaluation. */
    private final String application;
    
    /** 
     * The environment identifier for the Hyphen project.
     * This can be either:
     * - A project environment ID (e.g., `pevr_abc123`)
     * - A valid alternateId (1-25 characters, lowercase letters, numbers, hyphens, and underscores)
     */
    private final String environment;
    
    /** The Hyphen server URL */
    private final List<String> horizonUrls;
    
    /** Flag to enable toggle usage */
    private final boolean enableToggleUsage;
    
    /** The time-to-live (TTL) in seconds for the cache. */
    private final int cacheTtlSeconds;

    private HyphenProviderOptions(Builder builder) {
        this.application = Objects.requireNonNull(builder.application, "application is required");
        this.environment = Objects.requireNonNull(builder.environment, "environment is required");
        this.horizonUrls = new ArrayList<>(builder.horizonUrls);
        this.enableToggleUsage = builder.enableToggleUsage;
        this.cacheTtlSeconds = builder.cacheTtlSeconds;
    }

    public String getApplication() {
        return application;
    }

    public String getEnvironment() {
        return environment;
    }

    public List<String> getHorizonUrls() {
        return new ArrayList<>(horizonUrls);
    }

    public boolean isEnableToggleUsage() {
        return enableToggleUsage;
    }

    public int getCacheTtlSeconds() {
        return cacheTtlSeconds;
    }

    public static class Builder {
        private String application;
        private String environment;
        private List<String> horizonUrls = new ArrayList<>();
        private boolean enableToggleUsage = true;
        private int cacheTtlSeconds = 300;

        public Builder application(String application) {
            this.application = application;
            return this;
        }

        public Builder environment(String environment) {
            this.environment = environment;
            return this;
        }

        public Builder horizonUrls(List<String> horizonUrls) {
            this.horizonUrls = new ArrayList<>(horizonUrls);
            return this;
        }

        public Builder enableToggleUsage(boolean enableToggleUsage) {
            this.enableToggleUsage = enableToggleUsage;
            return this;
        }

        public Builder cacheTtlSeconds(int cacheTtlSeconds) {
            this.cacheTtlSeconds = cacheTtlSeconds;
            return this;
        }

        public HyphenProviderOptions build() {
            return new HyphenProviderOptions(this);
        }
    }
}
