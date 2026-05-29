package kr.farmily.api.crop.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CropPatchRequest(
        @Size(max = 50) String name,
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String colorHex,
        @Size(max = 100) String stage
) {}
