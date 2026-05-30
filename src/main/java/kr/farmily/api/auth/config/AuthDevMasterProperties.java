package kr.farmily.api.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "farmily.auth.dev-master")
public record AuthDevMasterProperties(
        boolean enabled,
        String code,
        String kakaoId,
        String nickname,
        String email
) {
    public AuthDevMasterProperties {
        if (code == null || code.isBlank()) code = "DEV_MASTER";
        if (kakaoId == null || kakaoId.isBlank()) kakaoId = "dev-master-1";
        if (nickname == null || nickname.isBlank()) nickname = "DevMaster";
        if (email == null || email.isBlank()) email = "dev-master@local";
    }
}
