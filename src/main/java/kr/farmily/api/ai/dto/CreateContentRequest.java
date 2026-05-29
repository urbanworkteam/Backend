package kr.farmily.api.ai.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kr.farmily.api.ai.domain.Platform;

import java.util.List;

public record CreateContentRequest(
        @NotNull Platform platform,
        @NotNull Long cropId,
        List<Long> diaryIds,
        @Size(max = 200) String keywords,
        @Size(max = 3) List<String> extraPhotoKeys
) {}
