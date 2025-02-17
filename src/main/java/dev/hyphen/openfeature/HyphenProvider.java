package dev.hyphen.openfeature;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.hyphen.openfeature.hook.TelemetryHook;
import dev.hyphen.openfeature.model.HyphenEvaluation;
import dev.hyphen.openfeature.model.EvaluationResponse;
import dev.openfeature.sdk.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class HyphenProvider implements FeatureProvider {
    private static final Logger logger = LoggerFactory.getLogger(HyphenProvider.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final HyphenClient client;
    private final HyphenProviderOptions options;

    public HyphenProvider(String publicKey, HyphenProviderOptions options) {
        this.client = new HyphenClient(publicKey, options);
        this.options = options;
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
        try {
            EvaluationResponse response = client.evaluate(ctx);
            HyphenEvaluation evaluation = response.getToggle(key);
            
            if (evaluation == null) {
                return ProviderEvaluation.<Boolean>builder()
                    .value(defaultValue)
                    .reason(Reason.ERROR.toString())
                    .errorCode(ErrorCode.FLAG_NOT_FOUND)
                    .build();
            }

            if (evaluation.getErrorMessage() != null) {
                return ProviderEvaluation.<Boolean>builder()
                    .value(defaultValue)
                    .reason(Reason.ERROR.toString())
                    .errorCode(ErrorCode.GENERAL)
                    .errorMessage(evaluation.getErrorMessage())
                    .build();
            }

            if (!"boolean".equals(evaluation.getType())) {
                return ProviderEvaluation.<Boolean>builder()
                    .value(defaultValue)
                    .reason(Reason.ERROR.toString())
                    .errorCode(ErrorCode.TYPE_MISMATCH)
                    .build();
            }

            boolean value = Boolean.parseBoolean(evaluation.getValue());
            return ProviderEvaluation.<Boolean>builder()
                .value(value)
                .variant(evaluation.getVariant())
                .reason(evaluation.getReason() != null ? evaluation.getReason() : Reason.TARGETING_MATCH.toString())
                .build();
        } catch (Exception e) {
            logger.error("Error evaluating boolean flag: " + key, e);
            return ProviderEvaluation.<Boolean>builder()
                .value(defaultValue)
                .reason(Reason.ERROR.toString())
                .build();
        }
    }

    @Override
    public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx) {
        try {
            EvaluationResponse response = client.evaluate(ctx);
            HyphenEvaluation evaluation = response.getToggle(key);
            
            if (evaluation == null) {
                return ProviderEvaluation.<String>builder()
                    .value(defaultValue)
                    .reason(Reason.ERROR.toString())
                    .errorCode(ErrorCode.FLAG_NOT_FOUND)
                    .build();
            }

            if (evaluation.getErrorMessage() != null) {
                return ProviderEvaluation.<String>builder()
                    .value(defaultValue)
                    .reason(Reason.ERROR.toString())
                    .errorCode(ErrorCode.GENERAL)
                    .errorMessage(evaluation.getErrorMessage())
                    .build();
            }

            if (!"string".equals(evaluation.getType())) {
                return ProviderEvaluation.<String>builder()
                    .value(defaultValue)
                    .reason(Reason.ERROR.toString())
                    .errorCode(ErrorCode.TYPE_MISMATCH)
                    .build();
            }

            return ProviderEvaluation.<String>builder()
                .value(evaluation.getValue())
                .variant(evaluation.getVariant())
                .reason(evaluation.getReason() != null ? evaluation.getReason() : Reason.TARGETING_MATCH.toString())
                .build();
        } catch (Exception e) {
            logger.error("Error evaluating string flag: " + key, e);
            return ProviderEvaluation.<String>builder()
                .value(defaultValue)
                .reason(Reason.ERROR.toString())
                .build();
        }
    }

    @Override
    public ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue, EvaluationContext ctx) {
        try {
            EvaluationResponse response = client.evaluate(ctx);
            HyphenEvaluation evaluation = response.getToggle(key);
            
            if (evaluation == null) {
                return ProviderEvaluation.<Integer>builder()
                    .value(defaultValue)
                    .reason(Reason.ERROR.toString())
                    .errorCode(ErrorCode.FLAG_NOT_FOUND)
                    .build();
            }

            if (evaluation.getErrorMessage() != null) {
                return ProviderEvaluation.<Integer>builder()
                    .value(defaultValue)
                    .reason(Reason.ERROR.toString())
                    .errorCode(ErrorCode.GENERAL)
                    .errorMessage(evaluation.getErrorMessage())
                    .build();
            }

            if (!"number".equals(evaluation.getType())) {
                return ProviderEvaluation.<Integer>builder()
                    .value(defaultValue)
                    .reason(Reason.ERROR.toString())
                    .errorCode(ErrorCode.TYPE_MISMATCH)
                    .build();
            }

            return ProviderEvaluation.<Integer>builder()
                .value(Integer.parseInt(evaluation.getValue()))
                .variant(evaluation.getVariant())
                .reason(evaluation.getReason() != null ? evaluation.getReason() : Reason.TARGETING_MATCH.toString())
                .build();
        } catch (Exception e) {
            logger.error("Error evaluating integer flag: " + key, e);
            return ProviderEvaluation.<Integer>builder()
                .value(defaultValue)
                .reason(Reason.ERROR.toString())
                .build();
        }
    }

    @Override
    public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext ctx) {
        try {
            EvaluationResponse response = client.evaluate(ctx);
            HyphenEvaluation evaluation = response.getToggle(key);
            
            if (evaluation == null) {
                return ProviderEvaluation.<Double>builder()
                    .value(defaultValue)
                    .reason(Reason.ERROR.toString())
                    .errorCode(ErrorCode.FLAG_NOT_FOUND)
                    .build();
            }

            if (evaluation.getErrorMessage() != null) {
                return ProviderEvaluation.<Double>builder()
                    .value(defaultValue)
                    .reason(Reason.ERROR.toString())
                    .errorCode(ErrorCode.GENERAL)
                    .errorMessage(evaluation.getErrorMessage())
                    .build();
            }

            if (!"number".equals(evaluation.getType())) {
                return ProviderEvaluation.<Double>builder()
                    .value(defaultValue)
                    .reason(Reason.ERROR.toString())
                    .errorCode(ErrorCode.TYPE_MISMATCH)
                    .build();
            }

            return ProviderEvaluation.<Double>builder()
                .value(Double.parseDouble(evaluation.getValue()))
                .variant(evaluation.getVariant())
                .reason(evaluation.getReason() != null ? evaluation.getReason() : Reason.TARGETING_MATCH.toString())
                .build();
        } catch (Exception e) {
            logger.error("Error evaluating double flag: " + key, e);
            return ProviderEvaluation.<Double>builder()
                .value(defaultValue)
                .reason(Reason.ERROR.toString())
                .build();
        }
    }

    @Override
    public ProviderEvaluation<Value> getObjectEvaluation(String key, Value defaultValue, EvaluationContext ctx) {
        try {
            EvaluationResponse response = client.evaluate(ctx);
            HyphenEvaluation evaluation = response.getToggle(key);
            
            if (evaluation == null) {
                return ProviderEvaluation.<Value>builder()
                    .value(defaultValue)
                    .reason(Reason.ERROR.toString())
                    .errorCode(ErrorCode.FLAG_NOT_FOUND)
                    .build();
            }

            if (evaluation.getErrorMessage() != null) {
                return ProviderEvaluation.<Value>builder()
                    .value(defaultValue)
                    .reason(Reason.ERROR.toString())
                    .errorCode(ErrorCode.GENERAL)
                    .errorMessage(evaluation.getErrorMessage())
                    .build();
            }

            if (!"object".equals(evaluation.getType())) {
                return ProviderEvaluation.<Value>builder()
                    .value(defaultValue)
                    .reason(Reason.ERROR.toString())
                    .errorCode(ErrorCode.TYPE_MISMATCH)
                    .build();
            }

            Map<String, Object> objectValue = objectMapper.readValue(evaluation.getValue(), Map.class);
            return ProviderEvaluation.<Value>builder()
                .value(new Value(objectValue))
                .variant(evaluation.getVariant())
                .reason(evaluation.getReason() != null ? evaluation.getReason() : Reason.TARGETING_MATCH.toString())
                .build();
        } catch (Exception e) {
            logger.error("Error evaluating object flag: " + key, e);
            return ProviderEvaluation.<Value>builder()
                .value(defaultValue)
                .reason(Reason.ERROR.toString())
                .build();
        }
    }

    public void sendTelemetry(String key, FlagEvaluationDetails<?> details) {
        HyphenEvaluation evaluation = new HyphenEvaluation();
        evaluation.setKey(key);
        evaluation.setValue(String.valueOf(details.getValue()));
        evaluation.setType("unknown");
        evaluation.setReason(details.getReason());
        evaluation.setErrorMessage(details.getErrorMessage());
        evaluation.setVariant(details.getVariant());
        
        client.postTelemetry(key, evaluation);
    }
}
