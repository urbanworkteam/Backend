package kr.farmily.api.ai.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateResultRequest(
        @Size(max = 2200) String caption,
        @Size(max = 30) List<@Pattern(regexp = "^#[a-zA-Z0-9가-힣_]{1,30}$") String> hashtags
) {}
