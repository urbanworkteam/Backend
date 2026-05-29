package kr.farmily.api.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record KakaoLoginRequest(
        @NotBlank String code,
        String redirectUri
) {}
