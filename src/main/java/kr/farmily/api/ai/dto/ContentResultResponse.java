package kr.farmily.api.ai.dto;

import kr.farmily.api.ai.domain.Platform;

import java.util.List;

public record ContentResultResponse(
        Platform platform,
        List<String> cardImageUrls,
        String caption,
        List<String> hashtags
) {}
