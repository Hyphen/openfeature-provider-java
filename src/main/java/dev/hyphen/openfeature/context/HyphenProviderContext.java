package dev.hyphen.openfeature.context;

import dev.openfeature.sdk.MutableContext;
import dev.openfeature.sdk.MutableStructure;
import java.util.HashMap;
import java.util.Map;

public class HyphenProviderContext {
    private final String targetingKey;
    private final UserAttributes user;
    private final String ipAddress;
    private final Map<String, String> customAttributes;

    private HyphenProviderContext(Builder builder) {
        this.targetingKey = builder.targetingKey;
        this.user = builder.user;
        this.ipAddress = builder.ipAddress;
        this.customAttributes = builder.customAttributes;
    }

    public MutableContext toMutableContext() {
        MutableContext context = new MutableContext();
        context.setTargetingKey(targetingKey);

        if (user != null) {
            Map<String, Object> userMap = user.toMap();
            MutableStructure userStruct = new MutableStructure();
            
            for (Map.Entry<String, Object> entry : userMap.entrySet()) {
                if (entry.getValue() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, String> nestedMap = (Map<String, String>) entry.getValue();
                    MutableStructure nestedStruct = new MutableStructure();
                    for (Map.Entry<String, String> nestedEntry : nestedMap.entrySet()) {
                        nestedStruct.add(nestedEntry.getKey(), nestedEntry.getValue());
                    }
                    userStruct.add(entry.getKey(), nestedStruct);
                } else {
                    userStruct.add(entry.getKey(), entry.getValue().toString());
                }
            }
            context.add("user", userStruct);
        }

        if (ipAddress != null) {
            context.add("ipAddress", ipAddress);
        }

        if (customAttributes != null) {
            MutableStructure customStruct = new MutableStructure();
            for (Map.Entry<String, String> entry : customAttributes.entrySet()) {
                customStruct.add(entry.getKey(), entry.getValue());
            }
            context.add("customAttributes", customStruct);
        }

        return context;
    }

    public static class Builder {
        private String targetingKey;
        private UserAttributes user;
        private String ipAddress;
        private Map<String, String> customAttributes;

        public Builder targetingKey(String targetingKey) {
            this.targetingKey = targetingKey;
            return this;
        }

        public Builder user(UserAttributes user) {
            this.user = user;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder customAttributes(Map<String, String> customAttributes) {
            this.customAttributes = new HashMap<>(customAttributes);
            return this;
        }

        public HyphenProviderContext build() {
            if (targetingKey == null || targetingKey.trim().isEmpty()) {
                throw new IllegalArgumentException("targetingKey is required");
            }
            return new HyphenProviderContext(this);
        }
    }
}
