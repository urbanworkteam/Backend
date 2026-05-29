package kr.farmily.api.auth.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "farmily.kakao")
public record KakaoProperties(
        String clientId,
        String clientSecret,
        String redirectUri,
        String adminKey,
        String localRestKey
) {}
