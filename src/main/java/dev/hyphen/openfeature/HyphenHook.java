package dev.hyphen.openfeature;

import java.util.Map;
import java.util.Optional;

import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.FlagEvaluationDetails;
import dev.openfeature.sdk.Hook;
import dev.openfeature.sdk.HookContext;
import dev.openfeature.sdk.MutableContext;
import dev.openfeature.sdk.Value;

public class HyphenHook implements Hook<Value> {
    private final HyphenProvider provider;

    public HyphenHook(HyphenProvider provider) {
        this.provider = provider;
    }

    @Override
    public Optional<EvaluationContext> before(HookContext<Value> ctx, Map<String, Object> hints) {
        MutableContext mutableContext = new MutableContext();
        mutableContext.add("application", provider.getOptions().getApplication());
        mutableContext.add("environment", provider.getOptions().getEnvironment());
        
        String targetingKey = generateTargetingKey(provider.getOptions());
        mutableContext.setTargetingKey(targetingKey);
        
        return Optional.of(mutableContext);
    }

    @Override
    public void after(HookContext<Value> ctx, FlagEvaluationDetails<Value> details, Map<String, Object> hints) {
        try {
            Evaluation evaluation = Evaluation.builder()
                    .key(details.getFlagKey())
                    .value(details.getValue().asObject())
                    .type(getTypeFromValue(details.getValue()))
                    .reason(details.getReason())
                    .build();

            MutableContext context = new MutableContext();
            context.add("application", provider.getOptions().getApplication());
            context.add("environment", provider.getOptions().getEnvironment());
            context.setTargetingKey(generateTargetingKey(provider.getOptions()));

            TelemetryPayload payload = TelemetryPayload.builder()
                    .context(new HyphenEvaluationContext(context))
                    .toggle(evaluation)
                    .build();

            provider.getHyphenClient().postTelemetry(payload);
        } catch (Exception e) {
            System.err.println("Unable to log usage: " + e.getMessage());
        }
    }

    @Override
    public void error(HookContext<Value> ctx, Exception error, Map<String, Object> hints) {
        System.err.println("Error in hook: " + error.getMessage());
    }

    @Override
    public void finallyAfter(HookContext<Value> ctx, FlagEvaluationDetails<Value> details, Map<String, Object> hints) {
        // No implementation needed
    }

    private String generateTargetingKey(HyphenProviderOptions options) {
        return String.format("%s-%s-%s",
                options.getApplication(),
                options.getEnvironment(),
                Integer.toHexString((int)(Math.random() * 1000000))
        );
    }

    private String getTypeFromValue(Value value) {
        if (value.isBoolean()) {
            return "boolean";
        } else if (value.isString()) {
            return "string";
        } else if (value.isNumber()) {
            return "number";
        } else if (value.isStructure()) {
            return "object";
        }
        return "unknown";
    }
}
