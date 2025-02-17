package dev.hyphen.openfeature.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.FlagEvaluationDetails;
import java.util.Map;

public class TelemetryPayload {
    @JsonProperty("context")
    private final EvaluationContext context;

    @JsonProperty("data")
    private final Map<String, FlagEvaluationDetails<?>> data;

    public TelemetryPayload(EvaluationContext context, Map<String, FlagEvaluationDetails<?>> data) {
        this.context = context;
        this.data = data;
    }

    public EvaluationContext getContext() {
        return context;
    }

    public Map<String, FlagEvaluationDetails<?>> getData() {
        return data;
    }
}
