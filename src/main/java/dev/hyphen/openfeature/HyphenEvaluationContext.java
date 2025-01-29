package dev.hyphen.openfeature;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.MutableContext;
import dev.openfeature.sdk.Structure;
import dev.openfeature.sdk.Value;

public class HyphenEvaluationContext implements EvaluationContext {
    private String targetingKey;
    private String application;
    private String environment;
    private String ipAddress;
    private Map<String, Object> customAttributes;
    private User user;
    private MutableContext structure;

    public static class User {
        private String id;
        private String email;
        private String name;
        private Map<String, Object> customAttributes;

        public User() {
            this.customAttributes = new HashMap<>();
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Map<String, Object> getCustomAttributes() {
            return customAttributes;
        }

        public void setCustomAttributes(Map<String, Object> customAttributes) {
            this.customAttributes = customAttributes;
        }
    }

    public HyphenEvaluationContext() {
        this.customAttributes = new HashMap<>();
        this.structure = new MutableContext();
    }

    public HyphenEvaluationContext(EvaluationContext context) {
        this();
        if (context != null) {
            this.targetingKey = context.getTargetingKey();
            Value attributesValue = context.getValue("attributes");
            if (attributesValue != null && attributesValue.isStructure()) {
                Structure attributes = attributesValue.asStructure();
                Value appValue = attributes.getValue("application");
                if (appValue != null) {
                    this.application = appValue.asString();
                }
                
                Value envValue = attributes.getValue("environment");
                if (envValue != null) {
                    this.environment = envValue.asString();
                }
                
                Value ipValue = attributes.getValue("ipAddress");
                if (ipValue != null) {
                    this.ipAddress = ipValue.asString();
                }
                
                Value userValue = attributes.getValue("user");
                if (userValue != null && userValue.isStructure()) {
                    Structure userStruct = userValue.asStructure();
                    User user = new User();
                    
                    Value idValue = userStruct.getValue("id");
                    if (idValue != null) {
                        user.setId(idValue.asString());
                    }
                    
                    Value emailValue = userStruct.getValue("email");
                    if (emailValue != null) {
                        user.setEmail(emailValue.asString());
                    }
                    
                    Value nameValue = userStruct.getValue("name");
                    if (nameValue != null) {
                        user.setName(nameValue.asString());
                    }
                    
                    Value customAttrsValue = userStruct.getValue("customAttributes");
                    if (customAttrsValue != null && customAttrsValue.isStructure()) {
                        Structure customAttrs = customAttrsValue.asStructure();
                        for (String key : customAttrs.keySet()) {
                            user.getCustomAttributes().put(key, customAttrs.getValue(key).asObject());
                        }
                    }
                    this.user = user;
                }
            }
        }
        updateStructure();
    }

    @Override
    public String getTargetingKey() {
        return targetingKey;
    }

    public void setTargetingKey(String targetingKey) {
        this.targetingKey = targetingKey;
        updateStructure();
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
        updateStructure();
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
        updateStructure();
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        updateStructure();
    }

    public Map<String, Object> getCustomAttributes() {
        return customAttributes;
    }

    public void setCustomAttributes(Map<String, Object> customAttributes) {
        this.customAttributes = customAttributes;
        updateStructure();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        updateStructure();
    }

    private void updateStructure() {
        structure = new MutableContext();
        if (targetingKey != null) {
            structure.setTargetingKey(targetingKey);
        }
        
        MutableContext attributes = new MutableContext();
        if (application != null) {
            attributes.add("application", application);
        }
        if (environment != null) {
            attributes.add("environment", environment);
        }
        if (ipAddress != null) {
            attributes.add("ipAddress", ipAddress);
        }
        if (user != null) {
            MutableContext userStruct = new MutableContext();
            if (user.getId() != null) {
                userStruct.add("id", user.getId());
            }
            if (user.getEmail() != null) {
                userStruct.add("email", user.getEmail());
            }
            if (user.getName() != null) {
                userStruct.add("name", user.getName());
            }
            if (!user.getCustomAttributes().isEmpty()) {
                MutableContext customAttrs = new MutableContext();
                for (Map.Entry<String, Object> entry : user.getCustomAttributes().entrySet()) {
                    customAttrs.add(entry.getKey(), entry.getValue().toString());
                }
                userStruct.add("customAttributes", customAttrs);
            }
            attributes.add("user", userStruct);
        }
        if (!customAttributes.isEmpty()) {
            for (Map.Entry<String, Object> entry : customAttributes.entrySet()) {
                attributes.add(entry.getKey(), entry.getValue().toString());
            }
        }
        structure.add("attributes", attributes);
    }

    @Override
    public Value getValue(String key) {
        return structure.getValue(key);
    }

    @Override
    public Set<String> keySet() {
        return structure.keySet();
    }

    @Override
    public boolean isEmpty() {
        return structure.isEmpty();
    }

    @Override
    public Map<String, Value> asMap() {
        return structure.asMap();
    }

    @Override
    public Map<String, Object> asObjectMap() {
        return structure.asObjectMap();
    }

    @Override
    public Map<String, Value> asUnmodifiableMap() {
        return structure.asUnmodifiableMap();
    }

    @Override
    public EvaluationContext merge(EvaluationContext other) {
        if (other == null) {
            return this;
        }

        HyphenEvaluationContext merged = new HyphenEvaluationContext();
        merged.setTargetingKey(other.getTargetingKey() != null ? other.getTargetingKey() : this.targetingKey);
        
        Map<String, Value> thisMap = this.asMap();
        Map<String, Value> otherMap = other.asMap();
        
        for (Map.Entry<String, Value> entry : thisMap.entrySet()) {
            merged.structure.add(entry.getKey(), entry.getValue().asString());
        }
        
        for (Map.Entry<String, Value> entry : otherMap.entrySet()) {
            merged.structure.add(entry.getKey(), entry.getValue().asString());
        }
        
        return merged;
    }
}
