package dev.hyphen.openfeature;

import java.util.Collections;
import java.util.List;

import dev.openfeature.sdk.ErrorCode;
import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.FeatureProvider;
import dev.openfeature.sdk.Hook;
import dev.openfeature.sdk.Metadata;
import dev.openfeature.sdk.ProviderEvaluation;
import dev.openfeature.sdk.Reason;
import dev.openfeature.sdk.Value;

public class HyphenProvider implements FeatureProvider {
    private final HyphenClient hyphenClient;
    private final HyphenProviderOptions options;
    private final List<Hook> hooks;

    public HyphenProvider(String publicKey, HyphenProviderOptions options) {
        if (options.getApplication() == null || options.getApplication().isEmpty()) {
            throw new IllegalArgumentException("Application is required");
        }
        if (options.getEnvironment() == null || options.getEnvironment().isEmpty()) {
            throw new IllegalArgumentException("Environment is required");
        }

        this.options = options;
        this.hyphenClient = new HyphenClient(publicKey, options);
        
        if (options.isEnableToggleUsage()) {
            this.hooks = Collections.singletonList(new HyphenHook(this));
        } else {
            this.hooks = Collections.emptyList();
        }
    }

    @Override
    public Metadata getMetadata() {
        return () -> "hyphen-toggle-java";
    }

    public List<Hook> getHooks() {
        return hooks;
    }

    @Override
    public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext context) {
        try {
            validateContext(context);
            Evaluation evaluation = getEvaluation(key, context, "boolean");
            return ProviderEvaluation.<Boolean>builder()
                    .value(Boolean.valueOf(evaluation.getValue().toString()))
                    .variant(evaluation.getValue().toString())
                    .reason(evaluation.getReason())
                    .build();
        } catch (Exception e) {
            return ProviderEvaluation.<Boolean>builder()
                    .value(defaultValue)
                    .reason(Reason.ERROR.name())
                    .errorCode(ErrorCode.GENERAL)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    @Override
    public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext context) {
        try {
            validateContext(context);
            Evaluation evaluation = getEvaluation(key, context, "string");
            return ProviderEvaluation.<String>builder()
                    .value(evaluation.getValue().toString())
                    .variant(evaluation.getValue().toString())
                    .reason(evaluation.getReason())
                    .build();
        } catch (Exception e) {
            return ProviderEvaluation.<String>builder()
                    .value(defaultValue)
                    .reason(Reason.ERROR.name())
                    .errorCode(ErrorCode.GENERAL)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    @Override
    public ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue, EvaluationContext context) {
        try {
            validateContext(context);
            Evaluation evaluation = getEvaluation(key, context, "number");
            return ProviderEvaluation.<Integer>builder()
                    .value(Integer.valueOf(evaluation.getValue().toString()))
                    .variant(evaluation.getValue().toString())
                    .reason(evaluation.getReason())
                    .build();
        } catch (Exception e) {
            return ProviderEvaluation.<Integer>builder()
                    .value(defaultValue)
                    .reason(Reason.ERROR.name())
                    .errorCode(ErrorCode.GENERAL)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    @Override
    public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext context) {
        try {
            validateContext(context);
            Evaluation evaluation = getEvaluation(key, context, "number");
            return ProviderEvaluation.<Double>builder()
                    .value(Double.valueOf(evaluation.getValue().toString()))
                    .variant(evaluation.getValue().toString())
                    .reason(evaluation.getReason())
                    .build();
        } catch (Exception e) {
            return ProviderEvaluation.<Double>builder()
                    .value(defaultValue)
                    .reason(Reason.ERROR.name())
                    .errorCode(ErrorCode.GENERAL)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    @Override
    public ProviderEvaluation<Value> getObjectEvaluation(String key, Value defaultValue, EvaluationContext context) {
        try {
            validateContext(context);
            Evaluation evaluation = getEvaluation(key, context, "object");
            return ProviderEvaluation.<Value>builder()
                    .value(Value.objectToValue(evaluation.getValue()))
                    .variant(evaluation.getValue().toString())
                    .reason(evaluation.getReason())
                    .build();
        } catch (Exception e) {
            return ProviderEvaluation.<Value>builder()
                    .value(defaultValue)
                    .reason(Reason.ERROR.name())
                    .errorCode(ErrorCode.GENERAL)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    private Evaluation getEvaluation(String key, EvaluationContext context, String expectedType) throws Exception {
        HyphenEvaluationContext hyphenContext = enrichContext(context);
        EvaluationResponse response = hyphenClient.evaluate(hyphenContext);
        
        Evaluation evaluation = response.getToggles().get(key);
        if (evaluation == null || evaluation.getErrorMessage() != null) {
            throw new Exception(evaluation != null ? evaluation.getErrorMessage() : "Evaluation does not exist");
        }
        
        if (!evaluation.getType().equals(expectedType)) {
            throw new Exception("Type mismatch");
        }
        
        return evaluation;
    }

    private HyphenEvaluationContext enrichContext(EvaluationContext context) {
        HyphenEvaluationContext hyphenContext = new HyphenEvaluationContext(context);
        hyphenContext.setApplication(options.getApplication());
        hyphenContext.setEnvironment(options.getEnvironment());
        
        if (hyphenContext.getTargetingKey() == null) {
            String targetingKey = generateTargetingKey(hyphenContext);
            hyphenContext.setTargetingKey(targetingKey);
        }
        
        return hyphenContext;
    }

    private String generateTargetingKey(HyphenEvaluationContext context) {
        if (context.getTargetingKey() != null) {
            return context.getTargetingKey();
        }
        if (context.getUser() != null && context.getUser().getId() != null) {
            return context.getUser().getId();
        }
        return String.format("%s-%s-%s", 
            options.getApplication(), 
            options.getEnvironment(), 
            Integer.toHexString((int)(Math.random() * 1000000))
        );
    }

    private void validateContext(EvaluationContext context) {
        if (context == null) {
            throw new IllegalArgumentException("Evaluation context is required");
        }
    }
}
