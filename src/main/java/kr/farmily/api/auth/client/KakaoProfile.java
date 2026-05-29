package kr.farmily.api.auth.client;

public record KakaoProfile(
        String kakaoId,
        String nickname,
        String email
) {}
