package kr.farmily.api.crop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CropRequest(
        @NotBlank @Size(max = 50) String name,
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "color must be hex like #FF5A5A") String colorHex,
        @Size(max = 100) String stage
) {}
