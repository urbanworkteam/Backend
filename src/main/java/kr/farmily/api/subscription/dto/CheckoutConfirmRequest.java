package kr.farmily.api.subscription.dto;

import jakarta.validation.constraints.NotBlank;

public record CheckoutConfirmRequest(
        @NotBlank String impUid,
        @NotBlank String merchantUid
) {}
