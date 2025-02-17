package dev.hyphen.openfeature;

import java.util.ArrayList;
import java.util.List;

public class HyphenProviderOptions {
    private final String application;
    private final String environment;
    private final List<String> horizonUrls;
    private final boolean enableToggleUsage;
    private final int cacheTtlSeconds;

    private HyphenProviderOptions(Builder builder) {
        this.application = builder.application;
        this.environment = builder.environment;
        this.horizonUrls = builder.horizonUrls;
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
        return horizonUrls;
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
        private int cacheTtlSeconds = 30;

        public Builder application(String application) {
            this.application = application;
            return this;
        }

        public Builder environment(String environment) {
            this.environment = environment;
            return this;
        }

        public Builder horizonUrls(List<String> horizonUrls) {
            this.horizonUrls = horizonUrls;
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
            if (application == null || application.isEmpty()) {
                throw new IllegalArgumentException("Application is required");
            }
            if (environment == null || environment.isEmpty()) {
                throw new IllegalArgumentException("Environment is required");
            }
            return new HyphenProviderOptions(this);
        }
    }
}
