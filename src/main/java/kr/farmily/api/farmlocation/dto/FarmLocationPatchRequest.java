package kr.farmily.api.farmlocation.dto;

import jakarta.validation.constraints.Size;

public record FarmLocationPatchRequest(
        @Size(max = 50) String label,
        @Size(max = 200) String address
) {}
