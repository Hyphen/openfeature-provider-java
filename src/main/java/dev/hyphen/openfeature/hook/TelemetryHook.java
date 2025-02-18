package dev.hyphen.openfeature.hook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dev.hyphen.openfeature.HyphenProvider;
import dev.hyphen.openfeature.HyphenClient;
import dev.hyphen.openfeature.HyphenProviderOptions;
import dev.hyphen.openfeature.util.ContextUtils;
import dev.openfeature.sdk.Hook;
import dev.openfeature.sdk.HookContext;
import dev.openfeature.sdk.FlagEvaluationDetails;

import java.util.Map;
import java.util.HashMap;

public class TelemetryHook implements Hook {
    private final HyphenClient client;
    private final ObjectMapper objectMapper;
    private final HyphenProviderOptions options;

    public TelemetryHook(HyphenProvider provider) {
        this.client = provider.getClient();
        this.options = provider.getOptions();
        this.objectMapper = new ObjectMapper()
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
            .setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY);
    }

    private Map<String, Object> cleanupDetails(HookContext context, FlagEvaluationDetails<?> details) {
        Map<String, Object> cleanDetails = new HashMap<>();
        cleanDetails.put("key", details.getFlagKey());
        cleanDetails.put("value", details.getValue());
        cleanDetails.put("type", context.getType().toString().toLowerCase());
        
        if (details.getVariant() != null) {
            cleanDetails.put("variant", details.getVariant());
        }
        if (details.getReason() != null) {
            cleanDetails.put("reason", details.getReason());
        }
        if (details.getErrorCode() != null) {
            cleanDetails.put("errorCode", details.getErrorCode());
        }
        if (details.getErrorMessage() != null) {
            cleanDetails.put("errorMessage", details.getErrorMessage());
        }
        
        return cleanDetails;
    }

    @Override
    public void after(HookContext context, FlagEvaluationDetails details, Map hints) {
        Map<String, Object> data = new HashMap<>();
        data.put("toggle", cleanupDetails(context, details));
        
        Map<String, Object> contextMap = ContextUtils.contextToMap(context.getCtx());
        
        // Add application and environment from options
        contextMap.put("application", options.getApplication());
        contextMap.put("environment", options.getEnvironment());
        
        Map<String, Object> fullPayload = new HashMap<>();
        fullPayload.put("context", contextMap);
        fullPayload.put("data", data);
        
        try {
            String jsonPayload = objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(fullPayload);
            System.out.println("TelemetryHook - Payload: " + jsonPayload);
            
            client.postTelemetry(fullPayload);
        } catch (Exception e) {
            // Silently ignore telemetry failures
        }
    }
}
