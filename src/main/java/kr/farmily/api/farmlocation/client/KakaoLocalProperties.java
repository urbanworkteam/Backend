package kr.farmily.api.farmlocation.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "farmily.kakao")
public record KakaoLocalProperties(String localRestKey) {}
