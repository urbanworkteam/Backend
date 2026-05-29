package kr.farmily.api.profile.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.Map;

public record BlockReorderRequest(@NotEmpty @Valid List<BlockOrder> blocks) {

    public record BlockOrder(Long id, Integer sortOrder, Boolean visible, Map<String, Object> payload) {}
}
