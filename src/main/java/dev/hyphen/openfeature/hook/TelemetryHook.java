package dev.hyphen.openfeature.hook;

import dev.hyphen.openfeature.HyphenProvider;
import dev.openfeature.sdk.Hook;
import dev.openfeature.sdk.HookContext;
import dev.openfeature.sdk.FlagEvaluationDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class TelemetryHook implements Hook {
    private static final Logger logger = LoggerFactory.getLogger(TelemetryHook.class);
    private final HyphenProvider provider;

    public TelemetryHook(HyphenProvider provider) {
        this.provider = provider;
    }

    @Override
    public void after(HookContext context, FlagEvaluationDetails details, Map hints) {
        try {
            provider.sendTelemetry(context.getFlagKey(), details);
        } catch (Exception e) {
            logger.warn("Failed to send telemetry", e);
        }
    }
}
