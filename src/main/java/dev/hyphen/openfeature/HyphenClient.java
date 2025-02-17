package dev.hyphen.openfeature;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.hyphen.openfeature.model.HyphenEvaluation;
import dev.hyphen.openfeature.model.EvaluationResponse;
import dev.openfeature.sdk.EvaluationContext;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import dev.openfeature.sdk.Value;

public class HyphenClient {
    private static final Logger logger = LoggerFactory.getLogger(HyphenClient.class);
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final OkHttpClient httpClient;
    private final Cache<String, EvaluationResponse> cache;
    private final String publicKey;
    private final List<String> horizonUrls;

    public HyphenClient(String publicKey, HyphenProviderOptions options) {
        this.publicKey = publicKey;
        this.horizonUrls = new ArrayList<>(options.getHorizonUrls());
        this.horizonUrls.add(buildDefaultHorizonUrl(publicKey));

        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();

        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(options.getCacheTtlSeconds()))
                .build();
    }

    public EvaluationResponse evaluate(EvaluationContext context) throws IOException {
        var payload = prepareEvaluatePayload(context);
        var cacheKey = generateCacheKey(context);
        
        var cachedResponse = cache.getIfPresent(cacheKey);
        if (cachedResponse != null) {
            return cachedResponse;
        }

        var response = tryUrls("/toggle/evaluate", payload);
        if (response != null) {
            cache.put(cacheKey, response);
        }
        return response; 
    }

    private String generateCacheKey(EvaluationContext context) {
        try {
            return objectMapper.writeValueAsString(context);
        } catch (Exception e) {
            logger.warn("Failed to generate cache key", e);
            return context.toString();
        }
    }

    private String buildDefaultHorizonUrl(String publicKey) {
        try {
            var keyWithoutPrefix = publicKey.replace("public_", "");
            var decoded = new String(Base64.getDecoder().decode(keyWithoutPrefix));
            var organizationId = decoded.split(":")[0];
            
            return organizationId.matches("^[a-zA-Z0-9_-]+$") 
                ? "https://" + organizationId + ".toggle.hyphen.cloud"
                : "https://toggle.hyphen.cloud";
        } catch (Exception e) {
            logger.warn("Failed to build default horizon URL", e);
            return "https://toggle.hyphen.cloud";
        }
    }

    private EvaluationResponse tryUrls(String path, String payload) throws IOException {
        IOException lastError = null;

        for (var baseUrl : horizonUrls) {
            try {
                var url = baseUrl.endsWith("/") ? baseUrl + path.substring(1) : baseUrl + path;
                var request = new Request.Builder()
                        .url(url)
                        .post(RequestBody.create(payload, JSON))
                        .addHeader("Content-Type", "application/json")
                        .addHeader("x-api-key", publicKey)
                        .build();

                try (var response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected response " + response);
                    }
                    var responseBody = response.body().string();
                    return objectMapper.readValue(responseBody, EvaluationResponse.class);
                }
            } catch (IOException e) {
                lastError = e;
            }
        }

        throw lastError != null ? lastError : new IOException("All URLs failed");
    }

    private Map<String, Object> valueToMap(Value value) {
        if (value == null || value.asStructure() == null) return null;
        
        return value.asStructure().asMap().entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> valueToObject(entry.getValue())
            ));
    }

    private Object valueToObject(Value value) {
        if (value == null) return null;
        return value.asStructure() != null ? valueToMap(value) : value.asObject();
    }

    private Map<String, Object> contextToMap(EvaluationContext context) {
        var map = new HashMap<String, Object>();
        map.put("targetingKey", context.getTargetingKey());
        
        context.asMap().entrySet().stream()
            .filter(entry -> !entry.getKey().equals("targetingKey"))
            .forEach(entry -> map.put(entry.getKey(), valueToObject(entry.getValue())));
        
        return map;
    }

    private String prepareEvaluatePayload(EvaluationContext context) throws IOException {
        var contextMap = contextToMap(context);
        return objectMapper.writeValueAsString(contextMap);
    }

    public void postTelemetry(Map<String, Object> payload) {
        try {
            tryUrls("/toggle/telemetry", objectMapper.writeValueAsString(payload));
        } catch (Exception e) {
            logger.warn("Failed to post telemetry", e);
        }
    }
}
