package dev.hyphen.openfeature;

public class Evaluation {
    private String key;
    private Object value;
    private String type;
    private String reason;
    private String errorMessage;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Evaluation evaluation;

        private Builder() {
            this.evaluation = new Evaluation();
        }

        public Builder key(String key) {
            evaluation.setKey(key);
            return this;
        }

        public Builder value(Object value) {
            evaluation.setValue(value);
            return this;
        }

        public Builder type(String type) {
            evaluation.setType(type);
            return this;
        }

        public Builder reason(String reason) {
            evaluation.setReason(reason);
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            evaluation.setErrorMessage(errorMessage);
            return this;
        }

        public Evaluation build() {
            return evaluation;
        }
    }
}
