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
    private Map<String, HyphenEvaluation> toggles;

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

    public Map<String, HyphenEvaluation> getToggles() {
        return toggles;
    }

    public void setToggles(Map<String, HyphenEvaluation> toggles) {
        this.toggles = toggles;
    }

    public HyphenEvaluation getToggle(String key) {
        return toggles.get(key);
    }

    public void addToggle(String key, HyphenEvaluation evaluation) {
        toggles.put(key, evaluation);
    }
}
