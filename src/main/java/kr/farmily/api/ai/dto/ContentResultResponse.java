package kr.farmily.api.ai.dto;

import kr.farmily.api.ai.domain.Platform;

import java.util.List;
import java.util.Map;

public record ContentResultResponse(
        Platform platform,
        List<String> cardImageUrls,
        String caption,
        List<String> hashtags,
        StoreMeta storeMeta
) {

    public record StoreMeta(
            String brix,
            String harvestPolicy,
            String farmingYears,
            List<String> reasonsToBuy,
            Map<String, String> productInfo,
            Integer price
    ) {}
}
