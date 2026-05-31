package kr.farmily.api.farmlocation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record FarmLocationRequest(
        @NotBlank @Size(max = 50) String label,
        @NotBlank @Size(max = 200) String address,
        @NotNull BigDecimal lat,
        @NotNull BigDecimal lng
) {}
