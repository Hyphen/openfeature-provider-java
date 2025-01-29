package dev.hyphen.openfeature;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HyphenClient {
    private static final MediaType JSON = MediaType.get("application/json");
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private final String publicKey;
    private final List<String> horizonUrls;
    private final String defaultHorizonUrl;
    private final OkHttpClient httpClient;
    private final Cache<String, EvaluationResponse> cache;
    private final Function<HyphenEvaluationContext, String> generateCacheKeyFn;

    public HyphenClient(String publicKey, HyphenProviderOptions options) {
        this.publicKey = publicKey;
        this.defaultHorizonUrl = buildDefaultHorizonUrl(publicKey);
        this.horizonUrls = new ArrayList<>(options.getHorizonUrls());
        this.horizonUrls.add(this.defaultHorizonUrl);
        
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();

        int ttlSeconds = options.getCache() != null ? options.getCache().getTtlSeconds() : 30;
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(ttlSeconds, TimeUnit.SECONDS)
                .build();

        this.generateCacheKeyFn = options.getCache() != null && options.getCache().getGenerateCacheKeyFn() != null
                ? options.getCache().getGenerateCacheKeyFn()
                : this::defaultGenerateCacheKey;
    }

    public EvaluationResponse evaluate(HyphenEvaluationContext context) throws IOException {
        String cacheKey = generateCacheKeyFn.apply(context);
        EvaluationResponse cachedResponse = cache.getIfPresent(cacheKey);
        if (cachedResponse != null) {
            return cachedResponse;
        }

        EvaluationResponse response = tryUrls("/toggle/evaluate", context);
        if (response != null) {
            cache.put(cacheKey, response);
        }
        return response;
    }

    public void postTelemetry(TelemetryPayload payload) throws IOException {
        tryUrls("/toggle/telemetry", payload);
    }

    private <T> T tryUrls(String path, Object payload) throws IOException {
        IOException lastError = null;

        for (String baseUrl : horizonUrls) {
            try {
                String url = normalizeUrl(baseUrl, path);
                return executePost(url, payload);
            } catch (IOException e) {
                lastError = e;
            }
        }

        throw lastError;
    }

    private <T> T executePost(String url, Object payload) throws IOException {
        String jsonBody = objectMapper.writeValueAsString(payload);
        
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("x-api-key", publicKey)
                .post(RequestBody.create(jsonBody, JSON))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response " + response);
            }

            ResponseBody body = response.body();
            if (body == null) {
                return null;
            }

            String responseBody = body.string();
            if (responseBody.isEmpty()) {
                return null;
            }

            return (T) objectMapper.readValue(responseBody, EvaluationResponse.class);
        }
    }

    private String normalizeUrl(String baseUrl, String path) {
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return baseUrl + "/" + path;
    }

    private String buildDefaultHorizonUrl(String publicKey) {
        String orgId = getOrgIdFromPublicKey(publicKey);
        return orgId != null 
            ? String.format("https://%s.toggle.hyphen.cloud", orgId)
            : "https://toggle.hyphen.cloud";
    }

    private String getOrgIdFromPublicKey(String publicKey) {
        try {
            String keyWithoutPrefix = publicKey.replaceFirst("^public_", "");
            String decoded = new String(Base64.getDecoder().decode(keyWithoutPrefix));
            String[] parts = decoded.split(":");
            if (parts.length > 0 && parts[0].matches("^[a-zA-Z0-9_-]+$")) {
                return parts[0];
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String defaultGenerateCacheKey(HyphenEvaluationContext context) {
        try {
            return objectMapper.writeValueAsString(context);
        } catch (Exception e) {
            return context.toString();
        }
    }
}
