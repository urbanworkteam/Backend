package kr.farmily.api.ai.dto;

import jakarta.validation.constraints.Size;

import java.util.List;

public record RegenerateRequest(
        @Size(max = 200) String keywords,
        @Size(max = 3) List<String> extraPhotoKeys
) {}
