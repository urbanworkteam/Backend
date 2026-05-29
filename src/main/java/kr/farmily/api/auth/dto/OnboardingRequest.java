package kr.farmily.api.auth.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OnboardingRequest(
        @NotBlank @Size(min = 3, max = 30) String handle,
        @NotBlank @Size(max = 50) String farmDisplayName,
        @Size(max = 50) String region,
        @Size(max = 100) String farmingMethod,
        @NotNull @Size(min = 1, max = 20) @Valid List<CropEntry> crops,
        @NotNull @Valid FarmLocationEntry farmLocation
) {

    public record CropEntry(
            @NotBlank @Size(max = 50) String name,
            String colorHex,
            String stage
    ) {}

    public record FarmLocationEntry(
            @NotBlank @Size(max = 50) String label,
            @NotBlank @Size(max = 200) String address
    ) {}
}
