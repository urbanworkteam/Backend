package kr.farmily.api.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "farmily.s3")
public record S3Properties(
        String bucket,
        String region,
        String endpoint,
        String accessKey,
        String secretKey,
        String cdnBaseUrl,
        Long presignTtlSeconds,
        Long displayTtlSeconds
) {}
