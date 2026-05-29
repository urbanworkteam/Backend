package kr.farmily.api.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterTokenRequest(
        @NotBlank @Pattern(regexp = "IOS|ANDROID") String platform,
        @NotBlank String token
) {}
