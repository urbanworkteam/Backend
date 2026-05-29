package kr.farmily.api.notification.dto;

import jakarta.validation.constraints.NotBlank;

public record DeleteTokenRequest(@NotBlank String token) {}
