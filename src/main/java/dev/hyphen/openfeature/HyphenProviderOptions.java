package dev.hyphen.openfeature;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class HyphenProviderOptions {
    private String application;
    private String environment;
    private List<String> horizonUrls;
    private boolean enableToggleUsage = true;
    private CacheConfig cache;

    public static class CacheConfig {
        private int ttlSeconds = 30;
        private Function<HyphenEvaluationContext, String> generateCacheKeyFn;

        public int getTtlSeconds() {
            return ttlSeconds;
        }

        public void setTtlSeconds(int ttlSeconds) {
            this.ttlSeconds = ttlSeconds;
        }

        public Function<HyphenEvaluationContext, String> getGenerateCacheKeyFn() {
            return generateCacheKeyFn;
        }

        public void setGenerateCacheKeyFn(Function<HyphenEvaluationContext, String> generateCacheKeyFn) {
            this.generateCacheKeyFn = generateCacheKeyFn;
        }
    }

    public HyphenProviderOptions() {
        this.horizonUrls = new ArrayList<>();
        this.cache = new CacheConfig();
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public List<String> getHorizonUrls() {
        return horizonUrls;
    }

    public void setHorizonUrls(List<String> horizonUrls) {
        this.horizonUrls = horizonUrls;
    }

    public boolean isEnableToggleUsage() {
        return enableToggleUsage;
    }

    public void setEnableToggleUsage(boolean enableToggleUsage) {
        this.enableToggleUsage = enableToggleUsage;
    }

    public CacheConfig getCache() {
        return cache;
    }

    public void setCache(CacheConfig cache) {
        this.cache = cache;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final HyphenProviderOptions options;

        private Builder() {
            this.options = new HyphenProviderOptions();
        }

        public Builder application(String application) {
            options.setApplication(application);
            return this;
        }

        public Builder environment(String environment) {
            options.setEnvironment(environment);
            return this;
        }

        public Builder horizonUrls(List<String> horizonUrls) {
            options.setHorizonUrls(horizonUrls);
            return this;
        }

        public Builder enableToggleUsage(boolean enableToggleUsage) {
            options.setEnableToggleUsage(enableToggleUsage);
            return this;
        }

        public Builder cache(CacheConfig cache) {
            options.setCache(cache);
            return this;
        }

        public HyphenProviderOptions build() {
            return options;
        }
    }
}
