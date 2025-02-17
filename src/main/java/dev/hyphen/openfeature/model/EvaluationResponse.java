package dev.hyphen.openfeature.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class EvaluationResponse {
    @JsonProperty("id")
    private String id;

    @JsonProperty("targetingKey")
    private String targetingKey;

    @JsonProperty("toggles")
    private Map<String, HyphenEvaluation> toggles;

    public EvaluationResponse() {
        this.toggles = Collections.emptyMap();
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
        return Collections.unmodifiableMap(toggles);
    }

    public void setToggles(Map<String, HyphenEvaluation> toggles) {
        this.toggles = Objects.requireNonNull(toggles, "toggles cannot be null");
    }

    public HyphenEvaluation getToggle(String key) {
        return Optional.ofNullable(toggles)
                .map(map -> map.get(key))
                .orElse(null);
    }

    @Override
    public String toString() {
        return "EvaluationResponse{" +
                "id='" + id + '\'' +
                ", targetingKey='" + targetingKey + '\'' +
                ", toggles=" + toggles +
                '}';
    }
}
