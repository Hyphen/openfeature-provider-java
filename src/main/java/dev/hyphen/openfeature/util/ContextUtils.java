package dev.hyphen.openfeature.util;

import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.Value;

import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public class ContextUtils {
    private ContextUtils() {
        // Prevent instantiation
    }

    public static Map<String, Object> valueToMap(Value value) {
        if (value == null || value.asStructure() == null) return null;
        
        return value.asStructure().asMap().entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> valueToObject(entry.getValue())
            ));
    }

    public static Object valueToObject(Value value) {
        if (value == null) return null;
        return value.asStructure() != null ? valueToMap(value) : value.asObject();
    }

    public static Map<String, Object> contextToMap(EvaluationContext context) {
        Map<String, Object> map = new HashMap<>();
        map.put("targetingKey", context.getTargetingKey());
        
        context.asMap().entrySet().stream()
            .filter(entry -> !entry.getKey().equals("targetingKey"))
            .forEach(entry -> map.put(entry.getKey(), valueToObject(entry.getValue())));
        
        return map;
    }
}
