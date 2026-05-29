package kr.farmily.api.subscription.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "farmily.portone")
public record PortOneProperties(
        String apiKey,
        String apiSecret,
        String impCode,
        String webhookSecret,
        Boolean testMode
) {}
