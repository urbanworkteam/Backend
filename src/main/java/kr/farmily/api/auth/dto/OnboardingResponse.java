package kr.farmily.api.auth.dto;

import kr.farmily.api.crop.dto.CropResponse;
import kr.farmily.api.farmlocation.dto.FarmLocationResponse;

import java.util.List;

public record OnboardingResponse(
        UserSummary user,
        FarmProfileSummary farmProfile,
        List<CropResponse> crops,
        FarmLocationResponse farmLocation
) {

    public record UserSummary(Long id, String name, String handle) {}

    public record FarmProfileSummary(String farmName, String region, String farmingMethod) {}
}
