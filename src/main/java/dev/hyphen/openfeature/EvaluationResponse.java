package dev.hyphen.openfeature;

import java.util.HashMap;
import java.util.Map;

public class EvaluationResponse {
    private Map<String, Evaluation> toggles;

    public EvaluationResponse() {
        this.toggles = new HashMap<>();
    }

    public Map<String, Evaluation> getToggles() {
        return toggles;
    }

    public void setToggles(Map<String, Evaluation> toggles) {
        this.toggles = toggles;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final EvaluationResponse response;

        private Builder() {
            this.response = new EvaluationResponse();
        }

        public Builder toggles(Map<String, Evaluation> toggles) {
            response.setToggles(toggles);
            return this;
        }

        public EvaluationResponse build() {
            return response;
        }
    }
}
