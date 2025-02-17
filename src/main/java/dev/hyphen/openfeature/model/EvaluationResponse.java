package dev.hyphen.openfeature.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

public class EvaluationResponse {
    @JsonProperty("id")
    private String id;

    @JsonProperty("targetingKey")
    private String targetingKey;

    @JsonProperty("toggles")
    private Map<String, Evaluation> toggles;

    public EvaluationResponse() {
        this.toggles = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTargetingKey() {
        return targetingKey;
    }

    public void setTargetingKey(String targetingKey) {
        this.targetingKey = targetingKey;
    }

    public Map<String, Evaluation> getToggles() {
        return toggles;
    }

    public void setToggles(Map<String, Evaluation> toggles) {
        this.toggles = toggles;
    }

    public Evaluation getToggle(String key) {
        return toggles.get(key);
    }

    public void addToggle(String key, Evaluation evaluation) {
        toggles.put(key, evaluation);
    }
}
