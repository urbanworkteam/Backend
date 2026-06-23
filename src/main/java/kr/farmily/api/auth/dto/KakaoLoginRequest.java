package kr.farmily.api.auth.dto;

import jakarta.validation.constraints.AssertTrue;

/**
 * 카카오 로그인 요청.
 * - 웹: {@code code} (+ optional {@code redirectUri}) — 서버가 토큰 교환을 수행
 * - 모바일 네이티브 SDK: {@code accessToken} — 서버는 토큰 교환을 건너뛰고 프로필만 조회
 * 둘 중 하나는 반드시 있어야 한다.
 */
public record KakaoLoginRequest(
        String code,
        String redirectUri,
        String accessToken
) {
    public boolean hasCode() {
        return code != null && !code.isBlank();
    }

    public boolean hasAccessToken() {
        return accessToken != null && !accessToken.isBlank();
    }

    @AssertTrue(message = "code 또는 accessToken 중 하나는 필수입니다")
    public boolean isCredentialPresent() {
        return hasCode() || hasAccessToken();
    }
}
