package kr.farmily.api.subscription.dto;

import jakarta.validation.constraints.NotBlank;

public record CheckoutStartRequest(@NotBlank String plan) {}
