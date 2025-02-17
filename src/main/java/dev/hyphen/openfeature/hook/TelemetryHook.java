package dev.hyphen.openfeature.hook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dev.hyphen.openfeature.HyphenProvider;
import dev.hyphen.openfeature.HyphenClient;
import dev.openfeature.sdk.Hook;
import dev.openfeature.sdk.HookContext;
import dev.openfeature.sdk.FlagEvaluationDetails;
import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.Value;

import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public class TelemetryHook implements Hook {
    private final HyphenClient client;
    private final ObjectMapper objectMapper;

    public TelemetryHook(HyphenProvider provider) {
        this.client = provider.getClient();
        this.objectMapper = new ObjectMapper()
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
            .setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY);
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
        Map<String, Object> map = new HashMap<>();
        map.put("targetingKey", context.getTargetingKey());
        
        context.asMap().entrySet().stream()
            .filter(entry -> !entry.getKey().equals("targetingKey"))
            .forEach(entry -> map.put(entry.getKey(), valueToObject(entry.getValue())));
        
        return map;
    }

    private Map<String, Object> cleanupDetails(FlagEvaluationDetails<?> details) {
        Map<String, Object> cleanDetails = new HashMap<>();
        cleanDetails.put("flagKey", details.getFlagKey());
        cleanDetails.put("value", details.getValue());
        
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
        data.put("toggle", cleanupDetails(details));
        
        Map<String, Object> contextMap = contextToMap(context.getCtx());
        
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
            System.out.println("Failed to serialize payload to JSON: " + e.getMessage());
        }
    }
}
