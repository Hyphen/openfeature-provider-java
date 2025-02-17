package dev.hyphen.openfeature.context;

import java.util.HashMap;
import java.util.Map;

public class UserAttributes {
    private final String id;
    private final String email;
    private final String name;
    private final Map<String, String> customAttributes;

    private UserAttributes(Builder builder) {
        this.id = builder.id;
        this.email = builder.email;
        this.name = builder.name;
        this.customAttributes = builder.customAttributes;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getCustomAttributes() {
        return customAttributes;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("email", email);
        map.put("name", name);
        if (customAttributes != null && !customAttributes.isEmpty()) {
            map.put("customAttributes", customAttributes);
        }
        return map;
    }

    public static class Builder {
        private String id;
        private String email;
        private String name;
        private Map<String, String> customAttributes = new HashMap<>();

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder addCustomAttribute(String key, String value) {
            this.customAttributes.put(key, value);
            return this;
        }

        public Builder customAttributes(Map<String, String> customAttributes) {
            this.customAttributes = new HashMap<>(customAttributes);
            return this;
        }

        public UserAttributes build() {
            if (id == null || id.trim().isEmpty()) {
                throw new IllegalArgumentException("id is required");
            }
            return new UserAttributes(this);
        }
    }
}
