package dev.hyphen.openfeature;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.hyphen.openfeature.model.Evaluation;
import dev.hyphen.openfeature.model.EvaluationResponse;
import dev.openfeature.sdk.EvaluationContext;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import dev.openfeature.sdk.Value;

public class HyphenClient {
    private static final Logger logger = LoggerFactory.getLogger(HyphenClient.class);
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final OkHttpClient httpClient;
    private final Cache<String, EvaluationResponse> cache;
    private final String publicKey;
    private final List<String> horizonUrls;
    private final HyphenProviderOptions options;

    public HyphenClient(String publicKey, HyphenProviderOptions options) {
        this.publicKey = publicKey;
        this.options = options;
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
        String payload = prepareEvaluatePayload(context);
        System.out.println("\n[EVALUATE PAYLOAD] " + payload + "\n");
        
        String cacheKey = generateCacheKey(context);
        EvaluationResponse cachedResponse = cache.getIfPresent(cacheKey);
        if (cachedResponse != null) {
            return cachedResponse;
        }

        EvaluationResponse response = tryUrls("/toggle/evaluate", payload);
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
            String keyWithoutPrefix = publicKey.replace("public_", "");
            String decoded = new String(Base64.getDecoder().decode(keyWithoutPrefix));
            String organizationId = decoded.split(":")[0];
            if (organizationId.matches("^[a-zA-Z0-9_-]+$")) {
                return "https://" + organizationId + ".toggle.hyphen.cloud";
            }
        } catch (Exception e) {
            logger.warn("Failed to build default horizon URL", e);
        }
        return "https://toggle.hyphen.cloud";
    }

    private EvaluationResponse tryUrls(String path, String payload) throws IOException {
        IOException lastError = null;

        for (String baseUrl : horizonUrls) {
            try {
                String url = baseUrl.endsWith("/") ? baseUrl + path.substring(1) : baseUrl + path;
                Request request = new Request.Builder()
                        .url(url)
                        .post(RequestBody.create(payload, JSON))
                        .addHeader("Content-Type", "application/json")
                        .addHeader("x-api-key", publicKey)
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected response " + response);
                    }
                    String responseBody = response.body().string();
                    System.out.println("\n[API RESPONSE]");
                    System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                        objectMapper.readValue(responseBody, Map.class)));
                    System.out.println();
                    return objectMapper.readValue(responseBody, EvaluationResponse.class);
                }
            } catch (IOException e) {
                lastError = e;
            }
        }

        throw lastError != null ? lastError : new IOException("All URLs failed");
    }

    private Map<String, Object> valueToMap(Value value) {
        if (value == null) return null;
        
        if (value.asStructure() != null) {
            Map<String, Object> structMap = new HashMap<>();
            for (Map.Entry<String, Value> entry : value.asStructure().asMap().entrySet()) {
                structMap.put(entry.getKey(), valueToObject(entry.getValue()));
            }
            return structMap;
        }
        
        return null;
    }

    private Object valueToObject(Value value) {
        if (value == null) return null;
        
        if (value.asStructure() != null) {
            return valueToMap(value);
        }
        
        return value.asObject();
    }

    private Map<String, Object> contextToMap(EvaluationContext context) {
        Map<String, Object> map = new HashMap<>();
        map.put("targetingKey", context.getTargetingKey());
        
        Map<String, Value> attributes = context.asMap();
        for (Map.Entry<String, Value> entry : attributes.entrySet()) {
            if (!entry.getKey().equals("targetingKey")) {
                map.put(entry.getKey(), valueToObject(entry.getValue()));
            }
        }
        
        return map;
    }

    private String prepareEvaluatePayload(EvaluationContext context) throws IOException {
        Map<String, Object> contextMap = contextToMap(context);
        String jsonContext = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(contextMap);
        System.out.println("\n[CONTEXT AS JSON]");
        System.out.println(jsonContext);
        System.out.println();
        return jsonContext;
    }

    public void postTelemetry(String key, Evaluation evaluation) {
        try {
            String payload = objectMapper.writeValueAsString(evaluation);
            System.out.println("\n[TELEMETRY PAYLOAD] " + payload + "\n");
            tryUrls("/toggle/telemetry", payload);
        } catch (Exception e) {
            logger.warn("Failed to post telemetry", e);
        }
    }
}
