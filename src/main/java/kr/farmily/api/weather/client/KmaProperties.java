package kr.farmily.api.weather.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "farmily.kma")
public record KmaProperties(
        String serviceKey,
        String baseUrl
) {}
