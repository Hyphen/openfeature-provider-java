package dev.hyphen.openfeature;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.hyphen.openfeature.hook.TelemetryHook;
import dev.hyphen.openfeature.model.HyphenEvaluation;
import dev.hyphen.openfeature.model.EvaluationResponse;
import dev.openfeature.sdk.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class HyphenProvider implements FeatureProvider {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final HyphenClient client;
    private final HyphenProviderOptions options;

    public HyphenProvider(String publicKey, HyphenProviderOptions options) {
        this.client = new HyphenClient(publicKey, options);
        this.options = options;
    }

    public HyphenClient getClient() {
        return client;
    }

    public HyphenProviderOptions getOptions() {
        return options;
    }

    @Override
    public Metadata getMetadata() {
        return () -> "hyphen-provider-java";
    }

    @Override
    public List<Hook> getProviderHooks() {
        if (options.isEnableToggleUsage()) {
            return Collections.singletonList(new TelemetryHook(this));
        }
        return new ArrayList<>();
    }

    @Override
    public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext ctx) {
        return evaluateFlag(key, defaultValue, ctx, "boolean", Boolean::parseBoolean);
    }

    @Override
    public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx) {
        return evaluateFlag(key, defaultValue, ctx, "string", value -> value);
    }

    @Override
    public ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue, EvaluationContext ctx) {
        return evaluateFlag(key, defaultValue, ctx, "number", Integer::parseInt);
    }

    @Override
    public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext ctx) {
        return evaluateFlag(key, defaultValue, ctx, "number", Double::parseDouble);
    }

    @Override
    public ProviderEvaluation<Value> getObjectEvaluation(String key, Value defaultValue, EvaluationContext ctx) {
        return evaluateFlag(key, defaultValue, ctx, "object", value -> {
            try {
                // First parse the JSON string into a Map
                Map<String, Object> objectValue = objectMapper.readValue(value, Map.class);
                // Then convert the Map to a Value object using objectToValue
                return Value.objectToValue(objectValue);
            } catch (Exception e) {
                throw new RuntimeException("Error parsing object value", e);
            }
        });
    }

    private <T> ProviderEvaluation<T> evaluateFlag(
            String key,
            T defaultValue,
            EvaluationContext ctx,
            String expectedType,
            Function<String, T> valueConverter) {
        try {
            EvaluationResponse response = client.evaluate(ctx);
            HyphenEvaluation evaluation = response.getToggle(key);

            if (evaluation == null) {
                return buildErrorEvaluation(defaultValue, ErrorCode.FLAG_NOT_FOUND);
            }

            if (evaluation.getErrorMessage() != null) {
                return buildErrorEvaluation(defaultValue, ErrorCode.GENERAL, evaluation.getErrorMessage());
            }

            if (!expectedType.equals(evaluation.getType())) {
                return buildErrorEvaluation(defaultValue, ErrorCode.TYPE_MISMATCH);
            }

            T value = valueConverter.apply(evaluation.getValue());
            return buildSuccessEvaluation(value, evaluation);
        } catch (Exception e) {
            return buildErrorEvaluation(defaultValue, null);
        }
    }

    private <T> ProviderEvaluation<T> buildErrorEvaluation(T defaultValue, ErrorCode code) {
        return buildErrorEvaluation(defaultValue, code, null);
    }

    private <T> ProviderEvaluation<T> buildErrorEvaluation(T defaultValue, ErrorCode code, String message) {
        var builder = ProviderEvaluation.<T>builder()
                .value(defaultValue)
                .reason(Reason.ERROR.toString());

        if (code != null) {
            builder.errorCode(code);
        }
        if (message != null) {
            builder.errorMessage(message);
        }
        return builder.build();
    }

    private <T> ProviderEvaluation<T> buildSuccessEvaluation(T value, HyphenEvaluation evaluation) {
        return ProviderEvaluation.<T>builder()
                .value(value)
                .variant(evaluation.getVariant())
                .reason(evaluation.getReason() != null ? evaluation.getReason() : Reason.TARGETING_MATCH.toString())
                .build();
    }
}
