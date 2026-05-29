package kr.farmily.api.ai.dto;

import kr.farmily.api.ai.domain.Platform;

import java.time.OffsetDateTime;

public record HistoryItemDto(
        Long id,
        Platform platform,
        OffsetDateTime createdAt,
        String thumbnailUrl,
        String caption
) {}
