package kr.farmily.api.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "farmily.ai")
public record AiProperties(
        String provider,
        String awsRegion,
        String bedrockAgentId,
        String bedrockAgentAliasId,
        Long invokeTimeoutSeconds,
        String resultBucket
) {}
