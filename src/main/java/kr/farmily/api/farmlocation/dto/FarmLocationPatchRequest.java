package kr.farmily.api.farmlocation.dto;

import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record FarmLocationPatchRequest(
        @Size(max = 50) String label,
        @Size(max = 200) String address,
        BigDecimal lat,
        BigDecimal lng
) {}
