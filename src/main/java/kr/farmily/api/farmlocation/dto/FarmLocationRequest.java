package kr.farmily.api.farmlocation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FarmLocationRequest(
        @NotBlank @Size(max = 50) String label,
        @NotBlank @Size(max = 200) String address
) {}
