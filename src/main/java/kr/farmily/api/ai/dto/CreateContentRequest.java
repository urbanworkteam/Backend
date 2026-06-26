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
        // 스마트스토어 상세 카드는 이미지 슬롯 7개 → 추가 사진 최대 7장 허용 (인스타는 프론트에서 3장으로 제한)
        @Size(max = 7) List<String> extraPhotoKeys
) {}
