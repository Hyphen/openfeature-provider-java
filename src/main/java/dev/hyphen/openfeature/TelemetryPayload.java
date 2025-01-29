package dev.hyphen.openfeature;

public class TelemetryPayload {
    private HyphenEvaluationContext context;
    private TelemetryData data;

    public static class TelemetryData {
        private Evaluation toggle;

        public Evaluation getToggle() {
            return toggle;
        }

        public void setToggle(Evaluation toggle) {
            this.toggle = toggle;
        }
    }

    public HyphenEvaluationContext getContext() {
        return context;
    }

    public void setContext(HyphenEvaluationContext context) {
        this.context = context;
    }

    public TelemetryData getData() {
        return data;
    }

    public void setData(TelemetryData data) {
        this.data = data;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final TelemetryPayload payload;

        private Builder() {
            this.payload = new TelemetryPayload();
            this.payload.data = new TelemetryData();
        }

        public Builder context(HyphenEvaluationContext context) {
            payload.setContext(context);
            return this;
        }

        public Builder toggle(Evaluation toggle) {
            payload.getData().setToggle(toggle);
            return this;
        }

        public TelemetryPayload build() {
            return payload;
        }
    }
}
