package kr.farmily.api.profile.dto;

import jakarta.validation.constraints.NotNull;
import kr.farmily.api.profile.domain.BlockType;

import java.util.Map;

public record CreateBlockRequest(
        @NotNull BlockType blockType,
        Map<String, Object> payload
) {}
